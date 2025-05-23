package com.cloudpos.scannerinsetmodedemo;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.cloudpos.DeviceException;
import com.cloudpos.advance.ext.POSTerminalAdvance;
import com.cloudpos.advance.ext.scanner.IScannerDevice;
import com.cloudpos.advance.ext.scanner.ScanParameter;
import com.cloudpos.advance.ext.scanner.ScanResult;
import com.cloudpos.scannerinsetmodedemo.utils.DeviceUtils;

/**
 * Created by gecx on 17-1-20.
 */

public class ScanActivity extends Activity implements View.OnClickListener {

    private Context context;
    private String TAG = "ScanActivity";
    private IScannerDevice scannerDevice;

    boolean flashlight;
    boolean indicatorlight;
    int cameraIndex;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_scan);
        context = this;
        initView();
    }

    private void initView() {
        TextView title = (TextView) findViewById(R.id.bar_title);
        ImageView back = (ImageView) findViewById(R.id.bar_back);
        Button changeCamera  = (Button) findViewById(R.id.changecamera);
        Button changeindicator  = (Button) findViewById(R.id.changeindicator);
        Button changeflashlight  = (Button) findViewById(R.id.changeflashlight);

        changeCamera.setOnClickListener(this);
        changeindicator.setOnClickListener(this);
        changeflashlight.setOnClickListener(this);

        title.setText("Scan");
        scannerDevice = POSTerminalAdvance.getInstance().getScannerDevice();
        open();
        back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
               close();
            }
        });
    }


    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.changecamera:
                sendBroadcastForCameraSwitch();
                break;
            case R.id.changeflashlight:
                flashlight = !flashlight;
                sendScanBroadcast(ScanParameter.BROADCAST_SET_FLASHLIGHT, flashlight);
                break;
            case R.id.changeindicator:
                indicatorlight = !indicatorlight;
                sendScanBroadcast(ScanParameter.BROADCAST_SET_INDICATOR, indicatorlight);

                break;
        }
    }

    private void sendBroadcastForCameraSwitch() {
        Intent intent = new Intent();
        intent.setAction(ScanParameter.BROADCAST_SET_CAMERA);
        cameraIndex = cameraIndex == 0 ? 1 : 0;
        intent.putExtra(ScanParameter.BROADCAST_VALUE, cameraIndex);
        sendBroadcast(intent);
    }

    private void sendScanBroadcast(String action, boolean value) {
        Intent intent = new Intent();
        intent.setAction(action);
        intent.putExtra(ScanParameter.BROADCAST_VALUE, value);
        sendBroadcast(intent);
    }

    private void open(){
        runInBackground(new Runnable() {
            @Override
            public void run() {
                try {
                    boolean result = scannerDevice.open(context);
                    if(result){
                        beginScan();
                    }
                } catch (DeviceException e) {
                    e.printStackTrace();
                }
            }
        });
    }
    private void close(){
        runInBackground(new Runnable() {
            @Override
            public void run() {
                try {
                    scannerDevice.stopScan();
                    scannerDevice.close();
                } catch (DeviceException e) {
                    e.printStackTrace();
                }
            }
        });
    }

    // It's necessary to calculate the heights of both the status bar and the title bar, with the final values to be provided in density-independent pixels (dp).
    public void beginScan() {
        runInBackground(() -> {
            int screenHeight = DeviceUtils.px2dp(context, DeviceUtils.getScreenHeight(context));
            int windowWidth = DeviceUtils.px2dp(context, DeviceUtils.getScreenWidth(context));
            int statusHeight = DeviceUtils.px2dp(context, DeviceUtils.getStausHeight(context));
            int windowHeight = screenHeight - 60 - statusHeight - 60; // 60 is the heights of both the status bar and the title bar
            ScanParameter parameter = new ScanParameter();
            parameter.set(ScanParameter.KEY_UI_WINDOW_TOP, 60 + 60);
            parameter.set(ScanParameter.KEY_UI_WINDOW_LEFT, 0);
            parameter.set(ScanParameter.KEY_UI_WINDOW_WIDTH, windowWidth);
            parameter.set(ScanParameter.KEY_UI_WINDOW_HEIGHT, windowHeight);
            parameter.set(ScanParameter.KEY_ENABLE_FLASH_ICON, false);
            parameter.set(ScanParameter.KEY_ENABLE_SWITCH_ICON, false);
            parameter.set(ScanParameter.KEY_ENABLE_INDICATOR_LIGHT, false);
            parameter.set(ScanParameter.KEY_SCAN_TIP_TEXT, "");
            parameter.set(ScanParameter.KEY_SCAN_SECTION_WIDTH, windowWidth);
            parameter.set(ScanParameter.KEY_SCAN_SECTION_HEIGHT, windowHeight);
            parameter.set(ScanParameter.KEY_SCAN_MODE, "overlay");

            try {
                ScanResult scannerResult = scannerDevice.scanBarcode(parameter);
                Log.d(TAG, "stopScan:" + scannerResult);
                String msg;
                if (scannerResult.getResultCode() == ScanResult.SCAN_SUCCESS) {
                    msg = scannerResult.getText() + "\ncode = " + scannerResult.getResultCode();
                } else {
                    msg = "errorcode = " + scannerResult.getResultCode();
                }
                runOnUiThread(() -> {
                    Intent intent = new Intent();
                    intent.putExtra("result", msg);
                    setResult(2, intent);
                    finish();
                });
            } catch (DeviceException e) {
                e.printStackTrace();
            }
        });
    }

    private void runInBackground(Runnable task) {
        new Thread(task).start();
    }


    @Override
    protected void onPause() {
        super.onPause();
        close();
    }

}
