package gov.nih.nimh.mass_sieve;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.*;

/**
 *
 * @author Alex Turbin (alex.academATgmail.com)
 */
public class TestBase {

    /**
     * For each required file creates corresponding File object. 
     * If no required files provided, returns default test file. <br/>
     * Each of required files must exist.
     */
    protected File[] getSeqFiles(String... requiredFiles) {
        List<File> result = new ArrayList<File>();

        // add files to a list
        if (null == requiredFiles || requiredFiles.length == 0) {
            result.add(new File(TestConstants.DIR_DATA, TestConstants.DEF_TEST_FILE));
        } else {
            for (String requiredFile : requiredFiles) {
                result.add(new File(TestConstants.DIR_DATA, requiredFile));
            }
        }

        // check if required files exists
        for (File f : result) {
            if (!f.exists()) {
                fail("File doesn't exist:" + f.getAbsolutePath());
            }
        }

        return result.toArray(new File[result.size()]);
    }

}
