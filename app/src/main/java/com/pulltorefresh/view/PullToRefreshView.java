package com.pulltorefresh.view;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.RotateAnimation;
import android.widget.AbsListView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import com.pulltorefresh.PullToRefreshCallBack;
import com.pulltorefresh.R;

/**
 * 作者：guofeng
 * 日期:16/7/4
 * 下拉刷新
 */
public class PullToRefreshView extends ListView implements AbsListView.OnScrollListener, PullToRefreshCallBack {

    private ImageView ivStatus;
    private TextView tvLoading;
    private View headView;
    private View bottomView;
    private LinearLayout llBottom;
    private LinearLayout llArrow;
    private LinearLayout llPb;
    /**
     * 当前头部状态
     */
    private int state = MoveState.NONE;
    /**
     * 头部高度
     */
    private int height;
    /**
     * 箭头旋转最大偏移量
     */
    private final int ARROW_ROTATE = 30;
    /**
     * 起点的坐标
     */
    private int startY;
    /**
     * 上次滑动Y坐标
     */
    private int lastY;
    /**
     * 箭头旋转动画时间
     */
    private final int DURATION = 200;
    /**
     * 头部是否可见
     */
    private boolean isVisibleHeadView;
    /**
     * 是否可以下拉刷新
     */
    private boolean canPull;
    /**
     * 滑动方向
     */
    private int mTouch;
    /**
     * 向上滑动
     */
    private final int TOUCH_DOWN = 1;
    /**
     * 向下滑动
     */
    private final int TOUCH_UP = 2;
    /**
     * 底部处于正常状态
     */
    private final int BOTTOM_STATE_NORMAL = 0;
    /**
     * 底部处于加载中状态
     */
    private final int BOTTOM_STATE_LOADING = 1;
    /**
     * 是否显示底部加载更多
     */
    private int bottomState = BOTTOM_STATE_NORMAL;
    /**
     * 加载更多
     */
    private OnLoadMoreCallBack onLoadMoreCallBack;
    /***
     * 下拉刷新
     */
    private OnAutoLoadCallBack onAutoLoadCallBack;

    public PullToRefreshView(Context context) {
        super(context);
        init(context);
    }

    public PullToRefreshView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    public PullToRefreshView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context);
    }

    public void setOnLoadMoreCallBack(OnLoadMoreCallBack onLoadMoreCallBack) {
        this.onLoadMoreCallBack = onLoadMoreCallBack;
    }

    public void setOnAutoLoadCallBack(OnAutoLoadCallBack onAutoLoadCallBack) {
        this.onAutoLoadCallBack = onAutoLoadCallBack;
    }

    /**
     * 初始化
     *
     * @param context
     */

    private void init(Context context) {
        //加载头部view
        headView = LayoutInflater.from(context).inflate(R.layout.pull_to_refresh_head_view, null);
        llArrow = (LinearLayout) headView.findViewById(R.id.ll_arrow);
        llPb = (LinearLayout) headView.findViewById(R.id.ll_pb);
        ivStatus = (ImageView) headView.findViewById(R.id.iv_status);
        tvLoading = (TextView) headView.findViewById(R.id.tv_loading);
        this.addHeaderView(headView);
        //加载底部view
        bottomView = LayoutInflater.from(context).inflate(R.layout.pull_to_refresh_botton_view, null);
        llBottom = (LinearLayout) bottomView.findViewById(R.id.ll_bottom);
        this.addFooterView(bottomView);
        refreshBottom();
        //测量头部高度
        int w = View.MeasureSpec.makeMeasureSpec(0,
                View.MeasureSpec.UNSPECIFIED);
        int h = View.MeasureSpec.makeMeasureSpec(0,
                View.MeasureSpec.UNSPECIFIED);
        headView.measure(w, h);
        height = headView.getMeasuredHeight();
        setTopMargin(-height);
        setOnScrollListener(this);
    }


    /**
     * 设置距离顶部距离
     *
     * @param top
     */
    private void setTopMargin(int top) {
        if (top <= -height) {
            isVisibleHeadView = false;
        } else {
            isVisibleHeadView = true;
        }
        headView.setPadding(headView.getPaddingLeft(), top, headView.getPaddingRight(), headView.getPaddingBottom());
        headView.invalidate();
        //重置状态
        if (top <= -height) {
            state = MoveState.NONE;
        }
        //头部可见滑动情况,同步listview滚动
        setSelection(0);
    }


    @Override
    public boolean onTouchEvent(MotionEvent ev) {
        switch (ev.getAction()) {
            case MotionEvent.ACTION_DOWN:
                if (!canPull) break;
                startY = (int) ev.getY();
                lastY = startY;
                break;
            case MotionEvent.ACTION_MOVE:
                if (!canPull) break;
                //当前滑动y坐标
                int curY = (int) ev.getY();
                //滑动距离
                int distance = curY - startY;
                //距离上边距离
                int top = distance - height;
                //刷新头部状态
                changeState(top, distance);
                //判断滑动方向
                if (curY - lastY > 0) {
                    mTouch = TOUCH_DOWN;
                } else {
                    mTouch = TOUCH_UP;
                }
                //消费事件
                if (mTouch == TOUCH_UP && isVisibleHeadView && state != MoveState.LOADING) {
                    return true;
                }
                lastY = curY;
                break;
            case MotionEvent.ACTION_UP:
                if (state == MoveState.ARROW_DOWN) {
                    state = MoveState.LOADING;
                    refreshHeadView();
                } else if (state == MoveState.ARROW_UP) {
                    state = MoveState.NONE;
                    refreshHeadView();
                }
                canPull = false;
                break;
        }
        return super.onTouchEvent(ev);
    }

    private void changeState(int top, int distance) {
        switch (state) {
            //正常状态
            case MoveState.NONE:
                if (mTouch == TOUCH_DOWN) {
                    state = MoveState.ARROW_UP;
                }
                break;
            //箭头朝上
            case MoveState.ARROW_UP:
                setTopMargin(top);
                if (distance > height + ARROW_ROTATE) {
                    state = MoveState.ARROW_DOWN;
                    arrowDownAnimation();
                    refreshHeadView();
                }
                break;
            //箭头朝下
            case MoveState.ARROW_DOWN:
                setTopMargin(top);
                if (distance < height + ARROW_ROTATE) {
                    state = MoveState.ARROW_UP;
                    arrowUpAnimation();
                    refreshHeadView();
                }
                break;
        }
    }

    private void refreshHeadView() {
        switch (state) {
            //正常状态
            case MoveState.NONE:
                tvLoading.setText("下拉可以刷新");
                llPb.setVisibility(View.GONE);
                llArrow.setVisibility(View.VISIBLE);
                setTopMargin(-height);
                break;
            //箭头朝下
            case MoveState.ARROW_DOWN:
                tvLoading.setText("松开可以刷新");
                llPb.setVisibility(View.GONE);
                llArrow.setVisibility(View.VISIBLE);
                break;
            //箭头朝上
            case MoveState.ARROW_UP:
                tvLoading.setText("下拉可以刷新");
                llPb.setVisibility(View.GONE);
                llArrow.setVisibility(View.VISIBLE);
                break;
            //加载中
            case MoveState.LOADING:
                onAutoStart();
                break;
        }
    }

    @Override
    public void onScrollStateChanged(AbsListView view, int scrollState) {
    }

    @Override
    public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
        canPull = firstVisibleItem == 0;
        int lastItem = firstVisibleItem + visibleItemCount;
        //滑动到底部
        if (lastItem == totalItemCount) {
            if (onLoadMoreCallBack != null) {
                if (bottomState == BOTTOM_STATE_NORMAL) {
                    bottomState = BOTTOM_STATE_LOADING;
                    onLoadMoreCallBack.onLoadMoreListener();
                    refreshBottom();
                }
            }
        }
    }

    /**
     * 加载更多完毕
     */
    public void onLoadMoreComplete() {
        bottomState = BOTTOM_STATE_NORMAL;
        refreshBottom();
    }

    /**
     * 刷新底部
     */
    private void refreshBottom() {
        llBottom.setVisibility(bottomState == BOTTOM_STATE_LOADING ? View.VISIBLE : View.GONE);
    }

    /**
     * 当前动画累加距离
     */
    private int curSpace;
    /**
     * 动画下拉刷新频率
     */
    private final int FREQUENT = 20;

    /**
     * 加载中完毕
     */
    @Override
    public void onAutoComplete() {
        handler.sendEmptyMessageDelayed(0, FREQUENT);
    }

    final Handler handler = new Handler(Looper.myLooper()) {
        @Override
        public void handleMessage(Message msg) {
            int addHeight = height / FREQUENT;
            curSpace += addHeight;
            if (curSpace >= height) {
                curSpace = height;
                setTopMargin(-curSpace);
                curSpace = 0;
                state = MoveState.NONE;
                refreshHeadView();
            } else {
                setTopMargin(-curSpace);
                handler.sendEmptyMessageDelayed(0, FREQUENT);
            }
            super.handleMessage(msg);
        }
    };

    /**
     * 开始加载动画
     */
    @Override
    public void onAutoStart() {
        tvLoading.setText("加载中...");
        llArrow.setVisibility(View.GONE);
        llPb.setVisibility(View.VISIBLE);
        state = MoveState.LOADING;
        setTopMargin(0);
        if (onAutoLoadCallBack != null) {
            onAutoLoadCallBack.onAutoLoadListener();
        }
    }


    /**
     * 执行箭头向下动画
     */
    private void arrowDownAnimation() {
        RotateAnimation animation = new RotateAnimation(0, 180, Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f);
        animation.setDuration(DURATION);
        animation.setFillAfter(true);
        ivStatus.startAnimation(animation);

    }

    /**
     * 执行箭头向上动画
     */
    private void arrowUpAnimation() {
        RotateAnimation animation = new RotateAnimation(180, 0, Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f);
        animation.setDuration(DURATION);
        animation.setFillAfter(true);
        ivStatus.startAnimation(animation);
    }


    public interface OnLoadMoreCallBack {
        /**
         * 加载更多事件
         */
        void onLoadMoreListener();
    }

    public interface OnAutoLoadCallBack {
        /**
         * 下拉刷新事件
         */
        void onAutoLoadListener();
    }

}


