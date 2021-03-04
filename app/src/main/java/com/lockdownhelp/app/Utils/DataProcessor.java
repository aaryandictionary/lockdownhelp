package com.lockdownhelp.app.Utils;

import android.content.Context;
import android.content.SharedPreferences;

public class DataProcessor {

    private SharedPreferences prefs;
    private SharedPreferences.Editor editor;

    private static DataProcessor INSTANCE=null;

    private DataProcessor(SharedPreferences preferences) {
        this.prefs = preferences;
        this.editor = preferences.edit();
    }

    public static synchronized DataProcessor getInstance(Context context){
        if (INSTANCE==null){
            INSTANCE=new DataProcessor(context.getSharedPreferences("lockdown_prefs",Context.MODE_PRIVATE));
        }

        return INSTANCE;
    }

    public void deleteAll(){
        editor.clear();
        editor.apply();
    }

    public  void setLatLng( String lat,String lng,String home_location) {
        editor.putString("user_lat",lat);
        editor.putString("user_lng",lng);
        editor.putString("user_home_location",home_location);
        editor.apply();
    }

    public  void setProfile( String name,String address,String mobile) {
        editor.putString("user_name",name);
        editor.putString("user_address",address);
        editor.putString("user_mobile",mobile);
        editor.apply();
    }

    public  void setUserName( String name) {
        editor.putString("user_name",name);
        editor.apply();
    }

    public  void setInitials( String initials) {
        editor.putString("user_initials",initials);
        editor.apply();
    }

    public  void setDistrictAndState( String district,String state) {
        editor.putString("user_district",district);
        editor.putString("user_state",state);
        editor.apply();
    }

    public  String getUserName() {
        return prefs.getString("user_name","None");
    }
    public  String getUserInitials() {
        return prefs.getString("user_initials","None");
    }
    public  String getUserMobile() {
        return prefs.getString("user_mobile","None");
    }
    public  String getUserAddress() {
        return prefs.getString("user_address","None");
    }
    public  String getUserState() {
        return prefs.getString("user_state","None");
    }
    public  String getUserDistrict() {
        return prefs.getString("user_district","None");
    }


    public  void setHomeLocation( String home_location) {
        editor.putString("user_home_location",home_location);
        editor.apply();
    }

    public  String getLat() {
        return prefs.getString("user_lat","None");
    }

    public  String getLng() {
        return prefs.getString("user_lng","None");
    }

    public  String getHomeLocation() {
        return prefs.getString("user_home_location","None");
    }

}
