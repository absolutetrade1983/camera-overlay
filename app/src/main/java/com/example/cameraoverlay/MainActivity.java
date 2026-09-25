package com.example.cameraoverlay;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.PixelFormat;
import android.net.Uri;
import android.os.Bundle;
import android.provider.Settings;
import android.view.Gravity;
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

    private LinearLayout overlayRoot;
    private ImageView overlayImage;
    private LinearLayout controls;

    private WindowManager.LayoutParams imageParams;
    private WindowManager.LayoutParams controlParams;

    private int imageSize = 500;
    private int imageX = 0;
    private int imageY = 150;

    private final int STEP = 30;
    private final int MIN_SIZE = 150;
    private final int MAX_SIZE = 1500;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (!Settings.canDrawOverlays(this)) {
            Intent intent = new Intent(
                    Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                    Uri.parse("package:" + getPackageName())
            );
            startActivity(intent);
            return;
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

        Button selectImage = new Button(this);
        selectImage.setText("SELECT PHOTO");
        layout.addView(selectImage);

        Button startOverlay = new Button(this);
        startOverlay.setText("START OVERLAY");
        layout.addView(startOverlay);

        Button stopOverlay = new Button(this);
        stopOverlay.setText("STOP OVERLAY");
        layout.addView(stopOverlay);

        setContentView(layout);

        selectImage.setOnClickListener(v -> openImagePicker());

        startOverlay.setOnClickListener(v -> {
            if (overlayRoot == null) {
                createOverlay();
            }
        });

        stopOverlay.setOnClickListener(v -> removeOverlay());
    }

    private void openImagePicker() {

        Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        intent.setType("image/*");

        startActivityForResult(intent, PICK_IMAGE);
    }

    @Override
    protected void onActivityResult(
            int requestCode,
            int resultCode,
            Intent data
    ) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == PICK_IMAGE &&
                resultCode == RESULT_OK &&
                data != null &&
                data.getData() != null) {

            Uri imageUri = data.getData();

            try {
                getContentResolver().takePersistableUriPermission(
                        imageUri,
                        Intent.FLAG_GRANT_READ_URI_PERMISSION
                );
            } catch (Exception ignored) {
            }

            if (overlayImage != null) {
                overlayImage.setImageURI(imageUri);
            }

            Toast.makeText(
                    this,
                    "Photo selected",
                    Toast.LENGTH_SHORT
            ).show();
        }
    }

    private void createOverlay() {

        windowManager =
                (WindowManager) getSystemService(WINDOW_SERVICE);

        overlayRoot = new LinearLayout(this);
        overlayRoot.setOrientation(LinearLayout.VERTICAL);
        overlayRoot.setGravity(Gravity.CENTER);

        overlayImage = new ImageView(this);
        overlayImage.setScaleType(ImageView.ScaleType.FIT_CENTER);

        overlayImage.setBackgroundColor(Color.TRANSPARENT);

        overlayRoot.addView(
                overlayImage,
                new LinearLayout.LayoutParams(
                        imageSize,
                        imageSize
                )
        );

        imageParams = new WindowManager.LayoutParams(
                imageSize,
                imageSize,
                WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY,
                WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
                PixelFormat.TRANSLUCENT
        );

        imageParams.gravity = Gravity.TOP | Gravity.START;
        imageParams.x = imageX;
        imageParams.y = imageY;

        createControls();

        windowManager.addView(overlayImage, imageParams);
        windowManager.addView(controls, controlParams);
    }

    private void createControls() {

        controls = new LinearLayout(this);
        controls.setOrientation(LinearLayout.VERTICAL);
        controls.setGravity(Gravity.CENTER);

        controls.setPadding(8, 8, 8, 8);
        controls.setBackgroundColor(Color.argb(220, 0, 0, 0));

        LinearLayout row1 = new LinearLayout(this);
        row1.setGravity(Gravity.CENTER);

        Button up = makeButton("↑");
        Button down = makeButton("↓");

        row1.addView(up);
        row1.addView(down);

        LinearLayout row2 = new LinearLayout(this);
        row2.setGravity(Gravity.CENTER);

        Button left = makeButton("←");
        Button right = makeButton("→");

        row2.addView(left);
        row2.addView(right);

        LinearLayout row3 = new LinearLayout(this);
        row3.setGravity(Gravity.CENTER);

        Button smaller = makeButton("−");
        Button bigger = makeButton("+");

        row3.addView(smaller);
        row3.addView(bigger);

        Button close = makeButton("X");

        controls.addView(row1);
        controls.addView(row2);
        controls.addView(row3);
        controls.addView(close);

        controlParams = new WindowManager.LayoutParams(
                WindowManager.LayoutParams.WRAP_CONTENT,
                WindowManager.LayoutParams.WRAP_CONTENT,
                WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY,
                WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
                PixelFormat.TRANSLUCENT
        );

        controlParams.gravity =
                Gravity.TOP | Gravity.START;

        controlParams.x = 20;
        controlParams.y = 20;

        up.setOnClickListener(v -> moveImage(0, -STEP));
        down.setOnClickListener(v -> moveImage(0, STEP));
        left.setOnClickListener(v -> moveImage(-STEP, 0));
        right.setOnClickListener(v -> moveImage(STEP, 0));

        smaller.setOnClickListener(v -> resizeImage(-50));
        bigger.setOnClickListener(v -> resizeImage(50));

        close.setOnClickListener(v -> removeOverlay());
    }

    private Button makeButton(String text) {

        Button button = new Button(this);

        button.setText(text);
        button.setTextSize(18);
        button.setTextColor(Color.WHITE);

        button.setPadding(5, 0, 5, 0);

        return button;
    }

    private void moveImage(int dx, int dy) {

        if (imageParams == null || overlayImage == null) {
            return;
        }

        imageX += dx;
        imageY += dy;

        imageParams.x = imageX;
        imageParams.y = imageY;

        windowManager.updateViewLayout(
                overlayImage,
                imageParams
        );
    }

    private void resizeImage(int amount) {

        if (imageParams == null || overlayImage == null) {
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

        windowManager.updateViewLayout(
                overlayImage,
                imageParams
        );
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
        overlayRoot = null;
        imageParams = null;
        controlParams = null;
    }

    @Override
    protected void onDestroy() {
        removeOverlay();
        super.onDestroy();
    }
}
