package com.ozm.rocks.ui.screen.pushwoosh;

import android.content.Context;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.os.Bundle;

import com.arellomobile.android.push.PushHandlerActivity;
import com.arellomobile.android.push.PushManager;
import com.arellomobile.android.push.utils.GeneralUtils;
import com.arellomobile.android.push.utils.Log;
import com.arellomobile.android.push.utils.PreferenceUtils;
import com.arellomobile.android.push.utils.notification.AbsNotificationFactory;
import com.arellomobile.android.push.utils.notification.DefaultNotificationFactory;
import com.arellomobile.android.push.utils.notification.PushData;

import timber.log.Timber;

public class PushwooshIntentService extends com.arellomobile.android.push.PushGCMIntentService {

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        super.onHandleIntent(intent);
        Timber.d("OzomePushWoosh: catch %s intent service", intent == null ? "empty" : "not empty");
        if (intent != null) {
            final Bundle extras = intent.getExtras();
            final String userdata = extras.getString("u");
            final String url = extras.getString("l");
            Timber.d("OzomePushWoosh: url:%s", url);
            Timber.d("OzomePushWoosh: userdata:%s", userdata);
            Timber.d("OzomePushWoosh: catch %s extras service", extras == null ? "empty" : "not empty");
        }
    }

    @Override
    protected void onMessage(Context context, Intent intent) {
        Log.info("GCMIntentService", "Received message");
        generateNotification(context, intent);
    }

    /**
     * Method was to copied from PushServiceHelper.generateNotification(Context var0, Intent var1);
     */
    private void generateNotification(Context var0, Intent var1) {
        Bundle var2 = var1.getExtras();
        if(var2 != null) {
            PushData var3 = new PushData(var2);
            if(var3.isContainPushwooshKey()) {
                var3.setAppOnForeground(GeneralUtils.isAppOnForeground(var0));
                var3.setVibrateType(PreferenceUtils.getVibrateType(var0));
                var3.setSoundType(PreferenceUtils.getSoundType(var0));

                boolean var4;
                Intent var5;
                try {
                    var4 = true;
                    ApplicationInfo var7 = var0.getPackageManager().getApplicationInfo(var0.getPackageName(), 128);
                    String var8 = var7.metaData.getString("PW_NOTIFICATION_RECEIVER");
                    Class var6 = Class.forName(var8);
                    var5 = new Intent(var0, var6);
                } catch (Exception var9) {
                    var4 = false;
                    var5 = new Intent(var0, PushHandlerActivity.class);
                    var5.addFlags(603979776);
                }

                var3.setUseIntentReceiver(var4);
                var5.putExtra("pushBundle", var2);
                AbsNotificationFactory var10 = getNotificationFactory(var0);
                var10.notify(var0, var2, var3, var5);
            }
        }
    }

    /**
     * Method was to copied from PushServiceHelper.getNotificationFactory(Context var0);
     */
    private static AbsNotificationFactory getNotificationFactory(Context var0) {
        AbsNotificationFactory var1 = PushManager.getInstance(var0).getNotificationFactory();
        return (AbsNotificationFactory)(var1 != null?var1:new DefaultNotificationFactory());
    }

}
