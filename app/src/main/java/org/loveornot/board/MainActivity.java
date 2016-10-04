package org.loveornot.board;

import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

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

public class MainActivity extends AppCompatActivity {
    EditText editText;
    TextView textView;

    Handler handler = new Handler();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        editText = (EditText) findViewById(R.id.editText);
        textView = (TextView) findViewById(R.id.textView);

        Button button = (Button) findViewById(R.id.button);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                requestVolley();
            }
        });
    }

    public void requestVolley() {
        String urlStr = editText.getText().toString();

        StringRequest request = new StringRequest(
                Request.Method.POST,
                urlStr,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        print("수신 데이터 : " + response);
                        processResponse(response);
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
                //params.put("mobile", "010-1000-1000");
                return params;
            }
        };

        RequestQueue queue = Volley.newRequestQueue(this);
        request.setShouldCache(false);
        queue.add(request);
        print("웹서버에 요청함.");

    }
    private void processResponse(String response) {
        Gson gson = new Gson();

        Type arraylistType = new TypeToken<ArrayList<Board>>(){}.getType();
        ArrayList<Board> board = gson.fromJson(response, arraylistType);
        print("매출 데이터 갯수 : " + board.size());
        for (int i = 0; i < board.size(); i++) {
            Board board_ = board.get(i);
            print(board_.no + ", " + board_.title+ ", " + board_.hit+ ", " + board_.content+ ", " + board_.date+ ", " + board_.id+ ", " + board_.password);
        }
    }

    public void print(final String data) {
        handler.post(new Runnable() {
            public void run() {
                textView.append(data + "\n");
            }
        });
    }

}
