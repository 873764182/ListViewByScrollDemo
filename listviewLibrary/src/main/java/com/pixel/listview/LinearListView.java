package com.pixel.listview;

import android.content.Context;
import android.graphics.Color;
import android.os.Handler;
import android.os.Looper;
import android.support.v7.widget.LinearLayoutCompat;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.HorizontalScrollView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import com.pixel.listview.inter.OnCreateSlidMenuClickInterface;
import com.pixel.listview.inter.OnCreateSlidMenuLeftInterface;
import com.pixel.listview.inter.OnCreateSlidMenuRightInterface;
import com.pixel.listview.inter.OnCreateViewInterface;
import com.pixel.listview.inter.OnItemClickInterface;
import com.pixel.listview.inter.OnItemLongClickInterface;
import com.pixel.listview.inter.OnScrollChangedInterface;
import com.pixel.listview.widget.PHorizontalScrollView;
import com.pixel.listview.widget.PScrollView;

import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;

/**
 * Created by Administrator on 2016/10/13.
 * <p>
 * 基于Linear的列表
 */

public class LinearListView extends LinearLayout implements View.OnTouchListener {
    public static final String TAG = "LinearListView";
    /*View标记 记录item是不是 head 或者 foot*/
    private static final int VIEW_FLAG = 0x20161014;
    /*全局上下文对象*/
    private volatile Context mContext;
    /*Handler主线程管理*/
    private final Handler mHandler = new Handler(Looper.getMainLooper());
    /*视图对象列表*/
    private final List<View> mViews = new ArrayList<>();
    /*列表的长度*/
    private int mListSize = 0;
    /*滑动刷新支持*/
    private FrameLayout mFrameLayout;
    /*滚动支持*/
    private PScrollView mScrollView;
    private PHorizontalScrollView mHScrollView;
    /*内容区域*/
    private LinearLayout mLinearLayout;
    /*添加一层蒙板实现滑动刷新*/
    private View mSlidView;
    /*布局管理器*/
    private LayoutParams mFLayoutParams = new LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
    private LayoutParams mVLayoutParams = new LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
    private FrameLayout.LayoutParams mSLayoutParams = new FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
    private LayoutParams mLLayoutParams = new LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
    /*最大值 Item的最大数量*/
    private int maxItem = Integer.MAX_VALUE;
    /*初始化完成*/
    private boolean isIniteOk = false;
    /*左边滑动的菜单*/
    private final Map<Integer, String[]> leftSlidMenu = new Hashtable<>();
    /*右边滑动的菜单*/
    private final Map<Integer, String[]> rightSlidMenu = new Hashtable<>();
    /*左边滑动菜单开关*/
    private boolean leftSlidOpen = false;
    /*右边滑动菜单开关*/
    private boolean rightSlidOpen = false;
    /*滑动菜单容器(竖直/横向)*/
    private final Map<Integer, View> slidMenuContainer = new Hashtable<>();
    /*根容器的宽高(显示在屏幕上的大小)*/
    private volatile int mRootWidth = 0, mRootHeight = 0, mContentWidth = 0, mContentHeight = 0;
    /*左边滑动菜单的集合*/
    private final Map<Integer, View> leftSlidViewMap = new Hashtable<>();
    /*右边滑动菜单的集合*/
    private final Map<Integer, View> rightSlidViewMap = new Hashtable<>();
    /*滑动坐标*/
    private final int[] coordinates = new int[4];
    /*是否需要执行关闭滑动菜单显示(列表滚动时需要关闭滑动菜单)*/
    private boolean isCloseSlidMenu = true;
    /*Header集合*/
    private final List<View> headerList = new ArrayList<>();
    /*Footer集合*/
    private final List<View> footerList = new ArrayList<>();

    /*创建View的回调*/
    private OnCreateViewInterface onCreateViewInterface;
    /*Item单击回调*/
    private OnItemClickInterface onItemClickInterface;
    /*Item长按回调*/
    private OnItemLongClickInterface onItemLongClickInterface;
    /*滑动按钮创建(右边)*/
    private OnCreateSlidMenuRightInterface onCreateSlidMenuRightInterface;
    /*滑动按钮创建(左边)*/
    private OnCreateSlidMenuLeftInterface onCreateSlidMenuLeftInterface;
    /*滑动按钮点击接口*/
    private OnCreateSlidMenuClickInterface onCreateSlidMenuClickInterface;

    public LinearListView(Context context) {
        super(context);
        this.mContext = context;
    }

    public LinearListView(Context context, AttributeSet attrs) {
        super(context, attrs);
        this.mContext = context;
    }

    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        super.onLayout(changed, left, top, right, bottom);
        this.mRootWidth = getWidth();   // right - left
        this.mRootHeight = getHeight(); // bottom - top
        if (changed) doInit();
    }

    private void runOnDelayed(Runnable runnable) {
        if (isIniteOk) {
            mHandler.post(runnable);
        } else {
            mHandler.postDelayed(runnable, 1000);
        }
    }

    // 设置点击事件
    private void setViewOnClick(final View view, final int position) {
        if (onItemLongClickInterface != null) {
            view.setOnLongClickListener(new OnLongClickListener() {
                @Override
                public boolean onLongClick(View v) {
                    return onItemLongClickInterface.onItemLongClick(view, position);
                }
            });
        }
        if (onItemClickInterface != null) {
            view.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View v) {
                    onItemClickInterface.onItemClick(view, position);
                }
            });
        }
    }

    // 获取滑动按钮的布局 竖直方向的时候 direction方向: 0 左/上, 1 右/下
    private View getSlidMenuVertical(final int direction, final Integer position, final String[] menus) {
        LinearLayout linearLayout = new LinearLayout(mContext);
        linearLayout.setLayoutParams(new LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.MATCH_PARENT));
        linearLayout.setOrientation(HORIZONTAL);
        linearLayout.setBackgroundColor(Color.argb(200, 238, 238, 238));
        for (int menuOrder = 0; menuOrder < menus.length; menuOrder++) { // 滑动按钮的样式可以在这里定义
            View slidMenu = null;
            if (direction == 1 && onCreateSlidMenuRightInterface != null) {
                slidMenu = onCreateSlidMenuRightInterface.getSlidMenuItem(linearLayout, position, menuOrder, menus[menuOrder]);
            } else if (direction == 0 && onCreateSlidMenuLeftInterface != null) {
                slidMenu = onCreateSlidMenuLeftInterface.getSlidMenuItem(linearLayout, position, menuOrder, menus[menuOrder]);
            } else {
                TextView textView = new TextView(mContext);
                textView.setLayoutParams(new LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.MATCH_PARENT));
                textView.setGravity(Gravity.CENTER);
                textView.setPadding(16, 8, 16, 8);
                textView.setText(menus[menuOrder]);
                slidMenu = textView;
            }
            if (onCreateSlidMenuClickInterface != null) {
                final View finalSlidMenu = slidMenu;
                final int finalMenuOrder = menuOrder;
                final int finalMenuOrder1 = menuOrder;
                slidMenu.setOnClickListener(new OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        onCreateSlidMenuClickInterface.onMenuClick(direction, finalSlidMenu, position, finalMenuOrder, menus[finalMenuOrder1]);
                    }
                });
            }
            if (slidMenu != null) linearLayout.addView(slidMenu);
        }
        return linearLayout;
    }

    // 获取滑动按钮的布局 横向方向的时候 direction方向: 0 左/上, 1 右/下
    private View getSlidMenuHorizontal(final int direction, final Integer position, final String[] menus) {
        LinearLayout linearLayout = new LinearLayout(mContext);
        linearLayout.setLayoutParams(new LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
        linearLayout.setOrientation(VERTICAL);
        linearLayout.setBackgroundColor(Color.argb(200, 238, 238, 238));
        for (int menuOrder = 0; menuOrder < menus.length; menuOrder++) { // 滑动按钮的样式可以在这里定义
            View slidMenu = null;
            if (direction == 1 && onCreateSlidMenuRightInterface != null) {
                slidMenu = onCreateSlidMenuRightInterface.getSlidMenuItem(linearLayout, position, menuOrder, menus[menuOrder]);
            } else if (direction == 0 && onCreateSlidMenuLeftInterface != null) {
                slidMenu = onCreateSlidMenuLeftInterface.getSlidMenuItem(linearLayout, position, menuOrder, menus[menuOrder]);
            } else {
                TextView textView = new TextView(mContext);
                textView.setLayoutParams(new LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
                textView.setGravity(Gravity.CENTER);
                textView.setPadding(16, 8, 16, 8);
                textView.setText(menus[menuOrder]);
                slidMenu = textView;
            }
            if (onCreateSlidMenuClickInterface != null) {
                final View finalSlidMenu = slidMenu;
                final int finalMenuOrder = menuOrder;
                final int finalMenuOrder1 = menuOrder;
                slidMenu.setOnClickListener(new OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        onCreateSlidMenuClickInterface.onMenuClick(direction, finalSlidMenu, position, finalMenuOrder, menus[finalMenuOrder1]);
                    }
                });
            }
            if (slidMenu != null) linearLayout.addView(slidMenu);
        }
        return linearLayout;
    }

    // 为Item添加包装(滑动菜单)
    private View viewSlidPackag(View itemView, final Integer position) {
        this.setViewOnClick(itemView, position);
        if (!leftSlidOpen && !rightSlidOpen) {
            return itemView;
        }
        if (getOrientation() == VERTICAL) {

            LinearLayout slidLinearLayout = new LinearLayout(mContext);
            slidLinearLayout.setLayoutParams(new LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT));
            slidLinearLayout.setOrientation(HORIZONTAL);
            itemView.setLayoutParams(new LayoutParams(mRootWidth, ViewGroup.LayoutParams.WRAP_CONTENT));    // 设置ItemView大小等于根容器大小
            slidLinearLayout.addView(itemView);
            final PHorizontalScrollView slidHScrollView = new PHorizontalScrollView(mContext);
            slidHScrollView.setLayoutParams(new LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT));
            slidHScrollView.setHorizontalScrollBarEnabled(false);
            slidHScrollView.setOverScrollMode(View.OVER_SCROLL_NEVER);
            if (leftSlidOpen) {
                String[] leftMenus = leftSlidMenu.get(position);
                if (leftMenus != null && leftMenus.length > 0) {
                    View leftView = getSlidMenuVertical(0, position, leftMenus);
                    slidLinearLayout.addView(leftView, 0);  // 添加在最左边
                    leftSlidViewMap.put(position, leftView);
                }
            }
            if (rightSlidOpen) {
                String[] rightMenus = rightSlidMenu.get(position);
                if (rightMenus != null && rightMenus.length > 0) {
                    View rightView = getSlidMenuVertical(1, position, rightMenus);
                    slidLinearLayout.addView(rightView);    // 添加在最右边
                    rightSlidViewMap.put(position, rightView);
                }
            }
            slidHScrollView.addView(slidLinearLayout);
            slidHScrollView.setOnScrollChangedInterface(new OnScrollChangedInterface() {
                @Override
                public void onScrollChanged(int x, int y, int oldx, int oldy) {
                    coordinates[0] = x;
                    coordinates[1] = y;
                    coordinates[2] = leftSlidViewMap.get(position) != null ? leftSlidViewMap.get(position).getWidth() : 0;
                    coordinates[3] = rightSlidViewMap.get(position) != null ? rightSlidViewMap.get(position).getWidth() : 0;
                }
            });
            slidHScrollView.setOnTouchListener(new OnTouchListener() {
                @Override
                public boolean onTouch(View v, MotionEvent event) {
                    if (event.getAction() == MotionEvent.ACTION_UP) {
                        if (coordinates[0] < coordinates[2] / 2) {
                            slidHScrollView.smoothScrollTo(0, 0);   // 打开左边菜单
                        } else if (coordinates[0] > coordinates[2] + coordinates[3] / 2) {
                            slidHScrollView.smoothScrollTo(coordinates[2] + coordinates[3], 0); // 打开右边菜单
                        } else {
                            slidHScrollView.smoothScrollTo(coordinates[2], 0);  // 关闭两边菜单
                        }
                        return true;
                    }
                    return false;
                }
            });
            slidMenuContainer.put(position, slidHScrollView);
            itemView = slidHScrollView;

        } else if (getOrientation() == HORIZONTAL) {

            LinearLayout slidLinearLayout = new LinearLayout(mContext);
            slidLinearLayout.setLayoutParams(new LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT));
            slidLinearLayout.setOrientation(VERTICAL);
            itemView.setLayoutParams(new LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, mRootHeight));    // 设置ItemView大小等于根容器大小
            slidLinearLayout.addView(itemView);
            final PScrollView slidScrollView = new PScrollView(mContext);
            slidScrollView.setLayoutParams(new LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT));
            slidScrollView.setVerticalScrollBarEnabled(false);
            slidScrollView.setOverScrollMode(View.OVER_SCROLL_NEVER);
            if (leftSlidOpen) {
                String[] leftMenus = leftSlidMenu.get(position);
                if (leftMenus != null && leftMenus.length > 0) {
                    View leftView = getSlidMenuHorizontal(0, position, leftMenus);
                    slidLinearLayout.addView(leftView, 0);  // 添加在最左边
                    leftSlidViewMap.put(position, leftView);
                }
            }
            if (rightSlidOpen) {
                String[] rightMenus = rightSlidMenu.get(position);
                if (rightMenus != null && rightMenus.length > 0) {
                    View rightView = getSlidMenuHorizontal(1, position, rightMenus);
                    slidLinearLayout.addView(rightView);    // 添加在最右边
                    rightSlidViewMap.put(position, rightView);
                }
            }
            slidScrollView.addView(slidLinearLayout);
            slidScrollView.setOnScrollChangedInterface(new OnScrollChangedInterface() {
                @Override
                public void onScrollChanged(int x, int y, int oldx, int oldy) {
                    coordinates[0] = x;
                    coordinates[1] = y;
                    coordinates[2] = leftSlidViewMap.get(position) != null ? leftSlidViewMap.get(position).getHeight() : 0;
                    coordinates[3] = rightSlidViewMap.get(position) != null ? rightSlidViewMap.get(position).getHeight() : 0;
                }
            });
            slidScrollView.setOnTouchListener(new OnTouchListener() {
                @Override
                public boolean onTouch(View v, MotionEvent event) {
                    if (event.getAction() == MotionEvent.ACTION_UP) {
                        if (coordinates[1] < coordinates[2] / 2) {
                            slidScrollView.smoothScrollTo(0, 0);   // 打开左边菜单
                        } else if (coordinates[1] > coordinates[2] + coordinates[3] / 2) {
                            slidScrollView.smoothScrollTo(0, coordinates[2] + coordinates[3]); // 打开右边菜单
                        } else {
                            slidScrollView.smoothScrollTo(0, coordinates[2]);  // 关闭上下菜单
                        }
                        return true;
                    }
                    return false;
                }
            });
            slidMenuContainer.put(position, slidScrollView);
            itemView = slidScrollView;

        }
        return itemView;
    }

    // 添加滑动刷新的头部与尾部
    private void addSlidRefreshView() {
        TextView textView = new TextView(mContext);
        textView.setText("滑动刷新");
        mFrameLayout.addView(textView, 0);
    }

    // 刷新
    public void refreshUiData() {
        for (Map.Entry<Integer, View> entry : slidMenuContainer.entrySet()) {
            ((LinearLayout) ((ViewGroup) entry.getValue()).getChildAt(0)).removeAllViews(); // 清空掉所有的滑动菜单
        }

        mLinearLayout.removeAllViews();
        // 添加Header
        for (View headView : headerList) {
            mLinearLayout.addView(headView);
        }
        // 添加列表内容
        mViews.clear();
        for (int position = 0; (position < mListSize && position < maxItem); position++) {
            mViews.add(onCreateViewInterface.getView(position));
            mLinearLayout.addView(viewSlidPackag(mViews.get(position), position));
        }
        // 添加Footer
        for (View footerView : footerList) {
            mLinearLayout.addView(footerView);
        }
        mLinearLayout.invalidate();

        this.mFrameLayout.removeAllViews();
        this.addSlidRefreshView();
        if (getOrientation() == VERTICAL) {
            this.mFrameLayout.addView(mScrollView);
        } else {
            this.mFrameLayout.addView(mHScrollView);
        }
        this.mFrameLayout.addView(mSlidView);
        this.removeAllViews();
        this.addView(mFrameLayout);
        this.invalidate();

        // 如果有左/上滑动菜单要主动关闭 不然会默认显示出来左边或者上边的按钮 这个方法要在界面初始化完调用才有效
        runOnDelayed(new Runnable() {
            @Override
            public void run() {
                for (Map.Entry<Integer, View> entry : leftSlidViewMap.entrySet()) {
                    View cView = slidMenuContainer.get(entry.getKey());
                    if (cView != null && getOrientation() == VERTICAL) {
                        ((HorizontalScrollView) cView).smoothScrollTo(entry.getValue().getWidth(), 0);
                    } else if (cView != null && getOrientation() == HORIZONTAL) {
                        ((ScrollView) cView).smoothScrollTo(0, entry.getValue().getHeight());
                    }
                }
                mContentWidth = mLinearLayout.getWidth();
                mContentHeight = mLinearLayout.getHeight();
            }
        });
    }

    private boolean isOpenRefresh = true;
    private boolean isOpenMore = false;
    private boolean isScrollSlid = true;    // 下拉刷新/上拉加载更多模式
    private boolean isSlidModelVertical = true; // 下拉刷新模式下 是竖直方向
    private boolean isSlidModelRefresh = true;  // 下拉刷新模式 是下拉刷新
    private int mNewX = 0;
    private int mNewY = 0;
    private int mOldX = 0;
    private int mOldY = 0;

    private int downX = 0;
    private int downY = 0;
    private int moveX = 0;
    private int moveY = 0;
    private int current = 0;    // 达到临界位置时滑动的坐标

    // 更新滑动刷新模式下的状态
    private void updateRefreshModel(boolean isVertical, boolean isRefresh) {
        this.isScrollSlid = false;
        this.isSlidModelVertical = isVertical;
        this.isSlidModelRefresh = isRefresh;
        mSlidView.setVisibility(VISIBLE);
    }

    @Override
    public boolean onTouch(View v, MotionEvent event) {
        if (!isScrollSlid) return true;
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                downX = (int) event.getX();
                downY = (int) event.getY();
                break;
            case MotionEvent.ACTION_MOVE:
                moveX = (int) event.getX();
                moveY = (int) event.getY();
                if (getOrientation() == VERTICAL) {
                    // 打开下拉刷新且滑到了顶部或者是列表高度小于等于屏幕高度时
                    if ((isOpenRefresh && mNewY <= 0) || mContentHeight <= mRootHeight) {
                        updateRefreshModel(true, true);
                    }
                    // 打开加载更多且滑到的底部或者是列表高度小于等于屏幕高度时
                    if ((isOpenMore && mNewY >= mContentHeight - mRootHeight) || mContentHeight <= mRootHeight) {
                        updateRefreshModel(true, false);
                    }
                } else if (getOrientation() == HORIZONTAL) {
                    // 打开下拉刷新且滑到了顶部或者是列表高度小于等于屏幕高度时
                    if ((isOpenRefresh && mNewX <= 0) || mContentWidth <= mRootWidth) {
                        updateRefreshModel(false, true);
                    }
                    // 打开加载更多且滑到的底部或者是列表高度小于等于屏幕高度时
                    if ((isOpenMore && mNewX >= mContentWidth - mRootWidth) || mContentWidth <= mRootWidth) {
                        updateRefreshModel(false, false);
                    }
                }
                break;
            case MotionEvent.ACTION_UP:
                isCloseSlidMenu = true; // 可以执行关闭滑动菜单操作
                current = 0;    // 复位当前坐标
                break;
            default:
        }
        return false;
    }

    // 滚动监听
    private OnScrollChangedInterface scrollChangedInterface = new OnScrollChangedInterface() {
        @Override
        public void onScrollChanged(int x, int y, int oldx, int oldy) {
            mNewX = x;
            mNewY = y;
            mOldX = oldx;
            mOldY = oldy;

            if ((Math.abs(x) - Math.abs(oldx)) > 0) {    // 发生横向滚动
                closeSlidMenuByHorizontal();
            }
            if ((Math.abs(y) - Math.abs(oldy)) > 0) {    // 发送竖直滚动
                closeSlidMenuByVertical();
            }
        }
    };

    // 滑动View监听(实现下拉刷新)
    private OnTouchListener slidOnTouchListener = new OnTouchListener() {
        @Override
        public boolean onTouch(View v, MotionEvent event) {
            switch (event.getAction()) {
                case MotionEvent.ACTION_DOWN:
                    break;
                case MotionEvent.ACTION_MOVE:
                    break;
                case MotionEvent.ACTION_UP:
                    mSlidView.setVisibility(GONE);
                    isScrollSlid = true;    // TODO 写到这里
                    break;
                default:
            }
            return false;
        }
    };

    // 关闭滑动按钮显示(竖直方向时)
    private void closeSlidMenuByVertical() {
        if (!isCloseSlidMenu) return;
        isCloseSlidMenu = false;
        for (Map.Entry<Integer, View> entry : slidMenuContainer.entrySet()) {
            View leftView = leftSlidViewMap.get(entry.getKey());
            if (leftView != null) {
                ((HorizontalScrollView) entry.getValue()).smoothScrollTo(leftView.getWidth(), 0);
            } else {
                ((HorizontalScrollView) entry.getValue()).smoothScrollTo(0, 0);
            }
        }
    }

    // 关闭滑动按钮显示(横向方向时)
    private void closeSlidMenuByHorizontal() {
        if (!isCloseSlidMenu) return;
        isCloseSlidMenu = false;
        for (Map.Entry<Integer, View> entry : slidMenuContainer.entrySet()) {
            View leftView = leftSlidViewMap.get(entry.getKey());
            if (leftView != null) {
                ((ScrollView) entry.getValue()).smoothScrollTo(0, leftView.getHeight());
            } else {
                ((ScrollView) entry.getValue()).smoothScrollTo(0, 0);
            }
        }
    }

    // 初始化容器
    private void initContainer() {
        mFrameLayout = new FrameLayout(mContext);
        mFrameLayout.setLayoutParams(mFLayoutParams);
        mFrameLayout.setBackgroundColor(Color.argb(200, 153, 153, 153));

        mSlidView = new View(mContext);
        mSlidView.setLayoutParams(mVLayoutParams);
        mSlidView.setBackgroundColor(Color.argb(100, 0, 0, 0)); // TODO 改颜色
        mSlidView.setClickable(true);
        mSlidView.setVisibility(GONE);
        mSlidView.setOnTouchListener(slidOnTouchListener);

        mLinearLayout = new LinearLayout(mContext);
        mLinearLayout.setLayoutParams(mLLayoutParams);
        mLinearLayout.setOrientation(getOrientation());
        mLinearLayout.setBackgroundColor(Color.argb(255, 255, 255, 255));

        if (getOrientation() == LinearLayout.VERTICAL) {
            mScrollView = new PScrollView(mContext);
            mScrollView.setLayoutParams(mSLayoutParams);
            mScrollView.addView(mLinearLayout);
            mScrollView.setOnScrollChangedInterface(scrollChangedInterface);
            mScrollView.setOnTouchListener(this);
            mScrollView.setVerticalScrollBarEnabled(false);
        } else {
            mHScrollView = new PHorizontalScrollView(mContext);
            mHScrollView.setLayoutParams(mSLayoutParams);
            mHScrollView.addView(mLinearLayout);
            mHScrollView.setOnScrollChangedInterface(scrollChangedInterface);
            mHScrollView.setOnTouchListener(this);
            mHScrollView.setHorizontalScrollBarEnabled(false);
        }
    }

    private void doInit() {
        if (!isIniteOk) this.initContainer();
        this.refreshUiData();
        isIniteOk = true;
    }

    // 添加头部
    public void addHeaderView(final View headerView) {
        runOnDelayed(new Runnable() {
            @Override
            public void run() {
                headerView.setTag(VIEW_FLAG, "HeaderView");   // 标记这个View 为 HeaderView
                headerList.add(0, headerView);
                refreshUiData();
            }
        });
    }

    // 删除头部
    public void removeHeaderView(final int location) {
        runOnDelayed(new Runnable() {
            @Override
            public void run() {
                if (headerList.size() > 0) {
                    headerList.remove(location);
                    refreshUiData();
                }
            }
        });
    }

    // 添加尾部
    public void addFooterView(final View footerView) {
        runOnDelayed(new Runnable() {
            @Override
            public void run() {
                footerView.setTag(VIEW_FLAG, "FooterView"); // 标记这个View 为 FooterView
                footerList.add(footerView);
                refreshUiData();
            }
        });
    }

    // 删除尾部
    public void removeFooterView(final int location) {
        runOnDelayed(new Runnable() {
            @Override
            public void run() {
                if (footerList.size() > 0) {
                    footerList.remove(location);
                    refreshUiData();
                }
            }
        });
    }

    public void addChildView(View view) {
        addChildView(view, false, false);
    }

    public void addChildView(View view, boolean isClear, boolean mandatoryAdd) {
        addChildView(view, isClear, mandatoryAdd, true);
    }

    // 在末尾添加View
    public void addChildView(final View view, final boolean isClear, final boolean mandatoryAdd, final boolean isRefresh) {
        runOnDelayed(new Runnable() {
            @Override
            public void run() {
                if (isClear) mViews.clear();    // 清空原有列表
                if (mandatoryAdd) { // 强制添加长度超过最大限制删除头部 默认添加到最大长度就不再添加
                    if (mViews.size() > maxItem) {
                        mViews.remove(0);
                    }
                    mViews.add(view);
                } else {
                    if (mViews.size() <= maxItem) {
                        mViews.add(view);
                    } else {
                        return;
                    }
                }
                if (isRefresh) refreshUiData();
            }
        });
    }

    // 添加View集合
    public void addViews(List<View> views, boolean isClear, boolean mandatoryAdd) {
        for (View view : views) {
            addChildView(view, isClear, mandatoryAdd, false);
        }
        refreshUiData();
    }

    // 获取滚动对象(需要延迟加载)
    public ScrollView getmScrollView() {
        return mScrollView;
    }

    // 获取滚动对象(需要延迟加载)
    public HorizontalScrollView getmHScrollView() {
        return mHScrollView;
    }

    // 获取列表对象(需要延迟加载)
    public LinearLayout getmLinearLayout() {
        return mLinearLayout;
    }

    public void setLeftSlidMenu(Map<Integer, String[]> leftSlidMenu) {
        this.leftSlidMenu.clear();
        this.leftSlidMenu.putAll(leftSlidMenu);
    }

    public void setLeftSlidMenu(Integer position, String... menus) {
        this.leftSlidMenu.put(position, menus);
    }

    public void setLeftSlidOpen(boolean leftSlidOpen) {
        this.leftSlidOpen = leftSlidOpen;
    }

    public void setRightSlidMenu(Map<Integer, String[]> rightSlidMenu) {
        this.rightSlidMenu.clear();
        this.rightSlidMenu.putAll(rightSlidMenu);
    }

    public void setRightSlidMenu(Integer position, String... menus) {
        this.rightSlidMenu.put(position, menus);
    }

    public void setRightSlidOpen(boolean rightSlidOpen) {
        this.rightSlidOpen = rightSlidOpen;
    }

    public void setmOrientation(@LinearLayoutCompat.OrientationMode int orientation) {
        setOrientation(orientation);
    }

    public void setOnItemClickInterface(OnItemClickInterface onItemClickInterface) {
        this.onItemClickInterface = onItemClickInterface;
    }

    public void setOnItemLongClickInterface(OnItemLongClickInterface onItemLongClickInterface) {
        this.onItemLongClickInterface = onItemLongClickInterface;
    }

    public void setOnCreateSlidMenuRightInterface(OnCreateSlidMenuRightInterface onCreateSlidMenuRightInterface) {
        this.onCreateSlidMenuRightInterface = onCreateSlidMenuRightInterface;
    }

    public void setOnCreateSlidMenuLeftInterface(OnCreateSlidMenuLeftInterface onCreateSlidMenuLeftInterface) {
        this.onCreateSlidMenuLeftInterface = onCreateSlidMenuLeftInterface;
    }

    public void setOnCreateSlidMenuClickInterface(OnCreateSlidMenuClickInterface onCreateSlidMenuClickInterface) {
        this.onCreateSlidMenuClickInterface = onCreateSlidMenuClickInterface;
    }

    public void setMaxItem(int maxItem) {
        this.maxItem = maxItem;
    }

    public void setListSize(int listSize) {
        this.mListSize = listSize;
    }

    public void setOnCreateViewInterface(OnCreateViewInterface onCreateViewInterface) {
        this.onCreateViewInterface = onCreateViewInterface;
    }

}
