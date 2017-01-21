package app.store;

import java.io.File;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import javax.xml.bind.JAXB;

import org.openlca.ilcd.lists.Location;
import org.openlca.ilcd.lists.LocationList;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import app.App;

public final class Locations {

	private Locations() {
	}

	public static Set<Location> get() {
		if (App.store == null)
			return Collections.emptySet();
		File folder = new File(App.store.getRootFolder(), "locations");
		if (!folder.exists())
			return Collections.emptySet();
		HashSet<Location> set = new HashSet<>();
		try {
			for (File file : folder.listFiles()) {
				LocationList list = JAXB.unmarshal(file, LocationList.class);
				for (Location loc : list.locations)
					set.add(loc);
			}
		} catch (Exception e) {
			Logger log = LoggerFactory.getLogger(Store.class);
			log.error("failed read location files from " + folder, e);
		}
		return set;
	}
}