package ferozepurwale.run;

import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.squareup.okhttp.Callback;
import com.squareup.okhttp.MediaType;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.RequestBody;

import java.io.IOException;

public class PlayScreen extends AppCompatActivity {

    public static final MediaType JSON = MediaType.parse("application/json; charset=utf-8");
    private static final String TAG = "PlayScreen";
    private static final String START_URL = "http://10.196.16.16:8080/start/";
    private final OkHttpClient client = new OkHttpClient();
    private Boolean stopStartRequests = false;
    Handler requestsHandler = new Handler();
    private final int delay = 5000; //milliseconds

    EditText name, email, opponent;
    TextView nameTextView, emailTextView, opponentTextView, gameStartText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        name = (EditText) findViewById(R.id.name);
        email = (EditText) findViewById(R.id.email);
        opponent = (EditText) findViewById(R.id.opponent);
        nameTextView = (TextView) findViewById(R.id.nameTextView);
        emailTextView = (TextView) findViewById(R.id.emailTextView);
        opponentTextView = (TextView) findViewById(R.id.opponentTextView);
        gameStartText = (TextView) findViewById(R.id.gameStartText);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_play_screen);

        gameStartText.setVisibility(View.INVISIBLE);

    }



    private void startGame(String Name, String Email) {
        String jsonData = "{" + "\"name\": \"" + "Nihal Singh" + "\","
                + "\"email\": \"" + "nihal.111@gmail.com" + "\""
                + "}";

        Log.d(TAG, jsonData);

        RequestBody body = RequestBody.create(JSON, jsonData);

        Request request = new com.squareup.okhttp.Request.Builder()
                .url(START_URL)
                .post(body)
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(com.squareup.okhttp.Request request, IOException throwable) {
                throwable.printStackTrace();
            }

            @Override
            public void onResponse(com.squareup.okhttp.Response response) throws IOException {
                if (!response.isSuccessful())
                    throw new IOException("Unexpected code " + response);
                else {
                    final String jsonData = response.body().string();
                    Log.d(TAG, "Response from " + START_URL + ": " + jsonData);

                    JsonObject jobj = new Gson().fromJson(jsonData, JsonObject.class);
                    String opponent_name = jobj.get("opponent_name").getAsString();
                    int wait = jobj.get("wait").getAsInt();

                    if (wait>0) {
                        startCountdown(opponent_name, wait);
                        stopStartRequests = true;
                    }
                }
            }
        });
    }

    private void startCountdown(String opponent_name, int wait) {
        email.setVisibility(View.INVISIBLE);
        emailTextView.setVisibility(View.INVISIBLE);
        email.setEnabled(false);
        gameStartText.setVisibility(View.VISIBLE);
        gameStartText.setText("Loading...");

        gameStartText.setText("Game starts in...");
        opponent.setVisibility(View.VISIBLE);
        opponentTextView.setVisibility(View.VISIBLE);
        opponent.setText(opponent_name);
        opponent.setEnabled(false);

    }

    public void onStartGameClick(View view) {
        requestsHandler.postDelayed(new Runnable(){
            public void run(){
                startGame(name.getText().toString(), email.getText().toString());
                if (!stopStartRequests)
                    requestsHandler.postDelayed(this, delay);
            }
        }, delay);
    }
}
