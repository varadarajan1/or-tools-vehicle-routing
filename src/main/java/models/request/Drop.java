package models.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class Drop {
    private Location location;
    private Long handlingDurationInSeconds;
    private DesiredHandlingStartTime desiredHandlingStartTime;
}
