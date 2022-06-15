package com.example.zenithsight;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.camera.core.Camera;
import androidx.camera.core.CameraSelector;
import androidx.camera.core.ImageCapture;
import androidx.camera.core.ImageCaptureException;
import androidx.camera.core.ImageProxy;
import androidx.camera.core.Preview;
import androidx.camera.lifecycle.ProcessCameraProvider;
import androidx.camera.view.PreviewView;
import androidx.core.content.ContextCompat;
import androidx.lifecycle.LifecycleOwner;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.hardware.HardwareBuffer;
import android.media.Image;
import android.os.Bundle;
import android.util.Size;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.google.common.util.concurrent.ListenableFuture;

import org.w3c.dom.Text;

import java.io.ByteArrayOutputStream;
import java.nio.Buffer;
import java.nio.ByteBuffer;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executor;

public class PreviewPage extends AppCompatActivity {
    private ListenableFuture<ProcessCameraProvider> camProviderFuture;

    private Button returnB;
    private Button takePicB;
    private ProgressBar imageProgress;
    private TextView progressText;

    private PreviewView previewView;
    private ImageCapture imageCapture;

    private boolean imageFinished = true;

    int numImages = 3;
    int delay = 1000; //ms
    Bitmap resultImage = null;

    boolean cancelOperation;

    private int currentPic = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_preview_page);

        previewView = (PreviewView) findViewById(R.id.previewView);
        imageProgress = (ProgressBar) findViewById(R.id.imageProgress);
        progressText = (TextView) findViewById(R.id.progressText);

        imageProgress.setVisibility(View.INVISIBLE);
        progressText.setVisibility(View.INVISIBLE);

        camProviderFuture = ProcessCameraProvider.getInstance(this);
        camProviderFuture.addListener(() -> {
            try {
                ProcessCameraProvider cameraProvider = camProviderFuture.get();
                startCameraX(cameraProvider);
            }
            catch (ExecutionException e) {
                e.printStackTrace();
            }
            catch (InterruptedException e) {
                e.printStackTrace();

            }
        }, ContextCompat.getMainExecutor(this));


        cancelOperation = false;

        returnB = (Button) findViewById(R.id.backButton);
        returnB.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view) {
                cancelOperation = true;
                finish(); // Exit preview screen
            }
        });

        imageProgress.setMax(numImages);

        takePicB = (Button) findViewById(R.id.takePicB);
        takePicB.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (imageFinished == true){
                    if (previewView.getPreviewStreamState().getValue() == PreviewView.StreamState.IDLE) {
                        System.out.println("Preview not ready");
                        return;
                    }

                    imageProgress.setVisibility(View.VISIBLE);
                    progressText.setVisibility(View.VISIBLE);

                    imageFinished = false; // image taking debounce

                    if (resultImage != null){
                        resultImage.recycle();
                        resultImage = null;
                    }

                    // start photo taking timer task immediately
                    new picTask().run();
                }
            }
        });

        Intent currentIntent = getIntent(); // retrieving extended data
    }

    private void startCameraX(ProcessCameraProvider cameraProvider) {
        cameraProvider.unbindAll();

        CameraSelector cameraSelector = new CameraSelector.Builder()
                .requireLensFacing(CameraSelector.LENS_FACING_BACK).build();

        Preview preview = new Preview.Builder().build();
        preview.setSurfaceProvider(previewView.getSurfaceProvider());

        imageCapture = new ImageCapture.Builder()
                .setCaptureMode(ImageCapture.CAPTURE_MODE_MINIMIZE_LATENCY)
                .setTargetResolution(new Size(1280, 720))
                .build();

        cameraProvider.bindToLifecycle((LifecycleOwner) this, cameraSelector, preview, imageCapture);
    }

    private class picTask extends TimerTask {
        @Override
        public void run() {
            currentPic += 1;
            if (currentPic > numImages){
                currentPic = 0;
                return;
            }
            else {
                System.out.println("Pic num: " + currentPic);
                onTakeImage(currentPic);

                //Timer nextTimer = new Timer();
                //nextTimer.schedule(new picTask(), delay);
            }
        }
    };

    private void onTakeImage(int currIter) {
        // Take image button pressed
        imageCapture.takePicture(ContextCompat.getMainExecutor(this), new ImageCapture.OnImageCapturedCallback() {
            @SuppressLint("UnsafeOptInUsageError")
            @Override
            public void onCaptureSuccess(@NonNull ImageProxy image) {
                super.onCaptureSuccess(image);

                // Copy pixel buffer to bitmap for later manipulation
                ByteBuffer pxBuffer = image.getPlanes()[0].getBuffer();
                byte[] bytes = new byte[pxBuffer.capacity()];
                pxBuffer.get(bytes);
                Bitmap bmp = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);

                if (resultImage == null){ // final image not initialized
                    resultImage = bmp.copy(Bitmap.Config.ARGB_8888, true);

                    // reset all pixels to 0,0,0
                    for (int x = 0; x < resultImage.getWidth(); x++){
                        System.out.println("X: " + x);
                        for (int y = 0; y < resultImage.getHeight(); y++){
                            resultImage.setPixel(x, y, Color.rgb(0, 0, 0));
                        }
                    }
                }

                System.out.println("4");
                // add to the mean
                for (int x = 0; x < resultImage.getWidth() && !cancelOperation; x++){
                    for (int y = 0; y < resultImage.getHeight() && !cancelOperation; y++){
                        Color currColor = bmp.getColor(x, y);
                        Color currentMean = resultImage.getColor(x, y);

                        resultImage.setPixel(x, y, Color.rgb(
                                currColor.red()/numImages + currentMean.red(),
                                currColor.green()/numImages + currentMean.green(),
                                currColor.blue()/numImages + currentMean.blue()
                                ));
                    }

                }

                System.out.println("Complete");
                bmp.recycle();
                image.close();

                imageProgress.setProgress(currIter);

                if (cancelOperation) {
                    finish();
                }
                else {
                    Timer nextTimer = new Timer();
                    nextTimer.schedule(new picTask(), delay);
                }

                // All pictures done being taken
                if (currIter == numImages){
                    onImageCaptureCompletion();
                }

            }

            @Override
            public void onError(@NonNull ImageCaptureException exception) {
                super.onError(exception);
                System.out.println("capture failure");

            }
        });
    }

    private void onImageCaptureCompletion(){
        System.out.println(resultImage.getHeight());
        imageFinished = true;

        imageProgress.setProgress(0);
        progressText.setVisibility(View.INVISIBLE);
        imageProgress.setVisibility(View.INVISIBLE);

        Intent resultIntent = new Intent(this, ResultActivity.class);
        ByteArrayOutputStream resultStream = new ByteArrayOutputStream();
        resultImage.compress(Bitmap.CompressFormat.PNG, 50, resultStream);

        resultIntent.putExtra("byteArray", resultStream.toByteArray());
        startActivity(resultIntent);

    }

}
