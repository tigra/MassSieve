package gov.nih.nimh.mass_sieve;

import java.io.File;
import org.junit.*;
import static org.junit.Assert.*;

/**
 *
 * @author Alex Turbin (alex.academATgmail.com)
 */
public class ExportExperimentsDatabaseTest extends TestBase {

    private DummyExperimentManager man;
    private ExperimentData expData;

    @Before
    public void setUp() {
        man = new DummyExperimentManager();
        expData = man.createNewExperiment("test");
        expData.setFilterSettings(new FilterSettings());

        File[] files = getSeqFiles();
        importData(files, man, expData);
    }

    @Test
    public void exportDatabase() {
        String outFilename = "export_experiments_database.txt";
        File outFile = new File(TestConstants.DIR_OUT, outFilename);

        PeptideCollection pepCollection = expData.getPepCollection();
        man.exportDatabase(outFile, pepCollection);

        // validate
        assertEquals("Output file doesn't exist", true, outFile.exists());
    }
}
