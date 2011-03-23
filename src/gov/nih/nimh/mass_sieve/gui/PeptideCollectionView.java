package gov.nih.nimh.mass_sieve.gui;

import gov.nih.nimh.mass_sieve.PeptideHit;
import gov.nih.nimh.mass_sieve.Protein;
import gov.nih.nimh.mass_sieve.Peptide;
import gov.nih.nimh.mass_sieve.PeptideCollection;
import gov.nih.nimh.mass_sieve.PeptideIndeterminacyType;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import javax.swing.JPopupMenu;
import prefuse.Constants;
import prefuse.Display;
import prefuse.Visualization;
import prefuse.action.ActionList;
import prefuse.action.RepaintAction;
import prefuse.action.assignment.ColorAction;
import prefuse.action.assignment.DataColorAction;
import prefuse.action.layout.graph.BalloonTreeLayout;
import prefuse.action.layout.graph.ForceDirectedLayout;
import prefuse.action.layout.graph.NodeLinkTreeLayout;
import prefuse.action.layout.graph.RadialTreeLayout;
import prefuse.activity.Activity;
import prefuse.controls.Control;
import prefuse.controls.DragControl;
import prefuse.controls.FocusControl;
import prefuse.controls.HoverActionControl;
import prefuse.controls.NeighborHighlightControl;
import prefuse.controls.PanControl;
import prefuse.controls.WheelZoomControl;
import prefuse.controls.ZoomToFitControl;
import prefuse.data.Graph;
import prefuse.data.Table;
import prefuse.data.expression.parser.ExpressionParser;
import prefuse.render.DefaultRendererFactory;
import prefuse.render.EdgeRenderer;
import prefuse.render.LabelRenderer;
import prefuse.util.ColorLib;
import prefuse.visual.EdgeItem;
import prefuse.visual.NodeItem;
import prefuse.visual.VisualItem;

/**
 *
 * @author Alex Turbin (alex.academATgmail.com)
 */
public class PeptideCollectionView {

    transient private PeptideHitListPanel peptideHitListPanel;
    transient private PeptideListPanel peptideListPanel;
    transient private ProteinListPanel proteinListPanel;
    transient private ProteinGroupListPanel clusterListPanel, parsimonyListPanel;
    transient private Graph clusterGraph;
    transient private NodeItem highlightedItem;
    private PeptideCollection pepCollection;

    public PeptideCollectionView(PeptideCollection aPepCollection) {
        pepCollection = aPepCollection;
    }

    public Display getGraphDisplay(GraphLayoutType glType, final ExperimentPanel expPanel,
            String highlightName) {
        Display display = new Display();
        int X = expPanel.getDetailWidth();
        int Y = expPanel.getDetailHeight();
        display.setSize(X, Y); // set display size
        //display.setHighQuality(true);
        //display.setPreferredSize(new Dimension(600,600));
        display.addControlListener(new DragControl()); // drag items around
        display.addControlListener(new PanControl());  // pan with background left-drag
        display.addControlListener(new WheelZoomControl()); // zoom with vertical right-drag
        display.addControlListener(new ZoomToFitControl(Visualization.ALL_ITEMS, 50, 500,
                Control.MIDDLE_MOUSE_BUTTON));
        display.addControlListener(new NeighborHighlightControl());

        Visualization vis = new Visualization();
        if (clusterGraph == null) {
            clusterGraph = this.toGraph();
        }
        vis.add("graph", clusterGraph);
        LabelRenderer r = new LabelRenderer("name");
        r.setHorizontalAlignment(Constants.CENTER);
        //r.setRoundedCorner(8, 8); // round the corners

        DefaultRendererFactory rf = new DefaultRendererFactory(r);
        rf.setDefaultEdgeRenderer(new EdgeRenderer(Constants.EDGE_TYPE_CURVE));
        vis.setRendererFactory(rf);
        int[] palette = new int[]{ColorLib.rgb(255, 180, 180), ColorLib.rgb(190, 190, 255)};
        DataColorAction fill = new DataColorAction("graph.nodes", "type", Constants.NOMINAL,
                VisualItem.FILLCOLOR, palette);
        fill.add(VisualItem.FIXED, ColorLib.rgb(255, 100, 100));
        fill.add(VisualItem.HIGHLIGHT, ColorLib.rgb(255, 200, 125));
        int[] text_palette = new int[]{ColorLib.gray(0), ColorLib.gray(255)};
        DataColorAction text = new DataColorAction("graph.nodes", "indeterminate",
                Constants.NUMERICAL, VisualItem.TEXTCOLOR, text_palette);
        //ColorAction text = new ColorAction("graph.nodes", VisualItem.TEXTCOLOR, ColorLib.gray(0));
        ColorAction edges = new ColorAction("graph.edges", VisualItem.STROKECOLOR,
                ColorLib.gray(200));

        ActionList color = new ActionList(Activity.INFINITY);
        color.add(text);
        color.add(fill);
        color.add(edges);
        color.add(new RepaintAction());

        if (highlightName != null) {
            String pred = "name='" + highlightName + "'";
            Iterator iter = vis.items("graph.nodes", ExpressionParser.predicate(pred));
            while (iter.hasNext()) {
                NodeItem ni = (NodeItem) iter.next();
                highlightedItem = ni;
                ni.setFixed(true);
                Iterator iterEdge = ni.edges();
                while (iterEdge.hasNext()) {
                    EdgeItem eItem = (EdgeItem) iterEdge.next();
                    NodeItem nItem = eItem.getAdjacentItem(ni);
                    if (eItem.isVisible()) {
                        eItem.setHighlighted(true);
                        nItem.setHighlighted(true);
                    }
                }
            }
        }

        ActionList layout = new ActionList();
        switch (glType) {
            case BALLOON_TREE:
                layout.add(new BalloonTreeLayout("graph"));
                break;
            case FORCE_DIRECTED:
                layout = new ActionList(Activity.INFINITY);
                layout.add(new ForceDirectedLayout("graph"));
                break;
            case NODE_LINK_TREE:
                layout.add(new NodeLinkTreeLayout("graph"));
                break;
            case RADIAL_TREE:
                layout.add(new RadialTreeLayout("graph"));
                break;
        }
        //layout.add(fill);
        layout.add(new RepaintAction());

        vis.putAction("color", color);
        vis.putAction("layout", layout);
        display.setVisualization(vis);
        vis.run("color");
        vis.run("layout");
        //Rectangle2D bounds = vis.getBounds(Visualization.ALL_ITEMS);
        //GraphicsLib.expand(bounds, 50 + (int)(1/display.getScale()));
        //DisplayLib.fitViewToBounds(display,bounds,1);
        //vis.runAfter("layout",1000);

        Control shutoffHighlight = new HoverActionControl("color") {
            //public void itemEntered(VisualItem item, MouseEvent evt) {
            //}

            @Override
            public void itemExited(VisualItem item, MouseEvent evt) {
                if (highlightedItem != null) {
                    highlightedItem.setFixed(false);
                    Iterator iterEdge = highlightedItem.edges();
                    while (iterEdge.hasNext()) {
                        EdgeItem eItem = (EdgeItem) iterEdge.next();
                        NodeItem nItem = eItem.getAdjacentItem(highlightedItem);
                        if (eItem.isVisible()) {
                            eItem.setHighlighted(false);
                            nItem.setHighlighted(false);
                        }
                    }
                    highlightedItem = null;
                }
            }
        };
        display.addControlListener(shutoffHighlight);

        Control selectItem = new FocusControl() {

            @Override
            public void itemClicked(VisualItem item, MouseEvent evt) {
                if (item.isInGroup("graph.nodes")) {
                    String itemName = item.getString("name");
                    if (item.getString("type").equals("peptide")) {
                        Peptide pep = pepCollection.getMinPeptides().get(itemName);
                        expPanel.showPeptide(pep, true);
                    }
                    if (item.getString("type").equals("protein")) {
                        Protein pro = pepCollection.getMinProteins().get(itemName);
                        expPanel.showProtein(pro, true);
                    }
                    String pred = "name='" + itemName + "'";
                    Iterator iter = item.getVisualization().items("graph.nodes",
                            ExpressionParser.predicate(pred));
                    while (iter.hasNext()) {
                        NodeItem ni = (NodeItem) iter.next();
                        highlightedItem = ni;
                        ni.setFixed(true);
                        Iterator iterEdge = ni.edges();
                        while (iterEdge.hasNext()) {
                            EdgeItem eItem = (EdgeItem) iterEdge.next();
                            NodeItem nItem = eItem.getAdjacentItem(ni);
                            if (eItem.isVisible()) {
                                eItem.setHighlighted(true);
                                nItem.setHighlighted(true);
                            }
                        }
                    }

                }
            }
        };
        display.addControlListener(selectItem);

        final JPopupMenu menu = new JPopupMenu();

        // Create and add a menu item
        //JMenuItem printItem = new JMenuItem("Print This");
        //printItem.addActionListener(new java.awt.event.ActionListener() {

        //    public void actionPerformed(java.awt.event.ActionEvent evt) {
        //        PrintUtilities.printComponent(display);
        //    }
        //});
        //menu.add(printItem);

        // Set the component to show the popup menu
        display.addMouseListener(new MouseAdapter() {

            @Override
            public void mousePressed(MouseEvent evt) {
                if (evt.isPopupTrigger()) {
                    menu.show(evt.getComponent(), evt.getX(), evt.getY());
                }
            }

            @Override
            public void mouseReleased(MouseEvent evt) {
                if (evt.isPopupTrigger()) {
                    menu.show(evt.getComponent(), evt.getX(), evt.getY());
                }
            }
        });
        display.pan(X / 2.0, Y / 2.0);
        return display;
    }

    public Graph toGraph() {
        Table edgeTable = new Table();
        Table nodeTable = new Table();
        HashMap<String, Integer> unique = new HashMap<String, Integer>();

        edgeTable.addColumn("Node1", int.class);
        edgeTable.addColumn("Node2", int.class);
        nodeTable.addColumn("key", int.class);
        nodeTable.addColumn("name", String.class);
        nodeTable.addColumn("type", String.class);
        nodeTable.addColumn("indeterminate", int.class);

        int idx = 0;
        for (Protein prot : pepCollection.getMinProteins().values()) {
            int row = nodeTable.addRow();
            unique.put(prot.getName(), idx);
            nodeTable.setInt(row, "key", idx++);
            nodeTable.setString(row, "name", prot.getName());
            nodeTable.setString(row, "type", "protein");
            nodeTable.setInt(row, "indeterminate", 0);
        }

        for (Peptide pep : pepCollection.getMinPeptides().values()) {
            int row = nodeTable.addRow();
            unique.put(pep.getSequence(), idx);
            nodeTable.setInt(row, "key", idx++);
            nodeTable.setString(row, "name", pep.getSequence());
            nodeTable.setString(row, "type", "peptide");
            if (pep.getIndeterminateType() == PeptideIndeterminacyType.NONE) {
                nodeTable.setInt(row, "indeterminate", 0);
            } else {
                nodeTable.setInt(row, "indeterminate", 1);
            }

        }

        for (Protein prot : pepCollection.getMinProteins().values()) {
            int id1 = unique.get(prot.getName());
            for (String pep : prot.getPeptides()) {
                int id2 = unique.get(pep);
                int row = edgeTable.addRow();
                edgeTable.setInt(row, "Node1", id1);
                edgeTable.setInt(row, "Node2", id2);
            }
        }
        Graph g = new Graph(nodeTable, edgeTable, false, "key", "Node1", "Node2");
        //System.err.println(g.getEdgeCount());
        return g;
    }

    public PeptideHitListPanel getPeptideHitListPanel(ExperimentPanel expPanel) {
        if (peptideHitListPanel == null) {
            peptideHitListPanel = new PeptideHitListPanel(expPanel);
            ArrayList<PeptideHit> peptideHits = pepCollection.getPeptideHits();
            peptideHitListPanel.addProteinPeptideHitList(peptideHits);
            peptideHitListPanel.setName("Peptide Hits (" + peptideHits.size() + ")");
        }
        return peptideHitListPanel;
    }

    public PeptideHitListPanel getPeptideHitListPanel(ExperimentPanel expPanel, Set<String> pepNameList) {
        PeptideHitListPanel pepHitListPanel = new PeptideHitListPanel(expPanel);
        ArrayList<PeptideHit> pepHitList = new ArrayList<PeptideHit>();
        for (String pepName : pepNameList) {
            pepHitList.addAll(pepCollection.getMinPeptides().get(pepName).getPeptideHits());
        }
        pepHitListPanel.addProteinPeptideHitList(pepHitList);
        pepHitListPanel.setName("Peptide Hits (" + pepHitList.size() + ")");
        return pepHitListPanel;
    }

    public PeptideListPanel getPeptideListPanel(ExperimentPanel expPanel) {
        if (peptideListPanel == null) {
            peptideListPanel = new PeptideListPanel(expPanel);
            HashMap<String, Peptide> minPeptides = pepCollection.getMinPeptides();
            ArrayList<Peptide> list = new ArrayList<Peptide>(minPeptides.values());
            peptideListPanel.addPeptideList(list, pepCollection.getExperimentSet());
            peptideListPanel.setName("Peptides (" + minPeptides.size() + ")");
        }
        return peptideListPanel;
    }

    public PeptideListPanel getPeptideListPanel(ExperimentPanel expPanel, Set<String> pepNameList) {
        PeptideListPanel pepListPanel = new PeptideListPanel(expPanel);
        ArrayList<Peptide> pepList = new ArrayList<Peptide>();
        HashMap<String, Peptide> minPeptides = pepCollection.getMinPeptides();
        for (String pepName : pepNameList) {
            pepList.add(minPeptides.get(pepName));
        }
        pepListPanel.addPeptideList(pepList, pepCollection.getExperimentSet());
        pepListPanel.setName("Peptides (" + pepList.size() + ")");
        return pepListPanel;
    }

    public ProteinListPanel getProteinListPanel(ExperimentPanel expPanel) {
        if (proteinListPanel == null) {
            proteinListPanel = new ProteinListPanel(expPanel);
            HashMap<String, Protein> minProteins = pepCollection.getMinProteins();
            ArrayList<Protein> list = new ArrayList<Protein>(minProteins.values());
            proteinListPanel.addProteinList(list, pepCollection.getExperimentSet());
            proteinListPanel.setName("Proteins (" + minProteins.size() + ")");
        }
        return proteinListPanel;
    }

    public ProteinListPanel getProteinListPanel(ExperimentPanel expPanel, Set<String> proNameList) {
        ProteinListPanel proListPanel = new ProteinListPanel(expPanel);
        ArrayList<Protein> proList = new ArrayList<Protein>();
        HashMap<String, Protein> minProteins = pepCollection.getMinProteins();
        for (String proName : proNameList) {
            proList.add(minProteins.get(proName));
        }
        proListPanel.addProteinList(proList, pepCollection.getExperimentSet());
        proListPanel.setName("Proteins (" + proList.size() + ")");
        return proListPanel;
    }

    public ProteinGroupListPanel getClusterListPanel(ExperimentPanel expPanel) {
        if (clusterListPanel == null) {
            clusterListPanel = new ProteinGroupListPanel(expPanel);
            HashMap<String, Protein> minProteins = pepCollection.getMinProteins();
            ArrayList<Protein> list = new ArrayList<Protein>(minProteins.values());
            clusterListPanel.addProteinList(list, pepCollection.getExperimentSet(), true);
            Map<Integer, PeptideCollection> clusters = pepCollection.getClusters();
            clusterListPanel.setName("Clusters (" + clusters.size() + ")");
        }
        return clusterListPanel;
    }

    public ProteinGroupListPanel getParsimonyListPanel(ExperimentPanel expPanel) {
        if (parsimonyListPanel == null) {
            parsimonyListPanel = new ProteinGroupListPanel(expPanel);
            parsimonyListPanel.addProteinList(pepCollection.getCountables(),
                    pepCollection.getExperimentSet(), false);
            parsimonyListPanel.setName("Parsimony (" + pepCollection.getCountablesCount() + ")");
        }
        return parsimonyListPanel;
    }
}
