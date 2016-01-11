package com.yellowpineapple.wakup.utils;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;

public class IntentBuilder<T extends Activity> {

    Context context;
    Intent intent;

    public IntentBuilder(Class<T> tClass, Context context) {
        intent = new Intent(context, tClass);
    }

    public void start() {
        context.startActivity(intent);
    }

    public Intent getIntent() {
        return intent;
    }

    public Context getContext() {
        return context;
    }
}
