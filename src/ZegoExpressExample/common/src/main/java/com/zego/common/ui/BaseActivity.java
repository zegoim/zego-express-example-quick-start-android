package com.zego.common.ui;

import android.os.Bundle;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.appcompat.app.AppCompatActivity;


import com.zego.common.widgets.log.FloatingView;

/**
 * Created by zego on 2019/2/19.
 */

public class BaseActivity extends AppCompatActivity {


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }


    @Override
    protected void onStart() {
        super.onStart();

        // 在应用内实现悬浮窗，需要依附Activity生命周期
        FloatingView.get().attach(this);
    }

    @Override
    protected void onStop() {
        super.onStop();
        // // 在应用内实现悬浮窗，需要依附Activity生命周期
        FloatingView.get().detach(this);
    }
}
