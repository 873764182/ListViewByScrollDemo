package com.pixel.listview.inter;

import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;

/**
 * Created by Administrator on 2016/10/13.
 * <p>
 * 创建View
 */

public interface OnCreateViewInterface {
    // 列表长度
    int getCount();

    // 列表的视图
    View getView(LayoutInflater inflater, LinearLayout parentView, int position);
}
