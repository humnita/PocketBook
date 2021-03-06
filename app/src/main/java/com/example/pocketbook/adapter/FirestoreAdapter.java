// responsible for retrieving data from Firecloud
// loosely relates to an array of books(snapshots)
// it implements a listener to be able to get data in real time
// will add more functions to make it more robust and flexible
// TODO: get rid of this and use firestore-ui:firecloud

package com.example.pocketbook.adapter;

import android.util.Log;

import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;

public abstract class FirestoreAdapter<VH extends RecyclerView.ViewHolder>
        extends RecyclerView.Adapter<VH>
        implements EventListener<QuerySnapshot> {

    private static final String TAG = "Firestore Adapter";

    private Query mQuery;
    private ListenerRegistration mRegistration;

    private ArrayList<DocumentSnapshot> mSnapshots = new ArrayList<>();

    public FirestoreAdapter(Query query) {
        mQuery = query;
    }

    @Override
    public void onEvent(QuerySnapshot documentSnapshots,
                        FirebaseFirestoreException e){
        // errors
        if (e != null) {
            Log.w(TAG, "onEvent:error", e);
            return;
        }

        // Dispatch the event
        Log.d(TAG, "onEvent:numChanges:" + documentSnapshots.getDocumentChanges().size());
        for (DocumentChange dc : documentSnapshots.getDocumentChanges()) {
            // Snapshot of the changed document
            switch (dc.getType()) {
                case ADDED:
                    mSnapshots.add(dc.getNewIndex(), dc.getDocument());
                    notifyItemInserted(dc.getNewIndex());
                    break;

                case MODIFIED:
                    if (dc.getOldIndex() == dc.getNewIndex()) {
                        // Item changed but remained in same position
                        mSnapshots.set(dc.getOldIndex(), dc.getDocument());
                        notifyItemChanged(dc.getOldIndex());
                    } else {
                        // Item changed and changed position
                        mSnapshots.remove(dc.getOldIndex());
                        mSnapshots.add(dc.getNewIndex(), dc.getDocument());
                        notifyItemMoved(dc.getOldIndex(), dc.getNewIndex());
                    }
                    break;

                case REMOVED:
                    mSnapshots.remove(dc.getOldIndex());
                    notifyItemRemoved(dc.getOldIndex());
                    break;
            }
        }

        onDataChanged();
    }

    public void startListening() {
        if (mQuery != null && mRegistration == null) {
            mRegistration = mQuery.addSnapshotListener(this);
        }
    }

    public void stopListening() {
        if (mRegistration != null) {
            mRegistration.remove();
            mRegistration = null;
        }

        mSnapshots.clear();
        notifyDataSetChanged();
    }


    public void setQuery(Query query) {
        // Stop listening
        stopListening();

        // Clear existing data
        mSnapshots.clear();
        notifyDataSetChanged();

        // Listen to new query
        mQuery = query;
        startListening();
    }

    @Override
    public int getItemCount() {
        return mSnapshots.size();
    }

    protected DocumentSnapshot getSnapshot(int index) {
        return mSnapshots.get(index);
    }

    protected void onError(FirebaseFirestoreException e) {
        Log.w(TAG, "onError", e);
    };

    protected void onDataChanged() {}

}
