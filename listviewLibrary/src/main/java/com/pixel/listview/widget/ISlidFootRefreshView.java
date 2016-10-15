package com.pixel.listview.widget;

import android.view.View;

/**
 * Created by Administrator on 2016/10/15.
 * <p>
 * 下拉刷新的头部
 */

public abstract class ISlidFootRefreshView {

    public ISlidFootRefreshView() {
    }

    // 上拉加载的滑动的时候
    public void onSliding(int scope, int sliding) {

    }

    // 拦截下拉加载 true
    public boolean interceptRefresh(int scope, int sliding){
        return false;
    }

    // 当前正在加载
    public void performMoreView(){
    }

    // 上拉加载时显示的视图
    public abstract View getMoreView(int width, int height);

}
