package app.navi;

import java.util.List;

import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.ui.actions.ActionContext;
import org.eclipse.ui.navigator.CommonActionProvider;
import org.openlca.ilcd.io.SodaConnection;

import app.navi.actions.ClassificationSync;
import app.navi.actions.ConnectionDeleteAction;
import app.navi.actions.FileDeletion;
import app.navi.actions.FileImport;
import app.navi.actions.NewConnectionAction;
import app.navi.actions.NewDataSetAction;
import app.navi.actions.RefDeleteAction;
import app.rcp.Icon;
import app.store.Connections;
import app.util.Viewers;

public class NavigationMenu extends CommonActionProvider {

	@Override
	public void fillContextMenu(IMenuManager menu) {
		ActionContext con = getContext();
		IStructuredSelection s = (IStructuredSelection) con
				.getSelection();
		List<NavigationElement> elements = Viewers.getAll(s);
		if (elements.isEmpty())
			return;
		NavigationElement first = elements.get(0);
		if (first instanceof TypeElement) {
			TypeElement e = (TypeElement) first;
			menu.add(new NewDataSetAction(e));
		}
		if (first instanceof CategoryElement) {
			CategoryElement e = (CategoryElement) first;
			menu.add(new NewDataSetAction(e));
		}
		if (first instanceof FolderElement) {
			FolderElement e = (FolderElement) first;
			catSync(e, menu);
			menu.add(new FileImport(e));
		}
		if (first instanceof RefElement) {
			RefElement e = (RefElement) first;
			menu.add(new NewDataSetAction(e));
			menu.add(new RefDeleteAction(e));
		}
		if (first instanceof FileElement) {
			FileElement e = (FileElement) first;
			menu.add(new FileDeletion(e));
		}
		if (first instanceof ConnectionFolder) {
			ConnectionFolder cf = (ConnectionFolder) first;
			menu.add(new NewConnectionAction(cf));
		}
		if (first instanceof ConnectionElement) {
			ConnectionElement e = (ConnectionElement) first;
			menu.add(new ConnectionDeleteAction(e));
		}
	}

	private void catSync(FolderElement e, IMenuManager menu) {
		if (e == null || e.type != FolderType.CLASSIFICATION)
			return;
		MenuManager syncMenu = new MenuManager("#Update classifications",
				Icon.DOWNLOAD.des(), "UpdateClassifications");
		menu.add(syncMenu);
		for (SodaConnection con : Connections.get()) {
			syncMenu.add(new ClassificationSync(con));
		}
	}

}
