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

public class ShowAllAdapter extends RecyclerView.Adapter<ShowAllAdapter.ViewHolder> {
    private Context context;
    private List<ShowAllModel> list;
    private boolean isAdmin;

    public ShowAllAdapter(Context context, List<ShowAllModel> list, boolean isAdmin) {
        this.context = context;
        this.list = list;
        this.isAdmin = isAdmin;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new ViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.show_all_item, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
//        Glide.with(context).load(list.get(position).getImg_url()).into(holder.mItemImage);
        ArrayList<String> imgUrls = list.get(position).getImg_url(); // Mengambil List URL gambar
        if (imgUrls != null && !imgUrls.isEmpty()) {
            String firstImageUrl = imgUrls.get(0); // Mengambil data pertama dari List img_url
            Glide.with(context).load(firstImageUrl).into(holder.mItemImage); // Memuat gambar ke ImageView
        }
        holder.mCost.setText("Rp. "+list.get(position).getPrice());
        holder.mName.setText(list.get(position).getName());

        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(context, DetailedActivity.class);
                intent.putExtra("detailed", list.get(position));
                context.startActivity(intent);
            }
        });

        if (isAdmin) {
            holder.adminDeleteProduct.setVisibility(View.VISIBLE);
        } else {
            holder.adminDeleteProduct.setVisibility(View.GONE);
        }
    }

    @Override
    public int getItemCount() {
        return list.size();
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

    public void setAdminMode(boolean isAdmin) {
        if (isAdmin) {
            // Tampilkan adminDeleteProduct
            notifyDataSetChanged();
        } else {
            // Sembunyikan adminDeleteProduct
            notifyDataSetChanged();
        }
    }
}
