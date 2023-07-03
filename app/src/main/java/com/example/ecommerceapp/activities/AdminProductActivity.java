package com.example.ecommerceapp.activities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import com.example.ecommerceapp.R;
import com.example.ecommerceapp.adapters.AdminProductAdapter;
import com.example.ecommerceapp.adapters.ShowAllAdapter;
import com.example.ecommerceapp.models.ShowAllModel;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.List;

public class AdminProductActivity extends AppCompatActivity {
    AdminProductAdapter adminProductAdapter;
    List<ShowAllModel> showAllModelList;

    TextView productAdd;
    Toolbar toolbar;
    FirebaseFirestore firestore;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_product);

        toolbar = findViewById(R.id.admin_product_toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        String type = getIntent().getStringExtra("type");
        String name = getIntent().getStringExtra("name");

        productAdd = findViewById(R.id.product_add_btn);

        productAdd.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(AdminProductActivity.this, AdminAddProductActivity.class);
                intent.putExtra("name", name);
                intent.putExtra("type", type);
                startActivity(intent);
            }
        });

        RecyclerView recyclerView = findViewById(R.id.admin_product_rec);

        recyclerView.setLayoutManager(new GridLayoutManager(this, 1));
        showAllModelList = new ArrayList<>();
        adminProductAdapter = new AdminProductAdapter(this, showAllModelList);
        recyclerView.setAdapter(adminProductAdapter);

        String title = "";

        if (name == null || name.isEmpty()) {
            title = "Semua Produk";
        } else {
            title = name;
        }

        toolbar.setTitle(title);

        firestore = FirebaseFirestore.getInstance();

        if (type != null || !type.isEmpty()) {
            Log.d("TYPEPRODUCT", type);
            firestore.collection("AllProducts").whereEqualTo("type", type)
                    .get()
                    .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                        @Override
                        public void onComplete(@NonNull Task<QuerySnapshot> task) {
                            if (task.isSuccessful()) {
                                for (DocumentSnapshot doc : task.getResult().getDocuments()) {
                                    ShowAllModel showAllModel = doc.toObject(ShowAllModel.class);
                                    showAllModelList.add(showAllModel);
                                    adminProductAdapter.notifyDataSetChanged();
                                }
                            }
                        }
                    });
        }
    }
}