package com.pixel.listview.widget;

import android.content.Context;
import android.view.View;

import com.pixel.listview.LinearListView;

/**
 * Created by Administrator on 2016/10/15.
 * <p>
 * 下拉刷新的头部
 */

public abstract class ISlidHeadRefreshView {

    public ISlidHeadRefreshView() {
    }

    public ISlidHeadRefreshView(LinearListView linearListView) {
    }

    // 返回触发刷新的滑动比例 (0 - 1)
    public float getTriggerRefreshValue() {
        return 2f / 3f;
    }

    // 返回执行刷新时悬停的位置 (0 - 1)
    public float getHoverRefreshValue() {
        return 1f / 2f;
    }

    // 下拉刷新的滑动的时候
    public void onSliding(int scope, int sliding) {
    }

    // 拦截下拉刷新 true
    public boolean interceptRefresh(int scope, int sliding) {
        return false;
    }

    // 当前正在刷新
    public void performRefreshView() {
    }

    // 关闭刷新
    public void closeRefreshView() {
    }

    // 下拉刷新时显示的视图
    public abstract View getRefreshView(Context context, int width, int height);

}
