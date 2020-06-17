package com.example.finalprojectmelanoma;

import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.example.finalprojectmelanoma.MultipleRunActivity.ResultItem;
import com.example.finalprojectmelanoma.MultipleRunActivity.ResultsItemAdapter;

import java.text.DecimalFormat;
import java.util.ArrayList;

public class DisplayResultsActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Intent intent = getIntent();
        boolean isMultiple = intent.getBooleanExtra("multiple", false);
        if(isMultiple){
            /* Display data as if it is a multiple */
            setContentView(R.layout.average_results);
            TextView ben_prob = findViewById(R.id.ben_probability);
            TextView mal_prob = findViewById(R.id.mal_probability);

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

            /* Get probabilities, get */
            ArrayList<String> paths = intent.getStringArrayListExtra("paths");
            ArrayList<float[][]> probs = (ArrayList<float[][]>) intent.getSerializableExtra("probability");
            ArrayList<ResultItem> mData = new ArrayList<>();


            float mal_avg = (float) 0.0;
            float ben_avg = (float) 0.0;
            try{
                for(int i = 0; i < paths.size(); i++){
                    mal_avg += probs.get(i)[0][0];
                    ben_avg += probs.get(i)[0][1];
                    mData.add(new ResultItem(paths.get(i),probs.get(i)));
                }
            }catch (NullPointerException e){}

            RecyclerView resultImages = findViewById(R.id.RecyclerResults);
            resultImages.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false));
            ResultsItemAdapter resultAdapter = new ResultsItemAdapter(  this, mData);
            resultImages.setAdapter(resultAdapter);

            DecimalFormat value = new DecimalFormat("#.#");
            ben_prob.setText(value.format((float)ben_avg/mData.size()*100) + "%");
            mal_prob.setText(value.format((float)mal_avg/mData.size()*100) + "%");

            TextView verdict = findViewById(R.id.verdict);
            if(((float)mal_avg/mData.size()*100) >= MainActivity.threshold){
                verdict.setTextColor(Color.RED);
                verdict.setText("Likely Melanoma");
            }
            else {
                verdict.setTextColor(Color.GRAY);
                verdict.setText("Likely Benign");
            }


        }
        else{
            /* Display data as if it is a single image */
            setContentView(R.layout.results);
            ImageView img = findViewById(R.id.imgView);
            TextView mel = findViewById(R.id.melTxt);
            TextView ben = findViewById(R.id.benignTxt);
            Button predictAnoth = findViewById(R.id.predictAnotherBtn);
            predictAnoth.setVisibility(View.INVISIBLE);

            float[] probs = intent.getFloatArrayExtra("probability");
            String path = intent.getStringExtra("image");

            DecimalFormat value = new DecimalFormat("#.#");

            String melpreds = "" + value.format(probs[0]) + "%";
            String benpreds = "" + value.format(probs[1]) + "%";
            mel.setText(melpreds);
            ben.setText(benpreds);

            Bitmap image = BitmapFactory.decodeFile(path);
            img.setImageBitmap(image);

        }

    }

}
