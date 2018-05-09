package com.fvcs.cn;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ObjectAnimator;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Window;
import android.view.WindowManager;
import android.widget.TextView;

/**
 * 过渡页面
 */
public class TransitionActivity extends AppCompatActivity {

    private TextView tv_bg ;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        this.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);// 去掉信息栏
        setContentView(R.layout.activity_transition);
        initViews();
    }

    private void initViews() {
        tv_bg = (TextView) findViewById(R.id.tv_bg);
        ObjectAnimator animator = ObjectAnimator.ofFloat(tv_bg, "alpha", 1f, 0.5f, 1f);
        animator.setDuration(3000);
        animator.start();
        animator.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                super.onAnimationEnd(animation);
                startActivity(new Intent(TransitionActivity.this,MainActivity.class));
                finish();
            }
        });
    }
}
