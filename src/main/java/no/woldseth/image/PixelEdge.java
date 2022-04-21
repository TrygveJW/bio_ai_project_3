package no.woldseth.image;


import java.util.HashMap;
import java.util.Objects;

/**
 * Defines a edge between two pixel, the edge is defined as always
 * going ether from the bottom upwards or from the left and rightwards
 */
public class PixelEdge {


    public Point from;
    public Point to;

    public PixelEdge connectedTop;
    public PixelEdge connectedBottom;

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        PixelEdge pixelEdge = (PixelEdge) o;
        return Objects.equals(from, pixelEdge.from) && Objects.equals(to, pixelEdge.to);
    }

    @Override
    public int hashCode() {
        return Objects.hash(from, to);
    }
}
