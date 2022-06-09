package com.example.zenithsight;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

public class MainActivity extends AppCompatActivity {
    private Button takePictureB;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        takePictureB = (Button) findViewById(R.id.takePicture);
        takePictureB.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                switchPreviewActivity();
            }
        });

    }

    private void switchPreviewActivity() {
        Intent previewIntent = new Intent(this, PreviewPage.class);
        previewIntent.putExtra("test", 1);
        startActivity(previewIntent);
    }


}