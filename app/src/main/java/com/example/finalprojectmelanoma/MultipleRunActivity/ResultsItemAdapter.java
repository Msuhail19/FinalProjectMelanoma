package com.example.finalprojectmelanoma.MultipleRunActivity;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.finalprojectmelanoma.MainActivity;
import com.example.finalprojectmelanoma.R;

import java.text.DecimalFormat;
import java.util.List;


public class ResultsItemAdapter extends RecyclerView.Adapter<ResultsItemAdapter.MyViewHolder> {

    private List<ResultItem> mData;
    private LayoutInflater mInflater;
    private OnItemClickListener mListener;

    // data is passed into the constructor
    public ResultsItemAdapter(Context context, List<ResultItem> data) {
        this.mInflater = LayoutInflater.from(context);
        this.mData = data;
    }

    public interface OnItemClickListener {
    }

    // inflates the row layout from xml when needed
    @Override
    public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = mInflater.inflate(R.layout.results_item2, parent, false);
        MyViewHolder viewHolder = new MyViewHolder(view, mListener);

        return viewHolder;
    }

    // binds the data to the TextView in each row
    @Override
    public void onBindViewHolder(@NonNull MyViewHolder holder, int position) {
        float[][] probability = mData.get(position).getProbability();
        String path = mData.get(position).getImagePath();
        Bitmap image = BitmapFactory.decodeFile(path);
        image = Bitmap.createScaledBitmap(image, 1000, 1000, false);


        /* Set recyclerview item equal to result item values */
        DecimalFormat value = new DecimalFormat("#.#");
        holder.img.setImageBitmap(image);
        holder.mal_txt.setText(value.format(probability[0][0]*100) + "%");
        holder.ben_txt.setText(value.format(probability[0][1]*100) + "%");


    }

    // total number of rows
    @Override
    public int getItemCount() {
        return mData.size();
    }


    public static class MyViewHolder extends RecyclerView.ViewHolder{
        private ImageView img;
        private TextView mal_txt;
        private TextView ben_txt;


        public MyViewHolder(View itemView, final OnItemClickListener listener){
            super(itemView);

            img = itemView.findViewById(R.id.result_item_image);
            mal_txt = itemView.findViewById(R.id.mal_item_text);
            ben_txt = itemView.findViewById(R.id.ben_item_text);

        }
    }
}