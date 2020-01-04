package com.example.pavan.callrecorderdemo.Services;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.telephony.PhoneStateListener;
import android.telephony.TelephonyManager;

public class Receiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {

//        Log.i("REGISTER","RECEIVER REGISTERED");
        TelephonyManager telephonyManager= (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
        telephonyManager.listen(new myPhoneStateListener(context), PhoneStateListener.LISTEN_CALL_STATE);
    }

}
