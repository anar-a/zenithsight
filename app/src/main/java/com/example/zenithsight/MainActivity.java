package com.example.zenithsight;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;

public class MainActivity extends AppCompatActivity {

    Button bTest;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        bTest = (Button) findViewById(R.id.leftButton);
        bTest.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view) {
                bTest.setText("Hello world");
            }
        });
    }




}