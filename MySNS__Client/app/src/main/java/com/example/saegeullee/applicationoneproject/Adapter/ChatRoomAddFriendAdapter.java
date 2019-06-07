package com.example.saegeullee.applicationoneproject.Adapter;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RadioButton;
import android.widget.TextView;

import com.example.saegeullee.applicationoneproject.Models.User;
import com.example.saegeullee.applicationoneproject.R;

import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

public class ChatRoomAddFriendAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    private static final String TAG = "ChatRoomAddFriendAdapte";

    // 이미 방에 해당 사용자가 없거나 이미 방에 들어 있거나
    private static final int VIEW_TYPE_NOT = 1;
    private static final int VIEW_TYPE_ALREADY = 2;

    public interface OnRadioButtonClickListener {
        void onRadioButtonChecked(int position);
        void onRadioButtonUnChecked(int position);
    }

    private OnRadioButtonClickListener onRadioButtonClickListener;

    public void setOnRadioButtonClickListener(OnRadioButtonClickListener onRadioButtonClickListener) {
        this.onRadioButtonClickListener = onRadioButtonClickListener;
    }

    private Context context;
    private List<User> friendList;

    public ChatRoomAddFriendAdapter(Context context, List<User> friendList) {
        this.context = context;
        this.friendList = friendList;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

        LayoutInflater layoutInflater = LayoutInflater.from(parent.getContext());
        RecyclerView.ViewHolder viewHolder = null;

        switch (viewType) {
            case VIEW_TYPE_ALREADY:
                View viewAlready = layoutInflater.inflate(R.layout.chatroom_addfriend_already_row, parent, false);
                viewHolder = new yetViewHolder(viewAlready);
                break;
            case VIEW_TYPE_NOT:
                View viewNot = layoutInflater.inflate(R.layout.chatroom_addfriend_row, parent, false);
                viewHolder = new ViewHolder(viewNot);
                break;
        }

        return viewHolder;
    }

    @Override
    public int getItemViewType(int position) {

        if(friendList.get(position).isInChatRoom()) {
            return VIEW_TYPE_ALREADY;
        } else {
            return VIEW_TYPE_NOT;
        }
    }

    @Override
    public void onBindViewHolder(@NonNull final RecyclerView.ViewHolder holder, final int position) {

        User user = friendList.get(position);

        switch (holder.getItemViewType()) {
            case VIEW_TYPE_NOT:
                ((ViewHolder) holder).bind(user);
                ((ViewHolder) holder).radioButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if(!((ViewHolder) holder).radioButton.isSelected()) {
                            ((ViewHolder) holder).radioButton.setChecked(true);
                            ((ViewHolder) holder).radioButton.setSelected(true);
                            if(onRadioButtonClickListener != null) {
                                onRadioButtonClickListener.onRadioButtonChecked(position);
                            }
                        } else {
                            ((ViewHolder) holder).radioButton.setChecked(false);
                            ((ViewHolder) holder).radioButton.setSelected(false);
                            if(onRadioButtonClickListener != null) {
                                onRadioButtonClickListener.onRadioButtonUnChecked(position);
                            }
                        }
                    }
                });
                break;

            case VIEW_TYPE_ALREADY:
                ((yetViewHolder) holder).bind(user);
                break;
        }
    }

    @Override
    public int getItemCount() {
        return friendList.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {

        private CircleImageView userImage;
        private TextView userName;
        private RadioButton radioButton;

        public ViewHolder(View itemView) {
            super(itemView);
            userImage = itemView.findViewById(R.id.userImage);
            userName = itemView.findViewById(R.id.userName);
            radioButton = itemView.findViewById(R.id.radioButton);
        }

        void bind(User user) {
            userName.setText(user.getUser_id());
            radioButton.setSelected(user.isRadioBtnChecked());
            radioButton.setChecked(user.isRadioBtnChecked());
        }
    }

    public class yetViewHolder extends RecyclerView.ViewHolder {

        private CircleImageView userImage;
        private TextView userName;

        public yetViewHolder(View itemView) {
            super(itemView);
            userImage = itemView.findViewById(R.id.userImage);
            userName = itemView.findViewById(R.id.userName);
        }

        void bind(User user) {
            userName.setText(user.getUser_id());
        }
    }
}
