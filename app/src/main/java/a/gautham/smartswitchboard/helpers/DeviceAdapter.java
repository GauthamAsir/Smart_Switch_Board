package a.gautham.smartswitchboard.helpers;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.firestore.FirebaseFirestore;

import java.util.List;
import java.util.Map;
import java.util.Objects;

import a.gautham.smartswitchboard.Common;
import a.gautham.smartswitchboard.R;
import a.gautham.smartswitchboard.activity.ManageDevice;
import a.gautham.smartswitchboard.fragments.DeviceDetailFragment;

public class DeviceAdapter extends RecyclerView.Adapter<DeviceAdapter.ViewHolder> {

    List<Map<String, Object>> devices;
    Map<String, Map<String, Object>> map;
    Context context;

    public DeviceAdapter(List<Map<String, Object>> devices, Map<String, Map<String, Object>> map) {
        this.devices = devices;
        this.map = map;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.devices_layout,
                parent, false);
        context = parent.getContext();
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {

        holder.device_name.setText(String.valueOf(devices.get(position).get("device_name")));
        long seconds = (long) devices.get(position).get("last_login");
        holder.last_login.setText(Common.getDate(seconds));

        holder.details.setOnClickListener(view -> {

            DeviceDetailFragment phoneLoginFragment = new DeviceDetailFragment(devices.get(position));
            phoneLoginFragment.show(((ManageDevice) context).getSupportFragmentManager(), phoneLoginFragment.getTag());
        });

        if (Objects.requireNonNull(devices.get(position).get("device_id")).equals(Common.DEVICE_ID)) {
            holder.log_out.setText(R.string.current_device);
            holder.log_out.setEnabled(false);
            return;
        }

        holder.log_out.setOnClickListener(view -> {

            Map<String, Map<String, Object>> devicesList = map;
            Map<String, Object> details = map.get(devices.get(position).get("device_id"));
            if (details != null) {
                details.put("logged_in", false);
            }
            devicesList.put(Objects.requireNonNull(devices.get(position).get("device_id")).toString(), details);

            FirebaseFirestore.getInstance().collection("Users")
                    .document(Common.uid)
                    .update("devices", devicesList);
            notifyDataSetChanged();

        });
    }

    @Override
    public int getItemCount() {
        return devices.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {

        TextView device_name, last_login;
        Button details, log_out;
        CardView rootCard;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            device_name = itemView.findViewById(R.id.device_name);
            last_login = itemView.findViewById(R.id.last_login);
            details = itemView.findViewById(R.id.details);
            log_out = itemView.findViewById(R.id.log_out);
            rootCard = itemView.findViewById(R.id.rootCard);
        }
    }

}
