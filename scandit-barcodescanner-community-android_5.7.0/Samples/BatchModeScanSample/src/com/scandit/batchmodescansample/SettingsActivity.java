package com.scandit.batchmodescansample;
/*
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.os.Build;
import android.os.Bundle;
import android.preference.*;

import com.scandit.barcodepicker.ScanSettings;
import com.scandit.base.camera.profiles.DeviceProfile;
import com.scandit.base.system.SbSystemUtils;
import com.scandit.recognition.Barcode;
import com.scandit.recognition.SymbologySettings;

public class SettingsActivity extends PreferenceActivity implements OnSharedPreferenceChangeListener {
	
	@SuppressWarnings("deprecation")
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		if (Build.VERSION.SDK_INT >= 14) {
			super.setTheme(android.R.style.Theme_DeviceDefault);
		} else if (Build.VERSION.SDK_INT >= 11) {
			super.setTheme(android.R.style.Theme_Holo);
		} else {
			super.setTheme(android.R.style.Theme_Black_NoTitleBar);
		}
		
		addPreferencesFromResource(R.xml.preferences);
		
		// Loop over the categories.
		for (int i = 0; i < getPreferenceScreen().getPreferenceCount(); i++) {
			Preference categoryPref = getPreferenceScreen().getPreference(i);
			if (categoryPref instanceof PreferenceCategory) {
				PreferenceCategory category = (PreferenceCategory) categoryPref;
				// Loop over the preferences inside a category.
				for (int j = 0; j < category.getPreferenceCount(); j++) {
					Preference preference = category.getPreference(j);
				    if (preference instanceof ListPreference) {
				        updateListPreference((ListPreference) preference);
				    } else if (preference instanceof EditTextPreference) {
				    	updateEditTextPreference((EditTextPreference) preference);
				    }
				}
			}
		}
	}
	
	@SuppressWarnings("deprecation")
	void updateListPreference(ListPreference preference) {
        preference.setSummary(preference.getEntry());
        
        if (preference.getKey().equals("camera_switch_visibility")) {
        	boolean enabled = true;
        	if (preference.getValue().equals("0")) {
        		enabled = false;
        	}
        	getPreferenceScreen().findPreference("camera_switch_button_x").setEnabled(enabled);
        	getPreferenceScreen().findPreference("camera_switch_button_y").setEnabled(enabled);
        }
	}
	
	void updateEditTextPreference(EditTextPreference preference) {
        preference.setSummary(preference.getText());
	}
	
	@SuppressWarnings("deprecation")
	@Override
	protected void onResume() {
	    super.onResume();
	    getPreferenceScreen().getSharedPreferences()
	            .registerOnSharedPreferenceChangeListener(this);
	}

	@SuppressWarnings("deprecation")
	@Override
	protected void onPause() {
	    super.onPause();
	    getPreferenceScreen().getSharedPreferences()
	            .unregisterOnSharedPreferenceChangeListener(this);
	}
	
	public static void setActivityOrientation(Activity activity) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(activity);
        if (Build.MODEL.startsWith("Glass")) {
            // Glass somehow has problems with android:configChanges="orientation|screenSize" that
            // makes the launch of an activity in SCREEN_ORIENTATION_SENSOR wonky (flickering at
            // the beginning) and the differentiation between native portrait and landscape is
            // impossible.
            activity.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
	    } else if (prefs.getBoolean("rotate_enabled", false)) {
            activity.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_SENSOR);
        } else {
            if (SbSystemUtils.getDeviceDefaultOrientation(activity) == Configuration.ORIENTATION_PORTRAIT) {
                activity.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
            } else {
                activity.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
            }
        }
	}

	@SuppressWarnings("deprecation")
	public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
		Preference preference = findPreference(key);
	    if (preference instanceof ListPreference) {
	        updateListPreference((ListPreference) preference);
	    } else if (preference instanceof EditTextPreference) {
	    	updateEditTextPreference((EditTextPreference) preference);
	    }
	}
	public static void setEnabledSymbologies(SharedPreferences prefs, ScanSettings settings) {
		settings.setSymbologyEnabled(Barcode.SYMBOLOGY_EAN13,
				prefs.getBoolean("ean13_and_upc12_enabled", true));
		settings.setSymbologyEnabled(Barcode.SYMBOLOGY_UPCA,
				prefs.getBoolean("ean13_and_upc12_enabled", true));
		settings.setSymbologyEnabled(Barcode.SYMBOLOGY_EAN8,
				prefs.getBoolean("ean8_enabled", true));
		settings.setSymbologyEnabled(Barcode.SYMBOLOGY_UPCE,
				prefs.getBoolean("upce_enabled", true));
		settings.setSymbologyEnabled(Barcode.SYMBOLOGY_CODE39,
				prefs.getBoolean("code39_enabled", true));
		settings.setSymbologyEnabled(Barcode.SYMBOLOGY_CODE93,
				prefs.getBoolean("code93_enabled", false));
		settings.setSymbologyEnabled(Barcode.SYMBOLOGY_CODE11,
				prefs.getBoolean("code11_enabled", false));
		settings.setSymbologyEnabled(Barcode.SYMBOLOGY_CODE128,
				prefs.getBoolean("code128_enabled", true));
		settings.setSymbologyEnabled(Barcode.SYMBOLOGY_CODE25,
				prefs.getBoolean("code25_enabled", false));
		settings.setSymbologyEnabled(Barcode.SYMBOLOGY_INTERLEAVED_2_OF_5,
				prefs.getBoolean("itf_enabled", true));
		settings.setSymbologyEnabled(Barcode.SYMBOLOGY_MSI_PLESSEY,
				prefs.getBoolean("msi_plessey_enabled", false));
		settings.setSymbologyEnabled(Barcode.SYMBOLOGY_GS1_DATABAR,
				prefs.getBoolean("databar_enabled", false));
		settings.setSymbologyEnabled(Barcode.SYMBOLOGY_GS1_DATABAR_EXPANDED,
				prefs.getBoolean("databar_expanded_enabled", false));
		settings.setSymbologyEnabled(Barcode.SYMBOLOGY_GS1_DATABAR_LIMITED,
				prefs.getBoolean("databar_limited_enabled", false));
		settings.setSymbologyEnabled(Barcode.SYMBOLOGY_CODABAR,
				prefs.getBoolean("codabar_enabled", false));


		settings.setSymbologyEnabled(Barcode.SYMBOLOGY_AZTEC,
				prefs.getBoolean("aztec_enabled", false));

		settings.setSymbologyEnabled(Barcode.SYMBOLOGY_MAXICODE,
				prefs.getBoolean("maxicode_enabled", false));

		settings.setSymbologyEnabled(Barcode.SYMBOLOGY_QR,
				prefs.getBoolean("qr_enabled", false));

		settings.setSymbologyEnabled(Barcode.SYMBOLOGY_DATA_MATRIX,
				prefs.getBoolean("data_matrix_enabled", false));

		settings.setSymbologyEnabled(Barcode.SYMBOLOGY_PDF417,
				prefs.getBoolean("pdf417_enabled", false));
				
	    settings.setSymbologyEnabled(Barcode.SYMBOLOGY_MICRO_PDF417,
				prefs.getBoolean("micro_pdf417_enabled", false));

		settings.setSymbologyEnabled(Barcode.SYMBOLOGY_TWO_DIGIT_ADD_ON,
				prefs.getBoolean("two_digit_add_on_enabled", false));

		settings.setSymbologyEnabled(Barcode.SYMBOLOGY_FIVE_DIGIT_ADD_ON,
				prefs.getBoolean("five_digit_add_on_enabled", false));

		settings.setSymbologyEnabled(Barcode.SYMBOLOGY_KIX,
				prefs.getBoolean("kix_enabled", false));
		settings.setSymbologyEnabled(Barcode.SYMBOLOGY_RM4SCC,
				prefs.getBoolean("rm4scc_enabled", false));
	}
	
	public static ScanSettings getSettings(Context context) {
		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
		ScanSettings settings = ScanSettings.create();
		setEnabledSymbologies(prefs, settings);
        if (prefs.getBoolean("two_digit_add_on_enabled", false) ||
            prefs.getBoolean("five_digit_add_on_enabled", false)) {
           settings.setMaxNumberOfCodesPerFrame(2);
        }
		settings.getSymbologySettings(Barcode.SYMBOLOGY_MSI_PLESSEY)
				.setChecksums(getMsiPlesseyChecksumType(prefs));

		if (settings.isSymbologyEnabled(Barcode.SYMBOLOGY_DATA_MATRIX)) {
			settings.getSymbologySettings(Barcode.SYMBOLOGY_DATA_MATRIX)
					.setColorInvertedEnabled(prefs.getBoolean("inverse_recognition_data_matrix", false));
		}

		if (settings.isSymbologyEnabled(Barcode.SYMBOLOGY_QR)) {
			settings.getSymbologySettings(Barcode.SYMBOLOGY_QR)
					.setColorInvertedEnabled(prefs.getBoolean("inverse_recognition_qr", false));
		}

		if (settings.isSymbologyEnabled(Barcode.SYMBOLOGY_DOTCODE)) {
			settings.getSymbologySettings(Barcode.SYMBOLOGY_DOTCODE)
					.setColorInvertedEnabled(prefs.getBoolean("inverse_recognition_dotcode", false));
		}
		return settings;
	}

	private static int getMsiPlesseyChecksumType(SharedPreferences prefs) {
		int checksum = Integer.valueOf(prefs.getString("msi_plessey_checksum", "1"));
		int actualChecksum = SymbologySettings.CHECKSUM_MOD_10;
		if (checksum == 0) {
			actualChecksum = SymbologySettings.CHECKSUM_NONE;
		} else if (checksum == 2) {
			actualChecksum = SymbologySettings.CHECKSUM_MOD_11;
		} else if (checksum == 3) {
			actualChecksum = SymbologySettings.CHECKSUM_MOD_1010;
		} else if (checksum == 4) {
			actualChecksum = SymbologySettings.CHECKSUM_MOD_1110;
		}
		return actualChecksum;
	}

}
