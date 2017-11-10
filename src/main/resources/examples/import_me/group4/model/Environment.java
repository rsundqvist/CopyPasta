package model;

import gui.feedback.FeedbackViewController;
import gui.pasta.PastaViewController;

/** Created by Richard Sundqvist on 23/02/2017. */
public class Environment {
  private final PastaManager pastaManager;
  private final FeedbackManager feedbackManager;

  public Environment(PastaManager pastaManager, FeedbackManager feedbackManager) {
    this.pastaManager = pastaManager;
    this.feedbackManager = feedbackManager;
  }

  public void load(
      PastaViewController pastaViewController, FeedbackViewController feedbackViewController) {
    pastaViewController.importPasta(pastaManager.getPastaList());
    // feedbackViewController.imp
  }
}
