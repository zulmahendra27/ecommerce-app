package com.example.ecommerceapp.activities;

import static androidx.constraintlayout.helper.widget.MotionEffect.TAG;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.example.ecommerceapp.R;
import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class AdminAddCategoryActivity extends AppCompatActivity {
    EditText categoryNameAdmin, categoryTypeAdmin;
    ImageView categoryImg;
    Toolbar toolbar;
    Button adminAddCategoryBtn;
    Uri categoryUri = Uri.parse("");

    FirebaseFirestore firestore;
    FirebaseAuth auth;
    FirebaseStorage storage;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_add_category);

        toolbar = findViewById(R.id.admin_add_category_toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        firestore = FirebaseFirestore.getInstance();
        auth = FirebaseAuth.getInstance();
        storage = FirebaseStorage.getInstance();

        categoryNameAdmin = findViewById(R.id.category_name);
        categoryTypeAdmin = findViewById(R.id.category_type);
        categoryImg = findViewById(R.id.category_image);
        adminAddCategoryBtn = findViewById(R.id.admin_add_category_btn);

        categoryImg.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent();
                intent.setAction(Intent.ACTION_GET_CONTENT);
                intent.setType("image/*");
                startActivityForResult(intent, 33);
            }
        });

        adminAddCategoryBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                final StorageReference reference = storage.getReference().child("category_images").child(UUID.randomUUID().toString());
                UploadTask uploadTask = reference.putFile(categoryUri);
                String categoryName = categoryNameAdmin.getText().toString();
                String categoryType = categoryTypeAdmin.getText().toString();

                if (categoryName.isEmpty()) {
                    Toast.makeText(AdminAddCategoryActivity.this, "Nama tidak boleh kosong", Toast.LENGTH_SHORT).show();
                    return;
                }

                if (categoryType.isEmpty()) {
                    Toast.makeText(AdminAddCategoryActivity.this, "Tipe tidak boleh kosong", Toast.LENGTH_SHORT).show();
                    return;
                }

                Task<Uri> urlTask = uploadTask.continueWithTask(new Continuation<UploadTask.TaskSnapshot, Task<Uri>>() {
                    @Override
                    public Task<Uri> then(@NonNull Task<UploadTask.TaskSnapshot> task) throws Exception {
                        if (!task.isSuccessful()) {
                            throw task.getException();
                        }

                        // Continue with the task to get the download URL
                        return reference.getDownloadUrl();
                    }
                }).addOnCompleteListener(new OnCompleteListener<Uri>() {
                    @Override
                    public void onComplete(@NonNull Task<Uri> task) {
                        if (task.isSuccessful()) {
                            Uri downloadUri = task.getResult();

                            // Add a new document with a generated id.
                            Map<String, Object> data = new HashMap<>();
                            data.put("name", categoryName);
                            data.put("type", categoryType);
                            data.put("img_url", String.valueOf(downloadUri));

                            firestore.collection("Category")
                                    .add(data)
                                    .addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
                                        @Override
                                        public void onSuccess(DocumentReference documentReference) {
                                            Toast.makeText(AdminAddCategoryActivity.this, "Kategori berhasil ditambahkan", Toast.LENGTH_SHORT).show();
                                            startActivity(new Intent(AdminAddCategoryActivity.this, AdminDashboardActivity.class));
                                            Log.d(TAG, String.valueOf(downloadUri));
                                        }
                                    })
                                    .addOnFailureListener(new OnFailureListener() {
                                        @Override
                                        public void onFailure(@NonNull Exception e) {
                                            Log.w(TAG, "Error adding document", e);
                                        }
                                    });
                        } else {
                            // Handle failures
                            // ...
                        }
                    }
                });
            }
        });
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (data.getData() != null) {
            categoryUri = data.getData();
            categoryImg.setImageURI(categoryUri);
        }
    }
}