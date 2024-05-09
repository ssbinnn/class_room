package com.example.class_room;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.text.SimpleDateFormat;
import java.util.Date;

public class MainActivity extends AppCompatActivity {
    private Handler mHandler; // 핸들러
    private TextView mTextView_number; // 현재 강의실 인원
    private TextView mTextView_professor; // 교수님 이름
    private TextView mTextView_class; // 과목 명
    private TextView mTextView_classNum; // 수강 인원
    private TextView mTextView_updateTime; //인원수 업데이트 시간
    private CommunicationRunnable comRunnable;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Intent intent = getIntent();
        String userID = intent.getStringExtra("userID"); // intent에서 userID 받기
        String userName = intent.getStringExtra("userName"); // intent에서 userName 받기

        mHandler = new Handler(Looper.getMainLooper());
        mTextView_number = (TextView)findViewById(R.id.textView_number);
        mTextView_class = (TextView)findViewById(R.id.textView_class);
        mTextView_classNum = (TextView)findViewById(R.id.textView_classNum);
        mTextView_professor = (TextView)findViewById(R.id.textView_professor);

        mTextView_professor.setText(userName + " 교수님"); // 교수님 이름 세팅

        mTextView_updateTime = (TextView)findViewById(R.id.textView_updateTime);


        try {
            comRunnable = new CommunicationRunnable(mHandler, userID, userName);
            new Thread(comRunnable).start(); // 스레드 시작
        } catch (IOException e) { // 예외 처리
            e.printStackTrace();
        }

        Button refreshButton = (Button) findViewById(R.id.button_refresh); // 새로 고침 버튼
        refreshButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) { // 버튼 클릭 시
                if (comRunnable != null) {
                    comRunnable.sendRefresh();
                } else {
                    // comRunnable 객체가 Null인 경우에 대한 처리
                    Toast.makeText(MainActivity.this, "서버와의 연결이 준비되지 않았습니다.", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    class CommunicationRunnable implements Runnable {
        private final String userID;
        private final String userName;
        private Socket socket;
        private BufferedReader in;
        private PrintWriter out;
        private Handler mHandler;

        public CommunicationRunnable(Handler handler, String userID, String userName) throws IOException {

            this.mHandler = handler;
            this.userID = userID; // 교수님 id
            this.userName = userName; // 교수님 이름
        }

        public void sendRefresh() {
            new Thread(new Runnable() {
                @Override
                public void run() {
                    SimpleDateFormat dayFormat = new SimpleDateFormat("E"); // 요일
                    String currentDay = dayFormat.format(new Date());
                    SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm"); // 시간
                    String currentTime = timeFormat.format(new Date());

                    String data = currentDay + " " + currentTime; // 문자열로 묶기

                    out.println(data); // 전송
                } //새로고침 버튼을 눌렀을때 서버로 보낼 현재 시간
            }).start();
        }

        @Override
        public void run() { // 서버와 연결 (스레드로 동작)
            try {
                this.socket = new Socket("165.229.125.92", 5000); // 연결시 IP 주소 확인 및 변경
                this.in = new BufferedReader(new InputStreamReader(socket.getInputStream())); // 입력 스트림 초기화
                this.out = new PrintWriter(socket.getOutputStream(), true); // 출력 스트림 초기화

                SimpleDateFormat dayFormat = new SimpleDateFormat("E"); // 요일
                String currentDay = dayFormat.format(new Date()); // 현재 요일
                SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm"); // 시간
                String currentTime = timeFormat.format(new Date()); // 현재 시간

                String connect_data = userID + "," + currentDay + " " + currentTime; // 문자열로 묶기
                out.println(connect_data); // 서버에 아이디, 접속 시간 전송
                // 서버 연결 직후 아이디와 접속 시간을 전송 (최초 한 번)
                // 이후에는 버튼 누를 때마다 현재 시간 전송

                while (true) {
                    if (in.ready()) {  // 서버로부터 데이터가 온 경우
                        long arrivalTime = System.currentTimeMillis(); //데이터 도착 시간 기록
                        final String data = in.readLine();
                        Log.d("ArrayValue", data); // 로그

                        // 교수 아이디,교수 이름,강의 번호,강의명,강의 요일, 강의 시작시간, 강의 종료시간,수강인원,현재인원
                        final String[] getData = data.split(",");  // 쉼표를 기준으로 데이터 분리

                        for (int i = 0; i < getData.length; i++) { // 로그
                            Log.d("ArrayValue", "getData[" + i + "]: " + getData[i]);
                        }

                        Date date = new Date(arrivalTime); // 도착 시간을 Date타입으로 변환
                        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss"); //시간 형식 지정
                        String formatDate = dateFormat.format(date); //형식에 맞게 날짜와 시간을 문자열로 변환
                        // System.out.println("도착 시간: " + formatDate);
                        mHandler.post(new Runnable() { // UI 업데이트
                            @Override
                            public void run() {
                                //mTextView_professor.setText(getData[1] + " 교수님");
                                mTextView_class.setText(getData[3]); // 수업명
                                mTextView_classNum.setText(getData[7] + "명 중"); // 수강 인원
                                mTextView_number.setText(getData[8] + "명"); // 현 인원

                                mTextView_updateTime.setText("최근 업데이트 : \n"+formatDate); //업데이트 시간
                            }
                        });
                    }
                }

            } catch (IOException e) { // 예외 처리
                e.printStackTrace();
            }
        }
    }


}
