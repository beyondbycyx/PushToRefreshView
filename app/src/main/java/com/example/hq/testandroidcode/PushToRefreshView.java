package com.example.hq.testandroidcode;

import android.content.Context;
import android.os.SystemClock;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewConfiguration;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;

/**
 * Created by hq on 2015/12/22.
 */
public class PushToRefreshView extends LinearLayout implements View.OnTouchListener {

  private static final String TAG = "PushToRefreshView ";
  /**
   * 下拉状态
   */
  public static final int STATUS_PULL_TO_REFRESH = 0;

  /**
   * 释放立即刷新状态
   */
  public static final int STATUS_RELEASE_TO_REFRESH = 1;

  /**
   * 正在刷新状态
   */
  public static final int STATUS_REFRESHING = 2;

  /**
   * 刷新完成或未刷新状态
   */
  public static final int STATUS_REFRESH_FINISHED = 3;

  /**
   * 下拉头部回滚的速度
   */
  public static final int SCROLL_SPEED = -20;



  /**
   * 正在刷新的最低时间
   */
  private   long REFRESHING_MIN_TIME = 3000;

  /**
   * 当前处理什么状态
   */
  private int currentStatus = STATUS_REFRESH_FINISHED;
  ;

  /**
   * 记录上一次的状态是什么，避免进行重复操作
   */
  private int lastStatus = currentStatus;

  private int mHideViewToMargin;
  private View mHideView;
  private int mHideViewHeight;
  private MarginLayoutParams mHideViewMarginParams;
  private int mTouchSlop;
  private boolean loadOnce; //记录第一次加载，是否曾经加载
  private ListView mListView;
  private float yDown;
  private TextView mTextView;
  private ProgressBar mProgressBar;
  private boolean ableToPull;

  public PushToRefreshView(Context context) {
    this(context, null);
  }

  public PushToRefreshView(Context context, AttributeSet attrs) {
    super(context, attrs);
    //该类的初始化操作
    setOrientation(VERTICAL);

    //添加hideview和初始化操作
    mHideView = LayoutInflater.from(context).inflate(R.layout.hide_view, null, true);
    mTextView = (TextView) mHideView.findViewById(R.id.text_view);
    mProgressBar = (ProgressBar) mHideView.findViewById(R.id.progress_bar);
    addView(mHideView, 0);

    //获取基本参数
    mTouchSlop = ViewConfiguration.get(context).getScaledTouchSlop();
  }

  /**
   * 将hideview上移到activity界面的顶部之外
   */
  @Override protected void onLayout(boolean changed, int l, int t, int r, int b) {
    super.onLayout(changed, l, t, r, b);

    //第一次进来初始化，并隐藏hideview
    if (changed && !loadOnce) {
      mHideViewHeight = mHideView.getHeight();
      mHideViewToMargin = -mHideViewHeight;

      //改变hideview的topmargin
      mHideViewMarginParams = (MarginLayoutParams) mHideView.getLayoutParams();
      mHideViewMarginParams.topMargin = mHideViewToMargin;

      //只在listview 上进行监听操作，
      mListView = ((ListView) getChildAt(1));
      mListView.setOnTouchListener(this);
      loadOnce = true;
      //不再进入该代码块
      loadOnce = true;
    }
  }

  @Override public boolean onTouch(View v, MotionEvent event) {

    setIsAbleToPull(event);
    if (ableToPull) {
      switch (event.getAction()) {
        case MotionEvent.ACTION_DOWN:
          yDown = (int) (event.getRawY() + 0.5f);
          Log.v(TAG, "onTouch ,yDown = " + yDown);
          break;
        case MotionEvent.ACTION_MOVE:
          //移动操作，响应用户的移动手势并一起更新UI
          int yMove = (int) (event.getRawY() - yDown + 0.5f);
          Log.v(TAG, "onTouch ,yMove = " + yMove);

          // 如果手指是下滑状态，并且下拉头是完全隐藏的，就屏蔽下拉事件
          if (yMove <= 0 && mHideViewMarginParams.topMargin <= (-mHideViewHeight)) {
            return false;
          }
          if (yMove < mTouchSlop) {
            return false;
          }

          if (STATUS_REFRESHING != currentStatus) {

            //1.改变 currentStatus 的状态
            if (mHideViewMarginParams.topMargin > 0) {
              //释放刷新操作--针对hideview内部的显示内容情况
              currentStatus = STATUS_RELEASE_TO_REFRESH;
            } else {
              //下拉刷新操作--针对hideview内部的显示内容情况
              currentStatus = STATUS_PULL_TO_REFRESH;
            }

            //2.通过偏移下拉头的topMargin值，使整个hidevie 往下移动--！！！
            mHideViewMarginParams.topMargin = (yMove / 2) + (-mHideViewHeight);
            mHideView.setLayoutParams(mHideViewMarginParams);
          }
          Log.v(TAG, "移动中，currentStatus = " + currentStatus);
          break;
        case MotionEvent.ACTION_UP:
          Log.v(TAG, "释放触摸点，currentStatus = " + currentStatus);
          //触摸点释放,由代码动态完成界面的效果
        default:
          if (STATUS_RELEASE_TO_REFRESH == currentStatus) {
            // 松手时如果是释放立即刷新状态，就去调用正在刷新的任务
            post(new RefreshingTask());
          } else if (STATUS_PULL_TO_REFRESH == currentStatus) {
            // 松手时如果是下拉状态，就去调用隐藏下拉头的任务
            post(new HideHeaderTask());
          }
          break;
      }

      //根据状态值，进行各种显示
      if (STATUS_PULL_TO_REFRESH == currentStatus) {

        // 当前正处于下拉或释放状态，要让ListView失去焦点，否则被点击的那一项会一直处于选中状态
        mListView.setPressed(false);
        mListView.setFocusable(false);
        mListView.setFocusableInTouchMode(false);
        lastStatus = currentStatus;

        //下拉显示
        showPullRefresh();
      }

      if (STATUS_RELEASE_TO_REFRESH == currentStatus) {
        // 当前正处于下拉或释放状态，要让ListView失去焦点，否则被点击的那一项会一直处于选中状态
        mListView.setPressed(false);
        mListView.setFocusable(false);
        mListView.setFocusableInTouchMode(false);

        //释放刷新显示
        showReleaseRefresh();
      }

      if (STATUS_REFRESHING == currentStatus) {
        //正在刷新操作

      }

      if (STATUS_REFRESH_FINISHED == currentStatus) {
        //刷新完毕
        Log.v(TAG, "刷新完毕，currentStatus = " + currentStatus);
      }

      return true;
    }

    return false; //必须返回true,否则down事件的move等后续事件无法接受到并执行。
  }

  private void showReleaseRefresh() {
    mTextView.setVisibility(VISIBLE);
    mTextView.setText("释放刷新");
    mProgressBar.setVisibility(INVISIBLE);
  }

  private void showPullRefresh() {
    mTextView.setVisibility(VISIBLE);
    mTextView.setText("下拉刷新");
    mProgressBar.setVisibility(INVISIBLE);
  }
  /**
   * 根据当前ListView的滚动状态来设定 {@link #ableToPull}
   * 的值，每次都需要在onTouch中第一个执行，这样可以判断出当前应该是滚动ListView，还是应该进行下拉。
   *
   * @param event
   */
  private void setIsAbleToPull(MotionEvent event) {
    View firstChild = mListView.getChildAt(0);
    if (firstChild != null) {
      int firstVisiblePos = mListView.getFirstVisiblePosition();
      if (firstVisiblePos == 0 && firstChild.getTop() == 0) {
        if (!ableToPull) {
          yDown = event.getRawY();
        }
        // 如果首个元素的上边缘，距离父布局值为0，就说明ListView滚动到了最顶部，此时应该允许下拉刷新
        ableToPull = true;
      } else {
        if (mHideViewMarginParams.topMargin != (-mHideViewHeight)) {
          mHideViewMarginParams.topMargin = -mHideViewHeight;
          mHideView.setLayoutParams(mHideViewMarginParams);
        }
        ableToPull = false;
      }
    } else {
      // 如果ListView中没有元素，也应该允许下拉刷新
      ableToPull = true;
    }
  }
  //动态隐藏头部的操作
  private class HideHeaderTask implements Runnable {

    @Override public void run() {
      int topMargin = mHideViewMarginParams.topMargin;
      while (true) {
        topMargin += SCROLL_SPEED;

        SystemClock.sleep(20);

        if (topMargin < -mHideViewHeight) {
          topMargin = -mHideViewHeight;
          refreshHideView(topMargin);
          break;
        }
        refreshHideView(topMargin);
      }
      //已经隐藏完了头部，状态值变为结束
      currentStatus = STATUS_REFRESH_FINISHED;
    }

    private void refreshHideView(int topMargin) {
      mHideViewMarginParams.topMargin = topMargin;
      mHideView.setLayoutParams(mHideViewMarginParams);
    }
  }

  /**
   * 动态进行刷新操作
   */
  private class RefreshingTask implements Runnable {

    @Override public void run() {

      //1.上移恢复原位

      int topMargin = mHideViewMarginParams.topMargin;
      while (true) {
        topMargin += SCROLL_SPEED;

        SystemClock.sleep(10);

        if (topMargin < 0) {
          topMargin = 0;
          refreshHideView(topMargin);

          break;
        }
        refreshHideView(topMargin);
      }
       //2.再进行刷新显示操作
      currentStatus = STATUS_REFRESHING;
      showRefresh();

      //3.取消刷新(隐藏头部)
      cancelRefresh(REFRESHING_MIN_TIME);


    }
    private void refreshHideView(int topMargin) {
      mHideViewMarginParams.topMargin = topMargin;
      mHideView.setLayoutParams(mHideViewMarginParams);
    }

    private void showRefresh() {
      mTextView.setVisibility(INVISIBLE);
      mProgressBar.setVisibility(VISIBLE);

    }
  }

  /**
   * 下列方法由外部调用
   * 由外部调用停掉真正刷新的显示
   */
  public void cancelRefresh() {
    cancelRefresh(0);
  }

  public void cancelRefresh(long time) {
    postDelayed(new Runnable() {
      @Override public void run() {
        Log.v(TAG, "已经暂停了3 秒");
        new HideHeaderTask().run();
      }
    }, time);
  }

  public  void setRefreshingMinTime(long refreshingMinTime) {
    REFRESHING_MIN_TIME = refreshingMinTime;
  }

}
