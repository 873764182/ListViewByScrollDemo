package com.panxiong.lvbsd;

import android.app.Activity;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.pixel.listview.LinearListView;
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
