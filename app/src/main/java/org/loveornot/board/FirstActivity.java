package org.loveornot.board;

import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.drawable.AnimationDrawable;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.DisplayMetrics;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.Toast;

public class FirstActivity extends AppCompatActivity {

    private MenuSharedPreferences menuSharedPreferences;
    private boolean switchFlag = false;

    private AnimationDrawable frameAnimation;

    private ImageView imageBottom;

    private ImageButton centerBt;
    private ImageButton sellBt;
    private ImageButton buyBt;
    private ImageButton freeBt;
    private ImageButton unknownBt;
    private ImageButton transBt;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_first);
         /*menuInfo설정*/
        menuSharedPreferences = new MenuSharedPreferences(this);

        /*광고 FrameAnimation 설정*/
        imageBottom = (ImageView)findViewById(R.id.imageBottom);
        imageBottom.setBackgroundResource(R.drawable.ad_list);
        frameAnimation = (AnimationDrawable) imageBottom.getBackground();
        frameAnimation.start();

        sellBt = (ImageButton) findViewById(R.id.sellBt);
        buyBt = (ImageButton) findViewById(R.id.buyBt);
        freeBt = (ImageButton) findViewById(R.id.freeBt);
        sellBt = (ImageButton) findViewById(R.id.sellBt);
        unknownBt = (ImageButton) findViewById(R.id.unknownBt);
        transBt = (ImageButton) findViewById(R.id.transBt);

        centerBt = (ImageButton) findViewById(R.id.centerBt);
        centerBt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (switchFlag == false) {
                    /*이미지와 크기 변경*/
                    int weight = (int) convertDpToPixel(80, getApplicationContext());
                    centerBt.setLayoutParams(new ViewGroup.LayoutParams(weight, weight));
                    centerBt.setBackgroundResource(R.drawable.circle_on);
                    /*버튼 보이기*/
                    sellBt.setVisibility(View.VISIBLE);
                    buyBt.setVisibility(View.VISIBLE);
                    freeBt.setVisibility(View.VISIBLE);
                    sellBt.setVisibility(View.VISIBLE);
                    unknownBt.setVisibility(View.VISIBLE);
                    transBt.setVisibility(View.VISIBLE);
                    switchFlag = true;
                } else {
                    /*이미지와 크기 변경*/
                    int weight = (int) convertDpToPixel(200, getApplicationContext());
                    centerBt.setLayoutParams(new ViewGroup.LayoutParams(weight, weight));
                    centerBt.setBackgroundResource(R.drawable.circle_off);
                    /*광고창 숨기기*/

                    /*버튼 숨기기*/
                    sellBt.setVisibility(View.GONE);
                    buyBt.setVisibility(View.GONE);
                    freeBt.setVisibility(View.GONE);
                    sellBt.setVisibility(View.GONE);
                    unknownBt.setVisibility(View.GONE);
                    transBt.setVisibility(View.GONE);
                    switchFlag = false;
                }
            }
        });
    }

    public void OnClick(View view) {
        ImageButton button = (ImageButton) view;
        Intent intent = new Intent(FirstActivity.this, MainActivity.class);
        switch (view.getId()) {
            case R.id.sellBt:
                menuSharedPreferences.storeMenu("sell");
                Toast.makeText(getApplicationContext(), menuSharedPreferences.getMenu(), Toast.LENGTH_LONG).show();
                startActivity(intent);
                break;
            case R.id.buyBt:
                menuSharedPreferences.storeMenu("buy");
                Toast.makeText(getApplicationContext(), menuSharedPreferences.getMenu(), Toast.LENGTH_LONG).show();
                startActivity(intent);
                break;
            case R.id.freeBt:
                menuSharedPreferences.storeMenu("free");
                Toast.makeText(getApplicationContext(), menuSharedPreferences.getMenu(), Toast.LENGTH_LONG).show();
                startActivity(intent);
                break;
            case R.id.unknownBt:
                menuSharedPreferences.storeMenu("unknown");
                Toast.makeText(getApplicationContext(), menuSharedPreferences.getMenu(), Toast.LENGTH_LONG).show();
                startActivity(intent);
                break;
            case R.id.transBt:
                menuSharedPreferences.storeMenu("trans");
                Toast.makeText(getApplicationContext(), menuSharedPreferences.getMenu(), Toast.LENGTH_LONG).show();
                startActivity(intent);
                break;
        }
    }

    /*dip를 px로 변환*/
    public float convertDpToPixel(float dp, Context context) {
        Resources resources = context.getResources();
        DisplayMetrics metrics = resources.getDisplayMetrics();
        float px = dp * (metrics.densityDpi / 160f);
        return px;
    }

    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);
        if (hasFocus) {
            // 어플에 포커스가 갈때 시작된다
            frameAnimation.start();
        } else {
            // 어플에 포커스를 떠나면 종료한다
            frameAnimation.stop();
        }
    }
}
