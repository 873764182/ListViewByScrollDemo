package com.panxiong.lvbsd;

import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.pixel.listview.LinearListView;
import com.pixel.listview.inter.OnCreateSlidMenuClickInterface;
import com.pixel.listview.inter.OnCreateViewInterface;
import com.pixel.listview.inter.OnItemClickInterface;
import com.pixel.listview.inter.OnItemLongClickInterface;
import com.pixel.listview.inter.OnSlidRefreshInterface;

import java.util.Hashtable;

/**
 * 自定义ListView需求说明
 * <p/>
 * 1. 可以实现列表方向调整,支持竖直列表，横向列表
 * 2. Item划出屏幕可以不被销毁，但是必须可以设置最大Item数量。超过最大Item值时可以选择是销毁旧的Item还是不再加载更多
 * 3. 列表Item支持单击、长按操作
 * 4. 列表支持添加、删除头尾操作
 * 5. Item可以左右或者上下滑动打开相应菜单，每个Item的操作菜单可以不同，滑动菜单可以存在个别item有，个别item没有的情况
 * 6. 上下啦刷新，下拉刷新，滑动到最后一个Item自动加载更多，加载更多与下拉刷新可以手动关闭这功能
 */
public class MainActivity extends AppCompatActivity {
    private LinearListView mLinearListView;

    private int flag = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mLinearListView = (LinearListView) findViewById(R.id.linearListView);
        mLinearListView.setListSize(5);
        mLinearListView.setOnCreateViewInterface(new OnCreateViewInterface() {
            @Override
            public View getView(int position) {
                View view = getLayoutInflater().inflate(R.layout.list_item_view, null);
                TextView textView = (TextView) view.findViewById(R.id.item_text);
                textView.setText("ITEM " + (flag += 1));
                return view;
            }
        });
//        mLinearListView.addHeaderView(getLayoutInflater().inflate(R.layout.head_view, null));
//        mLinearListView.addFooterView(getLayoutInflater().inflate(R.layout.footer_view, null));

        mLinearListView.setRightSlidOpen(true);
        mLinearListView.setRightSlidMenu(new Hashtable<Integer, String[]>() {
            {
                put(0, new String[]{"menu1", "menu2", "menu3"});
                put(1, new String[]{"menu1", "menu3"});
                put(2, new String[]{"menu1"});
                put(3, new String[]{"menu1", "menu2"});
                put(5, new String[]{"menu1", "menu2", "menu3"});
            }
        });
        mLinearListView.setLeftSlidOpen(true);
        mLinearListView.setLeftSlidMenu(5, "菜单1", "菜单2", "菜单3");
        mLinearListView.setLeftSlidMenu(6, "菜单");
        mLinearListView.setOnCreateSlidMenuClickInterface(new OnCreateSlidMenuClickInterface() {
            @Override
            public void onMenuClick(int direction, View view, int position, int menuOrder, String menuName) {
                Toast.makeText(MainActivity.this, "" + direction + position + menuOrder + menuName, Toast.LENGTH_SHORT).show();
            }
        });
//        mLinearListView.setOnCreateSlidMenuLeftInterface(new OnCreateSlidMenuLeftInterface() {
//            @Override
//            public View getSlidMenuItem(View containerView, int position, int menuOrder, String menuName) {
//                return getLayoutInflater().inflate(R.layout.footer_view, null);
//            }
//        });

        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
//                mLinearListView.addHeaderView(getLayoutInflater().inflate(R.layout.head_view, null));
//                mLinearListView.addHeaderView(getLayoutInflater().inflate(R.layout.head_view, null));
//                mLinearListView.addHeaderView(getLayoutInflater().inflate(R.layout.head_view, null));
//                mLinearListView.addFooterView(getLayoutInflater().inflate(R.layout.footer_view, null));
//                mLinearListView.addFooterView(getLayoutInflater().inflate(R.layout.footer_view, null));
//                mLinearListView.addFooterView(getLayoutInflater().inflate(R.layout.footer_view, null));
                mLinearListView.refreshUiData();   // 如果是横向列表是 每次都要调用一次刷新才能出现数据 目前找不到原因
            }
        }, 2000);

        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
//                mLinearListView.refreshUiData();
//                Toast.makeText(MainActivity.this, "刷新", Toast.LENGTH_SHORT).show();
            }
        }, 5000);

        mLinearListView.setOnItemClickInterface(new OnItemClickInterface() {
            @Override
            public void onItemClick(View view, int position) {
                Toast.makeText(MainActivity.this, "onItemClick " + position, Toast.LENGTH_SHORT).show();
            }
        });
        mLinearListView.setOnItemLongClickInterface(new OnItemLongClickInterface() {
            @Override
            public boolean onItemLongClick(View view, int position) {
                Toast.makeText(MainActivity.this, "onItemLongClick " + position, Toast.LENGTH_SHORT).show();
                return true;
            }
        });

        mLinearListView.setIsOpenRefresh(true);
        mLinearListView.setIsOpenMore(true);
        mLinearListView.setOnSlidRefreshInterface(new OnSlidRefreshInterface() {
            @Override
            public void doRefresh(Context mContext, LinearListView linearListView) {
                Log.e("MainActivity", "刷新");

                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        mLinearListView.closeRefreshView();
                    }
                }, 2000);
            }

            @Override
            public void doMore(Context mContext, LinearListView linearListView) {
                Log.e("MainActivity", "加载");

                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        mLinearListView.closeRefreshView();
                    }
                }, 2000);
            }
        });
    }

    private void test() {

        ListView listView = new ListView(this);
        listView.addHeaderView(null);
        listView.addFooterView(null);

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

            }
        });

        listView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
                return false;
            }
        });
    }

}
