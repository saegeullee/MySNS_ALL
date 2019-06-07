package com.example.saegeullee.applicationoneproject.Adapter;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.example.saegeullee.applicationoneproject.Models.User;
import com.example.saegeullee.applicationoneproject.R;

import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

public class ChatRoomAddFriendTopAdapter extends RecyclerView.Adapter<ChatRoomAddFriendTopAdapter.ViewHolder> {

    public interface OnCancelBtnClickListener {
        void onCancelBtnClicked(int position);
    }

    private OnCancelBtnClickListener onCancelBtnClickListener;

    public void setOnCancelBtnClickListener(OnCancelBtnClickListener onCancelBtnClickListener) {
        this.onCancelBtnClickListener = onCancelBtnClickListener;
    }

    private Context context;
    private List<User> friendList;

    public ChatRoomAddFriendTopAdapter(Context context, List<User> friendList) {
        this.context = context;
        this.friendList = friendList;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.chatroom_addfriend_top_row, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull final ViewHolder holder, final int position) {

        User user = friendList.get(position);
        holder.user_name.setText(user.getUser_id());

        holder.cancelSelection.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(onCancelBtnClickListener != null) {
                    onCancelBtnClickListener.onCancelBtnClicked(position);
                }
            }
        });

    }

    @Override
    public int getItemCount() {
        return friendList.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {

        private CircleImageView profile_image, cancelSelection;
        private TextView user_name;

        public ViewHolder(View itemView) {
            super(itemView);
            profile_image = itemView.findViewById(R.id.profile_image);
            user_name = itemView.findViewById(R.id.user_name);
            cancelSelection = itemView.findViewById(R.id.cancelBtn);
        }
    }
}
