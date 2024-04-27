package com.example.sw0b_001.Data.Platforms;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.fragment.app.FragmentTransaction;
import androidx.lifecycle.MutableLiveData;
import androidx.recyclerview.widget.AsyncListDiffer;
import androidx.recyclerview.widget.RecyclerView;

import com.example.sw0b_001.R;
import com.google.android.material.card.MaterialCardView;

import org.jetbrains.annotations.NotNull;

public class PlatformsRecyclerAdapter extends
        RecyclerView.Adapter<PlatformsRecyclerAdapter.ViewHolder> {

    public final AsyncListDiffer<Platforms> mDiffer =
            new AsyncListDiffer(this, Platforms.DIFF_CALLBACK);

    FragmentTransaction fragmentTransaction;

    public MutableLiveData<Integer> savedOnClickListenerLiveData = new MutableLiveData<>();
    public MutableLiveData<Integer> unSavedOnClickListenerLiveData = new MutableLiveData<>();
    public PlatformsRecyclerAdapter(FragmentTransaction fragmentTransaction){
        this.fragmentTransaction = fragmentTransaction;
    }

    @NonNull
    @NotNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull @NotNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        View view = inflater.inflate(R.layout.layout_platforms_thumbnail, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull @NotNull ViewHolder viewHolder, int position) {
        Platforms platforms = mDiffer.getCurrentList().get(position);
        viewHolder.bind(platforms, savedOnClickListenerLiveData, unSavedOnClickListenerLiveData);
    }

    @Override
    public int getItemCount() {
        return mDiffer.getCurrentList().size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {

        ImageView image;

        MaterialCardView cardView;

        public ViewHolder(@NonNull @NotNull View itemView) {
            super(itemView);
            this.cardView = itemView.findViewById(R.id.platforms_thumbnails_card);
            this.image = itemView.findViewById(R.id.platforms_thumbnails);
        }

        public void bind(Platforms platforms,
                         MutableLiveData<Integer> onClickListenerLiveData,
                         MutableLiveData<Integer> unSavedOnClickListenerLiveData) {
            image.setImageDrawable(itemView.getContext()
                    .getDrawable(_PlatformsHandler.hardGetLogoByName(itemView.getContext(),
                    platforms.getName())));

            cardView.setOnClickListener(it -> {
                Log.d(PlatformsRecyclerAdapter.class.getName(), platforms.getType());
                int type = -1;
                if(platforms.getType().equals("email")) { type = Platforms.TYPE_EMAIL; }
                else if(platforms.getType().equals("text")) { type = Platforms.TYPE_TEXT; }

                if (platforms.isSaved())
                    onClickListenerLiveData.setValue(type);
                else
                    unSavedOnClickListenerLiveData.setValue(type);
            });
        }
    }
}
