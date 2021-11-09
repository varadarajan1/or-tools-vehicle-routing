package models.request;

import lombok.*;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@EqualsAndHashCode
public class Location {
    private Double latitude;
    private Double longitude;

    public String toString() {
        return this.latitude + "," + this.longitude;
    }
}
