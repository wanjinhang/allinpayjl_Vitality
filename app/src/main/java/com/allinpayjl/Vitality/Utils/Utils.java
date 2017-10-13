package com.allinpayjl.Vitality.Utils;


import android.content.Context;
import android.content.SharedPreferences;

public class Utils {

    public static int PORT=4576;
//    public static String URL="10.120.2.173";

    public static String getURL(Context context){
        SharedPreferences userSettings= context.getSharedPreferences("setting", 0);
        String serverAddress = userSettings.getString("serverAddress","");
        return serverAddress;
    }


}
