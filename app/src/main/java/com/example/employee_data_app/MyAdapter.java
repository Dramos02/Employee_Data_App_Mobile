package com.example.employee_data_app;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;

public class MyAdapter extends RecyclerView.Adapter<MyAdapter.MyViewHolder> {

    Context context;
    ArrayList<User> userArrayList;

    public MyAdapter(Context context, ArrayList<User> userArrayList) {
        this.context = context;
        this.userArrayList = userArrayList;
    }

    @NonNull
    @Override
    public MyAdapter.MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(context).inflate(R.layout.data, parent, false);

        return new MyViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull MyAdapter.MyViewHolder holder, int position) {

        User user = userArrayList.get(position);

        holder.nameTextView.setText(user.fName);
        holder.emailTextView.setText(user.email);
        holder.phoneTextView.setText(user.phone);

    }

    @Override
    public int getItemCount() {
        return userArrayList.size();
    }

    public static class MyViewHolder extends RecyclerView.ViewHolder {
        TextView nameTextView, emailTextView, phoneTextView;

        public MyViewHolder(@NonNull View itemView) {
            super(itemView);

            // Initialize TextViews
            nameTextView = itemView.findViewById(R.id.name_char_id);
            emailTextView = itemView.findViewById(R.id.email_char_id);
            phoneTextView = itemView.findViewById(R.id.phone_char_id);
        }
    }
}
