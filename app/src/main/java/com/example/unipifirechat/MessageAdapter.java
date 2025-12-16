package com.example.unipifirechat;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.text.SimpleDateFormat; // <-- Import για την ώρα
import java.util.Date;           // <-- Import για την ημερομηνία
import java.util.List;
import java.util.Locale;         // <-- Import για τη γλώσσα ώρας

public class MessageAdapter extends RecyclerView.Adapter<MessageAdapter.MessageViewHolder> {

    public static final int MSG_TYPE_LEFT = 0;
    public static final int MSG_TYPE_RIGHT = 1;

    private final Context mContext;
    private final List<Message> mMessageList;
    private FirebaseUser firebaseUser;

    public MessageAdapter(Context mContext, List<Message> mMessageList) {
        this.mContext = mContext;
        this.mMessageList = mMessageList;
        this.firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
    }

    @NonNull
    @Override
    public MessageViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        if (viewType == MSG_TYPE_RIGHT) {
            View view = LayoutInflater.from(mContext).inflate(R.layout.item_message_sent, parent, false);
            return new MessageViewHolder(view);
        } else {
            View view = LayoutInflater.from(mContext).inflate(R.layout.item_message_received, parent, false);
            return new MessageViewHolder(view);
        }
    }

    @Override
    public void onBindViewHolder(@NonNull MessageViewHolder holder, int position) {
        Message message = mMessageList.get(position);

        // 1. Εμφάνιση κειμένου μηνύματος
        holder.show_message.setText(message.getText());

        // 2. ΕΞΥΠΝΗ Εμφάνιση Ώρας/Ημερομηνίας
        long timestamp = message.getTimestamp();
        Date messageDate = new Date(timestamp);
        Date todayDate = new Date(); // Η τρέχουσα ώρα

        // Φτιάχνουμε έναν formatter για να ελέγξουμε αν είναι η ίδια μέρα (π.χ. "20251211")
        SimpleDateFormat dayCheckFormat = new SimpleDateFormat("yyyyMMdd", Locale.getDefault());

        String messageDay = dayCheckFormat.format(messageDate);
        String currentDay = dayCheckFormat.format(todayDate);

        if (messageDay.equals(currentDay)) {
            // ΕΙΝΑΙ ΣΗΜΕΡΑ: Δείχνουμε μόνο ώρα (π.χ. "10:30")
            SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm", Locale.getDefault());
            holder.show_time.setText(timeFormat.format(messageDate));
        } else {
            // ΔΕΝ ΕΙΝΑΙ ΣΗΜΕΡΑ: Δείχνουμε ημερομηνία και ώρα (π.χ. "10/12/2025 10:30")
            SimpleDateFormat fullFormat = new SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault());
            holder.show_time.setText(fullFormat.format(messageDate));
        }
    }

    @Override
    public int getItemCount() {
        return mMessageList.size();
    }

    @Override
    public int getItemViewType(int position) {
        if (firebaseUser == null || firebaseUser.getUid() == null) {
            return MSG_TYPE_LEFT;
        }

        String senderId = mMessageList.get(position).getSenderId();
        if (senderId != null && senderId.equals(firebaseUser.getUid())) {
            return MSG_TYPE_RIGHT;
        } else {
            return MSG_TYPE_LEFT;
        }
    }

    public static class MessageViewHolder extends RecyclerView.ViewHolder {
        public TextView show_message;
        public TextView show_time; // <-- ΝΕΟ: Το TextView για την ώρα

        public MessageViewHolder(View itemView) {
            super(itemView);
            show_message = itemView.findViewById(R.id.textViewMessageContent);
            show_time = itemView.findViewById(R.id.textViewMessageTime); // <-- Σύνδεση με το XML
        }
    }
}