package no.woldseth.image;

import lombok.Getter;
import lombok.Setter;

@Getter
public class Pixel {

    private final int id;

    private final int x;

    private final int y;

    private final int red;

    private final int green;

    private final int blue;

    private final boolean is_edge;

    public Pixel(int id, int x, int y, int red, int green, int blue, boolean is_edge) {
        this.id      = id;
        this.x       = x;
        this.y       = y;
        this.red     = red;
        this.green   = green;
        this.blue    = blue;
        this.is_edge = is_edge;
    }


}
