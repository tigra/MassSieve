package gov.nih.nimh.mass_sieve.actions;

import gov.nih.nimh.mass_sieve.FilterSettings;

/**
 *
 * @author Alex Turbin (alex.academATgmail.com)
 */
public class ActionBuilder {
    
    private MergeAction action = new MergeAction();

    public void setExperimentName(String name)
    {
        action.experimentName = name;
    }

    public void addSearchFile(String file)
    {
        action.addInputFileName(file);
    }

    public void addExportExperimentsDatabase(String exportExpDBFilename) {
        action.exportExpDBFilename = exportExpDBFilename;
    }

    public void addExportExperimentsResults(String exportExpResFilesname) {
        action.exportExpResFilename = exportExpResFilesname;
    }

    public void addExportPreferredProteins(String exportPrefProtFilename) {
        action.exportPrefProtFilename = exportPrefProtFilename;
    }

    public void addSaveExperiment(String saveExpFilename) {
        action.saveExpFilename = saveExpFilename;
    }

    public void setFilter(FilterSettings filter) {
        action.filter = filter;
    }

    public MergeAction createAction() {
        return action;
    }

}
