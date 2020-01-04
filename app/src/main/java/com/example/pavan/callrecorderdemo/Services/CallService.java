package com.example.pavan.callrecorderdemo.Services;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.database.sqlite.SQLiteStatement;
import android.graphics.Color;
import android.graphics.Rect;
import android.os.Build;
import android.os.IBinder;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;

import com.example.pavan.callrecorderdemo.MainActivity;
import com.example.pavan.callrecorderdemo.R;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import jp.co.recruit_lifestyle.android.floatingview.FloatingViewListener;
import jp.co.recruit_lifestyle.android.floatingview.FloatingViewManager;


public class CallService extends Service implements FloatingViewListener {



    private static final String TAG = "Call Service";
    public static final String EXTRA_CUTOUT_SAFE_AREA = "cutout_safe_area";
    private static final int NOTIFICATION_ID = 9083150;
    private final String PATH="Call@";

    private FloatingViewManager floatingViewManager;

    SimpleDateFormat simpleDateFormat=new SimpleDateFormat("YYYY-MM-dd$HH:mm", Locale.getDefault());
    AudioRecorder audioRecorder;

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }


    private boolean pause=false;
    SQLiteStatement sqLiteStatement=MainActivity.sqLiteDatabase.compileStatement("INSERT INTO RECORDS(fileName,time,phoneNumber) VALUES(?,?,?)");

    @Override
    public int onStartCommand(final Intent intent, int flags, int startId) {
        super.onCreate();

        if(floatingViewManager != null){
            return START_STICKY;
        }


        DisplayMetrics displayMetrics=new DisplayMetrics();

        final WindowManager windowManager = (WindowManager) getSystemService(Context.WINDOW_SERVICE);

        LayoutInflater inflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        windowManager.getDefaultDisplay().getMetrics(displayMetrics);
        ImageView imageView= (ImageView) inflater.inflate(R.layout.widget_head,null,false);

        imageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if(pause && audioRecorder!=null){
                    audioRecorder.pause();
                    Toast.makeText(CallService.this,"paused recording",Toast.LENGTH_SHORT).show();
                    pause=false;
                }else if(audioRecorder == null && !pause){
                    String phone= "",finalPath=PATH,date="";
                    phone = intent.getStringExtra("phoneNumber");


                    if(phone.length() !=0){
                        finalPath+="$"+phone;
                        sqLiteStatement.bindString(3,phone);
                    }
                    date=simpleDateFormat.format(new Date());
                    finalPath+="$"+date;
                    sqLiteStatement.bindString(1,finalPath);
                    sqLiteStatement.bindString(2,date);

                    try {
                        audioRecorder=new AudioRecorder(finalPath);
                        audioRecorder.start();
                        sqLiteStatement.execute();
                        MainActivity.checkRecords();
                        Toast.makeText(CallService.this,"recording",Toast.LENGTH_SHORT).show();

                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    pause=true;

                }else {
                    Toast.makeText(CallService.this,"resumed recording",Toast.LENGTH_SHORT).show();
                    audioRecorder.resume();
                    pause=true;
                }
            }
        });


        floatingViewManager = new FloatingViewManager(this,this);
        floatingViewManager.setActionTrashIconImage(R.drawable.ic_trash_action);
        floatingViewManager.setFixedTrashIconImage(R.drawable.ic_trash_fixed);
        floatingViewManager.setSafeInsetRect((Rect) intent.getParcelableExtra(EXTRA_CUTOUT_SAFE_AREA));

        FloatingViewManager.Options options=new FloatingViewManager.Options();
        options.overMargin = (int) (16* displayMetrics.density);

        floatingViewManager.addViewToWindow(imageView,options);

        startForeground(NOTIFICATION_ID,createNotification(this));

        return START_REDELIVER_INTENT;




        //
//        int flag = WindowManager.LayoutParams.TYPE_PHONE;
//        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
//            flag = WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY;
//        }
//
//
//
//        layoutParams = new WindowManager.LayoutParams(
//                ViewGroup.LayoutParams.MATCH_PARENT,
//                ViewGroup.LayoutParams.MATCH_PARENT,
//                flag,
//                WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
//                PixelFormat.TRANSLUCENT
//        );
//
//
//        layoutParams.height = WindowManager.LayoutParams.WRAP_CONTENT;
//        layoutParams.width = WindowManager.LayoutParams.WRAP_CONTENT;
//        layoutParams.gravity = Gravity.RIGHT | Gravity.CENTER;
//
//
////        final WindowManager windowManager = (WindowManager) getSystemService(Context.WINDOW_SERVICE);
////
////        LayoutInflater inflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
//        try {
//
//
//            view = inflater.inflate(R.layout.popup_layout, null);
//            Button record = view.findViewById(R.id.record);
//            Button cancel = view.findViewById(R.id.cancel);
//            String text = intent.getStringExtra("phoneNumber") + "  is calling";
//
//
//            windowManager.addView(view, layoutParams);
//
//        } catch (Exception e) {
//            e.printStackTrace();
//
//        }

    }

    @Override
    public void onDestroy() {
        destroy();
        if(audioRecorder != null)
            audioRecorder.stop();
        super.onDestroy();
//        WindowManager windowManager = (WindowManager) getSystemService(Context.WINDOW_SERVICE);
//        windowManager.removeView(view);

    }

    private void destroy() {
        if (floatingViewManager != null) {
            floatingViewManager.removeAllViewToWindow();
            floatingViewManager = null;
        }
    }


    @Override
    public void onFinishFloatingView() {
        stopSelf();
        if(audioRecorder != null)
            audioRecorder.stop();
        Log.d(TAG, getString(R.string.finish_deleted));
    }

    @Override
    public void onTouchFinished(boolean isFinishing, int x, int y) {
        if(isFinishing){
            Log.d(TAG,"Finishing ");
            stopForeground(true);
        }else {
            Log.d(TAG,getString(R.string.finished_pos,x,y));
        }

    }







    private static Notification createNotification(Context context) {
        String NOTIFICATION_CHANNEL_ID = context.getPackageName();
        String channelName = "My Background Service";
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {

            NotificationChannel chan = new NotificationChannel(NOTIFICATION_CHANNEL_ID, channelName, NotificationManager.IMPORTANCE_NONE);

            chan.setLightColor(Color.BLUE);
            chan.setLockscreenVisibility(Notification.VISIBILITY_PRIVATE);
            NotificationManager manager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
            if (manager != null) {
                manager.createNotificationChannel(chan);
            }

        }
            final NotificationCompat.Builder builder = new NotificationCompat.Builder(context, NOTIFICATION_CHANNEL_ID);
            builder.setWhen(System.currentTimeMillis());
            builder.setSmallIcon(R.mipmap.ic_launcher_round);
            builder.setContentTitle(context.getString(R.string.app_name)+" running");
            builder.setContentText("Recording..");
            builder.setOngoing(true);
            builder.setPriority(NotificationCompat.PRIORITY_MIN);
            builder.setCategory(NotificationCompat.CATEGORY_SERVICE);
            return builder.build();

    }
}

