package gov.nih.nimh.mass_sieve.cli;

import org.junit.*;
import static org.junit.Assert.*;

import static gov.nih.nimh.mass_sieve.TestConstants.*;

/**
 *
 * @author Alex Turbin (alex.academATgmail.com)
 */
public class CLICompareDiffExpParsTest extends CLITestBase {

    private String input_exp_1 = "./" + DIR_DATA + "/" + DEF_EXP_FILE_1;
    private String input_exp_2 = "./" + DIR_DATA + "/" + DEF_EXP_FILE_2;
    private String outputExportDatabaseFilename = "./" + DIR_OUT + "/" + "cli_CDEP_export_db";
    private String outputExportResultsFilename = "./" + DIR_OUT + "/" + "cli_CDEP_export_results";
    private String outputExportPreferredFilename = "./" + DIR_OUT + "/" + "cli_CDEP_export_pref";
    private String outputExperimentFilename = "./" + DIR_OUT + "/" + "cli_CDEP_experiment";

    @Test
    public void test() {
        String[] cmdParams = {
            "-do", CLI.ACTION_NAME_COMPARE_PARSIMONY,
            "-n", "CompareExpParsimony",
            "-ae", input_exp_1,
            "-ae", input_exp_2,
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
