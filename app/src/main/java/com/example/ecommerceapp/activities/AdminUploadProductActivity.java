package com.example.ecommerceapp.activities;

import android.Manifest;
import android.app.Activity;
import android.app.Dialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
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
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class AdminUploadProductActivity extends AppCompatActivity implements AdminAddProductAdapter.CountOfImagesWhenRemoved, AdminAddProductAdapter.ItemClickListener {
    RecyclerView recyclerView;
    TextView textView;
    Button pick, reset;

    ArrayList<Uri> uri = new ArrayList<>();
    AdminAddProductAdapter adapter;

    private static final int Read_Permission = 101;
    private static final int PICK_IMAGE = 1;

    ActivityResultLauncher<Intent> activityResultLauncher;

    private Uri imageUri;
    StorageReference storageReference;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_upload_product);

        textView = findViewById(R.id.totalPhotos);
        recyclerView = findViewById(R.id.recyclerView_Gallery_Images);
        pick = findViewById(R.id.pick);
        reset = findViewById(R.id.reset);

        adapter = new AdminAddProductAdapter(uri, getApplicationContext(), this, this);
        recyclerView.setLayoutManager(new GridLayoutManager(AdminUploadProductActivity.this, 4));
        recyclerView.setAdapter(adapter);

        pick.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (ContextCompat.checkSelfPermission(AdminUploadProductActivity.this, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                    ActivityCompat.requestPermissions(AdminUploadProductActivity.this,
                            new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, Read_Permission);

                    return;
                }

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
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == PICK_IMAGE && resultCode == RESULT_OK && null != data) {
            if (data.getClipData() != null) {
                int countOfImages = data.getClipData().getItemCount();
                for (int i = 0; i < countOfImages; i++) {
                    if (uri.size() < 11) {
                        imageUri = data.getClipData().getItemAt(i).getUri();
                        uri.add(imageUri);
                        uploadToFirebase();
                    } else {
                        Toast.makeText(AdminUploadProductActivity.this, "Tidak diizinkan untuk mengupload lebih dari 11 gambar", Toast.LENGTH_SHORT).show();
                    }
                }

                adapter.notifyDataSetChanged();
                textView.setText("Jumlah Foto: " + uri.size());
            } else {
                if (uri.size() < 11) {
                    imageUri = data.getData();
                    uri.add(imageUri);
                    uploadToFirebase();
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
        final String randomName = UUID.randomUUID().toString();
        storageReference = FirebaseStorage.getInstance().getReference().child("products_images/" + randomName);

        storageReference.putFile(imageUri)
                .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {

                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {

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
