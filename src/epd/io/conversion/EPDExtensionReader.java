package epd.io.conversion;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;

import org.openlca.ilcd.processes.Process;
import org.openlca.ilcd.util.Processes;
import org.slf4j.LoggerFactory;

import epd.model.EpdDataSet;
import epd.model.EpdProfile;
import epd.model.ModuleEntry;
import epd.model.SubType;
import epd.model.content.ContentDeclaration;
import epd.model.qmeta.QMetaData;
import epd.util.Strings;

/**
 * Converts an ILCD process data set to an EPD data set.
 */
class EPDExtensionReader {

	private final Process process;
	private final EpdProfile profile;

	private EPDExtensionReader(Process process, EpdProfile profile) {
		this.process = process;
		this.profile = profile;
	}

	static EpdDataSet read(Process process, EpdProfile profile) {
		return new EPDExtensionReader(process, profile).read();
	}

	private EpdDataSet read() {
		var epd = new EpdDataSet(process);
		readExtensions(epd);
		return epd;
	}

	private void readExtensions(EpdDataSet epd) {
		readSubType(epd);
		readPublicationDate(epd);
		PublisherRef.read(epd);
		OriginalEPDRef.read(epd);
		epd.qMetaData = QMetaData.read(process);

		// read the extensions that are stored under `dataSetInformation`
		var info = Processes.getDataSetInfo(process);
		if (info == null || info.getEpdExtension() == null)
			return;
		var other = info.getEpdExtension();
		List<ModuleEntry> modules = ModuleConverter.readModules(other, profile);
		epd.moduleEntries.addAll(modules);
		epd.contentDeclaration = ContentDeclaration.read(other);
	}

	private void readSubType(EpdDataSet dataSet) {
		var method = Processes.getInventoryMethod(process);
		if (method == null || method.getEpdExtension() == null)
			return;
		var elem = Dom.getElement(method.getEpdExtension(), "subType");
		if (elem != null) {
			dataSet.subType = SubType.fromLabel(elem.getTextContent());
		}
	}

	private void readPublicationDate(EpdDataSet epd) {
		var time = Processes.getTime(epd.process);
		if (time == null || time.getEpdExtension() == null)
			return;
		var elem = Dom.getElement(time.getEpdExtension(), "publicationDateOfEPD");
		if (elem == null)
			return;
		var text = elem.getTextContent();
		if (Strings.nullOrEmpty(text))
			return;
		try {
			epd.publicationDate = LocalDate.parse(
				text, DateTimeFormatter.ISO_DATE);
		} catch (Exception e) {
			var log = LoggerFactory.getLogger(getClass());
			log.error("Invalid format for publication date: " + text, e);
		}
	}

}
