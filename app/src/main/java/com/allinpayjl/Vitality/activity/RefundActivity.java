package com.allinpayjl.Vitality.activity;

import android.content.ComponentName;
import android.content.Intent;
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
import android.widget.EditText;
import android.widget.Toast;

import com.allinpay.usdk.core.data.BaseData;
import com.allinpay.usdk.core.data.Busi_Data;
import com.allinpay.usdk.core.data.RequestData;
import com.allinpay.usdk.core.data.ResponseData;
import com.allinpayjl.Vitality.Utils.GetRequsteStr;
import com.allinpayjl.Vitality.Utils.USDKRuqester;
import com.vilyever.socketclient.SocketClient;
import com.vilyever.socketclient.SocketResponsePacket;


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
        Intent intent = new Intent();
        intent.setComponent(new ComponentName("com.allinpay.usdk",
                "com.allinpay.usdk.MainActivity"));

        Bundle bundle = new Bundle();
        RequestData data = new RequestData();
        EditText orig_ref_no = (EditText) findViewById(R.id.orig_ref_no);
        String ref_no = orig_ref_no.getText().toString();
        EditText orig_amount = (EditText) findViewById(R.id.orig_amount);
        String amount = orig_amount.getText().toString();
        int i =Integer.parseInt(amount);
        String a = String.format("%012d", i);
        EditText orig_date = (EditText) findViewById(R.id.orig_date);
        String date = orig_date.getText().toString();
        data.putValue(RequestData.ORIG_TRACE_NO,ref_no);
        data.putValue(RequestData.AMOUNT,a);
        data.putValue(RequestData.ORIG_DATE,date);
        switch (v.getId()) {

            case R.id.bank_refund_btn:

                data.putValue(RequestData.BUSINESS_ID, Busi_Data.BUSI_VOID_BANK);

                break;
            case R.id.scan_refund_btn:
                //响应Clicked事件
                EditText orig_trans_num = (EditText) findViewById(R.id.orig_trans_num);
                String trans_num = orig_trans_num.getText().toString();
                data.putValue(RequestData.ORIG_TRACE_NO,trans_num);
                data.putValue(RequestData.BUSINESS_ID, Busi_Data.BUSI_VOID_QR);
                break;
            default:
                break;

        }
        bundle.putSerializable(RequestData.KEY_ERTRAS, data);
        intent.putExtras(bundle);
        startActivityForResult(intent, USDKRuqester.SALE_VOID);
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

                        Intent intent = new Intent(RefundActivity.this,MainActivity.class);
                        startActivity(intent);

                    }
                });
                socketClient.connect();
            }else{
                Toast.makeText(RefundActivity.this,responseStr.getValue(BaseData.REJCODE_CN),Toast.LENGTH_LONG).show();
            }
        }
        socketClient.disconnect();
    }
}
