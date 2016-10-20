package com.panxiong.lvbsd;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.content.ContextCompat;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.pixel.listview.LinearListView;
import com.pixel.listview.inter.OnCreateViewInterface;
import com.pixel.listview.inter.OnSlidRefreshInterface;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * 竖直方向
 */
public class VerticalActivity extends Activity {
    private LinearListView mLinearListView = null;
    private SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:MM:SS");

    private final List<ItemEntity> listDatas = new ArrayList<>();
    private volatile int page = 1;

    @Override

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_vertical);

        // initListData();
        initListView();
    }

    private void initListData(int p) {
        if (p <= 1) {
            p = 1;
            listDatas.clear();
        }
        int index = p * 20;
        for (int i = index - 20; i < index; i++) {
            listDatas.add(new ItemEntity(R.mipmap.ic_launcher, "列表第 " + (i + 1) + " 行", System.currentTimeMillis()));
        }
    }

    private void initListView() {
        mLinearListView = (LinearListView) findViewById(R.id.vLinearListView);
        mLinearListView.setOnCreateViewInterface(new OnCreateViewInterface() {
            @Override
            public int getCount() {
                return listDatas.size();
            }

            @Override
            public View getView(int position) { // 因为列表不会被回收 所以不用考虑重用问题
                View convertView = getLayoutInflater().inflate(R.layout.item_view_v, null);
                ViewHolder viewHolder = new ViewHolder();
                viewHolder.headUri = (ImageView) convertView.findViewById(R.id.headUri);
                viewHolder.userName = (TextView) convertView.findViewById(R.id.userName);
                viewHolder.dateTime = (TextView) convertView.findViewById(R.id.dateTime);
                viewHolder.headUri.setImageDrawable(ContextCompat.getDrawable(VerticalActivity.this, R.mipmap.ic_launcher));
                viewHolder.userName.setText(listDatas.get(position).userName);
                viewHolder.dateTime.setText(sdf.format(new Date(listDatas.get(position).dateTime)));
                return convertView;
            }
        });
        mLinearListView.setIsOpenRefresh(true);
        mLinearListView.setIsOpenMore(true);
        mLinearListView.setOnSlidRefreshInterface(new OnSlidRefreshInterface() {
            @Override
            public void doRefresh(Context mContext, LinearListView linearListView) {
                page = 1;
                initListData(page);
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        mLinearListView.closeRefreshView();
                        mLinearListView.refreshUiData();
                    }
                }, 2000);
            }

            @Override
            public void doMore(Context mContext, LinearListView linearListView) {
                page += 1;
                initListData(page);
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        mLinearListView.closeRefreshView();
                        mLinearListView.refreshUiData();
                    }
                }, 2000);
            }
        });
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


    /*
    *        mLinearListView = (LinearListView) findViewById(R.id.linearListView);
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
        mLinearListView.setLeftSlidMenu(3, "菜单1", "菜单2", "菜单3");
        mLinearListView.setLeftSlidMenu(4, "菜单");
        mLinearListView.setOnCreateSlidMenuClickInterface(new OnCreateSlidMenuClickInterface() {
            @Override
            public void onMenuClick(int direction, View view, int position, int menuOrder, String menuName) {
                Toast.makeText(_MainActivity.this, "" + direction + position + menuOrder + menuName, Toast.LENGTH_SHORT).show();
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
                //  mLinearListView.refreshUiData();
            }
        }, 2000);

        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
//                mLinearListView.refreshUiData();
//                Toast.makeText(_MainActivity.this, "刷新", Toast.LENGTH_SHORT).show();
            }
        }, 5000);

        mLinearListView.setOnItemClickInterface(new OnItemClickInterface() {
            @Override
            public void onItemClick(View view, int position) {
                Toast.makeText(_MainActivity.this, "onItemClick " + position, Toast.LENGTH_SHORT).show();
            }
        });
        mLinearListView.setOnItemLongClickInterface(new OnItemLongClickInterface() {
            @Override
            public boolean onItemLongClick(View view, int position) {
                Toast.makeText(_MainActivity.this, "onItemLongClick " + position, Toast.LENGTH_SHORT).show();
                return true;
            }
        });

        mLinearListView.setIsOpenRefresh(true);
        mLinearListView.setIsOpenMore(true);
        mLinearListView.setOnSlidRefreshInterface(new OnSlidRefreshInterface() {
            @Override
            public void doRefresh(Context mContext, LinearListView linearListView) {
                Log.e("_MainActivity", "刷新");

                mLinearListView.refreshUiData();
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        mLinearListView.closeRefreshView();
                    }
                }, 2000);
            }

            @Override
            public void doMore(Context mContext, LinearListView linearListView) {
                Log.e("_MainActivity", "加载");

                mLinearListView.refreshUiData();
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        mLinearListView.closeRefreshView();
                    }
                }, 2000);
            }
        });
    }
    * */
}
