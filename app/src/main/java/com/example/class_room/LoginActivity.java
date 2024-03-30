package com.example.class_room;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.opencsv.CSVReader;
import com.opencsv.exceptions.CsvValidationException;

import java.io.IOException;
import java.io.StringReader;

public class LoginActivity extends AppCompatActivity {
    private EditText mEditText_id; // 아이디 입력 창
    private Button btn_Login;  // 로그인 버튼
    private String userName; // 교수님 이름

    @Override
    protected void onCreate(Bundle savedInstanceState) { // 액티비티 생성 시 호출
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        mEditText_id = findViewById(R.id.editText_id);
        btn_Login = findViewById(R.id.button_login);
        //professorInfo = ""; // 교수 정보 문자열

        btn_Login.setOnClickListener(new View.OnClickListener() { // 로그인 버튼 클릭 시
            @Override
            public void onClick(View v) {
                // 입력한 아이디 가져오기
                final String userID = mEditText_id.getText().toString().trim();

                String url = "https://raw.githubusercontent.com/ssbinnn/server_class_room/main/Class.csv"; // 서버의 CSV 파일 URL

                // Volley 라이브러리 : 웹 통신에 필요한 라이브러리
                RequestQueue queue = Volley.newRequestQueue(LoginActivity.this); // Volley 요청 큐

                // 서버로부터 문자열 데이터를 얻기 위해 클래스 사용
                // 매개변수-(HTTP메소드, 서버url, 결과콜백, 에러콜백)
                StringRequest stringRequest = new StringRequest(Request.Method.GET, url,
                        new Response.Listener<String>() {
                            @Override
                            public void onResponse(String response) {
                                if (authenticate(userID, response)!=null) { // 인증 성공 시 MainActivity로 전환
                                    userName = authenticate(userID, response); // 교수님 이름

                                    Intent intent = new Intent(LoginActivity.this, MainActivity.class); // 현재 액티비티에서 다른 액티비티 호출
                                    intent.putExtra("userID", userID); // 다른 액티비티에 데이터 전달
                                    intent.putExtra("userName", userName);
                                    startActivity(intent);
                                    finish();
                                } else {
                                    // 인증 실패
                                    Toast.makeText(LoginActivity.this, "인증 실패", Toast.LENGTH_SHORT).show();
                                }
                            }
                        }, new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) { // 웹 연결 에러 시
                        Toast.makeText(LoginActivity.this, "네트워크 오류: " + error.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
                queue.add(stringRequest); // 요청을 큐에 추가
            }
        });
    }

    private String authenticate(String userID, String csvData) {
        String authenticated = null;
        try {
            CSVReader reader = new CSVReader(new StringReader(csvData)); // csv 파일을 string[] 형식으로 읽음
            String[] nextLine;

            while ((nextLine = reader.readNext()) != null) { // 파일 끝까지 반복
                String professorID = nextLine[0].trim(); // CSV 파일 교수 아이디, trim()으로 양끝 공백 제거
                if (professorID.equals(userID)) { // 아이디가 일치하면 인증 성공
                    authenticated = nextLine[1]; // 이름 저장
                    break;
                }
            }
            reader.close();
        } catch (IOException e) { // 파일 입출력 관련 예외 처리
            e.printStackTrace();
        } catch (CsvValidationException e) { // CSV 파일 피싱 관련 예외 처리
            throw new RuntimeException(e); // 프로그램 중단
        }

        // 결과를 Logcat에 출력
        Log.d("AuthenticationResult", "UserID: " + userID + ", Authenticated: " + authenticated);

        return authenticated; // 인증 성공 시 교수님 이름, 실패 시 null 반환
    }
}
