package com.example.ericandroidsample;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.sendbird.android.GroupChannel;
import com.sendbird.android.SendBirdException;

import java.util.ArrayList;
import java.util.List;

// seleactable_user_list.xml, selectableUserListAdapter.java, SelectableUsersFragment.java

public class CreateGroupChannelActivity extends AppCompatActivity implements SelectableUsersFragment.UsersSelectedListener, SelectableDistinctFragment.DistinctSelectedListener {
    public static final String EXTRA_NEW_CHANNEL_URL = "EXTRA_NEW_CHANNEL_URL";

    static final int STATE_SELECT_USERS = 0;
    static final int STATE_SELECT_DISTINCT = 1;

    private Button mCreateGroupChannelButton, mNextButton;

    private List<String> mSelectedIds;
    private boolean mIsDistinct = true;
    private int mCurrentState;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_group_channel);
        setTitle("Create Group Channel");
        Toast.makeText(CreateGroupChannelActivity.this, "now in creating group channel page", Toast.LENGTH_SHORT).show();

        // display user list using fragment
        mSelectedIds = new ArrayList<>();
        if (savedInstanceState == null){
            Fragment fragment = SelectableUsersFragment.newInstance();
            FragmentManager fragmentManager = getSupportFragmentManager();
            fragmentManager.beginTransaction()
                    .replace(R.id.container_create_group_channel, fragment)
                    .commit();
        }


        mNextButton = (Button) findViewById(R.id.button_create_group_channel_next);
        mNextButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mCurrentState == STATE_SELECT_USERS){
                    Fragment fragment = SelectableDistinctFragment.newInstance();
                    getSupportFragmentManager().beginTransaction()
                            .replace(R.id.container_create_group_channel, fragment)
                            .addToBackStack(null)
                            .commit();
                }
            }
        });
        mNextButton.setEnabled(false);

        // user select users to invite (creating invited_user_list) using checkmark

        // click create button
        mCreateGroupChannelButton = (Button) findViewById(R.id.button_create_group_channel);
        mCreateGroupChannelButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mCurrentState == STATE_SELECT_USERS){
                    mIsDistinct = PreferenceUtils.getGroupChannelDistinct();
                    createGroupChannel(mSelectedIds, mIsDistinct);
                }
            }
        });
        mCreateGroupChannelButton.setEnabled(false);

        // display the group chat room


    }

    public boolean onOptionsItemSelected (MenuItem item){
        int id = item.getItemId();

        if (id == android.R.id.home){
            onBackPressed();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }


    void setState(int state){
        // no group channel existed
        if (state == STATE_SELECT_USERS){
            mCurrentState = STATE_SELECT_USERS;
            mCreateGroupChannelButton.setVisibility(View.VISIBLE);
            mNextButton.setVisibility(View.GONE);
        } else if (state == STATE_SELECT_DISTINCT){
            mCurrentState = STATE_SELECT_DISTINCT;
            mCreateGroupChannelButton.setVisibility(View.VISIBLE);
            mNextButton.setVisibility(View.GONE);
        }
    }

    public void onUserSelected(boolean selected, String userId){
        if (selected){
            mSelectedIds.add(userId);
        } else {
            mSelectedIds.remove(userId);
        }

        if (mSelectedIds.size() > 0){
            mCreateGroupChannelButton.setEnabled(true);
        } else {
            mCreateGroupChannelButton.setEnabled(false);
        }
    }

    public void onDistinctSelected(boolean distinct) {
        mIsDistinct = distinct;
    }
    /**
     * Creates a new Group Channel.
     *
     * Note that if you have not included empty channels in your GroupChannelListQuery,
     * the channel will not be shown in the user's channel list until at least one message
     * has been sent inside.
     *
     * @param userIds   The users to be members of the new channel.
     * @param distinct  Whether the channel is unique for the selected members.
     *                  If you attempt to create another Distinct channel with the same members,
     *                  the existing channel instance will be returned.
     */

    private void createGroupChannel(List<String> userIds, boolean distinct){
        GroupChannel.createChannelWithUserIds(userIds, distinct, new GroupChannel.GroupChannelCreateHandler() {
            @Override
            public void onResult(GroupChannel groupChannel, SendBirdException e) {
                if (e != null){
                    // error
                    return;
                }

                Intent intent = new Intent();
                intent.putExtra(EXTRA_NEW_CHANNEL_URL, groupChannel.getUrl());
                setResult(RESULT_OK, intent);
                finish();
            }
        });
    }

}
