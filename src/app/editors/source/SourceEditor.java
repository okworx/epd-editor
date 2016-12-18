package app.editors.source;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorSite;
import org.eclipse.ui.PartInitException;
import org.openlca.ilcd.commons.AdminInfo;
import org.openlca.ilcd.commons.DataEntry;
import org.openlca.ilcd.commons.Publication;
import org.openlca.ilcd.commons.Ref;
import org.openlca.ilcd.sources.DataSetInfo;
import org.openlca.ilcd.sources.Source;
import org.openlca.ilcd.sources.SourceInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import app.App;
import app.editors.DataSetEditor;
import app.editors.DependencyPage;
import app.editors.Editors;
import app.editors.RefEditorInput;
import epd.model.Version;
import epd.model.Xml;
import epd.util.Strings;

public class SourceEditor extends DataSetEditor {

	private static final String ID = "source.editor";

	Source source;

	public static void open(Ref ref) {
		if (ref == null)
			return;
		RefEditorInput input = new RefEditorInput(ref);
		Editors.open(input, ID);
	}

	@Override
	public void init(IEditorSite s, IEditorInput input)
			throws PartInitException {
		super.init(s, input);
		setPartName(Strings.cut(input.getName(), 75));
		try {
			RefEditorInput in = (RefEditorInput) input;
			source = App.store.get(Source.class, in.ref.uuid);
			initStructs();
		} catch (Exception e) {
			throw new PartInitException(
					"Failed to open editor: no correct input", e);
		}
	}

	private void initStructs() {
		if (source == null)
			source = new Source();
		if (source.adminInfo == null)
			source.adminInfo = new AdminInfo();
		if (source.adminInfo.dataEntry == null)
			source.adminInfo.dataEntry = new DataEntry();
		if (source.adminInfo.publication == null)
			source.adminInfo.publication = new Publication();
		if (source.sourceInfo == null)
			source.sourceInfo = new SourceInfo();
		if (source.sourceInfo.dataSetInfo == null)
			source.sourceInfo.dataSetInfo = new DataSetInfo();
	}

	@Override
	public void doSave(IProgressMonitor monitor) {
		try {
			updateVersion();
			App.store.put(source, source.getUUID());
			// TODO: navigation refresh
			for (Runnable handler : saveHandlers) {
				handler.run();
			}
			dirty = false;
			editorDirtyStateChanged();
		} catch (Exception e) {
			Logger log = LoggerFactory.getLogger(getClass());
			log.error("failed to save contact data set");
		}
	}

	private void updateVersion() {
		AdminInfo info = source.adminInfo;
		Version v = Version.fromString(info.publication.version);
		v.incUpdate();
		info.publication.version = v.toString();
		info.dataEntry.timeStamp = Xml.now();
	}

	@Override
	protected void addPages() {
		try {
			addPage(new SourcePage(this));
			addPage(new DependencyPage(this, source));
		} catch (Exception e) {
			Logger log = LoggerFactory.getLogger(getClass());
			log.error("failed to add page", e);
		}
	}
}