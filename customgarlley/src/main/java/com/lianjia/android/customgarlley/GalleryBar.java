package com.lianjia.android.customgarlley;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.view.View;
import android.view.WindowManager;

public class GalleryBar extends View {

	public int mCount = 0;
	public int mPos = 0;
	Paint paint = new Paint();
	final int padding = GetPixelByDIP(getContext(), 1.5f);// dp
	final int radius = GetPixelByDIP(getContext(), 3);
	final int smallRadius = (int) (radius * 0.8f);

	public GalleryBar(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
	}

	public GalleryBar(Context context, AttributeSet attrs) {
		super(context, attrs);
	}

	public GalleryBar(Context context) {
		super(context);
	}

	private int GetPixelByDIP(Context context, float dp) {
		return (int) (context.getResources().getDisplayMetrics().density * dp + 0.5F);
	}

	@Override
	protected void onDraw(Canvas canvas) {
		super.onDraw(canvas);

		// 描画背景
		if (mCount != 0) {
			int w = getWidth();
			int h = getHeight();
			for (int i = mCount - 1; i >= 0; i--) {
				paint.setAntiAlias(true);
				paint.setColor(Color.parseColor("#ffffff"));
				if (i == mCount - mPos - 1) {
					canvas.drawCircle(w - (radius * 3 + padding) * i - radius * 2, h - radius * 3, radius, paint);
					paint.setColor(getContext().getResources().getColor(android.R.color.holo_red_light));
					canvas.drawCircle(w - (radius * 3 + padding) * i - radius * 2, h - radius * 3, smallRadius, paint);
				} else {
					canvas.drawCircle(w - (radius * 3 + padding) * i - radius * 2, h - radius * 3, radius, paint);
				}
			}
		}
	}

	@Override
	protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
		int height;
		int heghtMode = MeasureSpec.getMode(heightMeasureSpec);
		if (heghtMode == MeasureSpec.EXACTLY) {
			height = MeasureSpec.getSize(heightMeasureSpec);
		} else {
			height = radius * 3 * 2;
		}
		setMeasuredDimension(widthMeasureSpec, MeasureSpec.makeMeasureSpec(height, MeasureSpec.EXACTLY));

	}

	/**
	 * 设置总数
	 *
	 * @param count
	 */
	public void setCount(int count) {
		mCount = count;
		requestLayout();
	}

	/**
	 * 设置显示位置
	 *
	 * @param pos
	 */
	public void setPosition(int pos) {

		if (mCount == 0) {
			return;
		}
		mPos = pos % mCount;
		invalidate();
	}
}
