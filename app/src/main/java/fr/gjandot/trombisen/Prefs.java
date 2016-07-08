package fr.gjandot.trombisen;

import android.os.Bundle;
import android.preference.Preference;
import android.preference.Preference.OnPreferenceChangeListener;
import android.preference.PreferenceActivity;

/**
 * The SettingsActivity is used to manage global preferences of the application. For more information, check settings.xml resource file
 * @author Magnitude Client
 */
public class Prefs extends PreferenceActivity {
    private ImageListPreference mListPref;   
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		//Loads preferences
		addPreferencesFromResource(R.xml.settings);
	}
	
	Preference.OnPreferenceChangeListener change_syst = new OnPreferenceChangeListener() {

	    @Override
	    public boolean onPreferenceChange(Preference preference, Object newValue) {
	        //Check that the string is an integer.
			int i = Integer.parseInt(newValue.toString());

			return true;
	    }
	};
	
}
