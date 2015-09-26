package hackcmu.lasertag;


        import android.app.Activity;
import android.app.Fragment;
import android.content.Context;
import android.os.Bundle;
import android.widget.FrameLayout;
import android.widget.Toast;

import com.google.android.gms.vision.CameraSource;
import com.mirasense.scanditsdk.interfaces.ScanditSDKCode;
import com.mirasense.scanditsdk.interfaces.ScanditSDKScanSession;

import java.util.List;

import static java.lang.System.loadLibrary;


/**
 * Created by Nick on 9/26/2015.
 */
public class GameFragment extends Fragment {

        static {
            loadLibrary("scanditsdk-android-4.8.1");
        }
        private CameraSource mSource;
        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            ScanditView scanditView = new ScanditView(this.getActivity()) { //TODO: CHANGE
                public void onScan(ScanditSDKScanSession scanditSDKScanSession, Activity a) {
                    List<ScanditSDKCode> codes = scanditSDKScanSession.getAllCodes();
                    if (codes.size() > 0) {
                        Context context = a.getApplicationContext();
                        ScanditSDKCode code = codes.get(0);
                        Toast toast = Toast.makeText(context, "You have shot " + code.getData() + "!", Toast.LENGTH_SHORT);
                        toast.show();
                        this.pauseScanning();
                    }
                }
            };
            getActivity().setContentView(R.layout.activity_join_game); //TODO: CHANGE
            FrameLayout frameLayout = (FrameLayout) getActivity().findViewById(R.id.placeholder); //TODO: CHANGE
            frameLayout.addView(scanditView);
        }


}
