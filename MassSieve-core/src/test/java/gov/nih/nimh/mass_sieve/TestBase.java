package gov.nih.nimh.mass_sieve;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.io.IOUtils;
import org.apache.commons.vfs.FileObject;
import org.apache.commons.vfs.FileSystemException;
import org.apache.commons.vfs.FileSystemManager;
import org.apache.commons.vfs.VFS;

import static org.junit.Assert.*;

/**
 * 
 * @author Alex Turbin (alex.academATgmail.com)
 */
public class TestBase {

	/**
	 * For each required file creates corresponding File object. If no required
	 * files provided, returns default test file. <br/>
	 * Each of required files must exist.
	 * @throws IOException 
	 * @throws FileNotFoundException 
	 */
	protected File[] getSeqFiles(String... requiredFiles) throws FileNotFoundException, IOException {
		List<File> result = new ArrayList<File>();

		FileSystemManager fsManager = VFS.getManager();

		// add files to a list
		if (null == requiredFiles || requiredFiles.length == 0) {
			result.add(new File(TestConstants.DIR_DATA, TestConstants.DEF_TEST_FILE));
		} else {
			for (String requiredFile : requiredFiles) {
				result.add(new File(TestConstants.DIR_DATA, requiredFile));
			}
		}

		// check if required files exists
		for (File f : result) {
			FileObject fileObject = fsManager.resolveFile("tar:gz:file:/" + f.getAbsolutePath() + ".tgz/"+f.getName());


			IOUtils.copyLarge(fileObject.getContent().getInputStream(), new FileOutputStream(f));
			
			if (!f.exists()) {
				fail("File doesn't exist:" + f.getAbsolutePath());
			}
		}

		return result.toArray(new File[result.size()]);
	}

}
