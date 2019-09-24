package com.allianzcloud.chatfirebasedemo;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Base64;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.allianzcloud.chatfirebasedemo.Model.MessagesVo;
import com.allianzcloud.chatfirebasedemo.Util.Constant;
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
        ImageView imgSender, imgReceiver , imgSP , imgRP;

        MyViewHolder(View view) {
            super(view);

            llSender = view.findViewById(R.id.llSender);
            llReceiver = view.findViewById(R.id.llReceiver);
            txtSender = view.findViewById(R.id.txtSender);
            txtReceiver = view.findViewById(R.id.txtReceiver);
            imgSender = view.findViewById(R.id.imgSender);
            imgReceiver = view.findViewById(R.id.imgReceiver);
            imgRP = view.findViewById(R.id.imgRP);
            imgSP = view.findViewById(R.id.imgSP);

        }
    }

    @SuppressLint("SetTextI18n")
    @Override
    public void onBindViewHolder(@NonNull final MyViewHolder holder, final int position) {
        final MessagesVo data = eventVos.get(position);

        if (data.getSender().equalsIgnoreCase(sender)) {
            holder.llSender.setVisibility(View.VISIBLE);
            holder.llReceiver.setVisibility(View.GONE);

            if (data.getType().equalsIgnoreCase(Constant.typeText)) {
                holder.txtSender.setVisibility(View.VISIBLE);
                holder.imgSender.setVisibility(View.GONE);
                holder.imgSP.setVisibility(View.GONE);

                holder.txtSender.setText(data.getMsg());
            } else if (data.getType().equalsIgnoreCase(Constant.typeImage)) {
                holder.txtSender.setVisibility(View.GONE);
                holder.imgSender.setVisibility(View.VISIBLE);
                holder.imgSender.setAlpha(1f);
                holder.imgSP.setVisibility(View.GONE);

                Picasso.get()
                        .load(data.getFile())
                        .resize(300, 300)
                        .centerCrop()
                        .into(holder.imgSender);
            } else if (data.getType().equalsIgnoreCase(Constant.typeVideo)) {
                holder.txtSender.setVisibility(View.GONE);
                holder.imgSender.setVisibility(View.VISIBLE);
                holder.imgSender.setAlpha(0.5f);
                holder.imgSP.setVisibility(View.VISIBLE);

                byte[] decodedByte = Base64.decode(data.getThumb(), 0);
                Bitmap bitmap = BitmapFactory.decodeByteArray(decodedByte, 0, decodedByte.length);

                holder.imgSender.setImageBitmap(bitmap);
            } else {
                holder.txtSender.setVisibility(View.GONE);
                holder.imgSender.setVisibility(View.GONE);
                holder.imgSender.setAlpha(1f);
                holder.imgSP.setVisibility(View.VISIBLE);
            }
        } else {
            holder.llSender.setVisibility(View.GONE);
            holder.llReceiver.setVisibility(View.VISIBLE);

            if (data.getType().equalsIgnoreCase(Constant.typeText)) {
                holder.txtReceiver.setVisibility(View.VISIBLE);
                holder.imgReceiver.setVisibility(View.GONE);
                holder.imgRP.setVisibility(View.GONE);

                holder.txtReceiver.setText(data.getMsg());
            } else if (data.getType().equalsIgnoreCase(Constant.typeImage)) {
                holder.txtReceiver.setVisibility(View.GONE);
                holder.imgReceiver.setVisibility(View.VISIBLE);
                holder.imgReceiver.setAlpha(1f);
                holder.imgRP.setVisibility(View.GONE);

                Picasso.get()
                        .load(data.getFile())
                        .resize(300, 300)
                        .centerCrop()
                        .into(holder.imgReceiver);

            } else if (data.getType().equalsIgnoreCase(Constant.typeVideo)) {
                holder.txtReceiver.setVisibility(View.GONE);
                holder.imgReceiver.setVisibility(View.VISIBLE);
                holder.imgReceiver.setAlpha(0.5f);
                holder.imgRP.setVisibility(View.VISIBLE);

                byte[] decodedByte = Base64.decode(data.getThumb(), 0);
                Bitmap bitmap = BitmapFactory.decodeByteArray(decodedByte, 0, decodedByte.length);

                holder.imgReceiver.setImageBitmap(bitmap);
            } else {
                holder.txtReceiver.setVisibility(View.GONE);
                holder.imgReceiver.setVisibility(View.GONE);
                holder.imgReceiver.setAlpha(1f);
                holder.imgRP.setVisibility(View.VISIBLE);
            }
        }

        holder.imgSP.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (data.getType().equalsIgnoreCase(Constant.typeAudio)){
                    clickListener.onAudioClick(data);
                } else {
                    clickListener.onVideoClick(data);
                }

            }
        });
        holder.imgRP.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                clickListener.onVideoClick(data);
            }
        });
    }

    private static ClickListener clickListener;

    void setOnItemClickListener(ClickListener clickListener) {
        ChatAdapter.clickListener = clickListener;
    }

    public interface ClickListener {
        void onVideoClick(MessagesVo data);
        void onAudioClick(MessagesVo data);
    }

    @Override
    public int getItemCount() {
        return eventVos.size();
    }

}