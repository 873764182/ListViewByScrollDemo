package com.panxiong.lvbsd;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.content.ContextCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.pixel.listview.LinearListView;
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
    }

    private void getListData(int p) {
        if (p <= 1) {
            p = 1;
            listDatas.clear();
        }
        int index = p * 20;
        for (int i = index - 20; i < index; i++) {
            listDatas.add(new ItemEntity(R.mipmap.ic_launcher, "LinearListView 列表第 " + (i + 1) + " 行", System.currentTimeMillis()));
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


    /*

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
