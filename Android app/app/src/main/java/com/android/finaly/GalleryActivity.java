package com.android.finaly;

import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

public class GalleryActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_gallery);
        this.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
        initialize();
    }

    private void initialize() {
        LinearLayout linearLayout = findViewById(R.id.file_listing);
        linearLayout.removeAllViews();
        Future<String[]> future = MainActivity.cameraExecutor.submit(new Callable<String[]>() {
            @Override
            public String[] call() {
                return MainActivity.camera.listing();
            }
        });
        String[] fileListing = new String[1];
        try {
            fileListing = future.get();
        } catch (ExecutionException e) {
            Log.e("initialize", "Execution error");
            Log.d("initialize", e.toString());
        } catch (InterruptedException e) {
            Log.e("initialize", "Interrupted error");
            Log.d("initialize", e.toString());
        }
        for (String s : fileListing) {
            TextView textView = new TextView(this);
            textView.setText(s);
            linearLayout.addView(textView);
        }
    }

    public void changeDirectory(View view) {

        Future<Boolean> future = MainActivity.cameraExecutor.submit(new Callable<Boolean>() {
            @Override
            public Boolean call() {
                return MainActivity.camera.changeDirectory(((EditText) findViewById(R.id.next_directory)).getText().toString());
            }
        });
        boolean result = false;
        try {
            result = future.get();
        } catch (ExecutionException e) {
            Log.e("changeDirectory", "Execution error");
            Log.d("changeDirectory", e.toString());
        } catch (InterruptedException e) {
            Log.e("changeDirectory", "Interrupted error");
            Log.d("changeDirectory", e.toString());
        }
        if (result) {
            initialize();
        }
    }
}
