package gov.nih.nimh.mass_sieve.actions;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 *
 * @author Alex Turbin (alex.academATgmail.com)
 */
public class CompareExpDiffAction extends ExternalAction {

    private static final String name = "CompareExpDiff";
    private List<File> files = new ArrayList<File>();
    private String exportFilename;
    ExportParams exportParams;

    public CompareExpDiffAction()
    {
    }

    public void addExperimentFile(String filename) {
        File f = new File(filename);
        files.add(f);
    }

    public String getExportExpDBFilename() {
        return exportParams.exportExpDBFilename;
    }

    public void setExportTableFilename(String exportFilename) {
        this.exportFilename = exportFilename;
    }

    public List<File> getExperimentFiles() {
        return Collections.unmodifiableList(files);
    }

    public String getFilename() {
        return exportFilename;
    }
}
