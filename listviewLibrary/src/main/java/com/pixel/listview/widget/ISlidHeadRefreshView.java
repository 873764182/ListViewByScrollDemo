package com.pixel.listview.widget;

import android.view.View;

/**
 * Created by Administrator on 2016/10/15.
 * <p>
 * 下拉刷新的头部
 */

public abstract class ISlidHeadRefreshView {

    public ISlidHeadRefreshView() {
    }

    // 下拉刷新的滑动的时候
    public void onSliding(int scope, int sliding) {

    }

    // 拦截下拉刷新 true
    public boolean interceptRefresh(int scope, int sliding){
        return false;
    }

    // 当前正在刷新
    public void performRefreshView(){
    }

    // 下拉刷新时显示的视图
    public abstract View getRefreshView(int width, int height);

}
