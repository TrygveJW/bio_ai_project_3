package no.woldseth.image;

import java.lang.Math;

public class Pixel {
    public final int red;
    public final int green;
    public final int blue;

    public PixelGroup pixelsGroup;
    public boolean updated;

    public Pixel(int red, int green, int blue) {
        this.red   = red;
        this.green = green;
        this.blue  = blue;
        this.updated = true;
    }

}
