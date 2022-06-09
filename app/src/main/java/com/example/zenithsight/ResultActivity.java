package com.example.zenithsight;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.widget.ImageView;

public class ResultActivity extends AppCompatActivity {

    Bitmap resultImage;
    ImageView resultDisplay;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_result);

        resultDisplay = (ImageView) findViewById(R.id.resultDisplay);

        Intent thisIntent = getIntent();
        resultImage = BitmapFactory.decodeByteArray(
                thisIntent.getByteArrayExtra("byteArray"),
                0,
                thisIntent.getByteArrayExtra("byteArray").length
        );


        resultDisplay.setRotation(90);
        resultDisplay.setImageBitmap(resultImage);
    }
}