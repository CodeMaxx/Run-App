package ferozepurwale.run;

import android.content.Intent;
import android.os.CountDownTimer;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.squareup.okhttp.Callback;
import com.squareup.okhttp.MediaType;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.RequestBody;
import com.squareup.picasso.Picasso;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.TimeUnit;


public class GameScreen extends AppCompatActivity {

    public static final MediaType JSON = MediaType.parse("application/json; charset=utf-8");
    private static final String PHOTO_URL = "http://10.196.16.16:8080/photo/";
    private final OkHttpClient client = new OkHttpClient();
    private static final String TAG = "GameScreen";
    private String opponent_name;
    private long startTime = 0;
    private ImageView gameImage;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_game_screen);

        opponent_name = getIntent().getStringExtra("opponent_name");
        gameImage = (ImageView) findViewById(R.id.gameImage);
        startTimer();

        getPhoto();
    }

    private void getPhoto() {
        String jsonData = "{" + "\"name\": \"" + "Nihal Singh" + "\","
                + "\"email\": \"" + "nihal.111@gmail.com" + "\""
                + "}";

        Log.d(TAG, jsonData);

        RequestBody body = RequestBody.create(JSON, jsonData);

        Request request = new com.squareup.okhttp.Request.Builder()
                .url(PHOTO_URL)
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
                    Log.d(TAG, "Response from " + PHOTO_URL + ": " + jsonData);

                    JsonObject jobj = new Gson().fromJson(jsonData, JsonObject.class);
                    String photo_url = jobj.get("photo_url").getAsString();
                    int total_photos = jobj.get("total_photos").getAsInt();
                    int win = jobj.get("win").getAsInt();

                    MyRunnable runnable = new MyRunnable();
                    runnable.setData(photo_url);
                    runOnUiThread(runnable);
                }
            }
        });
    }

    public class MyRunnable implements Runnable {
        private String photo_url;
        private void setData(String photo_url) {
            this.photo_url = photo_url;
        }

        public void run() {
            Picasso.with(GameScreen.this).load(photo_url).into(gameImage);
        }
    }


    private void startTimer() {
        Timer stopwatchTimer = new Timer();
        startTime = System.currentTimeMillis();
        stopwatchTimer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        TextView timerTextView = (TextView) findViewById(R.id.stopwatch_view);
                        timerTextView.setText(stopwatch());
                    }
                });

            }
        }, 0, 1000);
    }

    private String stopwatch() {
        long nowTime = System.currentTimeMillis();
        long millis = nowTime - startTime;
        return String.format("%02d:%02d:%02d", TimeUnit.MILLISECONDS.toHours(millis),
                TimeUnit.MILLISECONDS.toMinutes(millis) - TimeUnit.HOURS.toMinutes(TimeUnit.MILLISECONDS.toHours(millis)),
                TimeUnit.MILLISECONDS.toSeconds(millis) - TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes(millis)));
    }


}
