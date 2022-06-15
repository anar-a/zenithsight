package com.example.zenithsight;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

public class MainActivity extends AppCompatActivity {
    private Button takePictureB;

    private EditText imageCount;
    private EditText delayBetween;
    private AlertDialog cantProceed;

    private int imageCountNum;
    private int delayBetweenNum;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        imageCount = (EditText) findViewById(R.id.imageCount);
        delayBetween = (EditText) findViewById(R.id.delayBetween);

        imageCount.setText("3");
        delayBetween.setText("2");

        cantProceed = new AlertDialog.Builder(this).create();
        cantProceed.setTitle("Cannot Proceed");
        cantProceed.setMessage("Invalid inputs for settings. Must have at least one image, and" +
                " a delay less than 5 minutes.");

        takePictureB = (Button) findViewById(R.id.takePicture);
        takePictureB.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                imageCountNum = Integer.parseInt(imageCount.getText().toString());
                delayBetweenNum = Integer.parseInt(delayBetween.getText().toString());

                if (imageCountNum == 0 || delayBetweenNum > 5 * 60){
                    cantProceed.show();
                }
                else {
                    switchPreviewActivity();
                }
            }
        });

    }

    private void switchPreviewActivity() {
        Intent previewIntent = new Intent(this, PreviewPage.class);
        previewIntent.putExtra("test", 1);
        previewIntent.putExtra("imageCount", imageCountNum);
        previewIntent.putExtra("delayBetween", delayBetweenNum);
        startActivity(previewIntent);
    }


}