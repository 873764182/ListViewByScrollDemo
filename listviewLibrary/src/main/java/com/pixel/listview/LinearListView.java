package com.pixel.listview;

import android.content.Context;
import android.graphics.Color;
import android.graphics.Point;
import android.os.Handler;
import android.os.Looper;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
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
import com.pixel.listview.inter.OnScrollTopOrBottomInterface;
import com.pixel.listview.inter.OnSlidRefreshInterface;
import com.pixel.listview.widget.HSlidFootRefreshView;
import com.pixel.listview.widget.HSlidHeadRefreshView;
import com.pixel.listview.widget.ISlidFootRefreshView;
import com.pixel.listview.widget.ISlidHeadRefreshView;
import com.pixel.listview.widget.PHorizontalScrollView;
import com.pixel.listview.widget.PScrollView;
import com.pixel.listview.widget.SlidFootRefreshView;
import com.pixel.listview.widget.SlidHeadRefreshView;

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
        this.initScreenSize();
    }

    public LinearListView(Context context, AttributeSet attrs) {
        super(context, attrs);
        this.mContext = context;
        this.initScreenSize();
    }

    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        super.onLayout(changed, left, top, right, bottom);
        this.mRootWidth = getWidth();   // right - left
        this.mRootHeight = getHeight(); // bottom - top
        if (!isIniteOk) this.initContainer();
        if (changed) {
            refreshUiData();
            isIniteOk = true;
        }
    }

    // 延迟加载
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
                slidMenu = onCreateSlidMenuRightInterface.getSlidMenuItem(linearLayout, position, menus.length, menuOrder, menus[menuOrder]);
            } else if (direction == 0 && onCreateSlidMenuLeftInterface != null) {
                slidMenu = onCreateSlidMenuLeftInterface.getSlidMenuItem(linearLayout, position, menus.length, menuOrder, menus[menuOrder]);
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
                slidMenu = onCreateSlidMenuRightInterface.getSlidMenuItem(linearLayout, position, menus.length, menuOrder, menus[menuOrder]);
            } else if (direction == 0 && onCreateSlidMenuLeftInterface != null) {
                slidMenu = onCreateSlidMenuLeftInterface.getSlidMenuItem(linearLayout, position, menus.length, menuOrder, menus[menuOrder]);
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

    // 添加滑动刷新的头部
    private void addSlidRefreshHeadView(FrameLayout frameLayout) {
        if (getOrientation() == VERTICAL) {
            iSlidHeadRefreshView = new SlidHeadRefreshView(this);
        } else {
            iSlidHeadRefreshView = new HSlidHeadRefreshView(this); // 水平方向
        }
        this.mFrameLayout.addView(iSlidHeadRefreshView.getRefreshView(mContext, mRootWidth, mRootHeight), 0);
    }

    // 添加滑动刷新的尾部
    private void addSlidRefreshFootView(FrameLayout frameLayout) {
        LinearLayout linearLayout = new LinearLayout(mContext);
        linearLayout.setLayoutParams(new LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
        if (getOrientation() == VERTICAL) {
            iSlidFootRefreshView = new SlidFootRefreshView(this);
            linearLayout.setOrientation(VERTICAL);
            linearLayout.setGravity(Gravity.BOTTOM | Gravity.CENTER);
            linearLayout.addView(iSlidFootRefreshView.getMoreView(mContext, mRootWidth, mRootHeight));
        } else {
            iSlidFootRefreshView = new HSlidFootRefreshView(this);
            linearLayout.setOrientation(VERTICAL);
            linearLayout.setGravity(Gravity.RIGHT | Gravity.CENTER);
            linearLayout.addView(iSlidFootRefreshView.getMoreView(mContext, mRootWidth, mRootHeight)); // 水平方向
        }
        this.mFrameLayout.addView(linearLayout, 1); // 要套一层线性布局 才能把尾部布局定位在底部
    }

    /* 处理下拉刷新/上拉加载 */
    private static final int SLIDO_OFFSET = 100;  // 滑动误差偏移
    private boolean isOpenRefresh = false;   // 是否开启下拉刷新
    private boolean isOpenMore = false;      // 是否开启上拉加载
    private boolean isScrollSlid = true;    // 当前是下拉刷新/上拉加载更多模式
    private boolean isRefreshState = false; // 当前是正在刷新状态
    private boolean isSlidModelVertical = true; // 下拉刷新模式下 是竖直方向
    private boolean isSlidModelRefresh = true;  // 下拉刷新模式 是下拉刷新
    private int mNewX = 0, mNewY = 0, mOldX = 0, mOldY = 0; // 滚动视图的滚动参数
    private int downX = 0, downY = 0, moveX = 0, moveY = 0; // 滚动视图的触摸位置
    private int downX_slid = 0, downY_slid = 0, moveX_slid = 0, moveY_slid = 0; // 处理滑动刷新的蒙板的触摸参数
    private int headSlidSize = 0, footSlidSize = 0; // 头部尾部刷新View的高度(横向时时宽度)
    private float triggerRefreshValue = 2f / 3f; // 临界值 滑动到百分之多少时触发刷新事件
    private int screenWidth = 0, screenHeight = 0;  // 屏幕宽高(当列表为空且要求刷新时必须填充一个View支持刷新操作)

    private OnSlidRefreshInterface onSlidRefreshInterface;  // 刷新/加载 回调接口
    private ISlidHeadRefreshView iSlidHeadRefreshView;  // 下拉刷新头部(在用户打开下拉刷新时创建一个默认,用户也可以自定义)
    private ISlidFootRefreshView iSlidFootRefreshView;  // 上拉加载头部(在用户打开上拉加载时创建一个默认,用户也可以自定义)
    private OnScrollTopOrBottomInterface onScrollTopOrBottomInterface;  // 列表滚动到了顶部或者底部回调

    // 初始化屏幕宽高
    private void initScreenSize() {
        WindowManager wm = (WindowManager) mContext.getSystemService(Context.WINDOW_SERVICE);
        Point point = new Point();
        wm.getDefaultDisplay().getSize(point);
        screenWidth = point.x;
        screenHeight = point.y;
    }

    // 更新滑动刷新模式下的状态
    private void updateRefreshModel(boolean isVertical, boolean isRefresh) {
        this.isScrollSlid = false;
        this.isSlidModelVertical = isVertical;
        this.isSlidModelRefresh = isRefresh;
        this.mSlidView.setVisibility(VISIBLE);
        this.downX = 0;
        this.downY = 0;

        if (iSlidHeadRefreshView != null) { // 计算自定义头部高度/宽度
            View headView = iSlidHeadRefreshView.getRefreshView(mContext, mRootWidth, mRootHeight);
            if (headView != null) {
                if (getOrientation() == VERTICAL) {
                    headSlidSize = headView.getHeight();
                } else {
                    headSlidSize = headView.getWidth();
                }
            }
        }
        if (iSlidFootRefreshView != null) { // 计算自定义尾部高度/宽度
            View footView = iSlidFootRefreshView.getMoreView(mContext, mRootWidth, mRootHeight);
            if (footView != null) {
                if (getOrientation() == VERTICAL) {
                    footSlidSize = footView.getHeight();
                } else {
                    footSlidSize = footView.getWidth();
                }
            }
        }
    }

    @Override
    public boolean onTouch(View v, MotionEvent event) {
        if (!isScrollSlid) return true;
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                downX = (int) event.getRawX();
                downY = (int) event.getRawY();
                break;
            case MotionEvent.ACTION_MOVE:
                moveX = (int) event.getRawX();
                moveY = (int) event.getRawY();
                if (downX <= 0) downX = moveX;
                if (downY <= 0) downY = moveY;
                if (getOrientation() == VERTICAL) {
                    // 滑到顶部时
                    if (onScrollTopOrBottomInterface != null && mNewY <= 0) {
                        onScrollTopOrBottomInterface.onTop(mNewY, mOldY);
                    } else if (onScrollTopOrBottomInterface != null && mNewY >= mContentHeight - mRootHeight) {
                        onScrollTopOrBottomInterface.onBom(mNewY, mOldY);
                    }
                    // 打开下拉刷新且滑到了顶部或者是列表高度小于等于屏幕高度时
                    if ((isOpenRefresh && mNewY <= 0) || mContentHeight <= mRootHeight) {
                        if (moveY > downY && (moveY - downY >= SLIDO_OFFSET)) { //  向下滑
                            updateRefreshModel(true, true);
                        }
                    }
                    // 打开加载更多且滑到的底部或者是列表高度小于等于屏幕高度时
                    if ((isOpenMore && mNewY >= mContentHeight - mRootHeight) || mContentHeight <= mRootHeight) {
                        if (downY > moveY && (downY - moveY >= SLIDO_OFFSET)) {  // 向上滑
                            updateRefreshModel(true, false);
                        }
                    }
                } else if (getOrientation() == HORIZONTAL) {
                    // 滑到左边时
                    if (onScrollTopOrBottomInterface != null && mNewX <= 0) {
                        onScrollTopOrBottomInterface.onTop(mNewY, mOldY);
                    } else if (onScrollTopOrBottomInterface != null && mNewX >= mContentWidth - mRootWidth) {
                        onScrollTopOrBottomInterface.onBom(mNewY, mOldY);
                    }
                    // 打开下拉刷新且滑到了顶部或者是列表高度小于等于屏幕高度时
                    if ((isOpenRefresh && mNewX <= 0) || mContentWidth <= mRootWidth) {
                        if (moveX > downX && (moveX - downX >= SLIDO_OFFSET)) {  // 向右滑
                            updateRefreshModel(false, true);
                        }
                    }
                    // 打开加载更多且滑到的底部或者是列表高度小于等于屏幕高度时
                    if ((isOpenMore && mNewX >= mContentWidth - mRootWidth) || mContentWidth <= mRootWidth) {
                        if (downX > moveX && (downX - moveX >= SLIDO_OFFSET)) {  // 向左滑
                            updateRefreshModel(false, false);
                        }
                    }
                }
                break;
            case MotionEvent.ACTION_UP:
                downX = 0;
                downY = 0;
                isCloseSlidMenu = true; // 可以执行关闭滑动菜单操作
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
        private volatile int scope = 0;
        private volatile int moveValue = 0;
        private volatile Boolean isRefresh = null;  // 是下拉刷新
        private volatile boolean isIntercept = false;  // 是下拉刷新

        @Override
        public boolean onTouch(View v, MotionEvent event) {
            switch (event.getAction()) {
                case MotionEvent.ACTION_DOWN:
                    downX_slid = (int) event.getRawX();
                    downY_slid = (int) event.getRawY();
                    break;
                case MotionEvent.ACTION_MOVE:
                    moveX_slid = (int) event.getRawX();
                    moveY_slid = (int) event.getRawY();
                    if (downX_slid <= 0) downX_slid = moveX_slid;
                    if (downY_slid <= 0) downY_slid = moveY_slid;
                    if (isSlidModelVertical) {

                        if (isSlidModelRefresh) {
                            moveValue = (moveY_slid - downY_slid) / 2;
                            if (headSlidSize <= 0 && moveValue > mRootHeight / 4) { // 默认头部View为1/4的rootView
                                moveValue = mRootHeight / 4;
                            } else if (headSlidSize > 0 && moveValue > headSlidSize) {
                                moveValue = headSlidSize;
                            }
                            mSLayoutParams.topMargin = moveValue > 0 ? moveValue : 0;
                            mScrollView.setLayoutParams(mSLayoutParams);
                            scope = headSlidSize <= 0 ? (mRootHeight / 4) : headSlidSize;
                            if (iSlidHeadRefreshView != null) {
                                iSlidHeadRefreshView.onSliding(scope, moveValue);
                                isIntercept = iSlidHeadRefreshView.interceptRefresh(scope, moveValue);
                            }
                            isRefresh = true;
                        } else if (!isSlidModelRefresh) {
                            moveValue = (downY_slid - moveY_slid) / 2;
                            if (footSlidSize <= 0 && moveValue > mRootHeight / 8) { // 默认尾部View为1/8的rootView
                                moveValue = mRootHeight / 8;
                            } else if (footSlidSize > 0 && moveValue > footSlidSize) {
                                moveValue = footSlidSize;
                            }
                            mSLayoutParams.bottomMargin = moveValue > 0 ? moveValue : 0;
                            mScrollView.setLayoutParams(mSLayoutParams);
                            mScrollView.scrollTo(0, mContentHeight);    // smoothScrollTo
                            scope = footSlidSize <= 0 ? (mRootHeight / 8) : footSlidSize;
                            if (iSlidFootRefreshView != null) {
                                iSlidFootRefreshView.onSliding(scope, moveValue);
                                isIntercept = iSlidFootRefreshView.interceptRefresh(scope, moveValue);
                            }
                            isRefresh = false;
                        }

                    } else if (!isSlidModelVertical) {

                        if (isSlidModelRefresh) {
                            moveValue = (moveX_slid - downX_slid) / 2;
                            if (headSlidSize <= 0 && moveValue > mRootWidth / 4) { // 默认头部View为1/4的rootView
                                moveValue = mRootWidth / 4;
                            } else if (headSlidSize > 0 && moveValue > headSlidSize) {
                                moveValue = headSlidSize;
                            }
                            mSLayoutParams.leftMargin = moveValue > 0 ? moveValue : 0;
                            mHScrollView.setLayoutParams(mSLayoutParams);
                            scope = headSlidSize <= 0 ? (mRootWidth / 4) : headSlidSize;
                            if (iSlidHeadRefreshView != null) {
                                iSlidHeadRefreshView.onSliding(scope, moveValue);
                                isIntercept = iSlidHeadRefreshView.interceptRefresh(scope, moveValue);
                            }
                            isRefresh = true;
                        } else if (!isSlidModelRefresh) {
                            moveValue = (downX_slid - moveX_slid) / 2;
                            if (footSlidSize <= 0 && moveValue > mRootWidth / 4) { // 默认尾部View为1/8的rootView
                                moveValue = mRootWidth / 8;
                            } else if (footSlidSize > 0 && moveValue > footSlidSize) {
                                moveValue = footSlidSize;
                            }
                            mSLayoutParams.rightMargin = moveValue > 0 ? moveValue : 0;
                            mHScrollView.setLayoutParams(mSLayoutParams);
                            mHScrollView.scrollTo(mContentWidth, 0);    // smoothScrollTo 目前这里存在问题 使列表不能滑动 每次都被定为到末尾
                            scope = footSlidSize <= 0 ? (mRootWidth / 4) : footSlidSize;
                            if (iSlidFootRefreshView != null) {
                                iSlidFootRefreshView.onSliding(scope, moveValue);
                                isIntercept = iSlidFootRefreshView.interceptRefresh(scope, moveValue);
                            }
                            isRefresh = false;
                        }

                    }
                    break;
                case MotionEvent.ACTION_UP:
                    downX_slid = 0;
                    downY_slid = 0;
                    mSlidView.setVisibility(GONE);

                    if (!isRefreshState && moveValue >= scope * triggerRefreshValue) {    // 滑动距离超过HeadView的2/3时才触发刷新事件
                        isRefreshState = true;
                        if (!isIntercept && onSlidRefreshInterface != null) {
                            isScrollSlid = false;   // 关闭列表滚动
                            if (isRefresh != null && isRefresh) {
                                onSlidRefreshInterface.doRefresh(mContext, LinearListView.this);
                                if (iSlidHeadRefreshView != null) {
                                    iSlidHeadRefreshView.performRefreshView();
                                }
                            } else if (isRefresh != null && !isRefresh) {
                                onSlidRefreshInterface.doMore(mContext, LinearListView.this);
                                if (iSlidFootRefreshView != null) {
                                    iSlidFootRefreshView.performMoreView();
                                }
                            }
                            performRefreshView(isRefresh, scope, moveValue);
                        }
                    } else {
                        closeRefreshView();
                    }
                    break;
                default:
            }
            return false;
        }
    };

    // 关闭滑动视图
    private void performRefreshView(Boolean isRefresh, int scope, final int moveValue) {
        if (getOrientation() == VERTICAL) {
            if (isRefresh == null) {
                mSLayoutParams.topMargin = 0;
                mSLayoutParams.bottomMargin = 0;
            } else if (isRefresh) {
                mSLayoutParams.topMargin = scope / 2;
                mSLayoutParams.bottomMargin = 0;
            } else if (!isRefresh) {
                mSLayoutParams.topMargin = 0;
                mSLayoutParams.bottomMargin = scope * 3 / 5;
            }
            mScrollView.setLayoutParams(mSLayoutParams);
        } else {
            if (isRefresh == null) {
                mSLayoutParams.leftMargin = 0;
                mSLayoutParams.rightMargin = 0;
            } else if (isRefresh) {
                mSLayoutParams.leftMargin = scope / 2;
                mSLayoutParams.rightMargin = 0;
            } else if (!isRefresh) {
                mSLayoutParams.leftMargin = 0;
                mSLayoutParams.rightMargin = scope / 2;
            }
            mHScrollView.setLayoutParams(mSLayoutParams);
        }
    }

    // 关闭滑动菜单显示(竖直方向时)
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
        mFrameLayout.setBackgroundColor(Color.argb(255, 255, 255, 255));    // 容器背景设置 Color.argb(200, 153, 153, 153)

        mSlidView = new View(mContext);
        mSlidView.setLayoutParams(new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT));
        mSlidView.setBackgroundColor(Color.argb(0, 0, 0, 0)); // 蒙板颜色
        mSlidView.setClickable(true);
        mSlidView.setVisibility(GONE);
        mSlidView.setOnTouchListener(slidOnTouchListener);

        mLinearLayout = new LinearLayout(mContext);
        mLinearLayout.setLayoutParams(mLLayoutParams);
        mLinearLayout.setOrientation(getOrientation());
        mLinearLayout.setBackgroundColor(Color.argb(255, 255, 255, 255));   // 内容控件背景色

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

    // 打开下拉刷新或者上拉加载的情况下 内容不满容器高度/宽度 添加一个空白View填充
    private void setRefreshFillView(final boolean isOpen) {
        runOnDelayed(new Runnable() {
            @Override
            public void run() { // 把滚动视图缩短
                // 必须进行第二次延迟加载
                mHandler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        LayoutParams layoutParams = (LayoutParams) getLayoutParams();
                        if (getOrientation() == VERTICAL && mContentHeight <= mRootHeight) {
                            if (isOpen) {
                                layoutParams.height = mContentHeight - SLIDO_OFFSET;
                            } else {
                                layoutParams.height = mContentHeight;
                            }
                        } else if (getOrientation() == HORIZONTAL && mContentWidth <= mRootWidth) {
                            if (isOpen) {
                                layoutParams.width = mContentWidth - SLIDO_OFFSET;
                            } else {
                                layoutParams.width = mContentWidth;
                            }
                        }
                        setLayoutParams(layoutParams);
                    }
                }, 500);
            }
        });
    }

    /**
     * 获取触发滑动刷新的临界值
     */
    public float getTriggerRefreshValue() {
        return triggerRefreshValue;
    }

    /**
     * 设置触发滑动刷新的百分比 (范围: 0.00 - 1.00)
     */
    public void setTriggerRefreshValue(float triggerRefreshValue) {
        this.triggerRefreshValue = triggerRefreshValue;
    }

    /**
     * 关闭刷新视图(调用下拉刷新/上拉加载 结束后一定要调用)
     */
    public void closeRefreshView() {

        if (iSlidHeadRefreshView != null) {
            iSlidHeadRefreshView.closeRefreshView();
        }
        if (iSlidFootRefreshView != null) {
            iSlidFootRefreshView.closeMoreView();
        }
        if (getOrientation() == VERTICAL) {
            mSLayoutParams.topMargin = 0;
            mSLayoutParams.bottomMargin = 0;
            mScrollView.setLayoutParams(mSLayoutParams);
        } else {
            mSLayoutParams.leftMargin = 0;
            mSLayoutParams.rightMargin = 0;
            mHScrollView.setLayoutParams(mSLayoutParams);
        }
        isRefreshState = false;
        isScrollSlid = true; // 打开滚动视图滚动
    }

    /**
     * 刷新列表数据
     */
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
        // 处理打开滑动刷新但是列表为空的时
        if ((isOpenRefresh || isOpenMore) && mViews.size() <= 0) {
            View fillView = new View(mContext);
            fillView.setBackgroundColor(Color.argb(0, 255, 255, 255));
            fillView.setClickable(true);
            if (getOrientation() == VERTICAL) {
                fillView.setLayoutParams(new LayoutParams(mRootWidth <= 0 ? screenWidth : mRootWidth, screenHeight / 3));
            } else {
                fillView.setLayoutParams(new LayoutParams(screenWidth / 2, mRootHeight <= 0 ? screenHeight : mRootHeight));
            }
            mLinearLayout.addView(fillView);
        }

        // 添加Footer
        for (View footerView : footerList) {
            mLinearLayout.addView(footerView);
        }
        mLinearLayout.invalidate();

        this.mFrameLayout.removeAllViews();
        if (isOpenRefresh) {
            this.addSlidRefreshHeadView(mFrameLayout);  // 添加刷新头控件
        }
        if (isOpenMore) {
            this.addSlidRefreshFootView(mFrameLayout);  // 添加刷新尾控件
        }
        if (getOrientation() == VERTICAL) {
            mScrollView.removeAllViews();
            mScrollView.addView(mLinearLayout);
            this.mFrameLayout.addView(mScrollView);
        } else {
            mHScrollView.removeAllViews();
            mHScrollView.addView(mLinearLayout);
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

                // 设置根视图内容与内容视图一样大
                if (mContentWidth <= mRootWidth || mContentHeight <= mRootHeight) {
                    LayoutParams layoutParams = (LayoutParams) getLayoutParams();
                    if (layoutParams.width == LayoutParams.WRAP_CONTENT) {
                        if (isOpenRefresh || isOpenMore) {
                            layoutParams.width = mContentWidth - SLIDO_OFFSET;
                        } else {
                            layoutParams.width = mContentWidth;
                        }
                    }
                    if (layoutParams.height == LayoutParams.WRAP_CONTENT) {
                        if (isOpenRefresh || isOpenMore) {
                            layoutParams.height = mContentHeight - SLIDO_OFFSET;
                        } else {
                            layoutParams.height = mContentHeight;
                        }
                    }
                    setLayoutParams(layoutParams);
                }
            }
        });
    }

    /**
     * 是否开启下拉刷新 默认关闭
     */
    public void setIsOpenRefresh(boolean isOpenRefresh) {
        setRefreshFillView(isOpenRefresh);
        this.isOpenRefresh = isOpenRefresh;
    }

    /**
     * 是否开启上拉加载更多 默认关闭
     */
    public void setIsOpenMore(boolean isOpenMore) {
        setRefreshFillView(isOpenMore);
        this.isOpenMore = isOpenMore;
    }

    /**
     * 设置下拉刷新/上拉加载回调
     */
    public void setOnSlidRefreshInterface(OnSlidRefreshInterface onSlidRefreshInterface) {
        this.onSlidRefreshInterface = onSlidRefreshInterface;
    }

    /**
     * 设置自定义的下拉刷新头
     */
    public void setiSlidHeadRefreshView(ISlidHeadRefreshView iSlidHeadRefreshView) {
        this.iSlidHeadRefreshView = iSlidHeadRefreshView;
    }

    /**
     * 设置自定义的上拉加载尾
     */
    public void setISlidFootRefreshView(ISlidFootRefreshView iSlidFootRefreshView) {
        this.iSlidFootRefreshView = iSlidFootRefreshView;
    }

    /**
     * 列表滚动到底部或者顶部回调
     */
    public void setOnScrollTopOrBottomInterface(OnScrollTopOrBottomInterface onScrollTopOrBottomInterface) {
        this.onScrollTopOrBottomInterface = onScrollTopOrBottomInterface;
    }

    /**
     * 添加列表头部
     */
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

    /**
     * 删除列表头部 多个头部时 location 定位头部 一个头部 传入 0
     */
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

    /**
     * 添加尾部
     */
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

    /**
     * 删除列表尾部 location多个尾部时 定位尾部 一个尾部 传入 0
     */
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

    /**
     * 在末尾添加View (这里添加的要是超过列表item最大限制会把第一个顶出去 以此类推)
     */
    public void addChildView(View view) {
        addChildView(view, false, false);
    }

    /**
     * 在末尾添加View (这里添加的要是超过列表item最大限制会把第一个顶出去 以此类推)
     */
    public void addChildView(View view, boolean isClear, boolean mandatoryAdd) {
        addChildView(view, isClear, mandatoryAdd, true);
    }

    /**
     * 在末尾添加View (这里添加的要是超过列表item最大限制会把第一个顶出去 以此类推)
     */
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

    /**
     * 添加View集合 (这里添加的要是超过列表item最大限制会把第一个顶出去 以此类推)
     */
    public void addViews(List<View> views, boolean isClear, boolean mandatoryAdd) {
        for (View view : views) {
            addChildView(view, isClear, mandatoryAdd, false);
        }
        refreshUiData();
    }

    /**
     * 获取控件容器对象
     */
    public FrameLayout getmFrameLayout() {
        return mFrameLayout;
    }

    /**
     * 获取滚动对象 竖直方向时 (需要延迟加载)
     */
    public ScrollView getmScrollView() {
        return mScrollView;
    }

    /**
     * 获取滚动对象 横向方向时 (需要延迟加载)
     */
    public HorizontalScrollView getmHScrollView() {
        return mHScrollView;
    }

    /**
     * 获取列表对象(需要延迟加载)
     */
    public LinearLayout getmLinearLayout() {
        return mLinearLayout;
    }

    /**
     * 获取滑动刷新蒙板view
     */
    public View getmSlidView() {
        return mSlidView;
    }

    /**
     * 设置列表左边滑动菜单按钮 key 对应列表的行号
     */
    public void setLeftSlidMenu(Map<Integer, String[]> leftSlidMenu) {
        this.leftSlidMenu.clear();
        this.leftSlidMenu.putAll(leftSlidMenu);
    }

    /**
     * 设置左边滑动菜单的按钮 position 列表的行号
     */
    public void setLeftSlidMenu(Integer position, String... menus) {
        this.leftSlidMenu.put(position, menus);
    }

    /**
     * 设置是否打开列表左边的滑动菜单
     */
    public void setLeftSlidOpen(boolean leftSlidOpen) {
        this.leftSlidOpen = leftSlidOpen;
    }

    /**
     * 设置列表右边滑动菜单的菜单 key 对应列表行号
     */
    public void setRightSlidMenu(Map<Integer, String[]> rightSlidMenu) {
        this.rightSlidMenu.clear();
        this.rightSlidMenu.putAll(rightSlidMenu);
    }

    /**
     * 设置列表右边滑动菜单 position 对应列表行号
     */
    public void setRightSlidMenu(Integer position, String... menus) {
        this.rightSlidMenu.put(position, menus);
    }

    /**
     * 是否开启列表右边的滑动菜单
     */
    public void setRightSlidOpen(boolean rightSlidOpen) {
        this.rightSlidOpen = rightSlidOpen;
    }

    /**
     * 设置列表Item单击事件监听
     */
    public void setOnItemClickInterface(OnItemClickInterface onItemClickInterface) {
        this.onItemClickInterface = onItemClickInterface;
    }

    /**
     * 设置列表Item长按事件监听
     */
    public void setOnItemLongClickInterface(OnItemLongClickInterface onItemLongClickInterface) {
        this.onItemLongClickInterface = onItemLongClickInterface;
    }

    /**
     * 自定义列表右边的滑动菜单样式
     */
    public void setOnCreateSlidMenuRightInterface(OnCreateSlidMenuRightInterface onCreateSlidMenuRightInterface) {
        this.onCreateSlidMenuRightInterface = onCreateSlidMenuRightInterface;
    }

    /**
     * 自定义列表左边滑动菜单样式
     */
    public void setOnCreateSlidMenuLeftInterface(OnCreateSlidMenuLeftInterface onCreateSlidMenuLeftInterface) {
        this.onCreateSlidMenuLeftInterface = onCreateSlidMenuLeftInterface;
    }

    /**
     * 监听滑动按钮单击事件
     */
    public void setOnCreateSlidMenuClickInterface(OnCreateSlidMenuClickInterface onCreateSlidMenuClickInterface) {
        this.onCreateSlidMenuClickInterface = onCreateSlidMenuClickInterface;
    }

    /**
     * 设置列表的方向 请在一切方法执行前调用
     */
    public void setmOrientation(int orientation) {
        setOrientation(orientation);
    }

    /**
     * 列表的最大值 超过的数据将不会被加载到界面显示 但是调用addChildView()添加的view会把第一个顶出去 以此类推 (如果没有性能要求建议不要设置)
     */
    public void setMaxItem(int maxItem) {
        this.maxItem = maxItem;
    }

    /**
     * 设置列表的长度 (这个必须设置 不然列表将不显示数据)
     */
    public void setListSize(int listSize) {
        this.mListSize = listSize;
    }

    /**
     * 生成列表的ItemView (这个方法必须调用 不然列表将不显示数据)
     */
    public void setOnCreateViewInterface(OnCreateViewInterface onCreateViewInterface) {
        this.onCreateViewInterface = onCreateViewInterface;
    }

}
