package gov.nih.nimh.mass_sieve.logic;

import gov.nih.nimh.mass_sieve.actions.MergeAction;
import gov.nih.nimh.mass_sieve.AppConfig;
import gov.nih.nimh.mass_sieve.ExperimentData;
import gov.nih.nimh.mass_sieve.ExportProteinType;
import gov.nih.nimh.mass_sieve.util.LogStub;
import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author Alex Turbin (alex.academATgmail.com)
 */
public class ActionExecutor {

    public void perform(MergeAction action)
    {
        performMergeAction(action);
    }

    private void performMergeAction(MergeAction action)
    {
        ExperimentManager man = new ExperimentManager();

        String expName = action.getExperimentName();
        ExperimentData exp = man.createNewExperiment(expName);
        exp.setFilterSettings(action.getFilterSettings());
        List<File> files = searchForFiles(action.getFileNames());
        for (File f : files) {
            man.addFileToExperiment(exp, f, null, null);
        }
        man.recomputeCutoff(exp);
        
        String exportExpDBfilename = action.getExportExpDBFilename();
        if (null != exportExpDBfilename)
        {
            File file = new File(exportExpDBfilename);
            man.exportDatabase(file, exp.getPepCollection());
        }

        String exportExpResFilename = action.getExportExpResFilename();
        if (null != exportExpResFilename)
        {
            File file = new File(exportExpResFilename);
            man.exportResults(file, exp.getPepCollection(), ExportProteinType.ALL);
        }

        String exportPrefProtFilename = action.getExportPrefProtFilename();
        if (null != exportPrefProtFilename)
        {
            File file = new File(exportPrefProtFilename);
            man.exportResults(file, exp.getPepCollection(), ExportProteinType.PREFERRED);
        }

        String saveExpFilename = action.getSaveExpFilename();
        if (null != saveExpFilename)
        {
            File file = new File(saveExpFilename);
            try {
                man.saveExperiment(exp, file);
            } catch (DataStoreException ex) {
                //TODO: indicate about error to the calling environment.
                LogStub.error(ex);
            }
        }

    }

    private List<File> searchForFiles(List<String> fileNames) {
        List<File> result = new ArrayList<File>();
        if (null == fileNames)
            return result;

        for (String fileName : fileNames ) {
            // try by absolute path
            File file = new File(fileName);
            if (!file.exists())
            {
                // try current directory
                file = new File(".", fileName);
                if (!file.exists())
                {
                    // try default directory
                    String root = AppConfig.getDefaultSearchFilesDirectory();
                    file = new File(root, fileName);
                }
            }

            if (file.exists())
            {
                result.add(file);
            }
            else
            {
                LogStub.warn("Couldn't find file:" + fileName);
            }
        }

        return result;
    }

}
