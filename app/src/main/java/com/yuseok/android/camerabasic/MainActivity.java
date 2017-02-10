package com.yuseok.android.camerabasic;


import android.content.ContentValues;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
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

        // 1. 위젯을 셋팅하고
        setWidget();
        // 2. 버튼관련 컨트롤러 활성화처리
        buttonDisable();
        // 3. 리스너 계열 등록
        setListener();
        // 4. 권한처리
        checkPermission();
    }

    // 버튼 비활성화하기
    private void buttonDisable() {
        btnCamera.setEnabled(false);
    }

    // 버튼 활성화하기
    private void buttonEnable() {
        btnCamera.setEnabled(true);
    }

    private void init() {
        // 프로그램 실행
        // 권한처리가 통과 되었을때만 버튼을 활성화 시켜준다.
        buttonEnable();
    }

    private void checkPermission() {
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

    // 리스너 셋팅
    private void setListener() {
        btnCamera.setOnClickListener(clickListener);
    }

    // 리스너 정의
    Uri fileUri = null;
    private View.OnClickListener clickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            Intent intent = null;
            switch (v.getId()){
                case R.id.btnCamera:    // 카메라버튼 동작
                    intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
//                    startActivityForResult(intent, REQ_CAMERA);
                    // 마시멜로 이상 버전부터 기본 Action Image Capture 로는 처리안됨
                    // --- 카메라 촬영 후 미디어 컨텐트 uri 를 생성해서 외부저장소에 저장한다 ---
                    if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                        ContentValues values = new ContentValues(1);
                        values.put(MediaStore.Images.Media.MIME_TYPE, "image/jpg");
                        fileUri = getContentResolver().insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values);
                        intent.putExtra(MediaStore.EXTRA_OUTPUT, fileUri);
                        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION | Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
                    }
                        // --- 여기 까지 컨텐트 uri 강제세팅 ---

                        startActivityForResult(intent, REQ_CAMERA);
                        break;

            }
        }
    };

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) { // 데이터를 통해서 촬영한 이미지를 넘겨준다.
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode == REQ_CAMERA) {
                if(Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
                            fileUri = data.getData();
            }
            Log.i("Camera","fileUri==============================="+fileUri);

            if(fileUri != null) {
                imageView.setImageURI(fileUri);
            } else {
                Toast.makeText(this, "사진파일이 없습니다", Toast.LENGTH_LONG).show();
            }
        }
    }

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
//                finish();
            }
        }
    }
}
