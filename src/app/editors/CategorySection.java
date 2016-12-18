package app.editors;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.List;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.viewers.ITableLabelProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.forms.widgets.FormToolkit;
import org.eclipse.ui.forms.widgets.Section;
import org.openlca.ilcd.commons.Category;
import org.openlca.ilcd.commons.Classification;
import org.openlca.ilcd.commons.DataSetType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import app.App;
import app.M;
import app.rcp.Icon;
import app.util.Actions;
import app.util.FileChooser;
import app.util.Tables;
import app.util.UI;
import app.util.Viewers;

public class CategorySection {

	private Logger log = LoggerFactory.getLogger(getClass());

	private final IEditor editor;
	private final DataSetType type;
	private final List<Classification> classifications;

	public CategorySection(IEditor editor, DataSetType type,
			List<Classification> list) {
		this.editor = editor;
		this.type = type;
		classifications = list;
	}

	public void render(Composite parent, FormToolkit tk) {
		Section section = UI.section(parent, tk, M.Classification);
		Composite composite = UI.sectionClient(section, tk);
		UI.gridLayout(composite, 1);
		TableViewer viewer = Tables.createViewer(composite,
				M.ClassificationSystem,
				M.CategoryPath);
		viewer.setLabelProvider(new RowLabel());
		viewer.setInput(classifications);
		Tables.bindColumnWidths(viewer, 0.3, 0.7);
		bindActions(section, viewer);
	}

	private void bindActions(Section section, TableViewer viewer) {
		Action importAction = Actions.create(
				M.ImportClassificationFile,
				Icon.IMPORT.des(),
				() -> importFile());
		Action add = Actions.create("#Add", Icon.ADD.des(),
				() -> addRow(viewer));
		Action delete = Actions.create("#Delete", Icon.DELETE.des(),
				() -> deleteRow(viewer));
		Actions.bind(section, importAction, add, delete);
		Actions.bind(viewer, add, delete);
	}

	private void addRow(TableViewer viewer) {
		CategoryDialog dialog = new CategoryDialog(type);
		if (dialog.open() != Window.OK)
			return;
		Classification classification = dialog.getSelection();
		if (classification == null)
			return;
		classifications.add(classification);
		viewer.setInput(classifications);
		editor.setDirty();
	}

	private void deleteRow(TableViewer viewer) {
		Classification classification = Viewers.getFirstSelected(viewer);
		if (classification == null)
			return;
		classifications.remove(classification);
		viewer.setInput(classifications);
		editor.setDirty();
	}

	private void importFile() {
		File file = FileChooser.open("*.xml");
		if (file == null)
			return;
		try {
			File rootDir = App.store.getRootFolder();
			if (!rootDir.exists())
				return;
			File dir = new File(rootDir, "classifications");
			if (!dir.exists())
				dir.mkdirs();
			File localFile = new File(dir, file.getName());
			Files.copy(file.toPath(), localFile.toPath(),
					StandardCopyOption.REPLACE_EXISTING);
		} catch (Exception e) {
			log.error("failed to import classification file", e);
		}
	}

	private class RowLabel extends LabelProvider implements
			ITableLabelProvider {

		@Override
		public Image getColumnImage(Object element, int col) {
			return null;
		}

		@Override
		public String getColumnText(Object element, int col) {
			if (!(element instanceof Classification))
				return null;
			Classification classification = (Classification) element;
			switch (col) {
			case 0:
				return classification.name;
			case 1:
				return getPath(classification);
			default:
				return null;
			}
		}

		private String getPath(Classification classification) {
			List<Category> classes = classification.categories;
			classification.categories.sort((c1, c2) -> c1.level - c2.level);
			StringBuilder path = new StringBuilder();
			for (int i = 0; i < classes.size(); i++) {
				Category clazz = classes.get(i);
				if (clazz.classId != null && clazz.classId.length() < 8)
					path.append(clazz.classId).append(" ");
				path.append(clazz.value);
				if (i < (classes.size() - 1))
					path.append(" / ");
			}
			return path.toString();
		}
	}

}