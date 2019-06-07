package com.example.saegeullee.applicationoneproject.Adapter;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import com.example.saegeullee.applicationoneproject.Models.ChatRoom;
import com.example.saegeullee.applicationoneproject.Models.User;
import com.example.saegeullee.applicationoneproject.R;

import java.util.List;
import de.hdodenhof.circleimageview.CircleImageView;

public class ChatRoomListAdapter extends RecyclerView.Adapter<ChatRoomListAdapter.ViewHolder> {

    private static final String TAG = "ChatRoomListAdapter";

    public interface OnItemClickListener {
        void onItemClicked(int position);
    }

    private OnItemClickListener onItemClickListener;

    public void setOnItemClickListener(OnItemClickListener listener) {
        this.onItemClickListener = listener;
    }

    private Context context;
    private List<ChatRoom> chatRoomList;

    public ChatRoomListAdapter(Context context, List<ChatRoom> chatRoomList) {

        this.context = context;
        this.chatRoomList = chatRoomList;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.chatroom_row, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, final int position) {

        ChatRoom chatRoom = chatRoomList.get(position);

        holder.lastMessage.setText(chatRoom.getLastMessage());

        StringBuilder participants_name = new StringBuilder();

        /**
         * 현재 사용자 자신은 userList 에 포함이 안된다.
         */

        Log.d(TAG, "onBindViewHolder: chatRoom userList size : "+ chatRoom.getUserList().size());

        if(chatRoom.getUserList().size() == 1) {
            participants_name.append(chatRoom.getUserList().get(0).getUser_id());

        } else if(chatRoom.getUserList().size() == 2) {

            for(int i = 0; i < chatRoom.getUserList().size(); i++) {
                participants_name.append(chatRoom.getUserList().get(i).getUser_id());
                if(i != 2)
                    participants_name.append(", ");
            }

        } else if(chatRoom.getUserList().size() == 3) {
            for(int i = 0; i < chatRoom.getUserList().size(); i++) {
                participants_name.append(chatRoom.getUserList().get(i).getUser_id());
                if(i != 3)
                    participants_name.append(", ");
            }
            participants_name.append("...");

        } else {
            for(int i = 0; i < 4; i++) {
                participants_name.append(chatRoom.getUserList().get(i).getUser_id());
                if(i != 3)
                    participants_name.append(", ");
            }
            participants_name.append("...");
        }

        if(chatRoom.getUserList().size() >= 2) {
            holder.chatRoomMemberNumber.setText(String.valueOf(chatRoom.getUserList().size() + 1));
        } else {
            holder.chatRoomMemberNumber.setText("");
        }

        holder.user_name.setText(participants_name.toString());
        holder.timestamp.setText(chatRoom.getTimestamp());

        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(onItemClickListener != null) {
                    onItemClickListener.onItemClicked(position);
                }
            }
        });
    }

    @Override
    public int getItemCount() {
        return chatRoomList.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {

        private CircleImageView chatImage;
        private TextView user_name, lastMessage, timestamp, chatRoomMemberNumber;
        private Button chatMessageNum;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            chatImage = itemView.findViewById(R.id.userImage);
            user_name = itemView.findViewById(R.id.userName);
            lastMessage = itemView.findViewById(R.id.chat_last_message);
            timestamp = itemView.findViewById(R.id.chat_date);
            chatMessageNum = itemView.findViewById(R.id.messageNum);
            chatRoomMemberNumber = itemView.findViewById(R.id.chatRoomMemberNumber);
        }
    }
}
