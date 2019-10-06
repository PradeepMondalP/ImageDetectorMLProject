package com.example.machinnlearningproject2;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Point;
import android.graphics.Rect;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.ml.vision.FirebaseVision;
import com.google.firebase.ml.vision.common.FirebaseVisionImage;
import com.google.firebase.ml.vision.text.FirebaseVisionText;
import com.google.firebase.ml.vision.text.FirebaseVisionTextRecognizer;
import com.google.firebase.ml.vision.text.RecognizedLanguage;
import com.squareup.picasso.Picasso;

import org.w3c.dom.Text;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    private final int TEXT_RECO_REQ_CODE=100;
    private Button selectBtn , copyBtn;
    private TextView textView;
    private ImageView imageView;
    private ImageButton camBtn;
    private Bitmap mSelectedImage;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        selectBtn = (Button)findViewById(R.id.id_selet_pic);
        copyBtn = (Button)findViewById(R.id.id_copy_pic);
        textView = (TextView)findViewById(R.id.id_tv);
        imageView = (ImageView)findViewById(R.id.id_imageView);
        camBtn = (ImageButton)findViewById(R.id.id_camBtn);

        selectBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                Intent intent = new Intent();
                intent.setAction(Intent.ACTION_PICK);
                intent.setType("image/*");
                startActivityForResult(intent , 1);
            }
        });

        camBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                openCam();
            }
        });


        copyBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                String copiedText = textView.getText().toString();

                ClipboardManager clipboardManager = (ClipboardManager)getSystemService(CLIPBOARD_SERVICE);
                ClipData data = ClipData.newPlainText("Image Copied", copiedText);

                clipboardManager.setPrimaryClip(data);
                Toast.makeText(MainActivity.this,
                        "Text Copied.", Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if(requestCode==1  && resultCode==RESULT_OK && data!=null)
        {
            Uri myImage = data.getData();
            convertingImageToTextFullProcess(getApplicationContext() , myImage);
        }
        else if(requestCode==TEXT_RECO_REQ_CODE  && resultCode==RESULT_OK && data!=null)
            {
                mSelectedImage = (Bitmap)data.getExtras().get("data");
                textRecognization(mSelectedImage);

            }
        else if(resultCode==RESULT_CANCELED)
        {
            Toast.makeText(this,
                    "cancelled by user.", Toast.LENGTH_SHORT).show();
        }
        else
        {
            Toast.makeText(this,
                    "failed to capture image..", Toast.LENGTH_SHORT).show();
        }
    }

    private void convertingImageToTextFullProcess(Context applicationContext, Uri myImage) {
        try {
            imageView.setImageURI(myImage);

            FirebaseVisionImage image =FirebaseVisionImage.fromFilePath(getApplicationContext() ,myImage);
            FirebaseVisionTextRecognizer recognizer = FirebaseVision.getInstance().getOnDeviceTextRecognizer();

            recognizer.processImage(image).addOnSuccessListener(new OnSuccessListener<FirebaseVisionText>() {
                @Override
                public void onSuccess(FirebaseVisionText texts) {

                    processImageToText(texts);
                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {

                    Toast.makeText(MainActivity.this,
                            "something wrong..", Toast.LENGTH_SHORT).show();
                }
            });

        } catch (IOException e) {
            System.out.println("error here...........");
            System.out.println(e.getMessage());
        }
    }

    public void openCam()
    {
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        startActivityForResult(intent , TEXT_RECO_REQ_CODE);
    }

    private void textRecognization(Bitmap mSelectedImage) {

        FirebaseVisionImage image = null;
        imageView.setImageBitmap(mSelectedImage);
            image = FirebaseVisionImage.fromBitmap(mSelectedImage);

        FirebaseVisionTextRecognizer detector = FirebaseVision.getInstance().getOnDeviceTextRecognizer();

        detector.processImage(image)
                .addOnSuccessListener(new OnSuccessListener<FirebaseVisionText>() {
                    @Override
                    public void onSuccess(FirebaseVisionText result) {

                        processImageToText(result);
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {

                        Toast.makeText(MainActivity.this,
                                "Something wrong..", Toast.LENGTH_SHORT).show();
                    }
                });

    }
    public void processImageToText(FirebaseVisionText texts)
    {
        String text = texts.getText();
        textView.setText(" ");
        for(FirebaseVisionText.TextBlock block : texts.getTextBlocks()){

            textView.append("\n \n "+block.getText());
        }
    }

}
