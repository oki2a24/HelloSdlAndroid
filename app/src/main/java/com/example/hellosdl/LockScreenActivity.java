package com.example.hellosdl;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.util.Log;
import android.widget.ImageView;

import java.io.FileInputStream;

/**
 *
 */
public class LockScreenActivity extends Activity {

    private static final String TAG = "LockScreenActivity";

    ImageView lockScreenImageView;

    public static final String ACTION_CLOSE_LOCK_SCREEN = "LockScreenActivity.Close";

    private final BroadcastReceiver closeLockScreenBroadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if(intent.getAction().equals(ACTION_CLOSE_LOCK_SCREEN)) {
                Log.d(TAG, "Received intent to close lock screen");
                finish();
            }
        }
    };

    private final BroadcastReceiver setLockScreenIconBroadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            // TODO set lockscreen icon.
            Bitmap bitmap = null;
            String filename = getIntent().getStringExtra("image");
            try {
                FileInputStream inputSteam = openFileInput(filename);
                bitmap = BitmapFactory.decodeStream(inputSteam);
                inputSteam.close();

                lockScreenImageView.setImageBitmap(bitmap);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        registerReceiver(closeLockScreenBroadcastReceiver, new IntentFilter(ACTION_CLOSE_LOCK_SCREEN));
        registerReceiver(setLockScreenIconBroadcastReceiver, new IntentFilter("SET_LOCK_SCREEN_ICON"));

        lockScreenImageView = (ImageView) findViewById(R.id.imageViewLockScreen);

        setContentView(R.layout.activity_lock_screen);
    }

    @Override
    protected void onDestroy() {
        unregisterReceiver(closeLockScreenBroadcastReceiver);
        unregisterReceiver(setLockScreenIconBroadcastReceiver);

        super.onDestroy();
    }
}
