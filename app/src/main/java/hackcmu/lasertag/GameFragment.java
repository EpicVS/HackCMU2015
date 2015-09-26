package hackcmu.lasertag;


import android.app.Activity;
import android.app.Fragment;
import android.content.Context;
import android.os.Bundle;
        import android.view.LayoutInflater;
        import android.view.View;
        import android.view.ViewGroup;
        import android.widget.FrameLayout;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.vision.CameraSource;
import com.mirasense.scanditsdk.interfaces.ScanditSDKCode;
import com.mirasense.scanditsdk.interfaces.ScanditSDKScanSession;

import java.util.List;

import static java.lang.System.loadLibrary;


public class GameFragment extends Fragment {

    static {
        loadLibrary("scanditsdk-android-4.8.1");
    }
    private CameraSource mSource;

    private TextView redScore, blueScore;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    public static GameFragment newInstance() {
        GameFragment fragment = new GameFragment();

        return fragment;
    }

    public GameFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_game, container, false);

        redScore = (TextView) view.findViewById(R.id.red_team_score);
        blueScore= (TextView) view.findViewById(R.id.blue_team_score);

        // Inflate the layout for this fragment
        ScanditView scanditView = new ScanditView(this.getActivity()) { //TODO: CHANGE
            public void onScan(ScanditSDKScanSession scanditSDKScanSession, Activity a) {
                List<ScanditSDKCode> codes = scanditSDKScanSession.getAllCodes();
                if (codes.size() > 0) {
                    Context context = a.getApplicationContext();
                    ScanditSDKCode code = codes.get(codes.size() - 1);
                    onShoot(code.getData());

                    this.pauseScanning();
                }
            }
        };
        ((FrameLayout) view.findViewById(R.id.placeholder)).addView(scanditView);



        return view;
    }

    public void updateScore(int red, int blue) {
        if(redScore!=null && blueScore!=null){
            redScore.setText(red+"");
            blueScore.setText(blue+"");
        }

    }

    public void onShoot(String barcode){
        ((JoinGameActivity) getActivity()).sendShotBarcode(barcode);
    }

    public void gameOver(){
        TextView t = new TextView(getContext());
        t.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
        t.setTextSize(60);
        t.setText("Game Over");

        ((FrameLayout) getActivity().findViewById(R.id.placeholder)).addView(t);
    }

}