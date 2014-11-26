package com.nguyennk.proximitylock.helpers;

import android.app.ActivityManager;
import android.app.ActivityManager.RunningServiceInfo;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.hardware.SensorManager;

import com.nguyennk.proximitylock.services.ProximityService;

public class PreferencesHelper {
	// ===========================================================
	// Constants
	// ===========================================================
	private static final String KEY_MIN_WAVE_TIME = "min_wave";
	private static final String KEY_MAX_WAVE_TIME = "max_wave";
	private static final String KEY_IS_NOTIFICATION_ON = "is_notification_on";
	private static final String KEY_IS_RUNNING_FOREGROUND = "is_running_foreground";
	private static final String KEY_IS_START_ON_BOOT_ON = "is_start_on_boot_on";
	private static final String KEY_SENSOR_DELAY_VALUE = "sensor_delay_value";
	private static final String KEY_POCKET_DETECTION = "pocket_detection";
	private static final String KEY_DISABLE_IN_LANDSCAPE = "disable_in_landscape";

	// ===========================================================
	// Fields
	// ===========================================================

	// ===========================================================
	// Constructors
	// ===========================================================

	// ===========================================================
	// Getter & Setter
	// ===========================================================

	// ===========================================================
	// Methods for/from SuperClass/Interfaces
	// ===========================================================

	// ===========================================================
	// Methods
	// ===========================================================
	public static boolean isServiceRunning(Context context,
			Class<?> serviceClass) {
		ActivityManager manager = (ActivityManager) context
				.getSystemService(Context.ACTIVITY_SERVICE);
		for (RunningServiceInfo service : manager
				.getRunningServices(Integer.MAX_VALUE)) {
			if (serviceClass.getName().equals(service.service.getClassName())) {
				return true;
			}
		}
		return false;
	}

	public static boolean isServiceRunningForeground(Context context,
			Class<?> serviceClass) {
		ActivityManager manager = (ActivityManager) context
				.getSystemService(Context.ACTIVITY_SERVICE);
		for (RunningServiceInfo service : manager
				.getRunningServices(Integer.MAX_VALUE)) {
			if (serviceClass.getName().equals(service.service.getClassName())
					&& service.foreground == true) {
				return true;
			}
		}
		return false;
	}

	public static long getSavedMinWaveGestureLength(Context context) {
		SharedPreferences pref = context.getSharedPreferences(
				context.getPackageName(), Context.MODE_PRIVATE);
		return pref.getLong(KEY_MIN_WAVE_TIME,
				ProximityService.DEFAULT_MIN_WAVE_GESTURE_LENGTH);
	}

	public static long getSavedMaxWaveGestureLength(Context context) {
		SharedPreferences pref = context.getSharedPreferences(
				context.getPackageName(), Context.MODE_PRIVATE);
		return pref.getLong(KEY_MAX_WAVE_TIME,
				ProximityService.DEFAULT_MAX_WAVE_GESTURE_LENGTH);
	}

	public static boolean isNotificationOn(Context context) {
		SharedPreferences pref = context.getSharedPreferences(
				context.getPackageName(), Context.MODE_PRIVATE);
		return pref.getBoolean(KEY_IS_NOTIFICATION_ON, false);
	}

	public static boolean isForegroundModeOn(Context context) {
		SharedPreferences pref = context.getSharedPreferences(
				context.getPackageName(), Context.MODE_PRIVATE);
		return pref.getBoolean(KEY_IS_RUNNING_FOREGROUND, false);
	}

	public static boolean isStartOnBootOn(Context context) {
		SharedPreferences pref = context.getSharedPreferences(
				context.getPackageName(), Context.MODE_PRIVATE);
		return pref.getBoolean(KEY_IS_START_ON_BOOT_ON, false);
	}

	public static int getSensorDelayValue(Context context) {
		SharedPreferences pref = context.getSharedPreferences(
				context.getPackageName(), Context.MODE_PRIVATE);
		return pref.getInt(KEY_SENSOR_DELAY_VALUE,
				SensorManager.SENSOR_DELAY_FASTEST);
	}

	public static boolean isPocketDetectionOn(Context context) {
		SharedPreferences pref = context.getSharedPreferences(
				context.getPackageName(), Context.MODE_PRIVATE);
		return pref.getBoolean(KEY_POCKET_DETECTION, false);
	}
	
	public static boolean isDisabledInLandscape(Context context) {
		SharedPreferences pref = context.getSharedPreferences(
				context.getPackageName(), Context.MODE_PRIVATE);
		return pref.getBoolean(KEY_DISABLE_IN_LANDSCAPE, false);
	}

	public static void saveMinWaveGestureLength(Context context, long time) {
		SharedPreferences pref = context.getSharedPreferences(
				context.getPackageName(), Context.MODE_PRIVATE);
		Editor editor = pref.edit();

		editor.putLong(KEY_MIN_WAVE_TIME, time);
		editor.commit();
	}

	public static void saveMaxWaveGestureLength(Context context, long time) {
		SharedPreferences pref = context.getSharedPreferences(
				context.getPackageName(), Context.MODE_PRIVATE);
		Editor editor = pref.edit();

		editor.putLong(KEY_MAX_WAVE_TIME, time);
		editor.commit();
	}

	public static void saveNotificationState(Context context, boolean isOn) {
		SharedPreferences pref = context.getSharedPreferences(
				context.getPackageName(), Context.MODE_PRIVATE);
		Editor editor = pref.edit();

		editor.putBoolean(KEY_IS_NOTIFICATION_ON, isOn);
		editor.commit();
	}

	public static void saveForegroundState(Context context, boolean isOn) {
		SharedPreferences pref = context.getSharedPreferences(
				context.getPackageName(), Context.MODE_PRIVATE);
		Editor editor = pref.edit();

		editor.putBoolean(KEY_IS_RUNNING_FOREGROUND, isOn);
		editor.commit();
	}

	public static void saveStartOnBootState(Context context, boolean isOn) {
		SharedPreferences pref = context.getSharedPreferences(
				context.getPackageName(), Context.MODE_PRIVATE);
		Editor editor = pref.edit();

		editor.putBoolean(KEY_IS_START_ON_BOOT_ON, isOn);
		editor.commit();
	}

	public static void saveSensorDelayValue(Context context, int value) {
		SharedPreferences pref = context.getSharedPreferences(
				context.getPackageName(), Context.MODE_PRIVATE);
		Editor editor = pref.edit();

		editor.putInt(KEY_SENSOR_DELAY_VALUE, value);
		editor.commit();
	}

	public static void savePocketDetection(Context context, boolean isOn) {
		SharedPreferences pref = context.getSharedPreferences(
				context.getPackageName(), Context.MODE_PRIVATE);
		Editor editor = pref.edit();

		editor.putBoolean(KEY_POCKET_DETECTION, isOn);
		editor.commit();
	}
	
	public static void saveDisableInLandscapeSetting(Context context, boolean isDisableInLandscape) {
		SharedPreferences pref = context.getSharedPreferences(
				context.getPackageName(), Context.MODE_PRIVATE);
		Editor editor = pref.edit();

		editor.putBoolean(KEY_DISABLE_IN_LANDSCAPE, isDisableInLandscape);
		editor.commit();
	}
	// ===========================================================
	// Inner and Anonymous Classes
	// ===========================================================
}
