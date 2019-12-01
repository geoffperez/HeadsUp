package com.pd.trackeye;

import android.media.MediaPlayer;
import android.widget.EditText;

import com.google.android.gms.vision.CameraSource;

import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.io.IOException;

import org.mockito.runners.MockitoJUnitRunner;

import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class MainActivityTest {

    private static MainActivity mainActivityUnderTest;

    @BeforeClass
    public static void setUp() {
        mainActivityUnderTest = new MainActivity();
        mainActivityUnderTest.textView = mock(EditText.class);
        mainActivityUnderTest.mp = mock(MediaPlayer.class);
        mainActivityUnderTest.mpT = mock(MediaPlayer.class);
        mainActivityUnderTest.cameraSource = mock(CameraSource.class);
        mainActivityUnderTest.startWasPressed = false;
    }

    @Test
    public void testPlayPing() {
        // Run the test
        mainActivityUnderTest.playPing();

        // Verify the results
        verify(mainActivityUnderTest.mpT).start();
    }

    @Test
    public void testPlayPing_MediaPlayerThrowsIllegalStateException() {
        // Setup
        doThrow(IllegalStateException.class).when(mainActivityUnderTest.mpT).start();

        // Run the test
        mainActivityUnderTest.playPing();

        // Verify the results
    }

    @Test
    public void testPlayAlarm() {
        // Setup

        // Run the test
        mainActivityUnderTest.playAlarm();

        // Verify the results
        verify(mainActivityUnderTest.mp).start();
    }

    @Test
    public void testPlayAlarm_MediaPlayerThrowsIllegalStateException() {
        // Setup
        doThrow(IllegalStateException.class).when(mainActivityUnderTest.mp).start();

        // Run the test
        mainActivityUnderTest.playAlarm();

        // Verify the results
    }

    @Test
    public void testPauseAlarm() {
        // Setup

        // Run the test
        mainActivityUnderTest.pauseAlarm();

        // Verify the results
        verify(mainActivityUnderTest.mp).pause();
    }

    @Test
    public void testPauseAlarm_MediaPlayerThrowsIllegalStateException() {
        // Setup
        doThrow(IllegalStateException.class).when(mainActivityUnderTest.mp).pause();

        // Run the test
        mainActivityUnderTest.pauseAlarm();

        // Verify the results
    }

    @Test
    public void testCreateCameraSource() throws Exception {
        // Setup
        when(mainActivityUnderTest.cameraSource.start()).thenReturn(null);

        // Run the test
        mainActivityUnderTest.createCameraSource();

        // Verify the results
    }

    @Test
    public void testCreateCameraSource_CameraSourceThrowsIOException() throws Exception {
        // Setup
        when(mainActivityUnderTest.cameraSource.start()).thenThrow(IOException.class);

        // Run the test
        mainActivityUnderTest.createCameraSource();

        // Verify the results
    }

    @Test
    public void testShowStatus() {
        // Setup

        // Run the test
        mainActivityUnderTest.showStatus("message");

        // Verify the results
        verify(mainActivityUnderTest.textView).setText("text");
    }
}
