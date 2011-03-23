package gov.nih.nimh.mass_sieve.cli;

import gov.nih.nimh.mass_sieve.logic.ActionExecutor;
import gov.nih.nimh.mass_sieve.actions.MergeAction;
import gov.nih.nimh.mass_sieve.actions.ActionBuilder;
import gov.nih.nimh.mass_sieve.FilterSettings;
import gov.nih.nimh.mass_sieve.util.LogStub;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.GnuParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;

/**
 *
 * @author Alex Turbin (alex.academATgmail.com)
 */
public class LauncherCLI {

    private static final String OPT_NEW_EXPERIMENT = "new-experiment";
    private static final String OPT_ADD_SEARCH_RESULTS = "add-search-results";
    private static final String OPT_EXPORT_EXPERIMENTS_DATABASE = "export-experiments-database";
    private static final String OPT_EXPORT_EXPERIMENTS_RESULTS = "export-experiments-results";
    private static final String OPT_EXPORT_PREFERRED_PROTEINS = "export-preferred-proteins";
    private static final String OPT_SAVE_EXPERIMENT = "save-experiment";
    private static final String FILTER_OMSSA_CUTOFF = "filter-omssa-cutoff";
    private static final String FILTER_XTANDEM_CUTOFF = "filter-xtandem-cutoff";
    private static final String FILTER_MASCOT_CUTOFF = "filter-mascot-cutoff";
    private static final String FILTER_SEQUEST_CUTOFF = "filter-sequest-cutoff";
    private static final String FILTER_PEP_PROPHET_CUTOFF = "filter-pep-prophet-cutoff";
    private static final String FILTER_ESTFDR_CUTOFF = "filter-estfdr-cutoff";
    private static final String FILTER_TEXT = "filter-text";
    private static final String FILTER_USE_ION_IDENT = "filter-use-ion-ident";
    private static final String FILTER_USE_PEP_PROPHET = "filter-use-pep-prophet";
    private static final String FILTER_USE_INDETERMINATES = "filter-use-indeterminates";
    private static final String FILTER_USE_PEPTIDES = "filter-use-peptides";
    private static final String FILTER_PEPHIT_CUTOFF = "filter-pephit-cutoff";
    private static final String FILTER_USE_PROTEINS = "filter-use-proteins";
    private static final String FILTER_PEPTIDE_CUTOFF = "filter-peptide-cutoff";
    private final Options cmdOptions;

    public LauncherCLI() {
        cmdOptions = defineOptions();
    }

    public int run(String[] args) {
        try {
            ActionExecutor executor = new ActionExecutor();
            MergeAction action = parseOptions(cmdOptions, args);
            executor.perform(action);
        } catch (ParseException e) {
            //TODO: to log file
            // LogStub.error(e);
            printHelp(e.getMessage());
            return -1;
        } catch (Throwable t)
        {
            LogStub.error(t);
            printHelp(null);
            return -2;
        }
        return 0;
    }

    private void printHelp(String message)
    {
        HelpFormatter formatter = new HelpFormatter();
        formatter.printHelp("mass_sieve <options>", "Available options:", cmdOptions, message);
    }

    private Options defineOptions() {
        Options options = new Options();

        Option newExp = new Option("n", OPT_NEW_EXPERIMENT, true, "Creates new experiment with specified name");
        newExp.setRequired(true);
        Option addSearch = new Option("a", OPT_ADD_SEARCH_RESULTS, true, "Adds a file as a search result to experiment");
        addSearch.setRequired(true);
        
        options.addOption(newExp);
        options.addOption(addSearch);
        options.addOption(null, OPT_EXPORT_EXPERIMENTS_DATABASE, true, "Filename to save experiment database");
        options.addOption(null, OPT_EXPORT_EXPERIMENTS_RESULTS, true, "Filename to save experiment results");
        options.addOption(null, OPT_EXPORT_PREFERRED_PROTEINS, true, "Filename to save perferred proteins from experiment");
        options.addOption(null, OPT_SAVE_EXPERIMENT, true, "Filename to save whole experiment");

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

        return options;
    }

    private MergeAction parseOptions(Options opts, String[] args) throws ParseException {
        CommandLineParser parser = new GnuParser();
        CommandLine cmd = parser.parse(opts, args);

        ActionBuilder actionBuilder = new ActionBuilder();

        if (cmd.hasOption(OPT_NEW_EXPERIMENT)) {
            String expName = cmd.getOptionValue(OPT_NEW_EXPERIMENT);
            actionBuilder.setExperimentName(expName);
        }

        if (cmd.hasOption(OPT_ADD_SEARCH_RESULTS)) {
            String[] searches = cmd.getOptionValues(OPT_ADD_SEARCH_RESULTS);
            if (null != searches) {
                for (String s : searches) {
                    actionBuilder.addSearchFile(s);
                }
            }
        }

        boolean exportOptionSpecified = false;
        if (cmd.hasOption(OPT_EXPORT_EXPERIMENTS_DATABASE)) {
            String exportExpDBFilename = cmd.getOptionValue(OPT_EXPORT_EXPERIMENTS_DATABASE);
            actionBuilder.addExportExperimentsDatabase(exportExpDBFilename);
            exportOptionSpecified = true;
        }
        if (cmd.hasOption(OPT_EXPORT_EXPERIMENTS_RESULTS)) {
            String exportExpResFilesname = cmd.getOptionValue(OPT_EXPORT_EXPERIMENTS_RESULTS);
            actionBuilder.addExportExperimentsResults(exportExpResFilesname);
            exportOptionSpecified = true;
        }
        if (cmd.hasOption(OPT_EXPORT_PREFERRED_PROTEINS)) {
            String exportPrefProtFilename = cmd.getOptionValue(OPT_EXPORT_PREFERRED_PROTEINS);
            actionBuilder.addExportPreferredProteins(exportPrefProtFilename);
            exportOptionSpecified = true;
        }
        if (cmd.hasOption(OPT_SAVE_EXPERIMENT)) {
            String saveExpFilename = cmd.getOptionValue(OPT_SAVE_EXPERIMENT);
            actionBuilder.addSaveExperiment(saveExpFilename);
            exportOptionSpecified = true;
        }

        FilterSettings filter = parseFilterSettings(cmd);
        actionBuilder.setFilter(filter);

        if (!exportOptionSpecified)
        {
            throw new ParseException("No export options specified.");
        }

        MergeAction action = null;
        if (exportOptionSpecified)
        {
            action = actionBuilder.createAction();
        }

        return action;
    }

    private FilterSettings parseFilterSettings(CommandLine cmd) {
        FilterSettings filter = new FilterSettings();

        if (cmd.hasOption(FILTER_OMSSA_CUTOFF)) {
            String paramOmssaCutoff = cmd.getOptionValue(FILTER_OMSSA_CUTOFF);
            filter.setOmssaCutoff(paramOmssaCutoff);
        }
        if (cmd.hasOption(FILTER_XTANDEM_CUTOFF)) {
            String paramXTandemCutoff = cmd.getOptionValue(FILTER_XTANDEM_CUTOFF);
            filter.setXtandemCutoff(paramXTandemCutoff);
        }
        if (cmd.hasOption(FILTER_MASCOT_CUTOFF)) {
            String paramMascotCutoff = cmd.getOptionValue(FILTER_MASCOT_CUTOFF);
            filter.setMascotCutoff(paramMascotCutoff);
        }
        if (cmd.hasOption(FILTER_SEQUEST_CUTOFF)) {
            String paramSequestCutoff = cmd.getOptionValue(FILTER_SEQUEST_CUTOFF);
            filter.setSequestCutoff(paramSequestCutoff);
        }
        if (cmd.hasOption(FILTER_PEP_PROPHET_CUTOFF)) {
            String paramPepProCutoff = cmd.getOptionValue(FILTER_PEP_PROPHET_CUTOFF);
            filter.setPeptideProphetCutoff(paramPepProCutoff);
        }

        if (cmd.hasOption(FILTER_ESTFDR_CUTOFF)) {
            String paramEstFdrCutoff = cmd.getOptionValue(FILTER_ESTFDR_CUTOFF);
            filter.setEstimatedFdrCutoff(paramEstFdrCutoff);
        }
        if (cmd.hasOption(FILTER_TEXT)) {
            String paramFilterText = cmd.getOptionValue(FILTER_TEXT);
            filter.setFilterText(paramFilterText);
        }
        if (cmd.hasOption(FILTER_USE_ION_IDENT)) {
            String paramUseIonIdent = cmd.getOptionValue(FILTER_USE_ION_IDENT);
            filter.setUseIonIdent(!"0".equals(paramUseIonIdent));
        }
        if (cmd.hasOption(FILTER_USE_PEP_PROPHET)) {
            String paramUsePepProphet = cmd.getOptionValue(FILTER_USE_PEP_PROPHET);
            filter.setUsePepProphet(!"0".equals(paramUsePepProphet));
        }
        if (cmd.hasOption(FILTER_USE_INDETERMINATES)) {
            String paramUseIndet = cmd.getOptionValue(FILTER_USE_INDETERMINATES);
            filter.setUseIndeterminates(!"0".equals(paramUseIndet));
        }

        if (cmd.hasOption(FILTER_USE_PEPTIDES)) {
            String paramFilterPeptides = cmd.getOptionValue(FILTER_USE_PEPTIDES);
            filter.setFilterPeptides(!"0".equals(paramFilterPeptides));
        }
        if (cmd.hasOption(FILTER_PEPHIT_CUTOFF)) {
            String paramPepHitCutoff = cmd.getOptionValue(FILTER_PEPHIT_CUTOFF);
            filter.setPepHitCutoffCount(Integer.parseInt(paramPepHitCutoff));
        }
        if (cmd.hasOption(FILTER_USE_PROTEINS)) {
            String paramFilterProteins = cmd.getOptionValue(FILTER_USE_PROTEINS);
            filter.setFilterProteins(!"0".equals(paramFilterProteins));
        }
        if (cmd.hasOption(FILTER_PEPTIDE_CUTOFF)) {
            String paramPeptideCutoff = cmd.getOptionValue(FILTER_PEPTIDE_CUTOFF);
            filter.setPeptideCutoffCount(Integer.parseInt(paramPeptideCutoff));
        }
        return filter;

    }

    public static void main(String[] args) {
        LauncherCLI launcher = new LauncherCLI();
        int code = launcher.run(args);
        System.exit(code);
    }
}
