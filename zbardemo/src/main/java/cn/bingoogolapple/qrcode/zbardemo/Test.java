package cn.bingoogolapple.qrcode.zbardemo;

import java.io.File;

import cn.bingoogolapple.qrcode.core.UploadUtils;

/**
 * Created by gwd on 2016/6/2.
 */
public class Test {
    public static void main(String[] args){
        String requestUrl="http://192.168.1.2:8080/upload";
        File file=new File("E:\\System\\Pictures\\Apache\\demo.png");
        //String str= UploadUtils.uploadFile(file,requestUrl);
        System.out.println("....");
    }
}
