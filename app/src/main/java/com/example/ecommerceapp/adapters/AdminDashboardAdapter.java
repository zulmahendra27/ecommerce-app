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
import com.example.ecommerceapp.activities.AdminProductActivity;
import com.example.ecommerceapp.activities.DetailedActivity;
import com.example.ecommerceapp.activities.ShowAllActivity;
import com.example.ecommerceapp.models.AdminDashboardModel;
import com.example.ecommerceapp.models.ShowAllModel;

import java.util.List;

public class AdminDashboardAdapter extends RecyclerView.Adapter<AdminDashboardAdapter.ViewHolder> {
    private Context context;
    private List<AdminDashboardModel> list;

    public AdminDashboardAdapter(Context context, List<AdminDashboardModel> list) {
        this.context = context;
        this.list = list;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new ViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.admin_dashboard_item, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull AdminDashboardAdapter.ViewHolder holder, int position) {
        Glide.with(context).load(list.get(position).getImg_url()).into(holder.mCategoryImage);
        holder.mCategoryName.setText(list.get(position).getName());

        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(context, AdminProductActivity.class);
                intent.putExtra("type", list.get(position).getType());
                intent.putExtra("name", list.get(position).getName());
                context.startActivity(intent);
            }
        });
    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        private ImageView mCategoryImage;
        private TextView mCategoryName;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            mCategoryImage = itemView.findViewById(R.id.admin_category_image);
            mCategoryName = itemView.findViewById(R.id.admin_category_name);
        }
    }


}
