package com.example.pavan.callrecorderdemo.Services;

import android.media.MediaRecorder;
import android.os.Build;
import android.os.Environment;

import java.io.File;
import java.io.IOException;

class AudioRecorder {


    private MediaRecorder mediaRecorder = new MediaRecorder();

    /* Required Parameters
     Path ==> To save the recorded file


     */
    private String path;

    AudioRecorder(String path) {
        this.path=build(path);
    }

    private String build(String path) {
        if(!path.startsWith("/")){
            path="/"+path;
        }
        if(!path.contains(".")){
            path+=".mp3";
        }

        return Environment.getExternalStorageDirectory().getAbsolutePath()+"/recordedCalls"+path;
    }
    
    
    /*
    Start Recording
    */
     public void start() throws IOException {
        String state = Environment.getExternalStorageState();
        if(!state.equals(Environment.MEDIA_MOUNTED)){
            throw new IOException("SD Card is not Mounted, It's in"+state+".");
        }

        //check for directory for saving exists or not

        File directory=new File(path).getParentFile();
        if(!directory.exists() && !directory.mkdirs()){
            throw new IOException("Path to file could not be created");
        }
        mediaRecorder.setAudioSource(MediaRecorder.AudioSource.DEFAULT);
        mediaRecorder.setOutputFormat(MediaRecorder.OutputFormat.MPEG_4);
        mediaRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AAC);
        mediaRecorder.setAudioSamplingRate(44100);
        mediaRecorder.setAudioChannels(1);
        mediaRecorder.setAudioEncodingBitRate(96000);
        mediaRecorder.setOutputFile(path);
        mediaRecorder.prepare();

        try {
            Thread.sleep(2000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        mediaRecorder.start();



    }

    /*
        Stop Recording
    */
    public void stop(){
        mediaRecorder.stop();
        mediaRecorder.reset();
        mediaRecorder.release();
    }

    public void pause(){
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            mediaRecorder.pause();
        }
    }

    public void resume(){
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            mediaRecorder.resume();
        }

    }

}
