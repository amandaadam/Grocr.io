package com.example.ashwin.textdetector;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Matrix;
import android.media.Image;
import android.net.Uri;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.ml.vision.FirebaseVision;
import com.google.firebase.ml.vision.common.FirebaseVisionImage;
import com.google.firebase.ml.vision.common.FirebaseVisionImageMetadata;
import com.google.firebase.ml.vision.text.FirebaseVisionText;
import com.google.firebase.ml.vision.text.FirebaseVisionTextDetector;
import com.theartofdev.edmodo.cropper.CropImage;

import java.util.List;


public class MainActivity extends AppCompatActivity {



    private Button snapBtn;
    private Button detectBtn;
    private ImageView imageView;
    private TextView txtView;
    private Bitmap imageBitmap;
    Input myInput;
    DatabaseReference reff;

    Uri mImageUri;
    private Button cropper;
    private ImageView myImage;


    @Override
    protected void onCreate(Bundle savedInstanceState) {

        reff = FirebaseDatabase.getInstance().getReference().child("Input");

        super.onCreate(savedInstanceState);
        Toast.makeText(MainActivity.this, "CONNECTED TO DATABASE", Toast.LENGTH_LONG).show();



        setContentView(R.layout.activity_main);
        snapBtn = findViewById(R.id.snapBtn);
        detectBtn = findViewById(R.id.detectBtn);
        imageView = findViewById(R.id.imageView);
        txtView = findViewById(R.id.txtView);
        snapBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dispatchTakePictureIntent();
            }
        });
        detectBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                detectTxt();
            }
        });

        cropper = findViewById(R.id.cropper);
        //myImage = findViewById(R.id.myImage);
    }



    public void onChooseFile(View v) {

        CropImage.activity().start(MainActivity.this);
    }





    public static Bitmap rotateImage(Bitmap source, float angle) {
        Matrix matrix = new Matrix();
        matrix.postRotate(angle);
        return Bitmap.createBitmap(source, 0, 0, source.getWidth(), source.getHeight(), matrix, true);
    }




    private void parseAndSend(String unparsedString){
        //Creating a string builder to create a more flexible string
        StringBuilder builder = new StringBuilder(unparsedString);
        //Filter level 1 - removing numbers
        for(int i = 0; i< unparsedString.length(); i++){
                if(Character.isDigit(builder.charAt(i)) || builder.charAt(i) == ' ' ){
                    builder.deleteCharAt(i);
                }
            }


        }


    static final int REQUEST_IMAGE_CAPTURE = 1;

    private void dispatchTakePictureIntent() {
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
            startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == RESULT_OK) {
            Bundle extras = data.getExtras();
            imageBitmap = (Bitmap) extras.get("data");
            imageView.setImageBitmap(rotateImage(imageBitmap,90));
        }

        if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE) {
            CropImage.ActivityResult result = CropImage.getActivityResult(data);

            if(resultCode == RESULT_OK) {
                mImageUri = result.getUri();
                imageView.setImageURI(mImageUri);
            }

            else if(resultCode == CropImage.CROP_IMAGE_ACTIVITY_RESULT_ERROR_CODE) {

                Exception e = result.getError();
                Toast.makeText(this, "Possible error is : "+ e,Toast.LENGTH_SHORT).show();
            }
        }

    }

    private void detectTxt() {
        FirebaseVisionImage image = FirebaseVisionImage.fromBitmap(rotateImage(imageBitmap, 90));
        FirebaseVisionTextDetector detector = FirebaseVision.getInstance().getVisionTextDetector();
        detector.detectInImage(image).addOnSuccessListener(new OnSuccessListener<FirebaseVisionText>() {
            @Override
            public void onSuccess(FirebaseVisionText firebaseVisionText) {
                processTxt(firebaseVisionText);
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {

            }
        });
    }

    private void processTxt(FirebaseVisionText text) {
        String txt = "";
        List<FirebaseVisionText.Block> blocks = text.getBlocks();
        if (blocks.size() == 0) {
            Toast.makeText(MainActivity.this, "No Text :(", Toast.LENGTH_LONG).show();

            this.myInput = new Input();
            this.myInput.addToWords("No words found!");
            this.reff.push().setValue(myInput);
            Toast.makeText(MainActivity.this, "no words found", Toast.LENGTH_LONG);
            return;
        }
        for (FirebaseVisionText.Block block : text.getBlocks()) {


            txtView.setTextSize(18);
            txtView.setText(txt);

           for(FirebaseVisionText.Line line : block.getLines()){
               txt += line.getText() +"/n";
            }


        }
        this.myInput = new Input();
        this.myInput.addToWords(txt);
        this.reff.push().setValue(myInput   );
        Toast.makeText(MainActivity.this, " data inserted", Toast.LENGTH_LONG);
    }

}
