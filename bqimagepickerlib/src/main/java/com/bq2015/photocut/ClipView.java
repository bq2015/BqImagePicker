package com.bq2015.photocut;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.view.View;

/************************************************************
 * Author:  fangqq
 * Description:  裁剪高框类
 * Date:2016/5/4
 ************************************************************/
public class ClipView extends View {
	
	/**
	 * 边框距左右边界距离，用于调整边框长度
	 */
	public static final int BORDERDISTANCE = 10;
	
	private Paint mPaint;
	
	public ClipView(Context context) {
		this(context, null);
	}

	public ClipView(Context context, AttributeSet attrs) {
		this(context, attrs, 0);
	}

	public ClipView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		mPaint = new Paint();
	}

	@Override
	protected void onDraw(Canvas canvas) {
		super.onDraw(canvas);
		int width = this.getWidth();
		int height = this.getHeight();

		// 边框长度，据屏幕左右边缘50px
		int borderWidth = width - BORDERDISTANCE *2;
		int borderHeigth = (int) (borderWidth * 0.524);

		mPaint.setColor(0xaa000000);

		// 以下绘制透明暗色区域
		// top
		canvas.drawRect(0, 0, width, (height - borderHeigth) / 2, mPaint);
		// bottom
		canvas.drawRect(0, (height + borderHeigth) / 2, width, height, mPaint);
		// left
		canvas.drawRect(0, (height - borderWidth) / 2, BORDERDISTANCE,
				(height + borderWidth) / 2, mPaint);
		// right
		canvas.drawRect(borderWidth + BORDERDISTANCE, (height - borderHeigth) / 2, width,
				(height + borderWidth) / 2, mPaint);
		
		// 以下绘制边框线
		mPaint.setColor(Color.WHITE);
		mPaint.setStrokeWidth(2.0f);
		// top
		canvas.drawLine(BORDERDISTANCE, (height - borderHeigth) / 2, width - BORDERDISTANCE, (height - borderHeigth) / 2, mPaint);
		// bottom
		canvas.drawLine(BORDERDISTANCE, (height + borderHeigth) / 2, width - BORDERDISTANCE, (height + borderHeigth) / 2, mPaint);
		// left
		canvas.drawLine(BORDERDISTANCE, (height - borderHeigth) / 2, BORDERDISTANCE, (height + borderHeigth) / 2, mPaint);
		// right
		canvas.drawLine(width - BORDERDISTANCE, (height - borderHeigth) / 2, width - BORDERDISTANCE, (height + borderHeigth) / 2, mPaint);
	}

}
