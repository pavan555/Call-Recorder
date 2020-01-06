package com.example.pavan.callrecorderdemo;

import android.content.Context;
import android.database.DatabaseUtils;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.SeekBar;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Timer;
import java.util.TimerTask;

public class AudioEditActivity extends AppCompatActivity {

    EditText editText;
    SeekBar seekBar;
    boolean pause=false;
    Button playButton;

    MediaPlayer mediaPlayer;
    AudioManager audioManager;

    Timer timer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_audio_edit);


        editText=findViewById(R.id.editText);
        playButton=findViewById(R.id.playButton);
        String path=getIntent().getStringExtra("fileName");
        editText.setText(path);



        seekBar=findViewById(R.id.audioProgress);
        audioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);

        try {

            mediaPlayer = new MediaPlayer();
            mediaPlayer.reset();

            String state = Environment.getExternalStorageState();
            if(!state.equals(Environment.MEDIA_MOUNTED)){
                throw new IOException("SD Card is not Mounted, It's in"+state+".");
            }

            File file=new File(Environment.getExternalStorageDirectory().getPath() + File.separator+"recordedCalls"+File.separator + path+".mp3");
            FileInputStream fileInputStream=new FileInputStream(file);
            mediaPlayer.setDataSource(fileInputStream.getFD());
            mediaPlayer.prepareAsync();
            fileInputStream.close();
            mediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
            mediaPlayer.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
                @Override
                public void onPrepared(MediaPlayer mp) {
//                    Toast.makeText(AudioEditActivity.this,"Prepared",Toast.LENGTH_SHORT).show();
                    seekBar.setMax(mediaPlayer.getDuration());
                }
            });

        }catch (Exception e){
            e.printStackTrace();
        }

        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if(fromUser){
                    mediaPlayer.seekTo(progress);
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
                    mediaPlayer.pause();
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                    mediaPlayer.start();
            }
        });


        TimerTask timerTask=new TimerTask() {
            @Override
            public void run() {
                seekBar.setProgress(mediaPlayer.getCurrentPosition());
            }
        };
        timer = new Timer();
        timer.scheduleAtFixedRate(timerTask,0,200);

    }

    @Override
    public void onBackPressed() {
        timer.cancel();
        finish();
        mediaPlayer.release();
    }

    public void play(View view) {
        if(mediaPlayer!=null) {

            if(!pause) {
                mediaPlayer.start();
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    playButton.setForeground(getDrawable(android.R.drawable.ic_media_pause));
                }
                pause = true;
            }else {
                mediaPlayer.pause();
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    playButton.setForeground(getDrawable(android.R.drawable.ic_media_play));
                }
                pause = false;
            }

        }
    }


    public void renameFile(View view) {
        String path=editText.getText().toString();
        String msg="Please Enter name";


        if(!path.equals("")){
            File dir = new File(Environment.getExternalStorageDirectory().getAbsolutePath()+"/recordedCalls");
            if(dir.exists()){
                File from=new File(dir,getIntent().getStringExtra("fileName")+".mp3");
                File to=new File(dir,path+".mp3");
                if(to.exists()){
                    msg="Already name existed";
                }else if(from.exists()){
                    boolean result=from.renameTo(to);
                    MainActivity.sqLiteDatabase.execSQL("UPDATE RECORDS SET fileName ="+ DatabaseUtils.sqlEscapeString(path)+" WHERE ID="+getIntent().getStringExtra("ID"));
                    MainActivity.records.set(getIntent().getIntExtra("position",0),path);
                    MainActivity.arrayAdapter.notifyDataSetChanged();
                    if(result){
                        msg="Successfully updated";
                    }
                }

            }
        }

        Toast toast = Toast.makeText(AudioEditActivity.this,msg, Toast.LENGTH_LONG);
        toast.setGravity(Gravity.CENTER, 0, 0);
        toast.show();
    }
}
