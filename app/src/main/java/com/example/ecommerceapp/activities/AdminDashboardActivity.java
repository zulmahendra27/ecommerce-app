package com.example.ecommerceapp.activities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import com.example.ecommerceapp.R;
import com.example.ecommerceapp.adapters.AdminDashboardAdapter;
import com.example.ecommerceapp.adapters.ShowAllAdapter;
import com.example.ecommerceapp.models.AdminDashboardModel;
import com.example.ecommerceapp.models.ShowAllModel;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.List;

public class AdminDashboardActivity extends AppCompatActivity {
    Toolbar toolbar;
    TextView categoryAdd;
    AdminDashboardAdapter adminDashboardAdapter;
    List<AdminDashboardModel> adminDashboardModelList;
    FirebaseFirestore firestore;
    FirebaseAuth auth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_dashboard);

        categoryAdd = findViewById(R.id.category_add_btn);

        categoryAdd.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(AdminDashboardActivity.this, AdminAddCategoryActivity.class);
                startActivity(intent);
            }
        });

        toolbar = findViewById(R.id.admin_dashboard_toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeAsUpIndicator(R.drawable.ic_baseline_menu_24);

        RecyclerView recyclerView = findViewById(R.id.category_rec);

        recyclerView.setLayoutManager(new GridLayoutManager(this, 2));
        adminDashboardModelList = new ArrayList<>();
        adminDashboardAdapter = new AdminDashboardAdapter(this, adminDashboardModelList);
        recyclerView.setAdapter(adminDashboardAdapter);

        firestore = FirebaseFirestore.getInstance();

        firestore.collection("Category")
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            for (DocumentSnapshot doc : task.getResult().getDocuments()) {
                                AdminDashboardModel adminDashboardModel = doc.toObject(AdminDashboardModel.class);
                                adminDashboardModelList.add(adminDashboardModel);
                                adminDashboardAdapter.notifyDataSetChanged();
                            }
                        }
                    }
                });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main_menu_admin, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();
        auth = FirebaseAuth.getInstance();

        if (id == R.id.menu_logout_admin) {
            auth.signOut();
            startActivity(new Intent(AdminDashboardActivity.this, LoginActivity.class));
            finish();
        }

        return true;
    }
}