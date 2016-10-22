package org.loveornot.board;

import android.content.Context;
import android.content.SharedPreferences;

/**
 * Created by hunso on 2016-04-02.
 */
public class MenuSharedPreferences {
    public static final String SP_NAME = "MenuInfo";
    SharedPreferences menuInfo;
    public MenuSharedPreferences(Context context) {
        menuInfo = context.getSharedPreferences(SP_NAME, Context.MODE_PRIVATE);
    }

    /* 메뉴 정보 저장 */
    public void storeMenu(String str) {
        SharedPreferences.Editor spEditor = menuInfo.edit();
        spEditor.putString("menu", str);
        spEditor.commit();
    }
    /* 메뉴 정보 출력 */
    public String getMenu() {
        return menuInfo.getString("menu","");
    }
    public void clearMenu() {
        SharedPreferences.Editor spEditor = menuInfo.edit();
        spEditor.clear();
        spEditor.commit();
    }
}
