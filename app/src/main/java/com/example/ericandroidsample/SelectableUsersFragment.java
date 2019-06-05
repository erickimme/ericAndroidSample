package com.example.ericandroidsample;


import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import com.sendbird.android.ApplicationUserListQuery;
import com.sendbird.android.SendBird;
import com.sendbird.android.SendBirdException;
import com.sendbird.android.User;
import com.sendbird.android.UserListQuery;

import java.text.SimpleDateFormat;
import java.util.List;
/*
Fragment : displaying selectable users for creating groupchannel
This Frament use recyclerview(SelectableUserListAdapter) to display the list of selectableusers


* Using a RecyclerView has the following key steps:
1. Add RecyclerView support library to the Gradle build file
2. Define a model class to use as the data source
3. Add a RecyclerView to your activity to display the items
4. Create a custom row layout XML file to visualize the item
5. Create a RecyclerView.Adapter and ViewHolder to render the item
6. Bind the adapter to the data source to populate the RecyclerView
 */

public class SelectableUsersFragment extends Fragment {

    // recyclerView setup
    private LinearLayoutManager mLayoutManager;
    private RecyclerView mRecyclerView;
    private SelectableUserListAdapter mListAdapter;

    private ApplicationUserListQuery mUserListQuery;
    private UsersSelectedListener mListener;


    // To pass selected user IDs to the parent activity
    interface UsersSelectedListener {
        void onUserSelected(boolean selected, String userId);
    }

    static SelectableUsersFragment newInstance(){
        SelectableUsersFragment selectableUsersFragment = new SelectableUsersFragment();
        Bundle args = new Bundle();
        return selectableUsersFragment;
    }



    // The onCreateView method is called when Fragment should create its View object hierarchy,
    // either dynamically or via XML layout inflation.
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Defines the xml file for the fragment
        View rootView = inflater.inflate(R.layout.fragment_selectable_user, container, false); //xml
        mRecyclerView = (RecyclerView) rootView.findViewById(R.id.recycler_select_user);    //recylerview
        mListAdapter = new SelectableUserListAdapter(getActivity(), false, true);

        mListAdapter.setItemCheckedChangeListener(new SelectableUserListAdapter.OnItemCheckedChangeListener() {
            @Override
            public void OnItemChecked(User user, boolean checked) {
                if (checked){
                    mListener.onUserSelected(true, user.getUserId());
                } else {
                    mListener.onUserSelected(false, user.getUserId());
                }
            }
        });

        // get user information
        mListener = (UsersSelectedListener) getActivity();
        setUpRecyclerView();
        loadInitialUserList(15);
//        ((CreateGroupChannelActivity) getActivity()).setState()
        return rootView;
    }


    // setting recyclerview
    private void setUpRecyclerView(){
        mLayoutManager = new LinearLayoutManager(getActivity());
        mRecyclerView.setLayoutManager(mLayoutManager);
        mRecyclerView.setAdapter(mListAdapter);
        mRecyclerView.addItemDecoration(new DividerItemDecoration(getActivity(), DividerItemDecoration.VERTICAL));
        mRecyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(@NonNull RecyclerView recyclerView, int newState) {
                if (mLayoutManager.findLastVisibleItemPosition() == mListAdapter.getItemCount() - 1){
                    loadNextUserList(10);
                }
            }
        });
    }

    // loading users
    private void loadInitialUserList(int size){
        mUserListQuery = SendBird.createApplicationUserListQuery();
        mUserListQuery.setLimit(size);
        mUserListQuery.next(new UserListQuery.UserListQueryResultHandler() {
            @Override
            public void onResult(List<User> list, SendBirdException e) {
                if (e != null){
                    //error
                    return;
                }
                mListAdapter.setUserList(list);
            }
        });
    }

    private void loadNextUserList(int size){
        mUserListQuery.setLimit(size);

        mUserListQuery.next(new UserListQuery.UserListQueryResultHandler() {
            @Override
            public void onResult(List<User> list, SendBirdException e) {
                if (e != null){
                    //error
                    return;
                }

                for (User user: list){
                    mListAdapter.addLast(user);
                }
            }
        });
    }




}
