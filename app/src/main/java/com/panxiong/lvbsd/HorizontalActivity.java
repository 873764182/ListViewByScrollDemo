package com.panxiong.lvbsd;

import android.app.Activity;
import android.os.Bundle;
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

import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;

/**
 * 水平方向
 */
public class HorizontalActivity extends Activity {
    private LinearListView mLinearListView;

    private final List<ItemEntity> listDatas = new ArrayList<>();
    private final Map<Integer, View> listViews = new Hashtable<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_horizontal);

        this.getListData();
        this.initListView();
    }

    private void getListData() {
        for (int i = 0; i < 20; i++) {
            listDatas.add(new ItemEntity(R.mipmap.bg_h_item, "异世界 -> " + (i + 1)));
        }
    }

    private void initListView() {
        mLinearListView = (LinearListView) findViewById(R.id.linearListView);
        mLinearListView.setOnCreateViewInterface(new OnCreateViewInterface() {
            @Override
            public int getCount() {
                return listDatas.size();
            }

            @Override
            public View getView(LayoutInflater inflater, LinearLayout parentView, int position) {
                View convertView = listViews.get(position);
                ViewHolder viewHolder = null;
                if (convertView == null) {
                    convertView = inflater.inflate(R.layout.item_view_h, parentView, false);
                    viewHolder = new ViewHolder();
                    viewHolder.iamgeView = (ImageView) convertView.findViewById(R.id.iamge_view);
                    viewHolder.textView = (TextView) convertView.findViewById(R.id.text_view);
                    convertView.setTag(viewHolder);
                    listViews.put(position, convertView);
                } else {
                    viewHolder = (ViewHolder) convertView.getTag();
                }
                viewHolder.iamgeView.setBackgroundResource(listDatas.get(position).imageId);
                viewHolder.textView.setText(listDatas.get(position).imageName);
                return convertView;
            }
        });
        mLinearListView.setLeftSlidOpen(true);
        mLinearListView.setLeftSlidMenu(0, "顶部菜单");
        mLinearListView.setLeftSlidMenu(2, "顶部菜单");
        mLinearListView.setOnCreateSlidMenuLeftInterface(new OnCreateSlidMenuLeftInterface() {
            @Override
            public View getSlidMenuItem(LayoutInflater inflater, ViewGroup containerView, int position, int menuSize, int menuOrder, String menuName) {
                View slidMenu = inflater.inflate(R.layout.menu_slid_button_h, null);
                TextView textView = (TextView) slidMenu.findViewById(R.id.slidMenu_h);
                textView.setText(menuName);
                return slidMenu;
            }
        });
        mLinearListView.setRightSlidOpen(true);
        mLinearListView.setRightSlidMenu(0, "底部菜单");
        mLinearListView.setRightSlidMenu(1, "底部菜单");
        mLinearListView.setOnCreateSlidMenuRightInterface(new OnCreateSlidMenuRightInterface() {
            @Override
            public View getSlidMenuItem(LayoutInflater inflater, ViewGroup containerView, int position, int menuSize, int menuOrder, String menuName) {
                View slidMenu = inflater.inflate(R.layout.menu_slid_button_h, null);
                TextView textView = (TextView) slidMenu.findViewById(R.id.slidMenu_h);
                textView.setText(menuName);
                return slidMenu;
            }
        });
        mLinearListView.setOnCreateSlidMenuClickInterface(new OnCreateSlidMenuClickInterface() {
            @Override
            public void onMenuClick(int direction, View view, int position, int menuOrder, String menuName) {
                showToast("方向(0.左,1.右): " + direction + "\n按钮下标: " + position + "\n按钮名称: " + menuName);
            }
        });
    }

    private void showToast(String msg) {
        Toast.makeText(this, msg + "", Toast.LENGTH_SHORT).show();
    }

    static class ItemEntity {
        int imageId = 0;
        String imageName = "";

        public ItemEntity(int imageId, String imageName) {
            this.imageId = imageId;
            this.imageName = imageName;
        }
    }

    static class ViewHolder {
        ImageView iamgeView;
        TextView textView;
    }

    // 添加列表Head
    public void addHead(View view) {
        mLinearListView.addHeaderView(getLayoutInflater().inflate(R.layout.head_view_h, null));
    }

    // 删除列表Head
    public void delHead(View view) {
        mLinearListView.removeHeaderView(0);
    }

    // 添加列表Foot
    public void addFoot(View view) {
        mLinearListView.addFooterView(getLayoutInflater().inflate(R.layout.footer_view_h, null));
    }

    // 删除列表Foot
    public void delFoot(View view) {
        mLinearListView.removeFooterView(0);
    }
}
