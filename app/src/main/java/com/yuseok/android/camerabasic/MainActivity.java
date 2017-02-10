package com.yuseok.android.camerabasic;


import android.os.Build;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

public class MainActivity extends AppCompatActivity {

    // 권한 요청 코드
    private final int REQ_PERMISSION = 100;
    private final int REQ_CAMERA = 101;

    Button btnCamera;
    ImageView imageView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        setWidget();
        setListener();

        checkPermission();
    }

    private void init() {
        // 프로그램 실행
    }

    private void checkPermission() {
       boolean ok =  PermissionControl.checkPermission(this, REQ_PERMISSION);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (PermissionControl.checkPermission(this, REQ_PERMISSION)) {
                init();
            }
        } else {
            init();
        }
    }

    private void setWidget() {
        imageView = (ImageView) findViewById(R.id.imageView) ;
        btnCamera = (Button) findViewById(R.id.btnCamera);
    }

    private void setListener() {
        btnCamera.setOnClickListener(clickListener);
    }

    private View.OnClickListener clickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            switch (v.getId()){
                case R.id.btnCamera:    // 카메라버튼 동작

                    break;
            }
        }
    };



    // 2. 권한체크 후 콜백 < 사용자가 확인후 시스템이 호출하는 함수
    // onRequestPermissionsResult 액티비티 내에서 호출되는것이 아니라 시스템으로 호출되는 것이므로 public으로해야한다,
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if(requestCode == REQ_PERMISSION){
            // 2.1 배열에 넘긴 런타임권한을 체크해서 승인이 됬으면

            if(PermissionControl.onCheckResult(grantResults)) {
                init();
            }else {
                Toast.makeText(this, "권한을 허용하지 않으시면 프로그램을 실행할 수 없습니다.", Toast.LENGTH_LONG).show();
                // 선택 : 1 종료, 2 권한체크 다시 물어보기
                finish();
            }
        }
    }
}
