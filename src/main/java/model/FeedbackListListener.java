package model;

public interface FeedbackListListener {
  /** Indicates that the list has changed. */
  void listChanged();

  /** Indicates that the list is about to be exported - last chance for upates. */
  void feedbackAboutToExport();
}
