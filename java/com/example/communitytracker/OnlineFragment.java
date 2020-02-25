package com.example.communitytracker;


import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;


import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;


/**
 * Application fragment which handles the information
 * and display of users who are currently online.
 */
public class OnlineFragment extends Fragment {

    private RecyclerView recyclerView;
    private FirebaseRecyclerOptions<OnlineItem> query;
    private FirebaseRecyclerAdapter<OnlineItem, UserOnlineViewHolder> adapter;
    private DatabaseReference onlineUsers;

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
    }

    /**
     * Creates the View for the fragment and initiates a FirebaseRecyclerAdapter and FirebaseRecyclerOptions
     * which handles the display of users who comes online and removes them
     * when they go offline.
     *
     * @param inflater inflater to inflate the layout
     * @param container container of the views
     * @param savedInstanceState any saved instances
     * @return
     */
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        final String userID = FirebaseAuth.getInstance().getCurrentUser().getUid();
        View view = inflater.inflate(R.layout.fragment_online, container, false);
        recyclerView = view.findViewById(R.id.recycler_view);
        recyclerView.setHasFixedSize(true);
        onlineUsers = FirebaseDatabase.getInstance().getReference("users/online");
        query = new FirebaseRecyclerOptions.Builder<OnlineItem>()
                .setQuery(onlineUsers, OnlineItem.class).build();


        /**
         * The adapter is initiated with the recyclerOption query and then initiates the viewHolder with
         * the user data from the model (OnlineItem). Since you can't exclude data from the query of
         * the databasereference I simply hide the rendered OnlineItem of the currentUser (ugly implementation but I
         * found no other way to do it using the FirebaseUI recyclerAdapter.
         */
        adapter = new FirebaseRecyclerAdapter<OnlineItem, UserOnlineViewHolder>(query) {
            @Override
            protected void onBindViewHolder(@NonNull UserOnlineViewHolder holder, int position, @NonNull OnlineItem model) {
                final OnlineItem userModel = model;
                if (userModel.getID().equals(userID)) {
                    holder.itemView.setVisibility(View.GONE);
                    holder.itemView.setLayoutParams(new RecyclerView.LayoutParams(0, 0));
                } else {
                    holder.getNameView().setText(model.getName());
                    holder.getEmailView().setText(model.getEmail());
                    Log.println(Log.DEBUG, "onlineFragment", userModel.getID() + " " + userID);

                    /**
                     * ClickListener that calls an online user by their entered phone number
                     */
                    holder.getCallUserView().setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            String phoneNumber = userModel.getPhoneNumber();
                            Toast.makeText(getContext(), "Calling :" + phoneNumber, Toast.LENGTH_SHORT).show();
                            if (ContextCompat.checkSelfPermission(getContext(), Manifest.permission.CALL_PHONE) != PackageManager.PERMISSION_GRANTED) {
                                ActivityCompat.requestPermissions(getActivity(), new String[]{Manifest.permission.CALL_PHONE}, 1);
                            } else {
                                Intent intent = new Intent(Intent.ACTION_CALL, Uri.parse("tel:" + phoneNumber));
                                startActivity(intent);
                            }

                        }
                    });
                    /**
                     * ClickListener that calls starts the activity to mail an online user
                     */
                    holder.getEmailUserView().setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            Intent intent = new Intent(Intent.ACTION_SENDTO);
                            intent.setData(Uri.parse("mailto:"));
                            intent.putExtra(Intent.EXTRA_EMAIL, new String[]{userModel.getEmail()});
                            startActivity(Intent.createChooser(intent,"Choose a mail program"));
                        }
                    });

                }


            }

            /**
             * Inflates the parent layout with the online_item layout
             * and returns the inflated view object.
             * @param parent
             * @param viewType
             * @return
             */
            @NonNull
            @Override
            public UserOnlineViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

                View onlineView = LayoutInflater.from(parent.getContext()).inflate(R.layout.online_item, parent, false);
                return new UserOnlineViewHolder(onlineView);
            }
        };

        GridLayoutManager gridLayoutManager = new GridLayoutManager(getContext(), 1);
        recyclerView.setLayoutManager(gridLayoutManager);
        adapter.startListening();
        recyclerView.setAdapter(adapter);

        return view;

    }

    /**
     * Sends the saved states if there any
     * if a screen rotation has occurred.
     * @param state
     */
    @Override
    public void onSaveInstanceState(Bundle state) {
        super.onSaveInstanceState(state);
    }

}
