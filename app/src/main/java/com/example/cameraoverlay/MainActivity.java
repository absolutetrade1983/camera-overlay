package com.example.cameraoverlay;

import android.app.Activity;
import android.os.Bundle;
import android.graphics.Color;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

public class MainActivity extends Activity {

    FrameLayout root;

    View cameraView;
    ImageView photoView;

    LinearLayout controls;

    TextView status;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Main screen
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
        // PHOTO
        // --------------------------------

        photoView = new ImageView(this);

        photoView.setImageResource(
                getResources().getIdentifier(
                        "test_photo",
                        "drawable",
                        getPackageName()
                )
        );

        photoView.setScaleType(ImageView.ScaleType.FIT_CENTER);

        FrameLayout.LayoutParams photoParams =
                new FrameLayout.LayoutParams(
                        500,
                        500
                );

        photoParams.gravity = Gravity.CENTER;

        root.addView(photoView, photoParams);


        // --------------------------------
        // STATUS
        // --------------------------------

        status = new TextView(this);

        status.setText("PHOTO: FORWARD");
        status.setTextColor(Color.WHITE);
        status.setTextSize(18);
        status.setGravity(Gravity.CENTER);

        status.setBackgroundColor(Color.rgb(0, 120, 0));

        FrameLayout.LayoutParams statusParams =
                new FrameLayout.LayoutParams(
                        300,
                        70
                );

        statusParams.gravity =
                Gravity.TOP | Gravity.CENTER_HORIZONTAL;

        statusParams.topMargin = 40;

        root.addView(status, statusParams);


        // --------------------------------
        // CONTROL BOX
        // --------------------------------

        controls = new LinearLayout(this);

        controls.setOrientation(
                LinearLayout.VERTICAL
        );

        controls.setPadding(
                15, 15, 15, 15
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
                30, 0, 30, 60
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
        // KEEP CONTROLS ON TOP
        // --------------------------------

        controls.bringToFront();

        status.bringToFront();
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
         * Camera comes in front of photo.
         *
         * Therefore photo goes behind
         * the camera layer.
         */

        cameraView.bringToFront();

        // Controls must remain on top
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
