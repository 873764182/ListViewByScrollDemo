package com.pixel.listview.inter;

import android.view.View;

/**
 * Created by Administrator on 2016/10/14.
 */

public interface OnCreateSlidMenuLeftInterface {
    /**
     * 自定义左边滑动菜单按钮
     *
     * @param containerView 按钮容器(父视图)
     * @param position      列表行
     * @param menuOrder     按钮顺序
     * @param menuName      按钮名称
     * @return
     */
    View getSlidMenuItem(View containerView, int position, int menuSize, int menuOrder, String menuName);
}
