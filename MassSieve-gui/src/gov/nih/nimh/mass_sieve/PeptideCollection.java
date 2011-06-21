/*
 * PeptideCollection.java
 *
 * Created on March 31, 2006, 2:02 PM
 *
 * @author Douglas Slotta
 */
package gov.nih.nimh.mass_sieve;

import gov.nih.nimh.mass_sieve.gui.PeptideCollectionView;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class PeptideCollection implements Serializable, Comparable<PeptideCollection> {

    private HashMap<String, Peptide> minPeptides;
    private HashMap<String, Protein> minProteins;
    private HashMap<Integer, PeptideCollection> clusters;
    private HashSet<String> experimentSet;
    private ArrayList<PeptideHit> peptideHits;
    private ArrayList<Protein> equivalents,  subsets,  supersets,  subsumables,  differentiables,  discretes,  countables;
    private int cluster_num;
    private Integer countablesCount;

    private transient PeptideCollectionView view; // TODO: probably model shouldn't depent on view.

    /** Creates a new instance of PeptideCollection */
    public PeptideCollection() {
        peptideHits = new ArrayList<PeptideHit>();
        minPeptides = new HashMap<String, Peptide>();
        experimentSet = new HashSet<String>();
        cluster_num = -1;
        countablesCount = null;
    }

    /**
     * @return object resporsible for different views of this peptide collection.
     */
    public PeptideCollectionView getView() {
        if (null == view) {
            view = new PeptideCollectionView(this);
        }
        return view;
    }

    public void addPeptideHit(PeptideHit p) {
        if (p == null) {
            return;
        }
        peptideHits.add(p);
        experimentSet.add(p.getExperiment());
        String key = p.getSequence();
        if (minPeptides.containsKey(key)) {
            Peptide pg = minPeptides.get(key);
            pg.addPeptideHit(p);
        } else {
            Peptide pg = new Peptide(p);
            minPeptides.put(key, pg);
        }
    }

    public void updatePeptideHits() {
        peptideHits = new ArrayList<PeptideHit>();
        for (Peptide p : minPeptides.values()) {
            peptideHits.addAll(p.getPeptideHits());
            experimentSet.addAll(p.getExperimentSet());
        }
    }

    public void addPeptideGroup(Peptide pg) {
        String key = pg.getSequence();
        if (minPeptides.containsKey(key)) {
            System.out.print("This PeptideCollection already contains " + key);
            System.out.println(" are you sure this is what you want?");
        } else {
            minPeptides.put(key, pg);
        }
    }

//    public PeptideCollection getCutoffCollection(double omssa, double mascot, double xtandem) {
//        return getCutoffCollection(omssa, mascot, xtandem, false);
//    }
    public PeptideCollection getCutoffCollection(FilterSettings fs) { // TODO filter by expect cutoff
        PeptideCollection pc = new PeptideCollection();

        for (PeptideHit p : peptideHits) {
            if (fs.conformsToFilter(p)) {
                pc.addPeptideHit(p);
            }
        }

        return pc;
    }

    public void computeIndeterminates() {
        HashMap<String, HashSet<PeptideHit>> scan2pep = new HashMap<String, HashSet<PeptideHit>>();
        HashSet<PeptideHit> pepList;

        for (PeptideHit p : peptideHits) {
            String scan = p.getScanTuple();
            if (scan2pep.containsKey(scan)) {
                pepList = scan2pep.get(scan);
            } else {
                pepList = new HashSet<PeptideHit>();
                scan2pep.put(scan, pepList);
            }
            pepList.add(p);
        }
        for (HashSet<PeptideHit> pepSet : scan2pep.values()) {
            boolean indeterm = false;
            if (pepSet.size() > 1) {
                HashSet<String> pepSeqs = new HashSet<String>();
                for (PeptideHit p : pepSet) {
                    pepSeqs.add(p.getSequence());
                }
                if (pepSeqs.size() > 1) indeterm = true;
            }
            for (PeptideHit p : pepSet) {
                p.setIndeterminate(indeterm);
            }
        }
    }

    public PeptideCollection getNonIndeterminents() {
        PeptideCollection pc = new PeptideCollection();

        for (PeptideHit p : peptideHits) {
            if (!p.isIndeterminate()) {
                pc.addPeptideHit(p);
            }
        }
        return pc;
    }

    public void createProteinList(ProteinDB proteinDB) {
        minProteins = new HashMap<String, Protein>();
        for (Peptide pg : minPeptides.values()) {
            String pepName = pg.getSequence();
            for (String protName : pg.getProteins()) {
                if (minProteins.containsKey(protName)) {
                    Protein prot = minProteins.get(protName);
                    prot.addPeptide(pepName);
                } else {
                    Protein prot = new Protein();
                    prot.setProteinDB(proteinDB);
                    prot.setName(protName);
                    prot.addPeptide(pepName);
                    prot.setCluster(cluster_num);
                    minProteins.put(protName, prot);
                }
            }
        }
        for (Protein p : minProteins.values()) {
            ProteinInfo pInfo = new ProteinInfo();
            pInfo.setName(p.getName());
            pInfo.setDescription(p.getDescription());
            pInfo.setLength(p.getLength());
            proteinDB.add(pInfo);
        }
    }

    public void updateProteins(HashMap<String, Protein> mainProteins) {
        minProteins = new HashMap<String, Protein>();
        for (Peptide pg : minPeptides.values()) {
            String pepName = pg.getSequence();
            for (String protName : pg.getProteins()) {
                if (minProteins.containsKey(protName)) {
                    Protein prot = minProteins.get(protName);
                    prot.addPeptide(pepName);
                } else {
                    Protein prot = mainProteins.get(protName);
                    minProteins.put(protName, prot);
                }
            }
        }
        for (Peptide pg : minPeptides.values()) {
            pg.updateProteins(minProteins);
        }
        for (Protein p : minProteins.values()) {
            p.updatePeptides(minPeptides);
        }
    }

    public void updateClusters() {
        clusters = new HashMap<Integer, PeptideCollection>();
        // relate all peptides to -1 cluster
        for (Peptide pg : minPeptides.values()) {
            pg.setCluster(-1);
        }
        // assign each peptide to new cluster
        int clusterIndex = 1;
        for (Peptide pg : minPeptides.values()) {
            if (pg.getCluster() == -1) {
                PeptideCollection newCluster = new PeptideCollection();
                newCluster.setClusterNum(clusterIndex);
                pg.setCluster(clusterIndex);
                newCluster.addPeptideGroup(pg);
                addLinkedMembers(newCluster, clusterIndex, pg);
                newCluster.updateProteins(minProteins);
                newCluster.updateParsimony();
                clusters.put(clusterIndex, newCluster);
                clusterIndex++;
            }
        }

        // update groups of equivalence
        int equivGroup = 0;
        HashSet<String> usedProteins = new HashSet<String>();
        ArrayList<Protein> equivOrder = new ArrayList<Protein>();
        equivOrder.addAll(getCountables());
        equivOrder.addAll(getSubsets());
        equivOrder.addAll(getSubsumables());
        for (Protein p : equivOrder) {
            if (!usedProteins.contains(p.getName())) {
                p.setEquivalentGroup(equivGroup);
                usedProteins.add(p.getName());
                for (Protein ps : p.getEquivalent()) {
                    ps.setEquivalentGroup(equivGroup);
                    usedProteins.add(ps.getName());
                }
                equivGroup++;
            }
        }
    }

    private void addLinkedMembers(PeptideCollection cluster, Integer clustNum, Peptide pg) {
        for (String proName : pg.getProteins()) {
            Protein pro = minProteins.get(proName);
            pro.setCluster(clustNum);
            for (String pepName : pro.getPeptides()) {
                Peptide subPg = minPeptides.get(pepName);
                if (subPg.getCluster() == -1) {
                    subPg.setCluster(clustNum);
                    cluster.addPeptideGroup(subPg);
                    addLinkedMembers(cluster, clustNum, subPg);
                }
            }
        }
    }

    public void updateParsimony() {
        for (Protein prot : minProteins.values()) {
            for (String pepName : prot.getPeptides()) {
                Peptide pep = minPeptides.get(pepName);
                prot.addAssociatedProteins(pep.getProteins());
            }
        }
        for (Protein prot : minProteins.values()) {
            prot.updateParsimony(minProteins);
        }
        for (Protein prot : minProteins.values()) {
            prot.computeParsimonyType();
        }
    }

    public PeptideProteinNameSet getPeptideProteinNameSet() {
        PeptideProteinNameSet pps = new PeptideProteinNameSet();
        pps.setPeptides(minPeptides.keySet());
        pps.setProteins(minProteins.keySet());
        return pps;
    }

    public PeptideCollection filterByPeptidePerProtein(ProteinDB proteinDB, int numPeps) {
        HashSet<String> proDiscard = new HashSet<String>();
        for (String pName : minProteins.keySet()) {
            Protein p = minProteins.get(pName);
            if (p.getNumUniquePeptides() < numPeps) {
                proDiscard.add(pName);
            }
        }
        PeptideCollection new_pc = new PeptideCollection();
        for (PeptideHit ph : peptideHits) {
            new_pc.addPeptideHit(ph.maskProtein(proDiscard));
        }
        new_pc.createProteinList(proteinDB);
        return new_pc;
    }

    public PeptideCollection filterByProteinCoverage(ProteinDB proteinDB, int minCoverage) {
        HashSet<String> proDiscard = new HashSet<String>();
        for (String pName : minProteins.keySet()) {
            Protein p = minProteins.get(pName);
            if (p.getCoveragePercent() < minCoverage) {
                proDiscard.add(pName);
            }
        }
        PeptideCollection new_pc = new PeptideCollection();
        for (PeptideHit ph : peptideHits) {
            new_pc.addPeptideHit(ph.maskProtein(proDiscard));
        }
        new_pc.createProteinList(proteinDB);
        return new_pc;
    }

    public PeptideCollection getPeptidesByHits(int numHits) {
        PeptideCollection new_pc = new PeptideCollection();
        for (Peptide pg : minPeptides.values()) {
            if (pg.getNumPeptideHits() >= numHits) {
                new_pc.minPeptides.put(pg.getSequence(), pg);
            }
        }
        new_pc.updatePeptideHits();
        return new_pc;
    }

    public PeptideCollection getOmssa() {
        PeptideCollection new_pc = new PeptideCollection();
        for (Peptide pg : minPeptides.values()) {
            if (pg.containsOmssa()) {
                new_pc.minPeptides.put(pg.getSequence(), pg);
            }
        }
        return new_pc;
    }

    public PeptideCollection getMascot() {
        PeptideCollection new_pc = new PeptideCollection();
        for (Peptide pg : minPeptides.values()) {
            if (pg.containsMascot()) {
                new_pc.minPeptides.put(pg.getSequence(), pg);
            }
        }
        return new_pc;
    }

    public PeptideCollection getXTandem() {
        PeptideCollection new_pc = new PeptideCollection();
        for (Peptide pg : minPeptides.values()) {
            if (pg.containsXTandem()) {
                new_pc.minPeptides.put(pg.getSequence(), pg);
            }
        }
        return new_pc;
    }

    public PeptideCollection getSequest() {
        PeptideCollection new_pc = new PeptideCollection();
        for (Peptide pg : minPeptides.values()) {
            if (pg.containsSequest()) {
                new_pc.minPeptides.put(pg.getSequence(), pg);
            }
        }
        return new_pc;
    }

    public PeptideCollection getPepXML() {
        PeptideCollection new_pc = new PeptideCollection();
        for (Peptide pg : minPeptides.values()) {
            if (pg.containsPepXML()) {
                new_pc.minPeptides.put(pg.getSequence(), pg);
            }
        }
        return new_pc;
    }

    public PeptideCollection union(PeptideCollection pc) {
        PeptideCollection new_pc = new PeptideCollection();
        new_pc.minPeptides.putAll(pc.minPeptides);
        for (String key : minPeptides.keySet()) {
            if (!new_pc.minPeptides.containsKey(key)) {
                new_pc.minPeptides.put(key, minPeptides.get(key));
            }
        }
        return new_pc;
    }

    public PeptideCollection intersection(PeptideCollection pc) {
        PeptideCollection new_pc = new PeptideCollection();
        for (String key : minPeptides.keySet()) {
            if (pc.minPeptides.containsKey(key)) {
                new_pc.minPeptides.put(key, minPeptides.get(key));
            }
        }
        return new_pc;
    }

    public PeptideCollection difference(PeptideCollection pc) {
        PeptideCollection new_pc = new PeptideCollection();
        for (String key : minPeptides.keySet()) {
            if (!pc.minPeptides.containsKey(key)) {
                new_pc.minPeptides.put(key, minPeptides.get(key));
            }
        }
        return new_pc;
    }

    public void setClusterNum(int i) {
        cluster_num = i;
    }

    public int getClusterNum() {
        return cluster_num;
    }

    public PeptideCollection getCluster(int i) {
        return clusters.get(i);
    }

    /**
     * @return unmodifiable view of clusters.
     */
    public Map<Integer, PeptideCollection> getClusters() {
        return Collections.unmodifiableMap(clusters);
    }

    public int getNumElements() {
        return minPeptides.size() + minProteins.size();
    }

    public int compareTo(PeptideCollection pc) {
        int x = getNumElements();
        int y = pc.getNumElements();
        if (x > y) {
            return -1;
        }
        if (x < y) {
            return 1;
        }
        return 0;
    }

    @Override
    public String toString() {
        return "Cluster " + cluster_num + " (" + getNumElements() + ')';
    //return "Cluster " + cluster_num;
    }

    public HashMap<String, Peptide> getMinPeptides() {
        return minPeptides;
    }

    public void setMinPeptides(HashMap<String, Peptide> minPeptides) {
        this.minPeptides = minPeptides;
    }

    public HashMap<String, Protein> getMinProteins() {
        return minProteins;
    }

    public void setMinProteins(HashMap<String, Protein> minProteins) {
        this.minProteins = minProteins;
    }

    public ArrayList<PeptideHit> getPeptideHits() {
        return peptideHits;
    }

    public HashSet<String> getExperimentSet() {
        return experimentSet;
    }

    private void categorizeProteins() {
        equivalents = new ArrayList<Protein>();
        subsets = new ArrayList<Protein>();
        supersets = new ArrayList<Protein>();
        subsumables = new ArrayList<Protein>();
        differentiables = new ArrayList<Protein>();
        discretes = new ArrayList<Protein>();
        countables = new ArrayList<Protein>();

        ArrayList<Protein> sortProteins = new ArrayList<Protein>();
        sortProteins.addAll(minProteins.values());
        Collections.sort(sortProteins);
        for (Protein p : sortProteins) {
            switch (p.getParsimonyType()) {
                case EQUIVALENT:
                    equivalents.add(p);
                    break;
                case SUBSET:
                    subsets.add(p);
                    break;
                case SUPERSET:
                    supersets.add(p);
                    break;
                case SUBSUMABLE:
                    subsumables.add(p);
                    break;
                case DIFFERENTIABLE:
                    differentiables.add(p);
                    break;
                case DISCRETE:
                    discretes.add(p);
                    break;
            }
        }

        countables.addAll(discretes);
        countables.addAll(differentiables);
        countables.addAll(supersets);
        countables.addAll(equivalents);
    }

    public ArrayList<Protein> getEquivalents() {
        if (equivalents == null) {
            categorizeProteins();
        }
        return equivalents;
    }

    public ArrayList<Protein> getSubsets() {
        if (subsets == null) {
            categorizeProteins();
        }
        return subsets;
    }

    public ArrayList<Protein> getSupersets() {
        if (supersets == null) {
            categorizeProteins();
        }
        return supersets;
    }

    public ArrayList<Protein> getSubsumables() {
        if (subsumables == null) {
            categorizeProteins();
        }
        return subsumables;
    }

    public ArrayList<Protein> getDifferentiables() {
        if (differentiables == null) {
            categorizeProteins();
        }
        return differentiables;
    }

    public ArrayList<Protein> getDiscretes() {
        if (discretes == null) {
            categorizeProteins();
        }
        return discretes;
    }

    public ArrayList<Protein> getCountables() {
        if (countables == null) {
            categorizeProteins();
        }
        return countables;
    }

    public Integer getCountablesCount() {
        if (countablesCount == null) {
            HashSet<Integer> uniqueEquivs = new HashSet<Integer>();
            for (Protein p : getCountables()) {
                uniqueEquivs.add(p.getEquivalentGroup());
            }
            countablesCount = uniqueEquivs.size();
        }
        return countablesCount;
    }

    public Set<String> getPeptideNames() {
        return minPeptides.keySet();
    }

    public Set<String> getProteinNames() {
        return minProteins.keySet();
    }
}
