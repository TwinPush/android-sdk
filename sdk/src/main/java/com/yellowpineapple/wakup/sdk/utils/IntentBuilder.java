package com.yellowpineapple.wakup.sdk.utils;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;

public class IntentBuilder<T extends Activity> {

    Context context;
    Intent intent;

    public IntentBuilder(Class<T> tClass, Context context) {
        this.intent = new Intent(context, tClass);
        this.context = context;
    }

    public void start() {
        context.startActivity(intent);
    }

    protected Intent getIntent() {
        return intent;
    }

    protected Context getContext() {
        return context;
    }
}
