package com.android.finaly;

import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import java.io.IOException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import okhttp3.OkHttpClient;
import okhttp3.Request;

public class MainActivity extends AppCompatActivity {

    static final String TAG = MainActivity.class.toString();
    static Camera camera;
    static ExecutorService cameraExecutor = Executors.newSingleThreadExecutor();
    static ExecutorService arduinoExecutor = Executors.newSingleThreadExecutor();
    String arduinoIP = "192.168.42.5";
    OkHttpClient arduinoClient;
    TextView status;
    Button buttonVideo;
    Button buttonGoForward;
    Button buttonGoBackward;
    Button buttonGoLeft;
    Button buttonGoRight;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_main);
        this.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
        status = findViewById(R.id.textView);
        buttonVideo = findViewById(R.id.video);
        buttonGoForward = findViewById(R.id.up);
        buttonGoBackward = findViewById(R.id.down);
        buttonGoLeft = findViewById(R.id.left);
        buttonGoRight = findViewById(R.id.right);
        status.setText("ready");
        buttonGoForward.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        startGoingForward();
                        break;
                    case MotionEvent.ACTION_MOVE:
                        break;
                    case MotionEvent.ACTION_UP:
                        stopGoing();
                        break;
                    case MotionEvent.ACTION_CANCEL:
                        break;
                }
                return true;
            }
        });
        buttonGoBackward.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        startGoingBackward();
                        break;
                    case MotionEvent.ACTION_MOVE:
                        break;
                    case MotionEvent.ACTION_UP:
                        stopGoing();
                        break;
                    case MotionEvent.ACTION_CANCEL:
                        break;
                }
                return true;
            }
        });
        buttonGoRight.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        goRight();
                        break;
                    case MotionEvent.ACTION_MOVE:
                        break;
                    case MotionEvent.ACTION_UP:
                        stopServo();
                        break;
                    case MotionEvent.ACTION_CANCEL:
                        break;
                }
                return true;
            }
        });
        buttonGoLeft.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        goLeft();
                        break;
                    case MotionEvent.ACTION_MOVE:
                        break;
                    case MotionEvent.ACTION_UP:
                        stopServo();
                        break;
                    case MotionEvent.ACTION_CANCEL:
                        break;
                }
                return true;
            }
        });
        arduinoExecutor.submit(new Runnable() {
            @Override
            public void run() {
                arduinoClient = new OkHttpClient();
            }
        });
        cameraExecutor.submit(new Runnable() {
            @Override
            public void run() {
                try {
                    camera = new Camera(Camera.DEFAULT_IP, Camera.DEFAULT_PORT);
                    MainActivity.this.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            status.setText("connected");
                        }
                    });
                    camera.start();
                    MainActivity.this.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            status.setText("started");
                        }
                    });
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
    }

    public void takePhoto(View view) {
        cameraExecutor.submit(new Runnable() {
            @Override
            public void run() {
                camera.takePhoto();
            }
        });
        status.setText("photo taken");
    }

    public void startStopVideo(View view) {
        if (camera.isRecording()) {
            cameraExecutor.submit(new Runnable() {
                @Override
                public void run() {
                    camera.stopRecording();
                }
            });
            status.setText("recording stopped");
            findViewById(R.id.video).setBackgroundResource(R.drawable.play_icon);
        } else {
            cameraExecutor.submit(new Runnable() {
                @Override
                public void run() { camera.startRecording();
                }
            });
            status.setText("recording started");
            findViewById(R.id.video).setBackgroundResource(R.drawable.pause_icon);
        }
    }

    public void showFiles(View view) {
        Intent intent = new Intent(this, GalleryActivity.class);
        startActivity(intent);
    }

    public void startGoingForward() {
        final Request request = new Request.Builder()
                .url("http://" + arduinoIP + "/motor/1")
                .build();
        arduinoExecutor.submit(new Runnable() {
            @Override
            public void run() {
                try {
                    arduinoClient.newCall(request).execute();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });
        Log.i(TAG, "startGoingForward");
    }

    public void stopGoing() {
        final Request request = new Request.Builder()
                .url("http://" + arduinoIP + "/motor/0")
                .build();
        arduinoExecutor.submit(new Runnable() {
            @Override
            public void run() {
                try {
                    arduinoClient.newCall(request).execute();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });
        Log.i(TAG, "stopGoingForward");
    }

    public void startGoingBackward() {
        final Request request = new Request.Builder()
                .url("http://" + arduinoIP + "/motor/2")
                .build();
        arduinoExecutor.submit(new Runnable() {
            @Override
            public void run() {
                try {
                    arduinoClient.newCall(request).execute();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });
        Log.i(TAG, "startGoingBackward");
    }

    public void goLeft() {
        final Request request = new Request.Builder()
                .url("http://" + arduinoIP + "/servo/right")
                .build();
        arduinoExecutor.submit(new Runnable() {
            @Override
            public void run() {
                try {
                    arduinoClient.newCall(request).execute();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });
        Log.i(TAG, "goLeft");
    }

    public void goRight() {
        final Request request = new Request.Builder()
                .url("http://" + arduinoIP + "/servo/left")
                .build();
        arduinoExecutor.submit(new Runnable() {
            @Override
            public void run() {
                try {
                    arduinoClient.newCall(request).execute();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });
        Log.i(TAG, "goRight");
    }

    public void stopServo() {
        final Request request = new Request.Builder()
                .url("http://" + arduinoIP + "/servo/0")
                .build();
        arduinoExecutor.submit(new Runnable() {
            @Override
            public void run() {
                try {
                    arduinoClient.newCall(request).execute();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });
        Log.i(TAG, "stopServo");
    }

    public void forceStop(View view) {
        stopGoing();
        stopServo();
    }
}
