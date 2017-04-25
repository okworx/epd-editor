package app.store.validation;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.window.Window;
import org.eclipse.jface.wizard.Wizard;
import org.eclipse.jface.wizard.WizardDialog;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.openlca.ilcd.commons.Ref;
import org.openlca.ilcd.util.DependencyTraversal;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import app.App;
import app.M;
import app.StatusView;
import app.editors.RefTableLabel;
import app.util.Controls;
import app.util.MsgBox;
import app.util.Tables;
import app.util.UI;
import epd.util.ExtensionRefs;

public class ValidationDialog extends Wizard {

	private Logger log = LoggerFactory.getLogger(getClass());

	private final Ref ref;
	private final List<Ref> allRefs = new ArrayList<>();

	private Page page;

	private ValidationDialog(Ref ref) {
		this.ref = ref;
		allRefs.add(ref);
		setNeedsProgressMonitor(true);
	}

	public static int open(Ref ref) {
		if (ref == null)
			return Window.CANCEL;
		ValidationDialog d = new ValidationDialog(ref);
		WizardDialog dialog = new WizardDialog(UI.shell(), d);
		dialog.setPageSize(150, 300);
		return dialog.open();
	}

	@Override
	public boolean performFinish() {
		try {
			Validation v = new Validation(allRefs);
			getContainer().run(true, false, v);
			StatusView.open("Validation", v.getStatus());
			return true;
		} catch (Exception e) {
			MsgBox.error("#Error in validation", e.getMessage());
			log.error("validation failed", e);
			return false;
		}
	}

	@Override
	public void addPages() {
		page = new Page();
		addPage(page);
	}

	private class Page extends WizardPage {

		private TableViewer table;

		private Page() {
			super("ValidationDialogPage", "#Validate data set" + ": " +
					App.header(ref.name, 50), null);
			setPageComplete(true);
		}

		@Override
		public void createControl(Composite root) {
			Composite comp = new Composite(root, SWT.NONE);
			setControl(comp);
			UI.gridLayout(comp, 1);
			createCheck(comp);
			createTable(comp);
		}

		private void createCheck(Composite comp) {
			Button check = new Button(comp, SWT.CHECK);
			check.setText("#Include dependencies");
			Controls.onSelect(check, e -> {
				if (check.getSelection()) {
					collectRefs();
				} else {
					allRefs.clear();
					allRefs.add(ref);
					table.setInput(allRefs);
				}
			});
		}

		private void createTable(Composite parent) {
			table = Tables.createViewer(parent, "#Data set", M.UUID,
					M.Version);
			table.setLabelProvider(new RefTableLabel());
			Tables.bindColumnWidths(table, 0.6, 0.2, 0.2);
			table.setInput(allRefs);
		}

		private void collectRefs() {
			try {
				getContainer().run(true, false, monitor -> {
					monitor.beginTask("#Collect references:",
							IProgressMonitor.UNKNOWN);
					allRefs.clear();
					new DependencyTraversal(App.store).on(ref, ds -> {
						Ref next = Ref.of(ds);
						monitor.subTask(App.header(next.name, 75));
						allRefs.add(next);
						ExtensionRefs.collect(ds, allRefs);
					});
					App.runInUI("update table", () -> table.setInput(allRefs));
					monitor.done();
				});
			} catch (Exception e) {
				log.error("failed to collect references", e);
			}
		}

	}

}