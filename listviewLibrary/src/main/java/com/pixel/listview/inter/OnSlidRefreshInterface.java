package com.pixel.listview.inter;

import android.content.Context;
import android.view.View;

import com.pixel.listview.LinearListView;

/**
 * Created by Administrator on 2016/10/15.
 * <p>
 * 滑动刷新回调
 */

public interface OnSlidRefreshInterface {
    // 刷新
    void doRefresh(Context mContext, LinearListView linearListView);

    // 更多
    void doMore(Context mContext, LinearListView linearListView);
}
