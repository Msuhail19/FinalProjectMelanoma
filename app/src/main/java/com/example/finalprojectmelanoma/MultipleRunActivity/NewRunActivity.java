package com.example.finalprojectmelanoma.MultipleRunActivity;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.provider.MediaStore;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.loader.content.CursorLoader;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.finalprojectmelanoma.Fragments.HistoryFragment;
import com.example.finalprojectmelanoma.MainActivity;
import com.example.finalprojectmelanoma.R;

import org.tensorflow.lite.Interpreter;
import org.w3c.dom.Text;

import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;


public class NewRunActivity extends AppCompatActivity {

    private static final int MY_PERMISSIONS_REQUEST_READ_STORAGE = 2;
    private static final int GET_IMAGE_CODE = 1102;

    /* Items accessed from choose_image view*/
    private RecyclerView imagechooserRecView;
    private RelativeLayout getImageRelative;
    private RelativeLayout getPredictRelative;


    List<ImageItem> mData;
    private ImageItemRecyclerViewAdapter recAdapter;
    private InterpreterHandler handler;

    private String MODEL_FILE = "InceptionV3.tflite";
    private Interpreter tflite;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        /* Set view according to the activity file */
        super.onCreate(savedInstanceState);
        setContentView(R.layout.recycler);

        /* Set Title */
        setTitle(" Run Model On Images ");

        /* Connect to recyclerview */
        imagechooserRecView = findViewById(R.id.image_chooser);

        mData = new ArrayList<>();
        resetRecyclerView();
        final TextView instructions = findViewById(R.id.multiple_instructions);

        /* find items and add onclicklistener */
        getImageRelative = findViewById(R.id.getImageRelative);
        getImageRelative.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                addImage();
                instructions.setVisibility(View.INVISIBLE);
            }
        });

        getPredictRelative = findViewById(R.id.runPredictionsRelative);

        /* Define dialog for loading. */
        final ProgressDialog dialog = new ProgressDialog(NewRunActivity.this); // this = YourActivity
        dialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        dialog.setTitle("Loading");
        dialog.setMessage("Loading. Please wait...");
        dialog.setIndeterminate(true);
        dialog.setCanceledOnTouchOutside(false);

        /* Runnable to handle dismiss of dialog. */
        final Runnable runnable = new Runnable() {
            @Override
            public void run() {
                if (dialog.isShowing()) {
                    dialog.dismiss();
                }
            }
        };

        /* declare progressdialog and handler */

        getPredictRelative.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                /* Do not run if imagedata dosent exist */
                if(mData.size() < 1){
                    toastLong("Add an image first.");
                    return;
                }

                new OnRunPredictions(NewRunActivity.this).execute("");
            }
        });

        handler = new InterpreterHandler(NewRunActivity.this, MODEL_FILE);

    }

    /*
    * Adds new imageItem to mData list
    * */
    private boolean addNewItem(ImageItem item){
        if(mData.size() < 4){
            Log.v("New Item", "Adding new Item");
            mData.add(item);
            resetRecyclerView();
            return true;
        }
        return false;
    }

    /*
    * populate results view
    * */
    private void populateResultView() {
        TextView verdict = findViewById(R.id.verdict);
        TextView date = findViewById(R.id.average_results_date);
        TextView ben_prob = findViewById(R.id.ben_probability);
        TextView mal_prob = findViewById(R.id.mal_probability);

        float ben_average = 0 ;
        float mal_average = 0 ;
        List<ResultItem> resultData = new ArrayList<>();
        ArrayList<String> imagePaths = new ArrayList<>();
        ArrayList<float[][]> probabilities = new ArrayList<>();

        DecimalFormat value = new DecimalFormat("#.#");

        for(ImageItem x : mData){
            // work out average probability across the images provided
            mal_average += x.getProbability()[0][0];
            ben_average += x.getProbability()[0][1];
            resultData.add(new ResultItem(x.getImagePath(), x.getProbability()));
            probabilities.add(x.getProbability());
            imagePaths.add(x.getImagePath());
        }

        RecyclerView resultImages = findViewById(R.id.RecyclerResults);
        resultImages.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false));
        ResultsItemAdapter resultAdapter = new ResultsItemAdapter(  this, resultData);
        resultImages.setAdapter(resultAdapter);

        TextView hide = findViewById(R.id.hideAverage);
        final LinearLayout showAverage = findViewById(R.id.showAverage);
        final ConstraintLayout averageCard = findViewById(R.id.averageConstraintLayout);

        hide.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                averageCard.setVisibility(View.INVISIBLE);
                showAverage.setVisibility(View.VISIBLE);
            }
        });

        showAverage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                averageCard.setVisibility(View.VISIBLE);
                showAverage.setVisibility(View.INVISIBLE);
            }
        });


        ben_prob.setText(value.format((float)ben_average*100/mData.size()) + "%");
        mal_prob.setText(value.format((float)mal_average*100/mData.size()) + "%");

        if(((float)mal_average/mData.size()*100) >= MainActivity.threshold){
            verdict.setTextColor(Color.RED);
            verdict.setText("Likely Melanoma");
        }
        else {
            verdict.setTextColor(Color.GRAY);
            verdict.setText("Likely Benign");
        }

        Date currentTime = Calendar.getInstance().getTime();
        SimpleDateFormat sdf = new SimpleDateFormat("HH:mm   dd/MM/yy ");
        String currentDate = sdf.format(currentTime);

        HistoryFragment.addItem(imagePaths, probabilities, currentDate);

        Log.v("Ben average is : ", ""+ben_average);
    }




    /*
    * Run Predictions on mData
    * */
    private void runPredictions(){
        Log.v("Predictions : " , "Size is : " + mData.size());
        ArrayList<String> paths = new ArrayList<>();
        ArrayList<float[][]> probabilties = new ArrayList<>();

        for(int i = 0 ; i < mData.size() ; i++ ){
            try{
                // get path from mdata
                String path = mData.get(i).getImagePath();
                paths.add(path);

                /* Decode image get prediction results */
                Bitmap image = BitmapFactory.decodeFile(path);
                float[][] results = handler.run(image);

                // add float to mdata item
                mData.get(i).setProbability(results);
                probabilties.add(results);
            }
            catch(Exception e){
                Log.v("Predictions : " , "EXCEPTION THROWN ! Error occured ! ");
            }
        }

        /* Get current datetime as string */
        Date currentTime = Calendar.getInstance().getTime();
        SimpleDateFormat sdf = new SimpleDateFormat("HH:mm   dd/MM/yy ");
        String currentDate = sdf.format(currentTime);


        Log.v("Predictions : ","Exiting method");
    }



    /*
    * Refresh and reset the view on the screen
    * */
    private void resetRecyclerView(){
        imagechooserRecView.setLayoutManager(new GridLayoutManager(this, 2, RecyclerView.VERTICAL ,false));
        recAdapter = new ImageItemRecyclerViewAdapter(  this, mData);
        recAdapter.setOnItemClickListener(new ImageItemRecyclerViewAdapter.OnItemClickListener(){
            @Override
            public void onClearItem(int position) {
                mData.remove(position);
                resetRecyclerView();
            }
        });
        imagechooserRecView.setAdapter(recAdapter);
    }

    /* Request permissions to access image gallery
    *  Then add image to image view  */
    public void addImage(){
        /* Can only have 4 image  at maximum */
        if(mData.size() >= 4) {
            toastShort("Maximum number of Images Reached !");
            return;
        }

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED && Build.VERSION.SDK_INT >= 16) {
            // Permission is not granted
            Log.v(null, "--------------PERMISSION NOT GRANTED------------");
            ActivityCompat.requestPermissions(NewRunActivity.this,
                    new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},
                    MY_PERMISSIONS_REQUEST_READ_STORAGE);
        }
        else{
            Log.v(null, "--------------PERMISSION GRANTED------------");
            try{
                Intent photoPickerIntent = new Intent(Intent.ACTION_PICK);
                photoPickerIntent.setType("image/*");
                startActivityForResult(photoPickerIntent, GET_IMAGE_CODE);
            }
            catch(Exception e ){
                e.printStackTrace();
            }
        }
    }

    /* Upon loading image from intent
    *  Save image path, add new ImageItem and reset the imageview */
    protected void onActivityResult(int reqCode, int resultCode, Intent data) {
        super.onActivityResult(reqCode, resultCode, data);
        if (resultCode == RESULT_OK) {
            try {
                /* Decode image from uri.getData*/
                final Uri imageUri = data.getData();
                String path = getRealPathFromURI(imageUri);
                Log.v("Image path is: ", path);

                /* Add the image item newly created and show toast*/
                boolean suceeded = addNewItem(new ImageItem(path));
                if (suceeded){
                    toastShort("Image Item added successfully !");
                }
                else{
                    toastShort("You have reached maximum number of Images !");
                }


            } catch (NullPointerException e) {
                Log.v("Getting image : "," SOMETHING WENT WRONG !");
                e.printStackTrace();
                Toast.makeText(this, "Something went wrong", Toast.LENGTH_LONG).show();
            }
        }else {
            Toast.makeText(this, "You did not pick an Image.", Toast.LENGTH_SHORT).show();
        }
    }

    /*
    * Method to show a short toast */
    public void toastShort(String message){
        final Toast t = Toast.makeText(this, message, Toast.LENGTH_SHORT);
        t.setGravity(Gravity.BOTTOM| Gravity.CENTER_HORIZONTAL, 0, 0);
        t.show();
        new CountDownTimer(1500, 1500)
        {
            public void onTick(long millisUntilFinished) {t.show();}
            public void onFinish() {t.cancel();}
        }.start();
    }

    /*
     * Method to show a short toast */
    public void toastLong(String message){
        final Toast t = Toast.makeText(this, message, Toast.LENGTH_SHORT);
        t.show();
        new CountDownTimer(2900, 2900)
        {
            public void onTick(long millisUntilFinished) {t.show();}
            public void onFinish() {t.cancel();}
        }.start();
    }

    /* Get file path from uri
    * Allows us to get absolute file path from uri returned by image picker intent*/
    private String getRealPathFromURI(Uri contentUri) {
        String[] proj = { MediaStore.Images.Media.DATA };
        CursorLoader loader = new CursorLoader(this, contentUri, proj, null, null, null);
        Cursor cursor = loader.loadInBackground();
        int column_index = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
        cursor.moveToFirst();
        String result = cursor.getString(column_index);
        cursor.close();
        return result;
    }

    /*
    * Async class that handles running predictions in seperate thread.
    *
    * */
    public class OnRunPredictions extends AsyncTask<String, Void, String> {

        private Context mContext;
        private ProgressDialog dialog;

        public OnRunPredictions(Context context) {
            super();
            mContext = context;
            dialog = new ProgressDialog(context);
            dialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
            dialog.setTitle("Loading");
            dialog.setMessage("Running. Please wait...");
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            dialog.show();
        }

        @Override
        protected String doInBackground(String... values) {
            // If you want to use 'values' string in here
            runPredictions();
            return "Done !";
        }

        @Override
        protected void onPostExecute(String result) {
            try {
                Thread.sleep(500);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            dialog.dismiss();
            setContentView(R.layout.average_results);
            populateResultView();
        }
    }

}
