package me.jiangmin.dbb;

import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceActivity;

/**
 * 
 */
public class SettingsActivity extends PreferenceActivity {
	
	/**
	 * º”‘ÿ≤Àµ•œÓ
	 */
	@Override
	protected void onPostCreate(Bundle savedInstanceState) {
		super.onPostCreate(savedInstanceState);
		addPreferencesFromResource(R.xml.pref_dbb);
		
		//ÃÓ≥‰summary
		findPreference("pref_txt_email").setSummary(getPreferenceScreen().getSharedPreferences().getString("pref_txt_email", "Œ¥ÃÓ–¥"));
		getPreferenceScreen().getSharedPreferences().registerOnSharedPreferenceChangeListener(onSharedPreferenceChangeListener);
		
	}
	
	public OnSharedPreferenceChangeListener onSharedPreferenceChangeListener = new OnSharedPreferenceChangeListener() {
		
		@Override
		public void onSharedPreferenceChanged(SharedPreferences sharedPreferences,
				String key) {
			
			if (key.equals("pref_txt_email")) {
				Preference preference = findPreference(key);
				preference.setSummary(sharedPreferences.getString(key, ""));
			}
			
		}
	};
	
	
}
