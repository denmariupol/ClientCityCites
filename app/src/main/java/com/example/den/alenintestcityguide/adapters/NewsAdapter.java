package com.example.den.alenintestcityguide.adapters;

import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.example.den.alenintestcityguide.R;
import com.example.den.alenintestcityguide.sqlite.DB;
import com.example.den.alenintestcityguide.utils.Util;

/**
 * Created by den on 2017-04-10.
 */

public class NewsAdapter extends RecyclerView.Adapter<NewsAdapter.ViewHolder> {

    private Cursor cursor;
    private DB db;
    private Util util;
    //cursor col's indexes
    private int colId,colTitle,colText,colImportant,colDate,colImage;
    private Listener listener;

    public NewsAdapter(Cursor cursor, DB db, Util util) {
        this.cursor = cursor;
        this.db = db;
        this.util = util;
        getColsIndex();
    }


    public void setListener(Listener listener) {
        this.listener = listener;
    }


    //change cursor
    public void swapCursor(Cursor cursor){
        this.cursor = cursor;
        notifyDataSetChanged();
    }


    //getting cursor col's indexes
    private void getColsIndex(){
        colId = cursor.getColumnIndex(DB.COLUMN_ID);
        colTitle = cursor.getColumnIndex(DB.COLUMN_TITLE);
        colText = cursor.getColumnIndex(DB.COLUMN_TEXT);
        colImportant = cursor.getColumnIndex(DB.COLUMN_IMPORTANT);
        colDate = cursor.getColumnIndex(DB.COLUMN_DATE);
        colImage = cursor.getColumnIndex(DB.COLUMN_IMAGE);
    }


    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        CardView cardView = (CardView) LayoutInflater.from(parent.getContext()).inflate(R.layout.card_view,parent,false);

        return new ViewHolder(cardView);
    }


    @Override
    public void onBindViewHolder(ViewHolder holder, final int position) {
        CardView cardView = holder.cardView;
        cursor.moveToPosition(position);
        LinearLayout cardLayout = (LinearLayout)cardView.findViewById(R.id.card_layout);
        if(cursor.getInt(colImportant) == 1)
            cardLayout.setBackgroundColor(Color.parseColor("#DCDCDC"));

        ImageView imageView = (ImageView)cardView.findViewById(R.id.news_image);
        byte[] imageArray = cursor.getBlob(colImage);
        Bitmap bitmap = util.getImageFromBytes(imageArray);
        imageView.setImageBitmap(bitmap);

        TextView titleView = (TextView)cardView.findViewById(R.id.news_title);
        String title = cursor.getString(colTitle);
        titleView.setText(title);

        TextView dataView = (TextView)cardView.findViewById(R.id.data);
        String data = cursor.getString(colDate);
        String formatedData = util.getStringFromDate(data);
        dataView.setText(formatedData);

        cardView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(listener != null)
                    listener.onClick(position);
            }
        });
    }


    @Override
    public int getItemCount() {
        return cursor != null ? cursor.getCount() : 0;
    }


    @Override
    public int getItemViewType(int position) {
        cursor.moveToPosition(position);
        if(cursor.getInt(colImportant) == 1)
            return 1;
        else
            return position;
    }

    public class ViewHolder extends RecyclerView.ViewHolder {

        private CardView cardView;

        public ViewHolder(CardView itemView) {
            super(itemView);
            cardView = itemView;
        }
    }
    public interface Listener{
        public void onClick(int position);
    }
}
