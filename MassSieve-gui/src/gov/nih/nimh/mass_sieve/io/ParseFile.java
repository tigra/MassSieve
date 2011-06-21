/*
 * ParseFile.java
 *
 * Created on February 24, 2006, 9:11 AM
 *
 * @author Douglas Slotta
 */
package gov.nih.nimh.mass_sieve.io;

import be.proteomics.mascotdatfile.util.mascot.MascotDatfile;
import gov.nih.nimh.mass_sieve.PeptideHit;
import gov.nih.nimh.mass_sieve.ProteinInfo;
import gov.nih.nimh.mass_sieve.tasks.DeterminedTaskListener;
import gov.nih.nimh.mass_sieve.tasks.InputStreamObserver;
import gov.nih.nimh.mass_sieve.tasks.ObserverableInputStream;
import gov.nih.nimh.mass_sieve.tasks.Task;
import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.util.ArrayList;
import java.util.HashMap;
import org.xml.sax.*;
import org.xml.sax.helpers.*;

public class ParseFile {

    private AnalysisProgramType filetype;
    private String filename;
    private File file;
    private XMLReader xmlReader;
    private AnalysisHandler handler;

    //public ParseFile(String f, Component p) {
    public ParseFile(File f, InputStreamObserver taskObserver, DeterminedTaskListener parseListener) {
        filename = f.getPath();
        file = f;

        if (filename.endsWith(".dat")) {
            System.err.println("Parsing " + filename + " as a Mascot dat file");
            mascotDatHandler mdh = new mascotDatHandler(filename);
            MascotDatfile mdf = mdh.mascotDatRead(taskObserver);

            Task task = new Task(3, parseListener);
            mdh.mascotDatParse(mdf, task);
            handler = mdh;
        } else if (filename.endsWith(".sqt")) {
            System.err.println("Parsing " + filename + " as a Sequest sqt file");
            sequestSqtHandler sh = new sequestSqtHandler(filename);
            sh.sequestSqtParse();
            handler = sh;
        } else {  // Maybe it is an XML file?

            CheckXMLFiletype();

            switch (filetype) {
                case MASCOT:
                    System.err.println(filename + " is a Mascot XML file, this is not supported");
                    //handler = new mascotXMLHandler(filename);
                    //XMLParse();
                    break;
                case OMSSA:
                    System.err.println("Parsing " + filename + " as an OMSSA file");
                    handler = new omssaHandler(filename);
                    XMLParse(taskObserver);
                    ((omssaHandler) handler).scaleMasses();
                    break;
                case XTANDEM:
                    System.err.println("Parsing " + filename + " as a X!Tandem file");
                    handler = new xtandemHandler(filename);
                    XMLParse(taskObserver);
                    break;
                case PEPXML:
                    System.err.println("Parsing " + filename + " as a PepXML file");
                    handler = new pepXMLHandler(filename);
                    XMLParse(taskObserver);
                    break;
                case UNKNOWN:
                    System.err.println("Unable to determine filetype for: " + filename);
                    break;
            }
        }
    }

    public ArrayList<PeptideHit> getPeptideHits() {
        return handler.getPeptideHits();
    }

    public HashMap<String, ProteinInfo> getProteinDB() {
        return handler.getProteinDB();
    }

    public FileInformation getFileInformation() {
        return handler.getFileInformation();
    }

    protected int scanFilenameToScanNumber(String fn) {
        StringBuilder sb = new StringBuilder(fn);
        int val, start, stop;
        start = sb.indexOf(".") + 1;
        stop = sb.substring(start).indexOf(".") + start;
        val = Integer.parseInt(sb.substring(start, stop));
        return val;
    }

    private void XMLParse(InputStreamObserver taskObserver) {
        try {
            // Parse the input
            xmlReader = XMLReaderFactory.createXMLReader();
            xmlReader.setContentHandler(handler);

            ObserverableInputStream ois = new ObserverableInputStream(new FileInputStream(file), taskObserver);
            BufferedInputStream bin = new BufferedInputStream(ois);
            xmlReader.parse(new InputSource(bin));
        } catch (SAXException t) {
            filetype = AnalysisProgramType.UNKNOWN;
        } catch (Throwable t) {
            t.printStackTrace();
        }
    }

    void CheckXMLFiletype() {
        CheckTypeHandler check_handler = new CheckTypeHandler();

        // Use the default (non-validating) parser
        try {
            // Parse the input
            xmlReader = XMLReaderFactory.createXMLReader();
            xmlReader.setContentHandler(check_handler);
            xmlReader.parse(new InputSource(new FileInputStream(file)));
        } catch (TypeFoundException t) {
            filetype = t.getFileType();
            //System.err.println(filename + " must be a " + filetype.toString() + " file");
        } catch (SAXException t) {
            filetype = AnalysisProgramType.UNKNOWN;
        } catch (Throwable t) {
            t.printStackTrace();
        }
    }

}

class CheckTypeHandler extends DefaultHandler {

    @Override
    public void startElement(String namespaceURI, String sName, String qName, Attributes attrs) throws TypeFoundException {
        if (sName.equals("MSSearch") || sName.equals("MSResponse")) {
            throw new TypeFoundException(AnalysisProgramType.OMSSA);
        }
        if (sName.equals("bioml")) {
            throw new TypeFoundException(AnalysisProgramType.XTANDEM);
        }
        if (sName.equals("msms_pipeline_analysis")) {
            throw new TypeFoundException(AnalysisProgramType.PEPXML);
        }
        if (sName.equals("mascot_search_results")) {
            throw new TypeFoundException(AnalysisProgramType.MASCOT);
        }
    }

    @Override
    public void endDocument() throws TypeFoundException {
        throw new TypeFoundException(AnalysisProgramType.UNKNOWN);
    }
}

class TypeFoundException extends SAXException {

    public AnalysisProgramType type;

    TypeFoundException(AnalysisProgramType ftype) {
        type = ftype;
    }

    public AnalysisProgramType getFileType() {
        return type;
    }
}
