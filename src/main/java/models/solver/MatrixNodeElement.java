package models.solver;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import models.request.Location;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class MatrixNodeElement {
    private Long durationInSeconds;
    private Long distanceInMeters;
}
