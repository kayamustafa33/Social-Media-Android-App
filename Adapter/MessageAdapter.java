package com.mustafa.message_app.Adapter;

import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.PopupMenu;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.mustafa.message_app.Models.Message;
import com.mustafa.message_app.R;

import java.util.ArrayList;

public class MessageAdapter extends RecyclerView.Adapter<MessageAdapter.ViewHolder> {

    private ArrayList<Message> messageArrayList;
    String  userID,otherID;
    static Boolean state = false;
    int view_Send=1,view_received=2;
    private DatabaseReference databaseReference;
    private FirebaseDatabase firebaseDatabase;

    public MessageAdapter(ArrayList<Message> messageArrayList, String userID, String otherID) {
        this.messageArrayList = messageArrayList;
        this.userID = userID;
        this.otherID = otherID;
    }

    @NonNull
    @Override
    public MessageAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view;

        if(viewType == view_Send){
            view = LayoutInflater.from(parent.getContext()).inflate(R.layout.chat,parent,false);
            return new ViewHolder(view);
        }else{
            view = LayoutInflater.from(parent.getContext()).inflate(R.layout.other_chat,parent,false);
            return new ViewHolder(view);
        }
    }

    @Override
    public void onBindViewHolder(@NonNull MessageAdapter.ViewHolder holder, int position) {
        holder.textView.setText(messageArrayList.get(position).text);
        holder.time.setText(messageArrayList.get(position).date);

        firebaseDatabase = FirebaseDatabase.getInstance();
        databaseReference = firebaseDatabase.getReference();

        holder.itemView.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                PopupMenu popupMenu = new PopupMenu(holder.itemView.getContext(),v);
                popupMenu.getMenuInflater().inflate(R.menu.long_click_menu,popupMenu.getMenu());
                popupMenu.show();

                popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                    @Override
                    public boolean onMenuItemClick(MenuItem item) {
                        switch (item.getItemId()){
                            case R.id.deleteMessage:
                                Toast.makeText(v.getContext(), "Yapım aşamasında.", Toast.LENGTH_SHORT).show();
                                break;
                        }
                        return true;
                    }
                });

                return true;
            }
        });
    }

    @Override
    public int getItemCount() {
        return messageArrayList.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        TextView textView;
        TextView time;
        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            if(state){
                textView = itemView.findViewById(R.id.userTextView);
                time = itemView.findViewById(R.id.date);
            }else{
                textView = itemView.findViewById(R.id.otherTextView);
                time = itemView.findViewById(R.id.date2);
            }


        }
    }

    @Override
    public int getItemViewType(int position) {
        if(messageArrayList.get(position).fromUID.equals(otherID)){
            state = true;
            return view_Send;
        }else{
            state = false;
            return view_received;
        }
    }

    public void filteredList(ArrayList<Message> filteredList){
        messageArrayList = filteredList;
        notifyDataSetChanged();
    }
}
