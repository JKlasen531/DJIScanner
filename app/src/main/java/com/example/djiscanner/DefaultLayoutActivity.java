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

public class DefaultLayoutActivity extends AppCompatActivity implements View.OnClickListener
 {

    private Button mMediaManagerBtn;
    private Button startMissionBtn;

    private WaypointMissionOperator waypointMissionOperator;
    private WaypointMission mission;
    private WaypointMissionOperatorListener listener;
    private final int WAYPOINT_COUNT=-1; //set value once mission is done*/

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.i("DLA","set Content View");

        setContentView(R.layout.activity_default_layout);

        mMediaManagerBtn = findViewById(R.id.btn_mediaManager);
        mMediaManagerBtn.setOnClickListener(this);
        startMissionBtn = findViewById(R.id.btn_startMission);
        startMissionBtn.setOnClickListener(this);
        //add buttons to stop/pause mission

        if(waypointMissionOperator == null) {
            waypointMissionOperator = MissionControl.getInstance().getWaypointMissionOperator();
        }
        mission = createTestWaypointMission();
        DJIError djiError = waypointMissionOperator.loadMission(mission);
        Log.i("Activity","Mission loaded: " + String.valueOf(djiError == null));
        showResultToast(djiError);
        waypointMissionOperator.uploadMission(new CommonCallbacks.CompletionCallback() {
            @Override
            public void onResult(DJIError djiError) {
                if(djiError != null) {
                    Log.i("Activity","error during upload, retrying : " + djiError.getDescription());
                    waypointMissionOperator.retryUploadMission(null);
                }
            }
        });
        Log.i("Activity",String.valueOf(waypointMissionOperator.getCurrentState()));
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn_mediaManager: {
                Log.i("UXSDK","Media activity starting");
                Intent intent = new Intent(this, MediaManagerActivity.class);
                startActivity(intent);
                break;
            }
            case R.id.btn_startMission: {
                if(mission != null) {
                    waypointMissionOperator.startMission(new CommonCallbacks.CompletionCallback() {
                        @Override
                        public void onResult(DJIError djiError) {
                            showResultToast(djiError);
                            Log.i("Result", djiError == null ? "no error" : djiError.getDescription());
                        }
                    });
                    Log.i("Mission","mission started");
                } else {
                    Log.i("mission start", "something went wrong, mission is null");
                }
                break;
            }
            default:
                break;
        }
    }

    private void showResultToast(DJIError djiError) {
        Toast.makeText(getApplicationContext(), djiError == null ? "Action started!" : djiError.getDescription(), Toast.LENGTH_SHORT);
    }

    //mission code goes here
    //Timeline mission or waypoint mission
    //add something to customize the mission

    private WaypointMission createTestWaypointMission() {
        WaypointMission.Builder builder = new WaypointMission.Builder();
        double baseLatitude = 22;
        double baseLongitude = 113;
        /*Object latitudeValue = KeyManager.getInstance().getValue((FlightControllerKey.create(FlightControllerKey.HOME_LOCATION_LATITUDE)));
        Object longitudeValue = KeyManager.getInstance().getValue((FlightControllerKey.create(FlightControllerKey.HOME_LOCATION_LONGITUDE)));
        if(latitudeValue != null && latitudeValue instanceof Double) {
            baseLatitude = (double)latitudeValue;
        }
        if(longitudeValue != null && longitudeValue instanceof Double) {
            baseLongitude = (double)longitudeValue;
        }*/

        final float baseAltitude = 50.0f;
        List<Waypoint> waypointList = new ArrayList<>();
        builder.autoFlightSpeed(5f);
        builder.maxFlightSpeed(10f);
        builder.gotoFirstWaypointMode(WaypointMissionGotoWaypointMode.SAFELY);
        builder.finishedAction(WaypointMissionFinishedAction.NO_ACTION);
        builder.headingMode(WaypointMissionHeadingMode.AUTO);
        builder.flightPathMode(WaypointMissionFlightPathMode.NORMAL);
        builder.setExitMissionOnRCSignalLostEnabled(true);
        builder.setGimbalPitchRotationEnabled(true);
        builder.repeatTimes(1);

        // change to go back to waypoint 1 maybe
        // maybe WaypointMissionFlightPathMode.CURVED
        //create Waypoints and add them to the builder

        final Waypoint waypoint = new Waypoint(22, 113, 50.0f);
        waypoint.addAction(new WaypointAction(WaypointActionType.ROTATE_AIRCRAFT,0));
        //waypoint.addAction(new WaypointAction(WaypointActionType.CAMERA_FOCUS,1));
        //waypoint.addAction(new WaypointAction(WaypointActionType.START_TAKE_PHOTO,1));
        final Waypoint waypoint2 = new Waypoint(30, 113, 50.0f);
        final Waypoint waypoint3 = new Waypoint(30, 123, 50.0f);
        builder.addWaypoint(waypoint);
        builder.addWaypoint(waypoint2);
        builder.addWaypoint(waypoint3);
        Log.i("Mission Complete:",String.valueOf(builder.isMissionComplete()));
        return builder.build();
    }
}
