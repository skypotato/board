package org.loveornot.board;

import android.content.Context;
import android.content.SharedPreferences;

/**
 * Created by hunso on 2016-04-02.
 * SharedPreference 부분
 */
class MenuSharedPreferences {
    private static final String SP_NAME = "MenuInfo";
    private SharedPreferences menuInfo;

    MenuSharedPreferences(Context context) {
        menuInfo = context.getSharedPreferences(SP_NAME, Context.MODE_PRIVATE);
    }

    /* 검색 정보 저장 */
    void storeSearch(String str) {
        SharedPreferences.Editor spEditor = menuInfo.edit();
        spEditor.putString("search", str);
        spEditor.apply();
    }

    /* 검색 정보 출력 */
    String getSearch() {
        return menuInfo.getString("search", "");
    }


    /* 메뉴 정보 저장 */
    void storeMenu(String str) {
        SharedPreferences.Editor spEditor = menuInfo.edit();
        spEditor.putString("menu", str);
        spEditor.apply();
    }

    /* 메뉴 정보 출력 */
    public String getMenu() {
        return menuInfo.getString("menu", "");
    }

    public void clearMenu() {
        SharedPreferences.Editor spEditor = menuInfo.edit();
        spEditor.clear();
        spEditor.apply();
    }
}
