package a.gautham.smartswitchboard.fragments;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.android.material.bottomsheet.BottomSheetDialogFragment;

import java.util.Map;
import java.util.Objects;

import a.gautham.smartswitchboard.Common;
import a.gautham.smartswitchboard.databinding.DeviceDetailsFragmentBinding;
import a.gautham.smartswitchboard.helpers.GeoResponse;
import a.gautham.smartswitchboard.services.LocationApiService;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class DeviceDetailFragment extends BottomSheetDialogFragment {

    private static final String BASE_URL = "http://ip-api.com/";
    Map<String, Object> map;

    public DeviceDetailFragment(Map<String, Object> map) {
        this.map = map;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        DeviceDetailsFragmentBinding binding = DeviceDetailsFragmentBinding.inflate(
                inflater, container, false);

        LocationApiService apiService = getGeoApiService();
        apiService.getLocation().enqueue(new Callback<GeoResponse>() {
            @Override
            public void onResponse(@NonNull Call<GeoResponse> call, @NonNull Response<GeoResponse> response) {
                if (response.isSuccessful() && response.code() == 200 && response.body() != null) {
                    binding.deviceLocation.setText(String.format("%s, %s", response.body().getCity(), response.body().getRegionName()));
                }
            }

            @Override
            public void onFailure(@NonNull Call<GeoResponse> call, @NonNull Throwable t) {
                Common.toastShort(requireActivity(), "Failed to get location", 0, 0);
                Log.e("Get IP Location: ", Objects.requireNonNull(t.getMessage()));
            }
        });

        binding.deviceId.setText(String.valueOf(map.get("device_id")));
        binding.deviceName.setText(String.valueOf(map.get("device_name")));
        binding.deviceSdkVersion.setText(String.valueOf(map.get("sdk_version")));
        binding.deviceIp.setText(String.valueOf(map.get("ip")));

        long seconds = (long) map.get("last_login");
        binding.lastLogin.setText(Common.getDate(seconds));

        return binding.getRoot();
    }

    private LocationApiService getGeoApiService() {
        return new Retrofit.Builder()
                .baseUrl(BASE_URL)
                .addConverterFactory(GsonConverterFactory.create())
                .build()
                .create(LocationApiService.class);
    }

}
