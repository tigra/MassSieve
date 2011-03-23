package gov.nih.nimh.mass_sieve.actions;

import gov.nih.nimh.mass_sieve.FilterSettings;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 *
 * @author Alex Turbin (alex.academATgmail.com)
 */
public class MergeAction {
    private String name = "Merge";

    String experimentName;
    private List<String> inputFileNames = new ArrayList<String>();
    String exportExpDBFilename;
    String exportExpResFilename;
    String exportPrefProtFilename;
    String saveExpFilename;
    FilterSettings filter;

    public String getActionName()
    {
        return name;
    }

    public String getExperimentName()
    {
        return experimentName;
    }

    
    public void addInputFileName(String fileName)
    {
        inputFileNames.add(fileName);
    }

    public List<String> getFileNames()
    {
        return Collections.unmodifiableList(inputFileNames);
    }

    public String getExportExpDBFilename() {
        return exportExpDBFilename;
    }

    public String getExportExpResFilename() {
        return exportExpResFilename;
    }

    public String getExportPrefProtFilename()
    {
        return exportPrefProtFilename;
    }

    public String getSaveExpFilename()
    {
        return saveExpFilename;
    }

    public FilterSettings getFilterSettings()
    {
        return filter;
    }

}
