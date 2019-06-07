package com.example.saegeullee.applicationoneproject.Dialog;

import android.app.Dialog;
import android.content.Intent;
import android.content.res.TypedArray;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.AppBarLayout;
import android.support.design.widget.BottomSheetBehavior;
import android.support.design.widget.BottomSheetDialog;
import android.support.design.widget.BottomSheetDialogFragment;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;

import com.example.saegeullee.applicationoneproject.ChatRoomActivity;
import com.example.saegeullee.applicationoneproject.Constants;
import com.example.saegeullee.applicationoneproject.Models.User;
import com.example.saegeullee.applicationoneproject.R;
import com.example.saegeullee.applicationoneproject.feedUserDetailActivity;
import com.nostra13.universalimageloader.core.ImageLoader;

import de.hdodenhof.circleimageview.CircleImageView;

public class FragmentBottomSheetDialogFull extends BottomSheetDialogFragment {

    private static final String TAG = "FragmentBottomSheetDial";

    private BottomSheetBehavior mBehavior;

    private AppBarLayout app_bar_layout;

    private User user;
    private CircleImageView profile_image;
    private TextView user_name;
    private Button chatBtn, feedBtn;

    public void setUser(User user) {
        this.user = user;
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        final BottomSheetDialog dialog = (BottomSheetDialog) super.onCreateDialog(savedInstanceState);
        final View view = View.inflate(getContext(), R.layout.fragment_bottom_sheet_dialog_full, null);

        dialog.setContentView(view);
        mBehavior = BottomSheetBehavior.from((View) view.getParent());
        mBehavior.setPeekHeight(BottomSheetBehavior.PEEK_HEIGHT_AUTO);

        app_bar_layout = (AppBarLayout) view.findViewById(R.id.app_bar_layout);

        // set data to view

        profile_image = view.findViewById(R.id.profile_image);
        user_name = view.findViewById(R.id.user_name);

        String image_url = Constants.ROOT_URL_USER_PROFILE_IMAGE + user.getProfile_image();
        ImageLoader.getInstance().displayImage(image_url, profile_image);
        user_name.setText(user.getUser_name());

        chatBtn = view.findViewById(R.id.chatBtn);
        feedBtn = view.findViewById(R.id.feedBtn);

        Log.d(TAG, "onCreateDialog: user " + user.toString());

        feedBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getActivity(), feedUserDetailActivity.class);
                intent.putExtra(getActivity().getString(R.string.db_users_object), user);
                getActivity().startActivity(intent);
            }
        });

        chatBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                Intent intent = new Intent(getActivity(), ChatRoomActivity.class);
                intent.putExtra(getActivity().getString(R.string.db_users_object), user);
                getActivity().startActivity(intent);
            }
        });







//        hideView(app_bar_layout);

//        mBehavior.setBottomSheetCallback(new BottomSheetBehavior.BottomSheetCallback() {
//            @Override
//            public void onStateChanged(@NonNull View bottomSheet, int newState) {
//                if (BottomSheetBehavior.STATE_EXPANDED == newState) {
//                    showView(app_bar_layout, getActionBarSize());
//                    hideView(lyt_profile);
//                }
//                if (BottomSheetBehavior.STATE_COLLAPSED == newState) {
//                    hideView(app_bar_layout);
//                    showView(lyt_profile, getActionBarSize());
//                }
//
//                if (BottomSheetBehavior.STATE_HIDDEN == newState) {
//                    dismiss();
//                }
//            }
//
//            @Override
//            public void onSlide(@NonNull View bottomSheet, float slideOffset) {
//
//            }
//        });

        ((ImageButton) view.findViewById(R.id.bt_close)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dismiss();
            }
        });

        return dialog;
    }

    @Override
    public void onStart() {
        super.onStart();
        mBehavior.setState(BottomSheetBehavior.STATE_COLLAPSED);
    }

    private void hideView(View view) {
        ViewGroup.LayoutParams params = view.getLayoutParams();
        params.height = 0;
        view.setLayoutParams(params);
    }

    private void showView(View view, int size) {
        ViewGroup.LayoutParams params = view.getLayoutParams();
        params.height = size;
        view.setLayoutParams(params);
    }

    private int getActionBarSize() {
        final TypedArray styledAttributes = getContext().getTheme().obtainStyledAttributes(new int[]{android.R.attr.actionBarSize});
        int size = (int) styledAttributes.getDimension(0, 0);
        return size;
    }
}
