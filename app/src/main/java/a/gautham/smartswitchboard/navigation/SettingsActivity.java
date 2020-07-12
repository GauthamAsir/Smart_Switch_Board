package a.gautham.smartswitchboard.navigation;

import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.TypedValue;
import android.view.Window;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.appcompat.widget.Toolbar;
import androidx.preference.EditTextPreference;
import androidx.preference.ListPreference;
import androidx.preference.Preference;
import androidx.preference.PreferenceFragmentCompat;

import com.google.firebase.firestore.FirebaseFirestore;

import a.gautham.smartswitchboard.ChangePasswordFragment;
import a.gautham.smartswitchboard.Common;
import a.gautham.smartswitchboard.DeleteAccountFragment;
import a.gautham.smartswitchboard.R;
import a.gautham.smartswitchboard.ReportBug;

public class SettingsActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.settings_activity);
        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.settings, new SettingsFragment())
                .commit();
        Toolbar toolBar = findViewById(R.id.toolbar);
        toolBar.setTitle(R.string.menu_settings);
        toolBar.setNavigationOnClickListener(view -> onBackPressed());

        Window window = getWindow();
        TypedValue typedValue = new TypedValue();
        getApplicationContext().getTheme()
                .resolveAttribute(android.R.attr.textColorPrimary, typedValue, true);
        window.setStatusBarColor(typedValue.data);

    }

    public static class SettingsFragment extends PreferenceFragmentCompat {
        @SuppressLint("SwitchIntDef")
        @Override
        public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
            setPreferencesFromResource(R.xml.settings, rootKey);

            EditTextPreference namePref = findPreference("name");
            if (namePref != null) {
                namePref.setText(Common.NAME);

                namePref.setOnPreferenceChangeListener((preference, newValue) -> {
                    if (!newValue.equals(Common.NAME)) {
                        ProgressDialog dialog = new ProgressDialog(getContext());
                        dialog.setMessage("Updating Name");
                        dialog.show();

                        FirebaseFirestore.getInstance().collection("Users")
                                .document(Common.uid).update("name", newValue.toString())
                                .addOnCompleteListener(task -> {
                                    if (task.isSuccessful()) {
                                        Common.NAME = newValue.toString();
                                        dialog.dismiss();
                                        Common.toastShort(getContext(), "Name updated", 0, 0);
                                        return;
                                    }
                                    dialog.dismiss();
                                    Common.toastShort(getContext(), "Failed to update name", 0, 0);
                                });
                    }
                    return true;
                });
            }

            ListPreference themes = findPreference("themes");
            if (themes != null) {

                int mode = AppCompatDelegate.getDefaultNightMode();

                switch (mode) {
                    case AppCompatDelegate.MODE_NIGHT_NO:
                        themes.setValue("light");
                        break;
                    case AppCompatDelegate.MODE_NIGHT_YES:
                        themes.setValue("dark");
                        break;
                    case AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM:
                    default:
                        themes.setValue("follow_system");
                        break;
                }

                themes.setOnPreferenceChangeListener((preference, newValue) -> {

                    switch (newValue.toString()) {
                        case "light":
                            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
                            break;
                        case "dark":
                            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
                            break;
                        case "follow_system":
                        default:
                            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM);
                            break;
                    }

                    SharedPreferences preferences = requireActivity()
                            .getSharedPreferences("User", Context.MODE_PRIVATE);
                    preferences.edit().putInt("theme", AppCompatDelegate.getDefaultNightMode()).apply();

                    return true;
                });

            }

            Preference change_pass = findPreference("change_pass");
            if (change_pass != null) {
                change_pass.setOnPreferenceClickListener(preference -> {
                    ChangePasswordFragment changePasswordFragment = new ChangePasswordFragment();
                    changePasswordFragment.show(
                            requireActivity().getSupportFragmentManager(), changePasswordFragment.getTag());
                    return true;
                });
            }

            Preference delete_acc = findPreference("delete_account");
            if (delete_acc != null) {
                delete_acc.setOnPreferenceClickListener(preference -> {
                    DeleteAccountFragment deleteAccountFragment = new DeleteAccountFragment();
                    deleteAccountFragment.show(getChildFragmentManager(), deleteAccountFragment.getTag());
                    return true;
                });
            }

            Preference report_bug = findPreference("report_bug");
            if (report_bug != null) {
                report_bug.setOnPreferenceClickListener(preference -> {
                    requireActivity().startActivity(new Intent(requireActivity(), ReportBug.class));
                    return true;
                });
            }

        }
    }
}