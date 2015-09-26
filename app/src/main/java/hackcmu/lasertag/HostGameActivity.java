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
import android.widget.EditText;
import android.widget.TextView;
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
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.reflect.TypeToken;

import org.json.JSONArray;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;


public class HostGameActivity extends Activity implements
    GoogleApiClient.ConnectionCallbacks,
    GoogleApiClient.OnConnectionFailedListener,
    Connections.ConnectionRequestListener,
    Connections.MessageListener {

    // Identify if the device is the host
    private boolean mIsHost = true;
    private boolean stopped = false;
    private String gameName;
    private GoogleApiClient mGoogleApiClient;
    // each player JSON has attributes endpointId, deviceId, endpointName, barcode, and team
    JsonArray players = new JsonArray();
    boolean allPlayersReady = false;
    // each of the 2 teams has an array of player endpointIds, and score attribute
    JsonObject teamInfo = new JsonObject();

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
        Log.d("Host", "isConnectedToNetwork terminated (good)" + gameName);

        // Advertising with an AppIdentifer lets other devices on the
        // network discover this application and prompt the user to
        // install the application.
        List<AppIdentifier> appIdentifierList = new ArrayList<>();
        appIdentifierList.add(new AppIdentifier(getPackageName()));
        AppMetadata appMetadata = new AppMetadata(appIdentifierList);

        // The advertising timeout is set to run indefinitely
        // Positive values represent timeout in milliseconds
        long NO_TIMEOUT = 0L;

        Nearby.Connections.startAdvertising(mGoogleApiClient, gameName, appMetadata, NO_TIMEOUT,
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

    public void startGame() {
        Gson gson = new Gson();
        JsonObject json = new JsonObject();
        json.addProperty("gameStart", true);
        sendAll(gson.toJson(json));
    }

    public void shootEvent(String targetName, String shooterName,
                            int scoreRed, int scoreBlue,
                            String targetBarcode, String shooterBarcode) {
        Gson gson = new Gson();
        JsonObject json = new JsonObject();
        json.addProperty("target", targetName);
        json.addProperty("shooter", shooterName);
        json.addProperty("scoreRed", scoreRed);
        json.addProperty("scoreBlue", scoreBlue);
        json.addProperty("targetBarcode", targetBarcode);
        json.addProperty("shooterBarcode", shooterBarcode);
        sendAll(gson.toJson(json));
    }

    private void sendAll(String toSend) {
        for (int i = 0; i < players.size(); i++) {
            Nearby.Connections.sendReliableMessage(
                    mGoogleApiClient,
                    players.get(i).getAsJsonObject().get("endpointId").getAsString(),
                    toSend.getBytes()
            );
        }
    }

    @Override
    public void onConnectionRequest(final String remoteEndpointId, final String remoteDeviceId,
                                    final String remoteEndpointName, byte[] payload) {
        Log.d("Host", "Connection requested.");
        if (mIsHost) {
            byte[] myPayload = null;
            // Automatically accept all requests
            Nearby.Connections.acceptConnectionRequest(mGoogleApiClient, remoteEndpointId,
                    myPayload, this).setResultCallback(new ResultCallback<Status>() {
                @Override
                public void onResult(Status status) {
                    if (status.isSuccess() && !stopped) {
                        JsonObject player = new JsonObject();
                        player.addProperty("endpointId", remoteEndpointId);
                        player.addProperty("deviceId", remoteDeviceId);
                        player.addProperty("endpointName", remoteEndpointName);
                        addPlayer(player);
                    } else {
                        Log.d("Host", "Accepting connection failed.");
                    }
                }
            });
        } else {
            // Clients should not be advertising and will reject all connection requests.
            Nearby.Connections.rejectConnectionRequest(mGoogleApiClient, remoteEndpointId);
        }
    }

    private void generateToast(String toastMe) {
        Toast.makeText(getApplicationContext(), toastMe, Toast.LENGTH_SHORT).show();
    }

    private void generateText() {
        String redText = "Red Team:\n";
        String blueText = "Blue Team:\n";
        for (int i = 0; i < players.size(); i++) {
            String s = players.get(i).getAsJsonObject().get("endpointName").getAsString();
            if (players.get(i).getAsJsonObject().has("barcode")) {
                s += " :)";
            }
            s += "\n";
            if (i % 2 == 0) {
                redText += s;
            } else {
                blueText += s;
            }
        }
        ((TextView) findViewById(R.id.red_team_players)).setText(redText);
        ((TextView) findViewById(R.id.blue_team_players)).setText(blueText);
    }

    private void addPlayer(JsonObject player) {
        // players passed into this function will not have known barcodes
        players.add(player);
        // add player to a team
        int redSize = teamInfo.getAsJsonObject("red").getAsJsonArray("endpointIds").size();
        int blueSize = teamInfo.getAsJsonObject("blue").getAsJsonArray("endpointIds").size();
        if (redSize > blueSize) {
            teamInfo.getAsJsonObject("blue").getAsJsonArray("endpointIds").add(player.get("endpointId"));
            player.addProperty("team", "blue");
        } else {
            teamInfo.getAsJsonObject("red").getAsJsonArray("endpointIds").add(player.get("endpointId"));
            player.addProperty("team", "red");
        }
        allPlayersReady = false;
        generateText();
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

        Gson gson = new Gson();
        Type type = new TypeToken<HashMap<String, String>>(){}.getType();
        HashMap<String, String> map = gson.fromJson(new String(payload), type);
        // deserialize JSON message and run through if block:
        // big if block that handles all possible message cases
        if (map.containsKey("myBarcode")) {
            // add this barcode info to the corresponding player
            boolean updated = false;
            int i = 0;
            while (!updated && i < players.size()) {
                JsonObject player = players.get(i).getAsJsonObject();
                if (player.get("endpointId").getAsString().equals(endpointId)) {
                    player.addProperty("barcode", map.get("myBarcode"));
                    updated = true;
                }
                i++;
            }
            // check whether all players are ready
            allPlayersReady = true;
            for (int j = 0; j < players.size(); j++) {
                if (!players.get(j).getAsJsonObject().has("barcode")) {
                    allPlayersReady = false;
                }
            }
            generateText();
            if (stopped && allPlayersReady) {
                startGame();
            }
        } else if (map.containsKey("myTarget")) {
            // update scores
            //  check which team got hit
            String shotBarcode = map.get("myTarget");
            boolean redGotShot = false;
            JsonArray redEndpointIds = teamInfo
                    .get("red").getAsJsonObject()
                    .get("endpointIds").getAsJsonArray();
            for (int i = 0; i < redEndpointIds.size(); i++) {
                if (getBarcodeFromEndpointId(redEndpointIds.get(i).getAsString())
                        .equals(shotBarcode)) {
                    redGotShot = true;
                }
            }
            //  update that team score value
            int redScore = -1;
            redScore = teamInfo.get("red").getAsJsonObject().get("score").getAsInt();
            int blueScore = -1;
            blueScore = teamInfo.get("blue").getAsJsonObject().get("score").getAsInt();

            if (redGotShot) {
                teamInfo.get("blue").getAsJsonObject().remove("score");
                blueScore++;
                teamInfo.get("blue").getAsJsonObject().addProperty("score", blueScore);
            } else {
                teamInfo.get("red").getAsJsonObject().remove("score");
                redScore++;
                teamInfo.get("red").getAsJsonObject().addProperty("score", redScore);
            }
            // send out shoot event
            String targetName = "";
            String shooterName = "";
            String shooterBarcode = "";
            for (int i = 0; i < players.size(); i++) {
                JsonObject o = players.get(i).getAsJsonObject();
                if (o.get("barcode").getAsString().equals(shotBarcode)) {
                    targetName = o.get("endpointName").getAsString();
                }
                if (o.get("endpointId").getAsString().equals(endpointId)) {
                    shooterName = o.get("endpointName").getAsString();
                    shooterBarcode = o.get("barcode").getAsString();
                }
            }
            shootEvent(targetName, shooterName, redScore, blueScore, shotBarcode, shooterBarcode);
            // check if game is over
            int scoreCap = 5;
            if (redScore >= scoreCap || blueScore >= scoreCap) {
                sendAll("{gameEnd:true}");
            }
        } else {
            Toast.makeText(getApplicationContext(), 
                    "Unrecognized message." , Toast.LENGTH_LONG)
                    .show();
        }
    }

    String getBarcodeFromEndpointId(String endpointId) {
        for (int i = 0; i < players.size(); i++) {
            JsonObject player = players.get(i).getAsJsonObject();
            if (player.get("endpointId").getAsString().equals(endpointId)) {
                return player.get("barcode").getAsString();
            }
        }
        return null;
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

        JsonObject redTeam = new JsonObject();
        JsonArray redEndpointIds = new JsonArray();
        redTeam.add("endpointIds", redEndpointIds);
        redTeam.addProperty("score", 0);
        teamInfo.add("red", redTeam);

        JsonObject blueTeam = new JsonObject();
        JsonArray blueEndpointIds = new JsonArray();
        blueTeam.add("endpointIds", blueEndpointIds);
        blueTeam.addProperty("score", 0);
        teamInfo.add("blue", blueTeam);
    }

    public void start() {
        mGoogleApiClient.connect();
        ((TextView) findViewById(R.id.red_team_players)).setText("Red Team:\n");
        ((TextView) findViewById(R.id.blue_team_players)).setText("Blue Team:\n");
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

    public void buttonClick(View view) {
        gameName = ((EditText)findViewById(R.id.host_game_name)).getText().toString();
        start();
    }

    public void stopMorePlayers(View view) {
        stopped = true;
        if (allPlayersReady) {
            startGame();
        }
    }
}
