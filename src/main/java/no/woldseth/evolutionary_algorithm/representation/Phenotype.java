package no.woldseth.evolutionary_algorithm.representation;

import no.woldseth.DebugLogger;
import no.woldseth.evolutionary_algorithm.util.PrimsAlgorithm;
import no.woldseth.image.Image;
import no.woldseth.image.Pixel;
import no.woldseth.image.PixelGroup;

import java.lang.reflect.Array;
import java.util.*;
import java.util.stream.Collectors;

public class Phenotype extends Genotype {
    protected Image image;
    public int[] pixelGroupList;
    public ArrayList<PixelGroup> pixelGroups = new ArrayList<>();

    public Phenotype(Genotype genotype, Image image) {
        super(genotype.genome);

        this.image = image;
        generatePixelGroups();

        purgeSmallGroups();
    }

    public Phenotype(PixelConnectionType[] genome, Image image) {
        super(genome);

        this.image = image;
        generatePixelGroups();
        purgeSmallGroups();
    }

    public static int threshold = 100;

    private void purgeSmallGroups() {
        List<Set<Integer>> groupNeighbours = new ArrayList<>();

        int numGroups = this.pixelGroups.size();
        for (int i = 0; i < numGroups; i++) {
            groupNeighbours.add(new HashSet<>());
        }

        for (int y = 0; y < image.height - 1; y++) {
            for (int x = 0; x < image.width - 1; x++) {

                int gc     = this.pixelGroupList[image.getPointAsId(x, y)];
                int gDown  = this.pixelGroupList[image.getPointAsId(x, y + 1)];
                int gRight = this.pixelGroupList[image.getPointAsId(x + 1, y)];
                if (gc != gDown) {
                    groupNeighbours.get(gc).add(gDown);
                    groupNeighbours.get(gDown).add(gc);
                } else if (gc != gRight) {
                    groupNeighbours.get(gc).add(gRight);
                    groupNeighbours.get(gRight).add(gc);
                }
            }
        }

        for (int i = 0; i < pixelGroups.size(); i++) {
            if (pixelGroups.get(i).groupMembers.size() <= threshold) {
                var pGroup     = pixelGroups.get(i);
                var neighbours = groupNeighbours.get(i);
                for (var neighbor : neighbours) {
                    if (pixelGroups.get(neighbor).groupMembers.size() > threshold) {
                        for (Pixel pixel : pGroup.groupMembers) {
                            pixelGroups.get(neighbor).groupMembers.add(pixel);
                        }
                        pGroup.groupMembers.clear();
                        break;
                    }
                }
            }
        }
        this.pixelGroups = this.pixelGroups.stream().filter(pixelGroup -> pixelGroup.groupMembers.size() > 0).collect(
                Collectors.toCollection(ArrayList::new));


        for (int i = 0; i < pixelGroups.size(); i++) {

            var pGroup = pixelGroups.get(i);
            for (Pixel pixel : pGroup.groupMembers) {
                this.pixelGroupList[pixel.getId()] = i;
            }

        }

        //
        //        int roll = 0;
        //        for (int i = 0; i < pixelGroups.size(); i++) {
        //            if (pixelGroups.get(i).groupMembers.size() > threshold){
        //                var pixelGroup = pixelGroups.get(i);
        //                var oldGroup = this.pixelGroupList[pixelGroup.groupMembers.get(0).getId()];
        //
        //                if (oldGroup != roll){
        //                    for (Pixel pixel: pixelGroup.groupMembers) {
        //
        //                    }
        //                }
        //
        //                roll+=1;
        //            }else {
        //
        //            }
        //        }

        //        var keepMap      = new boolean[this.pixelGroups.size()];
        //        var translateMap = new int[this.pixelGroups.size()];
        //        for (int i = 0; i < pixelGroups.size(); i++) {
        //            keepMap[i] = pixelGroups.get(i).groupMembers.size() > threshold;
        //        }
        //        Arrays.fill(translateMap, - 1);

        //        for (int y = 0; y < image.height; y++) {
        //            for (int x = 0; x < image.width; x++) {
        //                int id    = image.getPointAsId(x, y);
        //                int group = this.pixelGroupList[id];
        //                if (! keepMap[group]) {
        //                    int newGroup = - 1;
        //                    if (x > 0 && keepMap[pixelGroupList[image.getPointAsId(x - 1, y)]]) {
        //                        newGroup = pixelGroupList[image.getPointAsId(x - 1, y)];
        //                    } else if (y > 0 && keepMap[pixelGroupList[image.getPointAsId(x, y - 1)]]) {
        //                        newGroup = pixelGroupList[image.getPointAsId(x, y - 1)];
        //                    } else {
        //                        //                        newGroup = pixelGroupList[image.getPointAsId(x, yTry)];
        //
        //                        int ny = 1;
        //                        while (! keepMap[pixelGroupList[image.getPointAsId(x, ny)]] && ny < image.height - 1) {
        //                            ny += 1;
        //                        }
        //                        if (keepMap[pixelGroupList[image.getPointAsId(x, ny)]]) {
        //                            newGroup = pixelGroupList[image.getPointAsId(x, ny)];
        //                        } else {
        //
        //                            int nx = 1;
        //                            while (! keepMap[pixelGroupList[image.getPointAsId(nx, y)]] && nx < image.width - 1) {
        //                                nx += 1;
        //                            }
        //                            if (keepMap[pixelGroupList[image.getPointAsId(nx, y)]]) {
        //                                newGroup = pixelGroupList[image.getPointAsId(nx, y)];
        //                            } else {
        //
        //                                throw new RuntimeException();
        //                            }
        //
        //                        }
        //
        //                    }
        //                    if (! keepMap[newGroup]) {
        //                        System.out.println("AAAAAAAAAAAAAAAA");
        //                        if (keepMap[newGroup + 1]) {
        //                            newGroup = newGroup + 1;
        //                        } else if (keepMap[newGroup - 1]) {
        //                            newGroup = newGroup - 1;
        //                        } else {
        //                            System.out.println(x);
        //                            System.out.println(y);
        //                            System.out.println(newGroup);
        //                            System.out.println(Arrays.toString(keepMap));
        //                            throw new RuntimeException();
        //                        }
        //
        //                    }
        //
        //                    //                    pixelGroupList[image.getPointAsId(x, y)] = newGroup;
        //                    //
        //                    //                    pixelGroups.get(newGroup).groupMembers.add(image.getPixel(image.getPointAsId(x, y)));
        //                    System.out.println(group);
        //
        //                    var killGroup     = this.pixelGroups.get(group);
        //                    var newPixelGroup = pixelGroups.get(newGroup);
        //                    System.out.println("kill g " + pixelGroupList[killGroup.groupMembers.get(0).getId()]);
        //                    for (Pixel pixel : killGroup.groupMembers) {
        //
        //                        if (newGroup == 0) {
        //                            System.out.println("new is 0");
        //                        }
        //                        newPixelGroup.groupMembers.add(pixel);
        //                        pixelGroupList[pixel.getId()] = newGroup;
        //
        //                        if (pixelGroupList[pixel.getId()] == 0) {
        //                            System.out.println("old stay 0");
        //                        }
        //                    }
        //                    killGroup.groupMembers.clear();
        //                    //                    for (int i = killGroup.groupMembers.size() - 1; i >= 0; i--) {
        //                    //
        //                    //                        var pixel = killGroup.groupMembers.get(i);
        //                    //                        newPixelGroup.groupMembers.add(pixel);
        //                    //                        killGroup.groupMembers.remove(pixel);
        //                    //                    }
        //                }
        //
        //
        //            }
        //        }

        //
        //        System.out.println(Arrays.toString(keepMap));
        //        for (int i = pixelGroups.size() - 1; i >= 0; i--) {
        //            //            if (! keepMap[i]) {//pixelGroups.get(i).groupMembers.size() == 0) {
        //            if (pixelGroups.get(i).groupMembers.size() == 0) {
        //                pixelGroups.remove(i);
        //            }
        //        }
        //
        //        int roll = 0;
        //        for (int i = 0; i < translateMap.length; i++) {
        //            if (keepMap[i]) {
        //                translateMap[i] = roll;
        //                                  roll += 1;
        //            }
        //        }
        //
        //
        //        for (int i = 0; i < pixelGroupList.length; i++) {
        //            //            pixelGroupList[i] = sketchyTransMap.get(i);
        //            pixelGroupList[i] = translateMap[pixelGroupList[i]];
        //            if (translateMap[pixelGroupList[i]] == - 1) {
        //                System.out.println("hit");
        //                System.out.println(i);
        //                System.out.println(pixelGroupList[i]);
        //                System.out.println(Arrays.toString(translateMap));
        //                System.exit(0);
        //            }
        //        }

    }

    public Genotype getThresholdGroupGenome() {
        var p = this;
        for (int i = 0; i < p.pixelGroups.size(); i++) {
            var pGroup = p.pixelGroups.get(i);
            if (pGroup.groupMembers.size() == 1) {
                var pixel    = pGroup.groupMembers.get(0);
                var oldGroup = pixelGroupList[pixel.getId()];
                int newGroup = oldGroup;
                if (pixel.getX() != image.width - 1) {
                    genome[pixel.getId()] = PixelConnectionType.RIGHT;
                    //                    newGroup = pixelGroupList[pixel.getId() - 1];
                } else {

                    genome[pixel.getId()] = PixelConnectionType.LEFT;
                    //                    newGroup = pixelGroupList[pixel.getId() + 1];
                }

                //                pixelGroupList[pixel.getId()] = newGroup;
                continue;
            }
            for (Pixel pixel : pGroup.groupMembers) {
                p.genome[pixel.getId()] = null;
            }

            PrimsAlgorithm.connectGroups(image, p.genome, pGroup.groupMembers.get(0).getId());

            for (Pixel pixel : pGroup.groupMembers) {
                if (p.genome[pixel.getId()] == null) {
                    DebugLogger.dbl().log("Found null in genome");
                    PrimsAlgorithm.dbl.print = true;

                    DebugLogger.dbl().log("p group", pGroup.groupMembers.stream().map(Pixel::getId).toList());
                    PrimsAlgorithm.connectGroups(image, p.genome, pGroup.groupMembers.get(0).getId());
                    //                    for (int j = 0; j < image.height; j++) {
                    //                        System.out.println(Arrays.toString(Arrays.copyOfRange(p.genome,
                    //                                                                              j * image.width,
                    //                                                                              (j + 1) * image.width
                    //                                                                             )));
                    //
                    //                    }

                    System.exit(0);

                }
                //                p.genome[pixel.getId()] = null;
            }
        }

        for (int n = 0; n < p.genome.length; n++) {
            if (p.genome[n] == null) {
                for (int i = 0; i < image.height; i++) {
                    System.out.println(Arrays.toString(Arrays.copyOfRange(genome,
                                                                          i * image.width,
                                                                          (i + 1) * image.width
                                                                         )));

                }
                throw new RuntimeException();
            }


        }
        return new Genotype(p.genome);
    }

    private void generatePixelGroups() {
        int numGenomes = genome.length;
        pixelGroupList = new int[numGenomes];
        Arrays.fill(pixelGroupList, - 1);

        for (int row = 0; row < image.height; row++) {
            for (int col = 0; col < image.width; col++) {
                int id    = (image.width * row) + col;
                var group = pixelGroupList[id];
                if (group == - 1) {
                    var groupSet = new HashSet<Integer>();
                    traverseFindGroup(groupSet, col, row, genome);
                }
            }
        }
    }

    private void addNewPixelGroup(HashSet<Integer> set) {
        int nextGroupId = Arrays.stream(pixelGroupList).max().getAsInt() + 1;
        //        System.out.println(nextGroupId);
        pixelGroups.add(new PixelGroup());
        set.forEach(integer -> {
            pixelGroupList[integer] = nextGroupId;
            pixelGroups.get(nextGroupId).groupMembers.add(image.getPixel(integer));
        });

    }

    private void mergeToPixelGroup(HashSet<Integer> set, int groupId) {
        //        System.out.println(set.size());
        set.forEach(integer -> {
            pixelGroupList[integer] = groupId;
            pixelGroups.get(groupId).groupMembers.add(image.getPixel(integer));
        });

    }

    private void traverseFindGroup(HashSet<Integer> groupSet,
                                   int currentX,
                                   int currentY,
                                   PixelConnectionType[] genome) {
        // if the traversal led out of the image stop
        if ((currentX >= image.width || currentX < 0) || (currentY >= image.height || currentY < 0)) {
            addNewPixelGroup(groupSet);
            return;
        }

        int id = (image.width * currentY) + currentX;

        // if the traversal loops stop and add as a new group
        if (groupSet.contains(id)) {
            addNewPixelGroup(groupSet);
            return;
        }

        var pixelPixelGroup = pixelGroupList[id];
        //        System.out.println(pixelPixelGroup);
        // if the traversal hits a pixel assigned to some group put the acumulated pixels in that group
        if (pixelPixelGroup != - 1) {
            mergeToPixelGroup(groupSet, pixelPixelGroup);
            return;
        }


        groupSet.add(id);

        int newX = currentX;
        int newY = currentY;
        switch (genome[id]) {
            case UP -> {
                newY -= 1;
            }
            case LEFT -> {
                newX -= 1;
            }
            case DOWN -> {
                newY += 1;
            }
            case RIGHT -> {
                newX += 1;
            }
            case SELF -> {
                addNewPixelGroup(groupSet);
                return;
            }
        }
        traverseFindGroup(groupSet, newX, newY, genome);


    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        Phenotype phenotype = (Phenotype) o;
        return Arrays.equals(pixelGroupList, phenotype.pixelGroupList);
    }

    @Override
    public int hashCode() {
        return Arrays.hashCode(pixelGroupList);
    }
}
