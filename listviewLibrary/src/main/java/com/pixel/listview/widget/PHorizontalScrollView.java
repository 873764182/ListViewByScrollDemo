package com.pixel.listview.widget;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.HorizontalScrollView;

import com.pixel.listview.inter.OnScrollChangedInterface;

/**
 * Created by Administrator on 2016/10/14.
 */

public class PHorizontalScrollView extends HorizontalScrollView {
    private OnScrollChangedInterface onScrollChangedInterface;

    public void setOnScrollChangedInterface(OnScrollChangedInterface onScrollChangedInterface) {
        this.onScrollChangedInterface = onScrollChangedInterface;
    }

    public PHorizontalScrollView(Context context) {
        super(context);
    }

    public PHorizontalScrollView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    protected void onScrollChanged(int l, int t, int oldl, int oldt) {
        super.onScrollChanged(l, t, oldl, oldt);
        if (onScrollChangedInterface != null) {
            onScrollChangedInterface.onScrollChanged(l, t, oldl, oldt);
        }
    }

}
