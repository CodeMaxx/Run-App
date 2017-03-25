package ferozepurwale.run;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.squareup.okhttp.Callback;
import com.squareup.okhttp.MediaType;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.RequestBody;
import com.squareup.picasso.Picasso;

import org.opencv.android.BaseLoaderCallback;
import org.opencv.android.LoaderCallbackInterface;
import org.opencv.android.OpenCVLoader;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.TimeUnit;

import static com.squareup.picasso.Picasso.with;


public class GameScreen extends AppCompatActivity {

    public static final MediaType JSON = MediaType.parse("application/json; charset=utf-8");
    static final int REQUEST_TAKE_PHOTO = 1;
    private static final String PHOTO_URL = "http://10.196.16.16:8080/photo/";
    private static final String TAG = "GameScreen";
    private final OkHttpClient client = new OkHttpClient();
    private final String myDir = Environment.getExternalStorageDirectory() + "/Run/";
    String mCurrentPhotoPath;
    private String opponent_name;
    private long startTime = 0;
    private ImageView gameImage;
    private BaseLoaderCallback mLoaderCallback = new BaseLoaderCallback(this) {
        @Override
        public void onManagerConnected(int status) {
            switch (status) {
                case LoaderCallbackInterface.SUCCESS: {
                    Log.i(TAG, "OpenCV loaded successfully");
                    try {

                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
                break;
                default: {
                    super.onManagerConnected(status);
                }
                break;
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_game_screen);

        opponent_name = getIntent().getStringExtra("opponent_name");
        gameImage = (ImageView) findViewById(R.id.gameImage);
        startTimer();

        getPhoto();
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (!OpenCVLoader.initDebug()) {
            Log.d(TAG, "Internal OpenCV library not found. Using OpenCV Manager for initialization");
            OpenCVLoader.initAsync(OpenCVLoader.OPENCV_VERSION_3_2_0, this, mLoaderCallback);
        } else {
            Log.d(TAG, "OpenCV library found inside package. Using it!");
            mLoaderCallback.onManagerConnected(LoaderCallbackInterface.SUCCESS);
        }
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

                    String filename = myDir + "temp.jpg";
                    Bitmap b = Picasso.with(GameScreen.this).load(photo_url).get();
                    FileOutputStream out = null;
                    try {
                        out = new FileOutputStream(filename);
                        b.compress(Bitmap.CompressFormat.JPEG, 85, out);
                    } catch (Exception e) {
                        e.printStackTrace();
                    } finally {
                        try {
                            if (out != null) {
                                out.close();
                            }
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }

                    MyRunnable runnable = new MyRunnable();
                    runnable.setData(photo_url);

                    runOnUiThread(runnable);
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

    public void dispatchTakePictureIntent(View view) {
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        // Ensure that there's a camera activity to handle the intent
        startActivityForResult(takePictureIntent, REQUEST_TAKE_PHOTO);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {
            case REQUEST_TAKE_PHOTO:
                if (resultCode == Activity.RESULT_OK) {
                    Bundle extras = data.getExtras();
                    Bitmap bitmap;
                    try {
                        bitmap = (Bitmap) extras.get("data");
//                        imageView.setImageBitmap(bitmap);
                        ImageCompare imageCompare = new ImageCompare();
                        Log.d("PATH", myDir);
                        Bitmap referenceBitmap = imageCompare.loadImageFromStorage(new File(myDir + "temp.jpg"));
                        String isSame = imageCompare.compareImages(bitmap, referenceBitmap);
                        Toast.makeText(this, String.valueOf(isSame), Toast.LENGTH_LONG).show();
                    } catch (Exception e) {
                        Toast.makeText(this, "Failed to load", Toast.LENGTH_SHORT)
                                .show();
                        Log.e("Camera", e.toString());
                    }
                }
        }
    }

    public class MyRunnable implements Runnable {
        private String photo_url;

        private void setData(String photo_url) {
            this.photo_url = photo_url;
        }

        public void run() {
            with(GameScreen.this).load(photo_url).into(gameImage);
        }
    }


}
