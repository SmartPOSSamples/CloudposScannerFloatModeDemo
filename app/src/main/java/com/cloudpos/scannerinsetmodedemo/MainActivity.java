package com.cloudpos.scannerinsetmodedemo;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

/**
 * Given feedback on the inflexibility of the previous scanning service UI, which could not fully meet customer UI requirements, we have introduced an embedded mode to the scanning service. This new mode allows clients to customize the UI outside the scanning frame, transforming the scanning frame into a floating window. As a result, clients can interact with both the scanning frame and their own interface simultaneously.
 *
 * This project serves as a demonstration of how to utilize the floating window mode of the scanning service.
 *
 */

public class MainActivity extends AppCompatActivity {


    private Context mContext;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mContext = this;
        initView();
    }

    private void initView() {
        TextView title = (TextView) findViewById(R.id.bar_title);
        ImageView back = (ImageView) findViewById(R.id.bar_back);
        Button scan = (Button) findViewById(R.id.btn_scan);
        RelativeLayout titleBar = (RelativeLayout) findViewById(R.id.titlebar);

        title.setText("InsetModeDemo");
        back.setVisibility(View.GONE);
        titleBar.setBackgroundColor(Color.rgb(0x56, 0xab, 0xe4));
        scan.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(mContext, ScanActivity.class);
                startActivityForResult(intent, 1);
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == 2) {
            CharSequence result = data.getCharSequenceExtra("result");
            Toast.makeText(mContext, result, Toast.LENGTH_LONG).show();
        }
    }
}
