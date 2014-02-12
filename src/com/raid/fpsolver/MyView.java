package com.raid.fpsolver;

import java.io.InputStream;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceHolder.Callback;
import android.view.SurfaceView;

public class MyView extends SurfaceView implements Callback {

	SurfaceHolder holder;
	Canvas canvas;
	Paint paint1, paint2, paint3, paint4, paint5;
	int ScreenW, ScreenH;
	int size_w, size_h,start_w;
	String fileName ;
	Fp fp;
	boolean running;
	final String TAG = "tag";

	public MyView(Context context, String str) {
		super(context);
		
		holder = this.getHolder();
		holder.addCallback(this);
		canvas = new Canvas();
		
		fileName = str;
		if(fileName.equals("") || fileName == null)
			return;

		paint1 = new Paint();
		paint1.setStyle(Paint.Style.STROKE);
		paint1.setStrokeWidth(0.5f);
		paint1.setAntiAlias(true);
		paint1.setColor(Color.DKGRAY);

		paint2 = new Paint();
		paint2.setColor(Color.BLACK);
		paint2.setAntiAlias(true);

		paint3 = new Paint();
		paint3.setColor(Color.LTGRAY);
		paint3.setAntiAlias(true);
		//paint3.setAlpha(125);

		paint4 = new Paint();
		paint4.setColor(Color.WHITE);
		paint4.setAntiAlias(true);
		
		paint5 = new Paint();
		paint5.setColor(Color.DKGRAY);
		paint5.setAntiAlias(true);

		byte[] buffer = null;
		
		
		
		try {
			InputStream in = getResources().getAssets().open(fileName+".txt");
			int length = in.available();
			buffer = new byte[length + 100];
			in.read(buffer);
			in.close();
		} catch (Exception e) {
			Log.i(TAG, "file error");
			e.printStackTrace();
		}

		fp = new Fp(buffer);
	}

	@Override
	public void surfaceChanged(SurfaceHolder arg0, int arg1, int arg2, int arg3) {

	}

	@Override
	public void surfaceCreated(SurfaceHolder arg0) {
		if(fileName.equals("") || fileName == null) {
			
			canvas = holder.lockCanvas();
			canvas.drawColor(Color.WHITE);
			holder.unlockCanvasAndPost(canvas);
			return;
		}
		
		ScreenW = this.getWidth();
		ScreenH = this.getHeight();
		running = true;
		
		new Thread(new DrawThread()).start();
		new Thread(new SolveThread()).start();
	}

	@Override
	public void surfaceDestroyed(SurfaceHolder arg0) {
		// TODO Auto-generated method stub

	}
	
	private class DrawThread implements Runnable {
		
		@Override
		public void run() {
			while(running) {
				try { 
					//Thread.sleep(100);
					canvas = holder.lockCanvas();
					canvas = fp.draw(canvas, ScreenW, ScreenH, paint1, paint2, paint3, paint4, paint5);
				}catch (Exception e) {} 
				finally { 
					if(canvas != null)
						holder.unlockCanvasAndPost(canvas);
				} 
			}
			Log.i(TAG, "stop drawing");
		}
	}
	
	private class SolveThread implements Runnable {
		
		@Override 
		public void run() {
			fp.solve();
			try{
			Thread.sleep(100);
			}catch(Exception e) {}
			running = false;
			Log.i(TAG, "stop solving");
		}
	}
	
}