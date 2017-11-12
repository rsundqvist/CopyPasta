package model;

import java.util.List;

public interface FeedbackListListener {
  /** Indicates that the list has changed. */
  void listChanged(List<Feedback> feedbackList);

  /** Indicates that the list is about to be exported - last chance for upates. */
  void feedbackAboutToExport(List<Feedback> feedbackList);
}
