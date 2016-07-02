package com.bq2015.photocut;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.util.AttributeSet;
import android.view.View;

/************************************************************
 * Author:  fangqq
 * Description:
 * Date:2016/5/6
 ************************************************************/
public class ClipCicleView extends View {
    /**
     * 边框距左右边界距离，用于调整边框长度
     */
    public static final int BORDERDISTANCE = 50;

    private Paint mPaint;
    public ClipCicleView(Context context, AttributeSet attrs) {
        super(context, attrs);
        mPaint = new Paint();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        int width = this.getWidth();
        int height = this.getHeight();

        int borderlength = width - BORDERDISTANCE *2;
        int r = borderlength/2;

        mPaint.setColor(0xaa000000);
        Bitmap bitmap = Bitmap.createBitmap(width,height, Bitmap.Config.ARGB_8888);
        Canvas canvas1 = new Canvas(bitmap);
        mPaint.setStyle(Paint.Style.FILL);
        canvas1.drawRect(0,0,width,height,mPaint);
        mPaint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.CLEAR));
        canvas1.drawCircle(width/2,height/2,r,mPaint);
        canvas.drawBitmap(bitmap,0,0,new Paint());
        mPaint.setXfermode(null);

        mPaint.setColor(Color.WHITE);
        mPaint.setStyle(Paint.Style.STROKE);
        mPaint.setStrokeWidth(2.0f);

        canvas.drawCircle(width/2,height/2,r-2,mPaint);

    }
}
