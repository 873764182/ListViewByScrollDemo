package com.pixel.listview.inter;

import android.view.View;

/**
 * Created by Administrator on 2016/10/13.
 * <p>
 * Item滑动菜单点击回调
 */

public interface OnSlidItemClickInterface {
    // 左边的滑动按钮
    void onLeftSlidItemClick(View itemView, int position, int menuOrder, String menuName);

    // 右边的滑动按钮
    void onRightSlidItemClick(View itemView, int position, int menuOrder, String menuName);
}
