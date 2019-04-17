package com.jason.bubbleview;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.MotionEvent;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends AppCompatActivity {

    private TextView textView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        textView = findViewById(R.id.textview);
        BubbleView.attach(MainActivity.this, textView, new BubbleView
                .BubbleViewDismissListener() {
            @Override
            public void onDismiss(View view) {
                Toast.makeText(MainActivity.this,"爆炸了",Toast.LENGTH_SHORT).show();
            }
        });

    }
}
