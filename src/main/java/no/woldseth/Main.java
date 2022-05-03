package no.woldseth;

import no.woldseth.evolutionary_algorithm.*;
import no.woldseth.evolutionary_algorithm.representation.Genotype;
import no.woldseth.evolutionary_algorithm.representation.MOOEvaluatedPhenotype;
import no.woldseth.evolutionary_algorithm.representation.Phenotype;
import no.woldseth.image.Image;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.awt.image.DataBufferByte;
import java.io.*;
import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class Main {

    private static DebugLogger dbl = new DebugLogger(true);

    public static void main(String[] args) {
        Phenotype.threshold                = 300;
        Initialization.initializationFlips = 100;
        NSGA();
        //        SGA();
    }

    private static void NSGA() {
        String imgNum = "86016";  // church thingy
        //        String imgNum = "86016"; // grass ring
        //        String imgNum = "147091"; // Tree against sky
        //        String imgNum = "176035"; // river
        //        String imgNum = "176039"; // clouds
        //        String imgNum = "353013"; // tree in vase

        File imagefp = new File("./training_images/" + imgNum + "/Test image.jpg");

        //        File imagefp = new File("./training_images/test/test_img.png");
        //        File imagefp = new File("./training_images/test/test_img_shitty_compressed.jpg");
        try {
            Image image = new Image(imagefp);

                        //var a = Initialization.generateInitialGenome(image);
                        //image.savePixelGroupEdgeDisplay(new Phenotype(a, image), "./init_test_img");
                        //System.exit(0);
            

            NSGA2 peeop    = new NSGA2(50, 20, 2, 0.07, 0.95, image);
            var   skadoosh = peeop.runGenalg(2);


            int counter = 0;
            for (Phenotype p : skadoosh) {
                image.savePixelGroupEdgeDisplay(p, "./pareto_front_img/imgNum" + counter);
                image.saveGroundTruthImage(p, "./pareto_front_type2/img2Num" + counter);
                image.savePhenotypeAsCsv(p,"./pareto_front_txt/txtNum" + counter);
                counter++;
            }
            System.out.println("Evaluating images, please wait...");
            evaluateFront(imgNum);

            counter = 0;
            //            for (Phenotype p : peeop.getMostBest((ArrayList<MOOEvaluatedPhenotype>) skadoosh)) {
            //                image.savePixelGroupEdgeDisplay(p, "./pareto_front_img/AA_BEST_imgNum" + counter);
            //                counter++;
            //            }
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

        //        String imgNum = "118035";  // church thingy
        //        String imgNum = "86016"; // grass ring
        //        String imgNum = "147091"; // Tree against sky
        //        String imgNum = "176035"; // river
        //        String imgNum = "176039"; // clouds
        String imgNum = "353013"; // tree in vase

        File imagefp = new File("./training_images/" + imgNum + "/Test image.jpg");

        try {
            Image image = new Image(imagefp);

            double deviationWeight    = 0.002;
            double edgeValWeight      = 8;
            double connectivityWeight = 1;

            SimpleGenteticAlgorithm genteticAlgorithm = new SimpleGenteticAlgorithm(50,
                                                                                    5,
                                                                                    2,
                                                                                    0.2,
                                                                                    0.8,
                                                                                    image,
                                                                                    deviationWeight,
                                                                                    edgeValWeight,
                                                                                    connectivityWeight
            );

            var best = genteticAlgorithm.runGenalg(50);


            int counter = 0;


            for (Phenotype p : best) {
                image.savePixelGroupEdgeDisplay(p, "./pareto_front_img/imgNum" + counter);
                image.saveGroundTruthImage(p, "./pareto_front_type2/img2Num" + counter);
                image.savePhenotypeAsCsv(p, "./pareto_front_txt/txtNum" + counter);
                counter++;
            }
            System.out.println("Evaluating images, please wait...");
            evaluateFront(imgNum);


        } catch (FileNotFoundException e) {
            throw new RuntimeException(e);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

    }
    private static void evaluateFront(String imgNum) {
        try {

            ProcessBuilder pb = new ProcessBuilder("python", "./python_evaluator/run.py", "--image_number", imgNum);
            Process pr = pb.start();
            BufferedReader in = new BufferedReader(new InputStreamReader(
                    pr.getInputStream()));

            String line;
            while ((line = in.readLine()) != null) {
                System.out.println(line);
            }
            in.close();
            pr.waitFor();

        } catch (Exception ignored) {
        }
    }
    private static int RPI(BufferedImage img1, BufferedImage img2) {
        int colorValueSlackRange = 40;
        int blackValueThreshold = 100;
        int pixelRangeCheck = 4;
        boolean checkEightSurroundingPixels = true;
        int height = img1.getHeight();
        int width = img1.getWidth();

        int counter = 0;
        int numberOfBlackPixels = 0;

        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                int color1 = img1.getRGB(x, y);
                int color2 = img2.getRGB(x, y);
                if (color1 < blackValueThreshold) {
                    numberOfBlackPixels += 1;
                    if (color1 == color2) {
                        counter += 1;
                    } else if (checkEightSurroundingPixels) {
                        boolean correctFound = false;
                        for (int w2 = y-pixelRangeCheck; w2 < y+pixelRangeCheck +1; w2++) {
                            if (correctFound)
                                break;
                            for (int h2 = x-pixelRangeCheck; w2 < x+pixelRangeCheck +1; w2++) {
                                if(w2 >= 0 && h2 >= 0 && w2 < width && h2 < height) {
                                    color2 = img2.getRGB(w2, h2);
                                    if (color1 - colorValueSlackRange < color2 && color2 < colorValueSlackRange + color1) {
                                        correctFound = true;
                                        counter += 1;
                                        break;
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
        return counter/ Math.max(numberOfBlackPixels, 1);
    }
}
