package net.konalt.nglasses;

import android.Manifest;
import android.content.Intent;
import android.content.res.Resources;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.TextView;

import androidx.annotation.RequiresPermission;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.google.android.material.slider.Slider;

import java.util.List;

public class ControlActivity extends AppCompatActivity {
    GlassesDevice device;

    TextView tvDeviceName;
    Slider brightnessSlider;
    Button btnClear;
    Button btnBadAppleStart;
    Button btnBadAppleStop;
    Spinner animationSpinner;
    Button btnSubmitAnimation;
    Spinner imageSpinner;
    Button btnSubmitImage;
    Spinner customImageSpinner;
    Button btnSubmitCustomImage;

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

    @Override
    protected void onRestart() {
        super.onRestart();
        if (App.currentDevice == null || !App.currentDevice.raw.connected) {
            Log.e("BLE", "No current device");
            finish();
        }
    }

    @RequiresPermission(Manifest.permission.BLUETOOTH_CONNECT)
    private void initUI() {
        tvDeviceName = findViewById(R.id.deviceName);
        btnClear = findViewById(R.id.btnClear);
        btnBadAppleStart = findViewById(R.id.btnBadAppleStart);
        btnBadAppleStop = findViewById(R.id.btnBadAppleStop);
        brightnessSlider = findViewById(R.id.brightnessSlider);
        animationSpinner = findViewById(R.id.animationSpinner);
        btnSubmitAnimation = findViewById(R.id.btnSubmitAnimation);
        btnSubmitImage = findViewById(R.id.btnSubmitImage);
        btnSubmitCustomImage = findViewById(R.id.btnSubmitCustomImage);
        customImageSpinner = findViewById(R.id.customImageSpinner);
        imageSpinner = findViewById(R.id.imageSpinner);

        ArrayAdapter<String> imageAdapter = new ArrayAdapter<>(this, androidx.appcompat.R.layout.support_simple_spinner_dropdown_item, C.images);
        ArrayAdapter<String> animAdapter = new ArrayAdapter<>(this, androidx.appcompat.R.layout.support_simple_spinner_dropdown_item, C.animations);
        ArrayAdapter<String> customImageAdapter = new ArrayAdapter<>(this, androidx.appcompat.R.layout.support_simple_spinner_dropdown_item, C.paletteImageNames);

        imageSpinner.setAdapter(imageAdapter);
        animationSpinner.setAdapter(animAdapter);
        customImageSpinner.setAdapter(customImageAdapter);

        brightnessSlider.addOnChangeListener((slider, v, fromUser) -> {
            if (!fromUser) return;
            int valueInt = (int) v;
            device.setBrightness(valueInt);
        });
    }

    @RequiresPermission(Manifest.permission.BLUETOOTH_CONNECT)
    private void initClickEvents() {
        Button testEyeActivity = findViewById(R.id.btnEyeActivity);
        testEyeActivity.setOnClickListener(v -> {
            Intent i = new Intent(this, EyesActivity.class);
            startActivity(i);
        });
        btnClear.setOnClickListener(v -> {
            device.clear();
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
            CustomImage.initPaletteImage(C.paletteImages[customImageSpinner.getSelectedItemPosition()], true, () -> {});
        });
    }
}