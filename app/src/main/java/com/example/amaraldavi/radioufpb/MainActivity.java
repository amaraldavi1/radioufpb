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
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.TaskStackBuilder;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
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
    private TextView txtModo;
    private long currentTime;
    private boolean isPlaying;
    private SeekBar volume;
    private Button btnPlay;
    private Button btnStop;
    private ProgressBar pgBar;
    private String radioURL = "http://cast3.hoost.com.br:8843/live";
    //private String radioURL = "http://audio.cabobranco.tv.br:8000/cbn";





    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        txtStatus = (TextView) findViewById(R.id.txtStatus);
        txtModo = (TextView) findViewById(R.id.txtModo);
        volume = (SeekBar)findViewById(R.id.volumeBar);
        btnPlay = (Button) findViewById(R.id.btnPlay);
        btnStop = (Button) findViewById(R.id.btnStop);
        pgBar = (ProgressBar) findViewById(R.id.progress);
        txtModo.setText("Presione o botão PLAY para iniciar");

        txtStatus.setText("");

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
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        outState.putLong("currentTime", currentTime);
        outState.putBoolean("isPlaying",isPlaying);

    }

    @Override
    protected void onPause() {
        super.onPause();

        //Notificacao

        NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(this)
                .setSmallIcon(R.drawable.notlogo)
                .setContentTitle("Radio UFPB")
                .setContentText("Escutando...");

        NotificationManager mNotificationManager =
                (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        // Creates an explicit intent for an Activity in your app
        Intent resultIntent = new Intent(this, MainActivity.class);

        // The stack builder object will contain an artificial back stack for the
        // started Activity.
        // This ensures that navigating backward from the Activity leads out of
        // your application to the Home screen.
        TaskStackBuilder stackBuilder = TaskStackBuilder.create(this);
        // Adds the back stack for the Intent (but not the Intent itself)
        stackBuilder.addParentStack(MainActivity.class);
        // Adds  the Intent that starts the Activity to the top of the stack
        stackBuilder.addNextIntent(resultIntent);
        PendingIntent resultPendingIntent =
                stackBuilder.getPendingIntent(
                        0123,
                        PendingIntent.FLAG_UPDATE_CURRENT
                );
        mBuilder.setContentIntent(resultPendingIntent);
        // mId allows you to update the notification later on.
        mNotificationManager.notify(0123, mBuilder.build());

        Log.i("Estado da Aplicação: ","onPause");

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


            //parar a thread de atualizar o tempo currenttime

            NotificationManager mNotificationManager =
                    (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
            mNotificationManager.cancel(0123);
        }

        Log.i("Estado da Aplicação: ","onDestroy");
    }

    //Métodos de reprodução

    public void playMusic(View v){

        if(player == null){

            try {
                txtModo.setText("Carregando...");
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
            updateTimeMusicThread(player, txtStatus);
        }

    }


    public void stopMusic(View v){
        isPlaying = false;
        if(player != null){
            player.stop();
            player.release();
            player = null;
            currentTime = 0;
            txtStatus.setText("");
            txtModo.setText("Parado");
            btnPlay.setEnabled(true);
            btnPlay.setVisibility(View.VISIBLE);
            btnStop.setVisibility(View.GONE);

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
                while(isPlaying){
                    try{
                        updateTimeMusic(mp.getCurrentPosition(), view);
                        Thread.sleep(1000);

                    }catch (IllegalStateException e){
                        e.printStackTrace();
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

        txtModo.setText("Bufferizando...");

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
        updateTimeMusicThread(mp, txtStatus);
        txtModo.setText("Reproduzindo...");
        btnPlay.setVisibility(View.GONE);
        pgBar.setVisibility(View.GONE);
        btnStop.setVisibility(View.VISIBLE);
    }


    @Override
    public void onSeekComplete(MediaPlayer mp) {

        Log.i("Script", "onSeekComplete()");
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
