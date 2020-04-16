package com.example.finalprojectmelanoma.MultipleRunActivity;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.finalprojectmelanoma.R;

import java.util.List;

public class ImageItemRecyclerViewAdapter extends RecyclerView.Adapter<ImageItemRecyclerViewAdapter.MyViewHolder> {

    Context mContext;
    private List<ImageItem> mData;
    private OnItemClickListener mListener;

    public ImageItemRecyclerViewAdapter(Context context, List<ImageItem> mData){
        this.mContext = context;
        this.mData = mData;
    }

    public void setOnItemClickListener(OnItemClickListener listener) {
        mListener = listener;
    }

    public interface OnItemClickListener {
        void onClearItem(int position);
    }


    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v;
        v = LayoutInflater.from(mContext).inflate(R.layout.image_item, parent,false);
        MyViewHolder viewHolder = new MyViewHolder(v, mListener);

        return viewHolder;
    }

    @Override
    public void onBindViewHolder(@NonNull MyViewHolder holder, int position) {
        /* Set TextView Values*/
        int count = position + 1;
        Log.v("POSITION IS : " , ""+position);

        // If no imagepath in object remove delete item onclicklistener.
        setLayout(holder, position);
    }

    public void setLayout(@NonNull MyViewHolder holder, int position){
        // Set imageview as image specified
        String path = mData.get(position).getImagePath();
        Bitmap image = BitmapFactory.decodeFile(path);
        //image = Bitmap.createScaledBitmap(image, 1000, 1000, false);
        holder.img.setImageBitmap(image);

        // Set count
        int count = position + 1;
        holder.imageCount.setText("" + count + "/4");

        // Set outline as second drawable
        holder.border.setBackgroundDrawable(mContext.getResources().getDrawable(R.drawable.customborder));
    }


    @Override
    public int getItemCount() {
        return mData.size();
    }

    public static class MyViewHolder extends RecyclerView.ViewHolder{
        private ImageView img;
        private ImageView delete;
        private LinearLayout border;
        private TextView imageCount;


        public MyViewHolder(View itemView, final OnItemClickListener listener){
            super(itemView);

            /* Define items */
            border = itemView.findViewById(R.id.item_border);
            imageCount = itemView.findViewById(R.id.item_image_count);

            /* Define Imageview */
            img = (ImageView) itemView.findViewById(R.id.item_img_skin);
            delete = (ImageView) itemView.findViewById(R.id.item_delete);

            delete.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (listener != null) {
                        int position = getAdapterPosition();
                        if (position != RecyclerView.NO_POSITION) {
                            listener.onClearItem(position);
                        }
                    }
                }
            });
        }
    }
}
