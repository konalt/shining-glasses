package net.konalt.nglasses;

import android.Manifest;
import android.content.res.ColorStateList;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresPermission;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.content.res.AppCompatResources;
import androidx.core.content.ContextCompat;

import com.google.android.material.slider.Slider;
import com.skydoves.colorpickerview.ColorEnvelope;
import com.skydoves.colorpickerview.ColorPickerView;
import com.skydoves.colorpickerview.listeners.ColorEnvelopeListener;
import com.skydoves.colorpickerview.listeners.ColorPickerViewListener;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

public class EyesActivity extends AppCompatActivity {
    GlassesDevice device;

    TextView tvDeviceName;

    ColorPickerView picker;
    Button btnEyesColorYellow;
    Button btnEyesColorUzi;
    Button btnEyesColorDoll;
    Button btnEyesColorLizzy;
    Button btnEyesColorThad;
    Button btnEyesColorWorker;
    Button btnEyesColorWhite;
    Button btnEyesColorRainbow;
    Button btnEyesColorApply;

    Button btnEyesToggleBlush;
    Button btnEyesToggleBlink;

    LinearLayout llEyeImages;
    List<ImageButton> imageButtons = new ArrayList<>();

    int currentColor = EyeManager.getColor("nvj");
    String currentEyes = "normal";

    private final Handler handler = new Handler(Looper.getMainLooper());

    @RequiresPermission(Manifest.permission.BLUETOOTH_CONNECT)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_eyes);

        if (App.currentDevice == null) {
            Log.e("BLE", "No current device");
            finish();
            return;
        }

        device = App.currentDevice;
        device.raw.onDisconnect(this::finish);

        initUI();

        initClickEvents();

        tvDeviceName.setText(device.name);

        picker.setInitialColor(currentColor);
        btnEyesColorApply.setBackgroundTintList(ColorStateList.valueOf(currentColor));

        EyeManager.setEyes(currentEyes, currentColor, true);
    }

    @RequiresPermission(Manifest.permission.BLUETOOTH_CONNECT)
    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (App.currentDevice != null && App.currentDevice.raw.connected) {
            App.currentDevice.setAnimation(0);
        }
        EyeManager.endBlink();
    }

    @RequiresPermission(Manifest.permission.BLUETOOTH_CONNECT)
    private void initImages() {
        final int gridWidth = 2; // grids made of linearlayouts my favourite yum yum!!!!

        LinearLayout currentLayout = new LinearLayout(this);;
        int gridPos = 0;
        for (Map.Entry<String, Bitmap> e : EyeManager.EYES_BITMAP.entrySet()) {
            if (Objects.equals(e.getKey(), "_blush")) continue;

            if (gridPos == 0) {
                Log.d(C.TAG, "Starting new grid line");
                currentLayout = new LinearLayout(this);
            }

            Log.d(C.TAG, "Adding image " + e.getKey());
            ImageButton btn = getImageButton(e);
            imageButtons.add(btn);
            currentLayout.addView(btn);

            gridPos++;

            if (gridPos == gridWidth) {
                Log.d(C.TAG, "Adding row, resetting grid position");
                gridPos = 0;
                llEyeImages.addView(currentLayout);
            }
        }
        if (currentLayout.getChildCount() > 0) {
            Log.d(C.TAG, "Children remain, adding");
            while (currentLayout.getChildCount() < gridWidth) {
                LinearLayout.LayoutParams param = new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT,
                        LinearLayout.LayoutParams.MATCH_PARENT,
                        0.5f
                );

                View view = new View(this);
                view.setLayoutParams(param);

                currentLayout.addView(view);
            }
            llEyeImages.addView(currentLayout);
        }
    }

    @RequiresPermission(Manifest.permission.BLUETOOTH_CONNECT)
    @NonNull
    private ImageButton getImageButton(Map.Entry<String, Bitmap> e) {
        String id = e.getKey();
        Bitmap bm = e.getValue();
        ImageButton btn = new ImageButton(this);
        btn.setBackground(AppCompatResources.getDrawable(this, R.drawable.eye_style_button));
        btn.setImageBitmap(bm);
        btn.setOnClickListener(v -> {
            EyeManager.setEyes(id, currentColor, true);
            for (ImageButton btn2 : imageButtons) {
                btn2.setAlpha(0.5f);
            }
            if (currentColor != 1) {
                btn.setColorFilter(currentColor, PorterDuff.Mode.MULTIPLY);
            }
            btn.setAlpha(1f);
            currentEyes = id;
        });
        btn.setScaleType(ImageView.ScaleType.FIT_CENTER);
        btn.setAdjustViewBounds(true);
        btn.setColorFilter(currentColor, PorterDuff.Mode.MULTIPLY);
        if (Objects.equals(id, currentEyes)) {
            btn.setAlpha(1f);
        } else {
            btn.setAlpha(0.5f);
        }
        btn.setTooltipText(id);
        LinearLayout.LayoutParams param = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT,
                0.5f
        );
        btn.setLayoutParams(param);
        return btn;
    }

    private void updateButtons() {
        Log.d(C.TAG, "Updating: " + currentColor);
        for (ImageButton btn : imageButtons) {
            if (currentColor == 1) {
                btn.setImageBitmap(EyeManager.EYES_BITMAP_RAINBOW.get(btn.getTooltipText()));
                btn.setColorFilter(Color.WHITE, PorterDuff.Mode.MULTIPLY);
            } else {
                btn.setImageBitmap(EyeManager.EYES_BITMAP.get(btn.getTooltipText()));
                btn.setColorFilter(currentColor, PorterDuff.Mode.MULTIPLY);
            }
        }
        try {
            picker.selectByHsvColor(currentColor);
        } catch (IllegalAccessException e) {
            throw new RuntimeException(e);
        }
        btnEyesColorApply.setBackgroundTintList(ColorStateList.valueOf(currentColor));
    }

    private void updateBlushButton() {
        btnEyesToggleBlush.setBackgroundTintList(ContextCompat.getColorStateList(this, EyeManager.currentBlushState ? R.color.button_red : R.color.button_green));
        btnEyesToggleBlush.setText((EyeManager.currentBlushState ? "Disable" : "Enable") + " Blush");
    }

    private void updateBlinkButton() {
        btnEyesToggleBlink.setBackgroundTintList(ContextCompat.getColorStateList(this, EyeManager.currentBlinkState ? R.color.button_red : R.color.button_green));
        btnEyesToggleBlink.setText((EyeManager.currentBlinkState ? "Disable" : "Enable") + " Blink");
    }

    @RequiresPermission(Manifest.permission.BLUETOOTH_CONNECT)
    private void initUI() {
        tvDeviceName = findViewById(R.id.deviceName);

        picker = findViewById(R.id.picker);
        btnEyesColorYellow = findViewById(R.id.btnEyesColorYellow);
        btnEyesColorUzi = findViewById(R.id.btnEyesColorUzi);
        btnEyesColorDoll = findViewById(R.id.btnEyesColorDoll);
        btnEyesColorLizzy = findViewById(R.id.btnEyesColorLizzy);
        btnEyesColorThad = findViewById(R.id.btnEyesColorThad);
        btnEyesColorWorker = findViewById(R.id.btnEyesColorWorker);
        btnEyesColorWhite = findViewById(R.id.btnEyesColorWhite);
        btnEyesColorRainbow = findViewById(R.id.btnEyesColorRainbow);
        btnEyesColorApply = findViewById(R.id.btnEyesColorApply);

        btnEyesToggleBlink = findViewById(R.id.btnEyesToggleBlink);
        btnEyesToggleBlush = findViewById(R.id.btnEyesToggleBlush);

        llEyeImages = findViewById(R.id.llEyeImages);

        initImages();
    }

    @RequiresPermission(Manifest.permission.BLUETOOTH_CONNECT)
    private void initClickEvents() {
        picker.setColorListener((ColorEnvelopeListener) (envelope, fromUser) -> {
            if (!fromUser) return;
            currentColor = envelope.getColor();
            updateButtons();
        });

        btnEyesColorYellow.setOnClickListener(v -> {
            currentColor = EyeManager.getColor("nvj");
            updateButtons();
            EyeManager.setEyes(currentEyes, currentColor, false);
        });
        btnEyesColorUzi.setOnClickListener(v -> {
            currentColor = EyeManager.getColor("uzi");
            updateButtons();
            EyeManager.setEyes(currentEyes, currentColor, false);
        });
        btnEyesColorDoll.setOnClickListener(v -> {
            currentColor = EyeManager.getColor("doll");
            updateButtons();
            EyeManager.setEyes(currentEyes, currentColor, false);
        });
        btnEyesColorLizzy.setOnClickListener(v -> {
            currentColor = EyeManager.getColor("lizzy");
            updateButtons();
            EyeManager.setEyes(currentEyes, currentColor, false);
        });
        btnEyesColorThad.setOnClickListener(v -> {
            currentColor = EyeManager.getColor("thad");
            updateButtons();
            EyeManager.setEyes(currentEyes, currentColor, false);
        });
        btnEyesColorWorker.setOnClickListener(v -> {
            currentColor = EyeManager.getColor("worker");
            updateButtons();
            EyeManager.setEyes(currentEyes, currentColor, false);
        });
        btnEyesColorWhite.setOnClickListener(v -> {
            currentColor = EyeManager.getColor("white");
            updateButtons();
            EyeManager.setEyes(currentEyes, currentColor, false);
        });
        btnEyesColorRainbow.setOnClickListener(v -> {
            currentColor = EyeManager.getColor("rainbow");
            updateButtons();
            EyeManager.setEyes(currentEyes, currentColor, false);
        });
        btnEyesColorApply.setOnClickListener(v -> {
            EyeManager.setEyes(currentEyes, currentColor, false);
        });

        btnEyesToggleBlink.setOnClickListener(v -> {
            boolean b = !EyeManager.currentBlinkState;
            EyeManager.setBlink(b);
            updateBlinkButton();
        });
        btnEyesToggleBlush.setOnClickListener(v -> {
            boolean b = !EyeManager.currentBlushState;
            EyeManager.setBlush(b);
            updateBlushButton();
        });
    }
}