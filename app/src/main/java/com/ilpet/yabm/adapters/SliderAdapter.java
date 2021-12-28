package com.ilpet.yabm.adapters;

import android.content.res.TypedArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.ilpet.yabm.R;

public class SliderAdapter extends RecyclerView.Adapter<SliderAdapter.MyViewHolder> {
    private final TypedArray images;
    private final TypedArray info;

    public SliderAdapter(TypedArray imageList, TypedArray infoList) {
        this.images = imageList;
        this.info = infoList;
    }

    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new MyViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.slider_item, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull MyViewHolder holder, int position) {
        holder.view.setBackground(images.getDrawable(position));
        holder.textInfo.setText(info.getText(position));
    }

    @Override
    public int getItemCount() {
        return images.length();
    }

    public static class MyViewHolder extends RecyclerView.ViewHolder {
        View view;
        TextView textInfo;

        public MyViewHolder(@NonNull View itemView) {
            super(itemView);
            view = itemView.findViewById(R.id.slider_view);
            textInfo = itemView.findViewById(R.id.help_info);
        }
    }
}
