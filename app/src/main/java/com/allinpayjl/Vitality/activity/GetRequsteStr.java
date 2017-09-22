package com.allinpayjl.Vitality.activity;

/**
 * Created by wansui on 2017/9/20.
 */

public class GetRequsteStr {
    private String amount ;//交易金额
    private String ref_no ;//交易参考号
    private String shop_id ;//商户号
    private String ter_id ;//终端号
    private String phone_num ;//手机号
    private String card_num ;//银行卡号
    private String quan_num ;//优惠券号

    public GetRequsteStr(String amount,String ref_no,String shop_id,String ter_id,String phone_num,String card_num,String quan_num){
        this.amount = amount;

        this.ref_no = String.format("%1$-18s",ref_no);
        this.shop_id = shop_id;
        this.ter_id = ter_id;

        this.phone_num =String.format("%1$-19s",phone_num);
        this.card_num = card_num;

        this.quan_num =String.format("%1$-18s",quan_num);
    }
    public String getStr(){
        return "01170001"+this.amount+this.quan_num+this.ref_no+this.shop_id+this.ter_id+this.card_num+this.phone_num;
    }
    public byte[] getBytes(){
        String test_str = this.quan_num+this.ref_no+this.shop_id+this.ter_id;
        byte[] bytes= test_str.getBytes();
        byte[] test2 = Validate(bytes);
        String test_str2 = this.quan_num+this.ref_no;
        byte[] bytes2= test_str2.getBytes();
        byte[] test = Encrypt(bytes2);
        final String str_data = "01170001"+this.amount+this.quan_num+this.ref_no+this.shop_id+this.ter_id+this.card_num+this.phone_num;
        final byte[] b_data = new byte[str_data.length()+7];
        for(int j = 0, k = 0, l = 0; j < b_data.length; j++)
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
        return b_data;
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
