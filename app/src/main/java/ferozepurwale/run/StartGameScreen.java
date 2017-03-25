package ferozepurwale.run;

import android.content.Intent;
import android.os.Bundle;
import android.os.CountDownTimer;
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

public class StartGameScreen extends AppCompatActivity {

    public static final MediaType JSON = MediaType.parse("application/json; charset=utf-8");
    private static final String TAG = "PlayScreen";
    private static final String START_URL = "http://10.196.13.169:8080/start/";
    private final OkHttpClient client = new OkHttpClient();
    private Boolean stopStartRequests = false;
    Handler handler = new Handler();
    private final int delay = 5000; //milliseconds

    private EditText name, email, opponent;
    private TextView nameTextView, emailTextView, opponentTextView, gameStartText, start;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_play_screen);
        name = (EditText) findViewById(R.id.name);
        email = (EditText) findViewById(R.id.email);
        opponent = (EditText) findViewById(R.id.opponent);
        nameTextView = (TextView) findViewById(R.id.nameTextView);
        emailTextView = (TextView) findViewById(R.id.emailTextView);
        opponentTextView = (TextView) findViewById(R.id.opponentTextView);
        gameStartText = (TextView) findViewById(R.id.gameStartText);
        start = (TextView) findViewById(R.id.start);

        gameStartText.setVisibility(View.INVISIBLE);
        opponent.setVisibility(View.INVISIBLE);
        opponentTextView.setVisibility(View.INVISIBLE);

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
                        stopStartRequests = true;
                        startCountdown(opponent_name, wait);
                    }
                }
            }
        });
    }

    private void startCountdown(String opponent_name, int wait) {
        MyRunnable runnable = new MyRunnable();
        runnable.setData(opponent_name, wait);
        runOnUiThread(runnable);


    }

    public class MyRunnable implements Runnable {
        private String opponent_name;
        private int wait;
        private void setData(String opponent_name, int wait) {
            this.opponent_name = opponent_name;
            this.wait = wait;
        }

        public void run() {
            Log.d(TAG, "aaya");
            gameStartText.setText("Game starts in...");
            opponent.setVisibility(View.VISIBLE);
            opponentTextView.setVisibility(View.VISIBLE);
            opponent.setText(opponent_name);
            opponent.setEnabled(false);

            new CountDownTimer(wait*1000, 1000) {

                public void onTick(long millisUntilFinished) {
                    start.setText(String.valueOf(millisUntilFinished / 1000));
                }

                public void onFinish() {
                    Log.d(TAG, "done");
                    Intent intent = new Intent(StartGameScreen.this, GameScreen.class);
                    intent.putExtra("opponent_name", opponent_name);
                    startActivity(intent);
                    handler.removeCallbacks(requestHandler);
                    finish();
                }

            }.start();

        }
    }

    public void onStartGameClick(View view) {

        email.setVisibility(View.INVISIBLE);
        emailTextView.setVisibility(View.INVISIBLE);
        email.setEnabled(false);
        name.setEnabled(false);
        gameStartText.setVisibility(View.VISIBLE);
        gameStartText.setText("Finding players online...");
        start.setEnabled(false);
        requestHandler.run();
    }

    public Runnable requestHandler = new Runnable() {
        @Override
        public void run() {
            Log.d(TAG, "call");
            if (!stopStartRequests) {
                startGame(name.getText().toString(), email.getText().toString());
                handler.postDelayed(this, delay);
            } else
                handler.removeCallbacks(requestHandler);
        }
    };
}
