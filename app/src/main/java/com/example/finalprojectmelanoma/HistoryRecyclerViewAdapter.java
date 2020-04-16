package com.example.finalprojectmelanoma;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.text.DecimalFormat;
import java.util.List;

public class HistoryRecyclerViewAdapter extends RecyclerView.Adapter<HistoryRecyclerViewAdapter.MyViewHolder> {

    Context mContext;
    List<RunDetails> mData;
    private OnItemClickListener mListener;

    public HistoryRecyclerViewAdapter(Context context, List<RunDetails> mDetails){
        this.mContext = context;
        this.mData = mDetails;
    }

    public void setOnItemClickListener(OnItemClickListener listener) {
        mListener = listener;
    }

    public interface OnItemClickListener {
        void onDeleteClick(int position);
        void onClickShowResults(int position);
    }


    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v;
        v = LayoutInflater.from(mContext).inflate(R.layout.item_history, parent,false);
        MyViewHolder viewHolder = new MyViewHolder(v, mListener);

        return viewHolder;
    }

    @Override
    public void onBindViewHolder(@NonNull MyViewHolder holder, int position) {
        /* Set TextView Values*/
        int count = position + 1;
        holder.tv_title.setText("Run " + count + "  ");

        if(!mData.get(position).isMultipleImages()){
            /* Get probabilities and set Text View*/
            float[] prob = mData.get(position).getProbability();

            DecimalFormat df = new DecimalFormat("#.#");
            holder.tv_mel.setText("Benign: " +  df.format(prob[1]) + "% ");
            holder.tv_ben.setText("Malignant: " +  df.format(prob[0]) + "% ");

            /*Set Date Accordingly*/
            String strDate = mData.get(position).getDate();
            holder.tv_date.setText(strDate);

            /*Set Image*/
            String path = mData.get(position).getImagePath();

            /* Set image file !  */
            Bitmap image = BitmapFactory.decodeFile(path);
            holder.img.setImageBitmap(image);
        }
        else{
            /* Code for adding multiple images resultview */
            holder.tv_title.setText("Run " + count + " (Average Results) ");
            List<String> paths = mData.get(position).getImagePaths();
            List<float[][]> probabilties = mData.get(position).getProbabilities();

            /* Set image file !  */
            Bitmap image = BitmapFactory.decodeFile(paths.get(0));
            holder.img.setImageBitmap(image);
            holder.number_of_images.setVisibility(View.VISIBLE);
            holder.number_of_images.setText(""+paths.size());

            DecimalFormat df = new DecimalFormat("#.##");

            float sum_benign = (float)0.0;
            float sum_malignant =(float)0.0;
            for(float[][] prob : probabilties){
                sum_malignant += prob[0][0];
                sum_benign += prob[0][1];
            }

            holder.tv_mel.setText("Benign: " +  df.format((float)sum_benign/probabilties.size()) + "% ");
            holder.tv_ben.setText("Malignant: " +  df.format((float)sum_malignant/probabilties.size()) + "% ");
            String strDate = mData.get(position).getDate();
            holder.tv_date.setText(strDate);

            
        }



        /* Add onclick listener to remove item.*/


    }

    @Override
    public int getItemCount() {
        return mData.size();
    }

    public static class MyViewHolder extends RecyclerView.ViewHolder{
        private TextView tv_title;
        private TextView tv_date;
        private TextView tv_risk;
        private TextView tv_mel;
        private TextView tv_ben;
        private ImageView img;
        private ImageView delete;
        private TextView number_of_images;

        private LinearLayout textBlock;



        public MyViewHolder(View itemView, final OnItemClickListener listener){
            super(itemView);

            /* Define each textview to alter */
            tv_title = (TextView) itemView.findViewById(R.id.item_title);
            tv_date = (TextView) itemView.findViewById(R.id.item_date);
            tv_risk = (TextView) itemView.findViewById(R.id.item_verdict);
            tv_mel = (TextView) itemView.findViewById(R.id.item_mal_prob);
            tv_ben = (TextView) itemView.findViewById(R.id.item_benign_prob);
            number_of_images = itemView.findViewById(R.id.number_of_images);

            /* Define Imageview */
            img = (ImageView) itemView.findViewById(R.id.item_img_skin);
            delete = (ImageView) itemView.findViewById(R.id.item_delete);

            /* Define Layout */
            number_of_images.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (listener != null) {
                        int position = getAdapterPosition();
                        if (position != RecyclerView.NO_POSITION) {
                            listener.onClickShowResults(position);
                        }
                    }
                }
            });

            delete.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (listener != null) {
                        int position = getAdapterPosition();
                        if (position != RecyclerView.NO_POSITION) {
                            listener.onDeleteClick(position);
                        }
                    }
                }
            });


        }
    }


}
