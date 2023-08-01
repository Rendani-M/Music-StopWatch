package com.musicplayerapp;

import android.database.Cursor;
import android.net.Uri;
import android.provider.MediaStore;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;

public class AudioAdapter extends RecyclerView.Adapter<AudioAdapter.AudioViewHolder>{

    private ArrayList<MusicModel> music;
    private RecyclerViewInterface recyclerViewInterface;

    public AudioAdapter(ArrayList<MusicModel> music, RecyclerViewInterface recyclerViewInterface){
        this.music= music;
        this.recyclerViewInterface= recyclerViewInterface;
    }

    @NonNull
    @Override
    public AudioViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view= LayoutInflater.from(parent.getContext()).inflate(R.layout.music_audioadapter_item,parent,false);
        return new AudioViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull AudioViewHolder holder, int position) {
        holder.title.setText(music.get(position).getTitle());
    }

    @Override
    public int getItemCount() {
        return music.size();
    }

    class AudioViewHolder extends RecyclerView.ViewHolder{
        TextView title;
        public AudioViewHolder(@NonNull View itemView) {
            super(itemView);
            title= itemView.findViewById(R.id.title_audioadapter_item);
            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if(recyclerViewInterface != null){
                        int pos= getAbsoluteAdapterPosition();
                        if(pos != RecyclerView.NO_POSITION){
                            recyclerViewInterface.onItemClick(pos,false);
                        }
                    }
                }
            });
        }
    }
}
