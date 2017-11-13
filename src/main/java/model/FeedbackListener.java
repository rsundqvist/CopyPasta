package model;

import java.util.List;

public interface FeedbackListener {
  /** Will not call {@link #listChanged()} also - this is implicit */
  void changeGroup(List<Feedback> feedbackList);

  /** Will not call {@link #listChanged()} also - this is implicit */
  void toggleDone(List<Feedback> feedbackList);

  void preview(Feedback feedback);

  boolean exportFeedback(List<Feedback> feedback, boolean asTxt, boolean asJson);

  /** Indicates that the list has changed (add/remove items usually) */
  void listChanged();
}
