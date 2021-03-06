package com.allinpayjl.Vitality.activity;

import android.content.ComponentName;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.allinpay.usdk.core.data.BaseData;
import com.allinpay.usdk.core.data.Busi_Data;
import com.allinpay.usdk.core.data.RequestData;
import com.allinpay.usdk.core.data.ResponseData;
import com.allinpayjl.Vitality.Utils.GetRequsteStr;
import com.allinpayjl.Vitality.Utils.USDKRuqester;
import com.allinpayjl.Vitality.Utils.Utils;
import com.vilyever.socketclient.SocketClient;
import com.vilyever.socketclient.SocketResponsePacket;

import java.io.UnsupportedEncodingException;


public class VoidActivity extends AppCompatActivity  implements View.OnClickListener {
    private SocketClient socketClient;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_void);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("消费撤销");

        socketClient = new SocketClient(Utils.getURL(VoidActivity.this), Utils.PORT);
        socketClient.setCharsetName("GBK");
        Button bankVoidBtn = (Button) findViewById(R.id.bank_void_btn);
        Button scanVoidBtn = (Button) findViewById(R.id.scan_void_btn);
        bankVoidBtn.setOnClickListener(this);
        scanVoidBtn.setOnClickListener(this);


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

            case R.id.bank_void_btn:
                getVoid(0);
                break;
            case R.id.scan_void_btn:
                //响应Clicked事件
                getVoid(1);
                break;
            default:
                break;

        }

    }
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        Bundle extras = data.getExtras();
        ResponseData responseStr = (ResponseData) extras.getSerializable(ResponseData.KEY_ERTRAS);
        assert responseStr != null;
        String code = responseStr.getValue(BaseData.REJCODE);
        if (requestCode ==USDKRuqester.SALE_VOID ) {
            if(code.equals("00")){
                String amount ="000000000000";//交易金额
                String ref_no = responseStr.getValue(BaseData.REF_NO);//交易参考号
                String shop_id = responseStr.getValue(BaseData.MERCH_ID);//商户号
                String ter_id = responseStr.getValue(BaseData.TER_ID);//终端号
                String phone_num = "";
                String card_num = "";
                String quan_num ="";
                GetRequsteStr getRequsetStr = new GetRequsteStr(amount,ref_no,shop_id,ter_id,phone_num,card_num,quan_num);
                final byte[] b_data =getRequsetStr.getBytes();

                socketClient.registerSocketDelegate(new SocketClient.SocketDelegate(){

                    @Override
                    public void onConnected(SocketClient client) {
//                            String str_data = "01170001000000000001123456                              82122014816004200820002621485****424815844460823               ";
                        socketClient.send(b_data);
                    }

                    @Override
                    public void onDisconnected(SocketClient client) {


                    }

                    @Override
                    public void onResponse(SocketClient client, @NonNull SocketResponsePacket responsePacket) {

                        String str_data = responsePacket.getMessage();
                        String cnMsg = null;
                        try {
                            cnMsg = getByteStr(str_data,10,18).trim();
                        } catch (UnsupportedEncodingException e) {
                            e.printStackTrace();
                        }
                        Toast.makeText(VoidActivity.this,cnMsg,Toast.LENGTH_LONG).show();

                    }
                });
                socketClient.connect();
            }else{
                Toast.makeText(VoidActivity.this,responseStr.getValue(BaseData.REJCODE_CN),Toast.LENGTH_LONG).show();
            }
        }
        socketClient.disconnect();
    }

    public static String getByteStr(String str, int start, int count) throws UnsupportedEncodingException{
        byte[] b = str.getBytes("GB2312");
        return new String(b, start, count,"GB2312");
    }

    public void getVoid(final int type){

        EditText orig_ref_no = (EditText) findViewById(R.id.orig_ref_no);
        String ref_no= orig_ref_no.getText().toString();//参考号

        if(!ref_no.equals("")){

            String amount = "000000000000";
            String card_num = String.format("%1$-14s","");

            String quan_num = String.format("%1$-18s","");//优惠码
            SharedPreferences userSettings= getSharedPreferences("setting", 0);
            String shop_id = userSettings.getString("shopId","");
            String ter_id = userSettings.getString("TER_ID","");
            String phone_num =String.format("%1$-19s","");
            ref_no =String.format("%1$-18s",ref_no);//流水号

            GetRequsteStr getRequsetStr1 = new GetRequsteStr(amount,ref_no,shop_id,ter_id,phone_num,card_num,quan_num);
            final byte[] b_data =getRequsetStr1.getBytes();
            socketClient = new SocketClient(Utils.getURL(VoidActivity.this), Utils.PORT);
            socketClient.setCharsetName("GBK");
            socketClient.registerSocketDelegate(new SocketClient.SocketDelegate(){

                @Override
                public void onConnected(SocketClient client) {
//                            String str_data = "01170001000000000001123456                              82122014816004200820002621485****424815844460823               ";
                    socketClient.send(b_data);
                }

                @Override
                public void onDisconnected(SocketClient client) {

                }

                @Override
                public void onResponse(SocketClient client, @NonNull SocketResponsePacket responsePacket) {
                    String responseMsg = responsePacket.getMessage();

                    assert responseMsg != null;
                    String responseCode = responseMsg.substring(4,8);//返回码
                    String errorCode = responseMsg.substring(8,10);
                    Log.e("TEXT",responseMsg);
                    Log.e("Code",responseCode+"|"+errorCode);
                    if(responseCode.equals("0002")&&errorCode.equals("00")){
                        Intent intent = new Intent();
                        intent.setComponent(new ComponentName("com.allinpay.usdk",
                                "com.allinpay.usdk.MainActivity"));

                        Bundle bundle = new Bundle();
                        RequestData data = new RequestData();
                        if(type ==0){
                            EditText void_trace_no = (EditText) findViewById(R.id.orig_trace_no);
                            String traceNo = void_trace_no.getText().toString();
                            data.putValue(RequestData.ORIG_TRACE_NO,traceNo);
                            data.putValue(RequestData.BUSINESS_ID, Busi_Data.BUSI_VOID_BANK);
                        }else if(type ==1){

                            EditText void_trans_num = (EditText) findViewById(R.id.orig_trans_num);
                            String transNum = void_trans_num.getText().toString();
                            data.putValue(RequestData.ORIG_TRACE_NO,transNum);
                            data.putValue(RequestData.BUSINESS_ID, Busi_Data.BUSI_VOID_QR);
                        }



                        bundle.putSerializable(RequestData.KEY_ERTRAS, data);
                        intent.putExtras(bundle);
                        startActivityForResult(intent, USDKRuqester.SALE_VOID);



                    }else{
                        String cnMsg = responseMsg.substring(10,19);
                        Toast.makeText(VoidActivity.this,cnMsg,Toast.LENGTH_LONG).show();
                    }


                }
            });
            socketClient.connect();
        }else{
            Toast.makeText(VoidActivity.this,"请输入填写完整",Toast.LENGTH_LONG).show();
        }
    }

}
