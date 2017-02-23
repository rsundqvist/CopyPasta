package model;

import javafx.scene.control.Alert;
import javafx.scene.control.TextArea;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Priority;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * Created by Richard Sundqvist on 22/02/2017.
 */
public class FeedbackManager {

    //region Field
    // ================================================================================= //
    // Field
    // ================================================================================= //
    private UniqueArrayList<Feedback> feedbackList = new UniqueArrayList<>();
    private UniqueArrayList<Feedback> doneFeedbackList = new UniqueArrayList<>();
    private UniqueArrayList<Feedback> notDoneFeedbackList = new UniqueArrayList<>();
    private Feedback template = null;

    /**
     * Produce a preview of a Feedback item, displayed in an Alert dialog.
     *
     * @param feedback The Feedback to preview.
     */
    public static void preview (Feedback feedback) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Feedback Preview");
        alert.setHeaderText("Feedback preview for group \"" + feedback.getGroup() + "\"");
        alert.setContentText("Output when exporting as a .txt-file:");

        TextArea textArea = new TextArea(feedback.getStylizedContent());
        textArea.setEditable(false);
        textArea.setWrapText(true);

        textArea.setMaxWidth(Double.MAX_VALUE);
        textArea.setMaxHeight(Double.MAX_VALUE);
        GridPane.setVgrow(textArea, Priority.ALWAYS);
        GridPane.setHgrow(textArea, Priority.ALWAYS);

        GridPane expContent = new GridPane();
        expContent.setMaxWidth(Double.MAX_VALUE);
        expContent.add(textArea, 0, 0);

        // Set expandable Exception into the dialog pane.
        alert.getDialogPane().setExpandableContent(expContent);
        alert.getDialogPane().setExpanded(true);

        alert.showAndWait();
    }
    //endregion

    //region Control
    // ================================================================================= //
    // Control
    // ================================================================================= //

    /**
     * Clear all feedback from the manager.
     */
    public void clear () {
        feedbackList.clear();
        notDoneFeedbackList.clear();
        doneFeedbackList.clear();
    }

    /**
     * Generate new feedback from the current template. The template, returned by {@link #getTemplate()}.
     *
     * @param groupList The list of groups to create feedback for.
     * @return The list of Feedback which was created.
     * @throws IllegalStateException If template has not been set.
     */
    public List<Feedback> generateFeedback (List<String> groupList) {
        if (template == null)
            throw new IllegalStateException("Template not set.");
        ArrayList<Feedback> newFeedbackList = new ArrayList<>(groupList.size());

        String header = template.getHeader();
        String content = template.getContent();
        String teacher = template.getTeacher();
        for (String group : groupList) {
            Feedback feedback = new Feedback();
            feedback.setHeader(header);
            feedback.setContent(content);
            feedback.setTeacher(teacher);
            feedback.setGroup(group);
            newFeedbackList.add(feedback);
        }

        feedbackList.addAll(newFeedbackList);
        notDoneFeedbackList.addAll(newFeedbackList);
        Collections.sort(feedbackList);
        Collections.sort(notDoneFeedbackList);
        return newFeedbackList;
    }

    /**
     * Remove feedback from manager.
     *
     * @param feedback The feedback to remove.
     */
    public void removeFeedback (Feedback feedback) {
        feedbackList.remove(feedback);
        doneFeedbackList.remove(feedback);
        notDoneFeedbackList.remove(feedback);
    }

    /**
     * Remove all feedback in the list from the manager.
     *
     * @param feedbackList The list of feedback to remove.
     */
    public void removeFeedback (List<Feedback> feedbackList) {
        feedbackList = new ArrayList<>(feedbackList);
        this.feedbackList.removeAll(feedbackList);
        doneFeedbackList.removeAll(feedbackList);
        notDoneFeedbackList.removeAll(feedbackList);
    }

    /**
     * Removes all feedback with group designations found in {@code groupList}
     *
     * @param groupList A list of groups whose feedback is to be removed.
     * @return Removed feedback items.
     */
    public List<Feedback> removeFeedbackByGroup (List<String> groupList) {
        List<Feedback> removedFeedbackList = new ArrayList<>();

        for (Feedback feedback : feedbackList)
            if (groupList.contains(feedback.getGroup()))
                removedFeedbackList.add(feedback);

        removeFeedback(removedFeedbackList);
        return removedFeedbackList;
    }

    /**
     * This method must be called to ensure {@link #doneFeedbackList} and {@link #notDoneFeedbackList} are accurate after
     * changing status of Feedback without using the {@link #setDoneStatus(Feedback, boolean)} method.
     */
    public void updateDoneUndoneLists () {
        doneFeedbackList.clear();
        notDoneFeedbackList.clear();

        for (Feedback feedback : feedbackList)
            if (feedback.isDone())
                doneFeedbackList.add(feedback);
            else
                notDoneFeedbackList.add(feedback);

        Collections.sort(doneFeedbackList);
        Collections.sort(notDoneFeedbackList);
    }

    /**
     * Change the done status of an item, update {@link #doneFeedbackList} and {@link #notDoneFeedbackList}.
     *
     * @param feedback The feedback items to change.
     * @param done The new done status.
     * @throws IllegalArgumentException If the supplied feedback isn't managed by this FeedbackManager.
     */
    public void setDoneStatus (Feedback feedback, boolean done) {
        feedback.setDone(done);
        updateDoneUndoneLists(feedback, done);
    }

    private void updateDoneUndoneLists (Feedback feedback, boolean done) {
        if (done) {
            notDoneFeedbackList.remove(feedback);
            doneFeedbackList.add(feedback);
        } else {
            doneFeedbackList.remove(feedback);
            notDoneFeedbackList.add(feedback);
        }

        Collections.sort(doneFeedbackList);
        Collections.sort(notDoneFeedbackList);
    }

    /**
     * Add feedback to the manager.
     *
     * @param feedback The feedback to add.
     */
    public void addFeedback (Feedback feedback) {
        if (feedbackList.add(feedback))
            updateDoneUndoneLists(feedback, feedback.isDone());
    }

    /**
     * Parse a group string, splitting on ',' and spaces (including consecutive spaces. Doubles are removed.
     *
     * @param s The string to parse.
     * @return An array of parsed group designations.
     */
    public static UniqueArrayList<String> parseGroupString (String s) {
        s = s.replaceAll(",", " ");
        s = s.replaceAll("\\s+", " ");
        UniqueArrayList<String> groups = new UniqueArrayList<>();
        groups.addAll(Arrays.asList(s.split(" ")));
        return groups;
    }

    /**
     * Opens a file chooser dialog and returns a list of Feedback. Will return {@code null} if the import failed.
     *
     * @return A list of Feedback, or {@code null} if nothing was imported.
     */
    public List<Feedback> importFeedback () {
        List<Feedback> feedbackList = IO.importFeedback();
        if (feedbackList != null)
            for (Feedback feedback : feedbackList)
                addFeedback(feedback);

        return feedbackList;
    }
    //endregion

    //region Getters and Setters
    // ================================================================================= //
    // Getters and Setters
    // ================================================================================= //

    /**
     * Returns {@code true} if the content of this Feedback differs from the template ( {@link #getTemplate()} ).
     *
     * @param feedback The feedback to test.
     * @return {@code true} if the content of this Feedback differs from the template.
     */
    public boolean isContentModified (Feedback feedback) {
        return !feedback.getContent().equals(template.getContent());
    }

    /**
     * Test a list of Feedback for differences from the template ( {@link #getTemplate()} ).
     *
     * @param feedbackList The Feedback to check.
     * @return The modified items. Will return an empty list if no items are modified.
     */
    public List<Feedback> isContentModified (List<Feedback> feedbackList) {
        List<Feedback> modified = new ArrayList<>(feedbackList.size());

        for (Feedback feedback : feedbackList)
            if (isContentModified(feedback))
                modified.add(feedback);

        return modified;
    }

    /**
     * Get Feedback by group designation.
     *
     * @param groups A list of groups (will not be changed).
     * @return A list of Feedback whose group were present in {@code groups}.
     */
    public List<Feedback> getByGroup (List<String> groups) {
        groups = new ArrayList<>(groups);
        List<Feedback> found = new ArrayList<>(groups.size());
        List<Feedback> feedbackList = new ArrayList<>(this.feedbackList);

        int i = 0;
        while (!groups.isEmpty() && i < feedbackList.size()) {
            Feedback feedback = feedbackList.get(i);

            String feedbackGroup = feedback.getGroup();
            if (groups.contains(feedbackGroup)) {
                feedbackList.remove(i);
                groups.remove(feedbackGroup);
                found.add(feedback);
                i = 0;
            } else {
                i++;
            }
        }


        return found;
    }

    /**
     * Returns {@code true} if all feedback is marked as done.
     *
     * @return {@code true} if all feedback is marked as done.
     */
    public boolean isAllFeedbackDone () {
        return feedbackList.size() == doneFeedbackList.size();
    }

    public List<Feedback> getFeedbackList () {
        return Collections.unmodifiableList(feedbackList);
    }

    /**
     * Add a list of feedback to the manager. Will not accept feedback whose group collide with an existing member.
     *
     * @param feedbackList The feedback added.
     */
    public void addFeedback (List<Feedback> feedbackList) {
        List<Feedback> newFeedbackList = new ArrayList<>(feedbackList);

        List<String> groups = getGroups();
        for (Feedback feedback : feedbackList)
            if (groups.contains(feedback.getGroup()))
                newFeedbackList.add(feedback);

        this.feedbackList.addAll(feedbackList);
        Collections.sort(this.feedbackList);
        updateDoneUndoneLists();
    }

    /**
     * Returns a list of all the groups ({@link Feedback#getGroup()}) of this manager. The list may be altered at will.
     *
     * @return A list of the groups contained in this manager.
     */
    public List<String> getGroups () {
        return getGroups(feedbackList);
    }

    /**
     * Returns a list of all the groups contained in the list. The list may be altered at will.
     *
     * @param feedbackList A group of feedback.
     * @return A list of the groups contained in the list.
     */
    public static List<String> getGroups (List<Feedback> feedbackList) {
        List<String> groups = new UniqueArrayList<>();

        for (Feedback feedback : feedbackList)
            groups.add(feedback.getGroup());
        return groups;
    }

    public List<Feedback> getDoneFeedbackList () {
        return Collections.unmodifiableList(doneFeedbackList);
    }

    public List<Feedback> getNotDoneFeedbackList () {
        return Collections.unmodifiableList(notDoneFeedbackList);
    }

    public Feedback getTemplate () {
        return template;
    }

    public void setTemplate (Feedback template) {
        this.template = template;
    }
    //endregion
}
