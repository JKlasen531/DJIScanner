package com.example.djiscanner;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;

import dji.sdk.media.MediaManager;

public class DefaultLayoutActivity extends AppCompatActivity implements View.OnClickListener {

    private Button mMediaManagerBtn;
    private Button startMission;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_default_layout);

        mMediaManagerBtn = findViewById(R.id.btn_mediaManager);
        mMediaManagerBtn.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn_mediaManager: {
                Intent intent = new Intent(this, MediaManagerActivity.class);
                startActivity(intent);
                break;
            }
            default:
                break;
        }
    }

    //mission code goes here
}
