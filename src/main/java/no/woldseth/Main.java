package no.woldseth;

import no.woldseth.evolutionary_algorithm.Criterion;
import no.woldseth.evolutionary_algorithm.Initialization;
import no.woldseth.evolutionary_algorithm.NSGA2;
import no.woldseth.evolutionary_algorithm.SimpleGenteticAlgorithm;
import no.woldseth.evolutionary_algorithm.representation.Genotype;
import no.woldseth.evolutionary_algorithm.representation.Phenotype;
import no.woldseth.image.Image;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.awt.image.DataBufferByte;
import java.io.*;
import java.lang.reflect.Array;
import java.util.Arrays;

public class Main {

    private static DebugLogger dbl = new DebugLogger(true);

    public static void main(String[] args) {
        /*
        rullesumm på init så kan man klippe litt et par gang som gjør den bedre
         */

        File imagefp = new File("./training_images/training_images/86016/Test image.jpg");

        //        File imagefp = new File("./training_images/training_images/118035/Test image.jpg");
        //        File imagefp = new File("./training_images/training_images/test/test_img.png");
        //        File imagefp = new File("./training_images/training_images/test/test_img_shitty_compressed.jpg");
        try {
            Image                   image             = new Image(imagefp);
            SimpleGenteticAlgorithm genteticAlgorithm = new SimpleGenteticAlgorithm(50, 1, 2, 0.2, 0.7, image);
            NSGA2 peeop = new NSGA2(50, 1, 2, 0.2, 0.7, image);
            peeop.runGenalg(300);

            //var ph = genteticAlgorithm.runGenalg(300);

            //image.savePixelGroupEdgeDisplay(ph);


        } catch (FileNotFoundException e) {
            throw new RuntimeException(e);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

    }
}
