package app.navi;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.swt.graphics.Image;
import org.openlca.ilcd.io.FileStore;

import app.App;
import app.rcp.Icon;

public class ListFolderElement implements NavigationElement {

	public final ListType type;
	private final NavigationElement parent;
	private List<NavigationElement> childs;

	public ListFolderElement(NavigationElement parent, ListType type) {
		this.parent = parent;
		this.type = type;
	}

	@Override
	public List<NavigationElement> getChilds() {
		if (childs == null) {
			childs = new ArrayList<>();
			update();
		}
		return childs;
	}

	@Override
	public NavigationElement getParent() {
		return parent;
	}

	@Override
	public int compareTo(NavigationElement other) {
		return 0;
	}

	@Override
	public String getLabel() {
		if (type == null)
			return "?";
		switch (type) {
		case CLASSIFICATION:
			return "#Classifications";
		case LOCATION:
			return "#Locations";
		default:
			return "?";
		}
	}

	@Override
	public Image getImage() {
		return Icon.FOLDER.img();
	}

	@Override
	public void update() {
		if (childs == null)
			return;
		childs.clear();
		File folder = getFolder();
		if (folder == null || !folder.exists())
			return;
		File[] files = folder.listFiles();
		for (File file : files)
			childs.add(new FileElement(this, file));
	}

	public File getFolder() {
		FileStore store = App.store;
		File root = null;
		if (store == null)
			root = new File("data/ILCD");
		else
			root = store.getRootFolder();
		return new File(root, getFolderName());
	}

	private String getFolderName() {
		if (type == null)
			return "other";
		switch (type) {
		case CLASSIFICATION:
			return "classifications";
		case LOCATION:
			return "locations";
		default:
			return "other";
		}
	}
}