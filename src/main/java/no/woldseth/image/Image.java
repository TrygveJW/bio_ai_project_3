package no.woldseth.image;

import no.woldseth.evolutionary_algorithm.representation.Phenotype;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.awt.image.DataBufferByte;
import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Arrays;
import java.util.HashMap;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

public class Image {
    private HashMap<Point, Pixel> imageMap;

    public final int width;
    public final int height;

    public final int numPixels;
    private final boolean hasAlphaChannel;
    private final int pixelLength;

    private File imageFile;
    public final Pixel[] pixleArray;

    public Image(File imageFile) throws IOException {
        BufferedImage image = ImageIO.read(imageFile);
        this.imageFile = imageFile;

        this.width     = image.getWidth();
        this.height    = image.getHeight();
        this.numPixels = width * height;

        this.hasAlphaChannel = image.getAlphaRaster() != null;
        this.pixelLength     = hasAlphaChannel ? 4 : 3;

        this.pixleArray = this.formatToPixelArray(image);

    }

    public Pixel getPixel(int x, int y) {
        return pixleArray[(y * width) + x];
    }

    public Pixel getPixel(int id) {
        return pixleArray[id];
    }

    public Point getIdAsPoint(int id) {
        int y = Math.floorDiv(id, width);
        int x = id - (y * width);
        return new Point(x, y);
    }

    public int getPointAsId(int x, int y) {
        return y * width + x;
    }

    public double getRgbDelta(int x1, int y1, int x2, int y2) {
        Pixel p1 = this.getPixel(x1, y1);
        Pixel p2 = this.getPixel(x2, y2);

        return calulateRgbDeltaFromPixels(p1, p2);
    }


    public double getRgbDelta(int id1, int id2) {
        Pixel p1 = this.getPixel(id1);
        Pixel p2 = this.getPixel(id2);

        return calulateRgbDeltaFromPixels(p1, p2);
    }

    private double calulateRgbDeltaFromPixels(Pixel p1, Pixel p2) {
        return Math.sqrt(Math.pow(p1.getRed() - p2.getRed(), 2) + Math.pow(p1.getGreen() - p2.getGreen(), 2) + Math.pow(
                p1.getBlue() - p2.getBlue(),
                2
                                                                                                                       ));
    }

    private Pixel[] formatToPixelArray(BufferedImage image) {
        Color[][] pixels = IntStream.range(0, this.height).parallel().mapToObj(y -> {
            Color[] pixelRow = new Color[this.width];
            for (int i = 0; i < this.width; i++) {
                Color rgb = new Color(image.getRGB(i, y));
                pixelRow[i] = rgb;
            }
            return pixelRow;
        }).toArray(value -> new Color[this.height][this.width]);

        Pixel[] pixelArray = new Pixel[this.width * this.height];


        for (int row = 0; row < height; row++) {
            for (int col = 0; col < width; col++) {
                var rgb    = pixels[row][col];
                var isEdge = ((col == 0 || row == 0) || (col == width - 1 || row == height - 1));
                Pixel pixel = new Pixel((row * width) + col,
                                        col,
                                        row,
                                        rgb.getRed(),
                                        rgb.getBlue(),
                                        rgb.getGreen(),
                                        isEdge
                );
                pixelArray[(row * width) + col] = pixel;

            }

        }
        return pixelArray;
        //        Arrays.stream(pixels).forEach(ints -> System.out.println(Arrays.deepToString(ints)));
    }

    public void saveGroundTruthImage(Phenotype phenotype, String filePath) {

    }

    public void savePixelGroupEdgeDisplay(Phenotype phenotype, String filePath) {
        try {
            Color borderColor = Color.magenta;

            BufferedImage bufferedImage = ImageIO.read(imageFile);

            IntStream.range(0, this.height).forEach(value -> {
                for (int i = 0; i < this.width - 1; i++) {
                    int g1 = phenotype.pixelGroupList[this.getPointAsId(i, value)];
                    int g2 = phenotype.pixelGroupList[this.getPointAsId(i + 1, value)];

                    if (g1 != g2) {
                        bufferedImage.setRGB(i, value, borderColor.getRGB());
                    }
                }
            });

            IntStream.range(0, this.width).forEach(value -> {
                for (int i = 0; i < this.height - 1; i++) {
                    int g1 = phenotype.pixelGroupList[this.getPointAsId(value, i)];
                    int g2 = phenotype.pixelGroupList[this.getPointAsId(value, i + 1)];

                    if (g1 != g2) {
                        bufferedImage.setRGB(value, i, borderColor.getRGB());
                    }
                }
            });

            File outFp = new File(filePath + ".png");
            ImageIO.write(bufferedImage, "png", outFp);

        } catch (Exception e) {

        }
    }

    public void savePhenotypeAsCsv(Phenotype phenotype, String filepath) {
        int[][] edgeMap = new int[this.height][this.width];
        try {
            Color borderColor = Color.magenta;

            BufferedImage bufferedImage = ImageIO.read(imageFile);

            IntStream.range(0, this.height).forEach(value -> {
                for (int i = 0; i < this.width - 1; i++) {
                    int g1 = phenotype.pixelGroupList[this.getPointAsId(i, value)];
                    int g2 = phenotype.pixelGroupList[this.getPointAsId(i + 1, value)];

                    edgeMap[value][i]     = (g1 != g2) ? 0 : 255;
                    //edgeMap[value][i + 1] = (g1 != g2) ? 0 : 255;
                }
            });

            IntStream.range(0, this.width).forEach(value -> {
                for (int i = 0; i < this.height - 1; i++) {
                    int g1 = phenotype.pixelGroupList[this.getPointAsId(value, i)];
                    int g2 = phenotype.pixelGroupList[this.getPointAsId(value, i + 1)];

                    edgeMap[i][value]     = (g1 != g2) ? 0 : 255;
                    //edgeMap[i + 1][value] = (g1 != g2) ? 0 : 255;
                }
            });

            for (int y = 0; y < this.height; y++) {
                for (int x = 0; x < this.width; x++) {
                    if (y == 0 || y == this.height - 1) {
                        edgeMap[y][x] = 0;
                    } else if (x == 0 || x == this.width - 1) {
                        edgeMap[y][x] = 0;
                    }
                }

            }
            File csvOutputFile = new File(filepath + ".txt");
            try (PrintWriter pw = new PrintWriter(csvOutputFile)) {
                for (int y = 0; y < this.height; y++) {
                    pw.println(Arrays.stream(edgeMap[y]).mapToObj(Integer::toString).collect(Collectors.joining(",")));
                }
            }
            //System.out.println(csvOutputFile.exists());

        } catch (Exception e) {
            e.printStackTrace();

        }
    }
}
