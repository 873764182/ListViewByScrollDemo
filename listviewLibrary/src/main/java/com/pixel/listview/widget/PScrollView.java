package com.pixel.listview.widget;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.ScrollView;

import com.pixel.listview.inter.OnScrollChangedInterface;

/**
 * Created by Administrator on 2016/10/14.
 */

public class PScrollView extends ScrollView {
    private OnScrollChangedInterface onScrollChangedInterface;

    public void setOnScrollChangedInterface(OnScrollChangedInterface onScrollChangedInterface) {
        this.onScrollChangedInterface = onScrollChangedInterface;
    }

    public PScrollView(Context context) {
        super(context);
    }

    public PScrollView(Context context, AttributeSet attrs) {
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
