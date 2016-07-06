package com.pulltorefresh;

/**
 * 作者：guofeng
 * ＊ 日期:16/7/5
 */
public interface PullToRefreshCallBack {
    /**
     * 加载完成
     */
    void onAutoComplete();

    /**
     * 开始加载
     */
    void onAutoStart();
}
