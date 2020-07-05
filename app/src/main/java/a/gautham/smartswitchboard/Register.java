package a.gautham.smartswitchboard;

import android.content.Intent;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.text.Editable;
import android.text.InputType;
import android.text.TextWatcher;
import android.util.Log;
import android.util.Patterns;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.EditText;
import android.widget.LinearLayout;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.google.android.gms.tasks.TaskExecutors;
import com.google.firebase.FirebaseException;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.PhoneAuthCredential;
import com.google.firebase.auth.PhoneAuthProvider;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.ListenerRegistration;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

import a.gautham.smartswitchboard.databinding.ActivityRegisterBinding;
import a.gautham.smartswitchboard.models.CurrentDevice;
import dmax.dialog.SpotsDialog;

public class Register extends AppCompatActivity {

    private ActivityRegisterBinding binding;
    private int progress = 0;
    private boolean validateName = false, validateEmail = false, validatePass = false, validateRePass = false,
            validateNum = false;

    private Animation slideIn, slideOut, backSlideIn, backSlideOut;

    private FirebaseAuth auth;
    private FirebaseFirestore firestore;
    private String verificationId;

    private AlertDialog alertDialog1;
    private android.app.AlertDialog dialog, vdialog, requestOpt;

    private ListenerRegistration registration;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityRegisterBinding.inflate(getLayoutInflater());
        View view = binding.getRoot();
        setContentView(view);

        slideIn = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.slide_in_right);
        slideOut = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.slide_out_left);

        backSlideIn = AnimationUtils.loadAnimation(getApplicationContext(),R.anim.slide_in_left);
        backSlideOut = AnimationUtils.loadAnimation(getApplicationContext(),R.anim.slide_out_right);

        auth = FirebaseAuth.getInstance();
        firestore = FirebaseFirestore.getInstance();

        dialog = new SpotsDialog.Builder()
                .setContext(this)
                .setCancelable(false)
                .setMessage("Registering You In ...")
                .setTheme(R.style.DialogCustom)
                .build();

        requestOpt = new SpotsDialog.Builder()
                .setContext(this)
                .setCancelable(false)
                .setMessage("Requesting Otp")
                .setTheme(R.style.DialogCustom)
                .build();

        vdialog = new SpotsDialog.Builder()
                .setContext(this)
                .setCancelable(false)
                .setMessage("Verifying Code ...")
                .setTheme(R.style.DialogCustom)
                .build();

    }

    @Override
    protected void onStart() {
        super.onStart();
        textWatchers();

        continue_listeners();
        back_listeners();

    }

    private void continue_listeners() {

        binding.continueName.setOnClickListener(view -> {
            if (validateName){
                out(binding.nameContainer);
                in(binding.emailContainer);
                binding.status1.setVisibility(View.VISIBLE);
                Objects.requireNonNull(binding.registerEmail.getEditText()).requestFocus();
                return;
            }

            Common.toastShort(getApplicationContext(), "Name validation failed", Color.RED,Color.WHITE);

        });

        binding.continueEmail.setOnClickListener(view -> {
            if (validateEmail){
                out(binding.emailContainer);
                in(binding.passwordContainer);
                binding.status2.setVisibility(View.VISIBLE);
                Objects.requireNonNull(binding.registerPass.getEditText()).requestFocus();
                return;
            }

            Common.toastShort(getApplicationContext(), "E-mail validation failed", Color.RED,Color.WHITE);

        });

        binding.continuePass.setOnClickListener(view -> {
            if (validatePass){
                out(binding.passwordContainer);
                in(binding.repasswordContainer);
                Objects.requireNonNull(binding.registerConfirmPass.getEditText()).requestFocus();
                return;
            }

            Common.toastShort(getApplicationContext(), "Password validation failed", Color.RED,Color.WHITE);

        });

        binding.continueRepass.setOnClickListener(view -> {
            if (validateRePass){
                out(binding.repasswordContainer);
                in(binding.phoneContainer);
                binding.status3.setVisibility(View.VISIBLE);
                Objects.requireNonNull(binding.registerNumber.getEditText()).requestFocus();
                return;
            }

            Common.toastShort(getApplicationContext(), "Confirm Password validation failed", Color.RED,Color.WHITE);

        });

        binding.continuePhone.setOnClickListener(view -> {
            if (validateNum){
                binding.status4.setVisibility(View.VISIBLE);

                requestOpt.show();

                DocumentReference docRef = firestore.collection("Phones").document(getPhoneNumber());

                if (!Common.checkInternet(getApplicationContext())){
                    Common.toastShort(getApplicationContext(), "No Internet Connection", Color.RED,Color.WHITE);
                    return;
                }

                registration = docRef.addSnapshotListener((documentSnapshot, e) -> {
                    if (documentSnapshot != null) {
                        if (documentSnapshot.exists()){
                            requestOpt.dismiss();
                            Common.toastShort(getApplicationContext(), "Phone number exists", Color.RED,Color.WHITE);
                            return;
                        }

                        String phoneNumber = "+" + "91" + getPhoneNumber();
                        sendVerificationCode(phoneNumber);
                        return;

                    }
                    String phoneNumber = "+" + "91" + getPhoneNumber();
                    sendVerificationCode(phoneNumber);

                });

                return;
            }

            Common.toastShort(getApplicationContext(), "Confirm Password validation failed", Color.RED,Color.WHITE);

        });

    }

    private void back_listeners() {

        binding.backEmail.setOnClickListener(view -> {
            back_out(binding.emailContainer);
            back_in(binding.nameContainer);
            binding.status1.setVisibility(View.INVISIBLE);
        });

        binding.backPass.setOnClickListener(view -> {
            back_out(binding.passwordContainer);
            back_in(binding.emailContainer);
            binding.status2.setVisibility(View.INVISIBLE);
        });

        binding.backRepass.setOnClickListener(view -> {
            back_out(binding.repasswordContainer);
            back_in(binding.passwordContainer);
            binding.status3.setVisibility(View.INVISIBLE);
        });

        binding.backPhone.setOnClickListener(view -> {
            back_out(binding.phoneContainer);
            back_in(binding.repasswordContainer);
            binding.status4.setVisibility(View.INVISIBLE);
        });

    }

    private void textWatchers() {

        Objects.requireNonNull(binding.registerName.getEditText()).addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void afterTextChanged(Editable editable) {
                if (getName().isEmpty()) {
                    binding.registerName.setError(getString(R.string.enter_valid_name));
                    validateName = false;
                } else {
                    binding.registerName.setError(null);
                    validateName = true;
                }
            }
        });

        Objects.requireNonNull(binding.registerEmail.getEditText()).addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void afterTextChanged(Editable editable) {
                if (getEmail().isEmpty()) {
                    binding.registerEmail.setError(getString(R.string.email_cant_empty));
                    validateEmail = false;
                } else if (!Patterns.EMAIL_ADDRESS.matcher(getEmail()).matches()) {
                    binding.registerEmail.setError(getString(R.string.enter_valid_email));
                    validateEmail = false;
                } else {
                    binding.registerEmail.setError(null);
                    validateEmail = true;
                }
            }
        });

        Objects.requireNonNull(binding.registerPass.getEditText()).addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void afterTextChanged(Editable editable) {
                if (getPass().isEmpty()) {
                    binding.registerPass.setError(getString(R.string.password_cant_empty));
                    validatePass = false;
                } else if (getPass().length()<6) {
                    binding.registerPass.setError(getString(R.string.password_lenght_error));
                    validatePass = false;
                } else {
                    binding.registerPass.setError(null);
                    validatePass = true;
                }
            }
        });

        Objects.requireNonNull(binding.registerConfirmPass.getEditText()).addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void afterTextChanged(Editable editable) {
                if (getPass().isEmpty()) {
                    binding.registerConfirmPass.setError(getString(R.string.password_cant_empty));
                    validateRePass = false;
                } else if (!getPassConfirm().equals(getPass())) {
                    binding.registerConfirmPass.setError(getString(R.string.password_mismatch));
                    validateRePass = false;
                } else {
                    binding.registerConfirmPass.setError(null);
                    validateRePass = true;
                }
            }
        });

        Objects.requireNonNull(binding.registerNumber.getEditText()).addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void afterTextChanged(Editable editable) {
                if (getPass().isEmpty()) {
                    binding.registerNumber.setError(getString(R.string.password_cant_empty));
                    validateNum = false;
                } else if (getPhoneNumber().length()<10) {
                    binding.registerNumber.setError(getString(R.string.enter_valid_number));
                    validateNum = false;
                } else if (getPhoneNumber().length()>10) {
                    binding.registerNumber.setError("Enter valid Indian phone number");
                    validateNum = false;
                }else if (getPhoneNumber().length()==10){
                    binding.registerNumber.setError(null);
                    validateNum = true;
                }
            }
        });

    }

    private void sendVerificationCode(String number){

        registration.remove();

        PhoneAuthProvider.getInstance().verifyPhoneNumber(
                number,
                60,
                TimeUnit.SECONDS,
                TaskExecutors.MAIN_THREAD,
                mCallBack
        );
    }

    private PhoneAuthProvider.OnVerificationStateChangedCallbacks
            mCallBack = new PhoneAuthProvider.OnVerificationStateChangedCallbacks() {

        @Override
        public void onCodeSent(@NonNull String s, @NonNull PhoneAuthProvider.ForceResendingToken forceResendingToken) {
            super.onCodeSent(s, forceResendingToken);
            verificationId = s;
            showAlertDialog();
        }

        @Override
        public void onVerificationCompleted(PhoneAuthCredential phoneAuthCredential) {
            String code = phoneAuthCredential.getSmsCode();
            if (code != null){
                verifyCode(code);
            }
        }

        @Override
        public void onVerificationFailed(@NonNull FirebaseException e) {
            if (dialog.isShowing())
                dialog.dismiss();
            if (requestOpt.isShowing())
                requestOpt.dismiss();
            Common.toastShort(getApplicationContext(), e.getMessage(), Color.RED,Color.WHITE);
        }
    };

    private void showAlertDialog() {

        AlertDialog.Builder alertDialog = new AlertDialog.Builder(this);
        alertDialog.setTitle("Phone Number Verification");
        alertDialog.setMessage("Enter The Code Sent To Your Number");

        final EditText adCode = new EditText(this);
        adCode.setInputType(InputType.TYPE_CLASS_NUMBER);

        alertDialog.setView(adCode); //Adding Editext To Alert Dialog
        alertDialog.setCancelable(false);

        alertDialog.setPositiveButton("Verify", (dialogInterface, i) -> {

        });
        alertDialog.setNegativeButton("Cancel", (dialogInterface, i) -> dialogInterface.dismiss());

        alertDialog1 = alertDialog.create();
        if (requestOpt.isShowing())
            requestOpt.dismiss();
        if (dialog.isShowing())
            dialog.dismiss();
        alertDialog1.show();

        alertDialog1.getButton(AlertDialog.BUTTON_POSITIVE).setTextColor(ContextCompat.getColor(getApplicationContext(),R.color.colorAccent));
        alertDialog1.getButton(AlertDialog.BUTTON_NEGATIVE).setTextColor(ContextCompat.getColor(getApplicationContext(),R.color.colorAccent));

        alertDialog1.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(v -> {
            vdialog.show();

            String code = adCode.getText().toString();

            adCode.setText(code);

            if (code.isEmpty() || code.length() < 6){
                adCode.setError("Enter Valid Code");
                adCode.requestFocus();
                if (vdialog.isShowing()){
                    vdialog.dismiss();
                }
            }else {
                verifyCode(code);
            }
        });
    }

    private void verifyCode(String code){
        PhoneAuthCredential credential = PhoneAuthProvider.getCredential(verificationId, code);
        signinwithCredential(credential);
    }

    private void signinwithCredential(PhoneAuthCredential credential){
        auth.signInWithCredential(credential).addOnCompleteListener(task -> {
           if (task.isSuccessful()){

               if (alertDialog1.isShowing())
                   alertDialog1.dismiss();

               if (vdialog.isShowing()){
                   vdialog.dismiss();
               }

               dialog.show();

               final String name = getName();
               final String email = getEmail();
               final String pass = getPass();
               final String pno = getPhoneNumber();
               Date date = new Date();
               SimpleDateFormat df = new SimpleDateFormat("dd-MM-yyyy | hh:mm:ss a", Locale.getDefault());

               final String createdDate = df.format(date);
               final String createdDeviceInfo = String.format(Locale.getDefault(),"%s | %s | %s",
                       Build.BRAND, Build.MODEL, Build.VERSION.SDK_INT);

               List<CurrentDevice> currentDeviceList = new ArrayList<>();
               currentDeviceList.add(new CurrentDevice(Build.BRAND, Build.MODEL,
                       String.valueOf(Build.VERSION.SDK_INT)));

               final Map<String, List<CurrentDevice>> currentDeviceMap = new HashMap<>();
               SimpleDateFormat dateFormat = new SimpleDateFormat("dd-MM-yyyy", Locale.getDefault());
               currentDeviceMap.put(dateFormat.format(date), currentDeviceList);

               auth.createUserWithEmailAndPassword(getEmail(), getPass()).addOnCompleteListener(task1 -> {
                   if (task1.isSuccessful()){

                       Map<String, Object> userMap = new HashMap<>();
                       userMap.put("name", name);
                       userMap.put("email", email);
                       userMap.put("password", pass);
                       userMap.put("phone_number", pno);
                       userMap.put("created_date", createdDate);
                       userMap.put("created_device_info", createdDeviceInfo);
                       userMap.put("current_device", currentDeviceMap);

                       FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();

                       Map<String, String> map = new HashMap<>();
                       map.put("uid", Objects.requireNonNull(user).getUid());
                       userMap.put("uid", Objects.requireNonNull(user).getUid());

                       firestore.collection("Phones").document(pno).set(map);

                       firestore.collection("Users")
                               .document(Objects.requireNonNull(user).getUid())
                               .set(userMap)
                               .addOnCompleteListener(task2 -> {

                                   if (requestOpt.isShowing())
                                       requestOpt.dismiss();
                                   if (dialog.isShowing())
                                       dialog.dismiss();

                                  if (task2.isSuccessful()){
                                      Common.uid = user.getUid();
                                      Common.toastShort(getApplicationContext(), "Register successful", Color.GREEN, Color.BLACK);
                                      startActivity(new Intent(getApplicationContext(), MainActivity.class));
                                      finish();
                                  }else {
                                      Common.toastShort(getApplicationContext(), Objects.requireNonNull(Objects.requireNonNull(task2.getException()).getMessage()), Color.RED, Color.WHITE);
                                      Log.e("Register: ", Objects.requireNonNull(Objects.requireNonNull(task2.getException()).getMessage()));
                                  }
                               });

                   }else {
                       Common.toastShort(getApplicationContext(), Objects.requireNonNull(Objects.requireNonNull(task1.getException()).getMessage()), Color.RED, Color.WHITE);
                       Log.e("Register: ", Objects.requireNonNull(Objects.requireNonNull(task1.getException()).getMessage()));
                   }
               });

           }else {
               if (dialog.isShowing()){
                   dialog.dismiss();
               }
               if (vdialog.isShowing()){
                   vdialog.dismiss();
               }
               Common.toastShort(getApplicationContext(), "Phone verification failed", Color.RED,Color.WHITE);
           }
        });
    }

    private String getName() {
        return Objects.requireNonNull(binding.registerName.getEditText()).getText().toString();
    }

    private String getEmail() {
        return Objects.requireNonNull(binding.registerEmail.getEditText()).getText().toString();
    }

    private String getPass() {
        return Objects.requireNonNull(binding.registerPass.getEditText()).getText().toString();
    }

    private String getPassConfirm() {
        return Objects.requireNonNull(binding.registerConfirmPass.getEditText()).getText().toString();
    }

    private String getPhoneNumber() {
        return Objects.requireNonNull(binding.registerNumber.getEditText()).getText().toString();
    }

    private void in(LinearLayout l1){
        l1.startAnimation(slideIn);
        l1.setVisibility(View.VISIBLE);
    }

    private void out(LinearLayout l1) {
        l1.startAnimation(slideOut);
        l1.setVisibility(View.GONE);
    }

    private void back_in(LinearLayout l1){
        l1.startAnimation(backSlideIn);
        l1.setVisibility(View.VISIBLE);
    }

    private void back_out(LinearLayout l1){
        l1.startAnimation(backSlideOut);
        l1.setVisibility(View.GONE);
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
    }
}