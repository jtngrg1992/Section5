package com.example.jatingarg.section5;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.SurfaceTexture;
import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraCaptureSession;
import android.hardware.camera2.CameraCharacteristics;
import android.hardware.camera2.CameraDevice;
import android.hardware.camera2.CameraManager;
import android.hardware.camera2.CaptureRequest;
import android.hardware.camera2.params.StreamConfigurationMap;
import android.os.Build;
import android.os.Environment;
import android.os.Handler;
import android.os.HandlerThread;
import android.support.annotation.NonNull;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Size;
import android.util.SparseIntArray;
import android.view.Surface;
import android.view.TextureView;
import android.view.View;
import android.widget.ImageButton;
import android.widget.Toast;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;

public class SecondActivity extends AppCompatActivity {

    private TextureView mTextureView;
    private CameraDevice mCameraDevice;
    private String mCameraID;
    private Size mPreviewSize;
    private CaptureRequest.Builder mCaptureRequestBuilder;
    private boolean isRecording = false;
    private File mVideoFolder;
    private String mvideoFileName;
    private static final String TAG = "MainActivity";
    private static SparseIntArray ORIENTATIONS = new SparseIntArray();
    static{
        ORIENTATIONS.append(Surface.ROTATION_0,0 );
        ORIENTATIONS.append(Surface.ROTATION_90,90 );
        ORIENTATIONS.append(Surface.ROTATION_180,180 );
        ORIENTATIONS.append(Surface.ROTATION_270,270 );
    }
    private CameraDevice.StateCallback mStateCallbackListner = new CameraDevice.StateCallback() {
        @Override
        public void onOpened(@NonNull CameraDevice camera) {
            mCameraDevice = camera;
            startPreview();
        }

        @Override
        public void onDisconnected(@NonNull CameraDevice camera) {
            camera.close();
            mCameraDevice = null;
        }

        @Override
        public void onError(@NonNull CameraDevice camera,int error) {
            camera.close();
            mCameraDevice = null;
        }
    };
    private TextureView.SurfaceTextureListener mListener = new TextureView.SurfaceTextureListener() {
        @Override
        public void onSurfaceTextureAvailable(SurfaceTexture surface, int width, int height) {
            setupCamera(width,height);
            connectCamera();
        }

        @Override
        public void onSurfaceTextureSizeChanged(SurfaceTexture surface, int width, int height) {

        }

        @Override
        public boolean onSurfaceTextureDestroyed(SurfaceTexture surface) {
            return false;
        }

        @Override
        public void onSurfaceTextureUpdated(SurfaceTexture surface) {

        }
    };

    private HandlerThread mHandlerThread;
    private Handler mBackgroundHandler;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_second);
        createVideoFolder();

        mTextureView = (TextureView)findViewById(R.id.texttureView);


    }

    @Override
    protected void onResume() {
        super.onResume();
        startBackgroundthread();
        if(mTextureView.isAvailable()){
            setupCamera(mTextureView.getWidth(),mTextureView.getHeight());
            connectCamera();
        }else{
            mTextureView.setSurfaceTextureListener(mListener);
        }
    }

    @Override
    protected void onPause() {
        closeCamera();
        stopBackgrounthread();
        super.onPause();
    }

    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);
        View decorView = getWindow().getDecorView();
        if(hasFocus){
            decorView.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN|View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                    |View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                    |View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                    |View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                    |View.SYSTEM_UI_FLAG_FULLSCREEN);
        }
    }

    private void closeCamera(){
        if(mCameraDevice != null){
            mCameraDevice.close();
            mCameraDevice = null;
        }
    }

    private void setupCamera(int width, int height){
        CameraManager cameraManager = (CameraManager) getSystemService(Context.CAMERA_SERVICE);
        try{
            for (String cameraId : cameraManager.getCameraIdList()){
                CameraCharacteristics cameraCharacteristics = cameraManager.getCameraCharacteristics(cameraId);
                if (cameraCharacteristics.get(CameraCharacteristics.LENS_FACING) == CameraCharacteristics.LENS_FACING_FRONT) {
                    continue;
                }

                StreamConfigurationMap map = cameraCharacteristics.get(CameraCharacteristics.SCALER_STREAM_CONFIGURATION_MAP);

                int deviceOrientation = getWindowManager().getDefaultDisplay().getRotation();
                int totalRotation = sensorToDeviceRotation(cameraCharacteristics,deviceOrientation);
                boolean swapRotation = totalRotation == 90 || totalRotation == 270;
                int rotatedWidth = width;
                int rotatedHeight = height;
                if(swapRotation){
                    rotatedWidth = rotatedHeight;
                    rotatedHeight = width;
                }
                mPreviewSize = chooseOptimalSize(map.getOutputSizes(SurfaceTexture.class),rotatedWidth,rotatedHeight);
                mCameraID = cameraId;
                return;

            }
        }catch(CameraAccessException e){
            e.printStackTrace();
        }

    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if(requestCode == 0){
            if(grantResults[0] != PackageManager.PERMISSION_GRANTED){
                Toast.makeText(getApplicationContext(),"Camera permission denied",Toast.LENGTH_SHORT).show();
            }
        }
        if(requestCode == 1){
            if(grantResults[0] == PackageManager.PERMISSION_GRANTED){
                isRecording = true;
                Toast.makeText(getApplicationContext(),"permission granted",Toast.LENGTH_SHORT).show();
                try{
                    createVideoFileName();
                }catch (IOException e){
                    e.printStackTrace();
                }
            }
        }
    }

    private void connectCamera(){
        CameraManager cameraManager = (CameraManager) getSystemService(Context.CAMERA_SERVICE);
        try{
            if(Build.VERSION.SDK_INT > Build.VERSION_CODES.M){
                if(ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED){
                    cameraManager.openCamera(mCameraID,mStateCallbackListner,mBackgroundHandler);
                }else{
                    if(shouldShowRequestPermissionRationale(Manifest.permission.CAMERA)){
                        Toast.makeText(this,"App requires access to camera", Toast.LENGTH_SHORT).show();

                    }
                    requestPermissions(new String[]{Manifest.permission.CAMERA},0);
                }
            }else{
                cameraManager.openCamera(mCameraID,mStateCallbackListner,mBackgroundHandler);
            }

        }catch(CameraAccessException e){
            e.printStackTrace();
        }

    }

    private void startPreview(){
        SurfaceTexture surfaceTexture = mTextureView.getSurfaceTexture();
        surfaceTexture.setDefaultBufferSize(mPreviewSize.getWidth(),mPreviewSize.getHeight());
        Surface previewSurface = new Surface(surfaceTexture);
        try{
            mCaptureRequestBuilder = mCameraDevice.createCaptureRequest(CameraDevice.TEMPLATE_PREVIEW);
            mCaptureRequestBuilder.addTarget(previewSurface);
            mCameraDevice.createCaptureSession(Arrays.asList(previewSurface), new CameraCaptureSession.StateCallback() {
                @Override
                public void onConfigured(@NonNull CameraCaptureSession session) {
                    try{
                        session.setRepeatingRequest(mCaptureRequestBuilder.build(),null,mBackgroundHandler);
                    }catch(CameraAccessException e){
                        e.printStackTrace();
                    }

                }

                @Override
                public void onConfigureFailed(@NonNull CameraCaptureSession session) {
                    Toast.makeText(getApplicationContext(),"Unable to setup camera preview",Toast.LENGTH_SHORT).show();
                }
            },null);
        }catch(CameraAccessException e){
            e.printStackTrace();
        }

    }

    private void startBackgroundthread(){
        mHandlerThread = new HandlerThread("Camera2Video");
        mHandlerThread.start();
        mBackgroundHandler = new Handler(mHandlerThread.getLooper());
    }

    private void stopBackgrounthread(){
        mHandlerThread.quitSafely();
        try{
            mHandlerThread.join();
            mHandlerThread = null;
            mBackgroundHandler = null;
        }catch(InterruptedException e){
            e.printStackTrace();
        }

    }

    private static int sensorToDeviceRotation(CameraCharacteristics camChars,int orientation){
        int sensorOrientation = camChars.get(CameraCharacteristics.SENSOR_ORIENTATION);
        orientation = ORIENTATIONS.get(orientation);
        return (sensorOrientation+orientation+360)%360;
    }

    private static class CompareSizeByArea implements Comparator<Size> {
        @Override
        public int compare(Size o1, Size o2) {
            return Long.signum((long)o1.getWidth() * o1.getHeight()/
                    (long)o2.getWidth() * o2.getHeight());
        }
    }

    private static Size chooseOptimalSize(Size[] choices, int width, int height){
        List<Size> isEnough = new ArrayList<Size>();
        for(Size option : choices){
            if(option.getHeight() == option.getWidth() * height / width &&
                    option.getWidth() >= width &&
                    option.getHeight() >= height){
                isEnough.add(option);
            }
        }
        if(isEnough.size() > 0){
            return Collections.min(isEnough, new SecondActivity.CompareSizeByArea());
        }else{
            return choices[0];
        }
    }

    private void createVideoFolder(){
        File videoFile = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_MOVIES);
        mVideoFolder = new File(videoFile,"VideoFolder");
        if(!mVideoFolder.exists()){
            mVideoFolder.mkdirs();
        }
    }

    private File createVideoFileName() throws IOException{
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String prepend = "VIO_"+timeStamp+"_";
        File videoFile = File.createTempFile(prepend,".mp4",mVideoFolder);
        mvideoFileName = videoFile.getAbsolutePath();
        return videoFile;
    }

    private void checkWriteStoragepermission(){
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.M){
            if(ContextCompat.checkSelfPermission(this,Manifest.permission.WRITE_EXTERNAL_STORAGE)==PackageManager.PERMISSION_GRANTED){
                isRecording = true;
                try{
                    createVideoFileName();
                }catch (IOException e){
                    e.printStackTrace();
                }
            }else{
                if(shouldShowRequestPermissionRationale(Manifest.permission.WRITE_EXTERNAL_STORAGE)){
                    Toast.makeText(getApplicationContext(),"app needs ext storage",Toast.LENGTH_SHORT).show();
                }
                requestPermissions(new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},1);

            }
        }else{
            isRecording = true;
            try{
                createVideoFileName();
            }catch (IOException e){
                e.printStackTrace();
            }
        }
    }
}
