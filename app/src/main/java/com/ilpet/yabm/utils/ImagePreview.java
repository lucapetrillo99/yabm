package com.ilpet.yabm.utils;

import android.app.Dialog;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.res.ResourcesCompat;
import androidx.fragment.app.DialogFragment;

import com.ilpet.yabm.R;
import com.squareup.picasso.Picasso;

public class ImagePreview extends DialogFragment {
    private static String image;
    private static String title;
    private boolean tap = true;

    public static ImagePreview newInstance(String imageToLoad, String imageTitle) {
        image = imageToLoad;
        title = imageTitle;
        return new ImagePreview();
    }

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setStyle(DialogFragment.STYLE_NO_FRAME, R.style.DialogAnimation);
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.image_preview, container, false);
        Toolbar toolbar = view.findViewById(R.id.toolbar);
        ImageView imageView = view.findViewById(R.id.preview_image);
        TextView toolbarTitle = view.findViewById(R.id.toolbar_title);
        toolbar.setBackgroundColor(Color.TRANSPARENT);
        toolbarTitle.setText(title);
        toolbarTitle.setTextColor(Color.WHITE);

        Drawable drawable = ResourcesCompat.getDrawable(getResources(),
                R.drawable.ic_back_button, null);
        assert drawable != null;
        drawable.setTint(Color.WHITE);
        toolbar.setNavigationIcon(drawable);

        Picasso.get().load(image)
                .fit()
                .centerInside()
                .into(imageView);

        toolbar.setNavigationOnClickListener(v -> dismiss());

        imageView.setOnClickListener(view1 -> {
            if (!tap) {
                toolbar.setVisibility(View.VISIBLE);
                tap = true;
            } else {
                toolbar.setVisibility(View.INVISIBLE);
                tap = false;
            }
        });
        return view;
    }

    @Override
    public void onStart() {
        super.onStart();
        Dialog dialog = getDialog();
        if (dialog != null) {
            int width = ViewGroup.LayoutParams.MATCH_PARENT;
            int height = ViewGroup.LayoutParams.MATCH_PARENT;
            dialog.getWindow().setLayout(width, height);
        }
    }
}
