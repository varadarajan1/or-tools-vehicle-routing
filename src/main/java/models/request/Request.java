package models.request;

import lombok.*;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class Request {
    @Singular
    private List<Ride> rides;
    @Singular
    private List<Vehicle> vehicles;
}
