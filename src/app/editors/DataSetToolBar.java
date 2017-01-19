package app.editors;

import org.eclipse.jface.action.IToolBarManager;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.part.EditorActionBarContributor;
import org.openlca.ilcd.commons.IDataSet;
import org.openlca.ilcd.commons.Ref;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import app.M;
import app.editors.contact.ContactEditor;
import app.editors.epd.EpdEditor;
import app.editors.flow.FlowEditor;
import app.editors.flowproperty.FlowPropertyEditor;
import app.editors.source.SourceEditor;
import app.editors.unitgroup.UnitGroupEditor;
import app.editors.upload.UploadDialog;
import app.rcp.Icon;
import app.util.Actions;
import app.util.MsgBox;

public class DataSetToolBar extends EditorActionBarContributor {

	private Logger log = LoggerFactory.getLogger(getClass());

	@Override
	public void contributeToToolBar(IToolBarManager manager) {
		manager.add(Actions.create(M.UploadDataSet,
				Icon.UPLOAD.des(), this::tryUpload));
	}

	private void tryUpload() {
		IEditorPart p = Editors.getActive();
		if (p.isDirty()) {
			MsgBox.error("#Unsaved changes",
					"#Please save the data set first.");
			return;
		}
		IDataSet ds = getDataSet(p);
		if (ds == null) {
			log.error("could not get data set from {}", p);
			return;
		}
		UploadDialog.open(Ref.of(ds));
	}

	private IDataSet getDataSet(IEditorPart p) {
		if (p instanceof ContactEditor)
			return ((ContactEditor) p).contact;
		if (p instanceof EpdEditor)
			return ((EpdEditor) p).dataSet.process;
		if (p instanceof FlowEditor)
			return ((FlowEditor) p).product.flow;
		if (p instanceof FlowPropertyEditor)
			return ((FlowPropertyEditor) p).property;
		if (p instanceof SourceEditor)
			return ((SourceEditor) p).source;
		if (p instanceof UnitGroupEditor)
			return ((UnitGroupEditor) p).unitGroup;
		return null;
	}

}