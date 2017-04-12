package com.ps.bindview;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.TextView;

import com.ps.annotation.BindView;
import com.ps.api.ViewBind;
import com.ps.api.ViewBinder;

public class MainActivity extends AppCompatActivity {

    @BindView(R.id.test_text)
    TextView textView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ViewBinder.bind(this);
        textView.setText("my annotation");
    }
}
