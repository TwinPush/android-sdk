package com.yellowpineapple.wakup.activities;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import com.yellowpineapple.wakup.R;
import com.yellowpineapple.wakup.utils.IntentBuilder;

public class ModalTextActivity extends Activity {

    public final static String TEXT_EXTRA = "text";

    // Properties
    String text;

    // Views
    TextView textView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_modal_text);

        injectExtras();
        injectViews();

        textView.setText(text);
    }

    private void injectExtras() {
        Bundle extras = getIntent().getExtras();
        if (extras!= null) {
            if (extras.containsKey(TEXT_EXTRA)) {
                text = extras.getString(TEXT_EXTRA);
            }
        }
    }

    private void injectViews() {
        textView = (TextView) findViewById(R.id.textView);

        View.OnClickListener closeOnClickListener = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        };

        textView.setOnClickListener(closeOnClickListener);
        View contentView = findViewById(R.id.content_view);
        contentView.setOnClickListener(closeOnClickListener);
        View mainView = findViewById(R.id.main_view);
        mainView.setOnClickListener(closeOnClickListener);
    }

    public static Builder intent(Context context, String text) {
        return new Builder(context, text);
    }

    public static class Builder extends IntentBuilder<ModalTextActivity> {

        Intent intent;
        Context context;

        public Builder(Context context, String text) {
            super(ModalTextActivity.class, context);
            intent = new Intent(context, ModalTextActivity.class);
            intent.putExtra(TEXT_EXTRA, text);
        }

        public void start() {
            context.startActivity(intent);
        }

    }
}
