package com.example.cameraoverlay;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
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

    private float photoX = 0;
    private float photoY = 0;
    private float photoScale = 1.0f;

    private float downX;
    private float downY;
    private float startX;
    private float startY;

    private int baseSize = 500;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        buildScreen();
    }

    private void buildScreen() {

        LinearLayout root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setBackgroundColor(Color.WHITE);

        // Top bar
        LinearLayout top = new LinearLayout(this);
        top.setGravity(Gravity.CENTER_VERTICAL);
        top.setPadding(20, 20, 20, 10);

        Button back = new Button(this);
        back.setText("←");

        TextView title = new TextView(this);
        title.setText("TEST CAMERA");
        title.setTextSize(20);
        title.setTextColor(Color.BLACK);
        title.setGravity(Gravity.CENTER);

        top.addView(
                back,
                new LinearLayout.LayoutParams(
                        70,
                        60
                )
        );

        LinearLayout.LayoutParams titleParams =
                new LinearLayout.LayoutParams(
                        0,
                        60,
                        1
                );

        top.addView(title, titleParams);

        root.addView(top);

        // Preview area
        preview = new FrameLayout(this);
        preview.setBackgroundColor(Color.WHITE);

        LinearLayout.LayoutParams previewParams =
                new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT,
                        0,
                        1
                );

        root.addView(preview, previewParams);

        // Face guide
        faceGuide = new View(this);

        GradientDrawable guideBackground =
                new GradientDrawable();

        guideBackground.setColor(Color.TRANSPARENT);
        guideBackground.setShape(
                GradientDrawable.OVAL
        );
        guideBackground.setStroke(
                6,
                Color.WHITE
        );

        faceGuide.setBackground(guideBackground);

        FrameLayout.LayoutParams guideParams =
                new FrameLayout.LayoutParams(
                        500,
                        500
                );

        guideParams.gravity = Gravity.TOP
                | Gravity.CENTER_HORIZONTAL;

        guideParams.topMargin = 80;

        preview.addView(
                faceGuide,
                guideParams
        );

        // Photo
        photo = new ImageView(this);

        photo.setScaleType(
                ImageView.ScaleType.CENTER_CROP
        );

        photo.setVisibility(View.GONE);

        FrameLayout.LayoutParams photoParams =
                new FrameLayout.LayoutParams(
                        baseSize,
                        baseSize
                );

        photoParams.gravity =
                Gravity.TOP | Gravity.CENTER_HORIZONTAL;

        photoParams.topMargin = 80;

        preview.addView(
                photo,
                0,
                photoParams
        );

        // Put guide above photo
        faceGuide.bringToFront();

        // Drag photo
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

                            downX = event.getRawX();
                            downY = event.getRawY();

                            startX = lp.leftMargin;
                            startY = lp.topMargin;

                            return true;
                        }

                        if (event.getAction() ==
                                MotionEvent.ACTION_MOVE) {

                            int newX =
                                    (int) (
                                            startX +
                                            event.getRawX() -
                                            downX
                                    );

                            int newY =
                                    (int) (
                                            startY +
                                            event.getRawY() -
                                            downY
                                    );

                            lp.leftMargin = newX;
                            lp.topMargin = newY;

                            photoX = newX;
                            photoY = newY;

                            photo.setLayoutParams(lp);

                            return true;
                        }

                        return true;
                    }
                }
        );

        // Instruction
        TextView instruction = new TextView(this);

        instruction.setText(
                "Fit the test photo inside the guide"
        );

        instruction.setTextSize(16);
        instruction.setTextColor(Color.WHITE);
        instruction.setGravity(Gravity.CENTER);

        GradientDrawable blackBox =
                new GradientDrawable();

        blackBox.setColor(Color.BLACK);
        blackBox.setCornerRadius(60);

        instruction.setBackground(blackBox);
        instruction.setPadding(35, 18, 35, 18);

        FrameLayout.LayoutParams textParams =
                new FrameLayout.LayoutParams(
                        FrameLayout.LayoutParams.WRAP_CONTENT,
                        FrameLayout.LayoutParams.WRAP_CONTENT
                );

        textParams.gravity =
                Gravity.CENTER_HORIZONTAL
                | Gravity.BOTTOM;

        textParams.bottomMargin = 150;

        preview.addView(
                instruction,
                textParams
        );

        // Bottom controls
        LinearLayout controls =
                new LinearLayout(this);

        controls.setOrientation(
                LinearLayout.HORIZONTAL
        );

        controls.setGravity(
                Gravity.CENTER
        );

        controls.setPadding(
                10,
                10,
                10,
                20
        );

        Button select = makeButton("PHOTO");

        Button minus = makeButton("−");

        Button plus = makeButton("+");

        Button reset = makeButton("RESET");

        Button capture = makeButton("TEST CAPTURE");

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

        // Select photo
        select.setOnClickListener(
                v -> openPicker()
        );

        // Resize down
        minus.setOnClickListener(
                v -> resizePhoto(-50)
        );

        // Resize up
        plus.setOnClickListener(
                v -> resizePhoto(50)
        );

        // Reset
        reset.setOnClickListener(
                v -> resetPhoto()
        );

        // Test capture
        capture.setOnClickListener(
                v -> {

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
        );

        back.setOnClickListener(
                v -> finish()
        );
    }

    private Button makeButton(String text) {

        Button b = new Button(this);

        b.setText(text);
        b.setTextSize(13);

        return b;
    }

    private void openPicker() {

        Intent intent =
                new Intent(
                        Intent.ACTION_OPEN_DOCUMENT
                );

        intent.addCategory(
                Intent.CATEGORY_OPENABLE
        );

        intent.setType("image/*");

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

            selectedPhoto = data.getData();

            photo.setImageURI(
                    selectedPhoto
            );

            photo.setVisibility(
                    View.VISIBLE
            );

            resetPhoto();

            Toast.makeText(
                    this,
                    "Test photo loaded",
                    Toast.LENGTH_SHORT
            ).show();
        }
    }

    private void resizePhoto(int amount) {

        FrameLayout.LayoutParams lp =
                (FrameLayout.LayoutParams)
                        photo.getLayoutParams();

        int newSize =
                lp.width + amount;

        if (newSize < 200) {
            newSize = 200;
        }

        if (newSize > 900) {
            newSize = 900;
        }

        lp.width = newSize;
        lp.height = newSize;

        photo.setLayoutParams(lp);
    }

    private void resetPhoto() {

        FrameLayout.LayoutParams lp =
                (FrameLayout.LayoutParams)
                        photo.getLayoutParams();

        lp.width = baseSize;
        lp.height = baseSize;

        lp.leftMargin = 0;
        lp.topMargin = 80;

        lp.gravity =
                Gravity.TOP
                | Gravity.CENTER_HORIZONTAL;

        photo.setLayoutParams(lp);
    }
}
