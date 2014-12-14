package com.nguyennk.proximitylock.helpers;

import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.NotificationManagerCompat;

import com.nguyennk.proximitylock.R;
import com.nguyennk.proximitylock.activities.MainActivity;
import com.nguyennk.proximitylock.services.ProximityService;

public class NotificationHelper {
	// ===========================================================
	// Constants
	// ===========================================================

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
	public static NotificationCompat.Builder getNotificationBuilder(
			Context context) {
		Intent notificationIntent = new Intent(context, MainActivity.class);
		notificationIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP
				| Intent.FLAG_ACTIVITY_SINGLE_TOP);
		PendingIntent pendingIntent = PendingIntent.getActivity(context, 0,
				notificationIntent, 0);

		return new NotificationCompat.Builder(context)
				.setSmallIcon(R.drawable.ic_launcher)
				.setAutoCancel(false)
				.setPriority(NotificationCompat.PRIORITY_HIGH)
				.setContentIntent(pendingIntent)
				.setOngoing(true)
				.setContentTitle(
						context.getResources().getString(R.string.app_name));
	}

	public static void refreshNotification(Context context) {
		if (PreferencesHelper.isNotificationOn(context)) {
			CharSequence msg = context.getResources().getString(
					R.string.text_not_running);
			int logoResId = NOT_RUNNING_LOGO_ID;
			CharSequence actionLabel = context.getResources().getString(
					R.string.btn_toggle_start_label_start);

			String action = ProximityService.ACTION_START;

			if (PreferencesHelper.isServiceRunning(context,
					ProximityService.class)) {
				msg = context.getResources().getString(R.string.text_running);
				logoResId = RUNNING_LOGO_ID;

				actionLabel = context.getResources().getString(
						R.string.btn_toggle_start_label_stop);
				action = ProximityService.ACTION_STOP;

				if (PreferencesHelper.isServiceRunningForeground(context,
						ProximityService.class)) {
					msg = context.getResources().getString(
							R.string.text_running_foreground);
					logoResId = RUNNING_LOGO_ID;
				}
			}

			Intent intent = new Intent(context, ProximityService.class);
			intent.setAction(action);

			if (PreferencesHelper.isForegroundModeOn(context)) {
				intent.putExtra(ProximityService.KEY_RUN_FOREGROUND, true);
				intent.putExtra(ProximityService.KEY_NOTIFICATION, getNotificationBuilder(context).build());
				
				if (action.equalsIgnoreCase(ProximityService.ACTION_STOP)) {
					msg = context.getResources().getString(
							R.string.text_running_foreground);
					logoResId = RUNNING_LOGO_ID;
				}
			}

			PendingIntent pendingIntent = PendingIntent.getService(context, 0,
					intent, PendingIntent.FLAG_UPDATE_CURRENT);

			NotificationCompat.Builder notificationBuilder = getNotificationBuilder(context)
					.setStyle(new NotificationCompat.BigTextStyle().bigText(msg))
					.setContentText(msg)
					.setSmallIcon(logoResId)
					.addAction(R.drawable.ic_launcher, actionLabel, pendingIntent);

			NotificationManagerCompat notificationManager = NotificationManagerCompat
					.from(context);
			notificationManager.notify(MainActivity.NOTIFICATION_ID,
					notificationBuilder.build());
		}
	}
	// ===========================================================
	// Inner and Anonymous Classes
	// ===========================================================
}
