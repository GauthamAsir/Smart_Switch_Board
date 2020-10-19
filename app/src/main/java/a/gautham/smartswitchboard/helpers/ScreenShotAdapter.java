package a.gautham.smartswitchboard.helpers;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

import a.gautham.smartswitchboard.R;

public class ScreenShotAdapter extends RecyclerView.Adapter<ScreenShotAdapter.ViewHolder> {

    List<String> imgs;

    public ScreenShotAdapter(List<String> imgs) {
        this.imgs = imgs;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_screenshot_image,
                parent, false);
        return new ScreenShotAdapter.ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        //
    }

    @Override
    public int getItemCount() {
        return imgs.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {

        ImageView imageView;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            imageView = itemView.findViewById(R.id.img_sc);
        }
    }

}
