package com.example.djiscanner;

import android.app.Activity;
import android.graphics.SurfaceTexture;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.TextureView;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.TextureView.SurfaceTextureListener;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import dji.common.camera.SettingsDefinitions;
import dji.common.camera.SystemState;
import dji.common.error.DJIError;
import dji.common.gimbal.Attitude;
import dji.common.gimbal.Rotation;
import dji.common.mission.waypoint.Waypoint;
import dji.common.mission.waypoint.WaypointMission;
import dji.common.mission.waypoint.WaypointMissionFinishedAction;
import dji.common.mission.waypoint.WaypointMissionFlightPathMode;
import dji.common.mission.waypoint.WaypointMissionGotoWaypointMode;
import dji.common.mission.waypoint.WaypointMissionHeadingMode;
import dji.common.product.Model;
import dji.common.useraccount.UserAccountState;
import dji.common.util.CommonCallbacks;
import dji.sdk.flightcontroller.FlightController;
import dji.sdk.base.BaseProduct;
import dji.sdk.camera.Camera;
import dji.sdk.camera.VideoFeeder;
import dji.sdk.codec.DJICodecManager;
import dji.sdk.mission.MissionControl;
import dji.sdk.mission.Triggerable;
import dji.sdk.mission.timeline.TimelineElement;
import dji.sdk.mission.timeline.TimelineEvent;
import dji.sdk.mission.timeline.TimelineMission;
import dji.sdk.mission.timeline.actions.GimbalAttitudeAction;
import dji.sdk.mission.timeline.actions.ShootPhotoAction;
import dji.sdk.mission.timeline.triggers.BatteryPowerLevelTrigger;
import dji.sdk.mission.timeline.triggers.Trigger;
import dji.sdk.mission.timeline.triggers.TriggerEvent;
import dji.sdk.products.Aircraft;
import dji.sdk.remotecontroller.RemoteController;
import dji.sdk.useraccount.UserAccountManager;

public class MainActivity extends Activity implements TextureView.SurfaceTextureListener, OnClickListener{

    private static final String TAG = MainActivity.class.getName();
    private RemoteController remoteController;
    private BaseProduct baseProduct;

    // Codec for video live view
    protected VideoFeeder.VideoDataListener mReceivedVideoDataListener = null;
    protected DJICodecManager mCodecManager = null;

    private MissionControl missionControl;
    private FlightController flightController;
    private TimelineEvent preEvent;
    private TimelineElement preElement;
    private DJIError preError;

    private Trigger.Listener triggerListener = new Trigger.Listener() {
        @Override
        public void onEvent(Trigger trigger, TriggerEvent event, @Nullable DJIError djiError) {
            //things that happen when a trigger occures
        }
    };

    protected TextureView mVideoSurface = null;
    private Button mCaptureBtn, mShootPhotoModeBtn, mRecordVideoModeBtn;
    private ToggleButton mRecordBtn;
    private TextView recordingTime;

    private Handler handler;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        initUI();

        baseProduct = DJIScannerApplication.getProductInstance();
        setContentView(R.layout.activity_main);
        handler = new Handler();

        //Flight and Mission Controller
        if(baseProduct == null || baseProduct.isConnected()) {
            missionControl = null;
        } else {
            missionControl =    MissionControl.getInstance();
            if(DJIScannerApplication.isAircraftConnected()) {
                flightController = ((Aircraft) baseProduct).getFlightController();
            }
        }

        // The callback for receiving the raw H264 video data for camera live view
        mReceivedVideoDataListener = new VideoFeeder.VideoDataListener() {

            @Override
            public void onReceive(byte[] videoBuffer, int size) {
                if (mCodecManager != null) {
                    mCodecManager.sendDataToDecoder(videoBuffer, size);
                }
            }
        };

        //Camera
        Camera camera = DJIScannerApplication.getCameraInstance();
        if (camera != null) {

            camera.setSystemStateCallback(new SystemState.Callback() {
                @Override
                public void onUpdate(SystemState cameraSystemState) {
                    if (null != cameraSystemState) {

                        int recordTime = cameraSystemState.getCurrentVideoRecordingTimeInSeconds();
                        int minutes = (recordTime % 3600) / 60;
                        int seconds = recordTime % 60;

                        final String timeString = String.format("%02d:%02d", minutes, seconds);
                        final boolean isVideoRecording = cameraSystemState.isRecording();

                        MainActivity.this.runOnUiThread(new Runnable() {

                            @Override
                            public void run() {

                                recordingTime.setText(timeString);

                                /*
                                 * Update recordingTime TextView visibility and mRecordBtn's check state
                                 */
                                if (isVideoRecording){
                                    recordingTime.setVisibility(View.VISIBLE);
                                }else
                                {
                                    recordingTime.setVisibility(View.INVISIBLE);
                                }
                            }
                        });
                    }
                }
            });

        }

        //Remote Controller
        if ((null != DJIScannerApplication.getProductInstance())
                && (DJIScannerApplication.getProductInstance() instanceof Aircraft)
                && (null != DJIScannerApplication.getAircraftInstance().getRemoteController())) {
            remoteController = ((Aircraft) DJIScannerApplication.getProductInstance()).getRemoteController();
            if(remoteController.isCustomizableButtonSupported()) {
                setupCustomizableButtons();
            }
        }

    }

    //RemoteControl

    private void setupCustomizableButtons() {
    }

    protected void onProductChange() {
        initPreviewer();
        loginAccount();
    }

    private void loginAccount(){

        UserAccountManager.getInstance().logIntoDJIUserAccount(this,
                new CommonCallbacks.CompletionCallbackWith<UserAccountState>() {
                    @Override
                    public void onSuccess(final UserAccountState userAccountState) {
                        Log.e(TAG, "Login Success");
                    }
                    @Override
                    public void onFailure(DJIError error) {
                        showToast("Login Error:"
                                + error.getDescription());
                    }
                });
    }

    @Override
    public void onResume() {
        Log.e(TAG, "onResume");
        super.onResume();
        initPreviewer();
        onProductChange();

        if(mVideoSurface == null) {
            Log.e(TAG, "mVideoSurface is null");
        }
    }

    @Override
    public void onPause() {
        Log.e(TAG, "onPause");
        uninitPreviewer();
        super.onPause();
    }

    @Override
    public void onStop() {
        Log.e(TAG, "onStop");
        super.onStop();
    }

    public void onReturn(View view){
        Log.e(TAG, "onReturn");
        this.finish();
    }

    @Override
    protected void onDestroy() {
        Log.e(TAG, "onDestroy");
        uninitPreviewer();
        super.onDestroy();
    }

    private void initUI() {
        // init mVideoSurface
        mVideoSurface = (TextureView)findViewById(R.id.video_previewer_surface);

        recordingTime = (TextView) findViewById(R.id.timer);
        mCaptureBtn = (Button) findViewById(R.id.btn_capture);
        mRecordBtn = (ToggleButton) findViewById(R.id.btn_record);
        mShootPhotoModeBtn = (Button) findViewById(R.id.btn_shoot_photo_mode);
        mRecordVideoModeBtn = (Button) findViewById(R.id.btn_record_video_mode);

        if (null != mVideoSurface) {
            mVideoSurface.setSurfaceTextureListener(this);
        }

        mCaptureBtn.setOnClickListener(this);
        mRecordBtn.setOnClickListener(this);
        mShootPhotoModeBtn.setOnClickListener(this);
        mRecordVideoModeBtn.setOnClickListener(this);

        recordingTime.setVisibility(View.INVISIBLE);

        mRecordBtn.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    startRecord();
                } else {
                    stopRecord();
                }
            }
        });
    }

    private void initPreviewer() {

        BaseProduct product = DJIScannerApplication.getProductInstance();

        if (product == null || !product.isConnected()) {
            showToast(getString(R.string.disconnected));
        } else {
            if (null != mVideoSurface) {
                mVideoSurface.setSurfaceTextureListener(this);
            }
            if (!product.getModel().equals(Model.UNKNOWN_AIRCRAFT)) {
                VideoFeeder.getInstance().getPrimaryVideoFeed().addVideoDataListener(mReceivedVideoDataListener);
            }
        }
    }

    private void uninitPreviewer() {
        Camera camera = DJIScannerApplication.getCameraInstance();
        if (camera != null){
            // Reset the callback
            VideoFeeder.getInstance().getPrimaryVideoFeed().addVideoDataListener(null);
        }
    }

    @Override
    public void onSurfaceTextureAvailable(SurfaceTexture surface, int width, int height) {
        Log.e(TAG, "onSurfaceTextureAvailable");
        if (mCodecManager == null) {
            mCodecManager = new DJICodecManager(this, surface, width, height);
        }
    }

    @Override
    public void onSurfaceTextureSizeChanged(SurfaceTexture surface, int width, int height) {
        Log.e(TAG, "onSurfaceTextureSizeChanged");
    }

    @Override
    public boolean onSurfaceTextureDestroyed(SurfaceTexture surface) {
        Log.e(TAG,"onSurfaceTextureDestroyed");
        if (mCodecManager != null) {
            mCodecManager.cleanSurface();
            mCodecManager = null;
        }

        return false;
    }

    @Override
    public void onSurfaceTextureUpdated(SurfaceTexture surface) {
    }

    public void showToast(final String msg) {
        runOnUiThread(new Runnable() {
            public void run() {
                Toast.makeText(MainActivity.this, msg, Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public void onClick(View v) {

        switch (v.getId()) {
            case R.id.btn_capture:{
                captureAction();
                break;
            }
            case R.id.btn_shoot_photo_mode:{
                switchCameraMode(SettingsDefinitions.CameraMode.SHOOT_PHOTO);
                break;
            }
            case R.id.btn_record_video_mode:{
                switchCameraMode(SettingsDefinitions.CameraMode.RECORD_VIDEO);
                break;
            }
            default:
                break;
        }
    }

    //Camera Methods

    private void switchCameraMode(SettingsDefinitions.CameraMode cameraMode){

        Camera camera = DJIScannerApplication.getCameraInstance();
        if (camera != null) {
            camera.setMode(cameraMode, new CommonCallbacks.CompletionCallback() {
                @Override
                public void onResult(DJIError error) {

                    if (error == null) {
                        showToast("Switch Camera Mode Succeeded");
                    } else {
                        showToast(error.getDescription());
                    }
                }
            });
        }
    }

    // Method for taking photo
    private void captureAction(){

        final Camera camera = DJIScannerApplication.getCameraInstance();
        if (camera != null) {

            SettingsDefinitions.ShootPhotoMode photoMode = SettingsDefinitions.ShootPhotoMode.SINGLE; // Set the camera capture mode as Single mode
            camera.setShootPhotoMode(photoMode, new CommonCallbacks.CompletionCallback(){
                @Override
                public void onResult(DJIError djiError) {
                    if (null == djiError) {
                        handler.postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                camera.startShootPhoto(new CommonCallbacks.CompletionCallback() {
                                    @Override
                                    public void onResult(DJIError djiError) {
                                        if (djiError == null) {
                                            showToast("take photo: success");
                                        } else {
                                            showToast(djiError.getDescription());
                                        }
                                    }
                                });
                            }
                        }, 2000);
                    }
                }
            });
        }
    }

    // Method for starting recording
    private void startRecord(){

        final Camera camera = DJIScannerApplication.getCameraInstance();
        if (camera != null) {
            camera.startRecordVideo(new CommonCallbacks.CompletionCallback(){
                @Override
                public void onResult(DJIError djiError)
                {
                    if (djiError == null) {
                        showToast("Record video: success");
                    }else {
                        showToast(djiError.getDescription());
                    }
                }
            }); // Execute the startRecordVideo API
        }
    }

    // Method for stopping recording
    private void stopRecord(){

        Camera camera = DJIScannerApplication.getCameraInstance();
        if (camera != null) {
            camera.stopRecordVideo(new CommonCallbacks.CompletionCallback(){

                @Override
                public void onResult(DJIError djiError)
                {
                    if(djiError == null) {
                        showToast("Stop recording: success");
                    } else {
                        showToast(djiError.getDescription());
                    }
                }
            }); // Execute the stopRecordVideo API
        }

    }

    //TimelineMissionControl

    private void initTimeline() {

        List<TimelineElement> elements = new ArrayList<>();

        missionControl = MissionControl.getInstance();
        final TimelineEvent preEvent = null;
        MissionControl.Listener listener = new MissionControl.Listener() {
            @Override
            public void onEvent(TimelineElement timelineElement, TimelineEvent timelineEvent, DJIError djiError) {
                updateTimelineStatus(timelineElement, timelineEvent, djiError);
            }
        };

        //Mission example steps:
        //reset the gimbal to horizontal angle in 2 seconds
        Attitude attitude = new Attitude(-30, Rotation.NO_ROTATION, Rotation.NO_ROTATION);
        GimbalAttitudeAction gimbalAction = new GimbalAttitudeAction(attitude);
        gimbalAction.setCompletionTime(2);
        elements.add(gimbalAction);

        //take a single photo
        elements.add(ShootPhotoAction.newShootSinglePhotoAction());

        //Missions e.g. Waypoint Missions are also addable to the elements ArrayList

        addBatteryPowerLevelTrigger(missionControl);
    }

    private void updateTimlineStatus(@Nullable TimelineElement element, TimelineEvent event, DJIError error) {

        if(element == preElement && event == preEvent && error == preError) {
            return;
        }

        if(element != null) {
            if(element instanceof TimelineMission) {
                //event
            } else {
                //event
            }
        } else {
            //TimelineEvent
        }

        preEvent = event;
        preElement = element;
        preError = error;
    }

    private WaypointMission initWaypointMission() {
        WaypointMission.Builder waypointMissionBuilder = new WaypointMission.Builder().autoFlightSpeed(5f)
                .maxFlightSpeed(10f)
                .setExitMissionOnRCSignalLostEnabled(false)
                .finishedAction(WaypointMissionFinishedAction.NO_ACTION)
                .flightPathMode(WaypointMissionFlightPathMode.NORMAL)
                .gotoFirstWaypointMode(WaypointMissionGotoWaypointMode.SAFELY)
                .headingMode(WaypointMissionHeadingMode.AUTO)
                .repeatTimes(1);

        List<Waypoint> waypoints = new LinkedList<>();

        //create Waypoints
        //Waypoint waypoint = new Waypoint(...);
        //add Action when Waypoint is reached
        //waypoint.addAction(new WaypointAction(...));

        //add waypoint to waypoints
        //seems deprecated, maybe add Waypoints via waypointMissionBuilder.addWaypoint(waypoint)....;

        waypointMissionBuilder.waypointList(waypoints).waypointCount(waypoints.size());
        return waypointMissionBuilder.build();
    }

    private void startTimeline() {
        if(MissionControl.getInstance().scheduledCount() > 0) {
            MissionControl.getInstance().startTimeline();
        } else {
            //Timeline not initialized yet
        }
    }

    private void stopTimeline() {
        MissionControl.getInstance().stopTimeline();
    }

    private void pauseTimeline() {
        MissionControl.getInstance().pauseTimeline();
    }

    private void resumeTimeline() {
        MissionControl.getInstance().resumeTimeline();
    }

    private void cleanTimelineDataAndLog() {
        if(missionControl.scheduledCount() > 0) {
            missionControl.unscheduleEverything();
            missionControl.removeAllListeners();
        }
        //Logout.setText("");
    }

    private void updateTimelineStatus(TimelineElement timelineElement, TimelineEvent timelineEvent, DJIError djiError) {
    }

    private void addBatteryPowerLevelTrigger(Triggerable triggerTarget) {
        float value = 20.0f;
        BatteryPowerLevelTrigger trigger = new BatteryPowerLevelTrigger();
        trigger.setPowerPercentageTriggerValue(value);
        addTrigger(trigger, triggerTarget, " at level " + value);
    }

    private void addTrigger(Trigger trigger, Triggerable triggerTarget, String additionalComment) {

        if (triggerTarget != null) {

            initTrigger(trigger);
            List<Trigger> triggers = triggerTarget.getTriggers();
            if(triggers == null) {
                triggers = new ArrayList<>();
            }

            triggers.add(trigger);
            triggerTarget.setTriggers(triggers);
        }
    }

    private void initTrigger(Trigger trigger) {
        trigger.addListener(triggerListener);
        trigger.setAction(new Trigger.Action() {
            @Override
            public void onCall() {
            }
        });
    }
}
