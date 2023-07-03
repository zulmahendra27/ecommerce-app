package com.example.ecommerceapp.activities;

import static androidx.constraintlayout.helper.widget.MotionEffect.TAG;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
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

public class AdminAddProductActivity extends AppCompatActivity {
    EditText productName, productDescription, productPrice;
    ImageView productImg;
    Toolbar toolbar;
    Button adminAddProductBtn;
    Uri productUri = Uri.parse("");

    FirebaseFirestore firestore;
    FirebaseAuth auth;
    FirebaseStorage storage;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_add_product);

        toolbar = findViewById(R.id.admin_add_product_toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        firestore = FirebaseFirestore.getInstance();
        auth = FirebaseAuth.getInstance();
        storage = FirebaseStorage.getInstance();

        String name = getIntent().getStringExtra("name");
        String type = getIntent().getStringExtra("type");

        productName = findViewById(R.id.admin_product_name);
        productDescription = findViewById(R.id.admin_product_description);
        productPrice = findViewById(R.id.admin_product_price);
        productImg = findViewById(R.id.admin_product_image);
        adminAddProductBtn = findViewById(R.id.admin_add_product_btn);

        productImg.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent();
                intent.setAction(Intent.ACTION_GET_CONTENT);
                intent.setType("image/*");
                startActivityForResult(intent, 33);
            }
        });

        adminAddProductBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                final StorageReference reference = storage.getReference().child("products_images").child(UUID.randomUUID().toString());
                UploadTask uploadTask = reference.putFile(productUri);
                String name = productName.getText().toString();
                String description = productDescription.getText().toString();
                String rating = "4.5";
                String priceText = productPrice.getText().toString();

                if (name.isEmpty()) {
                    Toast.makeText(AdminAddProductActivity.this, "Nama tidak boleh kosong", Toast.LENGTH_SHORT).show();
                    return;
                }

                if (description.isEmpty()) {
                    Toast.makeText(AdminAddProductActivity.this, "Deskripsi tidak boleh kosong", Toast.LENGTH_SHORT).show();
                    return;
                }

                if (priceText.isEmpty()) {
                    Toast.makeText(AdminAddProductActivity.this, "Harga tidak boleh kosong", Toast.LENGTH_SHORT).show();
                    return;
                }

                int price = Integer.parseInt(priceText);

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
                            data.put("name", name);
                            data.put("type", type);
                            data.put("description", description);
                            data.put("price", price);
                            data.put("rating", rating);
                            data.put("img_url", String.valueOf(downloadUri));

                            firestore.collection("AllProducts")
                                    .add(data)
                                    .addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
                                        @Override
                                        public void onSuccess(DocumentReference documentReference) {
                                            Toast.makeText(AdminAddProductActivity.this, "Produk berhasil ditambahkan", Toast.LENGTH_SHORT).show();
                                            Intent intent = new Intent(AdminAddProductActivity.this, AdminProductActivity.class);
                                            intent.putExtra("name", name);
                                            intent.putExtra("type", type);
                                            startActivity(intent);
//                                            Log.d(TAG, String.valueOf(downloadUri));
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
            productUri = data.getData();
            productImg.setImageURI(productUri);
        }
    }
}