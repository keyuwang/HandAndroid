package com.example.handandroid;

import static org.bytedeco.javacpp.opencv_core.*;

import static org.bytedeco.javacpp.opencv_imgproc.*;

import org.bytedeco.javacpp.*;

import org.bytedeco.javacpp.opencv_core.*;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.hardware.Camera;
import android.view.View;

public class CVPreview extends View implements Camera.PreviewCallback{

    private int TIME,COUNTER;
    private int wid,hei,len;
    private Mat inImg;
    private Mat gray;
    private Mat outImg;
    private Mat skinSample;
    private Rect roi;
    private byte[] rawData;
    private Bitmap bmp;
    
    private Hand hand;

	public CVPreview(Context context,int w,int h){
		super(context);
		

		Loader.load(opencv_core.class);
    
        rawData=new byte[w*h*3/2];
        BytePointer p= new BytePointer(rawData);
        inImg=new Mat(h,w,CV_8UC3);
        outImg=new Mat(h,w,CV_8UC4);
        bmp = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888);
        
        hand=new Hand(h,w);
        TIME=0;
        COUNTER=0;
        
        skinSample=new Mat(400,400,CV_8UC3);
        roi=new Rect(160,160,20,20);
        roi.x(180);
        roi.y(180);
        roi.width(20);
        roi.height(20);
	}

	@Override
	public void onPreviewFrame(byte[] data, Camera camera) {
		COUNTER++;
		// TODO Auto-generated method stub
        try {
            Camera.Size size = camera.getParameters().getPreviewSize();
            wid=size.width;
            hei=size.height;
            len=data.length;
//            System.arraycopy(data, 0, rawData, 0,len);
                     
            BytePointer t= new BytePointer(data);
            Mat raws=new Mat(hei+hei/2,wid,CV_8UC1,t);
            cvtColor(raws,inImg,CV_YUV2BGR_NV21);
            
            if(COUNTER==10)
            {
            	COUNTER=0;
            	TIME++;
         //   	MainActivity.saveFileWithNameExt(data,".nv21");
            }
            
            hand.update(inImg);
            
            raws.release();
            Mat display=hand.getResult();
            if(TIME>=5&&TIME<10)
            {
            	Mat rects = new Mat(display,roi);
            }
            
            rectangle(display,roi,new Scalar(255,255,0,0),-1,8,0);
            bmp.copyPixelsFromBuffer(display.getByteBuffer());
            
            postInvalidate();
            camera.addCallbackBuffer(data);
        } catch (RuntimeException e) {
        	
        }
            // The camera has probably just been released, ignore.
	}
	
	@Override
	protected void onDraw(Canvas canvas) {
        Paint paint = new Paint();
        paint.setColor(Color.RED);
        paint.setTextSize(20);

        String s = "TIME="+TIME;
        float textWidth = paint.measureText(s);
        
        canvas.scale(1.875f, 1.5f);
        canvas.drawBitmap(bmp,0,0,paint); 
        canvas.drawText(s, 50, 50, paint);
        
        if(TIME<=10)
        {
            paint.setColor(Color.GREEN);
            paint.setStyle(Paint.Style.STROKE);
            canvas.drawRect(160, 160, 180, 180, paint);
        }

    }
}
