package com.twincoders.twinpush.sdk.demo;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.os.Build;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.view.View;

/**
 * Created by agutierrez on 6/11/15.
 */
public abstract class ParentActivity extends AppCompatActivity {

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    /**
     * Shows the progress UI and hides the register form.
     */
    protected void showProgress(final boolean show, final View loadingView, final View mainView) {
        int shortAnimTime = getResources().getInteger(android.R.integer.config_shortAnimTime);

        mainView.setVisibility(show ? View.GONE : View.VISIBLE);
        mainView.animate().setDuration(shortAnimTime).alpha(
                show ? 0 : 1).setListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                mainView.setVisibility(show ? View.GONE : View.VISIBLE);
            }
        });

        loadingView.setVisibility(show ? View.VISIBLE : View.GONE);
        loadingView.animate().setDuration(shortAnimTime).alpha(
                show ? 1 : 0).setListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                loadingView.setVisibility(show ? View.VISIBLE : View.GONE);
            }
        });
    }
}
