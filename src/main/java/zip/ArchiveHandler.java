package zip;

import model.IO;
import org.codehaus.plexus.archiver.AbstractUnArchiver;
import org.codehaus.plexus.archiver.tar.TarGZipUnArchiver;
import org.codehaus.plexus.archiver.tar.TarUnArchiver;
import org.codehaus.plexus.archiver.zip.ZipUnArchiver;
import org.codehaus.plexus.logging.console.ConsoleLoggerManager;

import javax.swing.*;
import java.io.File;
import java.util.Arrays;

public class ArchiveHandler {
  public static final String[] ARCHIVE_TYPES = {"zip", "tar", "gz", "gzip"};

  public static boolean isArchive(File file) {
    String[] s = file.getName().split("\\.");
    if (s.length < 2) return false;

    String ext = s[s.length - 1].toLowerCase();
    return Arrays.asList(ARCHIVE_TYPES).contains(ext);
  }

  /**
   * Attempt to extract an archive in the target folder. Must be one of the supported types, see
   * {@link #ARCHIVE_TYPES}.
   *
   * @param archive The archive to extract.
   * @param target The target directory.
   * @param exhaustive If {@code true}, attempt to use all other extension types if there's an
   *     exception.
   * @return {@code false} if the extension type was unknown or if exhaustive extraction was used.
   */
  public static boolean extractArchive(File archive, File target, boolean exhaustive) {

    String[] s = archive.getName().split("\\.");
    String ext = s[s.length - 1].toLowerCase();
    try {
      return switchOnExtension(archive, target, ext);
    } catch (Exception e) {
      if (exhaustive) extractArchiveExhaustive(archive, target);
      else throw e;
    }
    return false;
  }

  private static void extractArchiveExhaustive(File archive, File target) {
    for (String ext : ARCHIVE_TYPES) {
      try {
        switchOnExtension(archive, target, ext);
        return; // No error? We're done!
      } catch (Exception e) {
        // Try another extension.
      }
    }
  }

  private static boolean switchOnExtension(File archive, File target, String ext) {
    switch (ext) {
      case "zip":
        extractZip(archive, target);
        break;
      case "tar":
        extractTar(archive, target);
        break;
      case "gz":
        extractTarGz(archive, target);
        break;
      case "gzip":
        extractGzip(archive, target);
        break;
      default:
        IO.showExceptionAlert(new Exception("Cannot extract \"" + ext + "\" archives."));
        return false;
    }
    return true;
  }

  private static void extractZip(File archive, File target) {
    ZipUnArchiver archiver = new ZipUnArchiver(archive);
    extractArchive(target, archiver);
  }

  private static void extractTar(File archive, File target) {
    TarUnArchiver archiver = new TarUnArchiver(archive);
    extractArchive(target, archiver);
  }

  private static void extractTarGz(File archive, File target) {
    TarUnArchiver archiver = new TarUnArchiver(archive);
    extractArchive(target, archiver);
  }

  private static void extractGzip(File archive, File target) {
    TarGZipUnArchiver archiver = new TarGZipUnArchiver(archive);
    extractArchive(target, archiver);
  }

  private static void extractArchive(File target, AbstractUnArchiver archiver) {

    ConsoleLoggerManager manager = new ConsoleLoggerManager();
    manager.initialize();

    archiver.enableLogging(manager.getLoggerForComponent("Archiver"));
    archiver.setOverwrite(true);
    archiver.setDestDirectory(target);
    archiver.extract();
  }

  /** Test */
  public static void main(String[] args) {
    File desktop = new File(System.getProperty("user.home") + "/Desktop");
    JFileChooser jFileChooser = new JFileChooser(desktop);
    if (jFileChooser.showOpenDialog(null) == JFileChooser.APPROVE_OPTION) {
      File file = jFileChooser.getSelectedFile();
      extractGzip(file, desktop);
    }
    System.out.println("Done.");
  }
}
