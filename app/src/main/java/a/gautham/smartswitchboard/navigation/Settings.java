package a.gautham.smartswitchboard.navigation;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import a.gautham.smartswitchboard.databinding.NavigationSettingsBinding;

public class Settings extends Fragment {

    NavigationSettingsBinding binding;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = NavigationSettingsBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }
}
