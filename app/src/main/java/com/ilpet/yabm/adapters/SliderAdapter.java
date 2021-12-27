package com.ilpet.yabm.adapters;

import android.content.res.TypedArray;
import android.graphics.drawable.Drawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.ilpet.yabm.R;

import java.util.ArrayList;

public class SliderAdapter extends RecyclerView.Adapter<SliderAdapter.MyViewHolder> {
    TypedArray list;

    public SliderAdapter(TypedArray list) {
        this.list = list;
    }

    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new MyViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.slider_item, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull MyViewHolder holder, int position) {
        holder.view.setBackground(list.getDrawable(position));
    }

    @Override
    public int getItemCount() {
        return list.length();
    }

    public static class MyViewHolder extends RecyclerView.ViewHolder {
        View view;

        public MyViewHolder(@NonNull View itemView) {
            super(itemView);
            view = itemView.findViewById(R.id.slider_view);
        }
    }
}
