package model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * @author Richard Sundqvist
 */
public abstract class PastaFilter {

    private PastaFilter () {
    }

    /**
     * Returns a list of filtered Pasta.
     *
     * @param pastaList The list to filter.
     * @param filterTags The tags to filter on.
     * @param anyTag If {@code true}, items will be included if any tags match. If {@code false}, all tags must match for
     * inclusion.
     * @param neg If {@code true}, the complement of the matching set will be used.
     * @return A list of pasta which passed the match the filter settings.
     */
    public static List<Pasta> filter (List<Pasta> pastaList, List<String> filterTags, boolean anyTag, boolean neg) {
        List<Pasta> filteredPastaList;

        if (anyTag)
            filteredPastaList = filterAny(pastaList, filterTags);
        else
            filteredPastaList = filterAll(pastaList, filterTags);

        if (neg) {
            List<Pasta> negatedPastaList = new ArrayList<>(pastaList);
            negatedPastaList.removeAll(filteredPastaList);
            filteredPastaList = negatedPastaList;
        }
        return filteredPastaList;
    }

    private static List<Pasta> filterAny (List<Pasta> pastaList, List<String> filterTags) {
        List<Pasta> includedPastaList = new ArrayList<>();
        for (Pasta pasta : pastaList)
            if (containsAnyTag(pasta, filterTags))
                includedPastaList.add(pasta);

        return includedPastaList;
    }

    private static List<Pasta> filterAll (List<Pasta> pastaList, List<String> filterTags) {
        List<Pasta> includedPastaList = new ArrayList<>();
        for (Pasta pasta : pastaList)
            if (containsAllTags(pasta, filterTags))
                includedPastaList.add(pasta);

        return includedPastaList;
    }

    private static boolean containsAnyTag (Pasta pasta, List<String> filterTags) {
        List<String> pastaTags = pasta.getContentTags();
        return !Collections.disjoint(pastaTags, filterTags);
    }

    private static boolean containsAllTags (Pasta pasta, List<String> filterTags) {
        List<String> pastaTags = pasta.getContentTags();
        return pastaTags.containsAll(filterTags);
    }
}
