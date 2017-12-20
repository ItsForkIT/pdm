package com.disarm.surakshit.pdm;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.List;

/**
 * Created by naman on 10/8/17.
 */

public class FileAdapter extends RecyclerView.Adapter<FileAdapter.MyViewHolder> {
    List<FileRecord> fr;
    FileAdapter(List<FileRecord> f){
        fr = f;
    }

    public class MyViewHolder extends RecyclerView.ViewHolder {
        public TextView no, source, type, date;
        public ImageView ivType;

        public MyViewHolder(View view) {
            super(view);
            no = (TextView) view.findViewById(R.id.tvNo);
            source = (TextView) view.findViewById(R.id.tvSource);
            type = (TextView) view.findViewById(R.id.tvtype);
            date = (TextView) view.findViewById(R.id.tvTime);
            ivType = (ImageView) view.findViewById(R.id.ivType);
        }
    }

    @Override
    public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.list_view,parent,false);
        return new MyViewHolder(v);
    }

    @Override
    public void onBindViewHolder(MyViewHolder holder, int position) {
        FileRecord f = fr.get(position);
        holder.no.setText(f.getNo()+"");
        holder.date.setText(f.getTime());
        holder.source.setText(f.getSource());
        holder.type.setText(f.getType());
        switch (f.getType()){
            case "IMG":
                holder.ivType.setImageResource(R.drawable.photo);
                break;
            case "VID":
                holder.ivType.setImageResource(R.drawable.video);
                break;
            case "TXT":
                holder.ivType.setImageResource(R.drawable.text);
                break;
            case "AUD":
                holder.ivType.setImageResource(R.drawable.audio);
                break;
            case "SMS":
                holder.ivType.setImageResource(R.drawable.text);

        }
    }

    @Override
    public int getItemCount() {
        return fr.size();
    }
}
