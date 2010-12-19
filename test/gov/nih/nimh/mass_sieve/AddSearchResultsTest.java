package gov.nih.nimh.mass_sieve;

import java.util.Map;
import java.io.File;
import java.util.HashMap;
import java.util.List;
import org.junit.*;
import static org.junit.Assert.*;

/**
 *
 * @author Alex Turbin (alex.academATgmail.com)
 */
public class AddSearchResultsTest extends TestBase {

    private DummyExperimentManager man;

    @Before
    public void setUp() {
        man = new DummyExperimentManager();
    }

    @Test
    public void testAddSearchResults() throws InterruptedException {
        File[] files = getSeqFiles();
        ExperimentData expData = man.createNewExperiment("test");
        expData.setFilterSettings(new FilterSettings());

        Map<String, ProteinInfo> res = new HashMap<String, ProteinInfo>();
        for (File f : files) {
            List<ProteinInfo> list = man.addFilesToExperiment(expData, f);
            for (ProteinInfo info : list) {
                res.put(info.getName(), info);
            }
        }

        assertFalse("Loaded proteins are empty", res.isEmpty());
        //TODO: add other checks that files are added fully and correctly (by examine expData)
    }
}
