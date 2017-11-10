package gui;

import javafx.scene.control.Alert;
import model.IO;

import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

public abstract class Settings {


    private Settings () {
    }

    public static boolean FIRST_RUN = true;

    /*
     * Settings
     */
    public static boolean USE_NATIVE_TXT_EDITOR = false;
    private static final String use_native_txt_editor = "use_native_txt_editor";

    public static String WORKSPACE_LOCATION = "user.dir";
    static final String workspace_location = "workspace_location";

    public static int FILE_DECORATION_WIDTH = 80;
    private static final String file_decoration_width = "file_decoration_width";

    public static boolean STARTUP_VERSION_CHECK = true;
    private static final String startup_version_check = "startup_version_check";

    /*
     * Class stuff
     */
    public static final Properties properties = new Properties();
    public static final int OPTION_INDEX = 0, ABOUT_INDEX = 1, TYPE_INDEX = 2;

    // ================================================================================= //
    // Methods that must be changed every time a new setting variable is introduced
    // ================================================================================= //
    public static Map<String, String[]> getAbout () {
        Map<String, String[]> about = new HashMap<>();

        about.put(use_native_txt_editor, // Key
                new String[]{"Native Preview", // Display name
                        "Try to use the native editor for some previews. As of 2017-11-09, will cause a crash on Ubuntu 16.04.", // About
                        Boolean.class.getCanonicalName() + ""}); // Type

        about.put(workspace_location, // Key
                new String[]{"Workspace Location", "Default (recommended): \"user.dir\". CopyPasta must be restarted for this to take effect." + "\nDefault location (Windows 10): \"%UserProfile%/CopyPasta/workspace\"", // about
                        String.class.getCanonicalName() + ""});

        about.put(file_decoration_width, // Key
                new String[]{"File Decoration Width", // Display name
                        "Width of automatically generated file sections in the feedback view, measured in characters.", Integer.class.getCanonicalName() + ""});

        about.put(startup_version_check, // Key
                new String[]{"Startup Version Check", // Display name
                        "Check for updates on startup.", Boolean.class.getCanonicalName() + ""});

        return about;
    }

    public static void loadFromProperties () {
        try {
            USE_NATIVE_TXT_EDITOR = Boolean.parseBoolean((String) properties.get(use_native_txt_editor));

            String s = (String) properties.get(startup_version_check);
            if (s != null && !s.isEmpty())
                STARTUP_VERSION_CHECK = Boolean.parseBoolean(s);

            WORKSPACE_LOCATION = (String) properties.get(workspace_location);

            if (WORKSPACE_LOCATION == null || WORKSPACE_LOCATION.isEmpty() || WORKSPACE_LOCATION.equals("null"))
                WORKSPACE_LOCATION = "user.dir";

            FILE_DECORATION_WIDTH = Integer.parseInt((String) properties.get(file_decoration_width));
        } catch (Exception e) {
            if (!FIRST_RUN)
                IO.showExceptionAlert(e);
        }
    }

    public static void restartForSettingsEffect () {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setHeaderText("Settings saved");
        alert.setContentText("The new settings will take effect once you restart the program.");
        alert.showAndWait();
    }

    public static void putToProperties () {
        putValue(use_native_txt_editor, "" + USE_NATIVE_TXT_EDITOR);
        putValue(workspace_location, "" + WORKSPACE_LOCATION);
        putValue(file_decoration_width, "" + FILE_DECORATION_WIDTH);
        putValue(startup_version_check, "" + STARTUP_VERSION_CHECK);
    }
    // endregion

    // ================================================================================= //
    // Other stuff
    // ================================================================================= //

    public static final void loadSettingsFile () {
        try {
            properties.load(new FileReader(Tools.SETTINGS_FILE));
        } catch (IOException e) {
        }
        loadFromProperties();
    }

    public static void putValue (String key, String value) {
        properties.put(key, value);
    }

    public static final void storeStoreSettingsFile () {
        try {
            new FileOutputStream(Tools.SETTINGS_FILE);

            putToProperties();

            properties.store(new FileOutputStream(Tools.SETTINGS_FILE), "CopyPasta settings file\nVersion: " + Tools.VERSION);
        } catch (IOException e) {
            IO.showExceptionAlert(e);
        }
    }

    public static void setRunningFile (boolean value) {
        IO.printStringToFile(value + "", Tools.IS_RUNNING_FILE);
    }

    public static boolean getRunningFile () {
        FIRST_RUN = Tools.IS_RUNNING_FILE.exists();
        return Boolean.parseBoolean(IO.getFileAsString(Tools.IS_RUNNING_FILE).replaceAll("\\s+", ""));
    }
}
