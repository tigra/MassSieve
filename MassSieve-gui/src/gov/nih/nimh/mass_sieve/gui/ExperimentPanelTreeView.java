/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package gov.nih.nimh.mass_sieve.gui;

import gov.nih.nimh.mass_sieve.Peptide;
import gov.nih.nimh.mass_sieve.PeptideCollection;
import gov.nih.nimh.mass_sieve.PeptideProteinNameSet;
import gov.nih.nimh.mass_sieve.Protein;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;

/**
 *
 * @author Alex Turbin (alex.academATgmail.com)
 */
public class ExperimentPanelTreeView {

    public DefaultTreeModel getTree(PeptideCollection pepCollection, ExperimentPanel expPanel) {
        DefaultMutableTreeNode root = new DefaultMutableTreeNode(expPanel.getName() + " Overview");
        root.add(getPeptideHitsTree(pepCollection, expPanel));
        root.add(getPeptideTree(pepCollection, expPanel, true));
        root.add(getProteinTree(pepCollection, expPanel, true));
        root.add(getClusterTree(pepCollection, expPanel));
        root.add(getParsimonyTree(pepCollection, expPanel));
        return new DefaultTreeModel(root);
    }

    private DefaultMutableTreeNode getPeptideHitsTree(PeptideCollection pepCollection, ExperimentPanel expPanel) {
        PeptideHitListPanel panel = pepCollection.getView().getPeptideHitListPanel(expPanel);
        DefaultMutableTreeNode root = new DefaultMutableTreeNode(panel);
        return root;
    }

    private DefaultMutableTreeNode getPeptideTree(PeptideCollection pepCollection, ExperimentPanel expPanel, boolean useListPanel) {
        DefaultMutableTreeNode root;
        if (useListPanel) {
            root = new DefaultMutableTreeNode(pepCollection.getView().getPeptideListPanel(expPanel));
        } else {
            PeptideProteinNameSet pps = pepCollection.getPeptideProteinNameSet();
            pps.setName("Peptides (" + pps.getPeptides().size() + ")");
            root = new DefaultMutableTreeNode(pps);
        }
        DefaultMutableTreeNode child;
        ArrayList<Peptide> sortPeptides = new ArrayList<Peptide>();
        sortPeptides.addAll(pepCollection.getMinPeptides().values());
        Collections.sort(sortPeptides);
        for (Peptide pg : sortPeptides) {
            //child = pg.getTree();
            child = new DefaultMutableTreeNode(pg);
            root.add(child);
        }
        return root;
    }

    private DefaultMutableTreeNode getProteinTree(PeptideCollection pepCollection, ExperimentPanel expPanel, boolean useListPanel) {
        DefaultMutableTreeNode root;
        if (useListPanel) {
            ProteinListPanel plp = pepCollection.getView().getProteinListPanel(expPanel);
            root = new DefaultMutableTreeNode(plp);
        } else {
            PeptideProteinNameSet pps = pepCollection.getPeptideProteinNameSet();
            pps.setName("Proteins (" + pps.getProteins().size() + ")");
            root = new DefaultMutableTreeNode(pps);
        }
        DefaultMutableTreeNode child;
        ArrayList<Protein> sortProteins = new ArrayList<Protein>();
        sortProteins.addAll(pepCollection.getMinProteins().values());
        Collections.sort(sortProteins);
        for (Protein prot : sortProteins) {
            //child = prot.getTree(expPanel);
            child = new DefaultMutableTreeNode(prot);
            root.add(child);
        }
        return root;
    }

    private DefaultMutableTreeNode getClusterTree(PeptideCollection pepCollection, ExperimentPanel expPanel) {
        PeptideCollectionView view = pepCollection.getView();
        DefaultMutableTreeNode root = new DefaultMutableTreeNode(view.getClusterListPanel(expPanel));
        DefaultMutableTreeNode child, grandchild;

        // Sort by cluster num
        ArrayList<Integer> sortClusterNum = new ArrayList<Integer>();
        sortClusterNum.addAll(pepCollection.getClusters().keySet());
        Collections.sort(sortClusterNum);
        for (Integer i : sortClusterNum) {
            PeptideCollection pc = pepCollection.getCluster(i);
            child = new DefaultMutableTreeNode(pc);
            root.add(child);
            grandchild = getPeptideTree(pc, expPanel, false);
            child.add(grandchild);
            grandchild = getProteinTree(pc, expPanel, false);
            child.add(grandchild);
        }
        return root;
    }

    private DefaultMutableTreeNode getParsimonyTree(PeptideCollection pepCollection, ExperimentPanel expPanel) {
        PeptideCollectionView view = pepCollection.getView();
        DefaultMutableTreeNode root = new DefaultMutableTreeNode(view.getParsimonyListPanel(expPanel));
        DefaultMutableTreeNode child;

        // Discrete
        PeptideProteinNameSet pps = ProteinListToPPNSet(pepCollection.getDiscretes());
        pps.setName("Discrete (" + pepCollection.getDiscretes().size() + ")");
        child = new DefaultMutableTreeNode(pps);
        for (Protein p : pepCollection.getDiscretes()) {
            child.add(new DefaultMutableTreeNode(p));
        }
        root.add(child);

        // Differentiable
        pps = ProteinListToPPNSet(pepCollection.getDifferentiables());
        pps.setName("Differentiable (" + pepCollection.getDifferentiables().size() + ")");
        child = new DefaultMutableTreeNode(pps);
        for (Protein p : pepCollection.getDifferentiables()) {
            child.add(new DefaultMutableTreeNode(p));
        }
        root.add(child);

        // Superset
        child = ProListToEquivTree(pepCollection.getSupersets(), "Superset");
        root.add(child);

        // Subsumable
        child = ProListToEquivTree(pepCollection.getSubsumables(), "Subsumable");
        root.add(child);

        // Subset
        child = ProListToEquivTree(pepCollection.getSubsets(), "Subset");
        root.add(child);

        // Equivalent
        child = ProListToEquivTree(pepCollection.getEquivalents(), "Equivalent");
        root.add(child);
        return root;
    }

    private DefaultMutableTreeNode ProListToEquivTree(ArrayList<Protein> proList, String name) {
        DefaultMutableTreeNode root, child;
        int size = 0;
        PeptideProteinNameSet pps = ProteinListToPPNSet(proList);

        root = new DefaultMutableTreeNode(pps);
        HashSet<String> usedProteins = new HashSet<String>();
        for (Protein p : proList) {
            if (!usedProteins.contains(p.getName())) {
                if (p.getEquivalent().size() > 0) {
                    ArrayList<Protein> equivList = new ArrayList<Protein>();
                    equivList.add(p);
                    equivList.addAll(p.getEquivalent());
                    PeptideProteinNameSet ppsEquiv = ProteinListToPPNSet(equivList);
                    ppsEquiv.setName(p.getName() + " Group (" + equivList.size() + ")");
                    child = new DefaultMutableTreeNode(ppsEquiv);
                    size++;
                    for (Protein ps : equivList) {
                        child.add(new DefaultMutableTreeNode(ps));
                        usedProteins.add(ps.getName());
                    }
                    root.add(child);
                } else {
                    root.add(new DefaultMutableTreeNode(p));
                    usedProteins.add(p.getName());
                    size++;
                }
            }
        }
        pps.setName(name + " (" + size + ")");
        return root;
    }

    private PeptideProteinNameSet ProteinListToPPNSet(ArrayList<Protein> proteins) {
        Set<String> protNames = new HashSet<String>();
        Set<String> pepNames = new HashSet<String>();
        for (Protein p : proteins) {
            protNames.add(p.getName());
            pepNames.addAll(p.getPeptides());
        }
        PeptideProteinNameSet pps = new PeptideProteinNameSet();
        pps.setPeptides(pepNames);
        pps.setProteins(protNames);
        return pps;
    }
}
