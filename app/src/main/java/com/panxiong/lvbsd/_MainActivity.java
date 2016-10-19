package com.panxiong.lvbsd;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;

/**
 * 自定义ListView需求说明
 * <p/>
 * 1. 可以实现列表方向调整,支持竖直列表，横向列表
 * 2. Item划出屏幕可以不被销毁，但是必须可以设置最大Item数量。超过最大Item值时可以选择是销毁旧的Item还是不再加载更多
 * 3. 列表Item支持单击、长按操作
 * 4. 列表支持添加、删除头尾操作
 * 5. Item可以左右或者上下滑动打开相应菜单，每个Item的操作菜单可以不同，滑动菜单可以存在个别item有，个别item没有的情况
 * 6. 上下拉刷新，下刷新，滑动到最后一个Item自动加载更多，加载更多与下拉刷新可以手动关闭这功能
 */
public class _MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
    }

    public void toVertical(View view) {
        startActivity(new Intent(this, VerticalActivity.class));
    }

    public void toHorizontal(View view) {
        startActivity(new Intent(this, HorizontalActivity.class));
    }

}
