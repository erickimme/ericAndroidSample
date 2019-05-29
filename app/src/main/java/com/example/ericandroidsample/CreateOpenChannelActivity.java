package com.example.ericandroidsample;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.TextInputEditText;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.sendbird.android.OpenChannel;
import com.sendbird.android.SendBirdException;

public class CreateOpenChannelActivity extends AppCompatActivity {
    private Button mCreateOpenChannelButton;
    private TextInputEditText mNameEditText;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_open_channel);

        setTitle("Create Open Channel");
        Toast.makeText(CreateOpenChannelActivity.this, "now in creating open channel page", Toast.LENGTH_SHORT).show();

        // UI
        mNameEditText = findViewById(R.id.edittext_create_open_channel_name);
        mCreateOpenChannelButton = (Button) findViewById(R.id.button_create_open_channel);


        mCreateOpenChannelButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String openChannelName = mNameEditText.getText().toString();
                createOpenChannel(openChannelName);
            }
        });


    }

    /*
    * createOpenChannel
    * NAME: the name of a channel, or the channel topic.
    * COVER_IMAGE_OR_URL: the file or URL of the cover image, which you can fetch to render into the UI.
    * DATA: the String field which can be a description of the channel or structured information such as a JSON string for other purposes.
    * OPERATOR_USERS: the list of users who are registered as operators of the channel.
    * CUSTOM_TYPE: the String field that allows you to subclassify your channel.
    *
    * Using the getCoverUrl() and updateChannel() methods, you can get and update the cover image URL of a channel.
     */

    private void createOpenChannel(String ChannelName){
        // check if the name is not empty
        if (!ChannelName.isEmpty()){
            // create channel here
            OpenChannel.createChannelWithOperatorUserIds(ChannelName, null, null, null, new OpenChannel.OpenChannelCreateHandler() {
                @Override
                public void onResult(OpenChannel openChannel, SendBirdException e) {
                    if (e != null){
                        // Error
                        return ;
                    }

                    Intent intent = new Intent();
                    setResult(RESULT_OK, intent);
//                    startActivity(intent);
                    finish();
                }
            });
        }
        else{
            Toast.makeText(CreateOpenChannelActivity.this, "Channel Name must contain at least 1 character", Toast.LENGTH_SHORT).show();
        }
    }
}
