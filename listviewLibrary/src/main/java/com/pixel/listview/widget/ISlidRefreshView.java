package com.pixel.listview.widget;

import android.view.View;

/**
 * Created by Administrator on 2016/10/15.
 * <p>
 * 下拉刷新的头部
 */

public abstract class ISlidRefreshView {
    private View mRefreshView;

    public ISlidRefreshView() {
    }

    // 下拉刷新的滑动的时候
    public void onSliding(int scope, int sliding) {

    }

    public abstract View getRefreshView();

}
