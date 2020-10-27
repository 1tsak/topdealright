package com.panaceasoft.psstore;


import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import androidx.viewpager.widget.PagerAdapter;

import com.bumptech.glide.Glide;

import java.util.List;

public class BannerAdapter extends PagerAdapter {
    Context context;
    List<ModelBanner> images;
    LayoutInflater layoutInflater;


    public BannerAdapter(Context context, List<ModelBanner> images) {
        this.context = context;
        this.images = images;
        layoutInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }

    @Override
    public int getCount() {
        return images.size();
    }

    @Override
    public boolean isViewFromObject(View view, Object object) {
        return view == ((RelativeLayout) object);
    }

    @Override
    public Object instantiateItem(ViewGroup container, final int position) {
        View itemView = layoutInflater.inflate(R.layout.item_banner, container, false);

        ImageView imageView = (ImageView) itemView.findViewById(R.id.imgview_bnr);
        Glide.with(context)
                .load("http://topdealright.xyz/v2/images/"+images.get(position).getBanner_image())
                .into(imageView);

        container.addView(itemView);

        //listening to image click


        return itemView;
    }

    @Override
    public void destroyItem(ViewGroup container, int position, Object object) {
        container.removeView((RelativeLayout) object);
    }
}