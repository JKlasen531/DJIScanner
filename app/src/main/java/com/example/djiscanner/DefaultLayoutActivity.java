package com.example.djiscanner;

import android.Manifest;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import java.util.ArrayList;
import java.util.List;

import dji.common.error.DJIError;
import dji.common.mission.waypoint.Waypoint;
import dji.common.mission.waypoint.WaypointAction;
import dji.common.mission.waypoint.WaypointActionType;
import dji.common.mission.waypoint.WaypointMissionFinishedAction;
import dji.common.mission.waypoint.WaypointMissionFlightPathMode;
import dji.common.mission.waypoint.WaypointMissionGotoWaypointMode;
import dji.common.mission.waypoint.WaypointMissionHeadingMode;
import dji.common.util.CommonCallbacks;
import dji.keysdk.FlightControllerKey;
import dji.keysdk.KeyManager;

import dji.common.mission.waypoint.WaypointMission;
import dji.sdk.media.MediaManager;
import dji.sdk.mission.MissionControl;
import dji.sdk.mission.waypoint.WaypointMissionOperator;
import dji.sdk.mission.waypoint.WaypointMissionOperatorListener;

public class DefaultLayoutActivity extends AppCompatActivity implements View.OnClickListener {

    /*private Button mMediaManagerBtn;
    private Button startMissionBtn;

    private WaypointMissionOperator waypointMissionOperator;
    private WaypointMission mission;
    private WaypointMissionOperatorListener listener;
    private final int WAYPOINT_COUNT=-1; //set value once mission is done*/

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.i("DLA","set Content View");
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.VIBRATE,
                            Manifest.permission.INTERNET, Manifest.permission.ACCESS_WIFI_STATE,
                            Manifest.permission.WAKE_LOCK, Manifest.permission.ACCESS_COARSE_LOCATION,
                            Manifest.permission.ACCESS_NETWORK_STATE, Manifest.permission.ACCESS_FINE_LOCATION,
                            Manifest.permission.CHANGE_WIFI_STATE, Manifest.permission.MOUNT_UNMOUNT_FILESYSTEMS,
                            Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.SYSTEM_ALERT_WINDOW,
                            Manifest.permission.READ_PHONE_STATE,
                    }
                    , 1);
        }

        setContentView(R.layout.activity_default_layout);

        /*mMediaManagerBtn = findViewById(R.id.btn_mediaManager);
        mMediaManagerBtn.setOnClickListener(this);
        startMissionBtn = findViewById(R.id.btn_startMission);
        startMissionBtn.setOnClickListener(this);*/
        //add buttons to stop/pause mission

        /*if(waypointMissionOperator == null) {
            waypointMissionOperator = MissionControl.getInstance().getWaypointMissionOperator();
        }*/
        //mission = createTestWaypointMission();
        //DJIError djiError = waypointMissionOperator.loadMission(mission);
        //showResultToast(djiError);
    }

    @Override
    public void onClick(View v) {
        /*switch (v.getId()) {
            case R.id.btn_mediaManager: {
                Intent intent = new Intent(this, MediaManagerActivity.class);
                startActivity(intent);
                break;
            }
            case R.id.btn_startMission: {
                /*if(mission != null) {
                    waypointMissionOperator.startMission(new CommonCallbacks.CompletionCallback() {
                        @Override
                        public void onResult(DJIError djiError) {
                            showResultToast(djiError);
                        }
                    });
                } else {
                    Log.i("mission start", "something went wrong, mission is null");
                }*/
                /*Log.e("ONClick","Mission start Button");
                break;
            }
            default:
                break;
        }*/
    }

    private void showResultToast(DJIError djiError) {
        Toast.makeText(getApplicationContext(), djiError == null ? "Action started!" : djiError.getDescription(), Toast.LENGTH_SHORT);
    }

    //mission code goes here
    //Timeline mission or waypoint mission
    //add something to customize the mission

    /*private WaypointMission createTestWaypointMission() {
        WaypointMission.Builder builder = new WaypointMission.Builder();
        double baseLatitude = 22;
        double baseLongitude = 113;
        Object latitudeValue = KeyManager.getInstance().getValue((FlightControllerKey.create(FlightControllerKey.HOME_LOCATION_LATITUDE)));
        Object longitudeValue = KeyManager.getInstance().getValue((FlightControllerKey.create(FlightControllerKey.HOME_LOCATION_LONGITUDE)));
        if(latitudeValue != null && latitudeValue instanceof Double) {
            baseLatitude = (double)latitudeValue;
        }
        if(longitudeValue != null && longitudeValue instanceof Double) {
            baseLongitude = (double)longitudeValue;
        }

        final float baseAltitude = 50.0f;
        builder.autoFlightSpeed(5f);
        builder.maxFlightSpeed(10f);
        builder.setExitMissionOnRCSignalLostEnabled(false);
        // change to go back to waypoint 1 maybe
        builder.finishedAction(WaypointMissionFinishedAction.NO_ACTION);
        // maybe WaypointMissionFlightPathMode.CURVED
        builder.flightPathMode(WaypointMissionFlightPathMode.NORMAL);
        builder.gotoFirstWaypointMode(WaypointMissionGotoWaypointMode.SAFELY);
        builder.headingMode(WaypointMissionHeadingMode.AUTO);
        builder.repeatTimes(1);
        //create Waypoints and add them to the builder
        final Waypoint waypoint = new Waypoint(22, 113, 50.0f);
        waypoint.addAction(new WaypointAction(WaypointActionType.CAMERA_FOCUS,1));
        waypoint.addAction(new WaypointAction(WaypointActionType.START_TAKE_PHOTO,1));
        builder.addWaypoint(waypoint);
        return builder.build();
    }*/
}
