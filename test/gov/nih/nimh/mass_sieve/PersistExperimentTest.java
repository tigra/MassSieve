package gov.nih.nimh.mass_sieve;

import gov.nih.nimh.mass_sieve.logic.DataStoreException;
import gov.nih.nimh.mass_sieve.logic.ExperimentsBundle;
import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.junit.*;
import static org.junit.Assert.*;

/**
 *
 * @author Alex Turbin (alex.academATgmail.com)
 */
public class PersistExperimentTest extends TestBase {

    private DummyExperimentManager man;

    @Before
    public void setUp() {
        man = new DummyExperimentManager();
    }

    @Test
    public void testSaveExperiment() throws DataStoreException {
        ExperimentData exp1 = createExperiment("test_1");
        ExperimentData exp2 = createExperiment("test_2");

        List<Experiment> experiments = new ArrayList<Experiment>();
        experiments.add(man.getPersistentExperiment(exp1));
        experiments.add(man.getPersistentExperiment(exp2));
        ExperimentsBundle eb = new ExperimentsBundle(experiments, ProteinDB.Instance.getMap());

        String outFileName = "save_experiments.bin";
        File outFile = new File(TestConstants.OUT_DIR, outFileName);
        man.saveExperimentsBundle(eb, outFile);
    }

    @Test
    public void testLoadExperiment() throws DataStoreException {
        File expFile = new File(TestConstants.DATA_DIR, "load_experiments.bin");
        assertTrue("Test file must exist: " + expFile.getAbsolutePath(), expFile.exists());

        ExperimentsBundle eb = man.loadExperimentsBundle(expFile);
        List<Experiment> experiments = eb.getExperiments();
        Map<String, ProteinInfo> proteinInfos = eb.getProteinInfos();

        assertTrue("Loaded empty experiments file.", !experiments.isEmpty());
        assertTrue("Loaded empty protein database.", !proteinInfos.isEmpty());
    }

    private ExperimentData createExperiment(String expName) {
        File[] files = getSeqFiles();
        ExperimentData expData = man.createNewExperiment(expName);
        expData.setFilterSettings(new FilterSettings());

        for (File f : files) {
            List<ProteinInfo> list = man.addFilesToExperiment(expData, f);
            for (ProteinInfo info : list) {
                ProteinDB.Instance.add(info);
            }
        }

        return expData;
    }
}
