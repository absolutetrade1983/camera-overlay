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
    private TextView dragHandle;

    private WindowManager.LayoutParams imageParams;
    private WindowManager.LayoutParams controlParams;

    private Uri selectedImage;

    private int imageSize = 500;
    private int imageX = 0;
    private int imageY = 150;

    private static final int MOVE_STEP = 40;
    private static final int SIZE_STEP = 30;
    private static final int MIN_SIZE = 150;
    private static final int MAX_SIZE = 1500;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        showMainScreen();
    }

    // =========================================================
    // MAIN SCREEN
    // =========================================================

    private void showMainScreen() {

        LinearLayout root = new LinearLayout(this);

        root.setOrientation(LinearLayout.VERTICAL);
        root.setGravity(Gravity.CENTER);
        root.setPadding(30, 30, 30, 30);
        root.setBackgroundColor(Color.rgb(25, 25, 25));

        TextView title = new TextView(this);

        title.setText("CAMERA OVERLAY");
        title.setTextSize(22);
        title.setTextColor(Color.WHITE);
        title.setGravity(Gravity.CENTER);
        title.setPadding(20, 20, 20, 30);

        root.addView(title);

        // -----------------------------------------------------
        // SELECT PHOTO
        // -----------------------------------------------------

        Button selectButton = new Button(this);

        selectButton.setText("SELECT PHOTO");

        selectButton.setOnClickListener(
                v -> selectPhoto()
        );

        root.addView(
                selectButton,
                new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT,
                        LinearLayout.LayoutParams.WRAP_CONTENT
                )
        );

        // -----------------------------------------------------
        // START OVERLAY
        // -----------------------------------------------------

        Button startButton = new Button(this);

        startButton.setText("START OVERLAY");

        startButton.setOnClickListener(
                v -> startOverlay(false)
        );

        root.addView(
                startButton,
                new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT,
                        LinearLayout.LayoutParams.WRAP_CONTENT
                )
        );

        // -----------------------------------------------------
        // START BACKWARD
        // -----------------------------------------------------

        Button backwardButton = new Button(this);

        backwardButton.setText("START BACKWARD");

        backwardButton.setOnClickListener(
                v -> startOverlay(true)
        );

        root.addView(
                backwardButton,
                new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT,
                        LinearLayout.LayoutParams.WRAP_CONTENT
                )
        );

        setContentView(root);
    }

    // =========================================================
    // SELECT PHOTO
    // =========================================================

    private void selectPhoto() {

        Intent intent =
                new Intent(Intent.ACTION_OPEN_DOCUMENT);

        intent.setType("image/*");

        intent.addCategory(
                Intent.CATEGORY_OPENABLE
        );

        startActivityForResult(
                intent,
                PICK_IMAGE
        );
    }

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

            selectedImage = data.getData();

            try {

                getContentResolver()
                        .takePersistableUriPermission(
                                selectedImage,
                                Intent.FLAG_GRANT_READ_URI_PERMISSION
                        );

            } catch (Exception ignored) {
            }

            Toast.makeText(
                    this,
                    "Photo selected",
                    Toast.LENGTH_SHORT
            ).show();
        }
    }

    // =========================================================
    // START OVERLAY / BACKWARD
    // =========================================================

    private void startOverlay(
            boolean backwardMode
    ) {

        if (selectedImage == null) {

            Toast.makeText(
                    this,
                    "Please select a photo first",
                    Toast.LENGTH_SHORT
            ).show();

            return;
        }

        if (!Settings.canDrawOverlays(this)) {

            Intent intent =
                    new Intent(
                            Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                            Uri.parse(
                                    "package:" +
                                            getPackageName()
                            )
                    );

            startActivity(intent);

            return;
        }

        if (overlayImage != null) {

            Toast.makeText(
                    this,
                    "Overlay already running",
                    Toast.LENGTH_SHORT
            ).show();

            return;
        }

        windowManager =
                (WindowManager)
                        getSystemService(
                                WINDOW_SERVICE
                        );

        createImageOverlay();

        createControlOverlay();

        if (backwardMode) {

            Toast.makeText(
                    this,
                    "START BACKWARD mode started",
                    Toast.LENGTH_SHORT
            ).show();

        } else {

            Toast.makeText(
                    this,
                    "START OVERLAY mode started",
                    Toast.LENGTH_SHORT
            ).show();
        }
    }

    // =========================================================
    // IMAGE OVERLAY
    // =========================================================

    private void createImageOverlay() {

        overlayImage =
                new ImageView(this);

        overlayImage.setImageURI(
                selectedImage
        );

        overlayImage.setScaleType(
                ImageView.ScaleType.FIT_CENTER
        );

        overlayImage.setBackgroundColor(
                Color.TRANSPARENT
        );

        imageParams =
                new WindowManager.LayoutParams(
                        imageSize,
                        imageSize,
                        WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY,
                        WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
                        PixelFormat.TRANSLUCENT
                );

        imageParams.gravity =
                Gravity.TOP | Gravity.START;

        imageParams.x = imageX;
        imageParams.y = imageY;

        // -----------------------------------------------------
        // DRAG PHOTO
        // -----------------------------------------------------

        overlayImage.setOnTouchListener(
                new View.OnTouchListener() {

                    float downX;
                    float downY;

                    int startX;
                    int startY;

                    @Override
                    public boolean onTouch(
                            View view,
                            MotionEvent event) {

                        if (event.getAction() ==
                                MotionEvent.ACTION_DOWN) {

                            downX =
                                    event.getRawX();

                            downY =
                                    event.getRawY();

                            startX =
                                    imageParams.x;

                            startY =
                                    imageParams.y;

                            return true;
                        }

                        if (event.getAction() ==
                                MotionEvent.ACTION_MOVE) {

                            imageParams.x =
                                    startX +
                                            (int)
                                                    (event.getRawX()
                                                            - downX);

                            imageParams.y =
                                    startY +
                                            (int)
                                                    (event.getRawY()
                                                            - downY);

                            imageX =
                                    imageParams.x;

                            imageY =
                                    imageParams.y;

                            updateImage();

                            return true;
                        }

                        return true;
                    }
                }
        );

        windowManager.addView(
                overlayImage,
                imageParams
        );
    }

    // =========================================================
    // CONTROL BOX
    // =========================================================

    private void createControlOverlay() {

        controls =
                new LinearLayout(this);

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
                Color.argb(
                        220,
                        30,
                        30,
                        30
                )
        );

        // -----------------------------------------------------
        // DRAG HANDLE
        // -----------------------------------------------------

        dragHandle =
                new TextView(this);

        dragHandle.setText(
                "☰ DRAG CONTROL BOX"
        );

        dragHandle.setTextColor(
                Color.WHITE
        );

        dragHandle.setTextSize(14);

        dragHandle.setGravity(
                Gravity.CENTER
        );

        dragHandle.setPadding(
                20,
                12,
                20,
                12
        );

        controls.addView(
                dragHandle
        );

        // -----------------------------------------------------
        // BUTTON ROW
        // -----------------------------------------------------

        LinearLayout row =
                new LinearLayout(this);

        row.setOrientation(
                LinearLayout.HORIZONTAL
        );

        row.setGravity(
                Gravity.CENTER
        );

        Button left =
                makeOverlayButton("←");

        Button up =
                makeOverlayButton("↑");

        Button minus =
                makeOverlayButton("−");

        Button plus =
                makeOverlayButton("+");

        Button down =
                makeOverlayButton("↓");

        Button right =
                makeOverlayButton("→");

        Button reset =
                makeOverlayButton("RESET");

        Button close =
                makeOverlayButton("X");

        row.addView(left);
        row.addView(up);
        row.addView(minus);
        row.addView(plus);
        row.addView(down);
        row.addView(right);
        row.addView(reset);
        row.addView(close);

        controls.addView(row);

        // -----------------------------------------------------
        // ACTIONS
        // -----------------------------------------------------

        left.setOnClickListener(
                v -> moveImage(
                        -MOVE_STEP,
                        0
                )
        );

        right.setOnClickListener(
                v -> moveImage(
                        MOVE_STEP,
                        0
                )
        );

        up.setOnClickListener(
                v -> moveImage(
                        0,
                        -MOVE_STEP
                )
        );

        down.setOnClickListener(
                v -> moveImage(
                        0,
                        MOVE_STEP
                )
        );

        minus.setOnClickListener(
                v -> resizeImage(
                        -SIZE_STEP
                )
        );

        plus.setOnClickListener(
                v -> resizeImage(
                        SIZE_STEP
                )
        );

        reset.setOnClickListener(
                v -> resetImage()
        );

        close.setOnClickListener(
                v -> removeOverlay()
        );

        // -----------------------------------------------------
        // CONTROL WINDOW
        // -----------------------------------------------------

        controlParams =
                new WindowManager.LayoutParams(
                        WindowManager.LayoutParams.WRAP_CONTENT,
                        WindowManager.LayoutParams.WRAP_CONTENT,
                        WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY,
                        WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
                        PixelFormat.TRANSLUCENT
                );

        controlParams.gravity =
                Gravity.TOP | Gravity.START;

        controlParams.x =
                imageX;

        controlParams.y =
                imageY +
                        imageSize +
                        20;

        // -----------------------------------------------------
        // DRAG CONTROL BOX
        // -----------------------------------------------------

        dragHandle.setOnTouchListener(
                new View.OnTouchListener() {

                    float downX;
                    float downY;

                    int startX;
                    int startY;

                    @Override
                    public boolean onTouch(
                            View view,
                            MotionEvent event) {

                        if (event.getAction() ==
                                MotionEvent.ACTION_DOWN) {

                            downX =
                                    event.getRawX();

                            downY =
                                    event.getRawY();

                            startX =
                                    controlParams.x;

                            startY =
                                    controlParams.y;

                            return true;
                        }

                        if (event.getAction() ==
                                MotionEvent.ACTION_MOVE) {

                            controlParams.x =
                                    startX +
                                            (int)
                                                    (event.getRawX()
                                                            - downX);

                            controlParams.y =
                                    startY +
                                            (int)
                                                    (event.getRawY()
                                                            - downY);

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

        windowManager.addView(
                controls,
                controlParams
        );
    }

    // =========================================================
    // BUTTON
    // =========================================================

    private Button makeOverlayButton(
            String text
    ) {

        Button button =
                new Button(this);

        button.setText(text);
        button.setTextSize(13);

        button.setMinWidth(55);
        button.setMinHeight(50);

        return button;
    }

    // =========================================================
    // MOVE
    // =========================================================

    private void moveImage(
            int dx,
            int dy
    ) {

        if (overlayImage == null ||
                imageParams == null) {
            return;
        }

        imageX += dx;
        imageY += dy;

        imageParams.x =
                imageX;

        imageParams.y =
                imageY;

        updateImage();
    }

    // =========================================================
    // RESIZE
    // =========================================================

    private void resizeImage(
            int amount
    ) {

        if (overlayImage == null ||
                imageParams == null) {
            return;
        }

        imageSize += amount;

        if (imageSize < MIN_SIZE) {
            imageSize = MIN_SIZE;
        }

        if (imageSize > MAX_SIZE) {
            imageSize = MAX_SIZE;
        }

        imageParams.width =
                imageSize;

        imageParams.height =
                imageSize;

        updateImage();
    }

    // =========================================================
    // UPDATE
    // =========================================================

    private void updateImage() {

        if (windowManager == null ||
                overlayImage == null ||
                imageParams == null) {
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

    // =========================================================
    // RESET
    // =========================================================

    private void resetImage() {

        imageSize = 500;
        imageX = 0;
        imageY = 150;

        if (imageParams != null) {

            imageParams.width =
                    imageSize;

            imageParams.height =
                    imageSize;

            imageParams.x =
                    imageX;

            imageParams.y =
                    imageY;

            updateImage();
        }
    }

    // =========================================================
    // REMOVE
    // =========================================================

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
        dragHandle = null;

        imageParams = null;
        controlParams = null;
    }

    @Override
    protected void onDestroy() {

        removeOverlay();

        super.onDestroy();
    }
}
