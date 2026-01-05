package com.example.prctc;

import android.content.Context;
import android.content.SharedPreferences;

public class SharedPrefManager {

    private static final String PREF_NAME = "user_pref";
    private static final String KEY_REMEMBER_ME = "remember_me";
    private SharedPreferences sharedPreferences;
    private SharedPreferences.Editor editor;

    public SharedPrefManager(Context context) {
        sharedPreferences = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        editor = sharedPreferences.edit();
    }

    // Save user data (optional, you already have this)
    public void saveUser(String name, String email, String password, String phone, String country) {
        editor.putString("name", name);
        editor.putString("email", email);
        editor.putString("password", password);
        editor.putString("phone", phone);
        editor.putString("country", country);
        editor.apply();
    }

    // Set Remember Me flag
    public void setRememberMe(boolean value) {
        editor.putBoolean(KEY_REMEMBER_ME, value);
        editor.apply();
    }

    // Get Remember Me flag
    public boolean isRememberMe() {
        return sharedPreferences.getBoolean(KEY_REMEMBER_ME, false);
    }

    // Clear Remember Me (optional, logout)
    public void clear() {
        editor.clear();
        editor.apply();
    }
}
