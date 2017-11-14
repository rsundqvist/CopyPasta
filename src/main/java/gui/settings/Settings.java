package gui.settings;

import gui.Tools;
import javafx.scene.control.Alert;
import model.IO;

import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

public abstract class Settings {

  /*
   * Class stuff
   */
  public static final Properties properties = new Properties();
  public static final int OPTION_INDEX = 0, ABOUT_INDEX = 1, TYPE_INDEX = 2;
  public static final String first_run = "first_run";
  public static final String indentation_style = "indentation_style";
  public static final String workspace_location = "workspace_location";
  private static final String use_native_txt_editor = "use_native_txt_editor";
  private static final String file_decoration_width = "file_decoration_width";
  public static final String startup_version_check = "startup_version_check";
  /*
   * Settings. The assigned values are used as defaults if no properties entry is found.
   */
  public static boolean USE_NATIVE_TXT_EDITOR = false;
  public static String WORKSPACE_LOCATION = "user.dir";
  public static int FILE_DECORATION_WIDTH = 80;
  public static boolean STARTUP_VERSION_CHECK = true;
  public static boolean FIRST_RUN = true;
  public static String INDENTATION_STYLE = "google";

  private Settings() {}

  // ================================================================================= //
  // Methods that must be changed every time a new setting variable is introduced
  // ================================================================================= //
  public static Map<String, String[]> getAbout() {
    Map<String, String[]> about = new HashMap<>();

    about.put(
        use_native_txt_editor, // Key
        new String[] {
          "Native Preview", // Display name
          "Try to use the native editor for some previews. As of 2017-11-09, will cause a crash on Ubuntu 16.04.", // About
          Boolean.class.getCanonicalName() + ""
        }); // Type

    about.put(
        workspace_location, // Key
        new String[] {
          "Workspace Location",
          "Default (recommended): \"user.dir\". Default location (Windows 10): \"%UserProfile%/CopyPasta/workspace\"", // about
          String.class.getCanonicalName() + ""
        });

    about.put(
        file_decoration_width, // Key
        new String[] {
          "File Decoration Width", // Display name
          "Width of automatically generated file sections in the feedback view, measured in characters.",
          Integer.class.getCanonicalName() + ""
        });

    about.put(
        startup_version_check, // Key
        new String[] {
          "Startup Version Check", // Display name
          "Check for updates on startup.",
          Boolean.class.getCanonicalName() + ""
        });

    about.put(
        first_run, // Key
        new String[] {
          "First Run", // Display name
          "Indicates that the program is running for the first time, showing startup help and overriding previous settings. MAY CAUSE DATA LOSS!!",
          Boolean.class.getCanonicalName() + ""
        });

    about.put(
        indentation_style, // Key
        new String[] {
          "Indentation Style", // Display name
          "The type of indentation styles used when clicking \"Indent \" in the \"Student Files\" view."
              + "Default is \"google\". Options: \"google\".",
          String.class.getCanonicalName() + ""
        });

    return about;
  }

  public static void loadFromProperties() {
    if (properties.isEmpty()) {
      putToProperties(); // Put default values
      return;
    }

    try {

      USE_NATIVE_TXT_EDITOR = Boolean.parseBoolean((String) properties.get(use_native_txt_editor));

      String s = (String) properties.get(startup_version_check);
      if (s != null && !s.isEmpty()) STARTUP_VERSION_CHECK = Boolean.parseBoolean(s);

      s = (String) properties.get(first_run);
      if (s != null && !s.isEmpty()) FIRST_RUN = Boolean.parseBoolean(s);

      WORKSPACE_LOCATION = (String) properties.get(workspace_location);
      INDENTATION_STYLE = (String) properties.get(indentation_style);

      if (WORKSPACE_LOCATION == null
          || WORKSPACE_LOCATION.isEmpty()
          || WORKSPACE_LOCATION.equals("null")) WORKSPACE_LOCATION = "user.dir";

      FILE_DECORATION_WIDTH = Integer.parseInt((String) properties.get(file_decoration_width));
    } catch (Exception e) {
      if (!FIRST_RUN) IO.showExceptionAlert(e);
    }
  }

  public static void putToProperties() {
    putValue(use_native_txt_editor, "" + USE_NATIVE_TXT_EDITOR);
    putValue(workspace_location, "" + WORKSPACE_LOCATION);
    putValue(file_decoration_width, "" + FILE_DECORATION_WIDTH);
    putValue(startup_version_check, "" + STARTUP_VERSION_CHECK);
    putValue(first_run, "" + FIRST_RUN);
    putValue(indentation_style, "" + INDENTATION_STYLE);
  }
  // endregion

  // ================================================================================= //
  // Other stuff
  // ================================================================================= //

  public static void restartForSettingsEffect() {
    Alert alert = new Alert(Alert.AlertType.INFORMATION);
    alert.setHeaderText("Settings saved");
    alert.setContentText("The new settings will take effect once you restart the program.");
    alert.showAndWait();
  }

  public static final void loadSettingsFile() {
    try {
      properties.load(new FileReader(Tools.SETTINGS_FILE));
    } catch (IOException e) {
      IO.showExceptionAlert(e);
    }
    loadFromProperties();
  }

  public static void putValue(String key, String value) {
    properties.put(key, value);
  }

  public static final void storeStoreSettingsFile() {
    try {
      new FileOutputStream(Tools.SETTINGS_FILE);

      putToProperties();

      properties.store(
          new FileOutputStream(Tools.SETTINGS_FILE),
          "CopyPasta settings file\nVersion: " + Tools.VERSION);
    } catch (IOException e) {
      IO.showExceptionAlert(e);
    }
  }

  public static boolean getRunningFile() {
    FIRST_RUN = Tools.IS_RUNNING_FILE.exists();
    return Boolean.parseBoolean(IO.getFileAsString(Tools.IS_RUNNING_FILE).replaceAll("\\s+", ""));
  }

  public static void setRunningFile(boolean value) {
    IO.printStringToFile(value + "", Tools.IS_RUNNING_FILE);
  }
}
