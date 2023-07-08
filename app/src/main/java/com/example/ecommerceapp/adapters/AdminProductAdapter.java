package com.example.ecommerceapp.adapters;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.ecommerceapp.R;
import com.example.ecommerceapp.activities.DetailedActivity;
import com.example.ecommerceapp.models.ShowAllModel;

import java.util.ArrayList;
import java.util.List;

public class AdminProductAdapter extends RecyclerView.Adapter<AdminProductAdapter.ViewHolder> {
    private Context context;
    private List<ShowAllModel> list;
    private ProductDeleteListener deleteListener;

    public AdminProductAdapter(Context context, List<ShowAllModel> list, ProductDeleteListener deleteListener) {
        this.context = context;
        this.list = list;
        this.deleteListener = deleteListener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new ViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.show_all_item, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull AdminProductAdapter.ViewHolder holder, int position) {
//        Glide.with(context).load(list.get(position).getImg_url()).into(holder.mItemImage);
        ArrayList<String> imgUrls = list.get(position).getImg_url(); // Mengambil List URL gambar
        if (imgUrls != null && !imgUrls.isEmpty()) {
            String firstImageUrl = imgUrls.get(0); // Mengambil data pertama dari List img_url
            Glide.with(context).load(firstImageUrl).into(holder.mItemImage); // Memuat gambar ke ImageView
        }
        holder.mCost.setText("Rp. "+list.get(position).getPrice());
        holder.mName.setText(list.get(position).getName());

        holder.adminDeleteProduct.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                deleteListener.deleteProduct(list.get(position));
            }
        });
    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    public long getItemId(int position) {
        return position; // Menggunakan posisi sebagai ID item
    }

    public interface ProductDeleteListener {
        void deleteProduct(ShowAllModel product);
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        private ImageView mItemImage, adminDeleteProduct;
        private TextView mCost;
        private TextView mName;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            mItemImage = itemView.findViewById(R.id.item_image);
            mCost = itemView.findViewById(R.id.item_cost);
            mName = itemView.findViewById(R.id.item_nam);
            adminDeleteProduct = itemView.findViewById(R.id.admin_delete_product);
        }
    }
}
