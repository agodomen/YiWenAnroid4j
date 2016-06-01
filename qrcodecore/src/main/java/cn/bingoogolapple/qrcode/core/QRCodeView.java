package cn.bingoogolapple.qrcode.core;

import android.annotation.TargetApi;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.graphics.PixelFormat;
import android.graphics.Rect;
import android.hardware.Camera;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.os.Handler;
import android.os.StatFs;
import android.util.AttributeSet;
import android.view.View;
import android.widget.FrameLayout;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public abstract class QRCodeView extends FrameLayout implements Camera.PreviewCallback {
    protected Camera mCamera;
    protected CameraPreview mPreview;
    protected ScanBoxView mScanBoxView;
    protected Delegate mDelegate;
    protected Handler mHandler;

    public QRCodeView(Context context, AttributeSet attributeSet) {
        this(context, attributeSet, 0);
    }

    public QRCodeView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        mHandler = new Handler();
        initView(context, attrs);
    }

    public void setResultHandler(Delegate delegate) {
        mDelegate = delegate;
    }

    private void initView(Context context, AttributeSet attrs) {
        mPreview = new CameraPreview(getContext());

        mScanBoxView = new ScanBoxView(getContext());
        mScanBoxView.initCustomAttrs(context, attrs);

        addView(mPreview);
        addView(mScanBoxView);
    }

    /**
     * 显示扫描框
     */
    public void showScanRect() {
        if (mScanBoxView != null) {
            mScanBoxView.setVisibility(View.VISIBLE);
        }
    }

    /**
     * 隐藏扫描框
     */
    public void hiddenScanRect() {
        if (mScanBoxView != null) {
            mScanBoxView.setVisibility(View.GONE);
        }
    }

    /**
     * 打开摄像头开始预览，但是并未开始识别
     */
    public void startCamera() {
        if (mCamera != null) {
            return;
        }

        try {
            mCamera = Camera.open();
        } catch (Exception e) {
            if (mDelegate != null) {
                mDelegate.onScanQRCodeOpenCameraError();
            }
        }
        if (mCamera != null) {
            mPreview.setCamera(mCamera);
            mPreview.initCameraPreview();
        }
    }

    /**
     * 关闭摄像头预览，并且隐藏扫描框
     */
    public void stopCamera() {
        stopSpotAndHiddenRect();
        if (mCamera != null) {
            mPreview.stopCameraPreview();
            mPreview.setCamera(null);
            mCamera.release();
            mCamera = null;
        }
    }

    /**
     * 延迟1.5秒后开始识别
     */
    public void startSpot() {
        startSpotDelay(1500);
    }

    /**
     * 延迟delay毫秒后开始识别
     *
     * @param delay
     */
    public void startSpotDelay(int delay) {
        startCamera();
        // 开始前先移除之前的任务
        mHandler.removeCallbacks(mOneShotPreviewCallbackTask);
        mHandler.postDelayed(mOneShotPreviewCallbackTask, delay);
    }

    /**
     * 停止识别
     */
    public void stopSpot() {
        if (mCamera != null) {
            mCamera.setOneShotPreviewCallback(null);
        }
        if (mHandler != null) {
            mHandler.removeCallbacks(mOneShotPreviewCallbackTask);
        }
    }

    /**
     * 停止识别，并且隐藏扫描框
     */
    public void stopSpotAndHiddenRect() {
        stopSpot();
        hiddenScanRect();
    }

    /**
     * 显示扫描框，并且延迟1.5秒后开始识别
     */
    public void startSpotAndShowRect() {
        startSpot();
        showScanRect();
    }

    /**
     * 打开闪光灯
     */
    public void openFlashlight() {
        mPreview.openFlashlight();
    }

    /**
     * 关闭散光灯
     */
    public void closeFlashlight() {
        mPreview.closeFlashlight();
    }
    public  void turnFlag(){
        flag=true;
    }
    private  static volatile   boolean flag=false;
    @Override
    public void onPreviewFrame(byte[] data, Camera camera) {

        if(flag==true){
          //  camera.autoFocus(mAutoFocusCallback);
             //camera.takePicture(null,null,jpegCallback);
            try {
                File f1=getOutputMediaFile(0);
            if(!f1.exists())
                    f1.createNewFile();
                FileOutputStream fileOutputStream=new FileOutputStream(f1);
                fileOutputStream.write(data);
                fileOutputStream.flush();
                fileOutputStream.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }

            takeImage(camera);
            flag=false;
        }

    /*  Camera.Parameters parameters = camera.getParameters();
        Camera.Size size = parameters.getPreviewSize();
        int width = size.width;
        int height = size.height;

        byte[] rotatedData = new byte[data.length];
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                rotatedData[x * height + height - y - 1] = data[x + y * width];
            }
        }
        int tmp = width;
        width = height;
        height = tmp;
        data = rotatedData;*/

        handleData(new String(), camera);
    }


    public synchronized void takeImage(Camera camera){

        Camera.Parameters params = camera.getParameters();
        camera.setParameters(params);
      //  camera.autoFocus(mAutoFocusCallback);
        camera.takePicture(null,null,mPicture);


    }



    private Camera.AutoFocusCallback mAutoFocusCallback = new Camera.AutoFocusCallback() {
        public void onAutoFocus(boolean success, Camera camera) {
            // TODO Auto-generated method stub
            if(success){
                camera.setOneShotPreviewCallback(QRCodeView.this);
            }
        }
    };
    private static Uri getOutputMediaFileUri(int type){
        return Uri.fromFile(getOutputMediaFile(type));
    }

    /** Create a File for saving an image or video */
    private static File getOutputMediaFile(int type){
        File mediaStorageDir = new File(Environment.getExternalStoragePublicDirectory(
                Environment.DIRECTORY_PICTURES), "Camera");
        if (! mediaStorageDir.exists()){
            if (! mediaStorageDir.mkdirs()){
                return null;
            }
        }
        File mediaFile;
        if (type == MEDIA_TYPE_IMAGE){
            mediaFile = new File(mediaStorageDir.getPath() + File.separator +
                    "demo.jpg");
        }else if(type==1024){
            mediaFile = new File(mediaStorageDir.getPath() + File.separator +
                    "tmp.jpg");
        }
        else {
             mediaFile= new File(mediaStorageDir.getPath() + File.separator +
                    type+".jpg");
        }

        return mediaFile;
    }
    public static final int MEDIA_TYPE_IMAGE = 1;

    private Camera.PictureCallback mPicture = new Camera.PictureCallback() {

        @Override
        public void onPictureTaken(byte[] data, Camera camera) {

            File pictureFile = getOutputMediaFile(MEDIA_TYPE_IMAGE);
            File f1=getOutputMediaFile(1024);

            try {
                if (!pictureFile.exists()){
                    pictureFile.createNewFile();
                }
                if(!f1.exists())
                    f1.createNewFile();
                FileOutputStream fos = new FileOutputStream(pictureFile);
                fos.write(data);
                fos.close();

                BitmapFactory.Options o = new BitmapFactory.Options();
                o.inJustDecodeBounds = true;
                Bitmap bitmap=BitmapFactory.decodeFile(pictureFile.getPath());
              //  Bitmap bitmap=BitmapFactory.decodeByteArray(data,0,data.length,o);
                bitmap=ImageCrop(bitmap);

                FileOutputStream fileOutputStream=new FileOutputStream(f1);
                bitmap.compress(Bitmap.CompressFormat.PNG,90,fileOutputStream);
                fileOutputStream.flush();
                fileOutputStream.close();
            } catch (FileNotFoundException e) {

            } catch (IOException e) {

            }finally {
                stopCamera();
                startCamera();
                showScanRect();
                startSpot();
            }

        }
    };
    private volatile   Camera.PictureCallback  jpegCallback = new Camera.PictureCallback(){
        @Override
        public void onPictureTaken(byte[] data, Camera camera) {
            // TODO Auto-generated method stub
            Camera.Parameters ps = camera.getParameters();
            if(ps.getPictureFormat() == PixelFormat.JPEG){
                //存储拍照获得的图片

                String path = save(data);
                stopCamera();
                startCamera();
           //     camera.autoFocus(mAutoFocusCallback);
                showScanRect();
                startSpot();
            }
        }
    };

    private synchronized String save(byte[] data){               //保存jpg到SD卡中
        String path = Environment.getExternalStorageDirectory()+"/Camera";
        File file=new File(path);
        if(!file.exists())
            file.mkdirs();
        try{
            if(Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)){
                //判断SD卡上是否有足够的空间
                String storage = Environment.getExternalStorageDirectory().toString();
                StatFs fs = new StatFs(storage);
                long available = fs.getAvailableBlocks()*fs.getBlockSize();
                if(available<data.length){
                    //空间不足直接返回空
                    return null;
                }

                File f = new File(path+"/tmp.jpg");
                File f1=new File(path+"/demo.jpg");
                if(!file.exists())
                    //创建文件
                    file.createNewFile();
                FileOutputStream fos = new FileOutputStream(f);
                fos.write(data);
                fos.close();
                BitmapFactory.Options o = new BitmapFactory.Options();
                o.inJustDecodeBounds = true;
                Bitmap bitmap=BitmapFactory.decodeByteArray(data,0,data.length,o);
                bitmap=ImageCrop(bitmap);

                FileOutputStream fileOutputStream=new FileOutputStream(f1);
                bitmap.compress(Bitmap.CompressFormat.PNG,100,fileOutputStream);
            }
        }catch(Exception e){
            e.printStackTrace();
            return null;
        }
        return path;
    }
    public  Bitmap ImageCrop(Bitmap bitmap) {
        int w = bitmap.getWidth(); // 得到图片的宽，高
        int h = bitmap.getHeight();

        int wh = w > h ? h : w;// 裁切后所取的正方形区域边长

        int retX = w > h ? (w - h) / 2 : 0;//基于原图，取正方形左上角x坐标
        int retY = w > h ? 0 : (h - w) / 2;
        w=(int)(w*0.2076923076);
        h=(int)(h*0.3948717948);
        Rect rect= mScanBoxView.getmFramingRect();
        int width=rect.width();
        int hegith=rect.height();
        int a=mScanBoxView.getLeft();
        int b=mScanBoxView.getRight();
        double c=mScanBoxView.getX();
        double d=mScanBoxView.getY();
        //下面这句是关键
        return Bitmap.createBitmap(bitmap,w ,h, 700,700, null, false);
    }
    protected abstract void handleData(String data,Camera camera);

    private Runnable mOneShotPreviewCallbackTask = new Runnable() {
        @Override
        public void run() {
            if (mCamera != null) {
                mCamera.setOneShotPreviewCallback(QRCodeView.this);
            }
        }
    };

    public interface Delegate {
        /**
         * 处理扫描结果
         *
         * @param result
         */
        void onScanQRCodeSuccess(String result);

        /**
         * 处理打开相机出错
         */
        void onScanQRCodeOpenCameraError();
    }
}