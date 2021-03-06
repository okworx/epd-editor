package app.rcp;

import java.io.File;
import java.net.URL;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.eclipse.core.runtime.Platform;
import org.eclipse.osgi.service.datalocation.Location;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Reads and writes values from and to the *.ini file which is located in the
 * installation directory of the application. Writing is not possible when it is
 * installed in a read-only folder. This class is independent from the user
 * interface and could be re-used in other packages if needed.
 */
public class IniFile {

	public String lang = "en";
	public int maxMemory = 1024;

	public static IniFile read() {
		try {
			File iniFile = getIniFile();
			if (!iniFile.exists())
				return new IniFile();
			return parseFile(iniFile);
		} catch (Exception e) {
			Logger log = LoggerFactory.getLogger(IniFile.class);
			log.error("failed to read *.ini file", e);
			return new IniFile();
		}
	}

	public void write() {
		Logger log = LoggerFactory.getLogger(IniFile.class);
		try {
			File iniFile = getIniFile();
			if (!iniFile.exists())
				return;
			List<String> oldLines = Files.readAllLines(iniFile.toPath());
			List<String> newLines = new ArrayList<>();
			boolean nextIsLanguage = false;
			for (String line : oldLines) {
				if (line.trim().equals("-nl")) {
					nextIsLanguage = true;
					newLines.add(line);
				} else if (nextIsLanguage) {
					nextIsLanguage = false;
					newLines.add(lang);
				} else if (line.trim().startsWith("-Xmx")) {
					newLines.add("-Xmx" + maxMemory + "M");
				} else {
					newLines.add(line);
				}
			}
			Files.write(iniFile.toPath(), newLines);
			log.info("wrote ini file {}", iniFile);
		} catch (Exception e) {
			log.error("failed to write *.ini file", e);
		}
	}

	private static File getIniFile() {
		Location location = Platform.getInstallLocation();
		URL url = location.getURL();
		File dir = new File(url.getFile());
		// TODO: if OS == macOS dir = Contents/MacOS ?
		for (File f : dir.listFiles()) {
			if (f.isFile() && f.getName().endsWith(".ini"))
				return f;
		}
		Logger log = LoggerFactory.getLogger(IniFile.class);
		log.warn("Could not find *.ini file");
		return new File(dir, "epd-editor.ini");
	}

	private static IniFile parseFile(File iniFile) throws Exception {
		List<String> lines = Files.readAllLines(iniFile.toPath());
		IniFile ini = new IniFile();
		boolean nextIsLanguage = false;
		for (String line : lines) {
			if (line.trim().equals("-nl")) {
				nextIsLanguage = true;
				continue;
			}
			if (nextIsLanguage) {
				ini.lang = line.trim();
				nextIsLanguage = false;
			} else if (line.trim().startsWith("-Xmx")) {
				readMemory(line, ini);
			}
		}
		return ini;
	}

	private static void readMemory(String line, IniFile ini) {
		if (line == null || ini == null)
			return;
		String memStr = line.trim().toLowerCase();
		Pattern pattern = Pattern.compile("-xmx([0-9]+)m");
		Matcher matcher = pattern.matcher(memStr);
		if (!matcher.find()) {
			Logger log = LoggerFactory.getLogger(IniFile.class);
			log.warn("could not extract memory value from "
					+ "{} with -xmx([0-9]+)m", memStr);
			return;
		}
		try {
			int val = Integer.parseInt(matcher.group(1));
			ini.maxMemory = val;
		} catch (Exception e) {
			Logger log = LoggerFactory.getLogger(IniFile.class);
			log.error("failed to parse memory value from ini: " + memStr, e);
		}
	}

	@Override
	public IniFile clone() {
		IniFile clone = new IniFile();
		clone.lang = lang;
		clone.maxMemory = maxMemory;
		return clone;
	}

	@Override
	public boolean equals(Object obj) {
		if (obj == null)
			return false;
		if (this == obj)
			return true;
		if (!(obj instanceof IniFile))
			return false;
		IniFile other = (IniFile) obj;
		return Objects.equals(this.lang, other.lang)
				&& this.maxMemory == other.maxMemory;
	}
}