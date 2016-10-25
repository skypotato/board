package org.loveornot.board;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.view.MenuItem;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class
        BoardActivity extends AppCompatActivity {
    private final String urlStr = "http://skypotato.esy.es/";

    private String no;

    private TextView titleTxt;
    private TextView nameTxt;
    private TextView dateTxt;
    private TextView hitTxt;
    private TextView contentTxt;

    private ProgressDialog mProgressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_board);

        Intent intent = getIntent();
        if (intent != null) {
            no = intent.getStringExtra("no");
        } else {
            Toast.makeText(getApplicationContext(), "no가 없습니다.", Toast.LENGTH_SHORT).show();
            finish();
        }

            /* 뒤로가기버튼 추가*/
        ActionBar actionBar = getSupportActionBar();

        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setHomeButtonEnabled(true);

        titleTxt = (TextView) findViewById(R.id.titleTxt);
        nameTxt = (TextView) findViewById(R.id.nameTxt);
        dateTxt = (TextView) findViewById(R.id.dateTxt);
        hitTxt = (TextView) findViewById(R.id.hitTxt);
        contentTxt = (TextView) findViewById(R.id.contentTxt);

        mProgressDialog = ProgressDialog.show(BoardActivity.this, "",
                "잠시만 기다려 주세요.", true);
        requestVolley("updateHit.php");
        requestVolley("selectOne.php");
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home: //뒤로가기 액션
                finish();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }
    public void requestVolley(String str) {
        final String strmenu = str;
        String url = urlStr + str;
        final StringRequest request = new StringRequest(
                Request.Method.POST,
                url,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        if (response != null) {
                            if(strmenu.equals("selectOne.php")) {
                                processResponse(response);
                            }else{
                                Toast.makeText(getApplicationContext(),response,Toast.LENGTH_SHORT).show();
                            }
                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        error.printStackTrace();
                        if (mProgressDialog!=null&&mProgressDialog.isShowing()){
                            mProgressDialog.dismiss();
                        }
                    }
                }
        ) {
            @Override
            protected Map<String, String> getParams() throws AuthFailureError {
                Map<String, String> params = new HashMap<String, String>();
                params.put("no", no);
                return params;
            }
        };

        RequestQueue queue = Volley.newRequestQueue(this);
        request.setShouldCache(false);
        queue.add(request); // 통신시작
    }

    public void processResponse(String response) {
        Gson gson = new Gson();

        Type arraylistType = new TypeToken<ArrayList<Board>>() {
        }.getType();
        ArrayList<Board> board = gson.fromJson(response, arraylistType);
        titleTxt.setText(board.get(0).getTitle());
        nameTxt.setText("작성자 : "+board.get(0).getName());
        dateTxt.setText(board.get(0).getDate());
        hitTxt.setText("조회수 : "+board.get(0).getHit());
        contentTxt.setText(board.get(0).getContent());

        if (mProgressDialog!=null&&mProgressDialog.isShowing()){
            mProgressDialog.dismiss();
        }

    }

}
