package com.pixel.listview.inter;

import android.view.View;

/**
 * Created by Administrator on 2016/10/14.
 * <p>
 * 滑动按钮点击回调
 */

public interface OnCreateSlidMenuClickInterface {
    void onMenuClick(int direction, View view, int position, int menuOrder, String menuName);
}
