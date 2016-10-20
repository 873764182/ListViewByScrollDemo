package com.panxiong.lvbsd;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;

import com.pixel.listview.LinearListView;

/**
 * 水平方向
 */
public class HorizontalActivity extends Activity {
    private LinearListView mLinearListView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_horizontal);

        this.initListView();
    }

    private void initListView() {
        mLinearListView = (LinearListView) findViewById(R.id.linearListView);
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
