package gov.nih.nimh.mass_sieve;

import gov.nih.nimh.mass_sieve.tasks.MultiTaskListener;
import java.io.InterruptedIOException;

/**
 *
 * @author Alex Turbin (alex.academATgmail.com)
 */
public class DummyTaskListener implements MultiTaskListener {

    public void onMultiTaskFinished() {
    }

    public void onTaskStarted(String taskName, int taskSize) {
    }

    public void onProgress(int taskStep) throws InterruptedIOException {
    }

    public void onTaskFinished() {
    }

    public void onTaskCancelled() {
    }

    public void onTaskFailed() {
    }
}
