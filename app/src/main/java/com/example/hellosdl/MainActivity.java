package com.example.hellosdl;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Start the SDLService
        Intent sdlServiceIntent = new Intent(this, SdlService.class);
        startService(sdlServiceIntent);
    }
}
