package com.pixel.listview.inter;

/**
 * Created by Administrator on 2016/10/17.
 * <p>
 * 列表滚动到底部或者底部时
 */

public interface OnScrollTopOrBottomInterface {

    // 滑动到了顶部
    void onTop(int mNewY, int mOldY);

    // 滑动到了底部
    void onBom(int mNewY, int mOldY);
}
