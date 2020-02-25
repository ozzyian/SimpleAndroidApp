package com.example.communitytracker;

import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

/**
 * ViewHolder class for the items in the firebase recyclerView
 * with references to the texviews and buttons.
 */
public class UserOnlineViewHolder extends RecyclerView.ViewHolder {

    private TextView name;
    private TextView email;
    private Button callUser;
    private Button emailUser;

    public UserOnlineViewHolder(@NonNull View itemView) {
        super(itemView);

        callUser = itemView.findViewById(R.id.call_button);
        name = itemView.findViewById(R.id.user_name);
        email = itemView.findViewById(R.id.user_email);
        emailUser = itemView.findViewById(R.id.email_button);
    }

    public Button getEmailUserView(){return emailUser;}

    public TextView getNameView() {
        return name;
    }

    public TextView getEmailView() {
        return email;
    }

    public Button getCallUserView() {
        return callUser;
    }
}
