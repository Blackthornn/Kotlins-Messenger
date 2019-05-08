package com.theblackthorn.kotlinsmessenger.messages

import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.ChildEventListener
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.squareup.picasso.Picasso
import com.theblackthorn.kotlinsmessenger.R
import com.theblackthorn.kotlinsmessenger.models.ChatMessage
import com.theblackthorn.kotlinsmessenger.models.User
import com.xwray.groupie.GroupAdapter
import com.xwray.groupie.Item
import com.xwray.groupie.ViewHolder
import kotlinx.android.synthetic.main.activity_chat_log.*
import kotlinx.android.synthetic.main.chat_from_row.view.*
import kotlinx.android.synthetic.main.chat_to_row.view.*

class ChatLogActivity : AppCompatActivity() {

    companion object {
        val TAG = "ChatLog"
    }

    val adapter = GroupAdapter<ViewHolder>()

    var toUser: User? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_chat_log)

        recyclerViewChatLog.adapter = adapter

        toUser = intent.getParcelableExtra<User>(NewMessageActivity.USER_KEY)
        supportActionBar?.title = toUser?.username

        //setupDummyData()
        listenForMessages()

        chatLogSendButton.setOnClickListener {
            Log.d(TAG, "Attempt to send message...")
            performSendMessage()
        }
    }

    private fun listenForMessages() {
        val fromId = FirebaseAuth.getInstance().uid
        val toId = toUser?.uid
        val ref = FirebaseDatabase.getInstance().getReference("/user-messages/$fromId/$toId")

        ref.addChildEventListener(object : ChildEventListener {

            override fun onCancelled(p0: DatabaseError) {

            }

            override fun onChildMoved(p0: DataSnapshot, p1: String?) {

            }

            override fun onChildChanged(p0: DataSnapshot, p1: String?) {

            }

            override fun onChildAdded(p0: DataSnapshot, p1: String?) {
                val chatMessage = p0.getValue(ChatMessage::class.java)

                if (chatMessage != null) {
                    Log.d(TAG, chatMessage.text)

                    if (chatMessage.fromId == FirebaseAuth.getInstance().uid) {
                        val currentUser = LatestMessagesActivity.currentUser ?: return
                        adapter.add(ChatFromItem(chatMessage.text, currentUser))
                    } else {
                        adapter.add(ChatToItem(chatMessage.text, toUser!!))
                    }
                }

            }

            override fun onChildRemoved(p0: DataSnapshot) {

            }

        })
    }


    private fun performSendMessage() {

        val text = chatLogEditText.text.toString()

        val fromID = FirebaseAuth.getInstance().uid
        val user = intent.getParcelableExtra<User>(NewMessageActivity.USER_KEY)
        val toId = user.uid

        if (fromID == null) return

//        val reference = FirebaseDatabase.getInstance().getReference("/messages").push()
        val reference = FirebaseDatabase.getInstance().getReference("/user-messages/$fromID/$toId").push()

        val toReference = FirebaseDatabase.getInstance().getReference("/user-messages/$toId/$fromID").push()

        val chatMessage = ChatMessage(reference.key!!, text, fromID, toId, System.currentTimeMillis() / 1000)
        reference.setValue(chatMessage)
            .addOnSuccessListener {
                Log.d(TAG, "Saved the chat message: ${reference.key}")
                chatLogEditText.text.clear()
                recyclerViewChatLog.scrollToPosition(adapter.itemCount - 1)
            }

        toReference.setValue(chatMessage)
            .addOnSuccessListener {
                chatLogEditText.text.clear()
            }
    }

}

class ChatFromItem(val text: String, val user: User) : Item<ViewHolder>() {
    override fun getLayout(): Int {
        return R.layout.chat_from_row
    }

    override fun bind(viewHolder: ViewHolder, position: Int) {
        viewHolder.itemView.chatFromRowTextView.text = text

        val uri = user.profileImageUrl
        val targetImageView = viewHolder.itemView.chatFromRowImageView
        Picasso.get().load(uri).into(targetImageView)

    }

}

class ChatToItem(val text: String, val user: User) : Item<ViewHolder>() {
    override fun getLayout(): Int {
        return R.layout.chat_to_row
    }

    override fun bind(viewHolder: ViewHolder, position: Int) {
        viewHolder.itemView.chatToRowTextView.text = text

        //load the image into the star
        val uri = user.profileImageUrl
        val targetImageView = viewHolder.itemView.chatToRowImageView
        Picasso.get().load(uri).into(targetImageView)
    }

}