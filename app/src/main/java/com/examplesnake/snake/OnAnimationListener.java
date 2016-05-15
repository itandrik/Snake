package com.examplesnake.snake;

import android.view.View;
import android.view.animation.Animation;
import android.widget.TextView;

/**
 * AnimationListener, which don't show bonus description after animation
 */
public class OnAnimationListener implements Animation.AnimationListener {
    View view;
    public void setView(View view) {
        this.view = view;
    }

    @Override
    public void onAnimationStart(Animation animation) {

    }

    @Override
    public void onAnimationEnd(Animation animation) {
        ((TextView)view).setText("");
    }

    @Override
    public void onAnimationRepeat(Animation animation) {

    }
}
