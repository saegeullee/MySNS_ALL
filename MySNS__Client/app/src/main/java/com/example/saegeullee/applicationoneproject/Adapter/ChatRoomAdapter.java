package com.example.saegeullee.applicationoneproject.Adapter;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.example.saegeullee.applicationoneproject.Models.ChatMessage;
import com.example.saegeullee.applicationoneproject.R;
import com.example.saegeullee.applicationoneproject.Utility.SharedPrefManager;
import com.example.saegeullee.applicationoneproject.Utility.TimeManager;
import com.nostra13.universalimageloader.core.ImageLoader;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.List;
import java.util.StringTokenizer;

import de.hdodenhof.circleimageview.CircleImageView;

public class ChatRoomAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private static final String TAG = "ChatRoomAdapter";

    private static final int VIEW_TYPE_ME = 1;
    private static final int VIEW_TYPE_OTHER = 2;
    //사용자 초대, 사용자 나감 등의 메시지 표시=
    private static final int VIEW_TYPE_NOTICE = 3;

    private Context context;
    private List<ChatMessage> messageList;
    private ChatMessage chatMessage;

    public ChatRoomAdapter(Context context, List<ChatMessage> messageList) {
        this.context = context;
        this.messageList = messageList;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

        LayoutInflater layoutInflater = LayoutInflater.from(parent.getContext());

        RecyclerView.ViewHolder viewHolder = null;

        switch (viewType) {
            case VIEW_TYPE_ME:
                View viewChatMine = layoutInflater.inflate(R.layout.chatmessage_row_mine, parent, false);
                viewHolder = new MyChatViewHolder(viewChatMine);
                break;
            case VIEW_TYPE_OTHER:
                View viewChatOther = layoutInflater.inflate(R.layout.chatmessage_row_other, parent, false);
                viewHolder = new OtherChatViewHolder(viewChatOther);
                break;
            case VIEW_TYPE_NOTICE:
                View viewChatNotice = layoutInflater.inflate(R.layout.chatmessage_row_notice, parent, false);
                viewHolder = new NoticeChatViewHolder(viewChatNotice);
        }

        return viewHolder;
    }

    @Override
    public int getItemViewType(int position) {
        if(messageList.get(position).getMsg_type().equals("msg")) {
            if (messageList.get(position).getId().equals(String.valueOf(SharedPrefManager.getInstance(context).getId()))) {
                return VIEW_TYPE_ME;
            } else {
                return VIEW_TYPE_OTHER;
            }
        } else {
            return VIEW_TYPE_NOTICE;
        }
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        if(messageList.get(position).getMsg_type().equals("msg")) {
            if (messageList.get(position).getId().equals(String.valueOf(SharedPrefManager.getInstance(context).getId()))) {
                configureMyChatViewHolder((MyChatViewHolder) holder, position);
            } else {
                configureOtherChatViewHolder((OtherChatViewHolder) holder, position);
            }
        } else {
            configureNoticeViewHolder((NoticeChatViewHolder) holder, position);
        }
    }

    public void configureNoticeViewHolder(NoticeChatViewHolder noticeChatViewHolder, int position) {

        chatMessage = messageList.get(position);

        String inviting_user_id = chatMessage.getUser_id();

        StringBuilder stringBuilder = new StringBuilder();

        try {
            JSONArray jsonArray = new JSONArray(chatMessage.getMessage());
            Log.d(TAG, "configureNoticeViewHolder: jsonArray : " + jsonArray);
            //jsonArray :
            // [{"id":7,"userId":"user3"},{"id":6,"userId":"user2"},{"id":5,"userId":"helloworld"}]

            for(int i = 0; i < jsonArray.length(); i++) {

                JSONObject object = jsonArray.getJSONObject(i);
                if(!object.getString("userId").equals(inviting_user_id))
                    stringBuilder.append(object.getString("userId"));

                if(i != jsonArray.length() - 1)
                    stringBuilder.append(",");
            }


        } catch (JSONException e) {
            e.printStackTrace();
        }



        noticeChatViewHolder.noticeText.setText(inviting_user_id + "님이 " + stringBuilder.toString() +"님을 초대하였습니다.");

    }

    public void configureMyChatViewHolder(MyChatViewHolder myChatViewHolder, int position) {

        chatMessage = messageList.get(position);

        myChatViewHolder.mDate.setText(TimeManager.compareTime(chatMessage.getDate()));
        myChatViewHolder.mMessage.setText(chatMessage.getMessage());

    }

    public void configureOtherChatViewHolder(OtherChatViewHolder otherChatViewHolder, int position) {

        chatMessage = messageList.get(position);

        otherChatViewHolder.mName.setText(chatMessage.getUser_id());
        otherChatViewHolder.mDate.setText(TimeManager.compareTime(chatMessage.getDate()));
        otherChatViewHolder.mMessage.setText(chatMessage.getMessage());

//        ImageLoader.getInstance().displayImage(chatMessage.getProfile_image(), otherChatViewHolder.mProfileImage);
    }

    @Override
    public int getItemCount() {
        return messageList.size();
    }

    private static class MyChatViewHolder extends RecyclerView.ViewHolder {

        private TextView mDate, mMessage;

        public MyChatViewHolder(@NonNull View itemView) {
            super(itemView);

            mMessage = itemView.findViewById(R.id.chatmessage_content);
            mDate = itemView.findViewById(R.id.chatmessage_date);

        }
    }

    private static class OtherChatViewHolder extends RecyclerView.ViewHolder {

        private CircleImageView mProfileImage;
        private TextView mName, mMessage, mDate;

        public OtherChatViewHolder(@NonNull View itemView) {
            super(itemView);
            mProfileImage = itemView.findViewById(R.id.userImage);
            mName = itemView.findViewById(R.id.userName);
            mMessage = itemView.findViewById(R.id.chatmessage_content);
            mDate = itemView.findViewById(R.id.chatmessage_date);
        }
    }

    private static class NoticeChatViewHolder extends RecyclerView.ViewHolder {

        private TextView noticeText;

        public NoticeChatViewHolder(@NonNull View itemView) {
            super(itemView);
            noticeText = itemView.findViewById(R.id.noticeText);
        }
    }
}
