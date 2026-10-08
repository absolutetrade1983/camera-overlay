package com.example.cameraoverlay;

import android.app.Activity;
import android.os.Bundle;
import android.graphics.Color;
import android.net.Uri;
import android.content.Intent;
import android.provider.Settings;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

public class MainActivity extends Activity {

    private static final int PICK_IMAGE = 1001;

    FrameLayout root;

    View cameraView;
    ImageView photoView;

    LinearLayout controls;

    TextView status;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // --------------------------------
        // MAIN SCREEN
        // --------------------------------

        root = new FrameLayout(this);
        root.setBackgroundColor(Color.BLACK);

        setContentView(root);


        // --------------------------------
        // CAMERA TEST BACKGROUND
        // --------------------------------

        cameraView = new View(this);
        cameraView.setBackgroundColor(Color.DKGRAY);

        FrameLayout.LayoutParams cameraParams =
                new FrameLayout.LayoutParams(
                        FrameLayout.LayoutParams.MATCH_PARENT,
                        FrameLayout.LayoutParams.MATCH_PARENT
                );

        root.addView(cameraView, cameraParams);


        // --------------------------------
        // PHOTO / BACK LAYER
        // --------------------------------

        photoView = new ImageView(this);

        photoView.setImageResource(
                getResources().getIdentifier(
                        "test_photo",
                        "drawable",
                        getPackageName()
                )
        );

        photoView.setScaleType(
                ImageView.ScaleType.FIT_CENTER
        );

        FrameLayout.LayoutParams photoParams =
                new FrameLayout.LayoutParams(
                        500,
                        500
                );

        photoParams.gravity = Gravity.CENTER;

        root.addView(
                photoView,
                photoParams
        );


        // --------------------------------
        // STATUS
        // --------------------------------

        status = new TextView(this);

        status.setText("PHOTO: FORWARD");
        status.setTextColor(Color.WHITE);
        status.setTextSize(18);
        status.setGravity(Gravity.CENTER);

        status.setBackgroundColor(
                Color.rgb(0, 120, 0)
        );

        FrameLayout.LayoutParams statusParams =
                new FrameLayout.LayoutParams(
                        300,
                        70
                );

        statusParams.gravity =
                Gravity.TOP | Gravity.CENTER_HORIZONTAL;

        statusParams.topMargin = 40;

        root.addView(
                status,
                statusParams
        );


        // --------------------------------
        // CONTROL BOX
        // --------------------------------

        controls = new LinearLayout(this);

        controls.setOrientation(
                LinearLayout.VERTICAL
        );

        controls.setPadding(
                15,
                15,
                15,
                15
        );

        controls.setBackgroundColor(
                Color.rgb(55, 55, 55)
        );

        FrameLayout.LayoutParams controlParams =
                new FrameLayout.LayoutParams(
                        FrameLayout.LayoutParams.MATCH_PARENT,
                        FrameLayout.LayoutParams.WRAP_CONTENT
                );

        controlParams.gravity =
                Gravity.BOTTOM;

        controlParams.setMargins(
                30,
                0,
                30,
                20
        );

        root.addView(
                controls,
                controlParams
        );


        // --------------------------------
        // BUTTON ROW
        // --------------------------------

        LinearLayout row =
                new LinearLayout(this);

        row.setOrientation(
                LinearLayout.HORIZONTAL
        );

        controls.addView(
                row,
                new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT,
                        80
                )
        );


        // --------------------------------
        // FORWARD BUTTON
        // --------------------------------

        Button forward =
                new Button(this);

        forward.setText(
                "START FORWARD"
        );

        row.addView(
                forward,
                new LinearLayout.LayoutParams(
                        0,
                        LinearLayout.LayoutParams.MATCH_PARENT,
                        1
                )
        );

        forward.setOnClickListener(
                new View.OnClickListener() {

                    @Override
                    public void onClick(View v) {

                        startForward();

                    }
                }
        );


        // --------------------------------
        // BACKWARD BUTTON
        // --------------------------------

        Button backward =
                new Button(this);

        backward.setText(
                "START BACKWARD"
        );

        row.addView(
                backward,
                new LinearLayout.LayoutParams(
                        0,
                        LinearLayout.LayoutParams.MATCH_PARENT,
                        1
                )
        );

        backward.setOnClickListener(
                new View.OnClickListener() {

                    @Override
                    public void onClick(View v) {

                        startBackward();

                    }
                }
        );


        // --------------------------------
        // CHANGE PHOTO BUTTON
        // --------------------------------

        Button changePhoto =
                new Button(this);

        changePhoto.setText(
                "CHANGE BACK PHOTO"
        );

        controls.addView(
                changePhoto,
                new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT,
                        80
                )
        );

        changePhoto.setOnClickListener(
                new View.OnClickListener() {

                    @Override
                    public void onClick(View v) {

                        openGallery();

                    }
                }
        );


        // --------------------------------
        // KEEP CONTROLS ON TOP
        // --------------------------------

        controls.bringToFront();
        status.bringToFront();


        // --------------------------------
        // OPEN GALLERY AUTOMATICALLY
        // --------------------------------

        root.postDelayed(
                new Runnable() {

                    @Override
                    public void run() {

                        openGallery();

                    }
                },
                400
        );
    }


    // ====================================
    // OPEN GALLERY
    // ====================================

    private void openGallery() {

        Intent intent =
                new Intent(Intent.ACTION_OPEN_DOCUMENT);

        intent.addCategory(
                Intent.CATEGORY_OPENABLE
        );

        intent.setType(
                "image/*"
        );

        startActivityForResult(
                intent,
                PICK_IMAGE
        );
    }


    // ====================================
    // PHOTO SELECTED
    // ====================================

    @Override
    protected void onActivityResult(
            int requestCode,
            int resultCode,
            Intent data) {

        super.onActivityResult(
                requestCode,
                resultCode,
                data
        );

        if (requestCode == PICK_IMAGE &&
                resultCode == RESULT_OK &&
                data != null) {

            Uri selectedImage =
                    data.getData();

            if (selectedImage != null) {

                try {

                    // Keep permission after app restart
                    final int takeFlags =
                            data.getFlags()
                                    & (
                                        Intent.FLAG_GRANT_READ_URI_PERMISSION
                                        | Intent.FLAG_GRANT_WRITE_URI_PERMISSION
                                    );

                    getContentResolver()
                            .takePersistableUriPermission(
                                    selectedImage,
                                    takeFlags
                            );

                } catch (Exception ignored) {
                    // Some gallery providers don't support
                    // persistent URI permissions.
                }

                // Set selected image
                photoView.setImageURI(
                        selectedImage
                );

                photoView.setScaleType(
                        ImageView.ScaleType.FIT_CENTER
                );

                // Automatically use it as BACK LAYER
                startBackward();
            }
        }
    }


    // ====================================
    // FORWARD
    // ====================================

    private void startForward() {

        // Photo comes in front
        photoView.bringToFront();

        // Status and controls stay visible
        status.bringToFront();
        controls.bringToFront();

        status.setText(
                "PHOTO: FORWARD"
        );

        status.setBackgroundColor(
                Color.rgb(0, 140, 0)
        );
    }


    // ====================================
    // BACKWARD
    // ====================================

    private void startBackward() {

        /*
         * Current test structure:
         *
         * Camera layer comes in front.
         * Photo layer stays behind it.
         */

        cameraView.bringToFront();

        // Controls remain visible
        status.bringToFront();
        controls.bringToFront();

        status.setText(
                "PHOTO: BACKWARD"
        );

        status.setBackgroundColor(
                Color.rgb(180, 90, 0)
        );
    }
}
