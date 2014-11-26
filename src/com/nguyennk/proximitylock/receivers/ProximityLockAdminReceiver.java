package com.nguyennk.proximitylock.receivers;

import android.app.admin.DeviceAdminReceiver;
import android.content.Context;
import android.content.Intent;
import android.widget.Toast;

import com.nguyennk.proximitylock.R;

public class ProximityLockAdminReceiver extends DeviceAdminReceiver {
	void showToast(Context context, CharSequence msg) {
		Toast.makeText(context, msg, Toast.LENGTH_SHORT).show();
	}

	@Override
	public void onEnabled(Context context, Intent intent) {
		showToast(context, context.getResources().getString(R.string.text_admin_enabled));
	}

	@Override
	public CharSequence onDisableRequested(Context context, Intent intent) {
		return context.getResources().getString(R.string.text_admin_request_disable);
	}

	@Override
	public void onDisabled(Context context, Intent intent) {
		showToast(context, context.getResources().getString(R.string.text_admin_disabled));
	}
}
