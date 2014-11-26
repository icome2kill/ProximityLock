package com.nguyennk.proximitylock.activities;

import android.app.Activity;
import android.content.Intent;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.support.v4.app.NotificationCompat;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.TextView;

import com.nguyennk.proximitylock.R;
import com.nguyennk.proximitylock.helpers.NotificationHelper;
import com.nguyennk.proximitylock.helpers.PreferencesHelper;
import com.nguyennk.proximitylock.services.ProximityService;

public class CalibrationActivity extends Activity implements
		SensorEventListener {
	// ===========================================================
	// Constants
	// ===========================================================
	private static final int MAX_TIME_WAVED = 5;
	private static final String SENSOR_DELAY_LABELS[] = { "Slow", "Normal",
			"Fast", "Fastest" };

	// ===========================================================
	// Fields
	// ===========================================================
	private TextView tvSensorStatus;
	private TextView tvWaveTime;
	private TextView tvWaveMinTime;
	private TextView tvWaveMaxTime;
	private TextView tvProgress;

	private TextView tvSensorDelayValue;
	private SeekBar sbSensorDelay;

	private Button btnReset;
	private Button btnSave;

	private boolean isServiceRunning;

	private SensorManager sensorManager;
	private Sensor sensor;

	private long timeCovered;
	private long[] samples;
	private int timeWaved;

	private long currentMinGestureLength = 0;
	private long currentMaxGestureLength = 0;

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
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setContentView(R.layout.activity_calibration);

		tvSensorStatus = (TextView) findViewById(R.id.tv_sensor_status);
		tvWaveTime = (TextView) findViewById(R.id.tv_wave_time);
		tvWaveMinTime = (TextView) findViewById(R.id.tv_wave_min_time);
		tvWaveMaxTime = (TextView) findViewById(R.id.tv_wave_max_time);
		tvProgress = (TextView) findViewById(R.id.tv_calibration_progress);
		btnReset = (Button) findViewById(R.id.btn_reset);
		btnReset.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				samples = new long[MAX_TIME_WAVED];
				timeWaved = 0;
				timeCovered = 0;

				resetToDefault();
				refreshTexts();

				btnSave.setEnabled(false);
			}
		});

		btnSave = (Button) findViewById(R.id.btn_save);
		btnSave.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View arg0) {
				saveResult();
				btnSave.setEnabled(false);
			}
		});

		sbSensorDelay = (SeekBar) findViewById(R.id.sb_sensor_delay);
		sbSensorDelay.setProgress(3 - PreferencesHelper.getSensorDelayValue(this));
		sbSensorDelay.setMax(3);
		sbSensorDelay.setOnSeekBarChangeListener(new OnSeekBarChangeListener() {
			@Override
			public void onStopTrackingTouch(SeekBar seekBar) {
			}

			@Override
			public void onStartTrackingTouch(SeekBar seekBar) {
			}

			@Override
			public void onProgressChanged(SeekBar seekBar, int progress,
					boolean isFromUser) {
				refreshSaveButton();
				tvSensorDelayValue.setText(SENSOR_DELAY_LABELS[progress]);

				sensorManager.unregisterListener(CalibrationActivity.this);
				sensorManager.registerListener(CalibrationActivity.this,
						sensor, 3 - progress);
			}
		});

		tvSensorDelayValue = (TextView) findViewById(R.id.tv_sensor_delay_value);

		isServiceRunning = PreferencesHelper.isServiceRunning(this,
				ProximityService.class);

		sensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
		sensor = sensorManager.getDefaultSensor(Sensor.TYPE_PROXIMITY);

		samples = new long[MAX_TIME_WAVED];
		timeWaved = 0;

		refreshTexts();
		refreshSaveButton();
	}

	@Override
	protected void onResume() {
		super.onResume();

		if (isServiceRunning) {
			stopService(new Intent(this, ProximityService.class));
		}
		sensorManager.registerListener(this, sensor,
				PreferencesHelper.getSensorDelayValue(this));
	}

	@Override
	protected void onPause() {
		super.onPause();

		if (isServiceRunning) {
			NotificationCompat.Builder builder = NotificationHelper
					.getNotificationBuilder(this).setContentText(
							getResources().getString(
									R.string.text_running_foreground));
			Intent service = new Intent(this, ProximityService.class);

			service.putExtra(ProximityService.KEY_RUN_FOREGROUND,
					PreferencesHelper.isForegroundModeOn(this));
			service.putExtra(ProximityService.KEY_NOTIFICATION, builder.build());
			startService(service);
			NotificationHelper.refreshNotification(this);
		}
		sensorManager.unregisterListener(this, sensor);
	}

	@Override
	public void onAccuracyChanged(Sensor sensor, int accuracy) {

	}

	@Override
	public void onSensorChanged(SensorEvent event) {
		determineSensorStatus(event);
		determineSensorTime(event);
	}

	// ===========================================================
	// Methods
	// ===========================================================
	private void refreshTexts() {
		currentMinGestureLength = PreferencesHelper.getSavedMinWaveGestureLength(this);
		currentMaxGestureLength = PreferencesHelper.getSavedMaxWaveGestureLength(this);
		tvWaveMinTime.setText(currentMinGestureLength / (1000 * 1000) + "ms");
		tvWaveMaxTime.setText(currentMaxGestureLength / (1000 * 1000) + "ms");
		
		tvSensorDelayValue.setText(SENSOR_DELAY_LABELS[sbSensorDelay.getProgress()]);
		tvProgress.setText(timeWaved + "/" + MAX_TIME_WAVED);
	}

	private void refreshSaveButton() {
		if (currentMinGestureLength != PreferencesHelper
				.getSavedMinWaveGestureLength(this)
				|| currentMaxGestureLength != PreferencesHelper
						.getSavedMaxWaveGestureLength(this)
				|| (3 - sbSensorDelay.getProgress()) != PreferencesHelper
						.getSensorDelayValue(this)) {
			btnSave.setEnabled(true);
		} else {
			btnSave.setEnabled(false);
		}
	}

	private void determineSensorStatus(SensorEvent event) {
		if (event.values[0] == 0) {
			tvSensorStatus.setText(getResources().getString(
					R.string.sensor_status_near));
		} else {
			tvSensorStatus.setText(getResources().getString(
					R.string.sensor_status_far));
		}
	}

	private void determineSensorTime(SensorEvent event) {
		if (event.values[0] == 0) {
			timeCovered = event.timestamp;
		} else {
			if (timeCovered == 0) {
				return;
			}
			timeCovered = event.timestamp - timeCovered;

			tvWaveTime.setText((timeCovered / (1000 * 1000)) + " ms");

			if (timeWaved < MAX_TIME_WAVED) {
				samples[timeWaved++] = timeCovered;
				tvProgress.setText(timeWaved + "/" + MAX_TIME_WAVED);
			}
			if (timeWaved == MAX_TIME_WAVED) {
				determineCalibrationResults();
				refreshSaveButton();
			}
		}
	}

	private void determineCalibrationResults() {
		long total = 0;
		for (int i = 0; i < samples.length; i++) {
			total += samples[i];
		}
		long average = total / samples.length;

		long minTime = (average / (2 * 50)) * 50;
		long maxTime = minTime * 4;

		currentMinGestureLength = minTime;
		currentMaxGestureLength = maxTime;

		tvWaveMinTime.setText((minTime / (1000 * 1000)) + "ms");
		tvWaveMaxTime.setText((maxTime / (1000 * 1000)) + "ms");
	}

	private void resetToDefault() {
		PreferencesHelper.saveMinWaveGestureLength(this,
				ProximityService.DEFAULT_MIN_WAVE_GESTURE_LENGTH);
		PreferencesHelper.saveMaxWaveGestureLength(this,
				ProximityService.DEFAULT_MAX_WAVE_GESTURE_LENGTH);
		PreferencesHelper
				.saveSensorDelayValue(this, SensorManager.SENSOR_DELAY_FASTEST);
	}

	private void saveResult() {
		PreferencesHelper.saveMinWaveGestureLength(this, currentMinGestureLength);
		PreferencesHelper.saveMaxWaveGestureLength(this, currentMaxGestureLength);
		PreferencesHelper.saveSensorDelayValue(this, 3 - sbSensorDelay.getProgress());
	}
	// ===========================================================
	// Inner and Anonymous Classes
	// ===========================================================
}
