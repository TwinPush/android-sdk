package com.yellowpineapple.wakup;

import android.app.Activity;
import android.os.Bundle;

import com.yellowpineapple.wakup.sdk.Wakup;
import com.yellowpineapple.wakup.sdk.WakupOptions;

public class SplashActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Wakup
        Wakup.instance(this).launch(
                    new WakupOptions("YOUR_API_KEY").
                            country("ES").
                            defaultLocation(41.38506, 2.17340)
            );
    }
}
