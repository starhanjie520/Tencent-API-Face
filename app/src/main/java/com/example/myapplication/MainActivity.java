package com.example.myapplication;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.pm.PackageManager;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.os.Build;
import android.os.Bundle;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.ImageFormat;
import android.graphics.Rect;
import android.graphics.SurfaceTexture;
import android.graphics.YuvImage;
import android.hardware.Camera;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.os.StrictMode;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.TextureView;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.ImageView;

import android.widget.Spinner;
import android.widget.TextView;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import com.tencentcloudapi.common.Credential;
import com.tencentcloudapi.common.profile.ClientProfile;
import com.tencentcloudapi.common.profile.HttpProfile;
import com.tencentcloudapi.common.exception.TencentCloudSDKException;

import com.tencentcloudapi.iai.v20180301.IaiClient;

import com.tencentcloudapi.iai.v20180301.models.SearchFacesRequest;
import com.tencentcloudapi.iai.v20180301.models.SearchFacesResponse;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import Decoder.BASE64Encoder;


public class MainActivity extends AppCompatActivity  implements TextureView.SurfaceTextureListener{

    private static final int CAMERA_REQUEST_CODE = 0x00000010;
    private static final int PERMISSION_CAMERA_REQUEST_CODE = 0x00000012;
    private TextureView textureView;
    private ImageView ivPic;
    private Button btnTakePic;
    private Camera mCamera;
    private Spinner spinner;

    private CheckBox checkBox ;
    private ImageView mImageView;
    private static final String[] m={"4회/s","2회/s","1회/s"};
    private static int frequent = 28;
    private ArrayAdapter<String> adapter;
    public static String name = "";
    public boolean check = false;
    TextView text;
    @SuppressLint("HandlerLeak")
    private Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case 1:
                    if(checkBox.isChecked()){
                        test();
                    }
                    text.setText(msg.obj.toString());
                    break;
                case 2:
                    break;
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        checkPermissionAndCamera();
        text = (TextView) findViewById(R.id.test001);
        textureView = (TextureView) findViewById(R.id.texture_view);
      //  textureView.setRotation(90.0f);
        textureView.setSurfaceTextureListener(this);
        mImageView = (ImageView) findViewById(R.id.iv);
        btnTakePic = (Button) findViewById(R.id.btn_takePic);
        spinner = (Spinner) findViewById(R.id.Spinner01);
        adapter = new ArrayAdapter<String>(this,android.R.layout.simple_spinner_item,m);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(adapter);
        spinner.setOnItemSelectedListener(new SpinnerSelectedListener());
        spinner.setVisibility(View.VISIBLE);

        checkBox = (CheckBox) findViewById(R.id.check1) ;
        checkBox.setChecked(false);
        checkBox.setOnClickListener(new CheckBox.OnClickListener() {
            @Override
            public void onClick(View v) {
                check = checkBox.isChecked();
            }
        }) ;

        btnTakePic.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                addCallBack();
            }
        });
       // test();
    }

    int Height = 0;
    int Width = 0;
    int X = 0;
    int Y = 0;
    private void test() {
        try {
            Bitmap bitmaps = Bitmap.createBitmap(400,700,Bitmap.Config.ARGB_8888);
            Canvas canvas = new Canvas(bitmaps);
            // canvas.drawColor(Color.LTGRAY);
            Paint paint = new Paint();
            paint.setStyle(Paint.Style.STROKE);
            paint.setStrokeWidth(2);
            paint.setColor(Color.RED);
           // paint.setAlpha(100);
            paint.setTextSize(20);
            DisplayMetrics dm=new DisplayMetrics();
            getWindowManager().getDefaultDisplay().getMetrics(dm);
            int deviceWidth = dm.widthPixels;
            int dipWidth  = (int) (dm.widthPixels  / dm.density);
            Log.d("dpx===deviceWidth===", String.valueOf(deviceWidth));
            Log.d("dpx===dipWidth===", String.valueOf(dipWidth));
            int aad  =(int)(dipWidth  / deviceWidth);
            Log.d("dpx===aad===", String.valueOf(aad));

            int aa = (int) Math.round(0.37);
            System.out.println(aa);
            int dpx = X*aa;
            int dpy = Y*aa;
            int dp_Width = Width*aa;
            int d_Height = Height*aa;

            canvas.drawText(name, (float) (X*0.37) + 20, (float) (Y*0.37)-20, paint);
            canvas.drawRect((float) (X*0.37), (float) (Y*0.37), (float) (X*0.37)+(float) (Width*0.37), (float) (Y*0.37)+(float) (Height*0.37),paint);
            mImageView.setImageBitmap(bitmaps);

        }catch (Exception e){
            e.printStackTrace();
        }
    }

    class SpinnerSelectedListener implements AdapterView.OnItemSelectedListener {

        public void onItemSelected(AdapterView<?> arg0, View arg1, int arg2,
                                   long arg3) {
            Log.d("region : ",m[arg2]);
            if(m[arg2].equals("1회/s")){
                frequent = 100;
            }if(m[arg2].equals("2회/s")){
                frequent = 14;
            }if(m[arg2].equals("4회/s")){
                frequent = 5;
            }else {
                frequent = 20;
            }
        }
        public void onNothingSelected(AdapterView<?> arg0) {
        }
    }

    private void checkPermissionAndCamera() {
        if (android.os.Build.VERSION.SDK_INT > 9) {
            StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
            StrictMode.setThreadPolicy(policy);
        }

        int hasCameraPermission = ContextCompat.checkSelfPermission(getApplication(),
                Manifest.permission.CAMERA);
        if (!(hasCameraPermission == PackageManager.PERMISSION_GRANTED)) {
            ActivityCompat.requestPermissions(this,new String[]{Manifest.permission.CAMERA},
                    PERMISSION_CAMERA_REQUEST_CODE);
        }
        if (Build.VERSION.SDK_INT >= 23) {
            int REQUEST_CODE_CONTACT = 101;
            String[] permissions = {Manifest.permission.WRITE_EXTERNAL_STORAGE};
            for (String str : permissions) {
                if (this.checkSelfPermission(str) != PackageManager.PERMISSION_GRANTED) {
                    //申请权限
                    this.requestPermissions(permissions, REQUEST_CODE_CONTACT);
                }
            }
        }
    }

    @Override
    public void onSurfaceTextureAvailable(SurfaceTexture surface, int width, int height) {
        mCamera = Camera.open(1);
        if (mCamera != null) {
            Camera.Parameters params = mCamera.getParameters();
            params.setPreviewSize(width, height);
            List<String> focusModes = params.getSupportedFocusModes();
            if (focusModes.contains(Camera.Parameters.FOCUS_MODE_AUTO)) {
                params.setFocusMode(Camera.Parameters.FOCUS_MODE_AUTO);
              //  mCamera.setParameters(params);
            }
            try {
                mCamera.setDisplayOrientation(90);// 设置预览角度，并不改变获取到的原始数据方向
                mCamera.setPreviewTexture(surface);
                mCamera.startPreview();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
    public static int temp = 0;
    private void addCallBack() {
        if (mCamera != null) {
            mCamera.setPreviewCallback(new Camera.PreviewCallback() {
                @Override
                public void onPreviewFrame(byte[] data, Camera camera) {
                    Camera.Size size = camera.getParameters().getPreviewSize();
                    try {
                        YuvImage image = new YuvImage(data, ImageFormat.NV21, size.width, size.height, null);
                        if (image != null) {
                            int j = temp % frequent;
                            temp++;
                            if( j == 0){
                                Date d = new Date();
                                String s = null;
                                DateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                                s = sdf.format(d);
                                System.out.println(s);
                                Log.d("timeeeeee===",s.toString());

                                ByteArrayOutputStream stream = new ByteArrayOutputStream();
                                image.compressToJpeg(new Rect(0, 0, size.width, size.height), 30, stream);
                                Bitmap bmp = BitmapFactory.decodeByteArray(stream.toByteArray(), 0, stream.size());
                                bmp = rotaingImageView(270,bmp);

                                //좌우 반전
                                Matrix sideInversion = new Matrix();
                                sideInversion.setScale(-1, 1);
                                bmp = Bitmap.createBitmap(bmp, 0, 0,bmp.getWidth(), bmp.getHeight(), sideInversion, false);

                              //  ivPic.setImageBitmap(bmp);
                                stream.close();
                                final Bitmap finalBmp = bmp;
                                new Thread(){
                                    @Override
                                    public void run() {
                                        super.run();
                                        try {
                                            saveBitmap(finalBmp);
                                        } catch (IOException e) {
                                            e.printStackTrace();
                                        }
                                    }
                                }.start();
                            }
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            });
        }
    }


    private void saveBitmap(final Bitmap bitmap) throws IOException
    {
        File tempFile = new File(Environment.getExternalStorageDirectory(),"/aaa.jpg");
        String storageDir =  Environment.getExternalStorageDirectory()+"/aaa.jpg";
        FileOutputStream out = null;
        try {
            out = new FileOutputStream(tempFile);
            bitmap.compress(Bitmap.CompressFormat.JPEG, 90, out);
            String face = GetTencentDB(getImageStr(storageDir));
            String PersonId = "";
            String Score = "";

            if(!face.equals("error")){
                JSONObject jsonObject = new JSONObject(face);
                JSONArray movieArray = jsonObject.getJSONArray("Results");
                JSONObject movieObject = movieArray.getJSONObject(0);
                JSONArray movieArray2 = movieObject.getJSONArray("Candidates");
                JSONObject movieObject2 = movieArray2.getJSONObject(0);
                Log.d("韩杰print===",jsonObject.toString());
                PersonId = movieObject2.getString("PersonName");
                Score = movieObject2.getString("Score");

                JSONObject xyz = movieObject.getJSONObject("FaceRect");
                Log.d("韩杰print===",xyz.toString());
                Height= xyz.getInt("Height");
                Width= xyz.getInt("Width");
                X= xyz.getInt("X");
                Y= xyz.getInt("Y");
            }else{
                PersonId = "알수 없음";
            }
            name = PersonId;
            String person_score = PersonId+":"+Score+"%";
            Message message = new Message();
            message.what = 1;
            message.obj = person_score;
            handler.sendMessage(message);
            Log.d("face",person_score);
        } catch (FileNotFoundException | JSONException e) {
            e.printStackTrace();
        }
    }


    public String GetTencentDB(final String imgFile) {
        String temp = "error";
        try{
            Credential cred = new Credential("XXXXXXXXXXXXX", "YYYYYYYYYYYYYYYYYYYYYYYY");
            HttpProfile httpProfile = new HttpProfile();
            httpProfile.setEndpoint("iai.tencentcloudapi.com");
            ClientProfile clientProfile = new ClientProfile();
            clientProfile.setHttpProfile(httpProfile);
            IaiClient client = new IaiClient(cred, "ap-seoul", clientProfile);
            String persionInfo = "1";
           // String params = "{\"GroupIds\":[\"hjtest\"],\"Image\":\""+imgFile+"\"}";
            String params = "{\"GroupIds\":[\"hjtest\"],\"Image\":\""+imgFile+"\",\"NeedPersonInfo\":1}";
            SearchFacesRequest req = SearchFacesRequest.fromJsonString(params, SearchFacesRequest.class);
            SearchFacesResponse resp = client.SearchFaces(req);
            System.out.println(SearchFacesRequest.toJsonString(resp));
            temp = SearchFacesRequest.toJsonString(resp);
        } catch (TencentCloudSDKException e) {
            temp = "error";
        }
        return temp;
    }

    public static Bitmap rotaingImageView(int angle, Bitmap bitmap) {
        Matrix matrix = new Matrix();
        matrix.postRotate(angle);
        Bitmap resizedBitmap = Bitmap.createBitmap(bitmap, 0, 0,
                bitmap.getWidth(), bitmap.getHeight(), matrix, true);
        return resizedBitmap;
    }

    public static String getImageStr(String imgFile) {
        InputStream inputStream = null;
        byte[] data = null;
        try {
            inputStream = new FileInputStream(imgFile);
            data = new byte[inputStream.available()];
            inputStream.read(data);
            inputStream.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

        BASE64Encoder encoder = new BASE64Encoder();
        return encoder.encode(data);
    }

    @Override
    public void onSurfaceTextureSizeChanged(SurfaceTexture surface, int width, int height) {

    }

    @Override
    public boolean onSurfaceTextureDestroyed(SurfaceTexture surface) {
        mCamera.stopPreview();
        mCamera.release();
        return false;
    }

    @Override
    public void onSurfaceTextureUpdated(SurfaceTexture surface) {

    }
}
