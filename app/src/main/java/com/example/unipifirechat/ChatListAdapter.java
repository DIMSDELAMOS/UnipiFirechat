package com.example.unipifirechat;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;

import java.util.List;

public class ChatListAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private final Context mContext;
    private final List<User> mUsers;

    // Νέα σταθερά για τους τύπους των views
    private static final int TYPE_CHAT = 0;
    private static final int TYPE_LOGOUT = 1;

    public ChatListAdapter(Context mContext, List<User> mUsers) {
        this.mContext = mContext;
        this.mUsers = mUsers;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        if (viewType == TYPE_CHAT) {
            View view = LayoutInflater.from(mContext).inflate(R.layout.item_chat_preview, parent, false);
            return new ChatViewHolder(view);
        } else { // TYPE_LOGOUT
            View view = LayoutInflater.from(mContext).inflate(R.layout.item_logout_button, parent, false);
            return new LogoutViewHolder(view);
        }
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        if (holder.getItemViewType() == TYPE_CHAT) {
            ChatViewHolder chatHolder = (ChatViewHolder) holder;
            User user = mUsers.get(position);

            chatHolder.username.setText(user.getUsername());
            // Λογική για άνοιγμα συνομιλίας
            chatHolder.itemView.setOnClickListener(v -> {
                Intent intent = new Intent(mContext, ChatActivity.class);
                intent.putExtra("TARGET_UID", user.getUid());
                intent.putExtra("TARGET_USERNAME", user.getUsername());
                mContext.startActivity(intent);
            });

        } else if (holder.getItemViewType() == TYPE_LOGOUT) {
            LogoutViewHolder logoutHolder = (LogoutViewHolder) holder;

            // Λογική για το πάτημα του κουμπιού Logout
            logoutHolder.buttonLogout.setOnClickListener(v -> {
                FirebaseAuth.getInstance().signOut();

                // Πλοήγηση πίσω στο AuthActivity
                Intent intent = new Intent(mContext, AuthActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                mContext.startActivity(intent);

                // Κλείνουμε το τρέχον Activity (MainActivity)
                if (mContext instanceof MainActivity) {
                    ((MainActivity) mContext).finish();
                }

                Toast.makeText(mContext, "Αποσύνδεση επιτυχής", Toast.LENGTH_SHORT).show();
            });
        }
    }

    @Override
    public int getItemCount() {
        // Ο αριθμός των χρηστών + 1 (για το κουμπί Logout)
        return mUsers.size() + 1;
    }

    @Override
    public int getItemViewType(int position) {
        if (position < mUsers.size()) {
            return TYPE_CHAT;
        } else {
            return TYPE_LOGOUT; // Το τελευταίο στοιχείο είναι το Logout
        }
    }

    // View Holder για τα στοιχεία συνομιλίας
    public static class ChatViewHolder extends RecyclerView.ViewHolder {
        public TextView username;

        public ChatViewHolder(View itemView) {
            super(itemView);
            username = itemView.findViewById(R.id.textViewChatUsername);
        }
    }

    // View Holder για το κουμπί Logout
    public static class LogoutViewHolder extends RecyclerView.ViewHolder {
        public Button buttonLogout;

        public LogoutViewHolder(View itemView) {
            super(itemView);
            buttonLogout = itemView.findViewById(R.id.buttonLogoutList);
        }
    }
}