package com.example.ericandroidsample;

import android.content.Intent;
import android.content.SharedPreferences;
import android.support.design.widget.TextInputEditText;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.sendbird.android.SendBird;
import com.sendbird.android.SendBirdException;
import com.sendbird.android.User;


public class LoginActivity extends AppCompatActivity {
    final static String APP_ID = "90EEACA0-2C62-4328-9F71-C7806FABDD75"; // eric_web_quickstart_Sample App
    private Button mConnectButton;
    private TextInputEditText mUserIdEditText, mUserNicknameEditText;
    private SharedPreferences mPrefs;




    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        Toast.makeText(LoginActivity.this, "Hi, welcome to Eric's sample app", Toast.LENGTH_SHORT).show();
        mPrefs = getSharedPreferences("label", 0);

        mConnectButton = (Button) findViewById(R.id.button_login);
        mUserIdEditText = (TextInputEditText) findViewById(R.id.edit_text_login_user_id);

        String savedUserID = mPrefs.getString("userId", "");
        mUserIdEditText.setText(savedUserID);
        mUserNicknameEditText = (TextInputEditText) findViewById(R.id.edit_text_login_user_nickname);

        SendBird.init(APP_ID, this.getApplicationContext());

        mConnectButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String userId = mUserIdEditText.getText().toString();
                userId = userId.replaceAll("\\s", "");
                String userNickname = mUserNicknameEditText.getText().toString();

                if (!userId.isEmpty() && !userNickname.isEmpty()) {
                    SharedPreferences.Editor mEditor = mPrefs.edit();
                    mEditor.putString("userNickName", userNickname).commit();
                    mEditor.putString("userId", userId).commit();
                    connectToSendBird(userId, userNickname);
                }else{
                    Toast.makeText(LoginActivity.this, "Please input userNickname", Toast.LENGTH_SHORT).show();

                }

            }
        });
    }


    /**
     * Attempts to connect a user to SendBird.
     * @param userId The unique ID of the user.
     * @param userNickname The user's nickname, which will be displayed in chats.
     */
    private void connectToSendBird(final String userId, final String userNickname) {
        mConnectButton.setEnabled(false);
//        Toast.makeText(LoginActivity.this, "inside connectToSendBird showing?", Toast.LENGTH_SHORT).show();
//        Toast.makeText(LoginActivity.this, "inside connectToSendBird showing1?", Toast.LENGTH_SHORT).show();
        SendBird.connect(userId, new SendBird.ConnectHandler() {
            @Override
            public void onConnected(User user, SendBirdException e) {
                if (e != null) {
                    // Error!
                    Toast.makeText(LoginActivity.this, "" + e.getCode() + ": " + e.getMessage(), Toast.LENGTH_SHORT).show();

                    // Show login failure snackbar
                    mConnectButton.setEnabled(true);
                    return;
                }

                // Update the user's nickname
                updateCurrentUserInfo(userNickname);
                Toast.makeText(LoginActivity.this, "connectiong to SendBird", Toast.LENGTH_SHORT).show();

                Intent intent = new Intent(LoginActivity.this, MainMenuActivity.class);
                intent.putExtra("userID", userId);
                intent.putExtra("userNickname", userNickname);
                startActivity(intent);
                finish();
            }
        });
    }

    /**
     * Updates the user's nickname.
     * @param userNickname The new nickname of the user.
     */
    private void updateCurrentUserInfo(String userNickname) {
        Toast.makeText(LoginActivity.this, "updatingUserInfo", Toast.LENGTH_SHORT).show();

        SendBird.updateCurrentUserInfo(userNickname, null, new SendBird.UserInfoUpdateHandler() {
            @Override
            public void onUpdated(SendBirdException e) {
                if (e != null) {
                    // Error!
                    Toast.makeText(LoginActivity.this, "" + e.getCode() + ":" + e.getMessage(), Toast.LENGTH_SHORT).show();
                    return;
                }

            }
        });
    }
}
