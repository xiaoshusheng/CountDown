package com.outer.countdown;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.outer.countdown.view.CountDownTextView;
import com.outer.countdown.view.RxCountDownTextView;

public class MainActivity extends AppCompatActivity implements View.OnClickListener, CountDownTextView.OnDownTime, RxCountDownTextView.OnDownTimeCallBack {

    private CountDownTextView mCdtTimer;
    private RxCountDownTextView mRtTimer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mCdtTimer = findViewById(R.id.cdt_timer);
        mRtTimer = findViewById(R.id.rt_timer);
        TextView mTvOne = findViewById(R.id.tv_one);
        TextView mTvTwo = findViewById(R.id.tv_two);

        mTvOne.setOnClickListener(this);
        mTvTwo.setOnClickListener(this);
        mRtTimer.onDownTime(this);

        //第二张模拟手机号不为空
        mRtTimer.setPhone("123");

        timerClick(true);
        begin();
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.tv_one:
                Toast.makeText(MainActivity.this, "重置", Toast.LENGTH_SHORT).show();
                resettingTimer();
                break;
            case R.id.tv_two:
                Toast.makeText(MainActivity.this, "重置", Toast.LENGTH_SHORT).show();
                mRtTimer.revise();
                break;
        }
    }

    private void begin() {
        /**
         * 发送验证码
         */
        mCdtTimer.setRun(false);
        //设置总时间
        mCdtTimer.setInittext("发送验证短信").setTotaltime(Constant.SMS_DEADLINE)
                .setSuffixRuntext("秒").setTimeColor(getResources().getColor(R.color.color_FFFFFF));
        //对于该控件的监听
        mCdtTimer.onDownTime(this);
    }

    @Override
    public void onDown() {
        /*
            点击后的操作逻辑...........
         */

        //不可点击
        timerClick(false);

    }

    @Override
    public void onFinish() {
        //可点击
        timerClick(true);
    }

    /**
     * 重置时间
     */
    public void resettingTimer() {
        mCdtTimer.clearTimer();
        timerClick(true);
        begin();
    }

    /**
     * 是否允许发送验证码
     */
    private void timerClick(boolean isClick) {
        mCdtTimer.setEnabled(isClick);
        mCdtTimer.isAllowRun(isClick);
    }

    @Override
    public void onTimerClick() {
        Toast.makeText(MainActivity.this, "开始", Toast.LENGTH_SHORT).show();
    }
}
