package com.example.cameraoverlay;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.provider.MediaStore;
import android.widget.Toast;

public class MainActivity extends Activity {

    private static final int CAMERA_REQUEST = 100;
    private static final int GALLERY_REQUEST = 200;

    private final Handler handler = new Handler();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Simple blank screen
        setContentView(R.layout.activity_main);

        // Open front camera
        openFrontCamera();

        // Open Gallery after camera starts
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                openGallery();
            }
        }, 1500);
    }

    private void openFrontCamera() {
        try {
            Intent cameraIntent =
                    new Intent(MediaStore.ACTION_IMAGE_CAPTURE);

            // Request front camera
            cameraIntent.putExtra(
                    "android.intent.extras.CAMERA_FACING",
                    1
            );

            cameraIntent.putExtra(
                    "android.intent.extras.LENS_FACING_FRONT",
                    1
            );

            cameraIntent.putExtra(
                    "android.intent.extra.USE_FRONT_CAMERA",
                    true
            );

            startActivityForResult(
                    cameraIntent,
                    CAMERA_REQUEST
            );

        } catch (Exception e) {
            Toast.makeText(
                    this,
                    "Camera open nahi ho raha",
                    Toast.LENGTH_SHORT
            ).show();
        }
    }

    private void openGallery() {
        try {

            Intent galleryIntent = new Intent(
                    Intent.ACTION_PICK,
                    MediaStore.Images.Media.EXTERNAL_CONTENT_URI
            );

            galleryIntent.setType("image/*");

            startActivityForResult(
                    galleryIntent,
                    GALLERY_REQUEST
            );

        } catch (Exception e) {

            Toast.makeText(
                    this,
                    "Gallery open nahi ho rahi",
                    Toast.LENGTH_SHORT
            ).show();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        // Remove pending Gallery launch
        handler.removeCallbacksAndMessages(null);
    }
}
