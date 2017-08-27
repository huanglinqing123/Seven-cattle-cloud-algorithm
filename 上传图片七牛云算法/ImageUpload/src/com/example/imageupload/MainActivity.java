package com.example.imageupload;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;

import com.qiniu.common.QiniuException;

import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.provider.MediaStore;
import android.app.Activity;
import android.app.Notification.Action;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Base64;
import android.view.Menu;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

public class MainActivity extends Activity {

	private static int RESULT_LOAD_IMAGE = 1;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		Button buttonLoadImage = (Button) findViewById(R.id.button1);
		buttonLoadImage.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View arg0) {

				// 打开系统相册方法
				// Intent intent = new Intent(
				// Intent.ACTION_PICK,
				// android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
				// intent.setType("image/*"); // 这个参数是确定要选择的内容为图片，
				// Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
				// intent.addCategory(Intent.CATEGORY_OPENABLE);
				// intent.setType("image/*");
				// 打开系统相册方法3
				 Intent intent = new Intent(
				 Intent.ACTION_PICK,
				 android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
				 intent.setDataAndType(
				 MediaStore.Images.Media.EXTERNAL_CONTENT_URI, "image/*");
				 startActivityForResult(intent, RESULT_LOAD_IMAGE);
//				new QiNiuUpload().huang(handler3);
			}
		});
	}

	//
	private Handler handler3 = new Handler() {
		public void handleMessage(android.os.Message msg) {
			String json=(String) msg.obj;
			Toast.makeText(MainActivity.this, json, 0).show();
		};
	};

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);

		if (requestCode == RESULT_LOAD_IMAGE && resultCode == RESULT_OK
				&& null != data) {
			Uri selectedImage = data.getData();
			String[] filePathColumn = { MediaStore.Images.Media.DATA };

			Cursor cursor = getContentResolver().query(selectedImage,
					filePathColumn, null, null, null);
			cursor.moveToFirst();

			int columnIndex = cursor.getColumnIndex(filePathColumn[0]);
			String picturePath = cursor.getString(columnIndex);

			
			
			
			
			ImageView imageView = (ImageView) findViewById(R.id.imageView1);
			imageView.setImageBitmap(BitmapFactory.decodeFile(picturePath));
			Bitmap bitmap = BitmapFactory.decodeFile(picturePath);
			String n = bitaString(bitmap);
			ImageView imageView2 = (ImageView) findViewById(R.id.imageView2);
//			Bitmap bitmap2 = bitmaphuhu(n);

			String[] sourceStrArray = picturePath.split("/");
			for (int i = 0; i < sourceStrArray.length; i++) {
				System.out.println(sourceStrArray[i]);
			}
//			imageView2.setImageBitmap(bitmap2);
				new QiNiuUpload().uploadPic(handler,picturePath);
			
			// Toast.makeText(MainActivity.this, picturePath, 0).show();
		}

	}

	Handler handler = new Handler() {
		public void handleMessage(android.os.Message msg) {
			String n = (String) msg.obj;
			Toast.makeText(MainActivity.this, n, 0).show();
		};
	};

	public static Bitmap bitmaphuhu(String bits) {
		byte[] bytes = Base64.decode(bits, Base64.DEFAULT);
		Bitmap bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
		return bitmap;

	}

	public static String bitaString(Bitmap bitmap) {
		String result = "";
		ByteArrayOutputStream bos = null;
		if (bitmap != null) {
			bos = new ByteArrayOutputStream();
			bitmap.compress(Bitmap.CompressFormat.JPEG, 90, bos);
			try {
				bos.flush();
				bos.close();
				byte[] bit = bos.toByteArray();
				result = Base64.encodeToString(bit, Base64.DEFAULT);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

		}
		return result;

	}

}
