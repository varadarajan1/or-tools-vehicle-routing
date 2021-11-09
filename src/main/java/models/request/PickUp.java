package models.request;

import lombok.*;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class PickUp {
    private Location location;
    private Long handlingDurationInSeconds;
    private DesiredHandlingStartTime desiredHandlingStartTime;
}
