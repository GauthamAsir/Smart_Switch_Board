package a.gautham.smartswitchboard.activity;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.ContextCompat;
import androidx.preference.EditTextPreference;
import androidx.preference.ListPreference;
import androidx.preference.Preference;
import androidx.preference.PreferenceFragmentCompat;
import androidx.preference.PreferenceScreen;

import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.gson.Gson;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Objects;

import a.gautham.smartswitchboard.Common;
import a.gautham.smartswitchboard.R;
import a.gautham.smartswitchboard.fragments.ChangePasswordFragment;
import a.gautham.smartswitchboard.fragments.DeleteAccountFragment;

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

    }

    @Override
    protected void onResume() {
        super.onResume();
        Common.SETTINGS_ENABLED = Common.getConnectivityStatus(getApplicationContext());
    }

    public static class SettingsFragment extends PreferenceFragmentCompat {
        @SuppressLint("SwitchIntDef")
        @Override
        public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
            setPreferencesFromResource(R.xml.settings, rootKey);

            PreferenceScreen root = findPreference("root");
            if (root != null) {
                if (Common.SETTINGS_ENABLED) {
                    root.setEnabled(true);
                } else {
                    Common.toastShort(requireActivity(), "Connect to internet to use this features", 0, 0);
                    root.setEnabled(false);
                }
            }

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
                    ChangePasswordFragment changePasswordFragment = new ChangePasswordFragment(false);
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

            Preference sync = findPreference("sync");
            if (sync != null) {
                sync.setOnPreferenceClickListener(preference -> {

                    AlertDialog.Builder builder = new AlertDialog.Builder(requireActivity());
                    builder.setTitle(R.string.sync);
                    builder.setMessage(R.string.sync_summary);
                    builder.setPositiveButton(R.string.yes, (dialogInterface, i) -> {
                        syncSSBList(Common.uid);
                        dialogInterface.dismiss();
                    });
                    builder.setNegativeButton(R.string.cancel, (dialogInterface, i) -> {
                    });
                    AlertDialog alertDialog = builder.create();
                    alertDialog.show();
                    alertDialog.getButton(AlertDialog.BUTTON_POSITIVE).setTextColor(
                            ContextCompat.getColor(requireActivity(), R.color.colorAccent)
                    );
                    alertDialog.getButton(AlertDialog.BUTTON_NEGATIVE).setTextColor(
                            ContextCompat.getColor(requireActivity(), R.color.colorAccent)
                    );
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

            Preference contact_support = findPreference("contact_support");
            if (contact_support != null) {
                contact_support.setOnPreferenceClickListener(preference -> {
                    BottomSheetDialog dialog = new BottomSheetDialog(requireActivity());
                    @SuppressLint("InflateParams") View view = LayoutInflater.from(requireActivity())
                            .inflate(R.layout.contact_support, null, false);
                    dialog.setContentView(view);

                    LinearLayout email = view.findViewById(R.id.email_container);
                    LinearLayout phone = view.findViewById(R.id.phone_container);

                    email.setOnClickListener(view1 -> {
                        Intent intent = new Intent(android.content.Intent.ACTION_SENDTO,
                                Uri.fromParts("mailto", getString(R.string.support_mail), null));
                        intent.putExtra(Intent.EXTRA_SUBJECT, "Smart Switch Board Support");
                        startActivity(Intent.createChooser(intent, "Send email..."));
                        dialog.dismiss();
                    });

                    phone.setOnClickListener(view1 -> {
                        Intent intent = new Intent(Intent.ACTION_DIAL);
                        intent.setData(Uri.parse("tel:+91" + getString(R.string.support_phone)));
                        startActivity(Intent.createChooser(intent, "Call via"));
                        dialog.dismiss();
                    });

                    dialog.show();
                    return true;
                });
            }
        }

        private void syncSSBList(String uid) {

            DocumentReference docRef = FirebaseFirestore.getInstance().collection("Users")
                    .document(uid);

            docRef.get().addOnCompleteListener(task -> {

                DocumentSnapshot document = task.getResult();

                if (task.isSuccessful() && document != null) {

                    if (document.get("deleted_ssb") != null)
                        convertData(document, "deleted_ssb");

                    if (document.get("ssb_list") != null)
                        convertData(document, "ssb_list");

                    AlertDialog.Builder successBuilder = new AlertDialog.Builder(requireActivity());
                    successBuilder.setTitle(R.string.sync);
                    successBuilder.setMessage(R.string.sync_successful);

                    successBuilder.setPositiveButton(R.string.ok, (dialogInterface, i) -> dialogInterface.dismiss());

                    AlertDialog successDialog = successBuilder.create();
                    successDialog.show();
                    successDialog.getButton(AlertDialog.BUTTON_POSITIVE).setTextColor(
                            ContextCompat.getColor(requireActivity(), R.color.colorAccent)
                    );
                } else {
                    Common.toastLong(requireActivity(), getString(R.string.unable_to_sync_old_data), 0, 0);
                }
            });
        }

        private void convertData(DocumentSnapshot document, String k) {

            String key = k.equals("ssb_list") ? "fire_db_temp" : "deleted_ssbs";

            @SuppressWarnings("unchecked")
            ArrayList<String> arrayList = (ArrayList<String>) Objects.requireNonNull(document.get(k));
            ArrayList<ArrayList<String>> ssbList = new ArrayList<>();
            if (arrayList.size() > 0) {
                for (String s : arrayList) {
                    ArrayList<String> myList = new ArrayList<>(Arrays.asList(s.split(",")));
                    ssbList.add(myList);
                }
            }
            saveArrayList(ssbList, key);
        }

        public void saveArrayList(ArrayList<ArrayList<String>> list, String key) {
            SharedPreferences prefs = requireActivity().getSharedPreferences("DB_temp", MODE_PRIVATE);
            SharedPreferences.Editor editor = prefs.edit();
            Gson gson = new Gson();
            String json = gson.toJson(list);
            editor.putString("fire_db_temp", json);
            editor.apply();
        }

    }

}