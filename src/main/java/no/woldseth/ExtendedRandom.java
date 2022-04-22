package no.woldseth;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class ExtendedRandom extends Random {

    public <T> List<T> randomChoice(List<T> selection, int numToChose, boolean pickWithReplacement) {
        if (selection.size() < numToChose) {
            throw new RuntimeException();
        }

        int          selectionSize = selection.size();
        ArrayList<T> returnList    = new ArrayList<>();

        if (pickWithReplacement) {
            ArrayList<Integer> keep = new ArrayList<>(IntStream.range(0, selectionSize).boxed().toList());
            for (int i = 0; i < numToChose; i++) {
                int selectedIdx = this.nextInt(keep.size());
                returnList.add(selection.get(keep.remove(selectedIdx)));
            }
        } else {
            for (int i = 0; i < numToChose; i++) {
                returnList.add(selection.get(this.nextInt(selectionSize)));
            }
        }
        return returnList;

    }
}
