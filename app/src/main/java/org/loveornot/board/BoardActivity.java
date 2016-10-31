package org.loveornot.board;

import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.facebook.AccessToken;
import com.facebook.GraphRequest;
import com.facebook.GraphResponse;
import com.google.gson.Gson;
import com.google.gson.JsonParseException;
import com.google.gson.reflect.TypeToken;

import org.json.JSONObject;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class
BoardActivity extends AppCompatActivity {

    /* 서버 URL */
    private final String urlStr = "http://skypotato.esy.es/";

    private String no;

    /* 기본 화면 구성 */
    private TextView titleTxt;
    private TextView nameTxt;
    private TextView dateTxt;
    private TextView hitTxt;
    private TextView contentTxt;

    /* 댓글 화면 구성 */
    private LinearLayout responseHolder;
    private TextView responseNum;
    private TextView responseData01;
    private TextView responseData02;
    private TextView responseData03;

    /* 댓글 더보기 버튼 */
    private Button responseMoreBt;

    /* 프로그래스바 */
    private ProgressDialog mProgressDialog;

    /* 결과 가공 */
    private Type arraylistType = new TypeToken<ArrayList<Board>>() {
    }.getType();
    private ArrayList<Board> board = null;
    private Gson gson = new Gson();

    private String type;
    private String userID;
    private String id;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_board);

        Intent getIntent = getIntent();
        if (getIntent != null) {
            no = getIntent.getStringExtra("no");
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

        responseHolder = (LinearLayout) findViewById(R.id.responseHolder);
        responseNum = (TextView) findViewById(R.id.responseNum);
        responseData01 = (TextView) findViewById(R.id.responseData01);
        responseData02 = (TextView) findViewById(R.id.responseData02);
        responseData03 = (TextView) findViewById(R.id.responseData03);

        responseMoreBt = (Button) findViewById(R.id.responseMoreBt);
        responseMoreBt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(BoardActivity.this, ResponseBoardActivity.class);
                intent.putExtra("boardNo", no);
                startActivity(intent);
            }
        });
    }


    private void startBoardData() {
        mProgressDialog = ProgressDialog.show(BoardActivity.this, "",
                "잠시만 기다려 주세요.", true);
        requestVolley("/response/totalNum.php");
        requestVolley("/response/selectOne.php");
        requestVolley("/board/updateHit.php");
        requestVolley("/board/selectOne.php");
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.main_board, menu);
        return super.onCreateOptionsMenu(menu);
    }

    /* 액션바 메뉴 이벤트 */
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home: //뒤로가기 액션
                finish();
                return true;
            case R.id.action_modify:
                if (AccessToken.getCurrentAccessToken() != null) {
                    if (userID.equals(id)) {
                        Intent intent = new Intent(BoardActivity.this, ModifyActivity.class);
                        intent.putExtra("no", no);
                        intent.putExtra("type", type);
                        intent.putExtra("title", titleTxt.getText().toString());
                        intent.putExtra("content", contentTxt.getText().toString());
                        startActivity(intent);
                    } else {
                        Toast.makeText(getApplicationContext(), "작성자가 아닙니다.", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Toast.makeText(getApplicationContext(), "로그인이 필요합니다.", Toast.LENGTH_SHORT).show();
                }
                return true;
            case R.id.action_delete:
                if (AccessToken.getCurrentAccessToken() != null) {
                    if (userID.equals(id)) {
                        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);
                        alertDialogBuilder.setTitle("게시물 삭제");
                        alertDialogBuilder.setMessage("게시물을 삭제하시겠습니까?")
                                .setCancelable(false)
                                .setPositiveButton("취소", new DialogInterface.OnClickListener() {
                                    public void onClick(
                                            DialogInterface dialog, int id) {
                                        dialog.cancel();
                                    }
                                })
                                .setNegativeButton("삭제", new DialogInterface.OnClickListener() {
                                    public void onClick(
                                            DialogInterface dialog, int id) {
                                        mProgressDialog = ProgressDialog.show(BoardActivity.this, "",
                                                "잠시만 기다려 주세요.", true);
                                        requestVolley("/board/delete.php");
                                        requestVolley("/board/deleteResponse.php");
                                    }
                                });
                        AlertDialog alertDialog = alertDialogBuilder.create();
                        alertDialog.show();
                    } else {
                        Toast.makeText(getApplicationContext(), "작성자가 아닙니다.", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Toast.makeText(getApplicationContext(), "로그인이 필요합니다.", Toast.LENGTH_SHORT).show();
                }
                return true;
        }
        return super.onOptionsItemSelected(item);
    }


    @Override
    protected void onResume() {
        if (AccessToken.getCurrentAccessToken() != null) {
            GraphRequest request = GraphRequest.newMeRequest(AccessToken.getCurrentAccessToken(), new GraphRequest.GraphJSONObjectCallback() {
                @Override
                public void onCompleted(JSONObject object, GraphResponse response) {
                    Log.d("TAG", "페이스북 로그인 결과" + response.toString());

                    try {
                        id = object.getString("id"); // id
                        Log.d("TAG", "페이스북 id->" + id);

                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            });
            request.executeAsync();
        }
        startBoardData(); // 시작 시 데이터 불러오기
        super.onResume();
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
                            if(strmenu.equals("/board/selectOne.php")) {
                                processResponse(response);
                            } else if (strmenu.equals("/response/totalNum.php")) {
                                processTotalNum(response);
                            } else if (strmenu.equals("/response/selectOne.php")) {
                                processResponseData(response);
                                if (mProgressDialog != null && mProgressDialog.isShowing()) {
                                    mProgressDialog.dismiss();
                                }
                            } else if (strmenu.equals("/board/delete.php")) {
                                Toast.makeText(getApplicationContext(), response, Toast.LENGTH_SHORT).show();
                                if (mProgressDialog != null && mProgressDialog.isShowing()) {
                                    mProgressDialog.dismiss();
                                }
                                finish();
                            }
                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        error.printStackTrace();
                        if (mProgressDialog != null && mProgressDialog.isShowing()) {
                            mProgressDialog.dismiss();
                        }
                    }
                }
        ) {
            @Override
            protected Map<String, String> getParams() throws AuthFailureError {
                Map<String, String> params = new HashMap<String, String>();
                if (no != null) {
                    params.put("no", no);
                    params.put("boardNo", no);
                }
                return params;
            }
        };

        RequestQueue queue = Volley.newRequestQueue(this);
        request.setShouldCache(false);
        queue.add(request); // 통신시작
    }

    public void processTotalNum(String response) {
        if (response != null) {
            try {
                responseNum.setText("댓글 : " + response);
            } catch (NumberFormatException e) {
                e.printStackTrace();
            }
        }
    }

    private void processResponseData(String response) {

        try {
            board = gson.fromJson(response, arraylistType);
        } catch (JsonParseException e) {
            e.printStackTrace();
        }

        try {
            String name = board.get(0).getName();
            String content = board.get(0).getContent();
            String date = board.get(0).getDate();

            responseHolder.setVisibility(View.VISIBLE);
            responseData01.setText(name);
            responseData02.setText(content);
            responseData03.setText(date);
        } catch (NullPointerException e) {
            e.printStackTrace();
        }

    }

    private void processResponse(String response) {

        try {
            board = gson.fromJson(response, arraylistType);
        } catch (JsonParseException e) {
            e.printStackTrace();
        }


        try {
            String title = board.get(0).getTitle();
            String name = board.get(0).getName();
            String date = board.get(0).getDate();
            String hit = board.get(0).getHit();
            String content = board.get(0).getContent();

            userID = board.get(0).getId();
            type = board.get(0).getType();

            titleTxt.setText(title);
            nameTxt.setText("작성자 : " + name);
            dateTxt.setText(date);
            hitTxt.setText("조회수 : " + hit);
            contentTxt.setText(content);
        } catch (NullPointerException e) {
            e.printStackTrace();
        }


    }

}
