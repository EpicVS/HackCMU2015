package hackcmu.lasertag;


import android.app.Activity;
import android.app.Fragment;
import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.vision.CameraSource;
import com.mirasense.scanditsdk.interfaces.ScanditSDKCode;
import com.mirasense.scanditsdk.interfaces.ScanditSDKScanSession;

import java.util.List;

import static java.lang.System.loadLibrary;


public class CodeScanningFragment extends Fragment {

    static {
        loadLibrary("scanditsdk-android-4.8.1");
    }
    private CameraSource mSource;

    private TextView redScore, blueScore;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    public static CodeScanningFragment newInstance() {
        CodeScanningFragment fragment = new CodeScanningFragment();

        return fragment;
    }

    public CodeScanningFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_code_scanning, container, false);
        // Inflate the layout for this fragment
        ScanditView scanditView = new ScanditView(this.getActivity()) {
            public void onScan(ScanditSDKScanSession scanditSDKScanSession, Activity a) {
                List<ScanditSDKCode> codes = scanditSDKScanSession.getAllCodes();
                if (codes.size() > 0) {
                    Context context = a.getApplicationContext();
                    ScanditSDKCode code = codes.get(0);
                    sendMyBarcode(code.getData());

                    this.stopScanning();
                }
            }
        };
        ((FrameLayout) view.findViewById(R.id.placeholder_scanning)).addView(scanditView);

        return view;
    }

    public void sendMyBarcode(String barcode){
        Toast toast = Toast.makeText(getActivity().getApplicationContext(),
                "Thank You. The game will start when everyone scans their codes.", Toast.LENGTH_LONG);
        toast.show();

        ((JoinGameActivity) getActivity()).sendMyBarcode(barcode);

    }


}