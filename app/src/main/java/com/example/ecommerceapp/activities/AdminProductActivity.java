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
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

public class AdminProductActivity extends AppCompatActivity implements AdminProductAdapter.ProductDeleteListener {
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

        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });

        String type = getIntent().getStringExtra("type");
        String name = getIntent().getStringExtra("name");

        productAdd = findViewById(R.id.product_add_btn);

        productAdd.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(AdminProductActivity.this, AdminUploadProductActivity.class);
                intent.putExtra("name", name);
                intent.putExtra("type", type);
                startActivity(intent);
            }
        });

        RecyclerView recyclerView = findViewById(R.id.admin_product_rec);

        recyclerView.setLayoutManager(new GridLayoutManager(this, 1));
        showAllModelList = new ArrayList<>();
        adminProductAdapter = new AdminProductAdapter(AdminProductActivity.this, showAllModelList, AdminProductActivity.this);
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
                                    adminProductAdapter.notifyDataSetChanged();
                                }
                            }
                        }
                    });
        }
    }

    @Override
    public void deleteProduct(ShowAllModel product) {
        // Menghapus dokumen dari Firestore
        firestore.collection("Products").document(product.getId())
                .delete()
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        // Menghapus file gambar dari Firebase Storage
                        ArrayList<String> imgUrls = product.getImg_url();
                        if (imgUrls != null && !imgUrls.isEmpty()) {
                            AtomicInteger numDeletedImages = new AtomicInteger(0);
                            int numImages = imgUrls.size();

                            for (String imgUrl : imgUrls) {
                                StorageReference imageRef = FirebaseStorage.getInstance().getReferenceFromUrl(imgUrl);
                                imageRef.delete()
                                        .addOnSuccessListener(new OnSuccessListener<Void>() {
                                            @Override
                                            public void onSuccess(Void aVoid) {
                                                if (numDeletedImages.incrementAndGet() == numImages) {
                                                    refreshProductList(product);
                                                }
                                            }
                                        })
                                        .addOnFailureListener(new OnFailureListener() {
                                            @Override
                                            public void onFailure(@NonNull Exception e) {
                                                if (numDeletedImages.incrementAndGet() == numImages) {
                                                    refreshProductList(product);
                                                }
                                            }
                                        });
                            }
                        } else {
                            refreshProductList(product);
                        }
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        // Gagal menghapus dokumen
                    }
                });
    }

    private void refreshProductList(ShowAllModel product) {
        // Menghapus item produk dari tampilan
        showAllModelList.remove(product);
        adminProductAdapter.notifyDataSetChanged();
    }
}