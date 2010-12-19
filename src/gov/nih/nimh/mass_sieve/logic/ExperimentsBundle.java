package gov.nih.nimh.mass_sieve.logic;

import gov.nih.nimh.mass_sieve.Experiment;
import gov.nih.nimh.mass_sieve.ProteinInfo;
import java.util.List;
import java.util.Map;

/**
 *
 * @author Alex Turbin (alex.academATgmail.com)
 */
public class ExperimentsBundle {

    private final List<Experiment> exps;
    private final Map<String, ProteinInfo> proteinDB;

    public ExperimentsBundle(List<Experiment> exps, Map<String, ProteinInfo> proteinDB) {
        this.exps = exps;
        this.proteinDB = proteinDB;
    }

    public List<Experiment> getExperiments() {
        return exps;
    }

    public Map<String, ProteinInfo> getProteinInfos() {
        return proteinDB;
    }
}
