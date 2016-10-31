package org.loveornot.board;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.text.format.DateUtils;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
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
import com.handmark.pulltorefresh.library.PullToRefreshBase;
import com.handmark.pulltorefresh.library.PullToRefreshListView;

import org.json.JSONObject;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class ResponseBoardActivity extends AppCompatActivity {


    /* 서버 URL */
    private final String urlStr = "http://skypotato.esy.es/response/";

    /* 입력 변수 */
    private String boardNo;
    private String content;
    private String id;
    private String name;

    /* 댓글 입력 창 */
    private EditText contentEdit;
    private Button saveBt;

    /* 총 페이지 및 현재 페이지 */
    private int page = 0;
    private int pageTotal = 0;

    /* 통신 중복을 막기위한 Flag */
    private boolean requestWaitFlag = false;

    /* 끌어서 새로고침 뷰와 어댑터 */
    private PullToRefreshListView mPullToRefreshListView;
    private ResponseListAdapter mAdapter;

    /* 프로그래스바 */
    private ProgressDialog mProgressDialog;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_response_board);

        Intent getIntent = getIntent();
        if (getIntent != null) {
            boardNo = getIntent.getStringExtra("boardNo");
        } else {
            Toast.makeText(getApplicationContext(), "boardNo가 없습니다.", Toast.LENGTH_SHORT).show();
            finish();
        }

        /* 뒤로가기버튼 추가*/
        ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setHomeButtonEnabled(true);

        /* 저장 */
        contentEdit = (EditText) findViewById(R.id.contentEdit);
        saveBt = (Button) findViewById(R.id.saveBt);

        /*새로고침뷰 설정*/
        mPullToRefreshListView = (PullToRefreshListView) findViewById(R.id.pull_to_refresh_list_response);
        mPullToRefreshListView.setOnRefreshListener(new PullToRefreshBase.OnRefreshListener<ListView>() {
            @Override
            public void onRefresh(PullToRefreshBase<ListView> refreshView) {
                String label = DateUtils.formatDateTime(getApplicationContext(), System.currentTimeMillis(),
                        DateUtils.FORMAT_SHOW_TIME | DateUtils.FORMAT_SHOW_DATE | DateUtils.FORMAT_ABBREV_ALL);
                refreshView.getLoadingLayoutProxy().setLastUpdatedLabel(label);

                if (requestWaitFlag) {
                    Toast.makeText(getApplicationContext(), "통신 중 입니다.", Toast.LENGTH_SHORT).show();
                } else {
                    refreshData();
                }
            }
        });
        mPullToRefreshListView.setOnLastItemVisibleListener(new PullToRefreshBase.OnLastItemVisibleListener() {
            @Override
            public void onLastItemVisible() {

                if (requestWaitFlag) {
                    Toast.makeText(getApplicationContext(), "통신 중 입니다.", Toast.LENGTH_SHORT).show();
                } else {
                    page++;
                    if (page < pageTotal) {
                        Toast.makeText(getApplicationContext(), "다음 페이지를 불러옵니다.", Toast.LENGTH_SHORT).show(); // 마지막 list 작업
                        requestVolley("select.php");
                    } else {
                        Toast.makeText(getApplicationContext(), "더 이상 게시물이 없습니다.", Toast.LENGTH_SHORT).show(); // 마지막 list 작업
                    }
                }
            }
        });

        saveBt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (AccessToken.getCurrentAccessToken() == null) {
                    Toast.makeText(getApplicationContext(), "로그인이 필요한 기능입니다.", Toast.LENGTH_SHORT).show();
                    finish();
                } else if (contentEdit.getText() == null || contentEdit.getText().equals("")) {
                    Toast.makeText(getApplicationContext(), "댓글을 입력하세요.", Toast.LENGTH_SHORT).show();
                } else if (requestWaitFlag != true) {
                    content = contentEdit.getText().toString();
                    /* 토큰의 id와 이름 가져오기 */
                    GraphRequest request = GraphRequest.newMeRequest(AccessToken.getCurrentAccessToken(), new GraphRequest.GraphJSONObjectCallback() {
                        @Override
                        public void onCompleted(JSONObject object, GraphResponse response) {
                            Log.d("TAG", "페이스북 로그인 결과" + response.toString());

                            try {
                                id = object.getString("id"); // id
                                name = object.getString("name"); // 이름

                                Log.d("TAG", "페이스북 id->" + id);
                                Log.d("TAG", "페이스북 이름->" + name);
                                requestWaitFlag = true;
                                requestVolley("insert.php");
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        }
                    });
                    request.executeAsync();
                }
            }
        });

        ListView actualListView = mPullToRefreshListView.getRefreshableView();
        mAdapter = new ResponseListAdapter(this);
        actualListView.setAdapter(mAdapter);
        refreshData();
    }

    private void refreshData() {
        page = 0;
        mProgressDialog = ProgressDialog.show(ResponseBoardActivity.this, "",
                "잠시만 기다려 주세요.", true);
        requestVolley("pageTotal.php");
        requestVolley("select.php");
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
                        requestWaitFlag = false;
                        if (response != null && response != "") {
                            if ("select.php".equals(strmenu)) {
                                processResponse(response);
                                if (mProgressDialog != null && mProgressDialog.isShowing()) {
                                    mProgressDialog.dismiss();
                                }
                            } else if ("pageTotal.php".equals(strmenu)) {
                                processPageTotal(response);
                            } else {
                                contentEdit.setText("");
                                refreshData();
                                Toast.makeText(getApplicationContext(), "입력완료..!", Toast.LENGTH_SHORT).show();
                            }
                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        error.printStackTrace();
                        requestWaitFlag = false;
                        mPullToRefreshListView.onRefreshComplete();
                        if (mProgressDialog != null && mProgressDialog.isShowing()) {
                            mProgressDialog.dismiss();
                        }
                    }
                }
        ) {
            @Override
            protected Map<String, String> getParams() throws AuthFailureError {
                Map<String, String> params = new HashMap<String, String>();
                if (id != null)
                    params.put("id", id);
                if (content != null)
                    params.put("content", content);
                if (name != null)
                    params.put("name", name);
                params.put("boardNo", boardNo);
                params.put("page", Integer.toString(page));
                return params;
            }
        };

        RequestQueue queue = Volley.newRequestQueue(this);
        request.setShouldCache(false);
        queue.add(request);
    }

    public void processPageTotal(String response) {
        if (response != null) {
            try {
                pageTotal = Integer.parseInt(response);
            } catch (NumberFormatException e) {
                e.printStackTrace();
            }
        }
    }

    public void processResponse(String response) {
        Type arraylistType = new TypeToken<ArrayList<Board>>() {
        }.getType();
        ArrayList<Board> board = null;
        Gson gson = new Gson();

        try {
            board = gson.fromJson(response, arraylistType);
        } catch (JsonParseException e) {
            e.printStackTrace();
        }

        if (page == 0) {
            mAdapter.addList(board);
        } else if (board != null) {
            mAdapter.addAll(board);
        }

        mAdapter.notifyDataSetChanged();
        mPullToRefreshListView.onRefreshComplete();
    }


}
