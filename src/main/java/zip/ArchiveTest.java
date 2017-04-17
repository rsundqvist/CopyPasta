package zip;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

/**
 * Created by Richard Sundqvist on 13/04/2017.
 */
public class ArchiveTest {

    private static final String INPUT_ZIP_FILE = "C:\\Users\\Richard Sundqvist\\Desktop\\ziptest.zip";
    private static final String INPUT_ZIP_FILE2 = "C:\\Users\\Richard Sundqvist\\Desktop\\ziptest2.zip";
    private static final String INPUT_ZIP_FILE3 = "C:\\Users\\Richard Sundqvist\\Desktop\\ziptest.7z";
    private static final String OUTPUT_FOLDER = "C:\\Users\\Richard Sundqvist\\Desktop\\zipout";

    public static void main (String[] args) throws Exception {
        System.out.println("ARCHIVE TESTER BEGIN");

        File out = new File(OUTPUT_FOLDER);
        if (!out.exists())
            out.mkdir();

        System.out.println(INPUT_ZIP_FILE);
        //unzip(new File(INPUT_ZIP_FILE), out);

        System.out.println();

        System.out.println(INPUT_ZIP_FILE2);
        //unzip(new File(INPUT_ZIP_FILE2), out);

        System.out.println();

        System.out.println(INPUT_ZIP_FILE3);
        unzip(new File(INPUT_ZIP_FILE3), out);

        System.out.println("ARCHIVE TESTER END");
    }

    public static void unzip (File inFile, File outDirectory) throws Exception {
        byte[] buffer = new byte[1024];

        ZipInputStream zis =
                new ZipInputStream(new FileInputStream(inFile));
        //get the zipped file list entry
        ZipEntry zipEntry;

        while ((zipEntry = zis.getNextEntry()) != null) {
            String fileName = zipEntry.getName();
            File newFile = new File(outDirectory.getPath() + File.separator + fileName);

            System.out.println("\tfile unzip : " + newFile.getAbsoluteFile());

            //create all non exists folders
            //else you will hit FileNotFoundException for compressed folder
            new File(newFile.getParent()).mkdirs();

            FileOutputStream fos = new FileOutputStream(newFile);

            int len;
            while ((len = zis.read(buffer)) > 0) {
                fos.write(buffer, 0, len);
            }

            fos.close();
        }

        zis.closeEntry();
        zis.close();

        System.out.println("Done");
    }
}
