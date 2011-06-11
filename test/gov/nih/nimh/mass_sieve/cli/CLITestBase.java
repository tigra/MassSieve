package gov.nih.nimh.mass_sieve.cli;

import java.io.File;

import static org.junit.Assert.*;

/**
 *
 * @author Alex Turbin (alex.academATgmail.com)
 */
public class CLITestBase {

    public void checkFileNotEmpty(String filename) {
        File realFile = new File(filename);
        boolean exists = realFile.exists() && realFile.isFile();
        assertEquals("File must exist:" + realFile.getAbsolutePath(), true, exists);
        boolean notEmpty = realFile.length() > 0;
        assertEquals("File must be not empty:" + realFile.getAbsolutePath(), true, notEmpty);
    }

}
