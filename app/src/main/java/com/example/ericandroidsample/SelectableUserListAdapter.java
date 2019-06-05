package com.example.ericandroidsample;

/*
* RecyclerView.Adapter
* RecyclerView.ViewHolder
*
*

*
 */

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.TextView;

import com.sendbird.android.User;

import java.util.ArrayList;
import java.util.List;

// Create the basic adapter extending from RecyclerView.Adapter
// Note that we specify the custom ViewHolder which gives us access to our views
public class SelectableUserListAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder>{

    // store user info variable for the list
    private List<User> mUsers;
    private Context mCtx;
    private static List<String> mSelectedUserIds;
    private boolean mIsBlockedList;
    private boolean mShowCheckbox;

    private SelectableUserViewHolder mSelectableUserHolder;

    // For the adapter to track which users have been selected
    private OnItemCheckedChangeListener mCheckedChangeListener;

    public interface OnItemCheckedChangeListener {
        void OnItemChecked(User user, boolean checked);
    }

    // adapter constructor and user info variables
    public SelectableUserListAdapter(Context context, boolean isBlockedList, boolean showCheckbox) {
        mCtx = context;
        mUsers = new ArrayList<>();
        mSelectedUserIds = new ArrayList<>();
        mIsBlockedList = isBlockedList;
        mShowCheckbox = showCheckbox;
    }

    // method : setItemCheckedChangeListener -> setting the itemCheckedChangeListener
    public void setItemCheckedChangeListener(OnItemCheckedChangeListener listener){
        mCheckedChangeListener = listener;
    }

    // method : setting userList
    public void setUserList(List<User> users){
        mUsers = users;
        notifyDataSetChanged();
    }

    // method: setShowCheckBox
    public void setShowCheckBox(boolean showCheckbox){
        mShowCheckbox = showCheckbox;
        // see if there is any user in the holder
        if (mSelectableUserHolder != null){
            mSelectableUserHolder.setShowCheckbox(showCheckbox);
        }
        notifyDataSetChanged();
    }



    // Usually involves inflating a layout from XML and returning the holder
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        Context context = parent.getContext();
        LayoutInflater inflater = LayoutInflater.from(context);
        View view = inflater.inflate(R.layout.selectable_user_list, parent, false);
        mSelectableUserHolder = new SelectableUserViewHolder(view, mIsBlockedList, mShowCheckbox);
        return mSelectableUserHolder;
    }

    // Involves populating data into the item through holder
    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        ((SelectableUserViewHolder) holder).bind(
                mCtx,
                mUsers.get(position),
                isSelected(mUsers.get(position)),
                mCheckedChangeListener);
    }

    // Returns the total count of items in the list
    @Override
    public int getItemCount() {
        return mUsers.size();
    }

    public boolean isSelected(User user){
        return mSelectedUserIds.contains(user.getUserId());
    }

    public void addLast(User user){
        mUsers.add(user);
        notifyDataSetChanged();
    }


    // Provide a direct reference to each of the views within a data item
    // Used to cache the views within the item layout for fast access
    public class SelectableUserViewHolder extends RecyclerView.ViewHolder{
        // Your holder should contain a member variable
        // for any view that will be set as you render a row
        private ImageView imageView_user_profile;
        private ImageView imageView_blocked;
        private TextView userNickname;
        private CheckBox checkbox;

        private boolean mShowCheckbox;
        private boolean mIsBlockedList;

        public SelectableUserViewHolder(@NonNull View itemView, boolean isBlockedList, boolean hideCheckbox) {
            super(itemView);

            this.setIsRecyclable(false);
            this.mShowCheckbox = hideCheckbox;
            this.mIsBlockedList = isBlockedList;

            checkbox = (CheckBox) itemView.findViewById(R.id.checkbox_selectable_user_list);
            imageView_user_profile = (ImageView) itemView.findViewById(R.id.image_selectable_user_list_profile);
            imageView_blocked = (ImageView) itemView.findViewById(R.id.image_user_list_blocked);
            userNickname = (TextView) itemView.findViewById(R.id.text_selectable_user_list_uesrNickname);
        }

        public void setShowCheckbox(boolean showCheckbox){
            mShowCheckbox = showCheckbox;
        }

        private void bind(final Context context, final User user, boolean isSeleted, final OnItemCheckedChangeListener listener){
            userNickname.setText(user.getNickname());
            Utils.displayRoundImageFromURL(context, user.getProfileUrl(), imageView_user_profile);

            // check if user is blocked
            if (mIsBlockedList) {
                imageView_blocked.setVisibility(View.VISIBLE);
            } else {
                imageView_blocked.setVisibility(View.GONE);
            }

            // update checkbox light
            if (mShowCheckbox){
                checkbox.setVisibility(View.VISIBLE);
            } else{
                checkbox.setVisibility(View.GONE);
            }

            // show checkbox on/off
            if (isSeleted) {
                checkbox.setChecked(true);
            } else {
                checkbox.setChecked(false);
            }

            if (mShowCheckbox){
                itemView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if (mShowCheckbox){
                            checkbox.setChecked(!checkbox.isChecked());
                        }
                    }
                });
            }

            checkbox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                    listener.OnItemChecked(user, isChecked);

                    if (isChecked){
                        mSelectedUserIds.add(user.getUserId());
                    } else {
                        mSelectedUserIds.remove(user.getUserId());
                    }
                }
            });

        }


//
//
//    public SelectableUserListAdapter(Context mCtx, List<SelectableUsersFragment> selectableUserslistFragment) {
//        this.mCtx = mCtx;
//        this.selectableUserslistFragment = selectableUserslistFragment;
//        notifyDataSetChanged();
//    }
//
//    @NonNull
//    @Override
//    public SelectableUserListViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
//        LayoutInflater inflater = LayoutInflater.from(mCtx);
//        View view = inflater.inflate(R.layout.selectable_user_list, null);
//        SelectableUserListViewHolder holder = new SelectableUserListViewHolder(view);
//        return holder;
//    }
//
//    @Override
//    public void onBindViewHolder(@NonNull SelectableUserListViewHolder holder, int i) {
//        SelectableUsersFragment selectableUsersFragment = selectableUserslistFragment.get(i);
//
//        holder.userNickname.setText(selectableUsersFragment.getNickname());
////        holder.imageView_user_profile.setImageDrawable(mCtx.getResources().getDrawable(selectableUsersFragment.getProfileUrl(), null));
//    }
//
//    @Override
//    public int getItemCount() {
//        return selectableUserslistFragment.size();
//    }



    //        public ViewHolder(@NonNull View itemView) {
//            // Stores the itemView in a public final member variable that can be used
//            // to access the context from any ViewHolder instance.
//            super(itemView);
//            imageView_user_profile =
//        }


//        public SelectableUserListViewHolder (View userView){
//            super(userView);
//
//            imageView_user_profile = userView.findViewById(R.id.image_selectable_user_list_profile);
//            userNickname = userView.findViewById(R.id.text_uesrNickname);
//        }
    }
}
