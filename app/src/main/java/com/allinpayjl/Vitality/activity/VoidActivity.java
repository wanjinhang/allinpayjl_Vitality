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

import com.allinpay.usdk.core.data.Busi_Data;
import com.allinpay.usdk.core.data.RequestData;
import com.vilyever.socketclient.SocketClient;
import com.vilyever.socketclient.SocketResponsePacket;


public class VoidActivity extends AppCompatActivity {
    private SocketClient socketClient;
    private String url = "10.120.2.173";
    private int port = 4576;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_void);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
        socketClient = new SocketClient(url, port);
        socketClient.setCharsetName("GBK");
        final EditText void_ref_no = (EditText) findViewById(R.id.void_ref_no);
        final EditText void_trace_no = (EditText) findViewById(R.id.void_trace_no);
        Button voidSubmit = (Button) findViewById(R.id.voidSubmit);

        voidSubmit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String ref_no = void_ref_no.getText().toString();//参考号
                String trace_no = void_trace_no.getText().toString();//凭证号==流水号(文档)
                if(!ref_no.equals("")&&!trace_no.equals("")){
                    String amount = "            ";
                    String card_num = "000000****0000";
                    String phone_num ="                   ";
                    String quan_num = "                  ";//优惠码
                    SharedPreferences userSettings= getSharedPreferences("setting", 0);
                    String shopIdStr = userSettings.getString("shopId","");
                    String t_code = userSettings.getString("TER_ID","");


                    int n =18 - ref_no.length();
                    for (int j=0;j<=n;j++){
                        ref_no+=" ";
                    }

                    String test_str = quan_num+ref_no+shopIdStr+t_code;
                    Log.e("haha",test_str);
                    byte[] bytes= test_str.getBytes();
                    byte[] test2 = Validate(bytes);
                    String test_str2 = quan_num+ref_no;
                    byte[] bytes2= test_str2.getBytes();
                    byte[] test = Encrypt(bytes2);
                    final String str_data = "01170001"+amount+quan_num+ref_no+shopIdStr+t_code+card_num+phone_num;
                    final byte[] b_data = new byte[str_data.length()+7];
                    for(int j = 0, k = 0, l = 0; j < b_data.length+7; j++)
                    {
                        if (j < 20)
                        {
                            b_data[j] = (byte)str_data.toCharArray()[j];
                        }
                        else if (j < 56)
                        {
                            b_data[j] = test[l++];
                        }
                        else if (j < 112)
                        {
                            b_data[j] = (byte)str_data.toCharArray()[j];
                        }
                        else if (j < 119)
                        {
                            b_data[j] = test2[k++];
                        }
                    }

                    socketClient = new SocketClient(url, port);
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
                                String amount = responseMsg.substring(28,40);
                                Intent intent = new Intent();
                                intent.setComponent(new ComponentName("com.allinpay.usdk",
                                        "com.allinpay.usdk.MainActivity"));

                                Bundle bundle = new Bundle();
                                RequestData data = new RequestData();

                                data.putValue(RequestData.AMOUNT,amount);
                                data.putValue(RequestData.BUSINESS_ID, Busi_Data.BUSI_SALE_BANK);

                                bundle.putSerializable(RequestData.KEY_ERTRAS, data);
                                intent.putExtras(bundle);
                                startActivityForResult(intent, USDKRuqester.SALE_BANK);



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
        });


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
    private byte[] Encrypt(byte[] ch)
    {
        byte key = (byte) ((0x9C ^ 0xA2 ^ 0xC0 ^ 0xF6 ^ 0xA5 ^ 0xEB ^ 0x82)&0xFF);
        for (int i = 0; i < ch.length; i++)
        {
            ch[i] = (byte)((ch[i] ^ key)&0xFF);
        }
        return ch;
    }

    private byte[] Validate(byte[] ch)
    {
        int value = 0;
        for (byte aCh : ch) {
            value ^= aCh & 0xFF;
        }

        byte[] li = new byte[7];
        li[0] = (byte)(value ^ 0x9C);
        li[1] = (byte)(value ^ 0xA2);
        li[2] = (byte)(value ^ 0xC0);
        li[3] = (byte)(value ^ 0xF6);
        li[4] = (byte)(value ^ 0xA5);
        li[5] = (byte)(value ^ 0xEB);
        li[6] = (byte)(value ^ 0x82);
        return li;
    }


}
