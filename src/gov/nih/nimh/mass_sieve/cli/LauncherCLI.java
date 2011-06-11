package gov.nih.nimh.mass_sieve.cli;

import gov.nih.nimh.mass_sieve.actions.CompareExpParsimonyActionBuilder;
import gov.nih.nimh.mass_sieve.actions.ExternalAction;
import gov.nih.nimh.mass_sieve.logic.ActionExecutor;
import gov.nih.nimh.mass_sieve.actions.MergeActionBuilder;
import gov.nih.nimh.mass_sieve.actions.ActionBuilder;
import gov.nih.nimh.mass_sieve.actions.CompareExpDiffActionBuilder;
import gov.nih.nimh.mass_sieve.logic.ActionResult;
import gov.nih.nimh.mass_sieve.util.LogStub;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.GnuParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;

import static gov.nih.nimh.mass_sieve.cli.CLI.*;

/**
 *
 * @author Alex Turbin (alex.academATgmail.com)
 */
public class LauncherCLI {

    private final Options cmdOptions;

    public LauncherCLI() {
        cmdOptions = defineOptions();
    }

    public int run(String[] args) {
        try {
            ActionExecutor executor = new ActionExecutor();
            ExternalAction action = parseOptions(cmdOptions, args);
            ActionResult actionResult = executor.perform(action);
            return (actionResult == ActionResult.SUCCESS) ? 0 : -1;
        } catch (CLIParseException e) {
            //TODO: to log file
            // LogStub.error(e);
            printHelp(e.getMessage());
            return -2;
        } catch (Throwable t)
        {
            LogStub.error(t);
            printHelp(null);
            return -3;
        }
    }

    private void printHelp(String message)
    {
        HelpFormatter formatter = new HelpFormatter();
        formatter.printHelp("mass_sieve <options>", "Available options:", cmdOptions, message);
    }

    private Options defineOptions() {
        Options options = new Options();

        Option actionName = new Option(OPT_ACTION, true, "Name of the action");
        actionName.setRequired(true);
        options.addOption(actionName);

        Option newExp = new Option("n", OPT_NEW_EXPERIMENT, true, "Creates new experiment with specified name");
        newExp.setRequired(false);
        Option addSearch = new Option("a", OPT_ADD_SEARCH_RESULTS, true, "Adds a file as a search result to experiment");
        addSearch.setRequired(false);
        //TODO: use groups or validate fullness by hand.
        
        options.addOption(newExp);
        options.addOption(addSearch);
        options.addOption(null, OPT_EXPORT_EXPERIMENTS_DATABASE, true, "Filename to save experiment database");
        options.addOption(null, OPT_EXPORT_EXPERIMENTS_RESULTS, true, "Filename to save experiment results");
        options.addOption(null, OPT_EXPORT_PREFERRED_PROTEINS, true, "Filename to save perferred proteins from experiment");
        options.addOption(null, OPT_SAVE_EXPERIMENT, true, "Filename to save whole experiment");

        options.addOption("ae", OPT_ADD_EXPERIMENT, true, "");
        options.addOption("ece", OPT_EXPORT_COMPARE_EXPS, true, "");

        options.addOption(null, FILTER_OMSSA_CUTOFF, true, "");
        options.addOption(null, FILTER_XTANDEM_CUTOFF, true, "");
        options.addOption(null, FILTER_MASCOT_CUTOFF, true, "");
        options.addOption(null, FILTER_SEQUEST_CUTOFF, true, "");
        options.addOption(null, FILTER_PEP_PROPHET_CUTOFF, true, "");
        options.addOption(null, FILTER_ESTFDR_CUTOFF, true, "");
        options.addOption(null, FILTER_TEXT, true, "");
        options.addOption(null, FILTER_USE_ION_IDENT, true, "");
        options.addOption(null, FILTER_USE_PEP_PROPHET, true, "");
        options.addOption(null, FILTER_USE_INDETERMINATES, true, "");

        options.addOption(null, FILTER_USE_PEPTIDES, true, "");
        options.addOption(null, FILTER_PEPHIT_CUTOFF, true, "");
        options.addOption(null, FILTER_USE_PROTEINS, true, "");
        options.addOption(null, FILTER_PEPTIDE_CUTOFF, true, "");
        options.addOption(null, FILTER_USE_PROTEINS_COVERAGE, true, "");
        options.addOption(null, FILTER_PROTEINS_COVERAGE, true, "");

        return options;
    }

    private ExternalAction parseOptions(Options opts, String[] args) throws CLIParseException {
        CommandLineParser parser = new GnuParser();
        CommandLine cmd = null;
        try {
            cmd = parser.parse(opts, args);
        } catch (ParseException ex) {
            throw new CLIParseException(ex);
        }

        ActionBuilder actionBuilder = createActionBuilder(cmd);
        ExternalAction action = actionBuilder.createAction(cmd);

        return action;
    }

    private ActionBuilder createActionBuilder(CommandLine cmd) {
        ActionBuilder result = null;

        String actionName = cmd.getOptionValue(OPT_ACTION);
        if (ACTION_NAME_MERGE.equalsIgnoreCase(actionName))
        {
            result = new MergeActionBuilder();
        }
        else if (ACTION_NAME_COMPARE.equalsIgnoreCase(actionName))
        {
            result = new CompareExpDiffActionBuilder();
        }
        else if (ACTION_NAME_COMPARE_PARSIMONY.equalsIgnoreCase(actionName))
        {
            result = new CompareExpParsimonyActionBuilder();
        }

        return result;
    }

    public static void main(String[] args) {
        LauncherCLI launcher = new LauncherCLI();
        int code = launcher.run(args);
        System.exit(code);
    }

}
