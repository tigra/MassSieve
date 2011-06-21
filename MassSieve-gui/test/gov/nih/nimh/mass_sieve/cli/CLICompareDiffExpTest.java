package gov.nih.nimh.mass_sieve.cli;

import org.junit.*;
import static org.junit.Assert.*;

import static gov.nih.nimh.mass_sieve.TestConstants.*;

/**
 *
 * @author Alex Turbin (alex.academATgmail.com)
 */
public class CLICompareDiffExpTest extends CLITestBase {
    private String input_exp_1 = "./"+DIR_DATA+"/"+DEF_EXP_FILE_1;
    private String input_exp_2 = "./"+DIR_DATA+"/"+DEF_EXP_FILE_2;
    private String export_result_filename = "./"+DIR_OUT+"/"+"cli_CDE_export_diff_exps";

    @Test
    public void test()
    {
        String[] cmdParams = {
            "-do", CLI.ACTION_NAME_COMPARE,
            "-n", "CompareExperiment",
            "-add-experiment", input_exp_1,
            "-add-experiment", input_exp_2,
            "-ece", export_result_filename
        };

        LauncherCLI launcher = new LauncherCLI();
        int exitCode = launcher.run(cmdParams);

        assertEquals(0, exitCode);
        checkFileNotEmpty(export_result_filename);
    }

}
