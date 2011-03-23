package gov.nih.nimh.mass_sieve.logic;

import antlr.RecognitionException;
import antlr.TokenStreamException;
import gov.nih.nimh.mass_sieve.util.LogStub;
import gov.nih.nimh.mass_sieve.util.IOUtils;
import gov.nih.nimh.mass_sieve.Experiment;
import gov.nih.nimh.mass_sieve.ExperimentData;
import gov.nih.nimh.mass_sieve.ExportProteinType;
import gov.nih.nimh.mass_sieve.FilterSettings;
import gov.nih.nimh.mass_sieve.Peptide;
import gov.nih.nimh.mass_sieve.PeptideCollection;
import gov.nih.nimh.mass_sieve.PeptideHit;
import gov.nih.nimh.mass_sieve.Protein;
import gov.nih.nimh.mass_sieve.ProteinDB;
import gov.nih.nimh.mass_sieve.ProteinInfo;
import gov.nih.nimh.mass_sieve.io.FileInformation;
import gov.nih.nimh.mass_sieve.io.ParseFile;
import gov.nih.nimh.mass_sieve.io.SetLexer;
import gov.nih.nimh.mass_sieve.io.SetParser;
import gov.nih.nimh.mass_sieve.tasks.DeterminedTaskListener;
import gov.nih.nimh.mass_sieve.tasks.InputStreamObserver;
import gov.nih.nimh.mass_sieve.tasks.InputStreamProgressObserver;
import gov.nih.nimh.mass_sieve.tasks.MultiTaskListener;
import gov.nih.nimh.mass_sieve.tasks.ObserverableInputStream;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InterruptedIOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.biojava.bio.BioException;
import org.biojavax.Namespace;
import org.biojavax.RichObjectFactory;
import org.biojavax.bio.seq.RichSequence;
import org.biojavax.bio.seq.RichSequenceIterator;

/**
 * Web controller in future.
 *
 * @author Alex Turbin (alex.academATgmail.com)
 */
public class ExperimentManager {

    private ProteinDB proteinDB = new ProteinDB();

    /**
     * Saves a set of experiments to a given file.
     * @throws DataStoreException if error occurs while saving bundle to storage.
     */
    public void saveExperimentsBundle(ExperimentsBundle eb, File f) throws DataStoreException {
        ObjectOutputStream os = null;
        try {
            os = new ObjectOutputStream(new FileOutputStream(f));

            List<Experiment> experiments = eb.getExperiments();
            os.writeInt(experiments.size());

            for (Experiment exp : experiments) {
                os.writeObject(exp);
            }

            os.writeObject(eb.getProteinDB().getMap());

        } catch (FileNotFoundException ex) {
            LogStub.error(ex);
            String msg = "Unable to save file";
            throw new DataStoreException(msg, ex);
        } catch (IOException ex) {
            LogStub.error(ex);
            String msg = "Unable to save file";
            throw new DataStoreException(msg, ex);
        } finally {
            IOUtils.closeSafe(os);
        }
    }

    /**
     * Loads set of experiments from a given file.
     * @throws DataStoreException
     */
    public ExperimentsBundle loadExperimentsBundle(File f) throws DataStoreException {
        List<Experiment> exps = new ArrayList<Experiment>();
        Map<String, ProteinInfo> proteinDBMap;

        ObjectInputStream ois = null;
        try {
            Object obj;
            ois = new ObjectInputStream(new FileInputStream(f));

            int expCount = ois.readInt();
            if (expCount == 1) {
                System.out.println("File contains " + expCount + " experiment");
            } else {
                System.out.println("File contains " + expCount + " experiments");
            }

            for (int i = 0; i < expCount; i++) {
                obj = ois.readObject();
                Experiment exp = (Experiment) obj;
                exps.add(exp);
            }

            obj = ois.readObject();
            proteinDBMap = (Map<String, ProteinInfo>) obj;
        } catch (FileNotFoundException ex) {
            LogStub.error(ex);
            String msg = "Unable to open file";
            throw new DataStoreException(msg, ex);
        } catch (ClassNotFoundException ex) {
            LogStub.error(ex);
            String msg = "File format does not match current MassSieve version";
            throw new DataStoreException(msg, ex);
        } catch (IOException ex) {
            LogStub.error(ex);
            String msg = "Unable to open file";
            throw new DataStoreException(msg, ex);
        } finally {
            IOUtils.closeSafe(ois);
        }

        addToProteinDatabase(proteinDBMap.values());
        ExperimentsBundle eb = new ExperimentsBundle(exps, proteinDB);
        // assing proteinDB to every experiment
        for (Experiment e : exps)
        {
            PeptideCollection pepCol = e.getPepCollection();
            HashMap<String, Protein> minProteins = pepCol.getMinProteins();
            for (Protein pro : minProteins.values())
            {
                pro.setProteinDB(proteinDB);
            }
        }
        return eb;
    }

    /**
     * Exports proteins, peptides, peptide hits, protein-peptide relationships
     * and equivalent proteins.
     * @param file File to export data.
     * @param pepCollection Source for exported data.
     * @return result of the operation.
     */
    public ActionResponse exportDatabase(File file, PeptideCollection pepCollection) {
        FileWriter fw = null;
        try {
            fw = new FileWriter(file);
            // export proteins
            fw.write("# Proteins\n");
            fw.write(Protein.toTabStringHeader());
            for (Protein pro : pepCollection.getMinProteins().values()) {
                fw.write(pro.toTabString());
            }
            // export peptides
            fw.write("\n\n# Peptides\n");
            fw.write(Peptide.toTabStringHeader());
            for (Peptide pep : pepCollection.getMinPeptides().values()) {
                fw.write(pep.toTabString());
            }
            // export peptide hits
            fw.write("\n\n# Peptide Hits\n");
            fw.write(PeptideHit.toTabStringHeader());
            for (PeptideHit pepHit : pepCollection.getPeptideHits()) {
                fw.write(pepHit.toTabString());
            }
            // export protein-peptide relationships
            fw.write("\n\n# ProteinToPeptides\n");
            fw.write("Protein\tPeptide\n");
            for (Protein pro : pepCollection.getMinProteins().values()) {
                for (Peptide pep : pro.getAllPeptides()) {
                    fw.write(pro.getName() + "\t" + pep.getSequence() + "\n");
                }
            }
            // export equivalent proteins
            fw.write("\n\n# EquivalentProteins\n");
            fw.write("Protein\tProtein\n");
            for (Protein pro1 : pepCollection.getMinProteins().values()) {
                for (Protein pro2 : pro1.getEquivalent()) {
                    fw.write(pro1.getName() + "\t" + pro2.getName() + "\n");
                }
            }
        } catch (IOException ex) {
            LogStub.error(ex);
            return new ActionResponse(ActionResult.FAILED, "Failed to export database.");
        } finally {
            IOUtils.closeSafe(fw);
        }
        return ActionResponse.SUCCESS;
    }

    public ActionResponse exportResults(File file, PeptideCollection pepCollection, ExportProteinType type) {
        FileWriter fw = null;
        try {
            fw = new FileWriter(file);
            //	Output column headers if any.
            if (ExportProteinType.PREFERRED == type) {
                fw.write("Proteins\tPeptides\tScans\n");
            } else {
                fw.write("Proteins\tParsimony\tPeptides\tScans\n");
            }
            for (Protein pro : pepCollection.getMinProteins().values()) {
                if ((ExportProteinType.PREFERRED == type) && !pro.isMostEquivalent()) {
                    continue;  // preferred proteins only
                }
                if (ExportProteinType.PREFERRED == type) {
                    fw.write(pro.getName());
                }
                for (Peptide pep : pro.getAllPeptides()) {
                    if (ExportProteinType.PREFERRED == type) {
                        fw.write("\t" + pep.getSequence() + "\t" + pep.getScanList(false) + "\n");
                    } else {
                        fw.write(pro.getName() + "\t" + pro.getParsimonyType() + "\t" + pep.getSequence() + "\t" + pep.getScanList(false) + "\n");
                    }
                }
            }
        } catch (IOException ex) {
            LogStub.error(ex);
            return new ActionResponse(ActionResult.FAILED, "Failed to export results.");
        } finally {
            IOUtils.closeSafe(fw);
        }
        return ActionResponse.SUCCESS;
    }

    public ActionResponse exportSeqDB(File file, Set<String> minProteins) {
        int seqCount = 0;
        OutputStream os = null;
        try {
            os = new BufferedOutputStream(new FileOutputStream(file));

            for (String prot : minProteins) {
                ProteinInfo info = proteinDB.get(prot);
                RichSequence rs = info.getRichSequence();
                if (rs != null && (rs.length() > 0)) {
                    RichSequence.IOTools.writeFasta(os, rs, null);
                    seqCount++;
                }
            }
        } catch (IOException ex) {
            LogStub.error(ex);
            return new ActionResponse(ActionResult.FAILED, "Unable to export sequences");
        } finally {
            IOUtils.closeSafe(os);
        }

        return new ActionResponse(ActionResult.SUCCESS, "Exported " + seqCount + " sequences.");
    }

    /**
     * Asynchroniously adds sequence db files to current experiment.
     * Notifies about current progress via listener.
     */
    public void addSeqDBfiles(final File[] files, final MultiTaskListener listener) {
        //TODO: use thread pool
        new Thread(new Runnable() {
            /* TODO: use RichSequence.IOTools.readFile to guess format,
            support more formats, support genbank directly
             */

            public void run() {
                int seqCount = 0;
                //int dupCount = 0;
                for (File f : files) {
                    System.err.println("Parsing " + f.getName() + " as the sequence database");
                    //XXX: >2gb files?
                    listener.onTaskStarted(f.getName(), (int) f.length());
                    try {
                        // Not sure if your input is EMBL or Genbank? Load them both here.^M
                        //Class.forName("org.biojavax.bio.seq.io.EMBLFormat");
                        //Class.forName("org.biojavax.bio.seq.io.UniProtFormat");
                        //Class.forName("org.biojavax.bio.seq.io.UniProtXMLFormat");
                        //Class.forName("org.biojavax.bio.seq.io.GenbankFormat");
                        Class.forName("org.biojavax.bio.seq.io.FastaFormat");

                        // Now let BioJavaX guess which format you actually should use (using the default namespace)
                        Namespace ns = RichObjectFactory.getDefaultNamespace();

                        InputStreamObserver inputObserver = new InputStreamProgressObserver(listener);
                        ObserverableInputStream pin = new ObserverableInputStream(new FileInputStream(f), inputObserver);
                        BufferedInputStream bin = new BufferedInputStream(pin);
                        RichSequenceIterator seqItr = RichSequence.IOTools.readStream(bin, ns);
                        while (seqItr.hasNext()) {
                            try {
                                RichSequence seq = seqItr.nextRichSequence();
                                String seqName = seq.getName();
                                //System.out.println(seqName);
                                if (proteinDB.contains(seqName)) {
                                    ProteinInfo pInfo = proteinDB.get(seqName);
                                    pInfo.updateFromRichSequence(seq);
                                    //RichSequence daSeq = proteinDB.get(seqName);
                                    //if (daSeq != null) {
                                    //    if (daSeq.getInternalSymbolList() != SymbolList.EMPTY_LIST ) dupCount++;
                                    //}
                                    //proteinDB.put(seqName, seq);
                                    System.err.println(seqName);
                                    seqCount++;
                                }
                            } catch (BioException ex) {
                                if (ex.getCause() != null) {
                                    throw ex.getCause();
                                }
                                LogStub.error(ex);
                            }
                        }
                        System.err.println(f.getName() + " sequence database read complete!");
                    } catch (InterruptedIOException ex) {
                        listener.onTaskCancelled();
                        System.err.println("Fasta import canceled by user");
                        break;
                    } catch (IOException ex) {
                        listener.onTaskFailed();
                        LogStub.error(ex);
                    } catch (ClassNotFoundException ex) {
                        LogStub.error(ex);
                    } catch (Throwable ex) {
                        LogStub.error(ex);
                    }
                    listener.onTaskFinished();
                }
                listener.onMultiTaskFinished();
            }
        }).start();
    }

    public ExperimentData createNewExperiment(String expName) {
        return new ExperimentData(expName);
    }

    public List<ProteinInfo> addFileToExperiment(ExperimentData expData, File f,
            InputStreamObserver inputObserver, DeterminedTaskListener parseListener) {
        List<ProteinInfo> result = new ArrayList<ProteinInfo>();

        String expName = expData.getName();
        expData.addFile(f);
        String filename = f.getName();

        ParseFile pf = new ParseFile(f, inputObserver, parseListener);
        Set<String> acceptedProteins = processPeptideCollection(expName, filename, pf.getPeptideHits(), expData);
        Map<String, ProteinInfo> pDB = pf.getProteinDB();
        for (String pName : pDB.keySet()) {
            if (acceptedProteins.contains(pName)) {
                result.add(pDB.get(pName));
            }
        }
        FileInformation fInfo = pf.getFileInformation();
        fInfo.setExperiment(expName);
        expData.addFileInfo(fInfo);

        addToProteinDatabase(result);
        return result;
    }

    private Set<String> processPeptideCollection(String expName, String filename, final List<PeptideHit> peptideHits,
            ExperimentData expData) {
        Set<String> acceptedProteins = new HashSet<String>();
        for (PeptideHit p : peptideHits) {
            p.setExperiment(expName);
            p.setSourceFile(filename);
            if (expData.getFilterSettings().peptideHitConformsToFilter(p)) {
                expData.getPepCollectionOriginal().addPeptideHit(p);
                acceptedProteins.addAll(p.getProteinNames());
            }
        }
        return acceptedProteins;
    }

    public PeptideCollection filterBySearchProgram(PeptideCollection pc, String filterText) {
        PeptideCollection result;
        StringReader setDescription = new StringReader(filterText);
        SetLexer lexer = new SetLexer(setDescription);
        SetParser parser = new SetParser(lexer);
        parser.setPeptideCollection(pc);
        try {
            result = parser.expr();
            result.updatePeptideHits();
            return result;
        } catch (TokenStreamException ex) {
            LogStub.error(ex);
        } catch (RecognitionException ex) {
            LogStub.error(ex);
        }
        return new PeptideCollection();
    }

    public void recomputeCutoff(ExperimentData expData) {
        FilterSettings filterSettings = expData.getFilterSettings();
        PeptideCollection pepFiltered;
        pepFiltered = expData.getPepCollectionOriginal().getCutoffCollection(filterSettings);
        pepFiltered.computeIndeterminates();
        if (!filterSettings.getUseIndeterminates()) {
            pepFiltered = pepFiltered.getNonIndeterminents();
        }
        pepFiltered = filterBySearchProgram(pepFiltered, expData.getFilterSettings().getFilterText());
        if (filterSettings.getFilterPeptides()) {
            pepFiltered = pepFiltered.getPeptidesByHits(filterSettings.getPepHitCutoffCount());
        }
        // TODO handle additional cutoff filter
        pepFiltered.createProteinList(proteinDB);
        if (filterSettings.getFilterProteins()) {
            int numPeps = filterSettings.getPeptideCutoffCount();
            pepFiltered = pepFiltered.filterByPeptidePerProtein(proteinDB, numPeps);
        }
        if (filterSettings.getFilterCoverage()) {
            pepFiltered.updateClusters();
            int minCoverage = filterSettings.getCoverageCutoffAmount();
            pepFiltered = pepFiltered.filterByProteinCoverage(proteinDB, minCoverage);
        }
        pepFiltered.updateClusters();
        expData.setPepCollection(pepFiltered);
    }

    /**
     * Creates new experiment data object that could be saved as serialized object.
     */
    public Experiment getPersistentExperiment(ExperimentData expData) {
        Experiment exp = new Experiment();
        exp.setName(expData.getName());
        exp.setFileInfos(expData.getFileInfos());
        exp.setFilterSettings(expData.getFilterSettings());
        exp.setPepCollection(expData.getPepCollection());
        exp.setPepCollectionOriginal(expData.getPepCollectionOriginal());
        return exp;
    }

    public void addToProteinDatabase(ProteinInfo pInfo) {
        String pName = pInfo.getName();
        if (!proteinDB.contains(pName)) {
            proteinDB.add(pInfo);
        } else {
            ProteinInfo pInfoOld = proteinDB.get(pName);
            pInfoOld.update(pInfo);
        }
    }

    public void addToProteinDatabase(Collection<ProteinInfo> proteins) {
        for (ProteinInfo info : proteins) {
            addToProteinDatabase(info);
        }
    }

    public ProteinDB getProteinDatabase() {
        return proteinDB;
    }

    public void saveExperiment(ExperimentData exp, File file) throws DataStoreException {
        Experiment persist = getPersistentExperiment(exp);
        List<Experiment> persistList = new ArrayList<Experiment>();
        persistList.add(persist);

        ExperimentsBundle bundle = new ExperimentsBundle(persistList, proteinDB);
        saveExperimentsBundle(bundle, file);
    }
}
