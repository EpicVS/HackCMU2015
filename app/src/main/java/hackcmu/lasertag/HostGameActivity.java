package hackcmu.lasertag;

import android.app.Activity;
import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.nearby.Nearby;
import com.google.android.gms.nearby.connection.AppIdentifier;
import com.google.android.gms.nearby.connection.AppMetadata;
import com.google.android.gms.nearby.connection.Connections;
import com.google.android.gms.nearby.connection.ConnectionsStatusCodes;

import java.util.ArrayList;
import java.util.List;


public class HostGameActivity extends Activity implements
    GoogleApiClient.ConnectionCallbacks,
    GoogleApiClient.OnConnectionFailedListener,
    Connections.ConnectionRequestListener,
    Connections.MessageListener {

    // Identify if the device is the host
    private boolean mIsHost = true;

    private GoogleApiClient mGoogleApiClient;

    private static int[] NETWORK_TYPES = {ConnectivityManager.TYPE_WIFI,
            ConnectivityManager.TYPE_ETHERNET};

    private boolean isConnectedToNetwork() {
        ConnectivityManager connManager =
                (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        for (int networkType : NETWORK_TYPES) {
            NetworkInfo info = connManager.getNetworkInfo(networkType);
            if (info != null && info.isConnectedOrConnecting()) {
                return true;
            }
        }
        return false;
    }

    private void startAdvertising() {
        if (!isConnectedToNetwork()) {
            // Implement logic when device is not connected to a network
        }
        Log.d("Host", "isConnectedToNetwork terminated (good)");

        // Advertising with an AppIdentifer lets other devices on the
        // network discover this application and prompt the user to
        // install the application.
        List<AppIdentifier> appIdentifierList = new ArrayList<>();
        appIdentifierList.add(new AppIdentifier(getPackageName()));
        AppMetadata appMetadata = new AppMetadata(appIdentifierList);

        // The advertising timeout is set to run indefinitely
        // Positive values represent timeout in milliseconds
        long NO_TIMEOUT = 0L;

        String name = "BACK"; // TODO: change this to the user-input game name
        Nearby.Connections.startAdvertising(mGoogleApiClient, name, appMetadata, NO_TIMEOUT,
                this).setResultCallback(new ResultCallback<Connections.StartAdvertisingResult>() {
            @Override
            public void onResult(Connections.StartAdvertisingResult result) {
                if (result.getStatus().isSuccess()) {
                    // Device is advertising
                    Log.d("Host", "Now advertising.");
                } else {
                    int statusCode = result.getStatus().getStatusCode();
                    // Advertising failed - see statusCode for more details
                    Log.d("Host", "Advertising failed.");
                }
            }
        });
    }

    @Override
    public void onConnectionRequest(final String remoteEndpointId, String remoteDeviceId,
                                    String remoteEndpointName, byte[] payload) {
        Log.d("Host", "Connection requested.");
        if (mIsHost) {
            byte[] myPayload = null;
            // Automatically accept all requests
            Nearby.Connections.acceptConnectionRequest(mGoogleApiClient, remoteEndpointId,
                    myPayload, this).setResultCallback(new ResultCallback<Status>() {
                @Override
                public void onResult(Status status) {
                    if (status.isSuccess()) {
                        // is success
                    } else {
                        // is failure
                    }
                }
            });
        } else {
            // Clients should not be advertising and will reject all connection requests.
            Nearby.Connections.rejectConnectionRequest(mGoogleApiClient, remoteEndpointId);
        }
    }

    @Override
    public void onDisconnected(String endpointId) {
        // handle disconnection
        Log.d("Host", "onDisconnected() called");
    }

    @Override
    public void onMessageReceived(String endpointId, byte[] payload, boolean isReliable) {
        // Implement parsing logic to process message
        Log.d("Host", "Message received");
        Log.d("Host ", new String(payload));
    }

    @Override
    public void onConnectionSuspended(int i) {
        Log.d("Host", "onConnectionSuspended() called");
        // add logic about handling suspended connection

        // Try to re-connect
        mGoogleApiClient.reconnect();
    }

    @Override
    public void onConnected(Bundle bundle) {
        // connected now -- maybe do stuff
        Log.d("Host", "GoogleAPIs connected.");
        startAdvertising();
        Log.d("Host", "Advertising started.");
    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {
        // connection failed -- do something maybe
        Log.d("Host", "onConnectionFailed() called");
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_host_game);

        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(Nearby.CONNECTIONS_API)
                .build();
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
    }

    @Override
    public void onStart() {
        super.onStart();
        mGoogleApiClient.connect();
        Log.d("Host", "Activity started.");
    }

    @Override
    public void onStop() {
        super.onStop();
        if (mGoogleApiClient != null && mGoogleApiClient.isConnected()) {
            mGoogleApiClient.disconnect();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_host_game, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}
