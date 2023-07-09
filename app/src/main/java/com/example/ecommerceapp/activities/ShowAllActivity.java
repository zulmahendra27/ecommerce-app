package com.example.ecommerceapp.activities;

import static androidx.constraintlayout.helper.widget.MotionEffect.TAG;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import com.example.ecommerceapp.R;
import com.example.ecommerceapp.adapters.ShowAllAdapter;
import com.example.ecommerceapp.models.ShowAllModel;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.List;

public class ShowAllActivity extends AppCompatActivity {
    ShowAllAdapter showAllAdapter;
    List<ShowAllModel> showAllModelList;
    boolean isAdmin;

    Toolbar toolbar;
    FirebaseFirestore firestore;
    FirebaseAuth auth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_show_all);

        toolbar = findViewById(R.id.show_all_toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });

        String type = getIntent().getStringExtra("type");
        String name = getIntent().getStringExtra("name");

        RecyclerView recyclerView = findViewById(R.id.show_all_rec);

        recyclerView.setLayoutManager(new GridLayoutManager(this, 2));
        showAllModelList = new ArrayList<>();
        showAllAdapter = new ShowAllAdapter(this, showAllModelList, isAdmin);
        recyclerView.setAdapter(showAllAdapter);

        String title = "";

        if (name == null || name.isEmpty()) {
            title = "Semua Produk";
        } else {
            title = name;
        }

        toolbar.setTitle(title);

        firestore = FirebaseFirestore.getInstance();
        auth = FirebaseAuth.getInstance();

        firestore.collection("Users")
                .whereEqualTo("uid", auth.getUid())
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            for (DocumentSnapshot document : task.getResult()) {
                                String level = document.getString("level");
                                if (level.equalsIgnoreCase("admin")) {
                                    isAdmin = true;
                                    showAllAdapter.setAdminMode(isAdmin);
                                }
                                break;
                            }
                        } else {
                            Log.d(TAG, "Error getting documents: ", task.getException());
                        }
                    }
                });

        if (type == null || type.isEmpty()) {
            firestore.collection("Products")
                    .get()
                    .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                        @Override
                        public void onComplete(@NonNull Task<QuerySnapshot> task) {
                            if (task.isSuccessful()) {
                                for (DocumentSnapshot doc : task.getResult().getDocuments()) {
                                    ShowAllModel showAllModel = doc.toObject(ShowAllModel.class);
                                    showAllModel.setId(doc.getId());
                                    showAllModelList.add(showAllModel);
                                    showAllAdapter.notifyDataSetChanged();
                                }
                            }
                        }
                    });
        }

        if (type != null && !type.isEmpty()) {
            firestore.collection("Products").whereEqualTo("type", type)
                    .get()
                    .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                        @Override
                        public void onComplete(@NonNull Task<QuerySnapshot> task) {
                            if (task.isSuccessful()) {
                                for (DocumentSnapshot doc : task.getResult().getDocuments()) {
                                    ShowAllModel showAllModel = doc.toObject(ShowAllModel.class);
                                    showAllModel.setId(doc.getId());
                                    showAllModelList.add(showAllModel);
                                    showAllAdapter.notifyDataSetChanged();
                                }
                            }
                        }
                    });
        }
    }
}