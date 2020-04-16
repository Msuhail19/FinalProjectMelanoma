package com.example.finalprojectmelanoma;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;

import android.content.DialogInterface;
import android.os.Build;
import android.content.Intent;
import android.util.Log;
import android.widget.Button;
import android.view.View;
import android.database.Cursor;
import android.net.Uri;
import android.provider.MediaStore;
import android.widget.ImageView;
import android.graphics.BitmapFactory;
import android.graphics.Bitmap;
import java.nio.ByteOrder;
import java.nio.ByteBuffer;
import java.nio.MappedByteBuffer;
import android.app.Activity;
import java.io.*;
import android.content.res.AssetFileDescriptor;
import android.widget.TextView;
import android.app.AlertDialog;

import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import android.Manifest;
import android.content.pm.PackageManager;
import android.content.Context;
import android.widget.Toast;

import com.example.finalprojectmelanoma.Fragments.HistoryFragment;

import java.nio.channels.FileChannel;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

import org.tensorflow.lite.Interpreter;

import pub.devrel.easypermissions.EasyPermissions;


public class RunActivity extends AppCompatActivity implements View.OnClickListener {

    private Button browseBtn, predictBtn;
    private TextView predictions;
    private Button predictAnother;
    private TextView melanomaPred;
    private TextView benignPred;
    private static int RESULT_LOAD_IMAGE = 1;
    private static final int MY_PERMISSIONS_REQUEST_READ_STORAGE = 2;
    private String MODEL_FILE = "InceptionV3.tflite";
    private Interpreter tflite;

    private int imgHeight = 299, imgWidth = 299;
    private int channels = 3;
    private float getStd = 128.f;
    private float getMean = 128.f;
    private boolean isQuantized = false;
    private int inputSize = imgHeight*imgWidth*channels*4;


    private static ByteBuffer globalImage;
    private static Bitmap scaledBitmap;
    public static String imagePath = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        /* Set view according to the activity file */
        super.onCreate(savedInstanceState);
        setContentView(R.layout.choose_image);

        setTitle("Run Model : ");


        /* Create a button and describe load image*/
        browseBtn = (Button) findViewById(R.id.browseBtn);
        browseBtn.setOnClickListener(this);

        /* Create a new button and assign prediction code*/
        predictBtn = (Button) findViewById(R.id.predictBtn);
        predictBtn.setOnClickListener(this);


        try {
            tflite = new Interpreter(loadModelFile(RunActivity.this, MODEL_FILE));
            System.out.println(" Model Loaded ! ");
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    @Override
    public void onClick(View view) {
        /* Browse button code */
        if (view.getId() == R.id.browseBtn) {
            getImage();
            ImageView imageView = (ImageView) findViewById(R.id.imgView);
            imageView.setImageBitmap(scaledBitmap);
        }

        /* Predict button code */
        if(view.getId() == R.id.predictBtn){
            setTitle("Results : ");
            float[][] result = new float[1][2];
            try{

                if(RunActivity.globalImage == null){
                    Toast.makeText(this, "Error : Try adding an image first", Toast.LENGTH_SHORT).show();
                    throw new IllegalArgumentException(" No image provided! ");
                }

                tflite.run(RunActivity.globalImage, result);
                setContentView(R.layout.results);

                /* Create a new button and assign prediction code*/
                predictAnother = (Button) findViewById(R.id.predictAnotherBtn);
                predictAnother.setOnClickListener(this);

                /* Connect to benign textview*/
                benignPred = (TextView) findViewById(R.id.benignTxt);

                /* Connect to mel textview*/
                melanomaPred = (TextView) findViewById(R.id.melTxt);

                // Set imageview as image
                ImageView imageView = (ImageView) findViewById(R.id.imgView);
                imageView.setImageBitmap(scaledBitmap);

                DecimalFormat value = new DecimalFormat("#.#");

                String melpreds = "" + value.format(result[0][0]*100) + "%";
                String benpreds = "" + value.format(result[0][1]*100) + "%";

                melanomaPred.setText(melpreds);
                benignPred.setText(benpreds);

                Date currentTime = Calendar.getInstance().getTime();
                SimpleDateFormat sdf = new SimpleDateFormat("HH:mm   dd/MM/yy ");
                String currentDate = sdf.format(currentTime);

                Log.v("DATENOW :" , currentDate);

                /* Add item to history list. */
                HistoryFragment.addItem(imagePath, result[0][0]*100, result[0][1]*100, currentDate);

            }catch(Exception e){
                e.printStackTrace();
            }

        }

        /* Predict another image*/
        if(view.getId() == R.id.predictAnotherBtn){
            setTitle("Predict :");
            /* Set content view as original */
            setContentView(R.layout.choose_image);

            /* Create a button and describe load image*/
            browseBtn = (Button) findViewById(R.id.browseBtn);
            browseBtn.setOnClickListener(this);

            /* Create a new button and assign prediction code*/
            predictBtn = (Button) findViewById(R.id.predictBtn);
            predictBtn.setOnClickListener(this);
        }
    }

    public void getImage(){
        if(Build.VERSION.SDK_INT > 23){
            String[] galleryPermissions = {Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE};

            if (EasyPermissions.hasPermissions(this, galleryPermissions)) {
                Log.v(null, "--------------PERMISSION GRANTED   2 ------------");
                Intent photoPickerIntent = new Intent(Intent.ACTION_PICK);
                photoPickerIntent.setType("image/*");
                startActivityForResult(photoPickerIntent, RESULT_LOAD_IMAGE);
            } else {
                EasyPermissions.requestPermissions(this, "Access for storage",
                        101, galleryPermissions);
            }
            return;
        }


        if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED && Build.VERSION.SDK_INT >= 16) {
            // Permission is not granted
            Log.v(null, "--------------PERMISSION NOT GRANTED------------");
            ActivityCompat.requestPermissions(RunActivity.this,
                    new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},
                    MY_PERMISSIONS_REQUEST_READ_STORAGE);
        }
        else{
            Log.v(null, "--------------PERMISSION GRANTED------------");
            try{
                Intent photoPickerIntent = new Intent(Intent.ACTION_PICK);
                photoPickerIntent.setType("image/*");
                startActivityForResult(photoPickerIntent, RESULT_LOAD_IMAGE);
            }
            catch(Exception e ){
                e.printStackTrace();
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String[] permissions, int[] grantResults){
        switch (requestCode) {
            case MY_PERMISSIONS_REQUEST_READ_STORAGE: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    // permission was granted, yay! Do the
                    // contacts-related task you need to do.

                    try{
                        Intent photoPickerIntent = new Intent(Intent.ACTION_PICK);
                        photoPickerIntent.setType("image/*");
                        startActivityForResult(photoPickerIntent, RESULT_LOAD_IMAGE);
                    }
                    catch(Exception e ){
                        e.printStackTrace();
                    }

                } else {
                    // permission denied, boo! Disable the
                    // functionality that depends on this permission.
                    Context context = this;
                    AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(context);
                    alertDialogBuilder.setTitle("Cannot access gallery");
                    alertDialogBuilder.setMessage("Permission to read images has not been granted.").setNegativeButton("Close",new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            // if this button is clicked, just close
                            // the dialog box and do nothing
                            dialog.cancel();
                        }
                    });
                    alertDialogBuilder.show();
                }
                return;
            }
        }

    }

    /*Load our model file*/
    private MappedByteBuffer loadModelFile(Activity activity,String MODEL_FILE) throws IOException {
        AssetFileDescriptor fileDescriptor = activity.getAssets().openFd(MODEL_FILE);
        FileInputStream inputStream = new FileInputStream(fileDescriptor.getFileDescriptor());
        FileChannel fileChannel = inputStream.getChannel();
        long startOffset = fileDescriptor.getStartOffset();
        long declaredLength = fileDescriptor.getDeclaredLength();
        return fileChannel.map(FileChannel.MapMode.READ_ONLY, startOffset, declaredLength);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == RESULT_LOAD_IMAGE && resultCode == RESULT_OK && null != data) {
            Uri selectedImage = data.getData();
            String[] filePathColumn = {MediaStore.Images.Media.DATA};

            Cursor cursor = getContentResolver().query(selectedImage,
                    filePathColumn, null, null, null);
            cursor.moveToFirst();

            int columnIndex = cursor.getColumnIndex(filePathColumn[0]);
            String picturePath = cursor.getString(columnIndex);
            cursor.close();

            // String picturePath contains the path of selected Image
            ImageView imageView = (ImageView) findViewById(R.id.imgView);
            Log.v("PATHS IS ", picturePath);
            Bitmap imageChosen = BitmapFactory.decodeFile(picturePath);
            RunActivity.imagePath = picturePath;

            // Set view as image chosen by user
            imageView.setImageBitmap(imageChosen);

            // Resize image for input into network
            if(imageChosen != null) {
                RunActivity.scaledBitmap = Bitmap.createScaledBitmap(imageChosen, 299, 299, false);
            }

            // Set global image var as converted byte buffer
            try {
                RunActivity.globalImage = bitmapToModelsMatchingByteBuffer(scaledBitmap);
            }catch(Exception e){
                e.printStackTrace();
            }
        }

    }



    private ByteBuffer bitmapToModelsMatchingByteBuffer(Bitmap bitmap) {
        ByteBuffer byteBuffer = ByteBuffer.allocateDirect(inputSize);
        byteBuffer.order(ByteOrder.nativeOrder());
        int[] intValues = new int[imgWidth * imgHeight];
        bitmap.getPixels(intValues, 0, bitmap.getWidth(), 0, 0, bitmap.getWidth(), bitmap.getHeight());
        int pixel = 0;
        for (int i = 0; i < imgWidth; ++i) {
            for (int j = 0; j < imgHeight; ++j) {
                int pixelVal = intValues[pixel++];
                if (isQuantized) {
                    for (byte channelVal : pixelToChannelValuesQuant(pixelVal)) {
                        byteBuffer.put(channelVal);
                    }
                } else {
                    for (float channelVal : pixelToChannelValues(pixelVal)) {
                        byteBuffer.putFloat(channelVal);
                    }
                }
            }
        }
        return byteBuffer;
    }

    private float[] pixelToChannelValues(int pixel) {
        if (channels == 1) {
            float[] singleChannelVal = new float[1];
            float rChannel = (pixel >> 16) & 0xFF;
            float gChannel = (pixel >> 8) & 0xFF;
            float bChannel = (pixel) & 0xFF;
            singleChannelVal[0] = (rChannel + gChannel + bChannel) / 3 / getStd;
            return singleChannelVal;
        } else if (channels == 3) {
            float[] rgbVals = new float[3];
            rgbVals[0] = ((((pixel >> 16) & 0xFF) - getMean) / getStd);
            rgbVals[1] = ((((pixel >> 8) & 0xFF) - getMean) / getStd);
            rgbVals[2] = ((((pixel) & 0xFF) - getMean) / getStd);
            return rgbVals;
        } else {
            throw new RuntimeException("Only 1 or 3 channels supported at the moment.");
        }
    }

    private byte[] pixelToChannelValuesQuant(int pixel) {
        byte[] rgbVals = new byte[3];
        rgbVals[0] = (byte) ((pixel >> 16) & 0xFF);
        rgbVals[1] = (byte) ((pixel >> 8) & 0xFF);
        rgbVals[2] = (byte) ((pixel) & 0xFF);
        return rgbVals;
    }

}