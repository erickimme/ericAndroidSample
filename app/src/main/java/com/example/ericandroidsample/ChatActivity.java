package com.example.ericandroidsample;

import android.media.Image;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Layout;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Adapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.sendbird.android.BaseChannel;
import com.sendbird.android.BaseMessage;
import com.sendbird.android.FileMessage;
import com.sendbird.android.GroupChannel;
import com.sendbird.android.OpenChannel;
import com.sendbird.android.SendBird;
import com.sendbird.android.SendBirdException;
import com.sendbird.android.User;
import com.sendbird.android.UserMessage;
import com.sendbird.android.shadow.okhttp3.Connection;

import org.w3c.dom.Text;

import java.util.ArrayList;
import java.util.List;

public class ChatActivity extends AppCompatActivity {

    private static final int CHANNEL_LIST_LIMIT = 30;
    private static final String CONNECTION_HANDLER_ID = "CONNECTION_HANDLER_GROUP_CHAT";
    private static final String CHANNEL_HANDLER_ID = "CHANNEL_HANDLER_GROUP_CHANNEL_CHAT";

    private ChatAdapter mChatAdapter;
    private RecyclerView mRecyclerView;
    private LinearLayoutManager mLayoutManager;
    private Button mSendButton;
    private EditText mMessageEditText;

    private GroupChannel mGroupChannel;
    private OpenChannel mOpenChannel;
    private String mChannelUrl;
    private String mChannelType;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);


        mSendButton = (Button) findViewById(R.id.chat_send_button);
        mMessageEditText = (EditText) findViewById(R.id.editText_chat);

        mRecyclerView = (RecyclerView) findViewById(R.id.recycler_chat);
        mLayoutManager = new LinearLayoutManager(this);
        mLayoutManager.setReverseLayout(true);
        mRecyclerView.setLayoutManager(mLayoutManager);
        mChatAdapter = new ChatAdapter();
        mRecyclerView.setAdapter(mChatAdapter);

        Bundle b = getIntent().getExtras();
        mChannelUrl = (String) b.get("channelUrl");
        mChannelType = (String) b.get("channelType");

        setTitle(mChannelUrl);

        switch (mChannelType){
            case Constants.openChannelType:
                // join open channel
                join_open_channel();
                break;
            case Constants.groupChannelType:
                join_group_channel();
                break;
            default:
                Log.e("App", "Invalid Channel Type: " + mChannelType);
                finish();
                break;
        }
        mSendButton.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v){
                String typedMessage = mMessageEditText.getText().toString();
                if (!typedMessage.isEmpty()){
                    mChatAdapter.sendMessage(typedMessage);
                    mMessageEditText.setText("");
                }


            }
        });

        mRecyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
                if (mLayoutManager.findLastVisibleItemPosition() == mChatAdapter.getItemCount() - 1){
                    mChatAdapter.loadPreviousMessages();
                }
            }
        });
    }


    /*
    * join group channel :
     */
    private void join_group_channel(){
        Toast.makeText(ChatActivity.this, "joining group channel", Toast.LENGTH_SHORT).show();

        GroupChannel.getChannel(mChannelUrl, new GroupChannel.GroupChannelGetHandler() {
            @Override
            public void onResult(final GroupChannel groupChannel, SendBirdException e) {
                if (e != null){
                    e.printStackTrace();
                    return;
                }

                mGroupChannel = groupChannel;
                //TODO : loadLatestMessages
                // call refresh()
                refresh_group_chat();
            }
        });
    }

    /*
    * open group channel
     */
    private void join_open_channel(){
        OpenChannel.getChannel(mChannelUrl, new OpenChannel.OpenChannelGetHandler(){
            @Override
            public void onResult(final OpenChannel openChannel, SendBirdException e){
                if (e != null){
                    e.printStackTrace();
                    return;
                }

                openChannel.enter(new OpenChannel.OpenChannelEnterHandler(){
                    @Override
                    public void onResult(SendBirdException e) {
                        if (e != null){
                            e.printStackTrace();
                            return;
                        };
                        mChatAdapter.addChannel(openChannel);
//                        mChatAdapter = new ChatAdapter(openChannel);
//                        mRecyclerView.setAdapter(mChatAdapter);
                    }
                });
            }
        });
    }

    private void refresh_group_chat() {
        if (mGroupChannel == null) {
            GroupChannel.getChannel(mChannelUrl, new GroupChannel.GroupChannelGetHandler() {
                @Override
                public void onResult(GroupChannel groupChannel, SendBirdException e) {
                    if (e != null) {
                        // Error!
                        e.printStackTrace();
                        return;
                    }

                    mGroupChannel = groupChannel;
                    mChatAdapter.setChannel(mGroupChannel);
                    mChatAdapter.loadLatestMessages(CHANNEL_LIST_LIMIT, new BaseChannel.GetMessagesHandler() {
                        @Override
                        public void onResult(List<BaseMessage> list, SendBirdException e) {
                            mChatAdapter.markAllMessagesAsRead();
                        }
                    });
//                    updateActionBarTitle();
                }
            });
        } else {
            mGroupChannel.refresh(new GroupChannel.GroupChannelRefreshHandler() {
                @Override
                public void onResult(SendBirdException e) {
                    if (e != null) {
                        // Error!
                        e.printStackTrace();
                        return;
                    }

                    mChatAdapter.loadLatestMessages(CHANNEL_LIST_LIMIT, new BaseChannel.GetMessagesHandler() {
                        @Override
                        public void onResult(List<BaseMessage> list, SendBirdException e) {
                            mChatAdapter.markAllMessagesAsRead();
                        }
                    });
//                    updateActionBarTitle();
                }
            });
        }
    }

    @Override
    protected void onResume(){
//        Toast.makeText(ChatActivity.this, "onResume clicked", Toast.LENGTH_SHORT).show();
//        Toast.makeText(ChatActivity.this, mChannelUrl, Toast.LENGTH_SHORT).show();
        super.onResume();

        // refresh the chatlist
        ConnectionManager.addConnectionManagementHandler(CONNECTION_HANDLER_ID, new ConnectionManager.ConnectionManagementHandler() {
            @Override
            public void onConnected(boolean reconnect) {
                if (mChannelType.equals(Constants.groupChannelType)){
                    refresh_group_chat();
                }
            }
        });

        // receive messages from sendbird Servers
        SendBird.addChannelHandler(CHANNEL_HANDLER_ID, new SendBird.ChannelHandler() {
            @Override
            public void onMessageReceived(BaseChannel baseChannel, BaseMessage baseMessage) {
                Toast.makeText(ChatActivity.this, baseChannel.getUrl(), Toast.LENGTH_SHORT).show();
                if (baseChannel.getUrl().equals(mChannelUrl) && baseMessage instanceof UserMessage) {
                    mChatAdapter.appendMessage(baseChannel, (UserMessage) baseMessage);
                }
            }
        });
    }

    @Override
    protected void onPause() {

        SendBird.removeChannelHandler(CHANNEL_HANDLER_ID);
        super.onPause();
    }




























    /**
     * ChatAdapter Class : create an Adapter for the recyclerView
     * Hold two recyclerView.ViewHolders (sent/receive)
     */
    private class ChatAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
        // viewholders
        private static final int VIEW_TYPE_MESSAGE_SENT = 1;
        private static final int VIEW_TYPE_MESSAGE_RECEIVED = 2;

        private ArrayList<BaseMessage> mMessageList;
        private BaseChannel mChannel;
        private GroupChannel mGroupChannel;
        private OpenChannel mOpenChannel;
        private boolean mIsMessageListLoading;


        private ArrayList<String> mFailedMessageIdList = new ArrayList<>();


        ChatAdapter(){
            mMessageList = new ArrayList<>();
            /*mChannel = channel;*/

//            refresh();
        }


        // Retrieves 50 most recent messages and store them in an ArrayList.
        void refresh(){
            mChannel.getPreviousMessagesByTimestamp(Long.MAX_VALUE, true, 50, true, BaseChannel.MessageTypeFilter.USER, null, new BaseChannel.GetMessagesHandler() {
                @Override
                public void onResult(List<BaseMessage> list, SendBirdException e) {
                    if (e != null){
                        e.printStackTrace();
                        return;
                    }
                    mMessageList = (ArrayList<BaseMessage>) list;
                    notifyDataSetChanged();
                }
            });
        }




        /**
         *  Notify that the user has read all (previously unread) messages in the channel.
         *  Typically, this would be called immediately after the user enters the chat and loads
         *  its most recent messages.
         */
        public void markAllMessagesAsRead() {
            if (mGroupChannel != null){
                mGroupChannel.markAsRead();
            }
            notifyDataSetChanged();
        }









        // load previous messages
        void loadPreviousMessages(){
            final long lastTimestamp = mMessageList.get(mMessageList.size() - 1).getCreatedAt();
            mChannel.getPreviousMessagesByTimestamp(lastTimestamp, false, 50, true, BaseChannel.MessageTypeFilter.USER, null, new BaseChannel.GetMessagesHandler() {
                @Override
                public void onResult(List<BaseMessage> list, SendBirdException e) {
                    if (e != null){
                        e.printStackTrace();
                        return;
                    }

                    mMessageList.addAll(list);
                    notifyDataSetChanged();
                }
            });
        }


        /**
         * Replaces current message list with new list.
         * Should be used only on initial load or refresh.
         */
        public void loadLatestMessages(int limit, final BaseChannel.GetMessagesHandler handler){
            if (mChannel == null){
                return;
            }

            if (isMessageListLoading()){
                return;
            }

            setMessageListLoading(true);
            mChannel.getPreviousMessagesByTimestamp(Long.MAX_VALUE, true, limit, true, BaseChannel.MessageTypeFilter.ALL, null, new BaseChannel.GetMessagesHandler() {
                @Override
                public void onResult(List<BaseMessage> list, SendBirdException e){
                    if (handler != null){
                        handler.onResult(list, e);
                    }

                    setMessageListLoading(false);
                    if (e != null){
                        e.printStackTrace();
                        return;
                    }

                    if (list.size() <= 0){
                        return;
                    }

                    for (BaseMessage message : mMessageList) {
                        if (isTempMessage(message) || isFailedMessage(message)){
                            list.add(0, message);
                        }
                    }

                    mMessageList.clear();

                    for (BaseMessage message : list){
                        mMessageList.add(message);
                    }

                    notifyDataSetChanged();
                }
            });
        }


        void addChannel(BaseChannel channel){
            mChannel = channel;

            refresh();
        }




        // Appending a new message to the beginning of the message list
        void appendMessage(BaseChannel channel, UserMessage message){
            mChannel = channel;
            mMessageList.add(0, message);
            notifyDataSetChanged();
        }



        // Send a new message, and appends the sent message to the beginning of the message list.
        void sendMessage(final String message){
            mChannel.sendUserMessage(message, new BaseChannel.SendUserMessageHandler() {
                @Override
                public void onSent(UserMessage userMessage, SendBirdException e) {
                    if (e != null){
                        e.printStackTrace();
                        return;
                    }
                    mMessageList.add(0, userMessage);
                    notifyDataSetChanged();
                }
            });
        }


        // Determine the viewtype by the sender of the message
        // sent or received
        @Override
        public int getItemViewType(int position){
            UserMessage message =  (UserMessage) mMessageList.get(position);

            if (message.getSender().getUserId().equals(SendBird.getCurrentUser().getUserId())){
                // IF the current user is the message sender
                return VIEW_TYPE_MESSAGE_SENT;
            } else {
                // if some other user sent the message
                return VIEW_TYPE_MESSAGE_RECEIVED;
            }
        }

        // Inflates the appropriate layout according to the ViewType
        @Override
        public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType){
            View view;

            if (viewType == VIEW_TYPE_MESSAGE_SENT){
                view = LayoutInflater.from(parent.getContext()).inflate(R.layout.message_item_sent, parent, false);
                return new SentMessageHolder(view);
            }
            else if (viewType == VIEW_TYPE_MESSAGE_RECEIVED){
                view = LayoutInflater.from(parent.getContext()).inflate(R.layout.message_item_received, parent, false);
                return new ReceivedMessageHolder(view);
            }
            return null;
        }

        // Passes the message obj to a viewholder
        @Override
        public void onBindViewHolder(RecyclerView.ViewHolder holder, int position){
            UserMessage message = (UserMessage) mMessageList.get(position);

            switch (holder.getItemViewType()){
                case VIEW_TYPE_MESSAGE_SENT:
                    ((SentMessageHolder) holder).bind(message);
                    break;
                case VIEW_TYPE_MESSAGE_RECEIVED:
                    ((ReceivedMessageHolder) holder).bind(message);
            }
        }

        @Override
        public int getItemCount() {
            return mMessageList.size();
        }




        void setChannel(GroupChannel channel){
            mChannel = channel;
        }

        public boolean isTempMessage(BaseMessage message){
            return message.getMessageId() == 0;
        }

        public boolean isFailedMessage(BaseMessage message){
            if (!isTempMessage(message)){
                return false;
            }

            if (message instanceof UserMessage) {
                int index = mFailedMessageIdList.indexOf(((UserMessage) message).getRequestId());
                if (index >= 0){
                    return true;
                }
            } else if (message instanceof FileMessage){
                int index = mFailedMessageIdList.indexOf(((FileMessage) message).getRequestId());
                if (index >= 0){
                    return true;
                }
            }
            return false;
        }



        // Message sent by me do not display a profile image or nickname
        private class SentMessageHolder extends RecyclerView.ViewHolder {
            TextView messageText, timeText;

            SentMessageHolder(View itemView){
                super(itemView);

                messageText = (TextView) itemView.findViewById(R.id.text_message_body);
                timeText = (TextView) itemView.findViewById(R.id.text_message_timestamp);
            }

            void bind(UserMessage message){
                messageText.setText(message.getMessage());

                // Format the timestamp into human time
                timeText.setText(Utils.formatTime(message.getCreatedAt()));
            }
        }

        // Message sent by others display a profile image and nickname
        private class ReceivedMessageHolder extends RecyclerView.ViewHolder{
            TextView messageText, timeText, nameText;
            ImageView profileImage;

            ReceivedMessageHolder(View itemView){
                super(itemView);

                messageText = (TextView) itemView.findViewById(R.id.text_message_body);
                timeText = (TextView) itemView.findViewById(R.id.text_message_timestamp);
                nameText = (TextView) itemView.findViewById(R.id.text_message_name);
                profileImage = (ImageView) itemView.findViewById(R.id.image_message_profile);
            }

            void bind(UserMessage message){
                messageText.setText(message.getMessage());
                nameText.setText(message.getSender().getNickname());
                Utils.displayRoundImageFromURL(ChatActivity.this, message.getSender().getProfileUrl(), profileImage);
                timeText.setText(Utils.formatTime(message.getCreatedAt()));

            }
        }


        private synchronized boolean isMessageListLoading() {
            return mIsMessageListLoading;
        }

        private synchronized void setMessageListLoading(boolean tf){
            mIsMessageListLoading = tf;
        }
    }
}
