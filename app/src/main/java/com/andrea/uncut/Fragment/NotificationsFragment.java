package com.andrea.uncut.Fragment;

import android.os.Bundle;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.andrea.uncut.Adapter.NotificationsAdapter;
import com.andrea.uncut.R;
import com.andrea.uncut.Model.Notification;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;


public class NotificationsFragment extends Fragment {

    private RecyclerView recyclerView; // Recycler
    private NotificationsAdapter notificationsAdapter; // Adapter
    private List<Notification> notificationList; // List of notifications

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        // Inflate the recycler
        View view = inflater.inflate(R.layout.fragment_notifications, container, false);

        recyclerView = view.findViewById(R.id.recycler_view);
        recyclerView.setHasFixedSize(true); // Children of the recycler have fixed width and height
        LinearLayoutManager mLayoutManager = new LinearLayoutManager(getContext());  // Linear Layout
        recyclerView.setLayoutManager(mLayoutManager); // Set linear layout manager to recycler view
        notificationList = new ArrayList<>(); // List of notifications
        notificationsAdapter = new NotificationsAdapter(getContext(), notificationList); // Notifications adapter
        recyclerView.setAdapter(notificationsAdapter); // Set adapter to its recycler

        readNotifications(); // Read all notifications

        return view;
    }

    private void readNotifications(){
        FirebaseUser firebaseUser = FirebaseAuth.getInstance().getCurrentUser(); // Current user
        //Location of notifications
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Notifications").child(firebaseUser.getUid());

        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                notificationList.clear(); // Clear the list of notifications
                for (DataSnapshot snapshot : dataSnapshot.getChildren()){
                    Notification notification = snapshot.getValue(Notification.class); // Get single notification
                    notificationList.add(notification); // Add the notification to the list
                }

                Collections.reverse(notificationList); // Show most recent notifiations first
                notificationsAdapter.notifyDataSetChanged(); // Update notifications
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }
}