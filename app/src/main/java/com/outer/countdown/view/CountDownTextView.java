package com.outer.countdown.view;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Color;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.Nullable;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.TextUtils;
import android.text.style.ForegroundColorSpan;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.widget.TextView;

import com.outer.countdown.R;

import java.text.DecimalFormat;
import java.util.Timer;
import java.util.TimerTask;

/**
 * 作者：create by YangZ on 2018/11/22 10:59
 * 邮箱：YangZL8023@163.com
 * describe:
 * 1.自定义倒计时控件
 */

@SuppressLint("AppCompatCustomView")
public class CountDownTextView extends TextView {
    // handler的Message
    private static final int COUNT_TIME = 1;
    // 提供默认的设置
    private static final String INIT_TEXT = "获取验证码";
    private static final String PREFIX_RUN_TEXT = "剩余时间";
    private static final String SUFFIX_RUN_TEXT = "秒";
    private static final String FINISH_TEXT = "点击重新获取";
    private static final int TOTAL_TIME = 60 * 1000;
    private static final int ONE_TIME = 1000;
    private static final int COLOR = Color.RED;
    // 来自布局文件中的属性设置
    private String mInittext;// 初始化文本
    private String mPrefixRuntext;// 运行时的文本前缀
    private String mSuffixRuntext;// 运行时的文本后缀
    private String mFinishtext;// 完成倒计时后的文本显示
    private int mTotaltime;// 倒计时的总时间
    private int mOnetime;// 一次时间
    private int mColor;
    // 实际使用的总时间
    private int mAllTotalTime;
    // 判断是否在倒计时中，防止多次点击
    private boolean isRun;
    // 是否允许倒计时
    private boolean isAllowRun;

    // 处理倒计时的方法
    private Timer mTimer;
    private TimerTask mTimerTask;

    /**
     * 倒计时的监听
     */
    private OnDownTime mDownTime;

    @SuppressLint("HandlerLeak")
    private Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case COUNT_TIME:
                    // 对秒数进行格式化
                    DecimalFormat df = new DecimalFormat("#00");
                    String strTotaltime = df.format(mAllTotalTime / 1000);
                    String runtimeText = mPrefixRuntext + strTotaltime + mSuffixRuntext;

                    // 对秒数进行颜色设置
                    Spannable spannable = new SpannableString(runtimeText);
                    ForegroundColorSpan redSpan = new ForegroundColorSpan(mColor);
                    spannable.setSpan(redSpan, mPrefixRuntext.length(), mPrefixRuntext.length() + strTotaltime.length(),
                            Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);

                    CountDownTextView.this.setText(spannable);
                    mAllTotalTime -= mOnetime;
//                    mDownTime.onDown();
                    if (mAllTotalTime < 0) {
                        CountDownTextView.this.setText(mFinishtext);
                        isRun = false;
                        clearTimer();
                        mDownTime.onFinish();
                    }
                    break;
                default:
                    break;
            }
        }
    };

    public CountDownTextView(Context context) {
        this(context, null);
    }

    public CountDownTextView(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, -1);
    }

    public CountDownTextView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        // 1. 在布局文件中提供设置
        TypedArray ta = context.obtainStyledAttributes(attrs, R.styleable.CountDownTimerView);
        mInittext = ta.getString(R.styleable.CountDownTimerView_init_text);
        mPrefixRuntext = ta.getString(R.styleable.CountDownTimerView_prefix_run_text);
        mSuffixRuntext = ta.getString(R.styleable.CountDownTimerView_suffix_run_text);
        mFinishtext = ta.getString(R.styleable.CountDownTimerView_finish_text);
        mTotaltime = ta.getInteger(R.styleable.CountDownTimerView_total_time, TOTAL_TIME);
        mOnetime = ta.getInteger(R.styleable.CountDownTimerView_one_time, ONE_TIME);
        mColor = ta.getColor(R.styleable.CountDownTimerView_time_color, COLOR);
        ta.recycle();
        // 2.代码设置值
        // 3.如果布局和代码都没有设置，则给予默认值
        initData();
        initTimer();
    }
    /**
     * 初始化数据
     */
    private void initData() {
        // 如果为空，则设置默认的值
        if (TextUtils.isEmpty(mInittext)) {
            mInittext = INIT_TEXT;
        }
        if (TextUtils.isEmpty(mPrefixRuntext)) {
            mPrefixRuntext = PREFIX_RUN_TEXT;
        }
        if (TextUtils.isEmpty(mSuffixRuntext)) {
            mSuffixRuntext = SUFFIX_RUN_TEXT;
        }
        if (TextUtils.isEmpty(mFinishtext)) {
            mFinishtext = FINISH_TEXT;
        }
        if (mTotaltime < 0) {
            mTotaltime = TOTAL_TIME;
        }
        if (mOnetime < 0) {
            mOnetime = ONE_TIME;
        }
        CountDownTextView.this.setText(mInittext);
    }

    /**
     * 初始化时间
     */
    private void initTimer() {
        mAllTotalTime = mTotaltime;
        mTimer = new Timer();
        mTimerTask = new TimerTask() {

            @Override
            public void run() {
                mHandler.sendEmptyMessage(COUNT_TIME);
            }
        };
    }

    @SuppressLint("ClickableViewAccessibility")
    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (event.getAction() == MotionEvent.ACTION_DOWN) {
            if (isAllowRun) {
                if (!isRun) {
                    // 每次开始倒计时时初始化
                    initTimer();
                    //触发点击操作
                    mDownTime.onDown();
                    // 倒计时任务启动
                    mTimer.schedule(mTimerTask, 0, mOnetime);
                    isRun = true;
                }
            }
        }
        return true;
    }

    /**
     * 清除时间
     */
    public void clearTimer() {
        if (mTimerTask != null) {
            mTimerTask.cancel();
            mTimerTask = null;
        }
        if (mTimer != null) {
            mTimer.cancel();
            mTimer = null;
        }
    }

    /**
     * 模拟onTouchEvent事件
     */
    public void mockOnDown() {
        if (isAllowRun) {
            if (!isRun) {
                // 每次开始倒计时时初始化
                initTimer();
                //触发点击操作
                mDownTime.onDown();
                // 倒计时任务启动
                mTimer.schedule(mTimerTask, 0, mOnetime);
                isRun = true;
            }
        }
    }

    /**
     * 设置初始化的文字
     *
     * @param mInittext
     */
    public CountDownTextView setInittext(String mInittext) {
        this.mInittext = mInittext;
        CountDownTextView.this.setText(mInittext);
        return this;
    }

    /**
     * 设置运行时的文字前缀
     *
     * @param mPrefixRuntext
     */
    public CountDownTextView setPrefixRuntext(String mPrefixRuntext) {
        this.mPrefixRuntext = mPrefixRuntext;
        return this;
    }

    /**
     * 设置运行时的文字后缀
     *
     * @param mSuffixRuntext
     */
    public CountDownTextView setSuffixRuntext(String mSuffixRuntext) {
        this.mSuffixRuntext = mSuffixRuntext;
        return this;
    }

    /**
     * 设置结束的文字
     *
     * @param mFinishtext
     * @return
     */
    public CountDownTextView setFinishtext(String mFinishtext) {
        this.mFinishtext = mFinishtext;
        return this;
    }

    /**
     * 设置倒计时的总时间
     *
     * @param mTotaltime
     * @return
     */
    public CountDownTextView setTotaltime(int mTotaltime) {
        this.mTotaltime = mTotaltime;
        return this;
    }

    /**
     * 设置一次倒计时的时间
     *
     * @param mOnetime
     * @return
     */
    public CountDownTextView setOnetime(int mOnetime) {
        this.mOnetime = mOnetime;
        return this;
    }

    /**
     * 设置默认倒计时秒数的颜色
     *
     * @param mColor
     * @return
     */
    public CountDownTextView setTimeColor(int mColor) {
        this.mColor = mColor;
        return this;
    }

    /**
     * 对外提供接口，编写倒计时时和倒计时完成时的操作
     */
    public interface OnDownTime {
        void onDown();

        void onFinish();
    }

    public void onDownTime(OnDownTime downTime) {
        if (downTime == null) {
            throw new RuntimeException("无效的点击");
        }
        this.mDownTime = downTime;
    }

    /**
     * 窗口销毁时，倒计时停止
     */
    @Override
    protected void onDetachedFromWindow() {
        // TODO Auto-generated method stub
        super.onDetachedFromWindow();
        clearTimer();
    }

    /**
     * 是否允许倒计时
     */
    public void isAllowRun(Boolean isAllowRun) {
        this.isAllowRun = isAllowRun;
    }

    /**
     * 重置倒计时
     */
    public void setRun(boolean run) {
        isRun = run;
    }
}
