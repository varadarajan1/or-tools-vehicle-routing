package models.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class Vehicle {
    private String vehicleId;
    private List<Seat> capacityConfigurations;
    private Location currentLocation;
}
