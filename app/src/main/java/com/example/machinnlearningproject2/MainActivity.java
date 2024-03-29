package com.example.machinnlearningproject2;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.FileProvider;

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
import android.os.Environment;
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
import com.theartofdev.edmodo.cropper.CropImage;
import com.theartofdev.edmodo.cropper.CropImageView;

import org.w3c.dom.Text;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    private Button selectBtn , copyBtn ;
    private TextView textView;
    private ImageView imageView;
    private Bitmap mSelectedImage;

    private String currentImagePath = null;
    private static final int IMAGE_REQUEST_CODE=1;
    private static final int GALLERY_REQUEST_CODE=10;

    private Uri myImage;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

       initialize();

        selectBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                Intent intent = new Intent();
                intent.setAction(Intent.ACTION_PICK);
                intent.setType("image/*");


                System.out.println("Pradeep........................");
                startActivityForResult(intent , GALLERY_REQUEST_CODE );

                System.out.println("mondal.....................");
            }
        });


        copyBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                 copyText();
            }
        });
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode , @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if(requestCode==GALLERY_REQUEST_CODE  && resultCode==RESULT_OK && data!=null)
        {
            CropImage.activity()
                    .setGuidelines( CropImageView.Guidelines.ON )
                    .setAspectRatio(1,1)
                    .start(this);

            System.out.println("request coede ="+ requestCode);
        }
        else
        if(requestCode==CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE)
        {
            System.out.println("request coede ="+ requestCode);
            CropImage.ActivityResult result =
                    CropImage.getActivityResult(data);

            if(resultCode == RESULT_OK )
            {
                myImage = result.getUri();

                convertingImageToTextFullProcess(getApplicationContext() , myImage);
            }
        }

        // for camera image
//        else if(requestCode==IMAGE_REQUEST_CODE  && resultCode==RESULT_OK && data!=null)
//            {
//                CropImage.activity()
//                        .setGuidelines( CropImageView.Guidelines.ON )
//                        .setAspectRatio(1,1)
//                        .start(this);
//                System.out.println("reuqest code = " + requestCode);
//                mSelectedImage = (Bitmap)data.getExtras().get("data");
//
//            }
//
//        else if(resultCode==RESULT_CANCELED)
//        {
//            Toast.makeText(this,
//                    "cancelled by user.", Toast.LENGTH_SHORT).show();
//        }
        else
        {
            Toast.makeText(this,
                    "failed to capture image..", Toast.LENGTH_SHORT).show();
        }
    }

    private void convertingImageToTextFullProcess(Context applicationContext, Uri myImage) {
        try {

            Picasso.with(this).load(myImage).into(imageView);

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

    public void processImageToText(FirebaseVisionText texts) {
        String text = texts.getText();
        textView.setText(" ");
        for(FirebaseVisionText.TextBlock block : texts.getTextBlocks()){

            textView.append("\n \n "+block.getText());
        }
    }

    public void captureImage(View view) throws IOException {

        Intent cameraIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);

        if(cameraIntent.resolveActivity(getPackageManager()) !=null)
        {

            File imageFile = null;
            imageFile = getImageFile();

            if(imageFile!=null)
            {

                Uri imageUri = FileProvider.getUriForFile(this ,
                        "com.example.android.fileprovider",imageFile);

                cameraIntent.putExtra(MediaStore.EXTRA_OUTPUT,imageUri);
                System.out.println("going to image req onActivirty resuly............./////////////...........//////////");
                startActivityForResult(cameraIntent , IMAGE_REQUEST_CODE);
                System.out.println("hm........................");

            }
        }
    }

    private File getImageFile() throws IOException {
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String imageName = "jpg_"+timeStamp+"_";
        File storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES);

        File imageFile = File.createTempFile( imageName , ".jpg" , storageDir);
        currentImagePath = imageFile.getAbsolutePath();

        return imageFile;
    }

    public void show_image(View view) {

        Bitmap bitmap = BitmapFactory.decodeFile(currentImagePath);
        imageView.setImageBitmap(bitmap);
        textRecognization(bitmap);

    }

    private void copyText() {

        String copiedText = textView.getText().toString();

        ClipboardManager clipboardManager = (ClipboardManager)getSystemService(CLIPBOARD_SERVICE);
        ClipData data = ClipData.newPlainText("Image Copied", copiedText);

        clipboardManager.setPrimaryClip(data);
        Toast.makeText(MainActivity.this,
                "Text Copied.", Toast.LENGTH_SHORT).show();
    }

    private void initialize() {
        selectBtn = (Button)findViewById(R.id.id_selet_pic);
        copyBtn = (Button)findViewById(R.id.id_copy_pic);
        textView = (TextView)findViewById(R.id.id_tv);
        imageView = (ImageView)findViewById(R.id.id_imageView);

    }

}



