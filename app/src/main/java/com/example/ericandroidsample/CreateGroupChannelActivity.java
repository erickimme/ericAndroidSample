package com.example.ericandroidsample;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

public class CreateGroupChannelActivity extends AppCompatActivity {
    private Button mCreateGroupChannelButton;


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_group_channel);

        setTitle("Create Group Channel");
        Toast.makeText(CreateGroupChannelActivity.this, "now in creating group channel page", Toast.LENGTH_SHORT).show();
    }
}
