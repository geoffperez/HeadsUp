package com.pd.trackeye;

import android.Manifest;
import android.content.pm.PackageManager;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.vision.CameraSource;
import com.google.android.gms.vision.Detector;
import com.google.android.gms.vision.MultiProcessor;
import com.google.android.gms.vision.Tracker;
import com.google.android.gms.vision.face.Face;
import com.google.android.gms.vision.face.FaceDetector;

import java.io.IOException;
import java.util.Date;
import java.util.Timer;

public class MainApp extends AppCompatActivity {

    EditText textView;                  // shows eye tracking status / message to user
    MediaPlayer mp;                     // declare media player
    CameraSource cameraSource;          // declare cameraSource
    boolean startWasPressed = false;    // used to check if "start" is pressed
    boolean closeWasPressed = false;    // used to check if "close" is pressed
    long timeLeft;
    private Timer timeNow;
    private Timer timeThen;
    private Timer timeDiff;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);                             // display main view
        mp = MediaPlayer.create(this,R.raw.alarm);                  // create media player
        final Button startButton = findViewById(R.id.startButton); // refers to start button
        final Button closeButton = findViewById(R.id.closeButton); // refers to close button

        // Listen for Start button to be pressed
        startButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
            textView.setVisibility(View.VISIBLE);       // show test once Started
            startWasPressed = true;                     // trigger startWasPressed
            startButton.setVisibility(View.INVISIBLE);  // hide Start button
            closeButton.setVisibility(View.VISIBLE);    // show Close button
            }
        });

        // Listen for close button to be pressed
        closeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) { closeApplication(); }
        });

        // Request permission to use device camera and handle otherwise
        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.CAMERA}, 1);
            Toast.makeText(this, "Grant Permission and restart app", Toast.LENGTH_SHORT).show();
        }
        else {
            textView = findViewById(R.id.textView);
            createCameraSource();
        }
    }//end onCreate

    private class EyesTracker extends Tracker<Face> {

        // Thresholds define the threshold of a face being detected or not
        private final float THRESHOLD; // original value = 0.75f;
        private final float TURNING_THRESHOLD;
        private EyesTracker() { /***************/THRESHOLD = 0.75f;
            TURNING_THRESHOLD = .75f;
        }//end EyesTracker

        @Override
        public void onUpdate(Detector.Detections<Face> detections, Face face) {
            if(startWasPressed){

                // If eyes are determined to be open then update text
                if (face.getIsLeftEyeOpenProbability() > THRESHOLD || face.getIsRightEyeOpenProbability() > THRESHOLD) {
                    showStatus("Eyes Detected and open.");
                    //pauseAlarm();

                    // If face turned too far then notify
                    if(face.getEulerZ() > TURNING_THRESHOLD){
                        showStatus("Face turned away, Play Alert!");
                        playAlarm();
                    }

                }else {
                    showStatus("Eyes Detected and closed, Play Alert!");
                    playAlarm();
                }
            }//end if startWasPressed
        }//end onUpdate

        @Override
        public void onMissing(Detector.Detections<Face> detections) {
            super.onMissing(detections);
            showStatus("Face Not Detected yet!");
            /* Possibly play alarm here? **/
        }//end onMissing

        @Override
        public void onDone() { super.onDone(); } //end onDone

        public void playAlarm() { mp.start(); } //end playAlarm

        /** In progress - Causes no sound to play at all **/
        public void pauseAlarm() { mp.pause(); } //end pauseAlarm

    }//end EyeTracker class

    class FaceTrackerFactory implements MultiProcessor.Factory<Face> {

        // Uncertain if actually used
        private FaceTrackerFactory() { /***************/ }
        @Override
        public Tracker<Face> create(Face face) { return new EyesTracker(); }//end create

    }//end class FaceTrackerFactory

    private void closeApplication(){ // Linked to button press - Does exactly what you think it does
        finish();
        moveTaskToBack(true);
    }//end closeApplication

    public void createCameraSource() {
        FaceDetector detector = new FaceDetector.Builder(this)
                .setTrackingEnabled(true)
                .setClassificationType(FaceDetector.ALL_CLASSIFICATIONS)
                .setMode(FaceDetector.FAST_MODE)
                .build();
        detector.setProcessor(new MultiProcessor.Builder(new FaceTrackerFactory()).build());

        cameraSource = new CameraSource.Builder(this, detector)
                .setRequestedPreviewSize(1024, 768)
                .setFacing(CameraSource.CAMERA_FACING_FRONT)
                .setRequestedFps(30.0f)
                .build();

        try {
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
                // TODO: Consider calling
                //    ActivityCompat#requestPermissions
                // here to request the missing permissions, and then overriding
                //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                //                                          int[] grantResults)
                // to handle the case where the user grants the permission. See the documentation
                // for ActivityCompat#requestPermissions for more details.
                return;
            }
            cameraSource.start();
        }
        catch (IOException e) {
            e.printStackTrace();
        }
    }//end createCameraSource

    @Override
    protected void onResume() {
        super.onResume();
        if (cameraSource != null) {
            try {
                if (ActivityCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
                    // TODO: Consider calling
                    //    ActivityCompat#requestPermissions
                    // here to request the missing permissions, and then overriding
                    //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                    //                                          int[] grantResults)
                    // to handle the case where the user grants the permission. See the documentation
                    // for ActivityCompat#requestPermissions for more details.
                    return;
                }
                cameraSource.start();
            }
            catch (IOException e) {
                e.printStackTrace();
            }//end try
        }//end if cameraSource
    }//end onResume

    // On application pause temporarily give up use of camera
    @Override
    protected void onPause() { // On application pause (If app is minimized)
        super.onPause();
        if (cameraSource!=null) {
            cameraSource.stop();
        }
    }//end onPause

    @Override
    protected void onDestroy() { // Clean up when app closes
        super.onDestroy();
        mp.stop();
        mp.release();
        if (cameraSource!=null) {
            cameraSource.release();
        }
    }//end onDestroy

    // Show status of facial view
    public void showStatus(final String message) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() { textView.setText(message); } // Used to update text notification
        });
    }//end showStatus

    public Timer timeSinceLastUpdate(Timer timeNow, Timer timeThen) {

        this.timeNow = timeNow;
        this.timeThen = timeThen;
        
        return timeDiff;
    }

}//end class MainApp