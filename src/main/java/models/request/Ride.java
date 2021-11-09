package models.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.ZonedDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class Ride {
    private String rideId;
    private ZonedDateTime creationTime;
    private CapacityConsumption capacityConsumption;
    private PickUp pickup;
    private Drop dropOff;
}
