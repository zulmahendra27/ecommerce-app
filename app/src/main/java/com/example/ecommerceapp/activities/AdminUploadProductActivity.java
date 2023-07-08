package com.example.ecommerceapp.activities;

import static androidx.constraintlayout.helper.widget.MotionEffect.TAG;

import android.Manifest;
import android.app.Activity;
import android.app.Dialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.ecommerceapp.R;
import com.example.ecommerceapp.adapters.AdminAddProductAdapter;
import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.TaskCompletionSource;
import com.google.android.gms.tasks.Tasks;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class AdminUploadProductActivity extends AppCompatActivity implements AdminAddProductAdapter.CountOfImagesWhenRemoved, AdminAddProductAdapter.ItemClickListener {
    RecyclerView recyclerView;
    TextView textView;
    EditText productName, productDescription, productPrice;
    Button pick, reset, add;

    ArrayList<Uri> uri = new ArrayList<>();
    ArrayList<Task<Uri>> uploadTasks = new ArrayList<>();
    AdminAddProductAdapter adapter;

    private static final int Read_Permission = 101;
    private static final int PICK_IMAGE = 1;

    ActivityResultLauncher<Intent> activityResultLauncher;

    StorageReference firebaseStorage = FirebaseStorage.getInstance().getReference();

    private Uri imageUri;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_upload_product);

        textView = findViewById(R.id.totalPhotos);
        recyclerView = findViewById(R.id.recyclerView_Gallery_Images);
        pick = findViewById(R.id.pick);
        reset = findViewById(R.id.reset);
        add = findViewById(R.id.admin_upload_product_btn);

        adapter = new AdminAddProductAdapter(uri, getApplicationContext(), this, this);
        recyclerView.setLayoutManager(new GridLayoutManager(AdminUploadProductActivity.this, 4));
        recyclerView.setAdapter(adapter);

        pick.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
//                if (ContextCompat.checkSelfPermission(AdminUploadProductActivity.this, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                    ActivityCompat.requestPermissions(AdminUploadProductActivity.this,
                            new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, Read_Permission);

//                    return;
//                }

                Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
                intent.setType("image/*");
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR2) {
                    intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true);
                }
//                intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true);
//                intent.setAction(Intent.ACTION_GET_CONTENT);
                startActivityForResult(Intent.createChooser(intent, "Select Picture"), PICK_IMAGE);
            }
        });

        reset.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                uri.clear();
                adapter.notifyDataSetChanged();
                textView.setText("Jumlah Foto: " + uri.size());
            }
        });

        add.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                uploadToFirebase();
            }
        });
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == Read_Permission) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // Izin diberikan, lakukan tindakan yang diperlukan
                Log.d("PERIZINAN", "IZIN DIBERIKAN");
            } else {
                // Izin ditolak, tangani di sini (misalnya, tampilkan pesan kesalahan)
                Log.d("PERIZINAN", "IZIN DITOLAK");
//                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, Read_Permission);
            }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == PICK_IMAGE && resultCode == RESULT_OK && null != data) {
            if (data.getClipData() != null) {
                int countOfImages = data.getClipData().getItemCount();

                for (int i = 0; i < countOfImages; i++) {
                    if (uri.size() < 8) {
                        imageUri = data.getClipData().getItemAt(i).getUri();
                        uri.add(imageUri);
//                        uploadToFirebase();
                    } else {
                        Toast.makeText(AdminUploadProductActivity.this, "Tidak diizinkan untuk mengupload lebih dari 8 gambar", Toast.LENGTH_SHORT).show();
                    }
                }

                adapter.notifyDataSetChanged();
                textView.setText("Jumlah Foto: " + uri.size());
            } else {
                if (uri.size() < 8) {
                    imageUri = data.getData();
                    uri.add(imageUri);
//                    uploadToFirebase();
                } else {
                    Toast.makeText(AdminUploadProductActivity.this, "Tidak diizinkan untuk mengupload lebih dari 11 gambar", Toast.LENGTH_SHORT).show();
                }
            }

            adapter.notifyDataSetChanged();
            textView.setText("Jumlah Foto: " + uri.size());
        } else {
            Toast.makeText(this, "Kamu belum memilih gambar", Toast.LENGTH_SHORT).show();
        }
    }

    private void uploadToFirebase() {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        ArrayList<String> downloadUrls = new ArrayList<>();

        productName = findViewById(R.id.admin_product_name);
        productDescription = findViewById(R.id.admin_product_description);
        productPrice = findViewById(R.id.admin_product_price);

        String type = getIntent().getStringExtra("type");
        String categoryName = getIntent().getStringExtra("name");

        String name = productName.getText().toString();
        String description = productDescription.getText().toString();
        String rating = "4.5";
        String priceText = productPrice.getText().toString();

        if (name.isEmpty()) {
            Toast.makeText(AdminUploadProductActivity.this, "Nama tidak boleh kosong", Toast.LENGTH_SHORT).show();
            return;
        }

        if (description.isEmpty()) {
            Toast.makeText(AdminUploadProductActivity.this, "Deskripsi tidak boleh kosong", Toast.LENGTH_SHORT).show();
            return;
        }

        if (priceText.isEmpty()) {
            Toast.makeText(AdminUploadProductActivity.this, "Harga tidak boleh kosong", Toast.LENGTH_SHORT).show();
            return;
        }

        if (uri.size() == 0) {
            Toast.makeText(AdminUploadProductActivity.this, "Silahkan upload foto terlebih dahulu", Toast.LENGTH_SHORT).show();
            return;
        }

        int price = Integer.parseInt(priceText);

        for (Uri uriList : uri) {
            final String randomName = UUID.randomUUID().toString();
            final StorageReference storageReference = firebaseStorage.child("products_images_test/" + randomName);
            final TaskCompletionSource<Uri> taskCompletionSource = new TaskCompletionSource<>();

            UploadTask uploadTask = storageReference.putFile(uriList);
            uploadTasks.add(taskCompletionSource.getTask());

            uploadTask.addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                    storageReference.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                        @Override
                        public void onSuccess(Uri uri) {
                            String downloadUrl = uri.toString();
                            downloadUrls.add(downloadUrl);

                            taskCompletionSource.setResult(uri);
                        }
                    }).addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            Log.e("DOWNLOAD URL", "Error getting download URL: " + e.getMessage());
                            taskCompletionSource.setException(e);
                        }
                    });
                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    Log.e("UPLOAD FILE", "Error uploading file: " + e.getMessage());
                    taskCompletionSource.setException(e);
                }
            });
        }

        Task<Void> allUploadTask = Tasks.whenAll(uploadTasks);

        allUploadTask.addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void unused) {
                Log.d("DOWNLOAD URL ALL", String.valueOf(downloadUrls));
                // Add a new document with a generated id.
                Map<String, Object> data = new HashMap<>();
                data.put("name", name);
                data.put("type", type);
                data.put("description", description);
                data.put("price", price);
                data.put("rating", rating);
                data.put("img_url", downloadUrls);

                db.collection("Products")
                        .add(data)
                        .addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
                            @Override
                            public void onSuccess(DocumentReference documentReference) {
                                Toast.makeText(AdminUploadProductActivity.this, "Produk berhasil ditambahkan", Toast.LENGTH_SHORT).show();
                                Intent intent = new Intent(AdminUploadProductActivity.this, AdminProductActivity.class);
                                intent.putExtra("name", categoryName);
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
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Log.e("UPLOAD FILE", "Error uploading files: " + e.getMessage());
            }
        });
    }

    @Override
    public void clicked(int getSize) {
        textView.setText("Jumlah Foto: " + uri.size());
    }

    @Override
    public void itemClick(int position) {
        Dialog dialog = new Dialog(this);

        dialog.setContentView(R.layout.custom_dialog_zoom);

        TextView textView = dialog.findViewById(R.id.text_dialog);
        ImageView imageView = dialog.findViewById(R.id.image_view_dialog);
        Button buttonClose = dialog.findViewById(R.id.btn_close_dialog);

        textView.setText("Gambar " + position);
        imageView.setImageURI(uri.get(position));
        buttonClose.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
            }
        });

        dialog.show();
    }
}
