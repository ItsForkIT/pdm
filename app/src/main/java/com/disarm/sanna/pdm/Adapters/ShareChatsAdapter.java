package com.disarm.sanna.pdm.Adapters;

import android.content.Context;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.disarm.sanna.pdm.R;

import java.util.ArrayList;

/**
 * Created by arka on 23/9/16.
 */

public class ShareChatsAdapter extends BaseAdapter {
    private ArrayList<String> msg;
    private Context context;
    private String sender;
    private ShareChatsInterface anInterface;

    public ShareChatsAdapter(ArrayList<String> msg, String sender, Context context) {
        this.msg = msg;
        this.sender = sender;
        this.context = context;
    }

    @Override
    public int getCount() {
        return msg.size();
    }

    @Override
    public Object getItem(int position) {
        return msg.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(final int position, View view, ViewGroup viewGroup) {
        View rowView = view;
        ViewHolder holder;

        if(rowView == null) {
            LayoutInflater inflater = (LayoutInflater)context.getSystemService(
                    Context.LAYOUT_INFLATER_SERVICE);
            rowView = inflater.inflate(R.layout.share_activity_row, viewGroup, false);
            holder = new ViewHolder(rowView);
            rowView.setTag(holder);
        } else {
            holder = (ViewHolder)rowView.getTag();
        }

        holder.msgText.setText(msg.get(position));

        LinearLayout layout = (LinearLayout)rowView.findViewById(
                R.id.share_bubble_layout);
        LinearLayout layoutParent = (LinearLayout)rowView.findViewById(
                R.id.share_bubble_layout_parent);

        if(msg.get(position).split("_")[3].equals(sender)) {
            layout.setBackgroundResource(R.drawable.bubble1);
            layoutParent.setGravity(Gravity.LEFT);
        } else {
            layout.setBackgroundResource(R.drawable.bubble2);
            layoutParent.setGravity(Gravity.RIGHT);
        }

        rowView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(anInterface != null) {
                    anInterface.onClick(view, position);
                }
            }
        });

        return rowView;
    }

    public class ViewHolder {
        public TextView msgText;

        public ViewHolder(View item) {
            msgText = (TextView)item.findViewById(R.id.share_row_message_text);
        }
    }

    public void setAnInterface(ShareChatsInterface anInterface) {
        this.anInterface = anInterface;
    }

    public interface ShareChatsInterface {
        public void onClick(View row, int position);
    }
}
