package com.pixel.listview.inter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

/**
 * Created by Administrator on 2016/10/14.
 */

public interface OnCreateSlidMenuRightInterface {
    /**
     * 自定义右边滑动菜单按钮
     *
     *
     * @param inflater
     * @param containerView 按钮容器(父视图)
     * @param position      列表行
     * @param menuOrder     按钮顺序
     * @param menuName      按钮名称
     * @return
     */
    View getSlidMenuItem(LayoutInflater inflater, ViewGroup containerView, int position, int menuSize, int menuOrder, String menuName);
}
