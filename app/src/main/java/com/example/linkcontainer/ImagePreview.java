package com.example.linkcontainer;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.DialogFragment;
import com.squareup.picasso.Picasso;

public class ImagePreview extends DialogFragment{
    private static String image;
    private static String title;

    static ImagePreview newInstance(String imageToLoad, String imageTitle) {
        image = imageToLoad;
        title = imageTitle;
        return new ImagePreview();
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setStyle(DialogFragment.STYLE_NO_FRAME, DialogFragment.STYLE_NO_FRAME);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.preview_image, container, false);
        Toolbar toolbar = view.findViewById(R.id.toolbar);
        ImageView imageView = view.findViewById(R.id.preview_image);
        TextView toolbarTitle = view.findViewById(R.id.toolbar_title);
        toolbarTitle.setText(title);

        toolbar.setNavigationIcon(R.drawable.ic_back_button);
        Picasso.get().load(image)
                .fit()
                .centerInside()
                .into(imageView);

        toolbar.setNavigationOnClickListener(v -> dismiss());
        return view;
    }
}
