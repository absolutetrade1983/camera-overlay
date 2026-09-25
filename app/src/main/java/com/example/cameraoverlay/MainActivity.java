package com.example.cameraoverlay;

import android.app.Activity;
import android.content.Intent;
import android.content.res.Configuration;
import android.graphics.Color;
import android.graphics.PixelFormat;
import android.net.Uri;
import android.os.Bundle;
import android.provider.Settings;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends Activity {

    private static final int PICK_IMAGE = 1001;

    private WindowManager windowManager;

    private ImageView overlayImage;
    private LinearLayout controls;

    private WindowManager.LayoutParams imageParams;
    private WindowManager.LayoutParams controlParams;

    private Uri selectedImageUri;

    private int imageSize = 500;
    private int imageX = 0;
    private int imageY = 150;

    private static final int STEP = 30;
    private static final int MOVE_STEP = 40;
    private static final int MIN_SIZE = 150;
    private static final int MAX_SIZE = 1500;

    private float downRawX;
    private float downRawY;
    private int startImageX;
    private int startImageY;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (!Settings.canDrawOverlays(this)) {
            Intent intent = new Intent(
                    Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                    Uri.parse("package:" + getPackageName())
            );
            startActivity(intent);
        }

        showMainScreen();
    }

    private void showMainScreen() {

        LinearLayout layout = new LinearLayout(this);
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.setGravity(Gravity.CENTER);
        layout.setPadding(40, 40, 40, 40);

        TextView title = new TextView(this);
        title.setText("CAMERA OVERLAY");
        title.setTextSize(24);
        title.setTextColor(Color.BLACK);
        title.setGravity(Gravity.CENTER);

        layout.addView(
                title,
                new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT,
                        LinearLayout.LayoutParams.WRAP_CONTENT
                )
        );

        Button selectImage = new Button(this);
        selectImage.setText("SELECT PHOTO");

        layout.addView(selectImage);

        Button startOverlay = new Button(this);
        startOverlay.setText("START OVERLAY");

        layout.addView(startOverlay);

        Button stopOverlay = new Button(this);
        stopOverlay.setText("STOP OVERLAY");

        layout.addView(stopOverlay);

        selectImage.setOnClickListener(v -> openImagePicker());

        startOverlay.setOnClickListener(v -> {

            if (!Settings.canDrawOverlays(this)) {
                Toast.makeText(
                        this,
                        "Please allow Display over other apps",
                        Toast.LENGTH_LONG
                ).show();

                Intent intent = new Intent(
                        Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                        Uri.parse("package:" + getPackageName())
                );

                startActivity(intent);
                return;
            }

            if (selectedImageUri == null) {
                Toast.makeText(
                        this,
                        "First select a photo",
                        Toast.LENGTH_SHORT
                ).show();
                return;
            }

            createOverlay();
        });

        stopOverlay.setOnClickListener(v -> removeOverlay());

        setContentView(layout);
    }

    private void openImagePicker() {

        Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);

        intent.addCategory(Intent.CATEGORY_OPENABLE);
        intent.setType("image/*");

        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        intent.addFlags(Intent.FLAG_GRANT_PERSISTABLE_URI_PERMISSION);

        startActivityForResult(intent, PICK_IMAGE);
    }

    @Override
    protected void onActivityResult(
            int requestCode,
            int resultCode,
            Intent data
    ) {

        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == PICK_IMAGE
                && resultCode == RESULT_OK
                && data != null
                && data.getData() != null) {

            selectedImageUri = data.getData();

            try {

                getContentResolver().takePersistableUriPermission(
                        selectedImageUri,
                        Intent.FLAG_GRANT_READ_URI_PERMISSION
                );

            } catch (Exception ignored) {
            }

            if (overlayImage != null) {
                overlayImage.setImageURI(selectedImageUri);
            }

            Toast.makeText(
                    this,
                    "Photo selected",
                    Toast.LENGTH_SHORT
            ).show();
        }
    }

    private void createOverlay() {

        if (overlayImage != null) {
            Toast.makeText(
                    this,
                    "Overlay already running",
                    Toast.LENGTH_SHORT
            ).show();
            return;
        }

        windowManager =
                (WindowManager) getSystemService(WINDOW_SERVICE);

        // -----------------------------
        // IMAGE OVERLAY
        // -----------------------------

        overlayImage = new ImageView(this);

        overlayImage.setImageURI(selectedImageUri);

        overlayImage.setScaleType(
                ImageView.ScaleType.FIT_CENTER
        );

        overlayImage.setBackgroundColor(
                Color.TRANSPARENT
        );

        imageParams = new WindowManager.LayoutParams(
                imageSize,
                imageSize,
                WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY,
                WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE
                        | WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL,
                PixelFormat.TRANSLUCENT
        );

        imageParams.gravity =
                Gravity.TOP | Gravity.START;

        imageParams.x = imageX;
        imageParams.y = imageY;

        // Make image draggable
        overlayImage.setOnTouchListener(
                new View.OnTouchListener() {

                    @Override
                    public boolean onTouch(
                            View view,
                            MotionEvent event
                    ) {

                        switch (event.getActionMasked()) {

                            case MotionEvent.ACTION_DOWN:

                                downRawX = event.getRawX();
                                downRawY = event.getRawY();

                                startImageX = imageParams.x;
                                startImageY = imageParams.y;

                                return true;

                            case MotionEvent.ACTION_MOVE:

                                float dx =
                                        event.getRawX() - downRawX;

                                float dy =
                                        event.getRawY() - downRawY;

                                imageX =
                                        startImageX + (int) dx;

                                imageY =
                                        startImageY + (int) dy;

                                imageParams.x = imageX;
                                imageParams.y = imageY;

                                updateImage();

                                return true;

                            case MotionEvent.ACTION_UP:

                                return true;
                        }

                        return true;
                    }
                }
        );

        // -----------------------------
        // CONTROL PANEL
        // -----------------------------

        controls = new LinearLayout(this);

        controls.setOrientation(
                LinearLayout.HORIZONTAL
        );

        controls.setGravity(
                Gravity.CENTER
        );

        controls.setPadding(
                8,
                8,
                8,
                8
        );

        controls.setBackgroundColor(
                0xCC222222
        );

        // LEFT
        Button left = makeButton("←");

        // UP
        Button up = makeButton("↑");

        // SMALL
        Button minus = makeButton("−");

        // BIG
        Button plus = makeButton("+");

        // DOWN
        Button down = makeButton("↓");

        // RIGHT
        Button right = makeButton("→");

        // RESET
        Button reset = makeButton("RESET");

        // CLOSE
        Button close = makeButton("X");

        controls.addView(left);
        controls.addView(up);
        controls.addView(minus);
        controls.addView(plus);
        controls.addView(down);
        controls.addView(right);
        controls.addView(reset);
        controls.addView(close);

        // -----------------------------
        // BUTTON ACTIONS
        // -----------------------------

        left.setOnClickListener(v ->
                moveImage(-MOVE_STEP, 0)
        );

        right.setOnClickListener(v ->
                moveImage(MOVE_STEP, 0)
        );

        up.setOnClickListener(v ->
                moveImage(0, -MOVE_STEP)
        );

        down.setOnClickListener(v ->
                moveImage(0, MOVE_STEP)
        );

        minus.setOnClickListener(v ->
                resizeImage(-STEP)
        );

        plus.setOnClickListener(v ->
                resizeImage(STEP)
        );

        reset.setOnClickListener(v -> {

            imageSize = 500;
            imageX = 0;
            imageY = 150;

            imageParams.width = imageSize;
            imageParams.height = imageSize;
            imageParams.x = imageX;
            imageParams.y = imageY;

            updateImage();
        });

        close.setOnClickListener(v ->
                removeOverlay()
        );

        // -----------------------------
        // CONTROL WINDOW
        // -----------------------------

        controlParams = new WindowManager.LayoutParams(
                WindowManager.LayoutParams.WRAP_CONTENT,
                WindowManager.LayoutParams.WRAP_CONTENT,
                WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY,
                WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE
                        | WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL,
                PixelFormat.TRANSLUCENT
        );

        controlParams.gravity =
                Gravity.BOTTOM | Gravity.CENTER_HORIZONTAL;

        controlParams.y = 120;

        // Add image
        windowManager.addView(
                overlayImage,
                imageParams
        );

        // Add controls
        windowManager.addView(
                controls,
                controlParams
        );

        Toast.makeText(
                this,
                "Overlay started",
                Toast.LENGTH_SHORT
        ).show();
    }

    private Button makeButton(String text) {

        Button button = new Button(this);

        button.setText(text);
        button.setTextSize(14);
        button.setTextColor(Color.WHITE);

        button.setPadding(
                8,
                0,
                8,
                0
        );

        return button;
    }

    private void moveImage(
            int dx,
            int dy
    ) {

        if (imageParams == null
                || overlayImage == null) {
            return;
        }

        imageX += dx;
        imageY += dy;

        imageParams.x = imageX;
        imageParams.y = imageY;

        updateImage();
    }

    private void resizeImage(int amount) {

        if (imageParams == null
                || overlayImage == null) {
            return;
        }

        imageSize += amount;

        if (imageSize < MIN_SIZE) {
            imageSize = MIN_SIZE;
        }

        if (imageSize > MAX_SIZE) {
            imageSize = MAX_SIZE;
        }

        imageParams.width = imageSize;
        imageParams.height = imageSize;

        updateImage();
    }

    private void updateImage() {

        if (windowManager == null
                || overlayImage == null
                || imageParams == null) {
            return;
        }

        try {

            windowManager.updateViewLayout(
                    overlayImage,
                    imageParams
            );

        } catch (Exception ignored) {
        }
    }

    private void removeOverlay() {

        if (windowManager == null) {
            return;
        }

        try {

            if (overlayImage != null) {
                windowManager.removeView(overlayImage);
            }

        } catch (Exception ignored) {
        }

        try {

            if (controls != null) {
                windowManager.removeView(controls);
            }

        } catch (Exception ignored) {
        }

        overlayImage = null;
        controls = null;
        imageParams = null;
        controlParams = null;

        Toast.makeText(
                this,
                "Overlay stopped",
                Toast.LENGTH_SHORT
        ).show();
    }

    @Override
    protected void onDestroy() {

        removeOverlay();

        super.onDestroy();
    }
}
