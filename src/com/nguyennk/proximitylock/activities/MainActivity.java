package com.nguyennk.proximitylock.activities;

import android.app.Activity;
import android.app.admin.DevicePolicyManager;
import android.content.ComponentName;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.NotificationManagerCompat;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.Toast;

import com.nguyennk.proximitylock.R;
import com.nguyennk.proximitylock.helpers.NotificationHelper;
import com.nguyennk.proximitylock.helpers.PreferencesHelper;
import com.nguyennk.proximitylock.receivers.ProximityLockAdminReceiver;
import com.nguyennk.proximitylock.services.ProximityService;

public class MainActivity extends Activity {
	private static final int RESULT_CODE_REQUEST_ADMIN = 100;

	public static final int NOTIFICATION_ID = 8963;

	private Button btnToggleStart;
	private Button btnCalibration;

	private CheckBox cbNotificationToggle;
	private CheckBox cbForegroundToggle;
	private CheckBox cbStartOnBoot;
	private CheckBox cbPocketDetection;
	private CheckBox cbDisableInLandscape;

	private NotificationCompat.Builder notificationBuilder;
	private NotificationManagerCompat notificationManager;

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		final Intent intent = new Intent(MainActivity.this,
				ProximityService.class);

		btnToggleStart = (Button) findViewById(R.id.btn_toggle_start);
		btnToggleStart.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				// Check to see the status of the service.
				if (PreferencesHelper.isServiceRunning(MainActivity.this,
						ProximityService.class)) {
					stopService(intent);
					if (PreferencesHelper.isNotificationOn(MainActivity.this)) {
						refreshNotification();
					}
				} else {
					requestAdmin();
					if (isAdminActivated()) {
						intent.putExtra(ProximityService.KEY_RUN_FOREGROUND,
								false);
						if (cbForegroundToggle.isChecked()) {
							startServiceForeground(intent);
						} else {
							startService(intent);
							if (PreferencesHelper
									.isNotificationOn(MainActivity.this)) {
								refreshNotification();
							}
						}
					} else {
						Toast.makeText(MainActivity.this, "Admin is required",
								Toast.LENGTH_SHORT).show();
					}
				}

				runOnUiThread(new Runnable() {
					@Override
					public void run() {
						determineServiceState();
					}
				});
			}
		});

		btnCalibration = (Button) findViewById(R.id.btn_calibration);
		btnCalibration.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				Intent intent = new Intent(MainActivity.this,
						CalibrationActivity.class);
				startActivity(intent);
			}
		});

		notificationBuilder = NotificationHelper.getNotificationBuilder(this);
		notificationManager = NotificationManagerCompat.from(this);

		cbNotificationToggle = (CheckBox) findViewById(R.id.cb_notification_toggle);
		cbNotificationToggle.setChecked(PreferencesHelper
				.isNotificationOn(this));
		cbNotificationToggle
				.setOnCheckedChangeListener(new OnCheckedChangeListener() {
					@Override
					public void onCheckedChanged(CompoundButton button,
							boolean isChecked) {
						PreferencesHelper.saveNotificationState(
								MainActivity.this, isChecked);

						if (isChecked) {
							refreshNotification();
						} else {
							notificationManager.cancel(NOTIFICATION_ID);
						}
					}
				});

		cbForegroundToggle = (CheckBox) findViewById(R.id.cb_foreground_toggle);
		if (PreferencesHelper.isServiceRunningForeground(this,
				ProximityService.class)
				|| PreferencesHelper.isForegroundModeOn(this)) {
			cbForegroundToggle.setChecked(true);
			cbNotificationToggle.setEnabled(false);
		} else {
			cbForegroundToggle.setChecked(false);
			cbNotificationToggle.setEnabled(true);
		}
		cbForegroundToggle
				.setOnCheckedChangeListener(new OnCheckedChangeListener() {
					@Override
					public void onCheckedChanged(CompoundButton button,
							boolean isChecked) {
						if (isChecked) {
							cbNotificationToggle.setChecked(true);
							cbNotificationToggle.setEnabled(false);
						} else {
							cbNotificationToggle.setChecked(PreferencesHelper
									.isNotificationOn(MainActivity.this));
							cbNotificationToggle.setEnabled(true);
						}

						PreferencesHelper.saveForegroundState(
								MainActivity.this, isChecked);

						if (PreferencesHelper.isServiceRunning(
								MainActivity.this, ProximityService.class)) {
							stopService(intent);
							if (isChecked) {
								startServiceForeground(intent);
							} else {
								intent.putExtra(
										ProximityService.KEY_RUN_FOREGROUND,
										false);
								startService(intent);
							}
						}

						if (PreferencesHelper
								.isNotificationOn(MainActivity.this)) {
							refreshNotification();
						}
					}
				});

		cbStartOnBoot = (CheckBox) findViewById(R.id.cb_start_on_boot);
		cbStartOnBoot.setChecked(PreferencesHelper.isStartOnBootOn(this));
		cbStartOnBoot.setOnCheckedChangeListener(new OnCheckedChangeListener() {

			@Override
			public void onCheckedChanged(CompoundButton arg0, boolean isChecked) {
				PreferencesHelper.saveStartOnBootState(MainActivity.this,
						isChecked);
			}
		});

		cbPocketDetection = (CheckBox) findViewById(R.id.cb_pocket_detection);
		cbPocketDetection.setChecked(PreferencesHelper
				.isPocketDetectionOn(this));
		cbPocketDetection
				.setOnCheckedChangeListener(new OnCheckedChangeListener() {
					@Override
					public void onCheckedChanged(CompoundButton button,
							boolean isChecked) {
						PreferencesHelper.savePocketDetection(
								MainActivity.this, isChecked);
					}
				});

		cbDisableInLandscape = (CheckBox) findViewById(R.id.cb_disable_in_landscape);
		cbDisableInLandscape.setChecked(PreferencesHelper
				.isDisabledInLandscape(this));
		cbDisableInLandscape
				.setOnCheckedChangeListener(new OnCheckedChangeListener() {

					@Override
					public void onCheckedChanged(CompoundButton buttonView,
							boolean isChecked) {
						PreferencesHelper.saveDisableInLandscapeSetting(
								MainActivity.this, isChecked);
					}
				});
	}

	@Override
	protected void onResume() {
		super.onResume();

		determineServiceState();
	}

	private void startServiceForeground(Intent intent) {
		intent.putExtra(ProximityService.KEY_RUN_FOREGROUND, true);
		notificationBuilder.setContentText(getResources().getString(
				R.string.text_running_foreground));
		intent.putExtra(ProximityService.KEY_NOTIFICATION,
				notificationBuilder.build());

		startService(intent);
	}

	private void refreshNotification() {
		NotificationHelper.refreshNotification(this);
	}

	private void determineServiceState() {
		String btnText = getResources()
				.getString(
						PreferencesHelper.isServiceRunning(this,
								ProximityService.class) ? R.string.btn_toggle_start_label_stop
								: R.string.btn_toggle_start_label_start);

		btnToggleStart.setText(btnText);
	}

	private void requestAdmin() {
		DevicePolicyManager deviceManager = (DevicePolicyManager) getSystemService(DEVICE_POLICY_SERVICE);
		ComponentName compName = new ComponentName(getApplicationContext(),
				ProximityLockAdminReceiver.class);

		if (!deviceManager.isAdminActive(compName)) {
			Intent adminRequestIntent = new Intent(
					DevicePolicyManager.ACTION_ADD_DEVICE_ADMIN);
			adminRequestIntent.putExtra(DevicePolicyManager.EXTRA_DEVICE_ADMIN,
					compName);
			adminRequestIntent.putExtra(
					DevicePolicyManager.EXTRA_ADD_EXPLANATION, getResources()
							.getString(R.string.text_admin_request_reason));

			startActivityForResult(adminRequestIntent,
					RESULT_CODE_REQUEST_ADMIN);
		}
	}

	private boolean isAdminActivated() {
		DevicePolicyManager deviceManager = (DevicePolicyManager) getSystemService(DEVICE_POLICY_SERVICE);
		ComponentName compName = new ComponentName(getApplicationContext(),
				ProximityLockAdminReceiver.class);

		return deviceManager.isAdminActive(compName);
	}

}
