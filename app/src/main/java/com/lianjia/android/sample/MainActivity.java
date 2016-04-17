package com.lianjia.android.sample;

import android.content.Context;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.view.WindowManager;
import android.widget.RelativeLayout;

import com.lianjia.android.customgarlley.CustomGarlley;
import com.lianjia.android.customgarlley.GalleryBar;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

	private CustomGarlley mHorViewGroup;
	private GalleryBar mStateBar;
	private final int IMAGE_WIDTH = 1536;
	private final int IMAGE_HEIGHT = 768;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		mHorViewGroup = (CustomGarlley) findViewById(R.id.FocusGroup);
		mStateBar = (GalleryBar) findViewById(R.id.FocusGroupState);

		initHorViewGroup();

	}

	private void initHorViewGroup() {
		setLayout();
		setAdapter();
	}

	private void setLayout() {
		int height = getScreenWidthInPixel(this) * IMAGE_HEIGHT / IMAGE_WIDTH;
		RelativeLayout.LayoutParams lp = (RelativeLayout.LayoutParams) mHorViewGroup.getLayoutParams();
		lp.height = height;
		lp.width = RelativeLayout.LayoutParams.MATCH_PARENT;
		mHorViewGroup.setLayoutParams(lp);
		mHorViewGroup.setAutoScrollNext(2000);
	}

	private void setAdapter() {
		GalleryAdapter adapter = new GalleryAdapter(this);
		mHorViewGroup.setAdapter(adapter);
		adapter.initList(onCreateList());
		mHorViewGroup.setStateBar(mStateBar);//set state bar
	}

	private List<ImgBean> onCreateList() {
		List<ImgBean> list = new ArrayList<>();
		list.add(new ImgBean());
		list.add(new ImgBean());
		list.add(new ImgBean());
		return list;
	}

	private int getScreenWidthInPixel(Context context) {
		int width = 0;
		WindowManager wm = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
		DisplayMetrics displayMetrics = new DisplayMetrics();
		wm.getDefaultDisplay().getMetrics(displayMetrics);
		width = displayMetrics.widthPixels;
		return width;
	}

}
