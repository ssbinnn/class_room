package com.example.class_room;

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

public class MainActivity extends AppCompatActivity {
    private Handler mHandler;
    private TextView mTextView_number; // 현재 강의실 인원
    private TextView mTextView_professor; // 교수님 명
    private TextView mTextView_class; // 과목 명
    private TextView mTextView_classNum; // 수강 인원
    private CommunicationRunnable comRunnable;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mHandler = new Handler(Looper.getMainLooper());
        mTextView_number = (TextView)findViewById(R.id.textView_number);
        mTextView_class = (TextView)findViewById(R.id.textView_class);
        mTextView_classNum = (TextView)findViewById(R.id.textView_classNum);
        mTextView_professor = (TextView)findViewById(R.id.textView_professor);

        try {
            comRunnable = new CommunicationRunnable(mHandler);
            new Thread(comRunnable).start(); // 스레드
        } catch (IOException e) {
            e.printStackTrace();
        }

        Button refreshButton = (Button) findViewById(R.id.button_refresh);
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
        private Socket socket;
        private BufferedReader in;
        private PrintWriter out;
        private Handler mHandler;

        public CommunicationRunnable(Handler handler) throws IOException {

            this.mHandler = handler;
        }

        public void sendRefresh() {
            new Thread(new Runnable() {
                @Override
                public void run() {
                    out.println("1234");
                } //새로고침을 눌렀을때, 서버로 보낼 교수아이디
            }).start();
        }

        @Override
        public void run() {
            try {
                this.socket = new Socket("192.168.200.190", 5000); //연결시 IP 주소 확인 및 변경
                this.in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                this.out = new PrintWriter(socket.getOutputStream(), true);

                while (true) {
                    if (in.ready()) {  // 서버로부터 데이터가 온 경우
                        final String data = in.readLine();
                        Log.d("ArrayValue", data);

                        // 교수 아이디,교수 이름,강의 번호,강의명,강의 요일, 강의 시작시간, 강의 종료시간,수강인원,현재인원
                        final String[] getData = data.split(",");  // 쉼표를 기준으로 데이터 분리

                        for (int i = 0; i < getData.length; i++) {
                            Log.d("ArrayValue", "getData[" + i + "]: " + getData[i]);
                        }

                        mHandler.post(new Runnable() {
                            @Override
                            public void run() {
                                mTextView_professor.setText(getData[1] + " 교수님");
                                mTextView_class.setText(getData[3]);
                                mTextView_classNum.setText(getData[7] + "명 중");
                                mTextView_number.setText(getData[8] + "명");
                            }
                        });
                    }
                }

            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }


}
