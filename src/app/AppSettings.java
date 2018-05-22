package app;

import java.io.File;

import app.store.Json;

public class AppSettings {

	/** The ID of the EPD profile that should be used in the application. */
	public String profile = "EN_15804";

	public String lang = "en";
	public boolean showDataSetXML = false;
	public boolean showDataSetDependencies = false;

	/**
	 * The name of the validation profile that is used when validating the data
	 * sets. It is the name of the respective file that is stored in the
	 * 'validation_profiles' folder of the workspace directory.
	 */
	public String validationProfile;

	public void save() {
		Json.write(this, new File(App.workspace, "settings.json"));
	}

	static AppSettings load() {
		File f = new File(App.workspace, "settings.json");
		if (!f.exists())
			return new AppSettings();
		AppSettings as = Json.read(f, AppSettings.class);
		return as != null ? as : new AppSettings();
	}

	@Override
	public AppSettings clone() {
		AppSettings clone = new AppSettings();
		clone.profile = profile;
		clone.lang = lang;
		clone.showDataSetXML = showDataSetXML;
		clone.showDataSetDependencies = showDataSetDependencies;
		clone.validationProfile = validationProfile;
		return clone;
	}

	public void setValues(AppSettings from) {
		if (from == null)
			return;
		profile = from.profile;
		lang = from.lang;
		showDataSetXML = from.showDataSetXML;
		showDataSetDependencies = from.showDataSetDependencies;
		validationProfile = from.validationProfile;
	}
}
