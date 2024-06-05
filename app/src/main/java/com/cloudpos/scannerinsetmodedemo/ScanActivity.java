package com.cloudpos.scannerinsetmodedemo;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.RemoteException;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.cloudpos.scanserver.aidl.IScanService;
import com.cloudpos.scanserver.aidl.ScanParameter;
import com.cloudpos.scanserver.aidl.ScanResult;
import com.cloudpos.scannerinsetmodedemo.aidlcontrol.AidlController;
import com.cloudpos.scannerinsetmodedemo.aidlcontrol.IAIDLListener;
import com.cloudpos.scannerinsetmodedemo.utils.DeviceUtils;

/**
 * Created by gecx on 17-1-20.
 */

public class ScanActivity extends Activity implements IAIDLListener, View.OnClickListener {

    private Context context;
    private String TAG = "ScanActivity";


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
        back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    scanService.stopScan();
                } catch (RemoteException e) {
                    e.printStackTrace();
                }
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        AidlController.getInstance().startScanService(context, this);

    }

    boolean flashlight;
    boolean indicatorlight;
    int cameraIndex;

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.changecamera:
                Intent intent1 = new Intent();
                intent1.setAction(ScanParameter.BROADCAST_SET_CAMERA);
                if (cameraIndex == 0) {
                    intent1.putExtra(ScanParameter.BROADCAST_VALUE, 1);
                    cameraIndex = 1;
                } else if (cameraIndex == 1) {
                    intent1.putExtra(ScanParameter.BROADCAST_VALUE, 0);
                    cameraIndex = 0;
                }
                sendBroadcast(intent1);

                break;
            case R.id.changeflashlight:
                Intent intent2 = new Intent();
                intent2.setAction(ScanParameter.BROADCAST_SET_FLASHLIGHT);
                if (flashlight) {
                    intent2.putExtra(ScanParameter.BROADCAST_VALUE, false);
                    flashlight = false;
                } else {
                    intent2.putExtra(ScanParameter.BROADCAST_VALUE, true);
                    flashlight = true;
                }
                sendBroadcast(intent2);
                break;
            case R.id.changeindicator:
                Intent intent3 = new Intent();
                intent3.setAction(ScanParameter.BROADCAST_SET_INDICATOR);
                if (indicatorlight) {
                    intent3.putExtra(ScanParameter.BROADCAST_VALUE, false);
                    indicatorlight = false;
                } else {
                    intent3.putExtra(ScanParameter.BROADCAST_VALUE, true);
                    indicatorlight = true;
                }
                sendBroadcast(intent3);

                break;
        }
    }

    // It's necessary to calculate the heights of both the status bar and the title bar, with the final values to be provided in density-independent pixels (dp).
    public void beginScan() {
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
            ScanResult scannerResult = scanService.scanBarcode(parameter);
            Log.d(TAG, "stopScan:" + scannerResult);
            String msg;
            if (scannerResult.getResultCode() == ScanResult.SCAN_SUCCESS) {
                msg = scannerResult.getText() + "\ncode = " + scannerResult.getResultCode();
            } else {
                msg = "errorcode = " + scannerResult.getResultCode();
            }
            Intent intent = new Intent();
            intent.putExtra("result", msg);
            setResult(2, intent);
            finish();
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void onPause() {
        try {
            scanService.stopScan();
        } catch (RemoteException e) {
            e.printStackTrace();
        }
        super.onPause();
    }

    @Override
    protected void onDestroy() {
        if(scanService != null){
            this.unbindService(scanConn);
            scanService = null;
            scanConn = null;
        }
        super.onDestroy();
    }
    private IScanService scanService; //Scan code service

    private ServiceConnection scanConn;


    @Override
    public void serviceConnected(Object objService, ServiceConnection connection) {
        if(objService instanceof IScanService){
            scanService = (IScanService) objService;
            scanConn = connection;
            new Thread(new Runnable() {
                @Override
                public void run() {
                    beginScan();
                }
            }).start();
        }
    }
}
