package com.example.cameraoverlay;

import android.app.Activity;
import android.content.Intent;
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

    private static final int SIZE_STEP = 30;
    private static final int MOVE_STEP = 40;
    private static final int MIN_SIZE = 150;
    private static final int MAX_SIZE = 1500;

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

        layout.addView(title);

        Button select = new Button(this);
        select.setText("SELECT PHOTO");
        layout.addView(select);

        Button start = new Button(this);
        start.setText("START OVERLAY");
        layout.addView(start);

        Button stop = new Button(this);
        stop.setText("STOP OVERLAY");
        layout.addView(stop);

        select.setOnClickListener(v -> openPicker());

        start.setOnClickListener(v -> {

            if (!Settings.canDrawOverlays(this)) {
                Toast.makeText(
                        this,
                        "Allow Display over other apps first",
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

        stop.setOnClickListener(v -> removeOverlay());

        setContentView(layout);
    }

    private void openPicker() {

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

        super.onActivityResult(
                requestCode,
                resultCode,
                data
        );

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

        // =========================
        // PHOTO
        // =========================

        overlayImage = new ImageView(this);

        overlayImage.setImageURI(selectedImageUri);

        overlayImage.setScaleType(
                ImageView.ScaleType.FIT_CENTER
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

        // PHOTO DRAG
        overlayImage.setOnTouchListener(
                new View.OnTouchListener() {

                    float downX;
                    float downY;
                    int startX;
                    int startY;

                    @Override
                    public boolean onTouch(
                            View v,
                            MotionEvent event
                    ) {

                        if (event.getAction() ==
                                MotionEvent.ACTION_DOWN) {

                            downX = event.getRawX();
                            downY = event.getRawY();

                            startX = imageParams.x;
                            startY = imageParams.y;

                            return true;
                        }

                        if (event.getAction() ==
                                MotionEvent.ACTION_MOVE) {

                            imageX =
                                    startX +
                                    (int)(event.getRawX() - downX);

                            imageY =
                                    startY +
                                    (int)(event.getRawY() - downY);

                            imageParams.x = imageX;
                            imageParams.y = imageY;

                            updateImage();

                            return true;
                        }

                        return true;
                    }
                }
        );

        // =========================
        // CONTROL BOX
        // =========================

        controls = new LinearLayout(this);

        controls.setOrientation(
                LinearLayout.VERTICAL
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
                0xDD333333
        );

        // DRAG HANDLE
        TextView dragHandle = new TextView(this);

        dragHandle.setText("☰  DRAG CONTROL BOX");
        dragHandle.setTextColor(Color.WHITE);
        dragHandle.setTextSize(14);
        dragHandle.setGravity(Gravity.CENTER);
        dragHandle.setPadding(20, 12, 20, 12);

        controls.addView(dragHandle);

        // BUTTON ROW
        LinearLayout row = new LinearLayout(this);

        row.setOrientation(
                LinearLayout.HORIZONTAL
        );

        row.setGravity(
                Gravity.CENTER
        );

        Button left = makeButton("←");
        Button up = makeButton("↑");
        Button minus = makeButton("−");
        Button plus = makeButton("+");
        Button down = makeButton("↓");
        Button right = makeButton("→");
        Button reset = makeButton("RESET");
        Button close = makeButton("X");

        row.addView(left);
        row.addView(up);
        row.addView(minus);
        row.addView(plus);
        row.addView(down);
        row.addView(right);
        row.addView(reset);
        row.addView(close);

        controls.addView(row);

        // =========================
        // BUTTON ACTIONS
        // =========================

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
                resizeImage(-SIZE_STEP)
        );

        plus.setOnClickListener(v ->
                resizeImage(SIZE_STEP)
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

        // =========================
        // CONTROL WINDOW
        // =========================

        controlParams = new WindowManager.LayoutParams(
                WindowManager.LayoutParams.WRAP_CONTENT,
                WindowManager.LayoutParams.WRAP_CONTENT,
                WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY,
                WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
                PixelFormat.TRANSLUCENT
        );

        controlParams.gravity =
                Gravity.TOP | Gravity.START;

        controlParams.x = imageX;
        controlParams.y = imageY + imageSize + 20;

        // =========================
        // DRAG CONTROL BOX
        // =========================

        dragHandle.setOnTouchListener(
                new View.OnTouchListener() {

                    float downX;
                    float downY;
                    int startX;
                    int startY;

                    @Override
                    public boolean onTouch(
                            View v,
                            MotionEvent event
                    ) {

                        if (event.getAction() ==
                                MotionEvent.ACTION_DOWN) {

                            downX = event.getRawX();
                            downY = event.getRawY();

                            startX = controlParams.x;
                            startY = controlParams.y;

                            return true;
                        }

                        if (event.getAction() ==
                                MotionEvent.ACTION_MOVE) {

                            controlParams.x =
                                    startX +
                                    (int)(event.getRawX() - downX);

                            controlParams.y =
                                    startY +
                                    (int)(event.getRawY() - downY);

                            try {

                                windowManager.updateViewLayout(
                                        controls,
                                        controlParams
                                );

                            } catch (Exception ignored) {
                            }

                            return true;
                        }

                        return true;
                    }
                }
        );

        // ADD PHOTO
        windowManager.addView(
                overlayImage,
                imageParams
        );

        // ADD CONTROL BOX
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

        button.setMinWidth(55);
        button.setMinHeight(50);

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
                windowManager.removeView(
                        overlayImage
                );
            }

        } catch (Exception ignored) {
        }

        try {

            if (controls != null) {
                windowManager.removeView(
                        controls
                );
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
