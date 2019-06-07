package com.example.saegeullee.applicationoneproject.Adapter;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.example.saegeullee.applicationoneproject.Models.ChatMessage;
import com.example.saegeullee.applicationoneproject.R;
import com.example.saegeullee.applicationoneproject.Utility.SharedPrefManager;
import com.nostra13.universalimageloader.core.ImageLoader;

import org.w3c.dom.Text;

import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

public class ChatRoomTempAdapter extends RecyclerView.Adapter<ChatRoomTempAdapter.ViewHolder> {

    private Context context;
    private List<ChatMessage> messageList;

    public ChatRoomTempAdapter(Context context, List<ChatMessage> messageList) {
        this.context = context;
        this.messageList = messageList;
    }

    @NonNull
    @Override
    public ChatRoomTempAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.chatmessage_row_mine, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ChatRoomTempAdapter.ViewHolder holder, int position) {

        ChatMessage chatMessage = messageList.get(position);

        holder.messageTextView.setText(chatMessage.getMessage());
        holder.dateTextView.setText(chatMessage.getDate());

    }

    @Override
    public int getItemCount() {
        return messageList.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {

        TextView messageTextView, dateTextView;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            messageTextView = itemView.findViewById(R.id.chatmessage_content);
            dateTextView = itemView.findViewById(R.id.chatmessage_date);
        }
    }
}