package com.example.citycycle;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.util.ArrayList;
import java.util.List;

public class UserAdapter extends RecyclerView.Adapter<UserAdapter.UserViewHolder> {
    private List<User> users = new ArrayList<>();
    private final UserClickListener clickListener;

    // Interface for click events
    public interface UserClickListener {
        void onUserClick(User user);
        void onUserLongClick(User user);
    }

    // Constructor with click listener
    public UserAdapter(UserClickListener listener) {
        this.clickListener = listener;
    }

    @NonNull
    @Override
    public UserViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_user, parent, false);
        return new UserViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull UserViewHolder holder, int position) {
        User user = users.get(position);
        holder.usernameTv.setText(user.getUsername());
        holder.emailTv.setText(user.getEmail());
        holder.mobileTv.setText(String.valueOf(user.getMobile()));
        holder.addressTv.setText(user.getAddress());

        // Set click listeners
        holder.itemView.setOnClickListener(v -> {
            if (clickListener != null) {
                clickListener.onUserClick(user);
            }
        });

        holder.itemView.setOnLongClickListener(v -> {
            if (clickListener != null) {
                clickListener.onUserLongClick(user);
                return true;
            }
            return false;
        });
    }

    @Override
    public int getItemCount() {
        return users.size();
    }

    public void setUsers(List<User> users) {
        this.users = users;
        notifyDataSetChanged();
    }

    static class UserViewHolder extends RecyclerView.ViewHolder {
        TextView usernameTv, emailTv, mobileTv, addressTv;

        UserViewHolder(View itemView) {
            super(itemView);
            usernameTv = itemView.findViewById(R.id.usernameTv);
            emailTv = itemView.findViewById(R.id.emailTv);
            mobileTv = itemView.findViewById(R.id.mobileTv);
            addressTv = itemView.findViewById(R.id.addressTv);
        }
    }
}