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

import com.pixel.listview.LinearListView;

/**
 * Created by Administrator on 2016/10/17.
 * <p>
 * 默认的尾部控件
 */

public class HSlidFootRefreshView extends ISlidFootRefreshView {
    private LinearLayout moreView = null;
    private TextView textViewArrow = null;
    private TextView textView = null;

    private RotateAnimation animation = null;

    public HSlidFootRefreshView(LinearListView linearListView) {
        super(linearListView);
        animation = new RotateAnimation(0f, 360f, Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f);
        animation.setInterpolator(new LinearInterpolator());
        animation.setRepeatCount(-1);
        animation.setDuration(1000);
    }

    @Override
    public View getMoreView(Context context, int width, int height) {
        if (moreView == null) {
            moreView = new LinearLayout(context);
            moreView.setLayoutParams(new LinearLayout.LayoutParams(width / 4, height));
            moreView.setOrientation(LinearLayout.HORIZONTAL);
            moreView.setGravity(Gravity.CENTER_VERTICAL | Gravity.RIGHT);
            moreView.setBackgroundColor(Color.argb(200, 255, 255, 255));

            LinearLayout linearLayout = new LinearLayout(context);
            linearLayout.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT));
            linearLayout.setOrientation(LinearLayout.HORIZONTAL);
            linearLayout.setGravity(Gravity.CENTER_VERTICAL | Gravity.RIGHT);

            textViewArrow = new TextView(context);
            textViewArrow.setGravity(Gravity.CENTER);
            textViewArrow.setPadding(16, 16, 16, 16);
            textViewArrow.setText("➤"); // 要反过来

            textView = new TextView(context);
            textView.setText("左滑加载");

            linearLayout.addView(textView);
            linearLayout.addView(textViewArrow);
            moreView.addView(linearLayout);
        }
        return moreView;
    }

    @Override
    public void onSliding(int scope, int sliding) {
        if (sliding > scope * 2 / 3) {  // 滑动超过总范围的2/3时松手就会触发刷新操作
            textView.setText("松手加载");
            textViewArrow.setRotation(0);
        } else {
            textView.setText("左滑加载");
            textViewArrow.setRotation(180);
        }
    }

    @Override
    public void performMoreView() {
        textViewArrow.startAnimation(animation);
        textView.setText("正在加载");
    }

    @Override
    public void closeMoreView() {
        textViewArrow.clearAnimation();
        textView.setText("左滑加载");
    }
}
