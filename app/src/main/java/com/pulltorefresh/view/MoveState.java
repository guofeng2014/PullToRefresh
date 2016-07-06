package com.pulltorefresh.view;

/**
 * 作者：guofeng
 * ＊ 日期:16/7/5
 */
public interface MoveState {
    /**
     * 头部隐藏状态
     */
    int NONE = 0;
    /**
     * 箭头朝下
     */
    int ARROW_DOWN = 1;
    /**
     * 箭头朝上
     */
    int ARROW_UP = 2;
    /**
     * 显示加载中
     */
    int LOADING = 3;
}
