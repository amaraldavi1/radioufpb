package com.example.amaraldavi.radioufpb;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnBufferingUpdateListener;
import android.media.MediaPlayer.OnCompletionListener;
import android.media.MediaPlayer.OnErrorListener;
import android.media.MediaPlayer.OnPreparedListener;
import android.media.MediaPlayer.OnSeekCompleteListener;
import android.os.Build;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.app.NotificationCompat;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.TextView;

import java.io.IOException;

public class MainActivity extends AppCompatActivity implements OnPreparedListener, OnSeekCompleteListener, OnCompletionListener, OnBufferingUpdateListener, OnErrorListener, OnSeekBarChangeListener{

    private MediaPlayer player;
    private TextView txtStatus;
    private TextView txtTempo;
    private long currentTime;
    private boolean isPlaying;
    private SeekBar volume;
    private Button btnPlay;
    private Button btnStop;
    private ProgressBar pgBar;

    private TextView radioTitle;
    private TextView radioDesc;



    //private String radioURL = "http://cast3.hoost.com.br:8843/live";
    private String radioURL = "http://audio.cabobranco.tv.br:8000/cbfm";
    //private String radioURL = "http://servidor36.brlogic.com:8038/live";



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

            txtStatus = (TextView) findViewById(R.id.txtStatus);
            txtTempo = (TextView) findViewById(R.id.txtTempo);
            volume = (SeekBar) findViewById(R.id.volumeBar);
            btnPlay = (Button) findViewById(R.id.btnPlay);
            btnStop = (Button) findViewById(R.id.btnStop);
            pgBar = (ProgressBar) findViewById(R.id.progress);
            radioTitle = (TextView) findViewById(R.id.radio_title);
            radioDesc = (TextView) findViewById(R.id.radio_desc);


            txtStatus.setText("Presione o botão PLAY para iniciar");


            VolumeControls();

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            Window window = getWindow();
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            window.setStatusBarColor(Color.parseColor("#00223b"));
        }

        if(savedInstanceState != null){
            currentTime = savedInstanceState.getLong("currentTime");
            isPlaying =  savedInstanceState.getBoolean("isPlaying");

            if(isPlaying){
                playMusic(null);
            }

        }
        Log.i("Estado da Aplicação: ","onCreate");

    }

    private void VolumeControls() {

        final AudioManager audioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
        volume.setMax(audioManager
                .getStreamMaxVolume(AudioManager.STREAM_MUSIC));
        volume.setProgress(audioManager
                .getStreamVolume(AudioManager.STREAM_MUSIC));

        volume.setOnSeekBarChangeListener(new OnSeekBarChangeListener() {
            public void onStopTrackingTouch(SeekBar arg0){
            }

            public void onStartTrackingTouch(SeekBar arg0){
            }

            public void onProgressChanged(SeekBar arg0, int progress, boolean arg2)
            {
                audioManager.setStreamVolume(AudioManager.STREAM_MUSIC,
                        progress, 0);
            }
        });
    }



    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_VOLUME_UP)
        {
            int index = volume.getProgress();
            volume.setProgress(index + 1);
            return true;
        } else if (keyCode == KeyEvent.KEYCODE_VOLUME_DOWN)
        {
            int index = volume.getProgress();
            volume.setProgress(index - 1);
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        outState.putLong("currentTime", currentTime);
        outState.putBoolean("isPlaying",isPlaying);

    }

    @Override
    protected void onPause() {
        super.onPause();
        Log.i("Estado da Aplicação : ","onPause");

    }

    @Override
    protected void onResume() {
        super.onResume();

        Log.i("Estado da Aplicação: ","onresume");

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        if(player != null){
            player.stop();
            player.release();
            player = null;

        }

        Log.i("Estado da Aplicação: ","onDestroy");
    }

    //Métodos de reprodução

    public void playMusic(View v){

        if(player == null){

            try {
                txtStatus.setText("Carregando...");
                btnPlay.setEnabled(false);
                btnPlay.setVisibility(View.GONE);
                pgBar.setVisibility(View.VISIBLE);
                player = new MediaPlayer();
                player.setAudioStreamType(AudioManager.STREAM_MUSIC);
                player.setDataSource(radioURL);
                player.prepareAsync();

                player.setOnBufferingUpdateListener(this);
                player.setOnCompletionListener(this);
                player.setOnErrorListener(this);
                player.setOnPreparedListener(this);
                player.setOnSeekCompleteListener(this);


            }catch (IOException e) {
                e.printStackTrace();
            }
        }else{
            player.start();
            isPlaying = true;
            updateTimeMusicThread(player, txtTempo);
        }

    }


    public void stopMusic(View v){
        isPlaying = false;
        if(player != null){
            player.stop();
            player.release();
            player = null;
            currentTime = 0;
            txtTempo.setText("");
            txtStatus.setText("Parado");
            btnPlay.setEnabled(true);
            btnPlay.setVisibility(View.VISIBLE);
            btnStop.setVisibility(View.GONE);



            NotificationManager mNotificationManager =
                    (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
            mNotificationManager.cancelAll();

        }

    }


    @Override
    public void onBackPressed() {
        super.onBackPressed();

        if(player != null) {
            player.stop();
            NotificationManager mNotificationManager =
                    (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
            mNotificationManager.cancelAll();
        }
    }

    private String getTimeString(long millis) {
        StringBuffer buf = new StringBuffer();

        int hours = (int) (millis / (1000 * 60 * 60));
        int minutes = (int) ((millis % (1000 * 60 * 60)) / (1000 * 60));
        int seconds = (int) (((millis % (1000 * 60 * 60)) % (1000 * 60)) / 1000);

        buf
                .append(String.format("%02d", hours))
                .append(":")
                .append(String.format("%02d", minutes))
                .append(":")
                .append(String.format("%02d", seconds));

        return buf.toString();
    }


    public void updateTimeMusic(final long currentTime, final TextView view){

        runOnUiThread(new Runnable() {
            @Override
            public void run() {

                String sCurrentTime = getTimeString(currentTime);
                view.setText(sCurrentTime);

            }
        });
    }


    public void updateTimeMusicThread(final MediaPlayer mp, final TextView view){

        new Thread(){
            public void run(){
                while(isPlaying ){
                    try{
                        updateTimeMusic(mp.getCurrentPosition(), view);
                        Thread.sleep(1000);

                    }catch (IllegalStateException e){
                        e.printStackTrace();
                        return;
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }
        }.start();
    }


    // LISTENERS
    @Override
    public void onBufferingUpdate(MediaPlayer mp, int percent) {

        txtStatus.setText("Bufferizando...");

    }

    @Override
    public void onCompletion(MediaPlayer mp) {
        Log.i("Script", "onCompletion()");
    }

    @Override
    public boolean onError(MediaPlayer mp, int what, int extra) {
        Log.i("Script", "onError()");
        return false;
    }

    @Override
    public void onPrepared(MediaPlayer mp) {
        Log.i("Script", "onPrepared()");

        isPlaying = true;

        mp.start();



        mp.seekTo((int) currentTime);
        mp.setLooping(true);
        updateTimeMusicThread(mp, txtTempo);

        txtStatus.setText("Reproduzindo...");
        btnPlay.setVisibility(View.GONE);
        pgBar.setVisibility(View.GONE);
        btnStop.setVisibility(View.VISIBLE);

        lancaNotificacao(MainActivity.this);

    }


    public void lancaNotificacao(Context context){

        final NotificationManager nm = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

        String contentTitle = "RADIO UFPB";

        // construct the Notification object.
        final android.support.v4.app.NotificationCompat.Builder  builder = new NotificationCompat.Builder(MainActivity.this)
                .setContentTitle(contentTitle)
                .setPriority(2)
                .setOngoing(true)
                .setContentText(txtStatus.getText())
                .setSmallIcon(R.drawable.notlogo);

        // Creates an explicit intent for an Activity in your app
        Intent resultIntent = new Intent(MainActivity.this, MainActivity.class);

        resultIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
        // Because clicking the notification opens a new ("special") activity, there's
        // no need to create an artificial back stack.
        PendingIntent resultPendingIntent =
                PendingIntent.getActivity(
                        MainActivity.this,
                        0,
                        resultIntent,
                        PendingIntent.FLAG_UPDATE_CURRENT
                );

        builder.setContentIntent(resultPendingIntent);

        nm.notify(R.drawable.notlogo, builder.build());

    }

    @Override
    public void onSeekComplete(MediaPlayer mp) {

    }


    @Override
    public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {

    }

    @Override
    public void onStartTrackingTouch(SeekBar seekBar) {

    }

    @Override
    public void onStopTrackingTouch(SeekBar seekBar) {

    }





}





