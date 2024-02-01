package com.example.employee_data_app;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.app.ProgressDialog;
import android.os.Bundle;
import android.util.Log;

import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;

public class AdminActivity extends AppCompatActivity {

    RecyclerView recyclerView;
    ArrayList<User> userArrayList;
    MyAdapter myAdapter;
    FirebaseFirestore db;

    ProgressDialog progressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin);

        progressDialog = new ProgressDialog(this);
        progressDialog.setCancelable(false);
        progressDialog.setMessage("Fetching Employee Data.....");
        progressDialog.show();

        recyclerView = findViewById(R.id.recyclerview);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        db = FirebaseFirestore.getInstance();
        userArrayList = new ArrayList<>();
        myAdapter = new MyAdapter(AdminActivity.this, userArrayList);
        recyclerView.setAdapter(myAdapter);

        eventChangeListener();
    }

    private void eventChangeListener() {
        db.collection("users")
                .addSnapshotListener(new EventListener<QuerySnapshot>() {
                    @Override
                    public void onEvent(@Nullable QuerySnapshot value, @Nullable FirebaseFirestoreException error) {
                        if (error != null) {
                            Log.e("Firestore error", error.getMessage());
                            if (progressDialog.isShowing())
                                progressDialog.dismiss();
                            return;
                        }

                        for (DocumentChange dc : value.getDocumentChanges()) {
                            if (dc.getType() == DocumentChange.Type.ADDED) {
                                User user = dc.getDocument().toObject(User.class);
                                userArrayList.add(user);
                            }
                        }

                        myAdapter.notifyDataSetChanged();
                        if (progressDialog.isShowing())
                            progressDialog.dismiss();
                    }
                });
    }
}
