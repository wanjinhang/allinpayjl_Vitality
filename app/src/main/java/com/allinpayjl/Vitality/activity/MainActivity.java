package com.allinpayjl.Vitality.activity;

import android.app.AlertDialog;
import android.content.ComponentName;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Switch;
import android.widget.TextView;
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



public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {
    private EditText card_edt = null;
    private EditText phone_edt = null;
    private EditText money_edt = null;
    private EditText quan_edt = null;
    private SocketClient socketClient;
    private long exitTime = 0;
    private float nowVersionCode,saveVersionCode;
    private static final String ISFIRSTLAUCH = "isFirstLauch";
    private static final String SAVEVERSIONCODE = "saveVersionCode";
    private static final String SAVESTATUS = "saveStatus";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        nowVersionCode = getVersionCode();
        SharedPreferences sp  = getSharedPreferences(ISFIRSTLAUCH,MODE_PRIVATE);
        saveVersionCode = sp.getFloat(SAVEVERSIONCODE,0);


        if (nowVersionCode > saveVersionCode) {
            //第一次安装启动
            SharedPreferences.Editor editor = sp.edit();
            editor.putFloat(SAVEVERSIONCODE, nowVersionCode);
            editor.putBoolean(SAVESTATUS, true);
            editor.commit();
            SharedPreferences userSettings = getSharedPreferences("setting", 0);
            SharedPreferences.Editor editor1 = userSettings.edit();
            editor1.putString("serverAddress","10.120.2.173");
            editor1.apply();
            //初始化数据

        }

        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.setDrawerListener(toggle);
        toggle.syncState();
        //设置头部商户号等信息
        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        View drawerView = navigationView.inflateHeaderView(R.layout.nav_header_main);
        ImageView setShopId_nav_header = (ImageView) drawerView.findViewById(R.id.setShopId_nav_header);
        TextView showShopName = (TextView) drawerView.findViewById(R.id.shopName_nav_header);
        TextView showShopId = (TextView) drawerView.findViewById(R.id.shopId_nav_header);

        initHeaderShopName(showShopName,showShopId);

        socketClient = new SocketClient(Utils.getURL(this), Utils.PORT);
        socketClient.setCharsetName("GBK");

        setShopId_nav_header.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent();
                intent.setComponent(new ComponentName("com.allinpay.usdk",
                        "com.allinpay.usdk.MainActivity"));
                Bundle bundle = new Bundle();
                RequestData data = new RequestData();
                data.putValue(RequestData.BUSINESS_ID, Busi_Data.BUSI_MANAGER_APP_CONFIG);

                bundle.putSerializable(RequestData.KEY_ERTRAS, data);
                intent.putExtras(bundle);
                startActivityForResult(intent, USDKRuqester.APP_CONFIG);


            }
        });
        // 设置金额显示
        money_edt = (EditText) findViewById(R.id.card_main_edit_money);
        money_edt.addTextChangedListener(new TextWatcher() {
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
                if (!TextUtils.isEmpty(money_edt.getText().toString()) && !money_edt.getText().toString().equals(numberStr)){
                    money_edt.setText(numberStr);        //赋值到editText上
                    money_edt.setSelection(numberStr.length()); //将光标定位到结尾
                }
            }
        });
        //获取银行卡号
        Button get_card_btn = (Button) findViewById(R.id.card_main_get_cardNum);
        get_card_btn.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                Intent intent = new Intent();
                intent.setComponent(new ComponentName("com.allinpay.usdk",
                        "com.allinpay.usdk.MainActivity"));
                Bundle bundle = new Bundle();
                RequestData data = new RequestData();
                data.putValue(RequestData.BUSINESS_ID, Busi_Data.BUSI_MANAGER_SERVICE_READCARD);

                bundle.putSerializable(RequestData.KEY_ERTRAS, data);
                intent.putExtras(bundle);
                startActivityForResult(intent, USDKRuqester.READ_CARD);
            }
        });

        card_edt = (EditText) findViewById(R.id.card_main_card_num);
//        card_edt.setKeyListener(null);
        quan_edt = (EditText) findViewById(R.id.card_main_quan);
        //是否使用优惠券
        final Switch quan_Switch = (Switch) findViewById(R.id.switch1);
        quan_Switch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if(isChecked){
                    quan_edt.setVisibility(View.VISIBLE);
                    quan_edt.setText("");

                }else{
                    quan_edt.setVisibility(View.GONE);
                    quan_edt.setText("");
                }
            }
        });
        card_edt = (EditText) findViewById(R.id.card_main_card_num);
        phone_edt = (EditText) findViewById(R.id.card_main_phoneNum);
        quan_Switch.isChecked();

        Button bank_pay_btn = (Button) findViewById(R.id.bank_pay_btn);
        //银行卡支付
        bank_pay_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String price= money_edt.getText().toString().replace(".", "");//金额
                String card_num_tmp = card_edt.getText().toString();//银行卡号
                String phone_num = phone_edt.getText().toString();//手机号
                if(!price.equals("")&&!card_num_tmp.equals("")){
                    int i =Integer.parseInt(price);
                    String amount = String.format("%012d", i);
                    String tmp1 =card_num_tmp.substring(0, 6);
                    String tmp2 =card_num_tmp.substring(card_num_tmp.length()-4,card_num_tmp.length());
                    String card_num = tmp1+"****"+tmp2;

                    String quan_num = quan_edt.getText().toString();//优惠码
                    SharedPreferences userSettings= getSharedPreferences("setting", 0);
                    String shop_id = userSettings.getString("shopId","");
                    String ter_id = userSettings.getString("TER_ID","");

                    quan_num =String.format("%1$-18s",quan_num);

                    phone_num =String.format("%1$-19s",phone_num);
                    String ref_no =String.format("%1$-18s","");//流水号

                    GetRequsteStr getRequsetStr1 = new GetRequsteStr(amount,ref_no,shop_id,ter_id,phone_num,card_num,quan_num);
                    final byte[] b_data =getRequsetStr1.getBytes();
                    socketClient = new SocketClient(Utils.getURL(MainActivity.this), Utils.PORT);
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
                                Toast.makeText(MainActivity.this,cnMsg,Toast.LENGTH_LONG).show();
                            }


                        }
                    });
                    socketClient.connect();
                }else{
                    Toast.makeText(MainActivity.this,"请输入填写完整",Toast.LENGTH_LONG).show();
                }

            }
        });

        //扫码支付
        Button scan_pay_btn = (Button) findViewById(R.id.scan_pay_btn);
        scan_pay_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String price= money_edt.getText().toString().replace(".", "");//金额
                if(!price.equals("")){
                    int i =Integer.parseInt(price);
                    String amount = String.format("%012d", i);
                    Intent intent = new Intent();
                    intent.setComponent(new ComponentName("com.allinpay.usdk",
                            "com.allinpay.usdk.MainActivity"));

                    Bundle bundle = new Bundle();
                    RequestData data = new RequestData();

                    data.putValue(RequestData.AMOUNT,amount);
                    data.putValue(RequestData.BUSINESS_ID, Busi_Data.BUSI_SALE_QR);

                    bundle.putSerializable(RequestData.KEY_ERTRAS, data);
                    intent.putExtras(bundle);
                    startActivityForResult(intent, USDKRuqester.SALE_SCAN);

                }else{
                    Toast.makeText(MainActivity.this,"请输入填写金额",Toast.LENGTH_LONG).show();
                }
            }
        });
        Button cash_pay_btn = (Button) findViewById(R.id.cash_pay_btn);
        cash_pay_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                AlertDialog dialog = new AlertDialog.Builder(MainActivity.this)
                        .setTitle("现金支付")
                        .setNegativeButton("取消支付", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                            }
                        })
                        .setPositiveButton("确定支付", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                String price= money_edt.getText().toString().replace(".", "");//金额
                                int i =Integer.parseInt(price);
                                String amount = String.format("%012d", i);
                                String ref_no = "000000000000000000";//交易参考号
                                String card_num = "CASH          ";//交易卡号
                                SharedPreferences userSettings= getSharedPreferences("setting", 0);
                                String shop_id = userSettings.getString("shopId","");
                                String ter_id = userSettings.getString("TER_ID","");


                                String quan_num =String.format("%1$-18s","");

                                String phone_num =String.format("%1$-19s","");

                                ref_no=String.format("%1$-18s",ref_no);
                                GetRequsteStr getRequsetStr1 = new GetRequsteStr(amount,ref_no,shop_id,ter_id,phone_num,card_num,quan_num);
                                final byte[] b_data =getRequsetStr1.getBytes();
                                socketClient = new SocketClient(Utils.getURL(MainActivity.this), Utils.PORT);
                                socketClient.setCharsetName("GBK");
                                socketClient.registerSocketDelegate(new SocketClient.SocketDelegate(){

                                    @Override
                                    public void onConnected(SocketClient client) {

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
                                        Toast.makeText(MainActivity.this,cnMsg,Toast.LENGTH_LONG).show();


                                    }
                                });
                                socketClient.connect();
                            }
                        })
                        .create();
                dialog.show();
            }
        });

        socketClient.disconnect();

    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        }
        if(System.currentTimeMillis() - exitTime > 2000) {
            Toast.makeText(this, "再按一次退出程序", Toast.LENGTH_SHORT).show();
            exitTime = System.currentTimeMillis();
        } else {
            finish();
            System.exit(0);
            android.os.Process.killProcess(android.os.Process.myPid());
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            Intent intent = new Intent(this,SettingActivity.class);
            startActivity(intent);
        }

        return super.onOptionsItemSelected(item);
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        if (id == R.id.nav_pay) {//消费支付

            Intent intent = new Intent(this,MainActivity.class);
            startActivity(intent);
        } else if (id == R.id.nav_void) {//消费撤销

            Intent intent = new Intent(this, VoidActivity.class);
            startActivity(intent);
        } else if (id == R.id.nav_refund) {//退货
            Intent intent = new Intent(this, RefundActivity.class);
            startActivity(intent);

        } else if (id == R.id.nav_balance_bank) {//余额查询

            startIntent(Busi_Data.BUSI_BALANCE_BANK);
        } else if(id == R.id.nav_trans_settle){//终端结算

            startIntent(Busi_Data.BUSI_MANAGER_TRANS_SETTLE);
        }else if(id == R.id.reprint_settle){//重新打印结算单

            startIntent(Busi_Data.BUSI_MANAGER_TRANS_SETTLE);
        }else if(id ==R.id.print_total){//打印汇总
            startIntent(Busi_Data.BUSI_MANAGER_TRANS_PRINT_TOTAL);
        }else if(id ==R.id.print_detail){//打印明细
            startIntent(Busi_Data.BUSI_MANAGER_TRANS_PRINT_DETAIL);
        }else if(id ==R.id.trans_reprint){//打印末笔
            Intent intent = new Intent();
            intent.setComponent(new ComponentName("com.allinpay.usdk",
                    "com.allinpay.usdk.MainActivity"));
            Bundle bundle = new Bundle();
            RequestData data = new RequestData();
            data.putValue(BaseData.ORIG_TRACE_NO,"000000");
            data.putValue(RequestData.BUSINESS_ID, Busi_Data.BUSI_MANAGER_TRANS_REPRINT);

            bundle.putSerializable(RequestData.KEY_ERTRAS, data);
            intent.putExtras(bundle);
            startActivity(intent);
        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {

        Bundle extras = data.getExtras();
        ResponseData respone = (ResponseData) extras.getSerializable(ResponseData.KEY_ERTRAS);
        assert respone != null;
        String code = respone.getValue(BaseData.REJCODE);
        if (requestCode == USDKRuqester.APP_CONFIG) {

            if(code.equals("00")){
                SharedPreferences userSettings = getSharedPreferences("setting", 0);
                SharedPreferences.Editor editor = userSettings.edit();
                editor.putString("shopId",respone.getValue(BaseData.MERCH_ID));
                editor.putString("shopName",respone.getValue(BaseData.MERCH_NAME));
                editor.putString("TER_ID",respone.getValue(BaseData.TER_ID));
                editor.apply();
            }

        }else if(requestCode == USDKRuqester.READ_CARD){

            if(respone.getValue(BaseData.CARDNO).length()>0){
                card_edt.setText(respone.getValue(BaseData.CARDNO));
            }else{
                card_edt.setText("");
            }

        }else if(requestCode ==USDKRuqester.SALE_BANK){
            if(code.equals("00")){
                String amount = respone.getValue(BaseData.AMOUNT);//交易金额
                String ref_no = respone.getValue(BaseData.REF_NO);//交易参考号
                String card_no = respone.getValue(BaseData.CARDNO);//交易卡号
                String shop_id = respone.getValue(BaseData.MERCH_ID);//商户号
                String ter_id = respone.getValue(BaseData.TER_ID);//终端号

                String tmp1 =card_no.substring(0, 6);
                String tmp2 =card_no.substring(card_no.length()-4,card_no.length());
                String card_num = tmp1+"****"+tmp2;

                String quan_num =String.format("%1$-18s","");

                String phone_num =String.format("%1$-19s",phone_edt.getText().toString());

                ref_no=String.format("%1$-18s",ref_no);
                GetRequsteStr getRequsetStr1 = new GetRequsteStr(amount,ref_no,shop_id,ter_id,phone_num,card_num,quan_num);
                final byte[] b_data =getRequsetStr1.getBytes();
                socketClient = new SocketClient(Utils.getURL(MainActivity.this), Utils.PORT);
                socketClient.setCharsetName("GBK");
                socketClient.registerSocketDelegate(new SocketClient.SocketDelegate(){

                    @Override
                    public void onConnected(SocketClient client) {
//                            String str_data = "01170001000000000001123456                              82122014816004200820002621485****424815844460823               ";
                        Log.e("Star",new String(b_data));
                        socketClient.send(b_data);
                    }

                    @Override
                    public void onDisconnected(SocketClient client) {
                        Log.e("DISCON","ahahahah");
                    }

                    @Override
                    public void onResponse(SocketClient client, @NonNull SocketResponsePacket responsePacket) {
                        String str_data = responsePacket.getMessage();
                        String responseCode = null;//返回码
                        String errorCode = null;
                        Log.e("hhhhh",str_data);
                        try {
                            responseCode = getByteStr(str_data,4,4).trim();
                            errorCode = getByteStr(str_data,8,2).trim();
                        } catch (UnsupportedEncodingException e) {
                            e.printStackTrace();
                        }


                        assert responseCode != null;
                        assert errorCode != null;
                        if(responseCode.equals("0002")&&errorCode.equals("00")){
                            String jf_all = null;//总积分
                            String jf_now = null;//本次积分
                            String quan_code = null;//优惠码 200075
                            String quan_type = null;//优惠类型
                            String quan_shop = null;//优惠商家
                            String quan_address =null;//优惠地址
                            String end_time = null;//截止日期

                            try {
                                jf_all = getByteStr(str_data,28,12).trim();
                                jf_now = getByteStr(str_data,40,12).trim();
                                quan_code = getByteStr(str_data, 52,18).trim();
                                quan_type = getByteStr(str_data,70,30).trim();
                                quan_shop = getByteStr(str_data,100,20).trim();
                                quan_address =getByteStr(str_data,120,30).trim();
                                end_time = getByteStr(str_data,150,8).trim();
                            } catch (UnsupportedEncodingException e) {
                                e.printStackTrace();
                            }

                            String print_str = "本次积分："+jf_now+"\r\n";
                            print_str += "总积分："+jf_all+"\r\n";
                            if(!(quan_code != null && quan_code.equals(""))){
                                print_str += "优惠码："+quan_code+"\r\n";
                                print_str += "优惠方式："+quan_type+"\r\n";
                                if(quan_shop != null && quan_shop.equals("")){
                                    print_str += "优惠商家：本商场所有商家\r\n";
                                }else{
                                    print_str += "优惠商家："+quan_shop+"\r\n";
                                    print_str += "优惠地址："+quan_address+"\r\n";
                                }
                                print_str += "截止时间："+end_time+"\r\n";

                            }
                            Log.e("dayin",print_str);

                            Intent intent = new Intent();
                            intent.setComponent(new ComponentName("com.allinpay.usdk",
                                    "com.allinpay.usdk.MainActivity"));
                            Bundle bundle = new Bundle();
                            RequestData data = new RequestData();
                            data.putValue(BaseData.BUSINESS_ID, Busi_Data.BUSI_MANAGER_SERVICE_PRINT);
                            data.putValue(BaseData.PRINT_APPEND_TEXT,print_str);
                            data.putValue(BaseData.PRINT_APPEND_PAGE,1);
                            data.putValue(BaseData.PRINT_APPEND_PIC,null);
                            bundle.putSerializable(RequestData.KEY_ERTRAS, data);
                            intent.putExtras(bundle);
                            startActivityForResult(intent,USDKRuqester.PRINTT_OVER);
                        }else{
                            String cnMsg = null;
                            try {
                                cnMsg = getByteStr(str_data,10,18).trim();
                            } catch (UnsupportedEncodingException e) {
                                e.printStackTrace();
                            }
                            Toast.makeText(MainActivity.this,cnMsg,Toast.LENGTH_LONG).show();
                        }


                    }
                });
                socketClient.connect();
            }else{
                Toast.makeText(MainActivity.this,respone.getValue(BaseData.REJCODE_CN),Toast.LENGTH_LONG).show();
            }
        }else if(requestCode ==USDKRuqester.PRINTT_OVER){
            card_edt.setText("");
            phone_edt.setText("");
            quan_edt.setText("");
            money_edt.setText("0.00");

        }else if(requestCode ==USDKRuqester.SALE_SCAN){
            if(code.equals("00")){
                String amount = respone.getValue(BaseData.AMOUNT);//交易金额
                String ref_no = "000000000000000000";//交易参考号
                String card_no = respone.getValue(BaseData.CARDNO);//交易卡号
                String shop_id = respone.getValue(BaseData.MERCH_ID);//商户号
                String ter_id = respone.getValue(BaseData.TER_ID);//终端号

                String tmp1 =card_no.substring(0, 6);
                String tmp2 =card_no.substring(card_no.length()-4,card_no.length());
                String card_num = tmp1+"****"+tmp2;

                String quan_num =String.format("%1$-18s","");

                String phone_num =String.format("%1$-19s",phone_edt.getText().toString());

//                ref_no=String.format("%1$-18s",ref_no);
                GetRequsteStr getRequsetStr1 = new GetRequsteStr(amount,ref_no,shop_id,ter_id,phone_num,card_num,quan_num);
                final byte[] b_data =getRequsetStr1.getBytes();
                socketClient = new SocketClient(Utils.getURL(MainActivity.this), Utils.PORT);
                socketClient.setCharsetName("GBK");
                socketClient.registerSocketDelegate(new SocketClient.SocketDelegate(){

                    @Override
                    public void onConnected(SocketClient client) {
                        Log.e("Star",new String(b_data));
                        socketClient.send(b_data);
                    }

                    @Override
                    public void onDisconnected(SocketClient client) {
                        Log.e("DISCON","ahahahah");
                    }

                    @Override
                    public void onResponse(SocketClient client, @NonNull SocketResponsePacket responsePacket) {
                        String str_data = responsePacket.getMessage();
                        String responseCode = null;//返回码
                        String errorCode = null;
                        Log.e("hhhhh",str_data);
                        try {
                            responseCode = getByteStr(str_data,4,4).trim();
                            errorCode = getByteStr(str_data,8,2).trim();
                        } catch (UnsupportedEncodingException e) {
                            e.printStackTrace();
                        }


                        assert responseCode != null;
                        assert errorCode != null;
                        if(responseCode.equals("0002")&&errorCode.equals("00")){
                            String jf_all = null;//总积分
                            String jf_now = null;//本次积分
                            String quan_code = null;//优惠码 200075
                            String quan_type = null;//优惠类型
                            String quan_shop = null;//优惠商家
                            String quan_address =null;//优惠地址
                            String end_time = null;//截止日期

                            try {
                                jf_all = getByteStr(str_data,28,12).trim();
                                jf_now = getByteStr(str_data,40,12).trim();
                                quan_code = getByteStr(str_data, 52,18).trim();
                                quan_type = getByteStr(str_data,70,30).trim();
                                quan_shop = getByteStr(str_data,100,20).trim();
                                quan_address =getByteStr(str_data,120,30).trim();
                                end_time = getByteStr(str_data,150,8).trim();
                            } catch (UnsupportedEncodingException e) {
                                e.printStackTrace();
                            }

                            String print_str = "本次积分："+jf_now+"\r\n";
                            print_str += "总积分："+jf_all+"\r\n";
                            if(!(quan_code != null && quan_code.equals(""))){
                                print_str += "优惠码："+quan_code+"\r\n";
                                print_str += "优惠方式："+quan_type+"\r\n";
                                if(quan_shop != null && quan_shop.equals("")){
                                    print_str += "优惠商家：本商场所有商家\r\n";
                                }else{
                                    print_str += "优惠商家："+quan_shop+"\r\n";
                                    print_str += "优惠地址："+quan_address+"\r\n";
                                }
                                print_str += "截止时间："+end_time+"\r\n";

                            }
                            Log.e("dayin",print_str);

                            Intent intent = new Intent();
                            intent.setComponent(new ComponentName("com.allinpay.usdk",
                                    "com.allinpay.usdk.MainActivity"));
                            Bundle bundle = new Bundle();
                            RequestData data = new RequestData();
                            data.putValue(BaseData.BUSINESS_ID, Busi_Data.BUSI_MANAGER_SERVICE_PRINT);
                            data.putValue(BaseData.PRINT_APPEND_TEXT,print_str);
                            data.putValue(BaseData.PRINT_APPEND_PAGE,1);
                            data.putValue(BaseData.PRINT_APPEND_PIC,null);
                            bundle.putSerializable(RequestData.KEY_ERTRAS, data);
                            intent.putExtras(bundle);
                            startActivityForResult(intent,USDKRuqester.PRINTT_OVER);
                        }else{
                            String cnMsg = null;
                            try {
                                cnMsg = getByteStr(str_data,10,18).trim();
                            } catch (UnsupportedEncodingException e) {
                                e.printStackTrace();
                            }
                            Toast.makeText(MainActivity.this,cnMsg,Toast.LENGTH_LONG).show();
                        }


                    }
                });
                socketClient.connect();

        }
        socketClient.disconnect();
    }
    }

    //初始化 标题商户名
    private  void initHeaderShopName(TextView shopName,TextView shopId){
        SharedPreferences userSettings= getSharedPreferences("setting", 0);
        String shopIdStr = userSettings.getString("shopId","");
        String shopNameStr = userSettings.getString("shopName","");
        Log.e("TEST",shopNameStr+"|"+shopIdStr);
        shopId.setText(shopIdStr);
        shopName.setText(shopNameStr);
    }

    /**
     *
      * @param str   源字符串
     * @param start  开始位置
     * @param count   字符串位数
     * @return String
     * @throws UnsupportedEncodingException ssss
     */
    public static String getByteStr(String str, int start, int count) throws UnsupportedEncodingException{
        byte[] b = str.getBytes("GB2312");
        return new String(b, start, count,"GB2312");
    }

    public void startIntent(String type){
        Intent intent = new Intent();
        intent.setComponent(new ComponentName("com.allinpay.usdk",
                "com.allinpay.usdk.MainActivity"));
        Bundle bundle = new Bundle();
        RequestData data = new RequestData();
        data.putValue(RequestData.BUSINESS_ID, type);

        bundle.putSerializable(RequestData.KEY_ERTRAS, data);
        intent.putExtras(bundle);
        startActivity(intent);
    }
    private float getVersionCode(){
        float versionCode = 0;
        try {
            versionCode = getPackageManager().getPackageInfo(getPackageName(),0).versionCode;
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
        return versionCode;
    }


}


