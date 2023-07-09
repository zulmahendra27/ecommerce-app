package com.example.ecommerceapp.activities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.viewpager.widget.ViewPager;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.example.ecommerceapp.R;
import com.example.ecommerceapp.adapters.ImageSliderAdapter;
import com.example.ecommerceapp.models.NewProductsModel;
import com.example.ecommerceapp.models.PopularProductsModel;
import com.example.ecommerceapp.models.ShowAllModel;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;

public class DetailedActivity extends AppCompatActivity {
    TextView rating, name, description, price, quantity;
    Button addToCart, buyNow;
    ImageView addItems, removeItems;

    Toolbar toolbar;

    int totalQuantity = 1;
    int totalPrice = 0;
    int productPrice = 0;

    NewProductsModel newProductsModel = null;
    PopularProductsModel popularProductsModel = null;
    ShowAllModel showAllModel = null;

    FirebaseAuth auth;
    private FirebaseFirestore firestore;

    private ViewPager imageSlider;
    private ImageSliderAdapter imageSliderAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detailed);

        toolbar = findViewById(R.id.detailed_toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });

        firestore = FirebaseFirestore.getInstance();
        auth = FirebaseAuth.getInstance();

        imageSlider = findViewById(R.id.image_slider_detailed);

        final Object obj = getIntent().getSerializableExtra("detailed");

        if (obj instanceof NewProductsModel) {
            newProductsModel = (NewProductsModel) obj;
        } else if (obj instanceof PopularProductsModel) {
            popularProductsModel = (PopularProductsModel) obj;
        } else if (obj instanceof ShowAllModel) {
            showAllModel = (ShowAllModel) obj;
        }

        quantity = findViewById(R.id.quantity);
        name = findViewById(R.id.detailed_name);
        rating = findViewById(R.id.rating);
        description = findViewById(R.id.detailed_desc);
        price = findViewById(R.id.detailed_price);

        addToCart = findViewById(R.id.add_to_cart);
        buyNow = findViewById(R.id.buy_now);

        addItems = findViewById(R.id.add_item);
        removeItems = findViewById(R.id.remove_item);

        // New Products
        if (newProductsModel != null) {
//            Glide.with(getApplicationContext()).load(newProductsModel.getImg_url()).into(detailedImg);
            name.setText(newProductsModel.getName());
            rating.setText(newProductsModel.getRating());
            description.setText(newProductsModel.getDescription());
            price.setText(String.valueOf(newProductsModel.getPrice()));
            productPrice = newProductsModel.getPrice();
            totalPrice = newProductsModel.getPrice() * totalQuantity;

            imageSliderAdapter = new ImageSliderAdapter(newProductsModel.getImg_url());
            imageSlider.setAdapter(imageSliderAdapter);
        }

        // Popular Products
        if (popularProductsModel != null) {
//            Glide.with(getApplicationContext()).load(popularProductsModel.getImg_url()).into(detailedImg);
            name.setText(popularProductsModel.getName());
            rating.setText(popularProductsModel.getRating());
            description.setText(popularProductsModel.getDescription());
            price.setText(String.valueOf(popularProductsModel.getPrice()));
            productPrice = popularProductsModel.getPrice();
            totalPrice = popularProductsModel.getPrice() * totalQuantity;

            imageSliderAdapter = new ImageSliderAdapter(popularProductsModel.getImg_url());
            imageSlider.setAdapter(imageSliderAdapter);
        }

        // Show All Products
        if (showAllModel != null) {
//            Glide.with(getApplicationContext()).load(showAllModel.getImg_url()).into(detailedImg);
            name.setText(showAllModel.getName());
            rating.setText(showAllModel.getRating());
            description.setText(showAllModel.getDescription());
            price.setText(String.valueOf(showAllModel.getPrice()));
            productPrice = showAllModel.getPrice();
            totalPrice = showAllModel.getPrice() * totalQuantity;

            imageSliderAdapter = new ImageSliderAdapter(showAllModel.getImg_url());
            imageSlider.setAdapter(imageSliderAdapter);
        }

        // Buy Now
        buyNow.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(DetailedActivity.this, AddressActivity.class);

                if (newProductsModel != null) {
                    intent.putExtra("item", newProductsModel);
                }

                if (popularProductsModel != null) {
                    intent.putExtra("item", popularProductsModel);
                }

                if (showAllModel != null) {
                    intent.putExtra("item", showAllModel);
                }
                intent.putExtra("totalQuantity", totalQuantity);
                startActivity(intent);
            }
        });

        // Add to Cart
        addToCart.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                addToCart();
            }
        });

        addItems.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (totalQuantity < 10) {
                    totalQuantity++;
                    quantity.setText(String.valueOf(totalQuantity));

                    if (newProductsModel != null) {
                        totalPrice = newProductsModel.getPrice() * totalQuantity;
                    } else if (popularProductsModel != null) {
                        totalPrice = popularProductsModel.getPrice() * totalQuantity;
                    } else if (showAllModel != null) {
                        totalPrice = showAllModel.getPrice() * totalQuantity;
                    }

                    price.setText(String.valueOf(totalPrice));
                }
            }
        });

        removeItems.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (totalQuantity > 1) {
                    totalQuantity--;
                    quantity.setText(String.valueOf(totalQuantity));

                    if (newProductsModel != null) {
                        totalPrice = newProductsModel.getPrice() * totalQuantity;
                    } else if (popularProductsModel != null) {
                        totalPrice = popularProductsModel.getPrice() * totalQuantity;
                    } else if (showAllModel != null) {
                        totalPrice = showAllModel.getPrice() * totalQuantity;
                    }

                    price.setText(String.valueOf(totalPrice));
                }
            }
        });
    }

    private void addToCart() {
        String saveCurrentTime, saveCurrentDate;
        Calendar calForDate = Calendar.getInstance();

        SimpleDateFormat currentDate = new SimpleDateFormat("MM dd, yyyy");
        saveCurrentDate = currentDate.format(calForDate.getTime());

        SimpleDateFormat currentTime = new SimpleDateFormat("HH:mm:ss a");
        saveCurrentTime = currentTime.format(calForDate.getTime());

        final HashMap<String, Object> cartMap = new HashMap<>();

        Log.d("PRODUCT PRICE", String.valueOf(productPrice));

        cartMap.put("productName", name.getText().toString());
        cartMap.put("productPrice", String.valueOf(productPrice));
        cartMap.put("currentTime", saveCurrentTime);
        cartMap.put("currentDate", saveCurrentDate);
        cartMap.put("totalQuantity", quantity.getText().toString());
        cartMap.put("totalPrice", totalPrice);

        firestore.collection("AddToCart").document(auth.getCurrentUser().getUid())
                .collection("User").add(cartMap).addOnCompleteListener(new OnCompleteListener<DocumentReference>() {
                    @Override
                    public void onComplete(@NonNull Task<DocumentReference> task) {
                        Toast.makeText(DetailedActivity.this, "Berhasil ditambahkan ke keranjang", Toast.LENGTH_SHORT).show();
                        finish();
                    }
                });
    }
}