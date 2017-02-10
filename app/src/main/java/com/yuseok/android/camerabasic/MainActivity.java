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

import com.bumptech.glide.Glide;

public class MainActivity extends AppCompatActivity {

    // 권한 요청 코드
    private final int REQ_PERMISSION = 100;
    private final int REQ_CAMERA = 101; // 카메라 권한요청코드
    private final int REQ_GALLERY = 102; // 갤러리 권한요청 코드

    Button btnCamera, btnGallery;
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
        btnGallery = (Button) findViewById(R.id.btnGallery);
    }

    // 리스너 셋팅
    private void setListener() {
        btnCamera.setOnClickListener(clickListener);
        btnGallery.setOnClickListener(clickListener);
    }

    // 사진촬영 후 임시로 저장할 파일 공간
    Uri fileUri = null;
    // 리스너 정의
    private View.OnClickListener clickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            Intent intent = null;
            switch (v.getId()){
                case R.id.btnCamera:    // 카메라버튼 동작
                    intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
//                  startActivityForResult(intent, REQ_CAMERA);

                    // 마시멜로 이상 버전부터 기본 Action Image Capture 로는 처리안됨
                    // --- 카메라 촬영 후 미디어 컨텐트 uri 를 생성해서 외부저장소에 저장한다 ---
                    if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                        // 저장할 미디어 속성을 정의하는 클래스
                        ContentValues values = new ContentValues(1);
                        // 속성중에 파일의 종류를 정의
                        values.put(MediaStore.Images.Media.MIME_TYPE, "image/jpg");
                        // 전역변수로 정의한 fileUri에 외부저장소 컨텐츠가 있는 Uri를 임시로 생성해서 넣어준다.
                        fileUri = getContentResolver().insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values);
                        // 위에서 생성한 fileUri를 사진저장 공간으로 설정
                        intent.putExtra(MediaStore.EXTRA_OUTPUT, fileUri);
                        // Uri에 읽기와 쓰기 권한을 시스템에 요청
                        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION | Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
                    }
                    // --- 여기 까지 컨텐트 uri 강제세팅 ---
                    startActivityForResult(intent, REQ_CAMERA);
                    break;

                case R.id.btnGallery: // 갤러리에서 이미지 불러오기
                    intent = new Intent(intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                    // 이미지 여러개 가져오는 플래그
                    // intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true);
                    intent.setType("image/*"); // 외부저장소에 있는 이미지만 가져오기 위한 필터링 //원하는 타입만 가져오게 필터링하는 작업
                    startActivityForResult(intent.createChooser(intent,"Select Picture"), REQ_GALLERY);
                    break;
            }
        }
    };

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) { // 데이터를 통해서 촬영한 이미지를 넘겨준다.
        super.onActivityResult(requestCode, resultCode, data);
        Log.i("Camera","resultCode==============================="+resultCode);

        switch(requestCode) {

            case REQ_GALLERY:
                if(resultCode == RESULT_OK) {
                    fileUri = data.getData();
                    Glide.with(this)
                            .load(fileUri)
                            .into(imageView);
                }
                break;
            case REQ_CAMERA :
                if (requestCode == REQ_CAMERA && resultCode == RESULT_OK) { // 사진 확인처리됨 RESULT_OK = -1
                    // 롤리팝 체크
                    if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
                        Log.i("Camera", "data.getData()===============================" + data.getData());
                        fileUri = data.getData();
                    }
                    Log.i("Camera", "fileUri===============================" + fileUri);
                    if (fileUri != null) {
                        // 글라이드로 이미지 세팅하면 자동으로 사이즈 조절
                        Glide.with(this)
                                .load(fileUri)
                                .into(imageView);
                    } else {
                        Toast.makeText(this, "사진파일이 없습니다", Toast.LENGTH_LONG).show();
                    }
                } else {
                    // resultCode 가 0이고 사진이 찍혔으면 uri 가 남는데
                    // uri 가 있을 경우 삭제처리...
                }
                break;
        }
    }
    // 2. 권한체크 후 콜백 < 사용자가 확인후 시스템이 호출하는 함수
    // onRequestPermissionsResult 액티비티 내에서 호출되는것이 아니라 시스템으로 호출되는 것이므로 public으로해야한다,
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if(requestCode == REQ_PERMISSION) {
            if (PermissionControl.onCheckResult(grantResults)) {
                init();
            } else {
                Toast.makeText(this, "권한을 허용하지 않으시면 프로그램을 실행할 수 없습니다.", Toast.LENGTH_LONG).show();
            }
        }
    }
}
