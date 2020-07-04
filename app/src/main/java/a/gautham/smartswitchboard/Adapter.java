package a.gautham.smartswitchboard;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.SwitchCompat;
import androidx.recyclerview.widget.RecyclerView;

public class Adapter extends RecyclerView.Adapter<ViewHolder> {
    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.switch_layout, parent);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {

    }

    @Override
    public int getItemCount() {
        return 0;
    }
}

class ViewHolder extends RecyclerView.ViewHolder{

    public TextView switch_name;
    public ImageView switch_img;
    public SwitchCompat switch_bt;

    public ViewHolder(@NonNull View itemView) {
        super(itemView);

        switch_name = itemView.findViewById(R.id.switch_name);
        switch_img = itemView.findViewById(R.id.switch_img);
        switch_bt = itemView.findViewById(R.id.switch_bt);

    }
}