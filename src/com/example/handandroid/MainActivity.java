package com.example.handandroid;


import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

import android.app.Activity;
import android.graphics.ImageFormat;
import android.hardware.Camera;
import android.hardware.Camera.PictureCallback;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.HandlerThread;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.FrameLayout;

public class MainActivity extends Activity {
	
	private String TAG = this.getClass().getName();
	
	private FrameLayout layout;
	private CameraPreview mPreview;
	private CVPreview cvPreview;
	private static Camera mCamera;
	private CameraHandlerThread mThread = null;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		
		 // Hide the window title.
        requestWindowFeature(Window.FEATURE_NO_TITLE);
		
		super.onCreate(savedInstanceState);
		
		getWindow().addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
		
		//mCamera=CameraPreview.getCameraInstance();
		//getCameraInstance();
		openCameraInThread();
		
		Camera.Parameters parameters = mCamera.getParameters();
		Camera.Size size = mCamera.getParameters().getPreviewSize();
		
		
        layout = new FrameLayout(this);
		
		cvPreview=new CVPreview(this,size.width,size.height);

        mCamera.setPreviewCallbackWithBuffer(cvPreview);
        
        
        byte[] data = new byte[size.width*size.height*
                               ImageFormat.getBitsPerPixel(parameters.getPreviewFormat())/8];
        mCamera.addCallbackBuffer(data);
		
		mPreview=new CameraPreview(this,mCamera);
		
		Button btn = new Button(this);
		btn.setText("capture");
		btn.setOnClickListener(
				   new View.OnClickListener() {
				        @Override
				        public void onClick(View v) {
				            // get an image from the camera
				            mCamera.takePicture(null, null, mPicture);
				            mCamera.stopPreview();
				            mCamera.startPreview();
				        }
				   }
				);
		
		layout.addView(mPreview);
		layout.addView(cvPreview);
//		layout.addView(btn);
        setContentView(layout);
        //setContentView(R.layout.activity_main);
        
        
		//Log.d("Test Tag", "Height= "+size.height+"width= "+size.width+ "byte= "+ImageFormat.getBitsPerPixel(parameters.getPreviewFormat()));
        
	}
	
	private PictureCallback mPicture = new PictureCallback() {

	    @Override
	    public void onPictureTaken(byte[] data, Camera camera) {

	    	saveFileWithNameExt(data,".jpg");
	    }
	};


	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle action bar item clicks here. The action bar will
		// automatically handle clicks on the Home/Up button, so long
		// as you specify a parent activity in AndroidManifest.xml.
		int id = item.getItemId();
		if (id == R.id.action_settings) {
			return true;
		}
		return super.onOptionsItemSelected(item);
	}
	
	static public void saveFileWithNameExt(byte[] paramArrayOfByte, String paramString)
	{
		File mediaStorageDir = new File(Environment.getExternalStoragePublicDirectory(
	              Environment.DIRECTORY_PICTURES), "MyCameraApp");
		if (! mediaStorageDir.exists()){
	        if (! mediaStorageDir.mkdirs()){
	            Log.d("MyCameraApp", "failed to create directory");
	            return;
	        }
	    }
		
		 // Create a media file name
	    String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
	    
	    File pictureFile = new File(mediaStorageDir.getPath() + File.separator + timeStamp + paramString);
		
	    try
	    {
	      FileOutputStream fos = new FileOutputStream(pictureFile);
	      fos.write(paramArrayOfByte);
	      fos.close();
	      Log.d("SavePictureFile", "path:" + pictureFile.getPath());

	      return;
	    }
	    catch (Exception localException)
	    {
	      for (;;)
	      {
	        Log.e("SavePictureFile", localException.getMessage());
	      }
	    }
	}
	
	private static void getCameraInstance()
	{
		try {
	        mCamera = Camera.open(); // attempt to get a Camera instance
	    }
	    catch (Exception e){
	        // Camera is not available (in use or does not exist)
	    	Log.e("getCameraInstance", "failed to open camera");
	    }
	}
	
	private void openCameraInThread() {
	    if (mThread == null) {
	        mThread = new CameraHandlerThread();
	    }

	    synchronized (mThread) {
	        mThread.openCamera();
	    }
	}
	
	private static class CameraHandlerThread extends HandlerThread{
		
		Handler mHandler = null;

		public CameraHandlerThread() {
			 super("CameraHandlerThread");
		     start();
		     mHandler = new Handler(getLooper());
			// TODO Auto-generated constructor stub
		}
		
		synchronized void notifyCameraOpened() {
	        notify();
	    }
		
		void openCamera()
		{
			mHandler.post(new Runnable(){
				@Override
				public void run() {
					getCameraInstance();
					notifyCameraOpened();
				}
			});
			
			try {
	            wait();
	        }
	        catch (InterruptedException e) {
	            Log.w("wait()", "wait was interrupted");
	        }
		}
		
	}
	
	
	
}
