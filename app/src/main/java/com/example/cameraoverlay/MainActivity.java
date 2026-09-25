package com.example.cameraoverlay;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.Outline;
import android.graphics.drawable.GradientDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewOutlineProvider;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends Activity {

    private static final int PICK_IMAGE = 1001;

    private FrameLayout preview;
    private ImageView photo;
    private View faceGuide;

    private Uri selectedPhoto;

    private int photoSize = 500;

    private int photoX = 0;
    private int photoY = 80;

    private float downX;
    private float downY;

    private int startX;
    private int startY;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        createScreen();
    }

    private void createScreen() {

        LinearLayout root = new LinearLayout(this);

        root.setOrientation(
                LinearLayout.VERTICAL
        );

        root.setBackgroundColor(
                Color.WHITE
        );

        // -------------------------
        // TOP BAR
        // -------------------------

        LinearLayout topBar =
                new LinearLayout(this);

        topBar.setGravity(
                Gravity.CENTER_VERTICAL
        );

        topBar.setPadding(
                15,
                15,
                15,
                5
        );

        Button back =
                new Button(this);

        back.setText("←");
        back.setTextSize(24);

        topBar.addView(
                back,
                new LinearLayout.LayoutParams(
                        70,
                        65
                )
        );

        TextView title =
                new TextView(this);

        title.setText(
                "TEST CAMERA"
        );

        title.setTextSize(20);
        title.setTextColor(Color.BLACK);
        title.setGravity(Gravity.CENTER);

        topBar.addView(
                title,
                new LinearLayout.LayoutParams(
                        0,
                        65,
                        1
                )
        );

        root.addView(topBar);

        // -------------------------
        // PREVIEW
        // -------------------------

        preview =
                new FrameLayout(this);

        preview.setBackgroundColor(
                Color.WHITE
        );

        root.addView(
                preview,
                new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT,
                        0,
                        1
                )
        );

        // -------------------------
        // PHOTO
        // -------------------------

        photo =
                new ImageView(this);

        photo.setScaleType(
                ImageView.ScaleType.CENTER_CROP
        );

        photo.setVisibility(
                View.GONE
        );

        makePhotoCircular();

        FrameLayout.LayoutParams photoParams =
                new FrameLayout.LayoutParams(
                        photoSize,
                        photoSize
                );

        photoParams.gravity =
                Gravity.TOP |
                Gravity.CENTER_HORIZONTAL;

        photoParams.topMargin = 80;

        preview.addView(
                photo,
                photoParams
        );

        // -------------------------
        // FACE GUIDE
        // -------------------------

        faceGuide =
                new View(this);

        GradientDrawable guide =
                new GradientDrawable();

        guide.setShape(
                GradientDrawable.OVAL
        );

        guide.setColor(
                Color.TRANSPARENT
        );

        guide.setStroke(
                5,
                Color.WHITE
        );

        faceGuide.setBackground(
                guide
        );

        FrameLayout.LayoutParams guideParams =
                new FrameLayout.LayoutParams(
                        500,
                        500
                );

        guideParams.gravity =
                Gravity.TOP |
                Gravity.CENTER_HORIZONTAL;

        guideParams.topMargin = 80;

        preview.addView(
                faceGuide,
                guideParams
        );

        faceGuide.bringToFront();

        // -------------------------
        // DRAG PHOTO
        // -------------------------

        photo.setOnTouchListener(
                new View.OnTouchListener() {

                    @Override
                    public boolean onTouch(
                            View v,
                            MotionEvent event
                    ) {

                        FrameLayout.LayoutParams lp =
                                (FrameLayout.LayoutParams)
                                        photo.getLayoutParams();

                        if (event.getAction() ==
                                MotionEvent.ACTION_DOWN) {

                            downX =
                                    event.getRawX();

                            downY =
                                    event.getRawY();

                            startX =
                                    lp.leftMargin;

                            startY =
                                    lp.topMargin;

                            return true;
                        }

                        if (event.getAction() ==
                                MotionEvent.ACTION_MOVE) {

                            int newX =
                                    startX +
                                    (int) (
                                            event.getRawX()
                                                    - downX
                                    );

                            int newY =
                                    startY +
                                    (int) (
                                            event.getRawY()
                                                    - downY
                                    );

                            lp.leftMargin =
                                    newX;

                            lp.topMargin =
                                    newY;

                            photoX =
                                    newX;

                            photoY =
                                    newY;

                            photo.setLayoutParams(
                                    lp
                            );

                            return true;
                        }

                        return true;
                    }
                }
        );

        // -------------------------
        // MESSAGE
        // -------------------------

        TextView message =
                new TextView(this);

        message.setText(
                "Fit your test photo in the guide"
        );

        message.setTextSize(16);
        message.setTextColor(Color.WHITE);
        message.setGravity(
                Gravity.CENTER
        );

        GradientDrawable messageBg =
                new GradientDrawable();

        messageBg.setColor(
                Color.BLACK
        );

        messageBg.setCornerRadius(
                60
        );

        message.setBackground(
                messageBg
        );

        message.setPadding(
                35,
                18,
                35,
                18
        );

        FrameLayout.LayoutParams messageParams =
                new FrameLayout.LayoutParams(
                        FrameLayout.LayoutParams.WRAP_CONTENT,
                        FrameLayout.LayoutParams.WRAP_CONTENT
                );

        messageParams.gravity =
                Gravity.CENTER_HORIZONTAL |
                Gravity.BOTTOM;

        messageParams.bottomMargin =
                120;

        preview.addView(
                message,
                messageParams
        );

        // -------------------------
        // CONTROLS
        // -------------------------

        LinearLayout controls =
                new LinearLayout(this);

        controls.setOrientation(
                LinearLayout.HORIZONTAL
        );

        controls.setGravity(
                Gravity.CENTER
        );

        controls.setPadding(
                5,
                5,
                5,
                15
        );

        Button select =
                makeButton("PHOTO");

        Button minus =
                makeButton("−");

        Button plus =
                makeButton("+");

        Button reset =
                makeButton("RESET");

        Button capture =
                makeButton("TEST CAPTURE");

        controls.addView(select);
        controls.addView(minus);
        controls.addView(plus);
        controls.addView(reset);
        controls.addView(capture);

        root.addView(
                controls,
                new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT,
                        LinearLayout.LayoutParams.WRAP_CONTENT
                )
        );

        setContentView(root);

        // -------------------------
        // BUTTONS
        // -------------------------

        back.setOnClickListener(
                v -> finish()
        );

        select.setOnClickListener(
                v -> openPhotoPicker()
        );

        minus.setOnClickListener(
                v -> resizePhoto(-40)
        );

        plus.setOnClickListener(
                v -> resizePhoto(40)
        );

        reset.setOnClickListener(
                v -> resetPhoto()
        );

        capture.setOnClickListener(
                v -> testCapture()
        );
    }

    // -------------------------
    // MAKE PHOTO CIRCULAR
    // -------------------------

    private void makePhotoCircular() {

        GradientDrawable circle =
                new GradientDrawable();

        circle.setShape(
                GradientDrawable.OVAL
        );

        circle.setColor(
                Color.TRANSPARENT
        );

        photo.setBackground(
                circle
        );

        photo.setClipToOutline(
                true
        );

        photo.setOutlineProvider(
                new ViewOutlineProvider() {

                    @Override
                    public void getOutline(
                            View view,
                            Outline outline
                    ) {

                        outline.setOval(
                                0,
                                0,
                                view.getWidth(),
                                view.getHeight()
                        );
                    }
                }
        );
    }

    // -------------------------
    // PHOTO PICKER
    // -------------------------

    private void openPhotoPicker() {

        Intent intent =
                new Intent(
                        Intent.ACTION_OPEN_DOCUMENT
                );

        intent.addCategory(
                Intent.CATEGORY_OPENABLE
        );

        intent.setType(
                "image/*"
        );

        intent.addFlags(
                Intent.FLAG_GRANT_READ_URI_PERMISSION
        );

        intent.addFlags(
                Intent.FLAG_GRANT_PERSISTABLE_URI_PERMISSION
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

            selectedPhoto =
                    data.getData();

            try {

                getContentResolver()
                        .takePersistableUriPermission(
                                selectedPhoto,
                                Intent.FLAG_GRANT_READ_URI_PERMISSION
                        );

            } catch (Exception ignored) {
            }

            photo.setImageURI(
                    selectedPhoto
            );

            photo.setVisibility(
                    View.VISIBLE
            );

            resetPhoto();

            Toast.makeText(
                    this,
                    "Photo loaded",
                    Toast.LENGTH_SHORT
            ).show();
        }
    }

    // -------------------------
    // RESIZE
    // -------------------------

    private void resizePhoto(
            int amount
    ) {

        if (selectedPhoto == null) {

            Toast.makeText(
                    this,
                    "First select a photo",
                    Toast.LENGTH_SHORT
            ).show();

            return;
        }

        photoSize += amount;

        if (photoSize < 200) {
            photoSize = 200;
        }

        if (photoSize > 900) {
            photoSize = 900;
        }

        FrameLayout.LayoutParams lp =
                (FrameLayout.LayoutParams)
                        photo.getLayoutParams();

        lp.width =
                photoSize;

        lp.height =
                photoSize;

        photo.setLayoutParams(
                lp
        );

        photo.bringToFront();
        faceGuide.bringToFront();
    }

    // -------------------------
    // RESET
    // -------------------------

    private void resetPhoto() {

        photoSize = 500;

        photoX = 0;
        photoY = 80;

        FrameLayout.LayoutParams lp =
                (FrameLayout.LayoutParams)
                        photo.getLayoutParams();

        lp.width =
                photoSize;

        lp.height =
                photoSize;

        lp.gravity =
                Gravity.TOP |
                Gravity.CENTER_HORIZONTAL;

        lp.leftMargin =
                0;

        lp.topMargin =
                80;

        photo.setLayoutParams(
                lp
        );

        photo.bringToFront();
        faceGuide.bringToFront();
    }

    // -------------------------
    // TEST CAPTURE
    // -------------------------

    private void testCapture() {

        if (selectedPhoto == null) {

            Toast.makeText(
                    this,
                    "Select a test photo first",
                    Toast.LENGTH_SHORT
            ).show();

            return;
        }

        Toast.makeText(
                this,
                "TEST CAPTURE OK",
                Toast.LENGTH_SHORT
        ).show();
    }

    // -------------------------
    // BUTTON
    // -------------------------

    private Button makeButton(
            String text
    ) {

        Button button =
                new Button(this);

        button.setText(text);
        button.setTextSize(13);

        return button;
    }
}
