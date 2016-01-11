package com.yellowpineapple.wakup.activities;

import android.app.Activity;
import android.widget.TextView;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.Click;
import org.androidannotations.annotations.EActivity;
import org.androidannotations.annotations.Extra;
import org.androidannotations.annotations.ViewById;

@EActivity(resName="activity_modal_text")
public class ModalTextActivity extends Activity {

    @Extra String text;

    @ViewById
    TextView textView;

    @AfterViews
    void afterViews() {
        textView.setText(text);
    }

    @Click(resName={"content_view", "main_view", "textView"})
    void close() {
        finish();
    }

}
