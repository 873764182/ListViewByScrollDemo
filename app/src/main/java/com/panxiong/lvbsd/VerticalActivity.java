package com.panxiong.lvbsd;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.content.ContextCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.pixel.listview.LinearListView;
import com.pixel.listview.inter.OnCreateSlidMenuClickInterface;
import com.pixel.listview.inter.OnCreateSlidMenuLeftInterface;
import com.pixel.listview.inter.OnCreateSlidMenuRightInterface;
import com.pixel.listview.inter.OnCreateViewInterface;
import com.pixel.listview.inter.OnItemClickInterface;
import com.pixel.listview.inter.OnItemLongClickInterface;
import com.pixel.listview.inter.OnSlidRefreshInterface;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;

/**
 * 竖直方向
 */
public class VerticalActivity extends Activity {
    private LinearListView mLinearListView = null;
    private SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:MM:SS");

    private final List<ItemEntity> listDatas = new ArrayList<>();
    private final Map<Integer, View> listViews = new Hashtable<>();
    private volatile int page = 1;

    @Override

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_vertical);

        this.initListView();
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                showToast("请下拉刷新加载列表数据");
            }
        }, 2000);
    }

    private void initListView() {
        // 获取布局文件里声明的控件
        mLinearListView = (LinearListView) findViewById(R.id.vLinearListView);
        // 限制列表最大行数 (不推荐限制)
        // mLinearListView.setMaxItem(10000);
        // 设置列表数据
        mLinearListView.setOnCreateViewInterface(new OnCreateViewInterface() {
            @Override
            public int getCount() {
                return listDatas.size();    // 返回列表行数
            }

            // 返回列表Item View 因为列表不会被回收 所以不用考虑重用问题 但调用refreshUiData时还是会重新生成 建议用一个List来保存View
            @Override
            public View getView(LayoutInflater inflater, LinearLayout parentView, int position) {
                View convertView = listViews.get(position);
                ViewHolder viewHolder = null;
                if (convertView == null) {
                    convertView = inflater.inflate(R.layout.item_view_v, parentView, false);
                    viewHolder = new ViewHolder();
                    viewHolder.headUri = (ImageView) convertView.findViewById(R.id.headUri);
                    viewHolder.userName = (TextView) convertView.findViewById(R.id.userName);
                    viewHolder.dateTime = (TextView) convertView.findViewById(R.id.dateTime);
                    convertView.setTag(viewHolder);
                    listViews.put(position, convertView);
                } else {
                    viewHolder = (ViewHolder) convertView.getTag();
                }
                // View可以被缓存 但数据必须要更新
                viewHolder.headUri.setImageDrawable(ContextCompat.getDrawable(VerticalActivity.this, R.mipmap.ic_launcher));
                viewHolder.userName.setText(listDatas.get(position).userName);
                viewHolder.dateTime.setText(sdf.format(new Date(listDatas.get(position).dateTime)));
                return convertView;
            }
        });
        // 打开下拉刷新
        mLinearListView.setIsOpenRefresh(true);
        // 打开上拉加载
        mLinearListView.setIsOpenMore(true);
        // 监听上/下拉刷新事件
        mLinearListView.setOnSlidRefreshInterface(new OnSlidRefreshInterface() {
            @Override
            public void doRefresh(Context mContext, LinearListView linearListView) {    // 下拉刷新回调
                page = 1;
                getListData(page);
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        mLinearListView.closeRefreshView();
                        mLinearListView.refreshUiData();
                    }
                }, 2000);
            }

            @Override
            public void doMore(Context mContext, LinearListView linearListView) {   // 上拉加载回调
                page += 1;
                getListData(page);
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        mLinearListView.closeRefreshView();
                        mLinearListView.refreshUiData();
                    }
                }, 2000);
            }
        });
//        mLinearListView.setiSlidHeadRefreshView();    // 自定义下拉刷新的头部与触发刷新事件
//        mLinearListView.setISlidFootRefreshView();    // 自定义上拉加载的尾部与触发加载条件
        // 设置列表Item单击事件
        mLinearListView.setOnItemClickInterface(new OnItemClickInterface() {
            @Override
            public void onItemClick(View view, int position) {
                showToast("单击了列表第 " + position + " 行");
            }
        });
        // 设置列表Item长按事件
        mLinearListView.setOnItemLongClickInterface(new OnItemLongClickInterface() {
            @Override
            public boolean onItemLongClick(View view, int position) {
                showToast("长按了列表第 " + position + " 行");
                return true;
            }
        });
        // 打开列表Item左边的滑动菜单 默认是关闭的
        mLinearListView.setLeftSlidOpen(true);
        mLinearListView.setLeftSlidMenu(new Hashtable<Integer, String[]>() {    // 同时设定多个
            {
                put(2, new String[]{"百度", "360"});
                put(3, new String[]{"网易", "淘宝"});
                put(4, new String[]{"天猫"});
            }
        });
        mLinearListView.setLeftSlidMenu(1, "企鹅", "微信"); // 单个设置列表Item左边菜单 这个方法必须在setLeftSlidMenu之后调用
        // 自定义列表Item的左边滑动按钮样式 按钮View的大小必须固定大小(和列表Item一样高) 不然有可能显示异常
        mLinearListView.setOnCreateSlidMenuLeftInterface(new OnCreateSlidMenuLeftInterface() {
            @Override
            public View getSlidMenuItem(LayoutInflater inflater, ViewGroup containerView, int position, int menuSize, int menuOrder, String menuName) {
                View slidMenu = null;
                if (menuOrder == 0) {
                    slidMenu = inflater.inflate(R.layout.menu_slid_button_red, null);
                    TextView textView = (TextView) slidMenu.findViewById(R.id.slidMenu_r);
                    textView.setText(menuName);
                } else if (menuOrder == 1) {
                    slidMenu = inflater.inflate(R.layout.menu_slid_button_yellow, null);
                    TextView textView = (TextView) slidMenu.findViewById(R.id.slidMenu_y);
                    textView.setText(menuName);
                } else {
                    slidMenu = inflater.inflate(R.layout.menu_slid_button_grey, null);
                    TextView textView = (TextView) slidMenu.findViewById(R.id.slidMenu_g);
                    textView.setText(menuName);
                }
                return slidMenu;
            }
        });
        // 打开列表Item右边的滑动菜单 默认是关闭的
        mLinearListView.setRightSlidOpen(true);
        mLinearListView.setRightSlidMenu(new Hashtable<Integer, String[]>() {    // 同时设定多个
            {
                put(4, new String[]{"京东"});
                put(5, new String[]{"支付宝"});
                put(6, new String[]{"今日头条", "简书"});
            }
        });
        mLinearListView.setRightSlidMenu(1, "CF", "LOL", "删除"); // 单个设置列表Item右边菜单 这个方法必须在setRightSlidMenu之后调用
        // 自定义列表Item的右边滑动按钮样式 按钮View的大小必须固定大小(和列表Item一样高) 不然有可能显示异常
        mLinearListView.setOnCreateSlidMenuRightInterface(new OnCreateSlidMenuRightInterface() {
            @Override
            public View getSlidMenuItem(LayoutInflater inflater, ViewGroup containerView, int position, int menuSize, int menuOrder, String menuName) {
                View slidMenu = null;
                if (menuOrder == 0) {
                    slidMenu = inflater.inflate(R.layout.menu_slid_button_grey, null);
                    TextView textView = (TextView) slidMenu.findViewById(R.id.slidMenu_g);
                    textView.setText(menuName);
                } else if (menuOrder == 1) {
                    slidMenu = inflater.inflate(R.layout.menu_slid_button_yellow, null);
                    TextView textView = (TextView) slidMenu.findViewById(R.id.slidMenu_y);
                    textView.setText(menuName);
                } else {
                    slidMenu = inflater.inflate(R.layout.menu_slid_button_red, null);
                    TextView textView = (TextView) slidMenu.findViewById(R.id.slidMenu_r);
                    textView.setText(menuName);
                }
                return slidMenu;
            }
        });
        // 监听列表Item滑动按钮点击
        mLinearListView.setOnCreateSlidMenuClickInterface(new OnCreateSlidMenuClickInterface() {
            @Override
            public void onMenuClick(int direction, View view, int position, int menuOrder, String menuName) {
                showToast("方向(0.左,1.右): " + direction + "\n按钮下标: " + position + "\n按钮名称: " + menuName);
            }
        });
    }

    private void getListData(int p) {
        if (p <= 1) {
            p = 1;
            listDatas.clear();
        }
        int index = p * 20;
        for (int i = index - 20; i < index; i++) {
            listDatas.add(new ItemEntity(R.mipmap.ic_launcher, "LinearListView ---> 列表第 " + (i + 1) + " 行", System.currentTimeMillis()));
        }
    }

    private void showToast(String msg) {
        Toast.makeText(this, msg + "", Toast.LENGTH_SHORT).show();
    }

    static class ItemEntity {
        Integer headUri = 0;
        String userName = "";
        Long dateTime = 0L;

        public ItemEntity(int headUri, String userName, long dateTime) {
            this.headUri = headUri;
            this.userName = userName;
            this.dateTime = dateTime;
        }
    }

    static class ViewHolder {
        ImageView headUri;
        TextView userName;
        TextView dateTime;
    }

    // 添加列表Head
    public void addHead(View view) {
        mLinearListView.addHeaderView(getLayoutInflater().inflate(R.layout.head_view, null));
    }

    // 删除列表Head
    public void delHead(View view) {
        mLinearListView.removeHeaderView(0);
    }

    // 添加列表Foot
    public void addFoot(View view) {
        mLinearListView.addFooterView(getLayoutInflater().inflate(R.layout.footer_view, null));
    }

    // 删除列表Foot
    public void delFoot(View view) {
        mLinearListView.removeFooterView(0);
    }

}
