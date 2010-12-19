/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package gov.nih.nimh.mass_sieve;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.junit.*;
import static org.junit.Assert.*;

/**
 *
 * @author Alex Turbin (alex.academATgmail.com)
 */
public class TestBase {

    protected File[] getSeqFiles() {
        List<File> result = new ArrayList<File>();
        File f = new File(TestConstants.DATA_DIR, "238_sax.cmn.mgf.pep.xml");
        if (!f.exists()) {
            fail("File doesn't exist:" + f.getAbsolutePath());
        }

        result.add(f);
        return result.toArray(new File[result.size()]);
    }
}
