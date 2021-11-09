package models.solver;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import models.request.Location;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class Node {
    private Location location;
    private NodeType nodeType;
}
