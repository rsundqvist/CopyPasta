/** @author Richard Sundqvist, Erik Pihl @Group 64 */
public class Verktyg {

  public static void slumpOrdning(Object[] obj) {

    Object[] tmp = new Object[obj.length];

    for (int i = 0; i < obj.length; ) { // Randomize order
      int random_index = (int) (Math.random() * obj.length);
      if (tmp[random_index] == null) {
        tmp[random_index] = obj[i];
        i++;
      }
    }
    for (int i = 0; i < obj.length; i++) { // Return objects in randomized order,
      obj[i] = tmp[i];
    }
  }
}
