import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.ImmutableList;
import com.google.maps.DistanceMatrixApi;
import com.google.maps.GeoApiContext;
import com.google.maps.model.DistanceMatrixElement;
import com.google.maps.model.DistanceMatrixRow;
import com.google.maps.model.TravelMode;
import com.google.maps.model.Unit;
import com.google.ortools.Loader;
import com.google.ortools.constraintsolver.*;
import com.google.protobuf.Duration;
import models.request.*;
import models.solver.MatrixNode;
import models.solver.MatrixNodeElement;
import models.solver.Node;
import models.solver.NodeType;

import java.util.*;
import java.util.logging.Logger;
import java.util.stream.Collectors;

/*
    Objective - Reduce distance/time travelled by each vehicle
    constraints - capacity, time window, pickup and drop
 */


/*
Each node
 -> index
 -> time windows for each node
 -> demand

Distance and Time Matrix
   -> Should include all points
   -> Should include vehicle location as nodes as well
   -> Add a row for depot

Vehicle
 -> Location
 -> Capacity

 Define time matrix
 Define demand at each node
 Define Tw for each node

Penalties for dropping and set as rejection

Other notes:
GlobalCostCoefficient - 90minutes?
 */

public class Solver {
    private static final Logger logger = Logger.getLogger("Solver");
    private static final ObjectMapper objectMapper = new ObjectMapper();

    public static void main(String[] args) {
        objectMapper.findAndRegisterModules();
        try {
            var fileSource = Solver.class.getClassLoader().getResource("SymphonyRequest.json");
            var request = objectMapper.readValue(fileSource, Request.class);
            Solver solver = new Solver();
            solver.solve(request);
        } catch (Exception e) {
            logger.severe(e.getMessage());
        }
    }

    public void solve(Request request) {


        Loader.loadNativeLibraries();

        var vehicles = request.getVehicles();

        // Construct Node List, mark them as pickup, drop or vehicle start
        var rideRequestByPickUpPoint =
                request.getRides().stream().collect(Collectors.groupingBy(ride -> ride.getPickup().getLocation(), Collectors.toSet()));

        var rideRequestByDropPoint
                = request.getRides().stream().collect(Collectors.groupingBy(ride -> ride.getDropOff().getLocation(), Collectors.toSet()));

        var vehicleNodes = vehicles.stream().map(Vehicle::getCurrentLocation).collect(Collectors.toSet());

        var locations = new HashSet<>(vehicleNodes);
        locations.addAll(rideRequestByPickUpPoint.keySet());
        locations.addAll(rideRequestByDropPoint.keySet());

        var locationsMaster = ImmutableList.copyOf(locations);

        var vehicleStartPoints = vehicles.stream().map(Vehicle::getCurrentLocation).mapToInt(locationsMaster::indexOf).toArray();
        var vehicleCapacities = vehicles.stream().mapToLong(veh -> veh.getCapacityConfigurations().stream().map(Seat::getSeats).reduce(0, Integer::sum))
                .toArray();
        // Construct Distance/Time Matrix
        var distanceMatrix = this.computeDistanceMatrix(locationsMaster);
        var distanceTimeMatrix = Arrays.stream(distanceMatrix).map(row -> new MatrixNode(this.convertToNodeElementArray(row.elements)))
                .toArray(MatrixNode[]::new);

        RoutingIndexManager routingIndexManager = new RoutingIndexManager(distanceTimeMatrix.length, vehicles.size(), vehicleStartPoints, vehicleStartPoints);
        RoutingModel model = new RoutingModel(routingIndexManager);

        final int transitCallbackIndex = model.registerTransitCallback((index1, index2) -> {
            var from = routingIndexManager.indexToNode(index1);
            var to = routingIndexManager.indexToNode(index2);
            return distanceTimeMatrix[from].getElements()[to].getDurationInSeconds();
        });

        model.addDimension(transitCallbackIndex, // transit callback index
                0, // no slack
                Integer.MAX_VALUE, // vehicle maximum travel time
                false, // start cumul to zero
                "Time");
        RoutingDimension timingDimension = model.getMutableDimension("Time");

        // objective - minimize time between nodes
        model.setArcCostEvaluatorOfAllVehicles(transitCallbackIndex);

        final int demandCallbackIndex = model.registerUnaryTransitCallback((long fromIndex) -> {
            int fromNode = routingIndexManager.indexToNode(fromIndex);
            var location = locationsMaster.get(fromNode);
            if (!rideRequestByPickUpPoint.containsKey(location)) return 0;
            return rideRequestByPickUpPoint.get(location).size();
        });

        model.addDimensionWithVehicleCapacity(demandCallbackIndex, 0, vehicleCapacities, true, "Capacity");

        RoutingSearchParameters searchParameters =
                main.defaultRoutingSearchParameters()
                        .toBuilder()
                        .setFirstSolutionStrategy(FirstSolutionStrategy.Value.PATH_CHEAPEST_ARC)
                        .build();

        var solver = model.solver();
        for (var req :
                request.getRides()) {
            var pickUpIndex = locationsMaster.indexOf(req.getPickup().getLocation());
            var dropOffIndex = locationsMaster.indexOf(req.getDropOff().getLocation());
            var pickup = routingIndexManager.nodeToIndex(pickUpIndex);
            var dropOff = routingIndexManager.nodeToIndex(dropOffIndex);
            model.addPickupAndDelivery(pickup, dropOff);
            solver.addConstraint(solver.makeEquality(model.vehicleVar(pickup), model.vehicleVar(dropOff)));
            solver.addConstraint(solver.makeLessOrEqual(timingDimension.cumulVar(pickup), timingDimension.cumulVar(dropOff)));
        }

        long penalty = 1000;
        for (int i = 1; i < distanceTimeMatrix.length; ++i) {
            model.addDisjunction(new long[]{routingIndexManager.nodeToIndex(i)}, penalty);
        }

        // Solve the problem.
        Assignment solution = model.solveWithParameters(searchParameters);
        System.out.println(solution);
    }

    private MatrixNodeElement[] convertToNodeElementArray(DistanceMatrixElement[] elements) {
        return Arrays.stream(elements).map(this::convertToNodeElement).toArray(MatrixNodeElement[]::new);
    }

    private MatrixNodeElement convertToNodeElement(DistanceMatrixElement matrixElement) {
        return new MatrixNodeElement(matrixElement.duration.inSeconds, matrixElement.distance.inMeters);
    }

    public DistanceMatrixRow[] computeDistanceMatrix(ImmutableList<Location> origins) {
        GeoApiContext context = new GeoApiContext.Builder().apiKey("").build();

        var request = DistanceMatrixApi
                .getDistanceMatrix(context, origins.stream().map(Location::toString).toArray(String[]::new),
                        origins.stream().map(Location::toString).toArray(String[]::new)).units(Unit.IMPERIAL).mode(TravelMode.DRIVING);
        return request.awaitIgnoreError().rows;
    }
}
