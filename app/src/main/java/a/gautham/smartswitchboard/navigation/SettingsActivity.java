package a.gautham.smartswitchboard.navigation;

import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.os.Bundle;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.preference.EditTextPreference;
import androidx.preference.ListPreference;
import androidx.preference.Preference;
import androidx.preference.PreferenceFragmentCompat;

import com.google.firebase.firestore.FirebaseFirestore;

import a.gautham.smartswitchboard.Common;
import a.gautham.smartswitchboard.DeleteAccountFragment;
import a.gautham.smartswitchboard.R;

public class SettingsActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.settings_activity);
        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.settings, new SettingsFragment())
                .commit();
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
        }
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

                    return true;
                });

            }

            Preference change_pass = findPreference("change_pass");
            if (change_pass != null) {
                change_pass.setOnPreferenceClickListener(preference -> {
                    Common.toastShort(getContext(), "TODO", 0, 0);
                    return true;
                });
            }

            Preference delete_acc = findPreference("delete_account");
            if (delete_acc != null) {
                delete_acc.setOnPreferenceClickListener(preference -> {
                    DeleteAccountFragment deleteAccountFragment = new DeleteAccountFragment();
                    deleteAccountFragment.setCancelable(false);
                    deleteAccountFragment.show(getChildFragmentManager(), deleteAccountFragment.getTag());
                    return true;
                });
            }

        }
    }
}