package com.lianjia.android.sample;

import android.content.Context;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;

import com.lianjia.android.customgarlley.BaseGalleyAdapter;

import java.util.List;

import butterknife.Bind;

/**
 * Created by GanQuan on 16/4/17.
 */
public class GalleryAdapter extends BaseGalleyAdapter<ImgBean> {

	public GalleryAdapter(Context context) {
		super(context);
	}

	@Override
	protected void onBindViewHolder(List<ViewBundle> list) {
		list.add(new ViewBundle(R.layout.layout_img, ViewHolder.class));
	}

	static class ViewHolder extends BaseViewHolder<ImgBean> {
		@Bind(R.id.pic)
		ImageView img;

		@Override
		protected void setView(final int position, ImgBean bean, final Context context) {
			img.setImageResource(bean.imgId);
			img.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View v) {
					Toast.makeText(context, "pisition:" + position, Toast.LENGTH_SHORT).show();
				}
			});
		}
	}
}
