package model;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/** @author Richard Sundqvist */
public abstract class PastaFilter {

  private PastaFilter() {}

  /**
   * Returns a list of Pasta, filtered on content tags.
   *
   * @param pastaList The list to filter.
   * @param contentTags The tags to filter on.
   * @param anyTag If {@code true}, items will be included if any tags match. If {@code false}, all
   *     tags must match for inclusion.
   * @param neg If {@code true}, the complement of the matching set will be used.
   * @return A list of pasta which passed the match the filter settings.
   */
  public static List<Pasta> filter(
      List<Pasta> pastaList, List<String> contentTags, boolean anyTag, boolean neg) {
    List<Pasta> filteredPastaList;

    if (anyTag) filteredPastaList = filterAny(pastaList, contentTags);
    else filteredPastaList = filterAll(pastaList, contentTags);

    if (neg) {
      List<Pasta> negatedPastaList = new ArrayList<>(pastaList);
      negatedPastaList.removeAll(filteredPastaList);
      filteredPastaList = negatedPastaList;
    }
    return filteredPastaList;
  }

  /**
   * Returns a list of Pasta, filtered on assignment.
   *
   * @param pastaList The list to filter.
   * @param assignment The assignment to filter on.
   * @param neg If {@code true}, the complement of the matching set will be used.
   * @return A list of pasta which passed the match the filter settings.
   */
  public static List<Pasta> filter(List<Pasta> pastaList, String assignment, boolean neg) {
    List<Pasta> filteredPastaList = new ArrayList<>(pastaList.size());
    assignment = FeedbackManager.parseAssignmentString(assignment);

    for (Pasta pasta : pastaList) {
      List<String> assignmentTags = pasta.getAssignmentTags();

      if (assignmentTags.isEmpty() || assignmentTags.contains(assignment))
        filteredPastaList.add(pasta);
    }

    if (neg) {
      List<Pasta> negatedPastaList = new ArrayList<>(pastaList);
      negatedPastaList.removeAll(filteredPastaList);
      filteredPastaList = negatedPastaList;
    }

    return filteredPastaList;
  }

  /**
   * Returns a list of Pasta which contains the search term. Will search {@link Pasta#content},
   * {@link Pasta#title}, {@link Pasta#contentTags} and {@link Pasta#assignmentTags}.
   *
   * @param pastaList The list to search.
   * @param searchTerms The terms to search for.
   * @return A list of pasta which passed the match the filter settings.
   */
  public static List<Pasta> search(List<Pasta> pastaList, List<String> searchTerms) {
    List<Pasta> filteredPastaList = new ArrayList<>();

    outer:
    for (Pasta pasta : pastaList) {
      StringBuilder sb = new StringBuilder(); // Create search string
      sb.append(pasta.getContent());
      sb.append(pasta.getTitle());

      for (String tag : pasta.getAssignmentTags()) sb.append(tag + " ");

      for (String tg : pasta.getContentTags()) sb.append(tg + " ");

      String searchString = sb.toString().toLowerCase();

      for (String searchTerm : searchTerms)
        if (searchString.contains(searchTerm.toLowerCase())) {
          filteredPastaList.add(pasta);
          continue outer;
        }
    }

    return filteredPastaList;
  }

  private static boolean searchString(List<String> searchTerms, String string) {
    List<String> tokens = Arrays.asList(string.split("\\s+"));
    return !Collections.disjoint(tokens, searchTerms);
  }

  private static List<Pasta> filterAny(List<Pasta> pastaList, List<String> filterTags) {
    List<Pasta> includedPastaList = new ArrayList<>();
    for (Pasta pasta : pastaList)
      if (containsAnyTag(pasta, filterTags)) includedPastaList.add(pasta);

    return includedPastaList;
  }

  private static List<Pasta> filterAll(List<Pasta> pastaList, List<String> filterTags) {
    List<Pasta> includedPastaList = new ArrayList<>();
    for (Pasta pasta : pastaList)
      if (containsAllTags(pasta, filterTags)) includedPastaList.add(pasta);

    return includedPastaList;
  }

  private static boolean containsAnyTag(Pasta pasta, List<String> filterTags) {
    List<String> pastaTags = pasta.getContentTags();
    return !Collections.disjoint(pastaTags, filterTags);
  }

  private static boolean containsAllTags(Pasta pasta, List<String> filterTags) {
    List<String> pastaTags = pasta.getContentTags();
    return pastaTags.containsAll(filterTags);
  }
}
