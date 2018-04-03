package com.scandit.extendedsample;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.graphics.RectF;
import android.os.Build;
import android.os.Bundle;
import android.preference.EditTextPreference;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.preference.PreferenceCategory;
import android.preference.PreferenceManager;

import com.scandit.barcodepicker.ScanOverlay;
import com.scandit.barcodepicker.ScanSettings;
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

		if (preference.getKey().equals("viewfinder_style")) {
			boolean enabled = true;
			if (preference.getValue().equals("2")) {
				enabled = false;
			}
			getPreferenceScreen().findPreference("viewfinder_width").setEnabled(enabled);
			getPreferenceScreen().findPreference("viewfinder_height").setEnabled(enabled);
			getPreferenceScreen().findPreference("viewfinder_landscape_width").setEnabled(enabled);
			getPreferenceScreen().findPreference("viewfinder_landscape_height").setEnabled(enabled);
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
	    } else if (prefs.getBoolean("rotate_enabled", true)) {
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

		settings.setSymbologyEnabled(Barcode.SYMBOLOGY_DOTCODE,
				prefs.getBoolean("dotcode_enabled", false));

		settings.setSymbologyEnabled(Barcode.SYMBOLOGY_MAXICODE,
				prefs.getBoolean("maxicode_enabled", false));

		settings.setSymbologyEnabled(Barcode.SYMBOLOGY_QR,
				prefs.getBoolean("qr_enabled", true));

		settings.setSymbologyEnabled(Barcode.SYMBOLOGY_MICRO_QR,
				prefs.getBoolean("micro_qr_enabled", false));

		settings.setSymbologyEnabled(Barcode.SYMBOLOGY_DATA_MATRIX,
				prefs.getBoolean("data_matrix_enabled", true));

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

		settings.setHighDensityModeEnabled(prefs.getBoolean("hd_mode_enabled", false));
        if (prefs.getBoolean("two_digit_add_on_enabled", false) ||
            prefs.getBoolean("five_digit_add_on_enabled", false)) {
           settings.setMaxNumberOfCodesPerFrame(2);
        }
		settings.getSymbologySettings(Barcode.SYMBOLOGY_MSI_PLESSEY).setChecksums(getMsiPlesseyChecksumType(prefs));

		if (settings.isSymbologyEnabled(Barcode.SYMBOLOGY_DATA_MATRIX)) {
			settings.getSymbologySettings(Barcode.SYMBOLOGY_DATA_MATRIX)
					.setColorInvertedEnabled(prefs.getBoolean("inverse_recognition_data_matrix", false));
		}

		if (settings.isSymbologyEnabled(Barcode.SYMBOLOGY_QR)) {
			settings.getSymbologySettings(Barcode.SYMBOLOGY_QR)
					.setColorInvertedEnabled(prefs.getBoolean("inverse_recognition_qr", false));
		}

		if (settings.isSymbologyEnabled(Barcode.SYMBOLOGY_MICRO_QR)) {
			settings.getSymbologySettings(Barcode.SYMBOLOGY_MICRO_QR)
					.setColorInvertedEnabled(prefs.getBoolean("inverse_recognition_micro_qr", false));
		}

		if (settings.isSymbologyEnabled(Barcode.SYMBOLOGY_DOTCODE)) {
			settings.getSymbologySettings(Barcode.SYMBOLOGY_DOTCODE)
					.setColorInvertedEnabled(prefs.getBoolean("inverse_recognition_dotcode", false));
		}

		boolean restrictScanningArea = prefs.getBoolean("restrict_scanning_area", false);
		if (restrictScanningArea) {
			float hotSpotY = prefs.getInt("hot_spot_y", 50) / 100.0f;
			float height = prefs.getInt("hot_spot_height", 25) / 100.0f;
			float width = prefs.getInt("hot_spot_width", 100) / 100.0f;

			RectF restricted = new RectF(0.5f - width * 0.5f, hotSpotY - 0.5f * height,
					                     0.5f + width * 0.5f, hotSpotY + 0.5f * height);

			settings.setScanningHotSpot(0.5f, hotSpotY);
			settings.setActiveScanningArea(ScanSettings.ORIENTATION_LANDSCAPE, restricted);
			settings.setActiveScanningArea(ScanSettings.ORIENTATION_PORTRAIT, restricted);
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

	public static void applyUISettings(Context context, ScanOverlay overlay) {
		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
		int viewfinder_val = Integer.valueOf(prefs.getString("viewfinder_style", "0"));
		if (viewfinder_val == 0) {
			overlay.setGuiStyle(ScanOverlay.GUI_STYLE_DEFAULT);
		} else if (viewfinder_val == 1) {
			overlay.setGuiStyle(ScanOverlay.GUI_STYLE_LASER);
		} else if (viewfinder_val == 2) {
			overlay.setGuiStyle(ScanOverlay.GUI_STYLE_NONE);
		} else {
			overlay.setGuiStyle(ScanOverlay.GUI_STYLE_LOCATIONS_ONLY);
		}

		overlay.setViewfinderDimension(
				prefs.getInt("viewfinder_width",  70) / 100.0f,
				prefs.getInt("viewfinder_height",  30) / 100.0f,
				prefs.getInt("viewfinder_landscape_width",  40) / 100.0f,
				prefs.getInt("viewfinder_landscape_height",  30) / 100.0f);

		overlay.setBeepEnabled(prefs.getBoolean("beep_enabled", true));
		overlay.setVibrateEnabled(prefs.getBoolean("vibrate_enabled", false));


		overlay.setTorchEnabled(prefs.getBoolean("torch_enabled", true));
		overlay.setTorchButtonMarginsAndSize(prefs.getInt("torch_button_x", 15),
											 prefs.getInt("torch_button_y", 15),
				                             40, 40);

		int cameraSwitchVisibility = getCameraSwitchVisibility(prefs);
		overlay.setCameraSwitchVisibility(cameraSwitchVisibility);
		overlay.setCameraSwitchButtonMarginsAndSize(prefs.getInt("camera_switch_button_x", 5),
				                                    prefs.getInt("camera_switch_button_y", 5),
				                                    40, 40);
	}

	private static int getCameraSwitchVisibility(SharedPreferences prefs) {
		int val = Integer.valueOf(prefs.getString("camera_switch_visibility", "0"));
		int cameraSwitchVisibility = ScanOverlay.CAMERA_SWITCH_NEVER;
		if (val == 1) {
			cameraSwitchVisibility = ScanOverlay.CAMERA_SWITCH_ON_TABLET;
		} else if (val == 2) {
			cameraSwitchVisibility = ScanOverlay.CAMERA_SWITCH_ALWAYS;
		}
		return cameraSwitchVisibility;
	}
}
