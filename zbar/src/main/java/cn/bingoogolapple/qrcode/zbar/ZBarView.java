package cn.bingoogolapple.qrcode.zbar;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.hardware.Camera;
import android.os.AsyncTask;
import android.os.Environment;
import android.text.TextUtils;
import android.util.AttributeSet;

import net.sourceforge.zbar.Config;
import net.sourceforge.zbar.ImageScanner;
import net.sourceforge.zbar.Symbol;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

import cn.bingoogolapple.qrcode.core.QRCodeView;
import cn.bingoogolapple.qrcode.core.UploadFileTask;

public class ZBarView extends QRCodeView {

    static {
        System.loadLibrary("iconv");
    }

    private ImageScanner mScanner;

    public ZBarView(Context context, AttributeSet attributeSet) {
        this(context, attributeSet, 0);
    }

    public ZBarView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        setupScanner();
    }

    public void setupScanner() {
        mScanner = new ImageScanner();
        mScanner.setConfig(0, Config.X_DENSITY, 3);
        mScanner.setConfig(0, Config.Y_DENSITY, 3);

        mScanner.setConfig(Symbol.NONE, Config.ENABLE, 0);
        for (BarcodeFormat format : BarcodeFormat.ALL_FORMATS) {
            mScanner.setConfig(format.getId(), Config.ENABLE, 1);
        }
    }

    @Override
    protected void handleData(final String data,final Camera camera) {
        new AsyncTask<Void,Void,String>() {

            @Override
            protected String doInBackground(Void... params) {

               String result="正在识别中";
                if(data!=null)
                    return result;
                //TODO
                return result;
            }

            @Override
            protected void onPostExecute(String result) {
                if (mDelegate != null && !TextUtils.isEmpty(result)) {
                    mDelegate.onScanQRCodeSuccess(result);
                } else {
                    try {
                        camera.setOneShotPreviewCallback(ZBarView.this);
                    } catch (RuntimeException e) {
                    }
                }
            }
        }.execute();

    }




    public String shotImage(byte[] data){
        String filename= Environment.getExternalStorageDirectory()+"";
        FileOutputStream fileOutputStream=null;
        try {
            File file=new File(filename+"/DCIM/temp.png");
            BitmapFactory.Options o = new BitmapFactory.Options();
            o.inJustDecodeBounds = true;
            Bitmap bitmap=BitmapFactory.decodeStream(new ByteArrayInputStream(data),null,o);
            bitmap=BitmapFactory.decodeByteArray(data,0,data.length,o);
            if(!file.exists())
                file.createNewFile();
            fileOutputStream=new FileOutputStream(file);
            // fileOutputStream.write(data);
            bitmap.compress(Bitmap.CompressFormat.PNG,100,fileOutputStream);
            fileOutputStream.flush();
            fileOutputStream.close();
            // Bitmap bitmap= BitmapFactory.decodeByteArray();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            return "waiting for a moment....";
        }catch (IOException e1){
            e1.printStackTrace();
            return  "retry";
        }
        return null;
    }
}