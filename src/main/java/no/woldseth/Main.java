package no.woldseth;

import no.woldseth.evolutionary_algorithm.*;
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

        NSGA();
        //        SGA();
    }

    private static void NSGA() {

        File imagefp = new File("./training_images/training_images/176039/Test image.jpg");
        //        File imagefp = new File("./training_images/training_images/118035/Test image.jpg");
        //        File imagefp = new File("./training_images/training_images/86016/Test image.jpg");

        //        File imagefp = new File("./training_images/training_images/118035/Test image.jpg");
        //        File imagefp = new File("./training_images/training_images/test/test_img.png");
        //        File imagefp = new File("./training_images/training_images/test/test_img_shitty_compressed.jpg");
        try {
            Image image = new Image(imagefp);

                        //var a = Initialization.generateInitialGenome(image);
                        //image.savePixelGroupEdgeDisplay(new Phenotype(a, image), "./init_test_img");
                        //System.exit(0);

            NSGA2 peeop    = new NSGA2(25, 2, 2, 0.2, 0.7, image);
            var   skadoosh = peeop.runGenalg(50);
            int   counter  = 0;
            for (Phenotype p : skadoosh) {
                image.savePixelGroupEdgeDisplay(p, "./pareto_front_img/imgNum" + counter);
                counter++;
            }
            PhenotypeCombiner.saveAggregatedPhenotype(skadoosh, image, imagefp, "./pareto_front_img/combined");


            //SimpleGenteticAlgorithm genteticAlgorithm = new SimpleGenteticAlgorithm(50, 1, 2, 0.2, 0.7, image);
            //var ph = genteticAlgorithm.runGenalg(100);
            //image.savePixelGroupEdgeDisplay(ph);


        } catch (FileNotFoundException e) {
            throw new RuntimeException(e);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private static void SGA() {

        //        File imagefp = new File("./training_images/training_images/176039/Test image.jpg");
        //        File imagefp = new File("./training_images/training_images/118035/Test image.jpg");
        File imagefp = new File("./training_images/training_images/86016/Test image.jpg");

        //        File imagefp = new File("./training_images/training_images/118035/Test image.jpg");
        //        File imagefp = new File("./training_images/training_images/test/test_img.png");
        //        File imagefp = new File("./training_images/training_images/test/test_img_shitty_compressed.jpg");
        try {
            Image image = new Image(imagefp);


            SimpleGenteticAlgorithm genteticAlgorithm = new SimpleGenteticAlgorithm(50, 1, 2, 0.2, 0.7, image);
            var                     ph                = genteticAlgorithm.runGenalg(100);
            image.savePixelGroupEdgeDisplay(ph, "./out_img");


        } catch (FileNotFoundException e) {
            throw new RuntimeException(e);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

    }
}
