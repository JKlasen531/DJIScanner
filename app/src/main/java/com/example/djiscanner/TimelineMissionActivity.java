package com.example.djiscanner;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import java.util.ArrayList;
import java.util.List;

import dji.common.gimbal.Attitude;
import dji.common.gimbal.Rotation;
import dji.common.mission.hotpoint.HotpointHeading;
import dji.common.mission.hotpoint.HotpointMission;
import dji.common.mission.hotpoint.HotpointStartPoint;
import dji.common.model.LocationCoordinate2D;
import dji.sdk.mission.MissionControl;
import dji.sdk.mission.Triggerable;
import dji.sdk.mission.timeline.TimelineElement;
import dji.sdk.mission.timeline.TimelineEvent;
import dji.sdk.mission.timeline.actions.GimbalAttitudeAction;
import dji.sdk.mission.timeline.actions.GoHomeAction;
import dji.sdk.mission.timeline.actions.GoToAction;
import dji.sdk.mission.timeline.actions.HotpointAction;
import dji.sdk.mission.timeline.actions.ShootPhotoAction;
import dji.sdk.mission.timeline.actions.TakeOffAction;
import dji.sdk.mission.timeline.triggers.BatteryPowerLevelTrigger;
import dji.sdk.mission.timeline.triggers.Trigger;

public class TimelineMissionActivity extends Activity implements View.OnClickListener{

    private Button startMission, stopMission, loadMission;
    private MissionControl missionControl;
    private TimelineElement preElement;
    private TimelineEvent preEvent;
    private double baseLatitude = 22;
    private double baseLongitude = 113;
    private final float baseAltitude = 30.0f;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_timeline_mission);
        init();
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.startMission2: {
                if(MissionControl.getInstance().scheduledCount()>0) {
                    MissionControl.getInstance().startTimeline();
                }
                break;
            }
            case R.id.stopMission2: {
                MissionControl.getInstance().stopTimeline();
                break;
            }
            case R.id.loadMission2: {
                initTimeline();
                break;
            }
            default: {
                break;
            }
        }
    }

    private void init() {
        startMission = findViewById(R.id.startMission2);
        stopMission = findViewById(R.id.stopMission2);
        loadMission = findViewById(R.id.loadMission2);
        startMission.setOnClickListener(this);
        stopMission.setOnClickListener(this);
        loadMission.setOnClickListener(this);
    }

    private void initTimeline() {
        List<TimelineElement> elements = new ArrayList<>();
        missionControl = MissionControl.getInstance();
        final TimelineEvent preEvent = null;

        elements.add(new TakeOffAction());

        Attitude attitude = new Attitude(-30, Rotation.NO_ROTATION, Rotation.NO_ROTATION);
        GimbalAttitudeAction gimbalAction = new GimbalAttitudeAction(attitude);
        gimbalAction.setCompletionTime(2);
        elements.add(gimbalAction);

        elements.add(new GoToAction(new LocationCoordinate2D(baseLatitude, baseLongitude), 10));

        elements.add(ShootPhotoAction.newShootIntervalPhotoAction(3,2));

        //also waypoint missions addable with TimelineMission.elementFromWaypointMission(WaypointMission)

        HotpointMission hotpointMission = new HotpointMission();
        hotpointMission.setHotpoint(new LocationCoordinate2D(baseLatitude, baseLongitude));
        hotpointMission.setAltitude(10);
        hotpointMission.setRadius(10);
        hotpointMission.setAngularVelocity(10);
        HotpointStartPoint startPoint = HotpointStartPoint.NEAREST;
        hotpointMission.setStartPoint(startPoint);
        HotpointHeading heading = HotpointHeading.TOWARDS_HOT_POINT;
        hotpointMission.setHeading(heading);
        elements.add(new HotpointAction(hotpointMission, 360));

        elements.add(new GoHomeAction());

        attitude = new Attitude(0, Rotation.NO_ROTATION, Rotation.NO_ROTATION);
        gimbalAction = new GimbalAttitudeAction(attitude);
        gimbalAction.setCompletionTime(2);
        elements.add(gimbalAction);

        if (missionControl.scheduledCount() > 0) {
            missionControl.unscheduleEverything();
            missionControl.removeAllListeners();
        }

        missionControl.scheduleElements(elements);
    }
}
