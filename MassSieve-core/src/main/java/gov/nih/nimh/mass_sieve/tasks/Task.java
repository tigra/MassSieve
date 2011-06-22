package gov.nih.nimh.mass_sieve.tasks;

/**
 *
 * @author Alex Turbin (alex.academATgmail.com)
 */
public class Task {
    private String curStep;
    private int curProgress;
    private final int totalSize;

    private DeterminedTaskListener listener;

    public Task(int totalSize, DeterminedTaskListener listener) {
        this.totalSize = totalSize;
        this.listener = listener;
    }

    public void setStep(String stepName) {
        curStep = stepName;
        listener.onChangeStepName(curStep);
    }

    public void setProgress(int value) {
        curProgress = value;
        listener.onProgress(curProgress, totalSize);
        checkIfDone();
    }

    private boolean checkIfDone() {
        if (totalSize <= curProgress) {
            listener.onFinish();
            return true;
        }
        return false;
    }

}
