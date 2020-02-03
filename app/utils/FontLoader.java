package utils;

import java.awt.Font;
import java.awt.FontFormatException;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import play.Logger;

/**
 * This utility is used to load custom fonts onto the JVM.  JasperReports, for instance,
 * can only render fonts that are available to the JVM, so to use the Museo fonts, they must be loaded.
 * <p>
 * With Java 6, only <code>.ttf</code> fonts load in the JVM successfully.  Open-type fonts will appear
 * to load successfully, but they don't. However, whenever we switch to Java 7, the .otf fonts
 * should load successfully.  
 * <p>
 * Note:  this is not optimized for performance! 
 * 
 * 
 * @author reesbyars
 * 
 * @see {@link jobs.JasperReportsCompilationJob}
 *
 */
public class FontLoader {
	
	public static final String DEFAULT_FONT_DIR_PATH = "./public/fonts/";
	
	/**
	 * loads fonts from {@value #DEFAULT_FONT_DIR_PATH}
	 * 
	 * @see {@link jobs.JasperReportsCompilationJob}
	 */
	public static void loadFromDefaultDir() {
		loadTypeFromDir(DEFAULT_FONT_DIR_PATH, ".ttf", Font.TRUETYPE_FONT);
		//loadTypeFromDir(DEFAULT_FONT_DIR_PATH, ".otf", Font.TRUETYPE_FONT);
	}
	
	private static void loadTypeFromDir(String dirPath, String fontExtension, int fontFormat) {
		File dir = new File(dirPath); 
		if (dir.isDirectory()) {
			for (File file : dir.listFiles()) {
				String fontPath = file.getPath();
				if (fontPath.endsWith(fontExtension)) {
					try {
						FileInputStream in = new FileInputStream(file);
						Font newFont = Font.createFont(fontFormat, in);
						Logger.info("Loaded font %s from location %s.", newFont.getFontName(), fontPath);
					} catch (FileNotFoundException fnfe) {
						Logger.error("Could not load font from location %s.  Message:  ", fontPath, fnfe.getMessage());
					} catch (IOException ioe) {
						Logger.error("Could not load font from location %s.  Message:  ", fontPath, ioe.getMessage());
					} catch (FontFormatException ffe) {
						Logger.error("Could not load font from location %s.  Message:  ", fontPath, ffe.getMessage());
					}
				}
			}
		}
	}

}
