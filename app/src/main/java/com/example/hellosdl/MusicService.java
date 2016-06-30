package com.example.hellosdl;

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.IBinder;
import android.os.PowerManager;
import android.util.Log;

import java.io.IOException;

public class MusicService extends Service implements MediaPlayer.OnPreparedListener, MediaPlayer.OnErrorListener, AudioManager.OnAudioFocusChangeListener {
    private static final String TAG = "MusicService";

    public static final String ACTION_PLAY_NEW_SOURCE = "musicService.play.new.source";
    public static final String ACTION_TOGGLE_PLAYBACK = "musicService.toggle.playBack";

    private MediaPlayer mediaPlayer;
    private boolean mediaPlayerPreparing = false;

    private BroadcastReceiver musicServiceReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            // TODO handle intents for play, pause, skip, next, events.
            if(intent == null) return;

            switch (intent.getAction()) {
                case ACTION_PLAY_NEW_SOURCE:
                    Log.d(TAG, "Play new source");
                    if(mediaPlayer != null) {
                        mediaPlayer.release();
                        mediaPlayer = null;
                        mediaPlayerPreparing = false;
                    }

                    String source = intent.getStringExtra("source");
                    initializeMediaPlayer(source);
                    break;
                case ACTION_TOGGLE_PLAYBACK:
                    Log.d(TAG, "Toggle playback");
                    // Play/Pause only works when the media playing is not in the init/preparing state
                    if(mediaPlayerPreparing) return;

                    if(mediaPlayer.isPlaying()) {
                        mediaPlayer.pause();
                    } else {
                        mediaPlayer.start();
                    }
                    break;
                default:
                    Log.w(TAG, "Unknown action received");
                    break;
            }
        }
    };

    @Override
    public void onCreate() {
        super.onCreate();

        IntentFilter filter = new IntentFilter();
        filter.addAction(ACTION_PLAY_NEW_SOURCE);
        filter.addAction(ACTION_TOGGLE_PLAYBACK);

        registerReceiver(musicServiceReceiver, filter);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.v(TAG, "onStartCommand");
        return super.onStartCommand(intent, flags, startId);
    }

    private void initializeMediaPlayer(String dataSource) {
        Log.v(TAG, "initializeMediaPlayer");
        if(mediaPlayer == null) {
            mediaPlayer = new MediaPlayer();
        }

        try {
            mediaPlayer.setDataSource(dataSource);
        } catch (IllegalArgumentException | IOException ex) {
            ex.printStackTrace();
        }

        mediaPlayer.setOnPreparedListener(this);
        mediaPlayer.setOnErrorListener(this);

        // Keep the phone awake so it can play audio
        mediaPlayer.setWakeMode(getApplicationContext(), PowerManager.PARTIAL_WAKE_LOCK);

        mediaPlayerPreparing = true;
        mediaPlayer.prepareAsync();
    }

    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        throw new UnsupportedOperationException("Not yet implemented");
    }

    @Override
    public void onPrepared(MediaPlayer mediaPlayer) {
        Log.v(TAG, "onPrepared");
        mediaPlayerPreparing = false;
        AudioManager audioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
        int result = audioManager.requestAudioFocus(this, AudioManager.STREAM_MUSIC, AudioManager.AUDIOFOCUS_GAIN);

        if(result == AudioManager.AUDIOFOCUS_REQUEST_GRANTED) {
            if(!mediaPlayer.isPlaying()) {
                mediaPlayer.start();
            }
        } else {
            Log.e(TAG, "Failed to get audio focus");
        }
    }

    @Override
    public boolean onError(MediaPlayer mediaPlayer, int i, int i1) {
        Log.e(TAG, "onError");

        mediaPlayer.reset();

        return false;
    }

    @Override
    public void onAudioFocusChange(int focusChange) {
        Log.v(TAG, "onAudioFocusChange");
        switch (focusChange) {
            case AudioManager.AUDIOFOCUS_GAIN:
                // resume playback
//                if (mediaPlayer == null) initializeMediaPlayer("http://vprbbc.streamguys.net:80/vprbbc24.mp3");
//                else if (!mediaPlayer.isPlaying()) mediaPlayer.start();
//                mediaPlayer.setVolume(1.0f, 1.0f);
                break;

            case AudioManager.AUDIOFOCUS_LOSS:
                // Lost focus for an unbounded amount of time: stop playback and release media player
                if (mediaPlayer.isPlaying()) mediaPlayer.stop();
                mediaPlayer.release();
                mediaPlayer = null;
                break;

            case AudioManager.AUDIOFOCUS_LOSS_TRANSIENT:
                // Lost focus for a short time, but we have to stop
                // playback. We don't release the media player because playback
                // is likely to resume
                if (mediaPlayer.isPlaying()) mediaPlayer.pause();
                break;

            case AudioManager.AUDIOFOCUS_LOSS_TRANSIENT_CAN_DUCK:
                // Lost focus for a short time, but it's ok to keep playing
                // at an attenuated level
                if (mediaPlayer.isPlaying()) mediaPlayer.setVolume(0.1f, 0.1f);
                break;
        }
    }

    @Override
    public void onDestroy() {
        Log.v(TAG, "onDestroy");
        unregisterReceiver(musicServiceReceiver);

        if(mediaPlayer != null) mediaPlayer.release();

        super.onDestroy();
    }
}
