/**
 * Created by Richard Sundqvist on 20/04/2017.
 */
public class Regex_Test {
    public static final String REGEX = "%([Ff]ile|FILE):[ \t]*\\S+[ \t]*%";
    public static final String TEST =
            ":::::: Steg3: %file:Steg3.java%\n" +
                    ":::::: CrystalModel: %FILE:  \t  CrystalModel.java%\n" +
                    ":::::: CrystalView: %File:CrystalView.java    %\n" +
                    ":::::: CrystalView: %File:    \t Crystal_View.java    %\n" +
                    ":::::: CrystalView: %File:      Crystal_View.java    %\n  " +
                    ":::::: CrystalControl: %FiLE:  \t  \t\tCrystalControl.java\t\t %\n" +
                    ":::::: CrystalControl: %FILE:  \t  \t\tCrystal_Control.java\t\t %\n" +
                    ":::::: CrystalControl: FiLE:  \t  \t test.java\t\t \n";

    public static void main (String[] args) {
        System.out.println(TEST);

        System.out.println("=========================================================================================");
        System.out.println("=========================================================================================");
        System.out.println("Matches for: \"" + REGEX + "\"");
        System.out.println("=========================================================================================");
        System.out.println("=========================================================================================");
        System.out.println();
        String r = TEST.replaceAll(REGEX, "<MATCH>");
        System.out.println(r);
    }
}
