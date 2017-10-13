package com.allinpayjl.Vitality.activity;


import android.content.ComponentName;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.allinpay.usdk.core.data.BaseData;
import com.allinpay.usdk.core.data.Busi_Data;
import com.allinpay.usdk.core.data.RequestData;
import com.allinpay.usdk.core.data.ResponseData;
import com.allinpayjl.Vitality.Utils.USDKRuqester;
import com.allinpayjl.Vitality.Utils.Utils;


public class SettingActivity extends AppCompatActivity  implements View.OnClickListener {
    public EditText shopName = null;
    public EditText shopId = null;
    EditText serverAddressET = null;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_setting);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("设置");
        shopName = (EditText) findViewById(R.id.setting_shopName);
        shopId = (EditText) findViewById(R.id.setting_shopId);
        initHeaderShopName(shopName,shopId);
        Button get_shopInfo = (Button) findViewById(R.id.get_shopInfo);
        Button setting_save = (Button) findViewById(R.id.setting_save);
        serverAddressET = (EditText) findViewById(R.id.setting_serverAddress);
        get_shopInfo.setOnClickListener(this);
        setting_save.setOnClickListener(this);
        String serverAddress = Utils.getURL(this);
        serverAddressET.setText(serverAddress);


    }
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                finish();
                break;
        }
        return super.onOptionsItemSelected(item);
    }


    /**
     * Called when a view has been clicked.
     *
     * @param v The view that was clicked.
     */
    @Override
    public void onClick(View v) {

        switch (v.getId()) {
            case R.id.get_shopInfo:
                Intent intent = new Intent();
                intent.setComponent(new ComponentName("com.allinpay.usdk",
                        "com.allinpay.usdk.MainActivity"));
                Bundle bundle = new Bundle();
                RequestData data = new RequestData();
                data.putValue(RequestData.BUSINESS_ID, Busi_Data.BUSI_MANAGER_APP_CONFIG);

                bundle.putSerializable(RequestData.KEY_ERTRAS, data);
                intent.putExtras(bundle);
                startActivityForResult(intent, USDKRuqester.APP_CONFIG);
                break;
            case R.id.setting_save:
                //响应Clicked事件

                String serverAddress = serverAddressET.getText().toString();
                SharedPreferences userSettings = getSharedPreferences("setting", 0);
                SharedPreferences.Editor editor = userSettings.edit();
                editor.putString("serverAddress",serverAddress);
                editor.apply();
                Toast.makeText(this,"成功保存",Toast.LENGTH_LONG).show();
                break;
            default:
                break;

        }

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == USDKRuqester.APP_CONFIG) {
            Bundle extras = data.getExtras();
            ResponseData respone = (ResponseData) extras.getSerializable(ResponseData.KEY_ERTRAS);
            assert respone != null;
            String code = respone.getValue(BaseData.REJCODE);

            if(code.equals("00")){
                SharedPreferences userSettings = getSharedPreferences("setting", 0);
                SharedPreferences.Editor editor = userSettings.edit();
                editor.putString("shopId",respone.getValue(BaseData.MERCH_ID));
                editor.putString("shopName",respone.getValue(BaseData.MERCH_NAME));
                editor.putString("TER_ID",respone.getValue(BaseData.TER_ID));
                editor.apply();
            }
            initHeaderShopName(shopName,shopId);
        }
    }
    //初始化 标题商户名

    private  void initHeaderShopName(TextView shopName, TextView shopId){
        SharedPreferences userSettings= getSharedPreferences("setting", 0);
        String shopIdStr = userSettings.getString("shopId","");
        String shopNameStr = userSettings.getString("shopName","");
        Log.e("TEST",shopNameStr+"|"+shopIdStr);
        shopId.setText(shopIdStr);
        shopName.setText(shopNameStr);
    }





}
