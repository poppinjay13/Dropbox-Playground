package com.poppinjay13.dropboxplayground.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.poppinjay13.dropboxplayground.R;
import com.poppinjay13.dropboxplayground.entities.Meta;

import java.text.MessageFormat;
import java.util.List;

public class DirectoryAdapter extends RecyclerView.Adapter<DirectoryAdapter.CustomViewHolder> {

    private final List<Meta> data;
    private final Context context;

    public DirectoryAdapter(List<Meta> dataList, Context context) {
        this.data = dataList;
        this.context = context;
    }

    @NonNull
    @Override
    public CustomViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        LayoutInflater layoutInflater = LayoutInflater.from(parent.getContext());
        View view = layoutInflater.inflate(R.layout.file_folder_meta, parent, false);
        return new CustomViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull CustomViewHolder holder, final int position) {
        try {
            final Meta metadata = data.get(position);
            if (metadata.getTag().equals("folder")) {
                holder.type.setImageResource(R.drawable.dropbox_folder);
                holder.name.setText(metadata.getName());
                holder.size.setVisibility(View.GONE);
                holder.modified.setVisibility(View.GONE);
            } else if (metadata.getTag().equals("file")) {
                holder.type.setImageResource(R.drawable.dropbox_file);
                holder.name.setText(metadata.getName());
                holder.size.setText(MessageFormat.format("{0} bytes", metadata.getSize()));
                holder.modified.setText(metadata.getServerModified().substring(0, 10));
            } else {
                Toast.makeText(context, "Oops", Toast.LENGTH_SHORT).show();
            }
            holder.itemView.setOnClickListener(v -> {
                //TODO
                Toast.makeText(context, "To be Implemented", Toast.LENGTH_SHORT).show();
            });
        } catch (Exception ex) {
            Toast.makeText(context, "An error occurred", Toast.LENGTH_SHORT).show();
            ex.printStackTrace();
        }

    }

    @Override
    public int getItemCount() {
        return data.size();
    }

    static class CustomViewHolder extends RecyclerView.ViewHolder {

        ImageView type;
        TextView name, size, modified;

        CustomViewHolder(View v) {
            super(v);
            type = v.findViewById(R.id.type);
            name = v.findViewById(R.id.title);
            size = v.findViewById(R.id.size);
            modified = v.findViewById(R.id.modified);
        }
    }
}
