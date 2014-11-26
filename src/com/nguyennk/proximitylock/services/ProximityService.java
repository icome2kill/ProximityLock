package com.nguyennk.proximitylock.services;

import android.app.Notification;
import android.app.Service;
import android.app.admin.DevicePolicyManager;
import android.content.ComponentName;
import android.content.Intent;
import android.content.res.Configuration;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Handler;
import android.os.IBinder;
import android.os.PowerManager;
import android.os.PowerManager.WakeLock;
import android.util.Log;

import com.nguyennk.proximitylock.activities.MainActivity;
import com.nguyennk.proximitylock.helpers.NotificationHelper;
import com.nguyennk.proximitylock.helpers.PreferencesHelper;
import com.nguyennk.proximitylock.receivers.ProximityLockAdminReceiver;

public class ProximityService extends Service implements SensorEventListener {
	// ===========================================================
	// Constants
	// ===========================================================
	public static final String KEY_RUN_FOREGROUND = "key_run_foreground";
	public static final String KEY_NOTIFICATION = "key_notification";
	public static final String ACTION_STOP = "STOP";
	public static final String ACTION_START = "START";

	public static final long DEFAULT_MIN_WAVE_GESTURE_LENGTH = 50 * 1000 * 1000; // 0.05
																					// seconds
	public static final long DEFAULT_MAX_WAVE_GESTURE_LENGTH = 500 * 1000 * 1000; // 0.5
																					// seconds
	public static final long DEFAULT_POCKET_DETECTION_TIME = 1500;
	// ===========================================================
	// Fields
	// ===========================================================
	private Handler handler;
	private Runnable runnable;

	private PowerManager powerManager;
	private WakeLock wakeLock;

	private DevicePolicyManager deviceManager;
	private SensorManager sensorManager;
	private Sensor sensor;

	private long timeCovered;
	private long savedMinWaveGestureLength;
	private long savedMaxWaveGestureLength;

	// ===========================================================
	// Constructors
	// ===========================================================

	// ===========================================================
	// Getter & Setter
	// ===========================================================

	// ===========================================================
	// Methods for/from SuperClass/Interfaces
	// ===========================================================
	@Override
	public IBinder onBind(Intent intent) {
		return null;
	}

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		savedMinWaveGestureLength = PreferencesHelper
				.getSavedMinWaveGestureLength(this) == 0 ? DEFAULT_MIN_WAVE_GESTURE_LENGTH
				: PreferencesHelper.getSavedMinWaveGestureLength(this);
		savedMaxWaveGestureLength = PreferencesHelper
				.getSavedMaxWaveGestureLength(this) == 0 ? DEFAULT_MAX_WAVE_GESTURE_LENGTH
				: PreferencesHelper.getSavedMaxWaveGestureLength(this);

		sensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
		sensor = sensorManager.getDefaultSensor(Sensor.TYPE_PROXIMITY);

		sensorManager.registerListener(this, sensor,
				PreferencesHelper.getSensorDelayValue(this));
		deviceManager = (DevicePolicyManager) getSystemService(DEVICE_POLICY_SERVICE);
		powerManager = (PowerManager) getSystemService(POWER_SERVICE);

		wakeLock = powerManager.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK,
				"MyWakelockTag");
		wakeLock.acquire();

		boolean runInForeground = intent.getBooleanExtra(KEY_RUN_FOREGROUND,
				false);
		if (runInForeground) {
			Log.d("ProxmityService", "Should run in foreground");
			Notification notification = intent
					.getParcelableExtra(KEY_NOTIFICATION);
			startForeground(MainActivity.NOTIFICATION_ID, notification);
		}

		handler = new Handler();
		runnable = new Runnable() {
			@Override
			public void run() {
				lock();
			}
		};

		if (intent.getAction() != null
				&& intent.getAction().equalsIgnoreCase(ACTION_STOP)) {
			stopSelf();
		}

		NotificationHelper.refreshNotification(this);

		detectLandscapeMode(getResources().getConfiguration().orientation);

		return Service.START_STICKY;
	}

	@Override
	public void onDestroy() {
		Log.d("ProximityService", "OnDestroy called");
		sensorManager.unregisterListener(this);
		wakeLock.release();
		stopForeground(false);

		handler.removeCallbacks(runnable);

		NotificationHelper.refreshNotification(this);
		super.onDestroy();
	}

	@Override
	public void onAccuracyChanged(Sensor sensor, int accuracy) {
	}

	@Override
	public void onSensorChanged(SensorEvent event) {
		detectWaveGesture(event);
		if (PreferencesHelper.isPocketDetectionOn(this)) {
			detectInPocket(event);
		}
	}

	@Override
	public void onConfigurationChanged(Configuration newConfig) {
		super.onConfigurationChanged(newConfig);

		detectLandscapeMode(newConfig.orientation);
	}

	// ===========================================================
	// Methods
	// ===========================================================
	private void lock() {
		if (deviceManager.isAdminActive(new ComponentName(
				getApplicationContext(), ProximityLockAdminReceiver.class))) {
			deviceManager.lockNow();
		}
	}
	@SuppressWarnings("deprecation")
	private void detectWaveGesture(SensorEvent event) {
		if (event.values[0] == 0 || event.values[0] < sensor.getMaximumRange()) {
			timeCovered = event.timestamp;
		} else {
			long coveredLength = event.timestamp - timeCovered;
			if (coveredLength >= savedMinWaveGestureLength
					&& coveredLength <= savedMaxWaveGestureLength) {
				// Actual Wave
				if (powerManager.isScreenOn()) {
					lock();
				} else {
					wakeUp();
				}
			}
		}
	}

	private void detectInPocket(SensorEvent event) {
		if (event.values[0] == 0 || event.values[0] < sensor.getMaximumRange()) {
			timeCovered = event.timestamp;

			handler.postDelayed(runnable, DEFAULT_POCKET_DETECTION_TIME);
		} else {
			handler.removeCallbacks(runnable);

			if (event.timestamp - timeCovered > DEFAULT_POCKET_DETECTION_TIME * 1000 * 1000) {
				wakeUp();
			}
		}
	}

	@SuppressWarnings("deprecation")
	private void wakeUp() {
		WakeLock wakeLock = powerManager.newWakeLock(
				PowerManager.FULL_WAKE_LOCK
						| PowerManager.ACQUIRE_CAUSES_WAKEUP
						| PowerManager.ON_AFTER_RELEASE, "MyWakeLock");
		wakeLock.acquire();
		wakeLock.release();
	}

	private void detectLandscapeMode(int orientation) {
		if (sensorManager == null || sensor == null) {
			return;
		}
		if (orientation == Configuration.ORIENTATION_LANDSCAPE) {
			sensorManager.unregisterListener(this, sensor);
		} else {
			sensorManager.registerListener(this, sensor,
					PreferencesHelper.getSensorDelayValue(this));
		}
	}
	// ===========================================================
	// Inner and Anonymous Classes
	// ===========================================================
}
