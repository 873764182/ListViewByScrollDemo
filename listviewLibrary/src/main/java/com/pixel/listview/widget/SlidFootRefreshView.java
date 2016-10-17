package com.pixel.listview.widget;

import android.content.Context;
import android.graphics.Color;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.LinearInterpolator;
import android.view.animation.RotateAnimation;
import android.widget.LinearLayout;
import android.widget.TextView;

/**
 * Created by Administrator on 2016/10/17.
 * <p>
 * 默认的尾部控件
 */

public class SlidFootRefreshView extends ISlidFootRefreshView {
    private LinearLayout moreView = null;
    private TextView textViewArrow = null;
    private TextView textView = null;

    private RotateAnimation animation = null;

    public SlidFootRefreshView() {
        animation = new RotateAnimation(0f, 360f, Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f);
        animation.setInterpolator(new LinearInterpolator());
        animation.setRepeatCount(-1);
        animation.setDuration(1000);
    }

    @Override
    public View getMoreView(Context context, int width, int height) {
        if (moreView == null) {
            moreView = new LinearLayout(context);
            moreView.setLayoutParams(new LinearLayout.LayoutParams(width, height / 8));
            moreView.setOrientation(LinearLayout.HORIZONTAL);
            moreView.setGravity(Gravity.CENTER | Gravity.BOTTOM);
            moreView.setBackgroundColor(Color.argb(200, 255, 255, 255));

            LinearLayout linearLayout = new LinearLayout(context);
            linearLayout.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, height / 16));
            linearLayout.setOrientation(LinearLayout.HORIZONTAL);
            linearLayout.setGravity(Gravity.CENTER);

            textViewArrow = new TextView(context);
            textViewArrow.setGravity(Gravity.CENTER);
            textViewArrow.setText("▲");

            textView = new TextView(context);
            textView.setPadding(4, 4, 4, 4);
            textView.setText("上拉加载");

            linearLayout.addView(textViewArrow);
            linearLayout.addView(textView);
            moreView.addView(linearLayout);
        }
        return moreView;
    }

    @Override
    public void onSliding(int scope, int sliding) {
        if (sliding > scope * 2 / 3) {  // 滑动超过总范围的2/3时松手就会触发刷新操作
            textView.setText("松手加载");
            textViewArrow.setText("▼");
        } else {
            textView.setText("上拉加载");
            textViewArrow.setText("▲");
        }
    }

    @Override
    public void performMoreView() {
        textViewArrow.startAnimation(animation);
        textView.setText("正在加载 ...");
    }

    @Override
    public void closeMoreView() {
        textViewArrow.clearAnimation();
        textView.setText("上拉加载");
    }
}
