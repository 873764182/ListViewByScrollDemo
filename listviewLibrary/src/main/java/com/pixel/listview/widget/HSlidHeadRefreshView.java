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
 * 默认的头部控件
 */

public class HSlidHeadRefreshView extends ISlidHeadRefreshView {
    private LinearLayout refreshView = null;
    private TextView textViewArrow = null;
    private TextView textView = null;

    private RotateAnimation animation = null;

    public HSlidHeadRefreshView() {
        animation = new RotateAnimation(0f, 360f, Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f);
        animation.setInterpolator(new LinearInterpolator());
        animation.setRepeatCount(-1);
        animation.setDuration(1000);
    }

    @Override
    public View getRefreshView(Context context, int width, int height) {
        if (refreshView == null) {
            refreshView = new LinearLayout(context);
            refreshView.setLayoutParams(new LinearLayout.LayoutParams(width / 4, height));
            refreshView.setOrientation(LinearLayout.VERTICAL);
            refreshView.setGravity(Gravity.CENTER_VERTICAL | Gravity.LEFT);
            refreshView.setBackgroundColor(Color.argb(200, 255, 255, 255));

            LinearLayout linearLayout = new LinearLayout(context);
            linearLayout.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT));
            linearLayout.setOrientation(LinearLayout.HORIZONTAL);
            linearLayout.setGravity(Gravity.CENTER_VERTICAL | Gravity.LEFT);

            textViewArrow = new TextView(context);
            textViewArrow.setGravity(Gravity.CENTER);
            textViewArrow.setPadding(16, 16, 16, 16);
            textViewArrow.setText("➤");

            textView = new TextView(context);
            textView.setText("右滑刷新");

            linearLayout.addView(textViewArrow);
            linearLayout.addView(textView);
            refreshView.addView(linearLayout);
        }
        return refreshView;
    }

    @Override
    public void onSliding(int scope, int sliding) {
        if (sliding > scope * 2 / 3) {  // 滑动超过总范围的2/3时松手就会触发刷新操作
            textView.setText("松手刷新");
            textViewArrow.setRotation(180);
        } else {
            textView.setText("右滑刷新");
            textViewArrow.setRotation(0);
        }
    }

    @Override
    public void performRefreshView() {
        textViewArrow.startAnimation(animation);
        textView.setText("正在刷新");
    }

    @Override
    public void closeRefreshView() {
        textViewArrow.clearAnimation();
        textView.setText("右滑刷新");
    }
}
