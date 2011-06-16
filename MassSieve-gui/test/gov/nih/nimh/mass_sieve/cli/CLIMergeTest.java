package gov.nih.nimh.mass_sieve.cli;

import java.io.File;
import gov.nih.nimh.mass_sieve.TestConstants;
import org.junit.*;
import static org.junit.Assert.*;

import static gov.nih.nimh.mass_sieve.TestConstants.*;

/**
 *
 * @author Alex Turbin (alex.academATgmail.com)
 */
public class CLIMergeTest extends CLITestBase {

    private String inputSearchFile1 = "./"+DIR_DATA+"/"+DEF_TEST_FILE;
    private String inputSearchFile2 = "./"+DIR_DATA+"/"+DEF_TEST_FILE_2;
    private String outputExportDatabaseFilename = "./"+DIR_OUT+"/"+"cli_MT_export_db";
    private String outputExportResultsFilename = "./"+DIR_OUT+"/"+"cli_MT_export_results";
    private String outputExportPreferredFilename = "./"+DIR_OUT+"/"+"cli_MT_export_pref";
    private String outputExperimentFilename = "./"+DIR_OUT+"/"+"cli_MT_experiment";
    private File outputDir;
    private boolean outputDirCreated = false;

    @Before
    public void setUp() {
        outputDir = new File(TestConstants.DIR_OUT);

        deleteResultingTestFiles();
        if (!outputDir.exists()) {
            outputDir.mkdir();
            outputDirCreated = true;
        }
    }

    @After
    public void tearDown() {
        // deleteResultingTestFiles();
        if (outputDirCreated) {
            outputDir.delete();
        }
    }

    private void deleteResultingTestFiles() {
        deleteFileIfExists(outputExportDatabaseFilename);
        deleteFileIfExists(outputExportResultsFilename);
        deleteFileIfExists(outputExportPreferredFilename);
        deleteFileIfExists(outputExperimentFilename);
    }

    private void deleteFileIfExists(String filename) {
        File file = new File(filename);
        if (file.exists()) {
            file.delete();
        }
    }

    @Test
    public void test() {
        String[] cmdParams = {
            "-do", CLI.ACTION_NAME_MERGE,
            "-n", "MergeExperiment",
            "-a", inputSearchFile1,
            "-a", inputSearchFile2,
            "--export-experiments-database",
            outputExportDatabaseFilename,
            "--export-experiments-results",
            outputExportResultsFilename,
            "--export-preferred-proteins",
            outputExportPreferredFilename,
            "--save-experiment",
            outputExperimentFilename
        };

        LauncherCLI launcher = new LauncherCLI();
        int exitCode = launcher.run(cmdParams);

        assertEquals(0, exitCode);

        checkFileNotEmpty(outputExportDatabaseFilename);
        checkFileNotEmpty(outputExportResultsFilename);
        checkFileNotEmpty(outputExportPreferredFilename);
        checkFileNotEmpty(outputExperimentFilename);
    }
}
