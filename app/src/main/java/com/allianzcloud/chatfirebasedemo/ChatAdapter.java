package com.allianzcloud.chatfirebasedemo;

import android.annotation.SuppressLint;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.VideoView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.allianzcloud.chatfirebasedemo.Model.MessagesVo;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;

public class ChatAdapter extends RecyclerView.Adapter<ChatAdapter.MyViewHolder> {

    private ArrayList<MessagesVo> eventVos;
    private Context context;
    String sender;

    public ChatAdapter(Context context, ArrayList<MessagesVo> eventVos, String sender) {
        this.context = context;
        this.eventVos = eventVos;
        this.sender = sender;
    }

    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.chat_item, parent, false);

        return new MyViewHolder(itemView);
    }

    class MyViewHolder extends RecyclerView.ViewHolder {

        LinearLayout llSender, llReceiver;
        TextView txtSender, txtReceiver;
        ImageView imgSender, imgReceiver;
        VideoView vidSender;

        MyViewHolder(View view) {
            super(view);

            llSender = view.findViewById(R.id.llSender);
            llReceiver = view.findViewById(R.id.llReceiver);
            txtSender = view.findViewById(R.id.txtSender);
            txtReceiver = view.findViewById(R.id.txtReceiver);
            imgSender = view.findViewById(R.id.imgSender);
            imgReceiver = view.findViewById(R.id.imgReceiver);
            vidSender = view.findViewById(R.id.vidSender);
        }
    }

    @SuppressLint("SetTextI18n")
    @Override
    public void onBindViewHolder(@NonNull final MyViewHolder holder, final int position) {
        final MessagesVo data = eventVos.get(position);

        if (data.getSender().equalsIgnoreCase(sender)) {
            holder.llSender.setVisibility(View.VISIBLE);
            holder.llReceiver.setVisibility(View.GONE);

            if (data.getType().equalsIgnoreCase("text")) {
                holder.txtSender.setVisibility(View.VISIBLE);
                holder.imgSender.setVisibility(View.GONE);
                holder.vidSender.setVisibility(View.GONE);

                holder.txtSender.setText(data.getMsg());
            } else if (data.getType().equalsIgnoreCase("image")){
                holder.txtSender.setVisibility(View.GONE);
                holder.imgSender.setVisibility(View.VISIBLE);
                holder.vidSender.setVisibility(View.GONE);

                Picasso.get()
                        .load(data.getFile())
                        .resize(300, 300)
                        .centerCrop()
                        .into(holder.imgSender);
            } else {
                holder.txtSender.setVisibility(View.GONE);
                holder.imgSender.setVisibility(View.GONE);
                holder.vidSender.setVisibility(View.VISIBLE);

                holder.vidSender.setVideoPath(data.getFile());
                holder.vidSender.start();
            }
        } else {
            holder.llSender.setVisibility(View.GONE);
            holder.llReceiver.setVisibility(View.VISIBLE);

            if (data.getType().equalsIgnoreCase("text")) {
                holder.txtReceiver.setVisibility(View.VISIBLE);
                holder.imgReceiver.setVisibility(View.GONE);

                holder.txtReceiver.setText(data.getMsg());
            } else {
                holder.txtReceiver.setVisibility(View.GONE);
                holder.imgReceiver.setVisibility(View.VISIBLE);
            }
        }
    }

    private static ClickListener clickListener;

    public void setOnItemClickListener(ClickListener clickListener) {
        ChatAdapter.clickListener = clickListener;
    }

    public interface ClickListener {
        void onItemClick(MessagesVo data);
    }

    @Override
    public int getItemCount() {
        return eventVos.size();
    }

}