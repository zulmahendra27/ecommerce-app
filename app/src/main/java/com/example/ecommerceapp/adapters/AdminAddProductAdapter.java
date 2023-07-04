package com.example.ecommerceapp.adapters;

import android.content.Context;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.ecommerceapp.R;

import java.util.ArrayList;

public class AdminAddProductAdapter extends RecyclerView.Adapter<AdminAddProductAdapter.ViewHolder> {
    private final ArrayList<Uri> uriArrayList;
    private final Context context;
    CountOfImagesWhenRemoved countOfImagesWhenRemoved;
    private final ItemClickListener itemClickListener;

    public AdminAddProductAdapter(ArrayList<Uri> uriArrayList, Context context, CountOfImagesWhenRemoved countOfImagesWhenRemoved, ItemClickListener itemClickListener) {
        this.uriArrayList = uriArrayList;
        this.context = context;
        this.countOfImagesWhenRemoved = countOfImagesWhenRemoved;
        this.itemClickListener = itemClickListener;
    }

    @NonNull
    @Override
    public AdminAddProductAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        View view = inflater.inflate(R.layout.custom_single_image, parent, false);

        return new ViewHolder(view, countOfImagesWhenRemoved, itemClickListener);
    }

    @Override
    public void onBindViewHolder(@NonNull AdminAddProductAdapter.ViewHolder holder, int position) {
//        holder.imageView.setImageURI(uriArrayList.get(position));
        Glide.with(context)
                .load(uriArrayList.get(position))
                .into(holder.imageView);

        holder.delete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                uriArrayList.remove(uriArrayList.get(position));
                notifyItemRemoved(position);
                notifyItemRangeChanged(position, getItemCount());
                countOfImagesWhenRemoved.clicked(uriArrayList.size());
            }
        });
    }

    @Override
    public int getItemCount() {
        return uriArrayList.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        ImageView imageView, delete;
        CountOfImagesWhenRemoved countOfImagesWhenRemoved;
        ItemClickListener itemClickListener;
        public ViewHolder(@NonNull View itemView, CountOfImagesWhenRemoved countOfImagesWhenRemoved, ItemClickListener itemClickListener) {
            super(itemView);

            this.countOfImagesWhenRemoved = countOfImagesWhenRemoved;
            this.itemClickListener = itemClickListener;
            imageView = itemView.findViewById(R.id.image);
            delete = itemView.findViewById(R.id.delete);

            itemView.setOnClickListener(this);
        }

        @Override
        public void onClick(View v) {
            if (itemClickListener != null) {
                itemClickListener.itemClick(getAdapterPosition());
            }
        }
    }

    public interface CountOfImagesWhenRemoved {
        void clicked(int getSize);
    }

    public interface ItemClickListener {
        void itemClick(int position);
    }
}
