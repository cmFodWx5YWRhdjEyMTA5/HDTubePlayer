package org.schabi.newpipe.youtubelist;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.StyleRes;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;

import org.schabi.newpipe.R;

/**
 * Created by liyanju on 2018/9/23.
 */

public class WaitingDialog extends Dialog {
    private ImageView mImageView;
    private Animation mAnimation;

    public WaitingDialog(@NonNull Context context) {
        this(context, R.style.EqualizerStyle);
    }

    public WaitingDialog(@NonNull Context context, @StyleRes int themeResId) {
        super(context, themeResId);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.item_waiting_dialog);
        mImageView = (ImageView) findViewById(R.id.img_waiting);
        mAnimation = AnimationUtils.loadAnimation(getContext(), R.anim.rotate_center);
        mAnimation.setDuration(1000);
        mAnimation.setRepeatMode(Animation.RESTART);
        mAnimation.setRepeatCount(Animation.INFINITE);
    }

    @Override
    public void show() {
        super.show();
        mImageView.clearAnimation();
        mImageView.startAnimation(mAnimation);
    }

    @Override
    public void dismiss() {
        super.dismiss();
    }
}
