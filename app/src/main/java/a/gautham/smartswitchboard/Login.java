package a.gautham.smartswitchboard;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.util.Patterns;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;

import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.Map;
import java.util.Objects;

import a.gautham.smartswitchboard.databinding.ActivityLoginBinding;
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

        binding.register.setOnClickListener(v -> {
            startActivity(new Intent(getApplicationContext(), Register.class));
        });

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

            FirebaseFirestore.getInstance().collection("Users")
                    .get().addOnCompleteListener(task -> {
                if (task.isSuccessful()) {
                    boolean contains = false;
                    for (QueryDocumentSnapshot document : Objects.requireNonNull(task.getResult())) {
                        Map<String, Object> documentSnapshot = document.getData();
                        if (documentSnapshot.containsValue(getEmail())) {
                            contains = true;
                            System.out.println("HEY HEY HEY HEY HEY HEY Contains");
                            break;
                        }
                    }
                    if (contains) {
                        FirebaseAuth.getInstance().signInWithEmailAndPassword(getEmail(), getPassword())
                                .addOnCompleteListener(task12 -> {
                                    if (task12.isSuccessful()) {
                                        SharedPreferences preferences = getSharedPreferences("User", Context.MODE_PRIVATE);
                                        preferences.edit().putString("uid", Objects.requireNonNull(FirebaseAuth.getInstance().getCurrentUser()).getUid()).apply();
                                        Common.uid = Objects.requireNonNull(FirebaseAuth.getInstance().getCurrentUser()).getUid();
                                        if (dialog.isShowing())
                                            dialog.dismiss();
                                        startActivity(new Intent(getApplicationContext(), MainActivity.class));
                                        finish();
                                    } else {
                                        if (dialog.isShowing())
                                            dialog.dismiss();
                                        Common.toastShort(getApplicationContext(), Objects.requireNonNull(Objects.requireNonNull(task12.getException()).getMessage()), Color.RED, Color.WHITE);
                                        Log.d("Login", Objects.requireNonNull(Objects.requireNonNull(task12.getException()).getMessage()));
                                    }
                                });
                    } else {
                        Common.toastShort(getApplicationContext(), "No User Found", 0, 0);
                        dialog.dismiss();
                    }
                }
            });

        });

        binding.loginPhone.setOnClickListener(view -> {
            PhoneLoginFragment phoneLoginFragment = new PhoneLoginFragment();
            phoneLoginFragment.show(getSupportFragmentManager(), phoneLoginFragment.getTag());
        });

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
                if (getEmail().isEmpty()){
                    binding.signInEmail.setError("Email can't be empty");
                    validateEmail = false;
                }else if (!Patterns.EMAIL_ADDRESS.matcher(getEmail()).matches()){
                    binding.signInEmail.setError("Invalid e-mail");
                    validateEmail = false;
                }else {
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
                if (getPassword().isEmpty()){
                    binding.signInPass.setError("Password can't be empty");
                    validatePass = false;
                }else {
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