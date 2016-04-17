package com.lianjia.android.customgarlley;

import android.content.Context;
import android.os.Handler;
import android.os.Message;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Scroller;

import java.lang.ref.WeakReference;
import java.util.LinkedList;
import java.util.Queue;

/**
 * @author GanQuan 轮播图
 */
public class CustomGarlley extends ViewGroup {
	private final String TAG = CustomGarlley.class.getSimpleName();
	private BaseAdapter mGalleryAdapter = null;
	// onlayout是否起作用 控件的大小
	private int mWidthMeasureSpec = 0;

	private int mheightMeasureSpec = 0;

	// down事件位置
	private float mDownX = 0;

	private float mDownY = 0;

	// private float mDownY = 0;
	// 最后的move位置
	private float mLastMoveX = 0;

	private float mLastMoveY = 0;

	// 保存位置变化差值
	private float mDisX = 0;

	// private float mDisY = 0;
	// 当前child0显示的数据
	private int mCurrentPosition;

	// 自动滚动使用
	private FlingRunnable mFlingRunnable = new FlingRunnable();

	// 滚动用事件定义
	private static final int AUTO_SCROLL = 100;

	// 滚动事件延时，默认3秒
	private long mDelayTile = 7000;

	// 滚动用handler
	private Handler mAutoScrollHandler = new GarlleyHandler(this);

	static class GarlleyHandler extends Handler {
		private WeakReference<CustomGarlley> mGarlley;

		public GarlleyHandler(CustomGarlley garlley) {
			mGarlley = new WeakReference<>(garlley);
		}

		@Override
		public void handleMessage(Message msg) {
			if (mGarlley.get() == null)
				return;
			CustomGarlley garlley = mGarlley.get();
			if (msg.what == AUTO_SCROLL) {
				garlley.performScrollNext();
				garlley.autoScrollNext();
			}
		}

	}

	/**
	 * 通知回调
	 */
	private NotifyCallBack mNotifyCallBack = null;

	/**
	 * 回收队列
	 */
	private Queue<View> mRecycleQueue = new LinkedList<View>();

	/**
	 * 当前滑动状态
	 */
	private static final int TOUCH_STATE_REST = 0;
	private static final int TOUCH_STATE_SCROLL = 1;
	private int mTouchState = TOUCH_STATE_REST;

	private boolean mCanOver = false;// 是否循环滚动

	public CustomGarlley(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
	}

	public CustomGarlley(Context context, AttributeSet attrs) {
		super(context, attrs);
	}

	public CustomGarlley(Context context) {
		super(context);
	}

	@Override
	protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
		mWidthMeasureSpec = widthMeasureSpec;
		mheightMeasureSpec = heightMeasureSpec;
		super.onMeasure(widthMeasureSpec, heightMeasureSpec);
	}

	@Override
	protected void onLayout(boolean changed, int l, int t, int r, int b) {
		if (getChildCount() == 0) {
			mRecycleQueue.clear();

			if (mGalleryAdapter != null && mGalleryAdapter.getCount() > 0) {
				View convertview = mGalleryAdapter.getView(checkPosition(getCurrentPosition()), null, null);

				if (convertview != null) {
					addView(convertview);

					convertview.measure(convertWToChild(mWidthMeasureSpec), convertHToChild(mheightMeasureSpec));

					convertview.layout(getPaddingLeft(), getPaddingTop(), MeasureSpec.getSize(mWidthMeasureSpec)
							- getPaddingRight(), MeasureSpec.getSize(mheightMeasureSpec) - getPaddingTop());
				}

				if (mNotifyCallBack != null) {
					mNotifyCallBack.onSelectedItem(checkPosition(getCurrentPosition()));
				}
			}
		}
	}

	/**
	 * 设置是否支持循环滑动
	 *
	 * @param flag
	 *            true不支持循环滑动 false支持
	 */
	public void setCanOver(boolean flag) {
		mCanOver = flag;
	}

	/**
	 * 设置回调
	 *
	 * @param cb
	 */
	public void setNotifyCallBack(NotifyCallBack cb) {
		mNotifyCallBack = cb;
	}

	/**
	 * 设置adapter
	 *
	 * @param ad
	 */
	public void setAdapter(BaseAdapter ad) {
		mGalleryAdapter = ad;
		mCurrentPosition = 0;

		removeAllViews();

		requestLayout();

	}

	public void setStateBar(final GalleryBar stateBar) {
		this.setNotifyCallBack(new NotifyCallBack() {
			@Override
			public void onSelectedItem(int position) {
				if (mGalleryAdapter != null && position < mGalleryAdapter.getCount()) {
					if (null != stateBar) {
						stateBar.setPosition(position);
					}
				}
			}
		});
		this.autoScrollNext();
		stateBar.setCount(mGalleryAdapter.getCount());
		stateBar.setPosition(0);
	}

	/**
	 * 更新数据
	 */
	public void updateData() {
		mCurrentPosition = 0;

		removeAllViews();

		requestLayout();
	}

	@Override
	public boolean onInterceptTouchEvent(MotionEvent ev) {
		// 禁止HorViewGroup的父View截获OnTouch事件
		// getParent().requestDisallowInterceptTouchEvent(true);
		if ((ev.getAction() == MotionEvent.ACTION_MOVE) && (mTouchState == TOUCH_STATE_SCROLL)) {
			return true;
		}

		switch (ev.getAction()) {
		case MotionEvent.ACTION_DOWN:
			Log.e(TAG, "HorViewGroup onInterceptTouchEvent ACTION_DOWN:");

			mDownX = ev.getX();
			mDownY = ev.getY();
			mLastMoveX = 0;
			mLastMoveY = 0;
			// getParent().requestDisallowInterceptTouchEvent(true);
			Log.e(TAG, "阻止父View截获事件");
			stopScrollNext();
		case MotionEvent.ACTION_MOVE:
			Log.e(TAG, "HorViewGroup onInterceptTouchEvent ACTION_MOVE:");

			if (ev.getY() - mDownY > 5 || ev.getY() - mDownY < -5) {
				getParent().requestDisallowInterceptTouchEvent(false);
				Log.e(TAG, "允许父View截获事件");
			}
			if (ev.getX() - mDownX > 10 || ev.getX() - mDownX < -10) {
				mTouchState = TOUCH_STATE_SCROLL;
				mFlingRunnable.stop();
				requestDisallowInterceptTouchEvent(true);
			}
			break;
		case MotionEvent.ACTION_UP:
			Log.e(TAG, "HorViewGroup onInterceptTouchEvent ACTION_UP:");
		case MotionEvent.ACTION_CANCEL:
			Log.e(TAG, "HorViewGroup onInterceptTouchEvent ACTION_CANCEL:");
			autoScrollNext();
			break;
		}
		return mTouchState == TOUCH_STATE_SCROLL;
	}

	@Override
	public boolean onTouchEvent(MotionEvent event) {
		switch (event.getAction()) {
		case MotionEvent.ACTION_DOWN:

			Log.e(TAG, "HorViewGroup onTouch ACTION_DOWN:");

			break;
		case MotionEvent.ACTION_MOVE:
			Log.e(TAG, "HorViewGroup onTouch ACTION_MOVE:");

			if (0 == mLastMoveX || 0 == mLastMoveY) {
				mLastMoveX = event.getX();
				mLastMoveY = event.getY();
				break;
			}

			mDisX = event.getX() - mLastMoveX;

			mLastMoveX = event.getX();
			mLastMoveY = event.getY();

			if (mCanOver && mGalleryAdapter != null) {
				if ((mCurrentPosition == 0 && mDisX > 0)
						|| (mCurrentPosition + getChildCount() == mGalleryAdapter.getCount() && mDisX < 0)) {
					return true;
				}
			}
			trackMotionScroll(mDisX);

			break;
		case MotionEvent.ACTION_UP:
			Log.e(TAG, "HorViewGroup onTouch ACTION_UP:");

			scrollTo(event.getX() - mDownX);
			mTouchState = TOUCH_STATE_REST;
			autoScrollNext();
			break;
		case MotionEvent.ACTION_CANCEL:
			Log.e(TAG, "HorViewGroup onTouch ACTION_CANCEL:");
			scrollTo(event.getX() - mDownX);
			mTouchState = TOUCH_STATE_REST;
			autoScrollNext();
			break;
		}
		return true;
	}

	/**
	 * 设置滚动间隔 小于3秒无效设置
	 *
	 * @param del
	 */
	public void setAutoScrollNext(long del) {
		if (del < 3000) {
			return;
		}
		mDelayTile = del;
	}

	/**
	 * 自动滚动到下一个
	 */
	public void autoScrollNext() {
		// Log.e(TAG, "autoScrollNext");
		if (mAutoScrollHandler.hasMessages(AUTO_SCROLL)) {
			return;
		}
		mAutoScrollHandler.sendEmptyMessageDelayed(AUTO_SCROLL, mDelayTile);

	}

	private void performScrollNext() {
		if (getChildCount() < 1) {
			return;
		}

		if (mTouchState == TOUCH_STATE_SCROLL) {
			return;
		}

		float dis = -getChildAt(0).getWidth();

		if (mNotifyCallBack != null) {
			for (int i = 0; i < getChildCount(); i++) {
				// 寻找画面内view
				if (getChildAt(i).getLeft() >= 0) {
					mNotifyCallBack.onSelectedItem(checkPosition(checkPosition(getCurrentPosition() + i) + 1));
					break;
				}
			}
			mFlingRunnable.startUsingDistance((int) dis);
		}
	}

	/**
	 * 停止滚动到下一个
	 */
	public void stopScrollNext() {
		Log.e(TAG, "stopScrollNext");
		mAutoScrollHandler.removeMessages(AUTO_SCROLL);
	}

	/**
	 * 用户滚动
	 *
	 * @param dx
	 */
	private void scrollTo(float dx) {
		if (getChildCount() <= 0) {
			return;
		}
		float dis = 0;
		if (dx > 0) {
			dis = getPaddingLeft() - getChildAt(0).getLeft();
		} else if (dx < 0) {
			dis = getPaddingLeft() - getChildAt(getChildCount() - 1).getLeft();
		}

		// Log.e(TAG, "scrollTo dis:" + dis);

		if (mNotifyCallBack != null) {
			for (int i = 0; i < getChildCount(); i++) {
				int offset = getChildAt(i).getWidth() / 3;
				if ((getChildAt(i).getLeft() + dis) < getPaddingLeft() + offset
						&& (getChildAt(i).getLeft() + dis) > getPaddingLeft() - offset) {
					mNotifyCallBack.onSelectedItem(checkPosition(getCurrentPosition() + i));
				}
			}
		}

		mFlingRunnable.startUsingDistance((int) dis);
	}

	/**
	 * view 移动处理
	 *
	 * @param disx
	 */
	void trackMotionScroll(float disx) {
		if (disx == 0) {
			return;
		}

		onMoveChildViews(disx);

		recycleView(disx);

		makeAndAddView(disx);
	}

	/**
	 * 移动view
	 *
	 * @param disx
	 */
	private void onMoveChildViews(float disx) {
		for (int i = 0; i < getChildCount(); i++) {
			getChildAt(i).offsetLeftAndRight((int) disx);
		}
	}

	/**
	 * 删除回收view
	 *
	 * @param disx
	 */
	private void recycleView(float disx) {
		if (getChildCount() <= 0) {
			return;
		}
		View view = null;
		// right ->
		if (disx > 0) {
			view = getChildAt(getChildCount() - 1);
			if (view.getLeft() > getWidth() - getPaddingRight()) {
				removeViewInLayout(view);
				mRecycleQueue.offer(view);
				// Log.e(TAG, "recycleView right mRecycleQueue size " +
				// mRecycleQueue.size());
			}
		}
		// left <-
		else if (disx < 0) {
			view = getChildAt(0);
			if (view.getRight() < getPaddingLeft()) {
				removeViewInLayout(view);
				changeCurrentPosition(1);
				mRecycleQueue.offer(view);
				// Log.e(TAG, "recycleView left mRecycleQueue size " +
				// mRecycleQueue.size());
			}
		}
	}

	/**
	 * 添加view
	 *
	 * @param disx
	 */
	private void makeAndAddView(float disx) {

		if (getChildCount() <= 0) {
			return;
		}

		View view = null;

		// left <-
		if (disx < 0) {
			view = getChildAt(getChildCount() - 1);

			if (view.getRight() < getWidth() - getPaddingRight()) {
				// Log.e("dragon", "start right add " + mRecycleQueue.size());

				int position = (getCurrentPosition() + getChildCount()) % mGalleryAdapter.getCount();

				View convertview = mGalleryAdapter.getView(position, mRecycleQueue.poll(), null);

				// Log.e(TAG, "start right add ----" + mRecycleQueue.size());

				if (convertview != null) {
					addViewInLayout(convertview, -1, new LayoutParams(LayoutParams.WRAP_CONTENT,
							LayoutParams.WRAP_CONTENT));

					convertview.measure(convertWToChild(mWidthMeasureSpec), convertHToChild(mheightMeasureSpec));

					convertview.layout(view.getLeft() + MeasureSpec.getSize(convertWToChild(mWidthMeasureSpec)),
							view.getTop(), view.getRight() + MeasureSpec.getSize(convertWToChild(mWidthMeasureSpec)),
							view.getBottom());
				}
			}
		}
		// right ->
		else if (disx > 0) {
			view = getChildAt(0);

			if (view.getLeft() > getPaddingLeft()) {
				// Log.e(TAG, "start left add " + mRecycleQueue.size());

				changeCurrentPosition(-1);

				View convertview = mGalleryAdapter.getView(getCurrentPosition(), mRecycleQueue.poll(), null);

				// Log.e(TAG, "start left add ------" + mRecycleQueue.size());
				if (convertview != null) {
					addViewInLayout(convertview, 0, new LayoutParams(LayoutParams.WRAP_CONTENT,
							LayoutParams.WRAP_CONTENT));

					convertview.measure(convertWToChild(mWidthMeasureSpec), convertHToChild(mheightMeasureSpec));

					convertview.layout(view.getLeft() - MeasureSpec.getSize(convertWToChild(mWidthMeasureSpec)),
							view.getTop(), view.getRight() - MeasureSpec.getSize(convertWToChild(mWidthMeasureSpec)),
							view.getBottom());
				}
			}
		}

		invalidate();
	}

	/**
	 * 移动position
	 *
	 * @param value
	 *            true 为自增，false 为自减
	 *            <p/>
	 *            mCurrentPosition记录的是0号child取得数据
	 */
	private void changeCurrentPosition(int value) {
		mCurrentPosition = checkPosition(mCurrentPosition + value);
	}

	/**
	 * check position 是否越界
	 *
	 * @param value
	 * @return
	 */
	private int checkPosition(int value) {
		int ret = 0;
		if (mGalleryAdapter != null && mGalleryAdapter.getCount() > 0) {
			ret = value;

			if (value < 0) {
				ret = mGalleryAdapter.getCount() - 1;
			}

			if (value >= mGalleryAdapter.getCount()) {
				ret = 0;
			}
		}

		return ret;
	}

	/**
	 * 返回第一个子view的数据位置
	 *
	 * @return
	 */
	private int getCurrentPosition() {
		return mCurrentPosition;
	}

	/**
	 * 自动滚动使用
	 *
	 * @author gq
	 */
	private class FlingRunnable implements Runnable {
		private Scroller mScroller;

		private int mLastFlingX;

		public FlingRunnable() {
			mScroller = new Scroller(getContext());
		}

		private void startCommon() {
			removeCallbacks(this);
		}

		public void startUsingDistance(int distance) {
			if (distance == 0)
				return;
			if (!mScroller.isFinished()) {
				return;
			}
			startCommon();

			mScroller.startScroll(0, 0, distance, 0, 500);

			mLastFlingX = 0;

			if (mAutoScrollHandler != null) {
				mAutoScrollHandler.post(this);
			}
		}

		public void stop() {
			// removeCallbacks(this);
			mScroller.forceFinished(true);
		}

		public void run() {
			boolean more = mScroller.computeScrollOffset();
			int currentx = mScroller.getCurrX();

			float dx = currentx - mLastFlingX;

			trackMotionScroll(dx);

			if (more) {
				mLastFlingX = currentx;

				if (mAutoScrollHandler != null) {
					mAutoScrollHandler.post(this);
				}
			} else {
				stop();
			}
		}
	}

	/**
	 * 将父控件大小转换成子空间大小
	 */
	private int convertWToChild(int spec) {

		int modeW = MeasureSpec.getMode(mWidthMeasureSpec);

		int w = MeasureSpec.getSize(mWidthMeasureSpec) - getPaddingLeft() - getPaddingRight();

		return MeasureSpec.makeMeasureSpec(w, modeW);
	}

	/**
	 * 将父控件大小转换成子空间大小
	 */
	private int convertHToChild(int spec) {
		int modeH = MeasureSpec.getMode(mheightMeasureSpec);

		int h = MeasureSpec.getSize(mheightMeasureSpec) - getPaddingTop() - getPaddingBottom();

		return MeasureSpec.makeMeasureSpec(h, modeH);
	}

	/**
	 * 定义通知回调
	 */
	public interface NotifyCallBack {
		void onSelectedItem(int position);
	}

	public void release() {
		if (mAutoScrollHandler != null) {
			mAutoScrollHandler.removeCallbacks(mFlingRunnable);
			mAutoScrollHandler.removeMessages(AUTO_SCROLL);
			mFlingRunnable = null;
			mAutoScrollHandler = null;
		}
	}

	@Override
	protected void onAttachedToWindow() {
		super.onAttachedToWindow();
	}

	@Override
	protected void onDetachedFromWindow() {
		release();
		super.onDetachedFromWindow();
	}
}
