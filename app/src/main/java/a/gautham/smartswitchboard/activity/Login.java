package a.gautham.smartswitchboard.activity;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.provider.Settings;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.util.Patterns;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.core.content.ContextCompat;

import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import a.gautham.smartswitchboard.Common;
import a.gautham.smartswitchboard.R;
import a.gautham.smartswitchboard.databinding.ActivityLoginBinding;
import a.gautham.smartswitchboard.fragments.ChangePasswordFragment;
import a.gautham.smartswitchboard.fragments.PhoneLoginFragment;
import dmax.dialog.SpotsDialog;

public class Login extends AppCompatActivity {

    private ActivityLoginBinding binding;
    private boolean validateEmail = false, validatePass = false;
    private android.app.AlertDialog dialog;

    private long back_pressed;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityLoginBinding.inflate(getLayoutInflater());
        View view = binding.getRoot();
        setContentView(view);

        dialog = new SpotsDialog.Builder()
                .setContext(this)
                .setCancelable(false)
                .setMessage("Logging You In ...")
                .setTheme(R.style.DialogCustom)
                .build();

        SharedPreferences preferences = getSharedPreferences("User", Context.MODE_PRIVATE);
        preferences.edit().putInt("theme", AppCompatDelegate.getDefaultNightMode()).apply();

        binding.register.setOnClickListener(v -> startActivity(new Intent(getApplicationContext(), Register.class)));

    }

    @Override
    protected void onStart() {
        super.onStart();

        textWatchers();

        binding.loginBt.setOnClickListener(view -> {
            if (!validateEmail) {
                Common.toastShort(getApplicationContext(), "Enter valid email/phone number", Color.RED, Color.WHITE);
                return;
            }

            if (!validatePass) {
                Common.toastShort(getApplicationContext(), "Enter valid password", Color.RED, Color.WHITE);
                return;
            }

            dialog.show();

            try {
                FirebaseAuth.getInstance().signInWithEmailAndPassword(getEmail(), getPassword())
                        .addOnCompleteListener(task12 -> {
                            if (task12.isSuccessful()) {
                                SharedPreferences preferences = getSharedPreferences("User", Context.MODE_PRIVATE);
                                preferences.edit().putString("uid", Objects.requireNonNull(FirebaseAuth.getInstance().getCurrentUser()).getUid()).apply();
                                preferences.edit().putString("password", getPassword()).apply();
                                preferences.edit().putBoolean("sync", true).apply();

                                Common.toastShort(getApplicationContext(), "Login successful", ContextCompat.getColor(
                                        getApplicationContext(), R.color.dark_green
                                ), Color.BLACK);
                                Common.uid = Objects.requireNonNull(FirebaseAuth.getInstance().getCurrentUser()).getUid();
                                if (dialog.isShowing())
                                    dialog.dismiss();
                                FirebaseFirestore.getInstance().collection("Users")
                                        .document(Objects.requireNonNull(FirebaseAuth.getInstance().getCurrentUser()).getUid())
                                        .get().addOnCompleteListener(task1 -> {
                                    if (task1.isSuccessful()) {
                                        @SuppressWarnings("unchecked")
                                        Map<String, Map<String, Object>> devicesList = (Map<String, Map<String, Object>>) Objects.requireNonNull(task1.getResult()).get("devices");

                                        if (devicesList == null)
                                            devicesList = new HashMap<>();
                                        else if (devicesList.containsValue(Common.DEVICE_ID))
                                            devicesList.remove(Common.DEVICE_ID);

                                        devicesList.put(Common.DEVICE_ID, new Common().getDeviceDetails(getApplicationContext(), true));
                                        FirebaseFirestore.getInstance().collection("Users")
                                                .document(Objects.requireNonNull(FirebaseAuth.getInstance().getCurrentUser()).getUid())
                                                .update("devices", devicesList);

                                    }
                                });
                                startActivity(new Intent(getApplicationContext(), MainActivity.class));
                                finish();
                            } else {
                                if (dialog.isShowing())
                                    dialog.dismiss();
                                Common.toastShort(getApplicationContext(), Objects.requireNonNull(Objects.requireNonNull(task12.getException()).getMessage()), Color.RED, Color.WHITE);
                                Log.d("Login", Objects.requireNonNull(Objects.requireNonNull(task12.getException()).getMessage()));
                            }
                        });
            } catch (Exception e) {
                if (dialog.isShowing())
                    dialog.dismiss();
                Common.toastShort(getApplicationContext(), Objects.requireNonNull(Objects.requireNonNull(e.getMessage())), Color.RED, Color.WHITE);
                Log.d("Login", Objects.requireNonNull(Objects.requireNonNull(e.getMessage())));
            }

        });

        binding.loginPhone.setOnClickListener(view -> {
            PhoneLoginFragment phoneLoginFragment = new PhoneLoginFragment();
            phoneLoginFragment.show(getSupportFragmentManager(), phoneLoginFragment.getTag());
        });

        binding.forgotPass.setOnClickListener(view -> {
            ChangePasswordFragment changePasswordFragment = new ChangePasswordFragment(true);
            changePasswordFragment.show(getSupportFragmentManager(), changePasswordFragment.getTag());
        });

    }

    @SuppressLint("HardwareIds")
    @Override
    protected void onResume() {
        super.onResume();
        Common.DEVICE_ID = Settings.Secure.getString(getApplicationContext().getContentResolver(), Settings.Secure.ANDROID_ID);
    }

    private void textWatchers() {

        Objects.requireNonNull(binding.signInEmail.getEditText()).addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void afterTextChanged(Editable editable) {
                if (getEmail().isEmpty()) {
                    binding.signInEmail.setError("Email can't be empty");
                    validateEmail = false;
                } else if (!Patterns.EMAIL_ADDRESS.matcher(getEmail()).matches()) {
                    binding.signInEmail.setError("Invalid e-mail");
                    validateEmail = false;
                } else {
                    binding.signInEmail.setError(null);
                    validateEmail = true;
                }
            }
        });

        Objects.requireNonNull(binding.signInPass.getEditText()).addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void afterTextChanged(Editable editable) {
                if (getPassword().isEmpty()) {
                    binding.signInPass.setError("Password can't be empty");
                    validatePass = false;
                } else {
                    binding.signInPass.setError(null);
                    validatePass = true;
                }
            }
        });

    }

    private String getEmail() {
        return Objects.requireNonNull(binding.signInEmail.getEditText()).getText().toString();
    }

    private String getPassword() {
        return Objects.requireNonNull(binding.signInPass.getEditText()).getText().toString();
    }

    @Override
    public void onBackPressed() {

        //SnackBar
        Snackbar snackbar = Snackbar.make(binding.root, "Press Again to Exit", Snackbar.LENGTH_SHORT);

        if (back_pressed + 2000 > System.currentTimeMillis()) {
            finish();
            moveTaskToBack(true);
            System.exit(1);
            android.os.Process.killProcess(android.os.Process.myPid());
            finish();
        } else {
            snackbar.setText("Press Again to Exit");
            snackbar.show();
            back_pressed = System.currentTimeMillis();
        }
    }
}