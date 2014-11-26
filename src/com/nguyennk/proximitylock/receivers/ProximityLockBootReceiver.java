package com.nguyennk.proximitylock.receivers;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.support.v4.app.NotificationCompat;

import com.nguyennk.proximitylock.helpers.NotificationHelper;
import com.nguyennk.proximitylock.helpers.PreferencesHelper;
import com.nguyennk.proximitylock.services.ProximityService;

public class ProximityLockBootReceiver extends BroadcastReceiver {
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
	@Override
	public void onReceive(Context context, Intent intent) {
		if (Intent.ACTION_BOOT_COMPLETED.equalsIgnoreCase(intent.getAction())) {
			if (!PreferencesHelper.isStartOnBootOn(context)) {
				return;
			}
			boolean shouldRunInForeground = PreferencesHelper
					.isForegroundModeOn(context);

			Intent service = new Intent(context, ProximityService.class);

			NotificationCompat.Builder notificationBuilder = NotificationHelper.getNotificationBuilder(context)
					.setContentText("Proximity lock is running in foreground");

			if (shouldRunInForeground) {
				service.putExtra(ProximityService.KEY_NOTIFICATION,
						notificationBuilder.build());
				service.putExtra(ProximityService.KEY_RUN_FOREGROUND,
						shouldRunInForeground);
			}

			context.startService(service);

			NotificationHelper.refreshNotification(context);
		}
	}
	// ===========================================================
	// Methods
	// ===========================================================

	// ===========================================================
	// Inner and Anonymous Classes
	// ===========================================================
}
