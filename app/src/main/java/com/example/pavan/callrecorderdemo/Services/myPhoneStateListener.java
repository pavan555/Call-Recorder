package com.example.pavan.callrecorderdemo.Services;

import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.telephony.PhoneStateListener;
import android.telephony.TelephonyManager;
import android.util.Log;

class myPhoneStateListener extends PhoneStateListener {

    public  boolean incoming=false;

    private int lastState = TelephonyManager.CALL_STATE_IDLE;

    private Context context;

    myPhoneStateListener(Context context) {
        super();
        this.context=context;
    }


    @Override
    public void onCallStateChanged(int state, String phoneNumber) {
        super.onCallStateChanged(state, phoneNumber);

        //Incoming call-  goes from IDLE to RINGING when it rings, to OFFHOOK when it's answered, to IDLE when its hung up
        //Outgoing call-  goes from IDLE to OFFHOOK when it dials out, to IDLE when hung up

        if (lastState == state) {
            //no change
            return;
        } else {

            Intent intent;

            switch (state) {

                case TelephonyManager.CALL_STATE_RINGING:
                    incoming = true;
                    intent = new Intent(context, CallService.class);
                    intent.putExtra("phoneNumber", phoneNumber);
                    context.startService(intent);
                    break;

                case TelephonyManager.CALL_STATE_OFFHOOK:
                    //Transition of ringing->offhook are pickups of incoming calls.  Nothing done on them
                    if (lastState != TelephonyManager.CALL_STATE_RINGING) {

                        incoming = false;

                        intent = new Intent(context, CallService.class);
                        intent.putExtra("phoneNumber", phoneNumber);

                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                            context.startForegroundService(intent);
                        }else{
                            context.startService(intent);
                        }
//                        Log.i("Number Outgoing", " " + phoneNumber + "yeah");


                    } else {

                        incoming = true;
//                        Log.i("Number INC", phoneNumber);


                    }

                    break;

                case TelephonyManager.CALL_STATE_IDLE:
                    //Went to idle-  this is the end of a call.  What type depends on previous state(s)
                    if (lastState == TelephonyManager.CALL_STATE_RINGING) {
                        //RINGED NOT PICKED -Missed call
                        Log.i("Missed call ", phoneNumber);

                    } else if (incoming) {
                        //incoming call ended
                        Log.i("incoming HANGED", phoneNumber + " hanged");

                    } else {
                        //outgoing call ended
                        Log.i("outgoing HANGED", phoneNumber + " hanged");

                    }
                    intent = new Intent(context, CallService.class);
                    context.stopService(intent);
                    break;


            }
            lastState = state;

        }
    }

}
