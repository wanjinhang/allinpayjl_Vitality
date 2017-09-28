package com.allinpayjl.Vitality.activity;

import android.content.ComponentName;
import android.content.Intent;
import android.content.SharedPreferences;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
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


public class RefundActivity extends AppCompatActivity implements View.OnClickListener{
    private SocketClient socketClient;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_refund);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("退货");
        socketClient = new SocketClient(Utils.URL, Utils.PORT);
        socketClient.setCharsetName("GBK");

        Button bank_refund_btn = (Button) findViewById(R.id.bank_refund_btn);
        Button scan_refund_btn = (Button) findViewById(R.id.scan_refund_btn);
        bank_refund_btn.setOnClickListener(this);
        scan_refund_btn.setOnClickListener(this);

        final EditText orig_amount = (EditText) findViewById(R.id.orig_amount);
        orig_amount.addTextChangedListener(new TextWatcher() {
            private String  numberStr;       //定义一个字符串来得到处理后的金额
            @Override
            public void beforeTextChanged(CharSequence s, int start, int
                    count, int after) {
            }
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

                int lenght = s.length();
//                Log.e("Sun", s + "====" + start + "=======" + before + "======" + count);
                double number;           //初始金额
                //第一次输入初始化 金额值
                if (lenght <= 1) {
                    number = Double.parseDouble(s.toString());
                    number = number / 100;//第一次 长度等于
                    numberStr = number + "";
                } else {
                    //之后的输入带入算法后将值设置给 金额值
                    if (s.toString().contains(".")) {
                        numberStr = getMoneyString(s.toString());  //这个方法看第三步
                    }
                }
            }
            private String getMoneyString(String money){
                String overMoney;//结果
                String[] pointBoth = money.split("\\.");//分隔点前点后
                String beginOne = pointBoth[0].substring(pointBoth[0].length()-1);//前一位
                String endOne = pointBoth[1].substring(0, 1);//后一位
                //小数点前一位前面的字符串，小数点后一位后面
                String beginPoint = pointBoth[0].substring(0,pointBoth[0].length()-1);
                String endPoint = pointBoth[1].substring(1);
                //根据输入输出拼点
                if (pointBoth[1].length()>2){//说明输入，小数点要往右移
                    overMoney=  pointBoth[0]+endOne+"."+endPoint;//拼接实现右移动
                }else if (
                        pointBoth[1].length()<2){//说明回退,小数点左移
                    overMoney = beginPoint+"."+beginOne+pointBoth[1];//拼接实现左移
                }else {
                    overMoney = money;
                }
                //去除点前面的0 或者补 0
                String overLeft = overMoney.substring(0,overMoney.indexOf("."));//得到前面的字符串
                if (overLeft.equals("") || overLeft.length() < 1){//如果没有就补零
                    overMoney = "0"+overMoney;
                }else if(overLeft.length() > 1 && "0".equals(overLeft.subSequence(0, 1))){//如果前面有俩个零
                    overMoney = overMoney.substring(1);//去第一个0
                }
                return overMoney;
            }

            @Override
            public void afterTextChanged(Editable s) {
                //在此判断输入框的值是否等于金额的值，如果不相同则赋值，如果不判断监听器将会出现死循环
                if (!TextUtils.isEmpty(orig_amount.getText().toString()) && !orig_amount.getText().toString().equals(numberStr)){
                    orig_amount.setText(numberStr);        //赋值到editText上
                    orig_amount.setSelection(numberStr.length()); //将光标定位到结尾
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

    /**
     * Called when a view has been clicked.
     *
     * @param v The view that was clicked.
     */
    @Override
    public void onClick(View v) {
        Log.e("haha","jjjjjj");
        Intent intent = new Intent();
        intent.setComponent(new ComponentName("com.allinpay.usdk",
                "com.allinpay.usdk.MainActivity"));

        Bundle bundle = new Bundle();
        RequestData data = new RequestData();
        EditText orig_ref_no = (EditText) findViewById(R.id.orig_ref_no);
        String ref_no = orig_ref_no.getText().toString();
        EditText orig_amount = (EditText) findViewById(R.id.orig_amount);
        String price= orig_amount.getText().toString().replace(".", "");
        int i =Integer.parseInt(price);
        String a = String.format("%012d", i);
        EditText orig_date = (EditText) findViewById(R.id.orig_date);
        String date = orig_date.getText().toString();
        data.putValue(RequestData.ORIG_REF_NO,ref_no);
        data.putValue(RequestData.AMOUNT,a);
        data.putValue(RequestData.ORIG_DATE,date);
        switch (v.getId()) {

            case R.id.bank_refund_btn:
                getRefund(0);

                break;
            case R.id.scan_refund_btn:
                //响应Clicked事件
                getRefund(1);
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
                        Toast.makeText(RefundActivity.this,cnMsg,Toast.LENGTH_LONG).show();
//                        Intent intent = new Intent(RefundActivity.this,MainActivity.class);
//                        startActivity(intent);

                    }
                });
                socketClient.connect();
            }else{
                Toast.makeText(RefundActivity.this,responseStr.getValue(BaseData.REJCODE_CN),Toast.LENGTH_LONG).show();
            }
        }
        socketClient.disconnect();
    }
    public static String getByteStr(String str, int start, int count) throws UnsupportedEncodingException{
        byte[] b = str.getBytes("GB2312");
        return new String(b, start, count,"GB2312");
    }
    public void getRefund(final int type){

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
            socketClient = new SocketClient(Utils.URL, Utils.PORT);
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
                        EditText orig_ref_no = (EditText) findViewById(R.id.orig_ref_no);
                        String ref_no = orig_ref_no.getText().toString();
                        EditText orig_amount = (EditText) findViewById(R.id.orig_amount);
                        String price= orig_amount.getText().toString().replace(".", "");
                        int i =Integer.parseInt(price);
                        String a = String.format("%012d", i);

                        data.putValue(RequestData.ORIG_REF_NO,ref_no);
                        data.putValue(RequestData.AMOUNT,a);
                        EditText orig_date = (EditText) findViewById(R.id.orig_date);
                        String date = orig_date.getText().toString();
                        data.putValue(RequestData.ORIG_DATE,date);
                        if(type ==0){

                            data.putValue(RequestData.BUSINESS_ID, Busi_Data.BUSI_REFUND_BANK);
                        }else if(type ==1){

                            EditText void_trans_num = (EditText) findViewById(R.id.orig_trans_num);
                            String transNum = void_trans_num.getText().toString();
                            data.putValue(RequestData.ORIG_TRACE_NO,transNum);
                            data.putValue(RequestData.BUSINESS_ID, Busi_Data.BUSI_REFUND_QR);
                        }



                        bundle.putSerializable(RequestData.KEY_ERTRAS, data);
                        intent.putExtras(bundle);
                        startActivityForResult(intent, USDKRuqester.SALE_VOID);



                    }else{
                        String cnMsg = responseMsg.substring(10,19);
                        Toast.makeText(RefundActivity.this,cnMsg,Toast.LENGTH_LONG).show();
                    }


                }
            });
            socketClient.connect();
        }else{
            Toast.makeText(RefundActivity.this,"请输入填写完整",Toast.LENGTH_LONG).show();
        }
    }
}
