package org.loveornot.board;


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

public class InsertActivity extends AppCompatActivity {

    private final String urlStr = "http://skypotato.esy.es/";
    private Spinner spinner;
    private EditText titleEdit;
    private EditText passEdit;
    private EditText contentEdit;
    private Button insertBt;

    private String id;
    private String name;
    private String type = "free";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_insert);

        titleEdit = (EditText) findViewById(R.id.titleEdit);
        passEdit = (EditText) findViewById(R.id.passEdit);
        contentEdit = (EditText) findViewById(R.id.contentEdit);

        insertBt = (Button) findViewById(R.id.insertBt);
        insertBt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                requestVolley("insert.php");
                finish();
            }
        });

        /* 뒤로가기버튼 추가*/
        ActionBar actionBar = getSupportActionBar();

        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setHomeButtonEnabled(true);

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
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.insert_main, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home: //뒤로가기 액션
                finish();
                return true;
            case R.id.action_finish: // 완료 액션
                requestVolley("insert.php");
                finish();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    /*통신*/
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
                            Toast.makeText(getApplicationContext(), response, Toast.LENGTH_SHORT).show();
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
                params.put("id", id);
                params.put("pass", passEdit.getText().toString());
                params.put("title", titleEdit.getText().toString());
                params.put("content", contentEdit.getText().toString());
                params.put("type", type);
                if("unknown".equals(type)){
                    params.put("name", "unknown");
                }else {
                    params.put("name", name);
                }
                return params;
            }
        };

        RequestQueue queue = Volley.newRequestQueue(this);
        request.setShouldCache(false);
        queue.add(request);
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
