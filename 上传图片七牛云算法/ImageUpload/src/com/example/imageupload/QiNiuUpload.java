package com.example.imageupload;
import com.example.imageupload.NativeUtil;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;

import javax.crypto.Mac;

import org.json.JSONException;
import org.json.JSONObject;

import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Rect;
import android.os.Handler;
import android.os.Message;
import android.util.Log;


import com.qiniu.android.common.Zone;
import com.qiniu.android.http.ResponseInfo;
import com.qiniu.android.storage.Configuration;
import com.qiniu.android.storage.UpCompletionHandler;
import com.qiniu.android.storage.UploadManager;
import com.qiniu.util.Auth;

public class QiNiuUpload {
	String jsonResult="";
	long timeinfo=0;
	String key="";
	double picSizeDouble=0.0;
	String cloudFileName="";
	static String cloudPicUrl="";
	
	public String getToken(String AK, String SK, String buketname) {
		String ACCESS_KEY = AK;
		String SECRET_KEY = SK;
		String bucketname = buketname;
		Auth auth = Auth.create(ACCESS_KEY, SECRET_KEY);
		String token = auth.uploadToken(bucketname);
		return token;
	}

	public static void compressImageToFile(Bitmap bmp,File file) {
	    // 0-100 100为不压缩
	    int options = 100; 
	    ByteArrayOutputStream baos = new ByteArrayOutputStream();
	    // 把压缩后的数据存放到baos中
	    bmp.compress(Bitmap.CompressFormat.JPEG, options, baos);
	    try {  
	        FileOutputStream fos = new FileOutputStream(file);  
	        fos.write(baos.toByteArray());  
	        fos.flush();  
	        fos.close();  
	    } catch (Exception e) {  
	        e.printStackTrace();  
	    } 
	}
	
	public double getPicFileSize(String picPath){
		File f= new File(picPath);
		 if (f.exists() && f.isFile()){
		  return Double.parseDouble(String.valueOf(f.length()))/1024/1024; //单位MB
		 }else{
			 return -1; //代表文件不存在
		 }	
	}
	
	public int upLoadQiNiu(String picPath,String cloudFileName)
	{
		double picSize=getPicFileSize(picPath);
		if(picSize>2.5)  //大于2.5M
		{
			Log.i("File Size","Too Lager");
			return 3; //文件太大
		}
		else
		{
			File picfile = new File(picPath); 
			String ACCESS_KEY = "V6UKn_EQAB0sNo8WC7N7cXwBABDIhnb6F6YjzkxS";
			String SECRET_KEY = "KKvicgXXzQMba_JuoP_rBsCQeeHMWxQ5k3i2t3oA";
			String bucketname = "codejudge";
			String token = getToken(ACCESS_KEY, SECRET_KEY, bucketname);
			Configuration config = new Configuration.Builder()
            .chunkSize(256 * 1024)  //分片上传时，每片的大小。 默认256K
            .putThreshhold(512 * 1024)  // 启用分片上传阀值。默认512K
            .connectTimeout(10) // 链接超时。默认10秒
            .responseTimeout(60) // 服务器响应超时。默认60秒
            .zone(Zone.httpAutoZone) // 设置区域，指定不同区域的上传域名、备用域名、备用IP。
            .build();
//重用uploadManager。一般地，只需要创建一个uploadManager对象
			UploadManager uploadManager = new UploadManager(config);
		//	uploadManager.put(picLocalUrl, "2017123.jpg", token, null, null);
			key=cloudFileName;
			uploadManager.put(picfile, key, token,
		            new UpCompletionHandler() {
		                @Override
						public void complete(String key,
								ResponseInfo info, JSONObject res) {
		                	//System.out.println(picLocalUrl);
		                	//Log.d("qiniu", arg2.toString());
		                	 Log.i("qiniu", key + ",\r\n " + info + ",\r\n " + res);
		                	 try {
								Log.i("qiniu_json",res.getString("key"));
		        
							} catch (JSONException e) {
								// TODO Auto-generated catch block
								e.printStackTrace();
							}
						}
		                }, null);	
			return 0; //文件上传成功
		}
	}
	
	
	public String createUrl(String baseUrl,String cloudFileName){
		return "http://"+baseUrl+"/"+cloudFileName;
	}
	
	
	
	public String uploadPic(final Handler handler,final String picLocalUrl) {
		// Get Token
		
	//	compressPic(picLocalUrl);
		
		new Thread(new Runnable() {
			
			@Override
			public void run() {
				// TODO Auto-generated method stub	
				
				timeinfo=System.currentTimeMillis(); //获取时间戳
				String picType=picLocalUrl.split("\\.")[1];		 //获取图片类型	
				
				cloudFileName=String.valueOf(timeinfo)+"."+picType;
				int stateNum=upLoadQiNiu(picLocalUrl,cloudFileName); //上传文件到云端 并设置好云端文件名
				if(stateNum==0){
					Message message=new Message();
					cloudPicUrl=createUrl("7xl54r.dl1.z0.glb.clouddn.com",cloudFileName);  //构建完整URL
					message.obj=cloudPicUrl;
					Log.i("url2",cloudPicUrl);
					handler.sendMessage(message);
				}else if(stateNum==3){
					Message message=new Message();
					message.obj="文件过大";
					handler.sendMessage(message);
				}
			}
		}).start();
		return picLocalUrl;
	}

//	public void huang(final Handler handler) {
//
//		new Thread(new Runnable() {
//			@Override
//			public void run() {
//				// TODO Auto-generated method stub
//
//				Message message = new Message();
//				int stateNum=upLoadQiNiu(picLocalUrl);  //调用上传文件函数上传文件
//				Log.i("State",String.valueOf(stateNum));
////				message.obj = token;
//				handler.sendMessage(message);
//			}
//		}).start();
//
//	}

	public static void compressBitmapToFile(Bitmap bmp, File file,int ratio1){
	    // 尺寸压缩倍数,值越大，图片尺寸越小
	    int ratio = ratio1;
	    // 压缩Bitmap到对应尺寸
	    Bitmap result = Bitmap.createBitmap(bmp.getWidth() / ratio, bmp.getHeight() / ratio, Config.ARGB_8888);
	    Canvas canvas = new Canvas(result);
	    Rect rect = new Rect(0, 0, bmp.getWidth() / ratio, bmp.getHeight() / ratio);
	    canvas.drawBitmap(bmp, null, rect, null);

	    ByteArrayOutputStream baos = new ByteArrayOutputStream();
	    // 把压缩后的数据存放到baos中
	    result.compress(Bitmap.CompressFormat.JPEG, 100 ,baos);
	    try {  
	        FileOutputStream fos = new FileOutputStream(file);  
	        fos.write(baos.toByteArray());  
	        fos.flush();  
	        fos.close();  
	    } catch (Exception e) {  
	        e.printStackTrace();  
	    } 
	}
}
