package com.pixel.listview.inter;

import android.view.View;

/**
 * Created by Administrator on 2016/10/14.
 * <p>
 * 滑动按钮点击回调
 */

public interface OnCreateSlidMenuClickInterface {
    /**
     * 列表滑动菜单单击回调
     *
     * @param direction 方向 左/右 按钮 0 左/上, 1 右/下
     * @param view      菜单View
     * @param position  列表行号
     * @param menuOrder 滑动按钮顺序 从左到右
     * @param menuName  滑动按钮的名称
     */
    void onMenuClick(int direction, View view, int position, int menuOrder, String menuName);
}
