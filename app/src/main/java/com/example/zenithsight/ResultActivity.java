package com.example.zenithsight;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.os.Bundle;
import android.os.Environment;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;

import com.google.android.material.snackbar.Snackbar;

import java.io.File;
import java.io.FileOutputStream;
import java.util.Calendar;
import java.util.Date;

public class ResultActivity extends AppCompatActivity {

    Bitmap resultImage;
    ImageView resultDisplay;

    private boolean hasStoragePermission = false;

    private Button saveImageB;
    private Button returnPreviewB;

    private Snackbar noPermissionWarning;


    private ActivityResultLauncher<String> requestPermissionLauncher =
            registerForActivityResult(new ActivityResultContracts.RequestPermission(), isGranted -> {
                if (isGranted){
                    hasStoragePermission = true;
                }
            });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_result);

        saveImageB = (Button) findViewById(R.id.saveImage);
        returnPreviewB = (Button) findViewById(R.id.returnPreview);

        noPermissionWarning = Snackbar.make(findViewById(R.id.mainLayout),"Storage permission denied", Snackbar.LENGTH_LONG);

        resultDisplay = (ImageView) findViewById(R.id.resultDisplay);

        Intent thisIntent = getIntent();
        resultImage = BitmapFactory.decodeByteArray(
                thisIntent.getByteArrayExtra("byteArray"),
                0,
                thisIntent.getByteArrayExtra("byteArray").length
        );

        resultDisplay.setImageBitmap(resultImage);

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)
                == PackageManager.PERMISSION_GRANTED){
            hasStoragePermission = true;
        }

        saveImageB.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (hasStoragePermission){
                    String root = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES).toString();
                    File zenithDir = new File(root + "/ZenithSight");

                    if (!zenithDir.exists()){
                        zenithDir.mkdirs();
                        System.out.println("Making dir");
                    }

                    Date currentTime = Calendar.getInstance().getTime();
                    String imageName = "Image" + currentTime.getTime() + ".png";

                    System.out.println("Doing");

                    File imageFile = new File(zenithDir, imageName);

                    if (imageFile.exists()) {
                        imageFile.delete();
                    }

                    try {
                        FileOutputStream imageOut = new FileOutputStream(imageFile);

                        resultImage = rotateBmp(resultImage, 90); // correct orientation

                        resultImage.compress(Bitmap.CompressFormat.PNG, 0, imageOut);
                        imageOut.flush();
                        imageOut.close();
                    }
                    catch (Exception e){
                        e.printStackTrace();
                    }

                }
                else {
                    requestPermissionLauncher.launch(Manifest.permission.WRITE_EXTERNAL_STORAGE);
                    noPermissionWarning.show();
                }
            }
        });

        returnPreviewB.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                resultImage.recycle();
                finish();
            }
        });


    }

    private Bitmap rotateBmp(Bitmap toRotate, float angle){
        Matrix matrix = new Matrix();
        matrix.postRotate(angle);
        return Bitmap.createBitmap(toRotate, 0, 0, toRotate.getWidth(), toRotate.getHeight(), matrix, true);
    }

}