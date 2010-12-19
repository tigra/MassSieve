/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package gov.nih.nimh.mass_sieve;

import gov.nih.nimh.mass_sieve.logic.ActionResponse;
import gov.nih.nimh.mass_sieve.logic.DataStoreException;
import gov.nih.nimh.mass_sieve.logic.ExperimentManager;
import gov.nih.nimh.mass_sieve.logic.ExperimentsBundle;
import gov.nih.nimh.mass_sieve.tasks.DeterminedTaskListener;
import gov.nih.nimh.mass_sieve.tasks.InputStreamObserver;
import gov.nih.nimh.mass_sieve.tasks.InputStreamProgressObserver;
import gov.nih.nimh.mass_sieve.tasks.TaskListener;
import java.io.File;
import java.util.List;

/**
 *
 * @author Alex Turbin (alex.academATgmail.com)
 */
public class DummyExperimentManager {

    private ExperimentManager man;
    private DeterminedTaskListener dtl;

    public DummyExperimentManager() {
        man = new ExperimentManager();

        dtl = new DeterminedTaskListener() {

            public void onChangeStepName(String stepName) {
            }

            public void onProgress(int curValue, int totalValue) {
            }

            public void onFinish() {
            }
        };
    }

    public List<ProteinInfo> addFilesToExperiment(ExperimentData expData, File f) {
        TaskListener taskListener = new DummyTaskListener();
        InputStreamObserver inObserver = new InputStreamProgressObserver(taskListener);
        return man.addFilesToExperiment(expData, f, inObserver, dtl);
    }

    public ExperimentData createNewExperiment(String name) {
        return man.createNewExperiment(name);
    }

    public ActionResponse exportResults(File file, PeptideCollection pepCollection, ExportProteinType type)  {
        return man.exportResults(file, pepCollection, type);
    }

    void recomputeCutoff(ExperimentData expData) {
        man.recomputeCutoff(expData);
    }

    public void saveExperimentsBundle(ExperimentsBundle eb, File f) throws DataStoreException {
        man.saveExperimentsBundle(eb, f);
    }

    public Experiment getPersistentExperiment(ExperimentData expData) {
        return man.getPersistentExperiment(expData);
    }

    public ExperimentsBundle loadExperimentsBundle(File f) throws DataStoreException {
        return man.loadExperimentsBundle(f);
    }
}
