package org.loveornot.board;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.format.DateUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.facebook.FacebookSdk;
import com.facebook.appevents.AppEventsLogger;
import com.google.gson.Gson;
import com.google.gson.JsonParseException;
import com.google.gson.reflect.TypeToken;
import com.handmark.pulltorefresh.library.PullToRefreshBase;
import com.handmark.pulltorefresh.library.PullToRefreshListView;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {

    /* 끌어서 새로고침 뷰와 어댑터 */
    private PullToRefreshListView mPullToRefreshListView;
    private TextListAdapter mAdapter;

    /* 서버 URL */
    private final String urlStr = "http://skypotato.esy.es/board/";

    /* 총 페이지 및 현재 페이지 */
    private int page = 0;
    private int pageTotal = 0;

    /* 검색창 */
    private ImageButton cancleBt;
    private ImageButton searchBt;
    private EditText searchEdit;

    /* 게시판 타이틀 이미지 */
    private ImageView imageView;

    /* 통신 중복을 막기위한 Flag */
    private boolean requestWaitFlag = false;

    /* 메뉴 저장 */
    private MenuSharedPreferences menuSharedPreferences;

    /* 프로그래스바 */
    private ProgressDialog mProgressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        /*Facebook*/
        FacebookSdk.sdkInitialize(getApplicationContext());
        AppEventsLogger.activateApp(this);

        /*menuInfo설정*/
        menuSharedPreferences = new MenuSharedPreferences(this);

        /* 구성요소 연결 */
        imageView = (ImageView) findViewById(R.id.imageTitle);
        searchEdit = (EditText) findViewById(R.id.search_edit);
        cancleBt = (ImageButton) findViewById(R.id.cancleBt);
        searchBt = (ImageButton) findViewById(R.id.searchBt);


        /*새로고침뷰 설정*/
        mAdapter = new TextListAdapter(this);
        mPullToRefreshListView = (PullToRefreshListView) findViewById(R.id.pull_to_refresh_list);

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

        /* 리스트 뷰 어댑터 등록 */
        ListView actualListView = mPullToRefreshListView.getRefreshableView();
        actualListView.setAdapter(mAdapter);

        actualListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

                Intent intent = new Intent(MainActivity.this, BoardActivity.class);
                Board board = (Board) mAdapter.getItem(position - 1);// onItemClick의 position은 1부터 시작.
                intent.putExtra("no", board.getNo());
                startActivity(intent);

            }
        });
        cancleBt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                searchEdit.setText(null);
            }
        });
        searchBt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (searchEdit.getText().toString() != null) {
                    try {
                        menuSharedPreferences.storeSearch(searchEdit.getText().toString());
                        refreshData();
                    } catch (NullPointerException e) {
                        e.printStackTrace();
                    }
                }
            }
        });
        setTitleImage();

        /* 네비게이션 바 */
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        toolbar.inflateMenu(R.menu.main);
        setSupportActionBar(toolbar);

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.setDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);
    }

    @Override
    protected void onResume() {
        refreshData();
        super.onResume();
    }

    private void refreshData() {
        page = 0;
        mProgressDialog = ProgressDialog.show(MainActivity.this, "",
                "잠시만 기다려 주세요.", true);
        requestVolley("pageTotal.php");
        requestVolley("select.php");
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        switch (id) {
            case R.id.action_search: // 검색창 숨김 여부
                LinearLayout layout = (LinearLayout) findViewById(R.id.searchHolder);
                if (layout.getVisibility() == View.GONE) {
                    layout.setVisibility(View.VISIBLE);
                } else {
                    layout.setVisibility(View.GONE);
                }
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    /* 타이틀 이미지 선택 함수 */
    private void setTitleImage() {
        switch (menuSharedPreferences.getMenu()) {
            case "free":
                imageView.setImageResource(R.drawable.t_free);
                break;
            case "sell":
                imageView.setImageResource(R.drawable.t_sell);
                break;
            case "buy":
                imageView.setImageResource(R.drawable.t_buy);
                break;
            case "trans":
                imageView.setImageResource(R.drawable.t_trans);
                break;
            case "unknown":
                imageView.setImageResource(R.drawable.t_unknown);
                break;
        }
    }

    /* 네비게이션 메뉴 선택 이벤트 */
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {

        Intent intent;
        int id = item.getItemId();

        switch (id) {
            case R.id.nav_sell:
                menuSharedPreferences.storeMenu("sell");
                refreshData();
                setTitleImage();
                break;
            case R.id.nav_buy:
                menuSharedPreferences.storeMenu("buy");
                refreshData();
                setTitleImage();
                break;
            case R.id.nav_anonymous:
                menuSharedPreferences.storeMenu("unknown");
                refreshData();
                setTitleImage();
                break;
            case R.id.nav_free:
                menuSharedPreferences.storeMenu("free");
                refreshData();
                setTitleImage();
                break;
            case R.id.nav_translation:
                menuSharedPreferences.storeMenu("trans");
                refreshData();
                setTitleImage();
                break;
            case R.id.nav_insert:
                intent = new Intent(MainActivity.this, InsertActivity.class);
                startActivity(intent);
                break;
            case R.id.nav_login:
                intent = new Intent(MainActivity.this, LoginActivity.class);
                startActivity(intent);
                break;
        }
        menuSharedPreferences.storeSearch(""); // 검색조건 초기화

        /* 네비게이션 드로워 설정 */
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    /* 통신 */
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
                        if (response != null) {
                            if (strmenu.equals("select.php")) {
                                processResponse(response); // select할 경우 결과 가공
                                /* 프로그래스바 중지 */
                                if (mProgressDialog != null && mProgressDialog.isShowing()) {
                                    mProgressDialog.dismiss();
                                }
                            } else {
                                processPageTotal(response); // pageTotal할 경우 결과 가공
                            }
                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        error.printStackTrace();
                        /* 통신 실패 시 프로그래스바와 통신 Flag 중지 */
                        requestWaitFlag = false;
                        if (mProgressDialog != null && mProgressDialog.isShowing()) {
                            mProgressDialog.dismiss();
                        }
                    }
                }
        ) {
            @Override
            protected Map<String, String> getParams() throws AuthFailureError {
                Map<String, String> params = new HashMap<String, String>();
                String currentPage = Integer.toString(page);
                String type = menuSharedPreferences.getMenu();
                String search = menuSharedPreferences.getSearch();

                if (currentPage != null)
                    params.put("page", currentPage);
                if (type != null)
                    params.put("type", type);
                if (search != null)
                    params.put("search", search);
                return params;
            }
        };

        RequestQueue queue = Volley.newRequestQueue(this);
        request.setShouldCache(false);
        queue.add(request); // 통신시작
        requestWaitFlag = true; // 통신 중복 Flag On
    }


    /* pageTotal 가공 */
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
        }.getType(); // 타입 지정
        ArrayList<Board> board = null;
        Gson gson = new Gson();

        /* json형에서 ArrayList<Board>객체로 변환 */
        try {
            board = gson.fromJson(response, arraylistType);
        } catch (JsonParseException e) {
            e.printStackTrace();
        }

        /* 페이지가 0일 경우 리스트 새로고침 외 리스트 추가 */
        if (page == 0) {
            mAdapter.addList(board);
        } else if (board != null) {
            mAdapter.addAll(board);
        }

        mAdapter.notifyDataSetChanged();
        mPullToRefreshListView.onRefreshComplete();
    }

}
