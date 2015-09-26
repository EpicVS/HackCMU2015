package hackcmu.lasertag;

import android.app.Activity;
import android.content.Context;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Toast;

import com.mirasense.scanditsdk.ScanditSDKAutoAdjustingBarcodePicker;
import com.mirasense.scanditsdk.interfaces.ScanditSDK;
import com.mirasense.scanditsdk.interfaces.ScanditSDKCode;
import com.mirasense.scanditsdk.interfaces.ScanditSDKListener;
import com.mirasense.scanditsdk.interfaces.ScanditSDKOnScanListener;
import com.mirasense.scanditsdk.interfaces.ScanditSDKScanSession;

import java.util.List;

/**
 * Created by Nick on 9/26/2015.
 */
public abstract class ScanditView extends ScanditSDKAutoAdjustingBarcodePicker implements ScanditSDKOnScanListener {

    private final Activity activity;
    public ScanditView(final Activity a) {
        super(a, "ufiMDPMZw4yFJRWaGVCd0tChuTgQcxQdB8VVcP7KZaw", ScanditSDK.CAMERA_FACING_BACK);
        activity = a;
        startScanning();
        pauseScanning();
        setOnTouchListener(new View.OnTouchListener() {
            public boolean onTouch(View view, MotionEvent motionEvent) {
                if (motionEvent.getAction() == MotionEvent.ACTION_DOWN) {
                    ScanditView.this.resumeScanning();
                } else if (motionEvent.getAction() == MotionEvent.ACTION_UP) {
                    ScanditView.this.pauseScanning();
                }
                return true;
            }
        });
        addOnScanListener(this);
    }

    public final void didScan(ScanditSDKScanSession scanditSDKScanSession) {
        onScan(scanditSDKScanSession, activity);
    }

    public abstract void onScan(ScanditSDKScanSession scanditSDKScanSession, final Activity a);
}
