package com.example.class_room;

import androidx.appcompat.app.AppCompatActivity;
import android.content.Intent; //Intent클래스 추가 : 다른 액티비티로 이동
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import java.io.IOException;
import android.util.Log;
import java.io.StringReader;
import com.opencsv.CSVReader;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.opencsv.exceptions.CsvValidationException;

public class LoginActivity extends AppCompatActivity {
    private EditText mEditText_id; //아이디 입력 창
    private Button btn_Login;  // 로그인 버튼
    private String professorInfo; // 교수 이름

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        mEditText_id = findViewById(R.id.editText_id);
        btn_Login = findViewById(R.id.button_login);
        professorInfo = ""; // 교수 정보 문자열
        btn_Login.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // 입력한 아이디 가져오기
                final String userID = mEditText_id.getText().toString().trim();

                String url = "https://raw.githubusercontent.com/ssbinnn/server_class_room/main/Class.csv"; // 서버의 CSV 파일 URL

                RequestQueue queue = Volley.newRequestQueue(LoginActivity.this);
                StringRequest stringRequest = new StringRequest(Request.Method.GET, url,
                        new Response.Listener<String>() {
                            @Override
                            public void onResponse(String response) {
                                if (authenticate(userID, response) != null) {
                                    // 인증 성공 시 MainActivity로 전환
                                    Intent intent = new Intent(LoginActivity.this, MainActivity.class);
                                    startActivity(intent);
                                    finish();
                                } else {
                                    // 인증 실패 처리
                                    Toast.makeText(LoginActivity.this, "인증 실패", Toast.LENGTH_SHORT).show();
                                }
                            }
                        }, new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Toast.makeText(LoginActivity.this, "네트워크 오류: " + error.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });

                // 요청을 큐에 추가
                queue.add(stringRequest);
            }
        });
    }

    private String authenticate(String userID, String csvData) {
        String authenticated = null;
        try {
            CSVReader reader = new CSVReader(new StringReader(csvData));
            String[] nextLine;
            while ((nextLine = reader.readNext()) != null) {
                String professorID = nextLine[0].trim(); // CSV 파일에서 교수 아이디
                if (professorID.equals(userID)) {
                    authenticated = nextLine[1]; // 아이디가 일치하면 인증 성공
                    break;
                }
            }
            reader.close();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (CsvValidationException e) {
            throw new RuntimeException(e);
        }

        // 결과를 Logcat에 출력
        Log.d("AuthenticationResult", "UserID: " + userID + ", Authenticated: " + authenticated);

        return authenticated;
    }

//    private boolean authenticate(String userid, String csvID) {
//        // CSV 파일에서 교수아이디와 비교
//       userid="1234";
//        try {
//            // CSV 파일 한 줄씩 읽어오면서 아이디 비교
//            CSVReader reader = new CSVReader(new StringReader(csvID));
//            String[] nextLine;
//            while ((nextLine = reader.readNext()) != null) {
//                String professorID = nextLine[0].trim(); // CSV 파일에서 교수 아이디
//                if (professorID.equals(userid)) {
//                    reader.close();
//                    return true; // 아이디가 일치하면 인증 성공
//                }
//            }
//            reader.close();
//        } catch (IOException e) {
//            e.printStackTrace();
//        } catch (CsvValidationException e) {
//            throw new RuntimeException(e);
//        }
//        return false; // 아이디가 일치하지 않으면 인증 실패
//    }
}
