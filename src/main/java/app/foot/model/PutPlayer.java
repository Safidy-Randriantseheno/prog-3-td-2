package app.foot.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

@Builder
@AllArgsConstructor
@Data
public class PutPlayer {
    private String name;
    private int number;
    private Integer team;
}

