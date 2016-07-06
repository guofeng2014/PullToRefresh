package com.pulltorefresh;

import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Toast;

import com.pulltorefresh.view.PullToRefreshView;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity implements PullToRefreshView.OnLoadMoreCallBack, AdapterView.OnItemClickListener, PullToRefreshView.OnAutoLoadCallBack {

    private List<String> list = new ArrayList<>();
    private PullToRefreshView lv;
    private ListAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        lv = (PullToRefreshView) findViewById(R.id.lv);
        lv.setOnLoadMoreCallBack(this);
        lv.setOnAutoLoadCallBack(this);
        lv.setOnItemClickListener(this);
        adapter = new ListAdapter(this);
        lv.setAdapter(adapter);
        getData();
        adapter.setData(list);
        //下拉刷新
        lv.onAutoStart();
    }

    private void getData() {
        for (int i = 0; i < 10; i++) {
            int size = list.size();
            list.add("测试数据" + (size + 1));
        }
    }

    /**
     * 下拉刷新
     */
    @Override
    public void onAutoLoadListener() {
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                lv.onAutoComplete();
            }
        }, 5000);
    }

    /**
     * 加载更多逻辑
     */
    @Override
    public void onLoadMoreListener() {
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                getData();
                adapter.setData(list);
                lv.onLoadMoreComplete();
                //只可以加载更多一次
                lv.setOnLoadMoreCallBack(null);
            }
        }, 5000);
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        String item = (String) parent.getItemAtPosition(position);
        if (!TextUtils.isEmpty(item)) {
            Toast.makeText(this, "点击了" + item, Toast.LENGTH_LONG).show();
        }
    }

}
