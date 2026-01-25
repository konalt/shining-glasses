package net.konalt.nglasses;

import android.Manifest;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.TextView;

import androidx.annotation.RequiresPermission;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.slider.Slider;

import java.util.List;

public class ControlActivity extends AppCompatActivity {
    GlassesDevice device;

    TextView tvDeviceName;
    Slider brightnessSlider;
    Button btnClear;
    Button btnEyesInitN;
    Button btnEyesInitUzi;
    Button btnEyesInitWorker;
    Button btnEyesInitPink;
    Button btnEyesInitRainbow;
    Button btnEyesBlink;
    Button btnEyesBlushEnable;
    Button btnEyesBlushDisable;
    Button btnBadAppleStart;
    Button btnBadAppleStop;
    Spinner animationSpinner;
    Button btnSubmitAnimation;
    Spinner imageSpinner;
    Button btnSubmitImage;
    Spinner customImageSpinner;
    Button btnSubmitCustomImage;
    Spinner eyeTypeSpinner;

    @RequiresPermission(Manifest.permission.BLUETOOTH_CONNECT)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_control);

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
    }

    @RequiresPermission(Manifest.permission.BLUETOOTH_CONNECT)
    private void initUI() {
        tvDeviceName = findViewById(R.id.deviceName);
        btnClear = findViewById(R.id.btnClear);
        btnEyesInitN = findViewById(R.id.btnEyesInitN);
        btnEyesInitUzi = findViewById(R.id.btnEyesInitUzi);
        btnEyesInitWorker = findViewById(R.id.btnEyesInitWorker);
        btnEyesInitPink = findViewById(R.id.btnEyesInitPink);
        btnEyesInitRainbow = findViewById(R.id.btnEyesInitRainbow);
        btnEyesBlink = findViewById(R.id.btnEyesBlink);
        btnEyesBlushEnable = findViewById(R.id.btnEyesBlushEnable);
        btnEyesBlushDisable = findViewById(R.id.btnEyesBlushDisable);
        btnBadAppleStart = findViewById(R.id.btnBadAppleStart);
        btnBadAppleStop = findViewById(R.id.btnBadAppleStop);
        brightnessSlider = findViewById(R.id.brightnessSlider);
        animationSpinner = findViewById(R.id.animationSpinner);
        btnSubmitAnimation = findViewById(R.id.btnSubmitAnimation);
        btnSubmitImage = findViewById(R.id.btnSubmitImage);
        btnSubmitCustomImage = findViewById(R.id.btnSubmitCustomImage);
        customImageSpinner = findViewById(R.id.customImageSpinner);
        imageSpinner = findViewById(R.id.imageSpinner);
        eyeTypeSpinner = findViewById(R.id.eyeTypeSpinner);

        ArrayAdapter<String> imageAdapter = new ArrayAdapter<>(this, androidx.appcompat.R.layout.support_simple_spinner_dropdown_item, C.images);
        ArrayAdapter<String> animAdapter = new ArrayAdapter<>(this, androidx.appcompat.R.layout.support_simple_spinner_dropdown_item, C.animations);
        ArrayAdapter<String> customImageAdapter = new ArrayAdapter<>(this, androidx.appcompat.R.layout.support_simple_spinner_dropdown_item, C.paletteImageNames);
        ArrayAdapter<String> eyeTypeAdapter = new ArrayAdapter<>(this, androidx.appcompat.R.layout.support_simple_spinner_dropdown_item, C.eyeTypeNames);

        imageSpinner.setAdapter(imageAdapter);
        animationSpinner.setAdapter(animAdapter);
        customImageSpinner.setAdapter(customImageAdapter);
        eyeTypeSpinner.setAdapter(eyeTypeAdapter);

        brightnessSlider.addOnChangeListener((slider, v, fromUser) -> {
            if (!fromUser) return;
            int valueInt = (int) v;
            device.setBrightness(valueInt);
        });
    }

    @RequiresPermission(Manifest.permission.BLUETOOTH_CONNECT)
    private void initClickEvents() {
        btnClear.setOnClickListener(v -> device.clear());
        btnEyesInitN.setOnClickListener(v -> {
            CustomImage.init(getSelectedEyeImage(), CustomImages.N_EYES_COLOR, true);
        });
        btnEyesInitUzi.setOnClickListener(v -> {
            CustomImage.init(getSelectedEyeImage(), CustomImages.UZI_COLOR, true);
        });
        btnEyesInitWorker.setOnClickListener(v -> {
            CustomImage.init(getSelectedEyeImage(), CustomImages.WORKER_COLOR, true);
        });
        btnEyesInitPink.setOnClickListener(v -> {
            CustomImage.init(getSelectedEyeImage(), CustomImages.RED_COLOR, true);
        });
        btnEyesInitRainbow.setOnClickListener(v -> {
            CustomImage.initRainbow(getSelectedEyeImage(), true);
        });
        btnEyesBlink.setOnClickListener(v -> {
            CustomImage.blink(getSelectedEyeImage(), CustomImage.currentColor);
        });
        btnEyesBlushEnable.setOnClickListener(v -> {
            CustomImage.init(CustomImages.BLUSH, CustomImage.currentColor, false);
        });
        btnEyesBlushDisable.setOnClickListener(v -> {
            CustomImage.init(CustomImages.BLUSH, 0x0, false);
        });
        btnBadAppleStart.setOnClickListener(v -> {
            List<List<List<int[]>>> animation = BadApple.parseAnimationData(Animations.BAD_APPLE);
            BadApple.playAnimation(animation, false);
        });
        btnBadAppleStop.setOnClickListener(v -> {
            BadApple.stopAnimation();
        });
        btnSubmitImage.setOnClickListener(v -> {
            device.setImage(imageSpinner.getSelectedItemPosition());
        });
        btnSubmitAnimation.setOnClickListener(v -> {
            device.setAnimation(animationSpinner.getSelectedItemPosition());
        });
        btnSubmitCustomImage.setOnClickListener(v -> {
            CustomImage.initPaletteImage(C.paletteImages[customImageSpinner.getSelectedItemPosition()], true);
        });
    }

    private String getSelectedEyeImage() {
        return C.eyeTypes[eyeTypeSpinner.getSelectedItemPosition()];
    }
}