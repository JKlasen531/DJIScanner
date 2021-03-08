package com.example.djiscanner;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import dji.common.error.DJIWaypointV2Error;
import dji.common.mission.waypoint.Waypoint;
import dji.common.mission.waypoint.WaypointAction;
import dji.common.mission.waypoint.WaypointActionType;
import dji.common.mission.waypoint.WaypointMission;
import dji.common.mission.waypoint.WaypointMissionFinishedAction;
import dji.common.mission.waypoint.WaypointMissionFlightPathMode;
import dji.common.mission.waypoint.WaypointMissionGotoWaypointMode;
import dji.common.mission.waypoint.WaypointMissionHeadingMode;
import dji.common.mission.waypoint.WaypointTurnMode;
import dji.common.mission.waypointv2.WaypointV2;
import dji.common.mission.waypointv2.WaypointV2Mission;
import dji.common.mission.waypointv2.WaypointV2MissionState;
import dji.common.mission.waypointv2.WaypointV2MissionTypes;
import dji.common.model.LocationCoordinate2D;
import dji.common.util.CommonCallbacks;
import dji.sdk.base.BaseProduct;
import dji.sdk.mission.MissionControl;
import dji.sdk.mission.waypoint.WaypointMissionOperator;
import dji.sdk.mission.waypoint.WaypointV2MissionOperator;

public class MissionTestActivity extends Activity implements View.OnClickListener{

    private Button startMission, stopMission, loadMission, uploadMission;

    //WaypointMission
    private WaypointMissionOperator waypointMissionOperator;
    private WaypointMission mission = null;
    private static final double HORIZONTAL_DISTANCE = 30;
    private static final double VERTICAL_DISTANCE = 30;
    private static final double ONE_METER_OFFSET = 0.00000899322;
    private double baseLatitude = 22;
    private double baseLongitude = 113;
    private final float baseAltitude = 30.0f;

    //WaypointV2Mission
    private final int REFRESH_FREQ = 10;
    private final int SATELLITE_COUNT = 10;
    private WaypointV2MissionOperator waypointV2MissionOperator = null;
    public WaypointV2Mission.Builder waypointV2MissionBuilder = null;
    //private WaypointV2MissionOperatorListener waypointV2MissionOperatorListener;
    private List<WaypointV2> waypointV2List = new ArrayList<>();
    private boolean canUploadMission = false;
    private boolean canUploadAction = false;
    private boolean canStartMission = false;
    //private WaypointV2ActionListener waypointV2ActionListener = null;
    //private List<WaypointV2Action> waypointV2ActionList = new ArrayList<>();

    private final String TAG = "MissionTestActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_mission_test);
        init();
    }

    private void init() {
        BaseProduct product = DJIScannerApplication.getProductInstance();
        if(waypointMissionOperator == null) {
            waypointMissionOperator = MissionControl.getInstance().getWaypointMissionOperator();
        }
        if(waypointV2MissionOperator == null) {
            waypointV2MissionOperator = MissionControl.getInstance().getWaypointMissionV2Operator();
        }
        startMission = findViewById(R.id.startMission);
        stopMission = findViewById(R.id.stopMission);
        loadMission = findViewById(R.id.loadMission);
        uploadMission = findViewById(R.id.uploadMission);
        startMission.setOnClickListener(this);
        stopMission.setOnClickListener(this);
        loadMission.setOnClickListener(this);
        uploadMission.setOnClickListener(this);
    }

    //WaypointMission
    /*@Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.startMission: {
                if(mission != null) {
                    waypointMissionOperator.startMission(new CommonCallbacks.CompletionCallback() {
                        @Override
                        public void onResult(DJIError djiError) {
                            if(djiError == null) {
                                Log.i(TAG,"Mission erfolgreich gestartet");
                            } else {
                                Log.i(TAG, "Mission starten fehlgeschlagen: " + djiError.getDescription());
                            }
                        }
                    });
                }
                break;
            }
            case R.id.stopMission: {
                waypointMissionOperator.stopMission(new CommonCallbacks.CompletionCallback() {
                    @Override
                    public void onResult(DJIError djiError) {
                        if(djiError == null) {
                            Log.i(TAG,"Mission erfolgreich gestoppt");
                        } else {
                            Log.i(TAG, "Mission stoppen fehlgeschlagen: " + djiError.getDescription());
                        }
                    }
                });
                break;
            }
            case R.id.loadMission: {
                mission = createTestWPMission();
                DJIError djiError = waypointMissionOperator.loadMission(mission);
                if(djiError == null) {
                    Log.i(TAG,"Mission erfolgreich geladen");
                } else {
                    Log.i(TAG, "Mission laden fehlgeschlagen: " + djiError.getDescription());
                }
                break;
            }
            case R.id.uploadMission: {
                if (WaypointMissionState.READY_TO_RETRY_UPLOAD.equals(waypointMissionOperator.getCurrentState()) || WaypointMissionState.READY_TO_UPLOAD.equals(waypointMissionOperator.getCurrentState())) {
                    waypointMissionOperator.uploadMission(new CommonCallbacks.CompletionCallback() {
                        @Override
                        public void onResult(DJIError djiError) {
                            if(djiError == null) {
                                Log.i(TAG,"Mission erfolgreich uploaded");
                            } else {
                                Log.i(TAG, "Mission upload fehlgeschlagen: " + djiError.getDescription());
                            }
                        }
                    });
                } else {
                    Log.i(TAG, "Mission noch nicht geladen!");
                }
                break;
            }
            default: {
                break;
            }
        }
    }*/

    //WaypointV2Mission
    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.startMission: {
                if(canStartMission) {
                    waypointV2MissionOperator.startMission(new CommonCallbacks.CompletionCallback<DJIWaypointV2Error>() {
                        @Override
                        public void onResult(DJIWaypointV2Error djiWaypointV2Error) {
                            if(djiWaypointV2Error == null) {
                                Log.i(TAG,"WP2: Mission erfolgreich gestartet");
                            } else {
                                Log.i(TAG,"WP2: Mission konnte nicht gestartet werden: "
                                 + djiWaypointV2Error.getDescription());
                            }
                        }
                    });
                }
                break;
            }
            case R.id.stopMission: {
                if(waypointV2MissionOperator.getCurrentState().equals(WaypointV2MissionState.EXECUTING)
                        || waypointV2MissionOperator.getCurrentState().equals(WaypointV2MissionState.INTERRUPTED)) {
                    waypointV2MissionOperator.stopMission(new CommonCallbacks.CompletionCallback<DJIWaypointV2Error>() {
                        @Override
                        public void onResult(DJIWaypointV2Error djiWaypointV2Error) {
                            if(djiWaypointV2Error == null) {
                                Log.i(TAG,"WP2: Mission erfolgreich gestoppt");
                            } else {
                                Log.i(TAG,"WP2: Mission konnte nicht gestoppt werden: "
                                        + djiWaypointV2Error.getDescription());
                            }
                        }
                    });
                }
                break;
            }
            case R.id.loadMission: {
                if (waypointV2MissionOperator.getCurrentState().equals(WaypointV2MissionState.READY_TO_UPLOAD) || waypointV2MissionOperator.getCurrentState().equals(WaypointV2MissionState.READY_TO_EXECUTE)) {
                    waypointV2MissionOperator.loadMission(createTestWV2PMission(), new CommonCallbacks.CompletionCallback<DJIWaypointV2Error>() {
                        @Override
                        public void onResult(DJIWaypointV2Error djiWaypointV2Error) {
                            if (djiWaypointV2Error == null) {
                                canUploadMission = true;
                                Log.i(TAG,"WP2: Mission erfolgreich geladen");
                            } else {
                                Log.i(TAG,"WP2: Mission konnte nicht geladen werden: " + djiWaypointV2Error.getDescription());
                            }
                        }
                    });
                } else {
                    Log.i(TAG,"WP2: Mission konnte nicht geladen werden");
                }
                break;
            }
            case R.id.uploadMission: {
                if(canUploadAction) {
                    waypointV2MissionOperator.uploadMission(new CommonCallbacks.CompletionCallback<DJIWaypointV2Error>() {
                        @Override
                        public void onResult(DJIWaypointV2Error djiWaypointV2Error) {
                            if(djiWaypointV2Error == null) {
                                Log.i(TAG,"WP2: Mission erfolgreich hochgeladen");
                                canStartMission = true; // should set this to the ActionListener
                            } else {
                                Log.i(TAG,"WP2: Mission konnte nicht hochgeladen werden: "
                                        + djiWaypointV2Error.getDescription());
                            }
                        }
                    });
                }
                break;
            }
            default: {
                break;
            }
        }
    }

    private WaypointV2Mission createTestWV2PMission() {
        waypointV2MissionBuilder = new WaypointV2Mission.Builder();

        // Waypoint 0: (0,30)
        WaypointV2 waypoint0 = new WaypointV2.Builder()
                .setCoordinate(new LocationCoordinate2D(baseLatitude, baseLongitude + VERTICAL_DISTANCE * ONE_METER_OFFSET))
                .setAltitude(baseAltitude)
                .setFlightPathMode(WaypointV2MissionTypes.WaypointV2FlightPathMode.GOTO_POINT_STRAIGHT_LINE_AND_STOP)
                .setHeadingMode(WaypointV2MissionTypes.WaypointV2HeadingMode.AUTO)
                .build();
        waypointV2List.add(waypoint0);

        // Waypoint 1: (30,30)
        WaypointV2 waypoint1 = new WaypointV2.Builder()
                .setCoordinate(new LocationCoordinate2D(baseLatitude + HORIZONTAL_DISTANCE * ONE_METER_OFFSET, baseLongitude + VERTICAL_DISTANCE * ONE_METER_OFFSET))
                .setAltitude(baseAltitude)
                .setFlightPathMode(WaypointV2MissionTypes.WaypointV2FlightPathMode.GOTO_POINT_STRAIGHT_LINE_AND_STOP)
                .setHeadingMode(WaypointV2MissionTypes.WaypointV2HeadingMode.AUTO)
                .build();
        waypointV2List.add(waypoint1);

        // Waypoint 2: (30,0)
        WaypointV2 waypoint2 = new WaypointV2.Builder()
                .setCoordinate(new LocationCoordinate2D(baseLatitude + HORIZONTAL_DISTANCE * ONE_METER_OFFSET, baseLongitude))
                .setAltitude(baseAltitude)
                .setFlightPathMode(WaypointV2MissionTypes.WaypointV2FlightPathMode.GOTO_POINT_STRAIGHT_LINE_AND_STOP)
                .setHeadingMode(WaypointV2MissionTypes.WaypointV2HeadingMode.AUTO)
                .build();
        waypointV2List.add(waypoint2);

        // Waypoint 3: (60,0)
        WaypointV2 waypoint3 = new WaypointV2.Builder()
                .setCoordinate(new LocationCoordinate2D(baseLatitude + HORIZONTAL_DISTANCE * ONE_METER_OFFSET * 2, baseLongitude))
                .setAltitude(baseAltitude)
                .setFlightPathMode(WaypointV2MissionTypes.WaypointV2FlightPathMode.GOTO_POINT_STRAIGHT_LINE_AND_STOP)
                .setHeadingMode(WaypointV2MissionTypes.WaypointV2HeadingMode.AUTO)
                .build();
        waypointV2List.add(waypoint3);

        // Waypoint 4: (60,30)
        WaypointV2 waypoint4 = new WaypointV2.Builder()
                .setCoordinate(new LocationCoordinate2D(baseLatitude + HORIZONTAL_DISTANCE * ONE_METER_OFFSET * 2, baseLongitude + VERTICAL_DISTANCE * ONE_METER_OFFSET))
                .setAltitude(baseAltitude)
                .setFlightPathMode(WaypointV2MissionTypes.WaypointV2FlightPathMode.GOTO_POINT_STRAIGHT_LINE_AND_STOP)
                .setHeadingMode(WaypointV2MissionTypes.WaypointV2HeadingMode.AUTO)
                .build();
        waypointV2List.add(waypoint4);

        // Waypoint 5: (90,30)
        WaypointV2 waypoint5 = new WaypointV2.Builder()
                .setCoordinate(new LocationCoordinate2D(baseLatitude + HORIZONTAL_DISTANCE * ONE_METER_OFFSET * 3, baseLongitude + VERTICAL_DISTANCE * ONE_METER_OFFSET))
                .setAltitude(baseAltitude)
                .setFlightPathMode(WaypointV2MissionTypes.WaypointV2FlightPathMode.GOTO_POINT_STRAIGHT_LINE_AND_STOP)
                .setHeadingMode(WaypointV2MissionTypes.WaypointV2HeadingMode.AUTO)
                .build();
        waypointV2List.add(waypoint5);

        // Waypoint 6: (90,0)
        WaypointV2 waypoint6 = new WaypointV2.Builder()
                .setCoordinate(new LocationCoordinate2D(baseLatitude + HORIZONTAL_DISTANCE * ONE_METER_OFFSET * 3, baseLongitude))
                .setAltitude(baseAltitude)
                .setFlightPathMode(WaypointV2MissionTypes.WaypointV2FlightPathMode.GOTO_POINT_STRAIGHT_LINE_AND_STOP)
                .setHeadingMode(WaypointV2MissionTypes.WaypointV2HeadingMode.AUTO)
                .build();
        waypointV2List.add(waypoint6);

        // Waypoint 7: (0,0)
        WaypointV2 waypoint7 = new WaypointV2.Builder()
                .setCoordinate(new LocationCoordinate2D(baseLatitude, baseLongitude))
                .setAltitude(baseAltitude)
                .setFlightPathMode(WaypointV2MissionTypes.WaypointV2FlightPathMode.GOTO_POINT_STRAIGHT_LINE_AND_STOP)
                .setHeadingMode(WaypointV2MissionTypes.WaypointV2HeadingMode.AUTO)
                .build();
        waypointV2List.add(waypoint7);

        waypointV2MissionBuilder = new WaypointV2Mission.Builder();
        waypointV2MissionBuilder.setMissionID(new Random().nextInt(65535))
                .setMaxFlightSpeed(15f)
                .setAutoFlightSpeed(5f)
                .setFinishedAction(WaypointV2MissionTypes.MissionFinishedAction.AUTO_LAND)
                .setGotoFirstWaypointMode(WaypointV2MissionTypes.MissionGotoWaypointMode.SAFELY)
                .setExitMissionOnRCSignalLostEnabled(true)
                .setRepeatTimes(1)
                .addwaypoints(waypointV2List);

        return waypointV2MissionBuilder.build();
    }

    private WaypointMission createTestWPMission() {
        WaypointMission.Builder builder = new WaypointMission.Builder();

        builder.autoFlightSpeed(5f);
        builder.maxFlightSpeed(10f);
        builder.setExitMissionOnRCSignalLostEnabled(false);
        builder.finishedAction(WaypointMissionFinishedAction.NO_ACTION);
        builder.flightPathMode(WaypointMissionFlightPathMode.NORMAL);
        builder.gotoFirstWaypointMode(WaypointMissionGotoWaypointMode.SAFELY);
        //builder.setPointOfInterest(new LocationCoordinate2D(15,15));
        builder.headingMode(WaypointMissionHeadingMode.AUTO);
        builder.repeatTimes(1);

        //Waypoint 0: (0,0)
        Waypoint waypoint0 = new Waypoint(baseLatitude, baseLongitude, baseAltitude);
        waypoint0.turnMode = WaypointTurnMode.CLOCKWISE;
        waypoint0.addAction(new WaypointAction(WaypointActionType.ROTATE_AIRCRAFT,0 + calculateTurnAngle()));
        waypoint0.addAction(new WaypointAction(WaypointActionType.START_TAKE_PHOTO, 1000));
        builder.addWaypoint(waypoint0);

        // Waypoint 1: (0,30)
        Waypoint waypoint1 = new Waypoint(baseLatitude, baseLongitude + HORIZONTAL_DISTANCE * ONE_METER_OFFSET, baseAltitude);
        waypoint0.turnMode = WaypointTurnMode.COUNTER_CLOCKWISE;
        waypoint1.addAction(new WaypointAction(WaypointActionType.ROTATE_AIRCRAFT, 0 - calculateTurnAngle()));
        waypoint1.addAction(new WaypointAction(WaypointActionType.START_TAKE_PHOTO, 0));
        builder.addWaypoint(waypoint1);

        // Waypoint 2: (30,30)
        Waypoint waypoint2 = new Waypoint(baseLatitude + VERTICAL_DISTANCE * ONE_METER_OFFSET, baseLongitude + HORIZONTAL_DISTANCE * ONE_METER_OFFSET, baseAltitude);
        waypoint0.turnMode = WaypointTurnMode.COUNTER_CLOCKWISE;
        waypoint2.addAction(new WaypointAction(WaypointActionType.ROTATE_AIRCRAFT, -180 + calculateTurnAngle()));
        waypoint2.addAction(new WaypointAction(WaypointActionType.START_TAKE_PHOTO, 0));
        builder.addWaypoint(waypoint2);

        // Waypoint 3: (30,0)
        Waypoint waypoint3 = new Waypoint(baseLatitude + VERTICAL_DISTANCE * ONE_METER_OFFSET, baseLongitude, baseAltitude);
        waypoint0.turnMode = WaypointTurnMode.COUNTER_CLOCKWISE;
        waypoint3.addAction(new WaypointAction(WaypointActionType.ROTATE_AIRCRAFT, 180 - calculateTurnAngle()));
        waypoint3.addAction(new WaypointAction(WaypointActionType.START_TAKE_PHOTO, 0));
        builder.addWaypoint(waypoint3);

        return builder.build();
    }

    private int calculateTurnAngle() {
        return Math.round((float)Math.toDegrees(Math.atan(VERTICAL_DISTANCE/ HORIZONTAL_DISTANCE)));
    }
}
