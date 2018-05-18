package epd.model;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import epd.util.Strings;

public class EpdProfile {

	public String id;
	public String name;
	public String description;
	public final List<Indicator> indicators = new ArrayList<>();
	public final List<Module> modules = new ArrayList<>();

	/** Get the indicator with the given ID from the this profile. */
	public Indicator indicator(String uuid) {
		if (uuid == null)
			return null;
		for (Indicator i : indicators) {
			if (Objects.equals(uuid, i.uuid))
				return i;
		}
		return null;
	}

	/** Get the module for the given name from the profile. */
	public Module module(String name) {
		if (name == null)
			return null;
		for (Module module : modules) {
			if (Strings.nullOrEqual(name, module.name))
				return module;
		}
		return null;
	}
}
