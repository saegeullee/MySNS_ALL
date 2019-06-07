package com.example.saegeullee.applicationoneproject.Adapter;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.constraint.ConstraintLayout;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.example.saegeullee.applicationoneproject.Constants;
import com.example.saegeullee.applicationoneproject.Models.Comment;
import com.example.saegeullee.applicationoneproject.R;
import com.nostra13.universalimageloader.core.ImageLoader;
import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

public class CommentListAdapter extends RecyclerView.Adapter<CommentListAdapter.ViewHolder> {

    private static final String TAG = "CommentListAdapter";

    private Context context;
    private List<Comment> commentList;

    public CommentListAdapter(Context context, List<Comment> commentList) {
        this.context = context;
        this.commentList = commentList;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_post_comment, parent,false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, final int position) {

        Comment comment = commentList.get(position);

        holder.timestamp.setText(comment.getDate());
        holder.commentText.setText(comment.getCommentText());
        holder.user_id.setText(comment.getUser().getUser_id());

        String profile_image_url = Constants.ROOT_URL_USER_PROFILE_IMAGE + comment.getUser().getProfile_image();

        ImageLoader.getInstance().displayImage(profile_image_url, holder.profile_image);

    }

    @Override
    public int getItemCount() {
        return commentList.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {

        private ConstraintLayout commentContainer;
        private CircleImageView profile_image;
        private TextView user_id, timestamp, commentText;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            profile_image = itemView.findViewById(R.id.profile_image);
            user_id = itemView.findViewById(R.id.user_name);
            commentText = itemView.findViewById(R.id.commentText);
            timestamp = itemView.findViewById(R.id.timestamp);
            commentContainer = itemView.findViewById(R.id.commentContainer);
        }
    }

}
