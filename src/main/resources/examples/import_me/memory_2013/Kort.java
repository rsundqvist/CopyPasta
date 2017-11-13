/** @author Richard Sundqvist, Erik Pihl @Group 64 */
import javax.swing.*;
import java.awt.*;

@SuppressWarnings("serial")
public class Kort extends JButton {

  private Status status;;
  private Icon picture;

  public Kort(Icon pic) {
    this(pic, Status.SAKNAS);
  }

  public Kort(Icon pic, Status sta) {
    super(pic);
    this.setDisabledIcon(pic); // hindrar gr� vid disable
    this.picture = pic;
    this.status = sta;
    statusHandler(sta);
  }

  private void statusHandler(Status sta) {
    if (sta == Status.DOLT) {
      this.setBackground(Color.BLUE);
      this.setIcon(null);
    } else if (sta == Status.SYNLIGT) {
      this.setIcon(this.picture);
      // this.setBackground(Color.WHITE); //tycker det �r snyggare s�h�r, men korttest kr�ver att
      // bakgrunden f�rblir bl�
    } else {
      this.setBackground(Color.WHITE);
      this.setIcon(null);
    }
  }

  public Status getStatus() {
    return this.status;
  }

  public void setStatus(Status sta) {
    this.status = sta;
    statusHandler(sta);
  }

  public Kort copy() {
    return new Kort(this.picture, this.status);
  }

  public boolean sammaBild(Kort rhs) {
    if (rhs instanceof Kort || rhs != null) {
      return this.picture.equals(rhs.picture);
    } else {
      return false;
    }
  }

  // MISC
  public enum Status {
    DOLT,
    SYNLIGT,
    SAKNAS
  }
} // END KORT CLASS
