package com.example.pocketbook.fragment;

import android.annotation.SuppressLint;
import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.example.pocketbook.R;
import com.example.pocketbook.adapter.ProfileAdapter;
import com.example.pocketbook.adapter.RequestAdapter;
import com.example.pocketbook.model.Book;
import com.example.pocketbook.model.Request;
import com.example.pocketbook.model.User;
import com.example.pocketbook.util.FirebaseIntegrity;
import com.example.pocketbook.util.ScrollUpdate;
import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.Objects;

/**
 * Borrower Profile Page fragment that contains the user Profile (Books/Info)
 */
public class BorrowerFragment extends Fragment {

    private User currentUser;
    private FirebaseFirestore mFirestore;

    private static final int numColumns = 2;
    private static final int LIMIT = 20;

    private Query readyForPickupBooksQuery;
    private Query borrowedBooksQuery;
    private Query requestedBooksQuery;

    private RecyclerView mReadyForPickupBooksRecycler;
    private RecyclerView mBorrowedBooksRecycler;
    private RecyclerView mRequestedBooksRecycler;

    private ProfileAdapter readyForPickupBookAdapter;
    private ProfileAdapter borrowedBookAdapter;
    private ProfileAdapter requestedBookAdapter;

    private static final String USERS = "users";
    private ScrollUpdate scrollUpdate;
    private Fragment ownerFragment = this;
    private boolean firstTimeFragLoads = true;

    FirestoreRecyclerOptions<Book> readyForPickupBookOptions;
    FirestoreRecyclerOptions<Book> borrowedBookOptions;
    FirestoreRecyclerOptions<Book> requestedBookOptions;


    public static BorrowerFragment newInstance(User user) {
        BorrowerFragment ownerFragment = new BorrowerFragment();
        Bundle args = new Bundle();
        args.putSerializable("PF_USER", user);
        ownerFragment.setArguments(args);
        return ownerFragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // get the book argument passed to the newInstance() method
        if (getArguments() != null) {
            this.currentUser = (User) getArguments().getSerializable("PF_USER");
        }

        // Initialize Firestore
        mFirestore = FirebaseFirestore.getInstance();

        // query to retrieve accepted books
        readyForPickupBooksQuery = mFirestore.collection("catalogue")
                .whereArrayContains("requesters", currentUser.getEmail())
                .whereEqualTo("status", "ACCEPTED").limit(LIMIT);

        // query to retrieve borrowed books
        borrowedBooksQuery = mFirestore.collection("catalogue")
                .whereArrayContains("requesters", currentUser.getEmail())
                .whereEqualTo("status", "BORROWED").limit(LIMIT);

        // query to retrieve requested books
        requestedBooksQuery = mFirestore.collection("catalogue")
                .whereArrayContains("requesters", currentUser.getEmail())
                .whereEqualTo("status", "REQUESTED").limit(LIMIT);

        readyForPickupBookOptions = new FirestoreRecyclerOptions.Builder<Book>()
                .setQuery(readyForPickupBooksQuery, Book.class)
                .build();

        borrowedBookOptions = new FirestoreRecyclerOptions.Builder<Book>()
                .setQuery(borrowedBooksQuery, Book.class)
                .build();

        requestedBookOptions = new FirestoreRecyclerOptions.Builder<Book>()
                .setQuery(requestedBooksQuery, Book.class)
                .build();

        DocumentReference docRef = FirebaseFirestore.getInstance().collection("users")
                .document(currentUser.getEmail());
        docRef.addSnapshotListener((snapshot, e) -> {
            if (e != null) {
                Log.e("OWNER_LISTENER", "Listen failed.", e);
                return;
            }

            if ((snapshot != null) && snapshot.exists()) {
                currentUser = FirebaseIntegrity.getUserFromFirestore(snapshot);

                if (currentUser == null) {
                    return;
                }

                // TODO: Add isAdded to other listeners
                // if fragment can have a manager; tests crash without this line
                if ((!firstTimeFragLoads) && ownerFragment.isAdded()) {
                    getParentFragmentManager()
                            .beginTransaction()
                            .detach(BorrowerFragment.this)
                            .attach(BorrowerFragment.this)
                            .setTransition(FragmentTransaction.TRANSIT_NONE)
                            .addToBackStack(null)
                            .commitAllowingStateLoss();
                } else {
                    firstTimeFragLoads = false;
                }
            }
            else if (ownerFragment.isAdded()) {
                getParentFragmentManager().beginTransaction()
                        .detach(BorrowerFragment.this).commitAllowingStateLoss();
            }
        });
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        // Inflate the layout for this fragment
        View rootView = inflater.inflate(R.layout.fragment_borrower, container, false);

        LinearLayout titleReadyForPickups = rootView.findViewById(R.id.TitleBarReadyForPickupBorrower);
        LinearLayout titleRequested = rootView.findViewById(R.id.TitleBarRequestedBorrower);
        LinearLayout titleBorrowed = rootView.findViewById(R.id.TitleBarBorrowedBorrower);

        TextView viewAllReadyForPickups = rootView.findViewById(R.id.ViewAllReadyForPickupBorrower);
        TextView viewAllRequestedBooks = rootView.findViewById(R.id.ViewAllRequestedBorrower);
        TextView viewAllBorrowedBooks = rootView.findViewById(R.id.ViewAllBorrowedBorrower);

        mReadyForPickupBooksRecycler = rootView.findViewById(R.id.profileBorrowerRecyclerReadyForPickupBooks);
        LinearLayoutManager readyForPickuplayoutManager = new LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL, false);

        mReadyForPickupBooksRecycler.setLayoutManager(readyForPickuplayoutManager);
        FirestoreRecyclerOptions<Book> readyForPickupOptions = new FirestoreRecyclerOptions.Builder<Book>()
                .setQuery(readyForPickupBooksQuery, Book.class)
                .build();
        readyForPickupBookAdapter = new ProfileAdapter(readyForPickupOptions, currentUser, getActivity(), titleReadyForPickups, mReadyForPickupBooksRecycler);
        mReadyForPickupBooksRecycler.setAdapter(readyForPickupBookAdapter);

        mBorrowedBooksRecycler = rootView.findViewById(R.id.profileBorrowerRecyclerBorrowedBooks);
        LinearLayoutManager borrowedlayoutManager = new LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL, false);
        mBorrowedBooksRecycler.setLayoutManager(borrowedlayoutManager);
        FirestoreRecyclerOptions<Book> borrowedOptions = new FirestoreRecyclerOptions.Builder<Book>()
                .setQuery(borrowedBooksQuery, Book.class)
                .build();
        borrowedBookAdapter = new ProfileAdapter(borrowedOptions, currentUser, getActivity(), titleBorrowed, mBorrowedBooksRecycler);
        mBorrowedBooksRecycler.setAdapter(borrowedBookAdapter);

        mRequestedBooksRecycler = rootView.findViewById(R.id.profileBorrowerRecyclerRequestedBooks);
        LinearLayoutManager requestedlayoutManager = new LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL, false);
        mRequestedBooksRecycler.setLayoutManager(requestedlayoutManager);
        FirestoreRecyclerOptions<Book> requestedOptions = new FirestoreRecyclerOptions.Builder<Book>()
                .setQuery(requestedBooksQuery, Book.class)
                .build();
        requestedBookAdapter = new ProfileAdapter(requestedOptions, currentUser, getActivity(), titleRequested, mRequestedBooksRecycler);
        mRequestedBooksRecycler.setAdapter(requestedBookAdapter);

        // initially hide views if they have no content
        if (readyForPickupOptions.getSnapshots().size() == 0) {
            titleReadyForPickups.setVisibility(View.GONE);
            mReadyForPickupBooksRecycler.setVisibility(View.GONE);
        }
        if (requestedOptions.getSnapshots().size() == 0) {
            titleRequested.setVisibility(View.GONE);
            mRequestedBooksRecycler.setVisibility(View.GONE);
        }
        if (borrowedOptions.getSnapshots().size() == 0) {
            titleBorrowed.setVisibility(View.GONE);
            mBorrowedBooksRecycler.setVisibility(View.GONE);
        }

        viewAllReadyForPickups.setOnClickListener(v1 -> {

            ReadyForPickupFragment readyForPickupFragment
                    = ReadyForPickupFragment.newInstance(currentUser);
            Bundle bundle = new Bundle();
            bundle.putSerializable("PF_USER", currentUser);
            readyForPickupFragment.setArguments(bundle);
            Objects.requireNonNull(getActivity())
                    .getSupportFragmentManager().beginTransaction()
                    .replace(getActivity().findViewById(R.id.container).getId(),
                            readyForPickupFragment)
                    .addToBackStack(null).commit();

        });

        viewAllRequestedBooks.setOnClickListener(v1 -> {

            RequestedBooksFragment requestedBooksFragment
                    = RequestedBooksFragment.newInstance(currentUser);
            Bundle bundle = new Bundle();
            bundle.putSerializable("PF_USER", currentUser);
            requestedBooksFragment.setArguments(bundle);
            Objects.requireNonNull(getActivity())
                    .getSupportFragmentManager().beginTransaction()
                    .replace(getActivity().findViewById(R.id.container).getId(),
                            requestedBooksFragment)
                    .addToBackStack(null).commit();

        });

        viewAllBorrowedBooks.setOnClickListener(v1 -> {

            BorrowedBooksFragment borrowedBooksFragment
                    = BorrowedBooksFragment.newInstance(currentUser);
            Bundle bundle = new Bundle();
            bundle.putSerializable("PF_USER", currentUser);
            borrowedBooksFragment.setArguments(bundle);
            Objects.requireNonNull(getActivity())
                    .getSupportFragmentManager().beginTransaction()
                    .replace(getActivity().findViewById(R.id.container).getId(),
                            borrowedBooksFragment)
                    .addToBackStack(null).commit();

        });

        return rootView;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    @Override
    public void onStart() {
        super.onStart();
        readyForPickupBookAdapter.startListening();
        borrowedBookAdapter.startListening();
        requestedBookAdapter.startListening();
    }

    @Override
    public void onStop() {
        super.onStop();
        readyForPickupBookAdapter.stopListening();
        borrowedBookAdapter.stopListening();
        requestedBookAdapter.stopListening();
    }
}