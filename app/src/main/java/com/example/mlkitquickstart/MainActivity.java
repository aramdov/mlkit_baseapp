package com.example.mlkitquickstart;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.camera.core.ImageAnalysis;
import androidx.camera.core.ImageProxy;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Point;
import android.graphics.Rect;
import android.media.Image;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.mlkit.vision.common.InputImage;
import com.google.mlkit.vision.text.Text;
import com.google.mlkit.vision.text.TextRecognition;
import com.google.mlkit.vision.text.TextRecognizer;

import java.util.List;

import static android.widget.Toast.LENGTH_SHORT;

public class MainActivity extends AppCompatActivity {

    ImageView imageView;
    TextView textView;

    @RequiresApi(api = Build.VERSION_CODES.M)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // find imageview and textview
        imageView = findViewById(R.id.imageId);
        textView = findViewById(R.id.textId);

        // check app level permission is granted for camera
        if (checkSelfPermission(Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            // grant permission
            requestPermissions(new String[] {Manifest.permission.CAMERA}, 101);
        }

    }

    public void doProcess(View view) {
        // open the camera, utilize implicit intents
        // maybe Startactivityforresult then we pass it into the ImageProxy? Intent to return a result like Instagram
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        // start activity to return a data object and use it (for result)
        startActivityForResult(intent, 101);

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        // take the image and set it in the imageview. start by taking data argument
         Bundle bundle = data.getExtras();
         // from bundle, extract the image. cast to bitmap because the bundle.get returns an object
        Bitmap bitmap = (Bitmap) bundle.get("data");
        // set the image in imageview
        imageView.setImageBitmap(bitmap);

        // process the image

        // attempt #1, converting Bitmap to {InputImage} object
            // Figure out if we need to account for rotation on the image. CameraX wont need to worry about this but Bitmap yes.
        InputImage image = InputImage.fromBitmap(bitmap, 0);
        // 2. get instance of Text Recognition Vision
        TextRecognizer recognizer = TextRecognition.getClient();
        // 3. create an instance of ML kit visiontext (firebase extracts textrecognizer from vision)

        // 4. create a task to process the image
        Task<Text> result =
                recognizer.process(image)
                        .addOnSuccessListener(new OnSuccessListener<Text>() {
                            @Override
                            public void onSuccess(Text visionText) {
                                // Task completed successfully. {Text} Object is passed to success listener.
                                // {Text} object contains full text recognized in the image & 0 or more {TextBlock} objects.
                                // {TextBlock} represents a rectangular block of text which has 0 or more {Line} objects.
                                // Each {Line} object contains 0 or more {Element} objects which represent words and word-like entities such as dates and numbers
                                // We might be interested in parsing {Element}s. For now we do a triple nested for loop for each {TextBlock}, {Line}, {Element}

                                String resultText = visionText.getText();
                                textView.setText(resultText);
//                                for (Text.TextBlock block : visionText.getTextBlocks()) {
//                                    String blockText = block.getText();
//                                    Point[] blockCornerPoints = block.getCornerPoints();
//                                    Rect blockFrame = block.getBoundingBox();
//                                    for (Text.Line line : block.getLines()) {
//                                        String lineText = line.getText();
//                                        Point[] lineCornerPoints = line.getCornerPoints();
//                                        Rect lineFrame = line.getBoundingBox();
//                                        for (Text.Element element : line.getElements()) {
//                                            String elementText = element.getText();
//                                            Point[] elementCornerPoints = element.getCornerPoints();
//                                            Rect elementFrame = element.getBoundingBox();
//                                        }
//                                    }
//                                }
                            }
                        })
                        .addOnFailureListener(
                                new OnFailureListener() {
                                    @Override
                                    public void onFailure(@NonNull Exception e) {
                                        // Task failed with an exception
                                        // Toast.makeText(getApplicationContext(), "on failure", Toast.LENGTH_SHORT).show();
                                        Log.i("CameraX", "Text Recognition failed. Error: " + e);
                                    }
                                })
                        // TODO: Make sure this is working!!!!
                        // check for errors in this OnCompleteListener implementation (im scared)
                        .addOnCompleteListener(new OnCompleteListener<Text>() {
                            @Override
                            public void onComplete(@NonNull Task<Text> task) {

                            }
                        });

    }

    // Need to create an InputImage object form either {Bitmap, media.Image, ByteBuffer, byte array or file on the device}
    // Added Google ML kit dependencies, CameraX depenecies including Kotling-Android extension, check project and app gradle files
        // revisit and make sure compatibility doesn't raise issues
        // Want to use CameraX to make rotation calculation automatic and easy for ML Kit input image data

    private class ImageAnalyzia implements ImageAnalysis.Analyzer {

        // CameraX library lets OnImageCapturedListener and ImageAnalysis.Analyzer calculate the rotation value for us
        @Override
        public void analyze(ImageProxy imageProxy) {
            Image mediaImage = imageProxy.getImage();
            if (mediaImage != null) {
                InputImage image =
                        InputImage.fromMediaImage(mediaImage, imageProxy.getImageInfo().getRotationDegrees());
                // Pass image to an ML Kit Vision API

                // processing the image by passing the image to the {process} method
                Task<Text> result =
                        recognizer.process(image)
                                .addOnSuccessListener(new OnSuccessListener<Text>() {
                                    @Override
                                    public void onSuccess(Text visionText) {
                                        // Task completed successfully. {Text} Object is passed to success listener.
                                        // {Text} object contains full text recognized in the image & 0 or more {TextBlock} objects.
                                        // {TextBlock} represents a rectangular block of text which has 0 or more {Line} objects.
                                        // Each {Line} object contains 0 or more {Element} objects which represent words and word-like entities such as dates and numbers
                                        // We might be interested in parsing {Element}s. For now we do a triple nested for loop for each {TextBlock}, {Line}, {Element}

                                        String resultText = visionText.getText();
                                        for (Text.TextBlock block : visionText.getTextBlocks()) {
                                            String blockText = block.getText();
                                            Point[] blockCornerPoints = block.getCornerPoints();
                                            Rect blockFrame = block.getBoundingBox();
                                            for (Text.Line line : block.getLines()) {
                                                String lineText = line.getText();
                                                Point[] lineCornerPoints = line.getCornerPoints();
                                                Rect lineFrame = line.getBoundingBox();
                                                for (Text.Element element : line.getElements()) {
                                                    String elementText = element.getText();
                                                    Point[] elementCornerPoints = element.getCornerPoints();
                                                    Rect elementFrame = element.getBoundingBox();
                                                }
                                            }
                                        }
                                    }
                                })
                                .addOnFailureListener(
                                        new OnFailureListener() {
                                            @Override
                                            public void onFailure(@NonNull Exception e) {
                                                // Task failed with an exception
                                                // Toast.makeText(getApplicationContext(), "on failure", Toast.LENGTH_SHORT).show();
                                                Log.i("CameraX", "Text Recognition failed. Error: " + e);
                                            }
                                        })
                                // TODO: Make sure this is working!!!!
                                // check for errors in this OnCompleteListener implementation (im scared)
                                .addOnCompleteListener(new OnCompleteListener<Text>() {
                                    @Override
                                    public void onComplete(@NonNull Task<Text> task) {
                                        imageProxy.close();
                                    }
                                });

            }
        }
    }

    // 2 different versions/paths for importing class for TextRecognizer, come back if there are issues
    // get instance of textrecognizer
    TextRecognizer recognizer = TextRecognition.getClient();

}