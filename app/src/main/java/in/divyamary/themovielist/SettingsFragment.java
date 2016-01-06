package in.divyamary.themovielist;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.preference.PreferenceGroup;


public class SettingsFragment extends PreferenceFragment implements SharedPreferences.OnSharedPreferenceChangeListener {

    public SettingsFragment() {
        // Required empty public constructor
    }

    @Override
    public void onResume() {
        super.onResume();
        getPreferenceScreen().getSharedPreferences()
                .registerOnSharedPreferenceChangeListener(this);
    }

    @Override
    public void onPause() {
        super.onPause();
        getPreferenceScreen().getSharedPreferences()
                .unregisterOnSharedPreferenceChangeListener(this);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.pref_general);
        initializeSummary(getPreferenceScreen());
    }

    private void initializeSummary(Preference preference) {
        if(preference instanceof PreferenceGroup){
            PreferenceGroup preferenceGroup = (PreferenceGroup) preference;
            for (int i = 0; i < preferenceGroup.getPreferenceCount(); i++) {
                initializeSummary(preferenceGroup.getPreference(i));
            }
        } else {
            updatePreferenceSummary(preference);
        }
    }

    private void updatePreferenceSummary(Preference preference) {
        if (preference instanceof ListPreference) {
            ListPreference listPref = (ListPreference) preference;
            preference.setSummary(listPref.getEntry());
        }
    }


    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        Preference preference = findPreference(key);
        if(key.equals(getString(R.string.pref_sort_type))){
            // Set summary to be the user-description for the selected value
            preference.setSummary(sharedPreferences.getString(key, ""));
        }

    }
}
