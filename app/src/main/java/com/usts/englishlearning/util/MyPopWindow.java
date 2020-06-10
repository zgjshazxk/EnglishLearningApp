package com.usts.englishlearning.util;

import android.animation.Animator;
import android.animation.ObjectAnimator;
import android.content.Context;
import android.view.View;

import com.usts.englishlearning.R;

import razerdp.basepopup.BasePopupWindow;

public class MyPopWindow extends BasePopupWindow {

    public static int animatTime = 600;

    public MyPopWindow(Context context) {
        super(context);
    }

    @Override
    public View onCreateContentView() {
        return createPopupById(R.layout.item_authority);
    }

    @Override
    protected Animator onCreateShowAnimator() {
        ObjectAnimator showAnimator = ObjectAnimator.ofFloat(getDisplayAnimateView(), View.TRANSLATION_X, -getScreenWidth(), 0);
        showAnimator.setDuration(animatTime);
        //showAnimator.setInterpolator(new OvershootInterpolator(3));
        return showAnimator;
    }

    @Override
    protected Animator onCreateDismissAnimator() {
        ObjectAnimator showAnimator = ObjectAnimator.ofFloat(getDisplayAnimateView(), View.TRANSLATION_X, 0, getScreenWidth());
        showAnimator.setDuration(animatTime);
        //showAnimator.setInterpolator(new OvershootInterpolator(-3));
        return showAnimator;
    }

}
