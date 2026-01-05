package com.example.prctc;

import android.content.Context;
import android.content.SharedPreferences;

public class SharedPrefManager {

    private static final String PREF_NAME = "UserPrefs";
    private static final String KEY_NAME = "name";
    private static final String KEY_EMAIL = "email";
    private static final String KEY_PASSWORD = "password";
    private static final String KEY_PHONE = "phone";
    private static final String KEY_COUNTRY = "country";
    private static final String KEY_REMEMBER_ME = "rememberMe";

    private SharedPreferences sharedPreferences;
    private SharedPreferences.Editor editor;

    public SharedPrefManager(Context context) {
        sharedPreferences = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        editor = sharedPreferences.edit();
    }

    public void saveUser(String name, String email, String password, String phone, String country) {
        editor.putString(KEY_NAME, name);
        editor.putString(KEY_EMAIL, email);
        editor.putString(KEY_PASSWORD, password);
        editor.putString(KEY_PHONE, phone);
        editor.putString(KEY_COUNTRY, country);
        editor.apply();
    }

    public void setRememberMe(boolean isChecked) {
        editor.putBoolean(KEY_REMEMBER_ME, isChecked);
        editor.apply();
    }

    public boolean isRemembered() {
        return sharedPreferences.getBoolean(KEY_REMEMBER_ME, false);
    }

    public String getEmail() {
        return sharedPreferences.getString(KEY_EMAIL, "");
    }

    public String getPassword() {
        return sharedPreferences.getString(KEY_PASSWORD, "");
    }
    
    public String getName() {
        return sharedPreferences.getString(KEY_NAME, "User");
    }

    public void logout() {
        editor.putBoolean(KEY_REMEMBER_ME, false);
        editor.apply();
    }

    public void clearAll() {
        editor.clear();
        editor.apply();
    }
}
