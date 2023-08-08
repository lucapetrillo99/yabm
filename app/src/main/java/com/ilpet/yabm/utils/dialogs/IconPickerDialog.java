package com.ilpet.yabm.utils.dialogs;

import android.app.Dialog;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.DialogFragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.ilpet.yabm.R;
import com.ilpet.yabm.activities.CategoriesActivity;
import com.ilpet.yabm.adapters.IconAdapter;

public class IconPickerDialog extends DialogFragment {
    private OnIconSelectedListener listener;
    private final CategoriesActivity activity;

    public interface OnIconSelectedListener {
        void onIconSelected(Drawable icon);
    }

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setStyle(DialogFragment.STYLE_NO_FRAME, R.style.DialogAnimation);
    }

    public IconPickerDialog(CategoriesActivity activity) {
        this.activity = activity;
    }

    public void setOnIconSelectedListener(OnIconSelectedListener listener) {
        this.listener = listener;
    }

    @NonNull
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.icon_picker_dialog, container, false);
        Toolbar toolbar = view.findViewById(R.id.toolbar);
        TextView toolbarTitle = view.findViewById(R.id.toolbar_title);

        toolbar.setNavigationIcon(R.drawable.ic_back_button);
        toolbar.setNavigationOnClickListener(v -> dismiss());
        toolbarTitle.setText(getString(R.string.choose_icon_title));

        RecyclerView iconRecyclerView = view.findViewById(R.id.icon_recycler_view);
        iconRecyclerView.setLayoutManager(new GridLayoutManager(requireContext(), 6));

        IconAdapter iconAdapter = new IconAdapter(activity);
        iconRecyclerView.setAdapter(iconAdapter);

        iconAdapter.setOnItemClickListener(icon -> {
            if (listener != null) {
                listener.onIconSelected(icon);
                dismiss();
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
