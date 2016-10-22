package org.loveornot.board;

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
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.handmark.pulltorefresh.library.PullToRefreshBase;
import com.handmark.pulltorefresh.library.PullToRefreshListView;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {

    private PullToRefreshListView mPullToRefreshListView;
    private ArrayList<Board> mDataSet = new ArrayList<Board>();
    private TextListAdapter mAdapter;

    private final String urlStr = "http://skypotato.esy.es/";
    private int page = 0;
    private int pageTotal = 0;

    private ImageButton cancleBt;
    private ImageButton searchBt;
    private EditText searchEdit;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        /*menuInfo설정*/
        /*검색 설정*/
        searchEdit = (EditText)findViewById(R.id.search_edit);
        cancleBt = (ImageButton)findViewById(R.id.cancleBt);
        searchBt = (ImageButton)findViewById(R.id.searchBt);

        cancleBt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                searchEdit.setText(null);
            }
        });

        searchBt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                /*검색조건*/
            }
        });

        /*새로고침뷰 설정*/
        mPullToRefreshListView = (PullToRefreshListView) findViewById(R.id.pull_to_refresh_list);
        mPullToRefreshListView.setOnRefreshListener(new PullToRefreshBase.OnRefreshListener<ListView>() {
            @Override
            public void onRefresh(PullToRefreshBase<ListView> refreshView) {
                String label = DateUtils.formatDateTime(getApplicationContext(), System.currentTimeMillis(),
                        DateUtils.FORMAT_SHOW_TIME | DateUtils.FORMAT_SHOW_DATE | DateUtils.FORMAT_ABBREV_ALL);
                refreshView.getLoadingLayoutProxy().setLastUpdatedLabel(label);
                page = 0;
                requestVolley("pageTotal.php");
                requestVolley("select.php");
            }
        });
        mPullToRefreshListView.setOnLastItemVisibleListener(new PullToRefreshBase.OnLastItemVisibleListener() {
            @Override
            public void onLastItemVisible() {
                page++;
                if (page < pageTotal) {
                    Toast.makeText(getApplicationContext(), "다음 페이지를 불러옵니다.", Toast.LENGTH_SHORT).show(); // 마지막 list 작업
                    requestVolley("select.php");
                } else {
                    Toast.makeText(getApplicationContext(), "더 이상 게시물이 없습니다.", Toast.LENGTH_SHORT).show(); // 마지막 list 작업
                }
            }
        });
        ListView actualListView = mPullToRefreshListView.getRefreshableView();
        mAdapter = new TextListAdapter(this);
        mAdapter.addList(mDataSet);
        actualListView.setAdapter(mAdapter);
        actualListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Board board = (Board) mAdapter.getItem(position - 1);//onItemClick의 position은 1부터 시작.
                Toast.makeText(getApplicationContext(), board.getNo(), Toast.LENGTH_SHORT).show();
            }
        });
        requestVolley("pageTotal.php");
        requestVolley("select.php");

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
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        switch (id) {
            case R.id.action_search:
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

    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        switch (id) {
            case R.id.nav_sell:
                break;
            case R.id.nav_buy:
                break;
            case R.id.nav_anonymous:
                break;
            case R.id.nav_free:
                break;
            case R.id.nav_translation:
                break;
            case R.id.nav_insert:
                Intent intent = new Intent(MainActivity.this,InsertActivity.class);
                startActivity(intent);
                break;
            case R.id.nav_search:
                break;
        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    public void requestVolley(String str) {
        final String strmenu = str;
        String url = urlStr + str;
        StringRequest request = new StringRequest(
                Request.Method.POST,
                url,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        if (response != null) {
                            if (strmenu.equals("select.php")) {
                                processResponse(response);
                            } else {
                                processPageTotal(response);
                            }
                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        error.printStackTrace();
                    }
                }
        ) {
            @Override
            protected Map<String, String> getParams() throws AuthFailureError {
                Map<String, String> params = new HashMap<String, String>();
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
            pageTotal = Integer.parseInt(response);
        }
    }

    public void processResponse(String response) {
        Gson gson = new Gson();

        Type arraylistType = new TypeToken<ArrayList<Board>>() {
        }.getType();
        ArrayList<Board> board = gson.fromJson(response, arraylistType);
        if (page == 0) {
            mAdapter.addList(board);
        } else if (board != null) {
            mAdapter.addAll(board);
        }
        mAdapter.notifyDataSetChanged();
        mPullToRefreshListView.onRefreshComplete();
    }

}
