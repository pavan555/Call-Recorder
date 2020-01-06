package com.example.pavan.callrecorderdemo.Services;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.telephony.PhoneStateListener;
import android.telephony.TelephonyManager;

import static android.content.Context.MODE_PRIVATE;


public class Receiver extends BroadcastReceiver {

    public  static SQLiteDatabase sqLiteDatabase;

    @Override
    public void onReceive(Context context, Intent intent) {

//        Log.i("REGISTER","RECEIVER REGISTERED");
        sqLiteDatabase=context.openOrCreateDatabase("records",MODE_PRIVATE,null);
        TelephonyManager telephonyManager= (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
        telephonyManager.listen(new myPhoneStateListener(context), PhoneStateListener.LISTEN_CALL_STATE);
    }

}
