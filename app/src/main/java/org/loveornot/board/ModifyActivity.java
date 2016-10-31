package org.loveornot.board;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
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

import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

public class ModifyActivity extends AppCompatActivity {

    /* 서버 URL */
    private final String urlStr = "http://skypotato.esy.es/board/";

    private Spinner spinner;

    /* 화면 구성 */
    private EditText titleEdit;
    private EditText contentEdit;
    private Button insertBt;

    /* 입력 매개변수 */
    private String no;
    private String id;
    private String name;
    private String type;
    private String title;
    private String content;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_modify);

        Intent getIntent = getIntent();
        if (getIntent != null) {
            no = getIntent.getStringExtra("no");
            title = getIntent.getStringExtra("title");
            type = getIntent.getStringExtra("type");
            content = getIntent.getStringExtra("content");
        } else {
            Toast.makeText(getApplicationContext(), "no가 없습니다.", Toast.LENGTH_SHORT).show();
            finish();
        }

        insertBt = (Button) findViewById(R.id.insertBt);
        titleEdit = (EditText) findViewById(R.id.titleEdit);
        contentEdit = (EditText) findViewById(R.id.contentEdit);

        titleEdit.setText(title);
        contentEdit.setText(content);


         /* 뒤로가기버튼 추가*/
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setHomeButtonEnabled(true);
        }

        /*게시판 리스트*/
        spinner = (Spinner) findViewById(R.id.typeSpinner);
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this, R.array.type_array, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(adapter);
        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                switch (parent.getItemAtPosition(position).toString()) {
                    case "자유게시판":
                        type = "free";
                        break;
                    case "삽니다.":
                        type = "buy";
                        break;
                    case "팝니다.":
                        type = "sell";
                        break;
                    case "익명게시판":
                        type = "unknown";
                        break;
                    case "번역게시판":
                        type = "trans";
                        break;
                }

            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                type = "free";
            }
        });
        spinnerSelection(type);

        insertBt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                requestVolley("update.php");
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.main_insert, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home: //뒤로가기 액션
                finish();
                return true;
            case R.id.action_finish: // 완료 액션
                requestVolley("update.php");
                finish();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    /*통신*/
    public void requestVolley(String str) {
        final String strmenu = str;
        String url = urlStr + strmenu;
        StringRequest request = new StringRequest(
                Request.Method.POST,
                url,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        if (response != null) {
                            if (strmenu.equals("update.php")) {
                                Toast.makeText(getApplicationContext(), response, Toast.LENGTH_SHORT).show();
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
                Map<String, String> params = new HashMap<>();
                params.put("no", no);
                params.put("title", titleEdit.getText().toString());
                params.put("content", contentEdit.getText().toString());
                params.put("type", type);
                if ("unknown".equals(type)) {
                    params.put("name", "unknown");
                } else {
                    params.put("name", name);
                }
                return params;
            }
        };

        RequestQueue queue = Volley.newRequestQueue(this);
        request.setShouldCache(false);
        queue.add(request);
    }


    private void spinnerSelection(String type) {
        switch (type) {
            case "free":
                spinner.setSelection(0);
                break;
            case "buy":
                spinner.setSelection(1);
                break;
            case "sell":
                spinner.setSelection(2);
                break;
            case "unknown":
                spinner.setSelection(3);
                break;
            case "trans":
                spinner.setSelection(4);
                break;
        }
    }

    @Override
    protected void onResume() {
        if (AccessToken.getCurrentAccessToken() == null) {
            Toast.makeText(getApplicationContext(), "로그인이 필요한 기능입니다.", Toast.LENGTH_SHORT).show();
            finish();
        } else {
            GraphRequest request = GraphRequest.newMeRequest(AccessToken.getCurrentAccessToken(), new GraphRequest.GraphJSONObjectCallback() {
                @Override
                public void onCompleted(JSONObject object, GraphResponse response) {
                    Log.d("TAG", "페이스북 로그인 결과" + response.toString());

                    try {
                        id = object.getString("id"); // id
                        name = object.getString("name"); // 이름

                        Log.d("TAG", "페이스북 id->" + id);
                        Log.d("TAG", "페이스북 이름->" + name);

                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            });
            request.executeAsync();
        }
        super.onResume();
    }
}
