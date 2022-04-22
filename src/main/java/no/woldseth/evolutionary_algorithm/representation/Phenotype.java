package no.woldseth.evolutionary_algorithm.representation;

import no.woldseth.image.Image;
import no.woldseth.image.Pixel;
import no.woldseth.image.PixelGroup;

import java.lang.reflect.Array;
import java.util.*;

public class Phenotype extends Genotype {
    protected Image image;
    public int[] pixelGroupList;
    public ArrayList<PixelGroup> pixelGroups = new ArrayList<>();

    public Phenotype(Genotype genotype, Image image) {
        super(genotype.genome);

        this.image = image;
        generatePixelGroups();
    }

    public Phenotype(PixelConnectionType[] genome, Image image) {
        super(genome);

        this.image = image;
        generatePixelGroups();
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
}
