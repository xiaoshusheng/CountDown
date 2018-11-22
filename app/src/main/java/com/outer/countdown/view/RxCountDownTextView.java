package com.outer.countdown.view;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Color;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.jakewharton.rxbinding2.view.RxView;
import com.jakewharton.rxbinding2.widget.RxTextView;
import com.outer.countdown.R;

import java.util.concurrent.TimeUnit;

import io.reactivex.Observable;
import io.reactivex.ObservableSource;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Consumer;
import io.reactivex.functions.Function;
import io.reactivex.schedulers.Schedulers;


/**
 * 作者：create by YangZ on 2018/11/22 10:59
 * 邮箱：YangZL8023@163.com
 * describe:
 * 1.自定义倒计时控件
 */

public class RxCountDownTextView extends LinearLayout {
    private TextView mTvTime;

    // 提供默认的设置
    private static final String INIT_TEXT = "获取验证码";
    private static final String PREFIX_RUN_TEXT = "剩余时间";
    private static final String SUFFIX_RUN_TEXT = "秒";
    private static final String FINISH_TEXT = "点击重新获取";
    private static final int TOTAL_TIME = 10;
    private static final int COLOR = Color.RED;
    // 来自布局文件中的属性设置
    private String mInittext;// 初始化文本
    private String mPrefixRuntext;// 运行时的文本前缀
    private String mSuffixRuntext;// 运行时的文本后缀
    private String mFinishtext;// 完成倒计时后的文本显示
    private int mTotaltime;// 倒计时的总时间
    private int mColor;

    private Observable<Long> mObservableCountTime;
    private Consumer<Long> mConsumerCountTime;

    //用于主动取消订阅倒计时，或者退出当前页面。
    private Disposable mDisposable;

    private String phone;

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getPhone() {
        return phone;
    }

    /**
     * 倒计时的监听
     */
    private OnDownTimeCallBack mDownTime;

    public RxCountDownTextView(Context context) {
        this(context, null);
    }

    public RxCountDownTextView(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, -1);
    }

    public RxCountDownTextView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        View timeView = LayoutInflater.from(context).inflate(R.layout.layout_count_down_timer, this);
        mTvTime = timeView.findViewById(R.id.tv_time);
        // 1. 在布局文件中提供设置
        TypedArray ta = context.obtainStyledAttributes(attrs, R.styleable.CountDownTimerView);
        mInittext = ta.getString(R.styleable.CountDownTimerView_init_text);
        mPrefixRuntext = ta.getString(R.styleable.CountDownTimerView_prefix_run_text);
        mSuffixRuntext = ta.getString(R.styleable.CountDownTimerView_suffix_run_text);
        mFinishtext = ta.getString(R.styleable.CountDownTimerView_finish_text);
        mTotaltime = ta.getInteger(R.styleable.CountDownTimerView_total_time, TOTAL_TIME);
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
        mTvTime.setText(mInittext);
    }

    /**
     * 初始化时间
     */
    private void initTimer() {
        mObservableCountTime = RxView.clicks(mTvTime).throttleFirst(1, TimeUnit.SECONDS)
                .subscribeOn(AndroidSchedulers.mainThread())
                .flatMap(new Function<Object, ObservableSource<Boolean>>() {
                    @Override
                    public ObservableSource<Boolean> apply(Object o) throws Exception {
                        if (TextUtils.isEmpty(getPhone())) {
                            return Observable.empty();
                        }
                        return Observable.just(true);
                    }
                })
                .flatMap(new Function<Boolean, ObservableSource<Long>>() {
                    @Override
                    public ObservableSource<Long> apply(Boolean aBoolean) throws Exception {
                        //更新发送按钮的状态并初始化显现倒计时文字
                        mTvTime.setEnabled(false);
                        RxView.enabled(mTvTime).accept(false);
                        RxTextView.text(mTvTime).accept(mPrefixRuntext + mTotaltime + mSuffixRuntext);
                        //在实际操作中可以在此发送获取网络的请求
                        mDownTime.onTimerClick();

                        return Observable.interval(1, TimeUnit.SECONDS, Schedulers.io())
                                .take(mTotaltime)
                                //将递增数字替换成递减的倒计时数字
                                .map(new Function<Long, Long>() {
                                    @Override
                                    public Long apply(Long aLong) throws Exception {
                                        return mTotaltime - (aLong + 1);
                                    }
                                });
                    }
                }).observeOn(AndroidSchedulers.mainThread());

        mConsumerCountTime = new Consumer<Long>() {
            @Override
            public void accept(Long aLong) throws Exception {
                //当倒计时为 0 时，还原 btn 按钮
                if (aLong == 0) {
                    mTvTime.setEnabled(true);
                    RxView.enabled(mTvTime).accept(true);
                    RxTextView.text(mTvTime).accept(mFinishtext);
                } else {
                    mTvTime.setEnabled(false);
                    RxTextView.text(mTvTime).accept(mPrefixRuntext + aLong + mSuffixRuntext);
                }
            }
        };

        //订阅
        mDisposable = mObservableCountTime.subscribe(mConsumerCountTime);
    }

    /**
     * 重置
     */
    public void revise(){
        if (mDisposable != null && !mDisposable.isDisposed()) {
            //停止倒计时
            mDisposable.dispose();
            //重新订阅
            mDisposable = mObservableCountTime.subscribe(mConsumerCountTime);
            //按钮可点击
            try {
                RxView.enabled(mTvTime).accept(true);
                RxTextView.text(mTvTime).accept(mInittext);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * 窗口销毁时，倒计时停止
     */
    @Override
    protected void onDetachedFromWindow() {
        // TODO Auto-generated method stub
        super.onDetachedFromWindow();
        if (mDisposable != null) {
            mDisposable.dispose();
        }
    }

    /**
     * 设置初始化的文字
     *
     * @param mInittext
     */
    public RxCountDownTextView setInittext(String mInittext) {
        this.mInittext = mInittext;
        mTvTime.setText(mInittext);
        return this;
    }

    /**
     * 设置运行时的文字前缀
     *
     * @param mPrefixRuntext
     */
    public RxCountDownTextView setPrefixRuntext(String mPrefixRuntext) {
        this.mPrefixRuntext = mPrefixRuntext;
        return this;
    }

    /**
     * 设置运行时的文字后缀
     *
     * @param mSuffixRuntext
     */
    public RxCountDownTextView setSuffixRuntext(String mSuffixRuntext) {
        this.mSuffixRuntext = mSuffixRuntext;
        return this;
    }

    /**
     * 设置结束的文字
     *
     * @param mFinishtext
     * @return
     */
    public RxCountDownTextView setFinishtext(String mFinishtext) {
        this.mFinishtext = mFinishtext;
        return this;
    }

    /**
     * 设置倒计时的总时间
     *
     * @param mTotaltime
     * @return
     */
    public RxCountDownTextView setTotaltime(int mTotaltime) {
        this.mTotaltime = mTotaltime;
        return this;
    }

    /**
     * 设置默认倒计时秒数的颜色
     *
     * @param mColor
     * @return
     */
    public RxCountDownTextView setTimeColor(int mColor) {
        this.mColor = mColor;
        return this;
    }

    /**
     * 对外提供接口，编写倒计时时和倒计时完成时的操作
     */
    public interface OnDownTimeCallBack {
        void onTimerClick();
    }

    public void onDownTime(OnDownTimeCallBack downTime) {
        if (downTime == null) {
            throw new RuntimeException("无效的点击");
        }
        this.mDownTime = downTime;
    }

}
