package ferozepurwale.run;

import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
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
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.TimeUnit;


public class GameScreen extends AppCompatActivity {

    public static final MediaType JSON = MediaType.parse("application/json; charset=utf-8");
    private static final String PHOTO_URL = "http://10.196.16.16:8080/photo/";
    private static final String REFRESH_URL = "http://10.196.16.16:8080/refresh/";
    private static final String TAG = "GameScreen";
    private final OkHttpClient client = new OkHttpClient();
    private final int delay = 5000; //milliseconds
    Handler handler = new Handler();
    public Runnable refreshRequestHandler = new Runnable() {
        @Override
        public void run() {
            Log.d(TAG, "refresh");
            String jsonData = "{" + "\"name\": \"" + "Nihal Singh" + "\","
                    + "\"email\": \"" + "nihal.111@gmail.com" + "\""
                    + "}";

            Log.d(TAG, jsonData);

            RequestBody body = RequestBody.create(JSON, jsonData);

            Request request = new com.squareup.okhttp.Request.Builder()
                    .url(REFRESH_URL)
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
                        Log.d(TAG, "Response from " + REFRESH_URL + ": " + jsonData);

                        JsonObject jobj = new Gson().fromJson(jsonData, JsonObject.class);
                        int score = jobj.get("score").getAsInt();
                        int win = jobj.get("win").getAsInt();
                        int opponent_score = jobj.get("opponent_score").getAsInt();
                        int total_photos = jobj.get("total_photos").getAsInt();

                        RefreshRunnable runnable = new RefreshRunnable();
                        runnable.setData(score, opponent_score, total_photos);
                        runOnUiThread(runnable);
                        Log.d(TAG, "score: " + score + " win: " + win + " opponent_score: " + opponent_score);
                    }
                }
            });
            handler.postDelayed(this, delay);
        }
    };
    private String opponent_name;
    private long startTime = 0;
    private ImageView gameImage;
    private TextView my_scoreTV, opponent_scoreTV;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_game_screen);

        opponent_name = getIntent().getStringExtra("opponent_name");
        gameImage = (ImageView) findViewById(R.id.gameImage);

        my_scoreTV = (TextView) findViewById(R.id.my_score);
        opponent_scoreTV = (TextView) findViewById(R.id.opponent_score);

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

                    PhotoRunnable runnable = new PhotoRunnable();
                    runnable.setData(photo_url, total_photos);
                    runOnUiThread(runnable);

                    handler.removeCallbacks(refreshRequestHandler);
                    refreshRequestHandler.run();
                }
            }
        });
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

    public class PhotoRunnable implements Runnable {
        private String photo_url;
        private int total_photos;

        private void setData(String photo_url, int total_photos) {
            this.photo_url = photo_url;
            this.total_photos = total_photos;
        }

        public void run() {
            Picasso.with(GameScreen.this).load(photo_url).into(gameImage);
        }
    }

    public class RefreshRunnable implements Runnable {
        private int opponent_score, score, total;

        private void setData(int score, int opponent_score, int total) {
            this.score = score;
            this.opponent_score = opponent_score;
            this.total = total;
        }

        public void run() {
            my_scoreTV.setText((score > 0 ? score : 0) + "/" + total);
            opponent_scoreTV.setText((opponent_score > 0 ? score : 0) + "/" + total);
        }
    }

}
