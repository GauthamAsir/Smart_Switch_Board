package a.gautham.smartswitchboard.fragments;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.vectordrawable.graphics.drawable.Animatable2Compat;
import androidx.vectordrawable.graphics.drawable.AnimatedVectorDrawableCompat;

import com.google.android.gms.tasks.TaskExecutors;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.FirebaseException;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.PhoneAuthCredential;
import com.google.firebase.auth.PhoneAuthProvider;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.ListenerRegistration;

import java.util.Objects;
import java.util.concurrent.TimeUnit;

import a.gautham.smartswitchboard.Common;
import a.gautham.smartswitchboard.R;
import a.gautham.smartswitchboard.activity.MainActivity;

public class PhoneLoginFragment extends BottomSheetDialogFragment {

    private TextInputLayout phone;
    private EditText otpView;
    private ImageView loadingImg;
    private TextView loadingInfo;

    private LinearLayout phoneInputContainer, loadingContainer, otpContainer;

    private FirebaseAuth auth;
    private ListenerRegistration registration;

    private FirebaseFirestore fireStore;
    private String verificationId;

    public PhoneLoginFragment() {
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.phone_login_fragment,container,false);

        phone = root.findViewById(R.id.phoneInputDialog);
        Button login = root.findViewById(R.id.login_phone_dialog);
        loadingImg = root.findViewById(R.id.loading);
        loadingInfo = root.findViewById(R.id.info);
        phoneInputContainer = root.findViewById(R.id.phoneInputContainer);
        loadingContainer = root.findViewById(R.id.loadingContainer);
        otpContainer = root.findViewById(R.id.otp_container);
        Button verify = root.findViewById(R.id.verify);
        otpView = root.findViewById(R.id.optView);

        auth = FirebaseAuth.getInstance();
        fireStore = FirebaseFirestore.getInstance();

        login.setOnClickListener(view -> {

            if (getPhone().length()>10){
                phone.setError("Enter only Indian phone number");
            }else if (getPhone().length()!=10){
                phone.setError("Enter valid phone number");
            }else {
                phone.setError(null);
            }

            if (getPhone().length()==10){

                loading(true);
                setLoadingInfo("Connecting to database");

                DocumentReference docRef = fireStore.collection("Phones").document(getPhone());

                if (!Common.checkInternet(requireActivity())) {
                    Common.toastShort(getActivity(), "No Internet Connection", Color.RED, Color.WHITE);
                    return;
                }

                registration = docRef.addSnapshotListener((documentSnapshot, e) -> {
                    if (documentSnapshot != null) {
                        if (documentSnapshot.exists()){
                            String phoneNumber = "+" + "91" + getPhone();
                            sendVerificationCode(phoneNumber);
                            return;
                        }

                        Common.toastShort(getActivity(), "No User Found", Color.RED,Color.WHITE);
                        loading(false);
                        return;

                    }
                    Common.toastShort(getActivity(), "No User Found", Color.RED,Color.WHITE);
                    loading(false);

                });

            }

        });

        verify.setOnClickListener(view -> {

            String otp = otpView.getText().toString();

            if (otp.length()!=6){
                otpView.setError("Enter valid otp");
                return;
            }

            otpView.setError(null);

            loading(true);
            setLoadingInfo("Verifying Otp");
            verifyCode(otp);

        });

        return root;
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
            loading(false);
            phoneInputContainer.setVisibility(View.GONE);
            otpContainer.setVisibility(View.VISIBLE);
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
            Common.toastShort(getActivity(), e.getMessage(), Color.RED,Color.WHITE);
            loading(false);
        }
    };

    private void verifyCode(String code){
        PhoneAuthCredential credential = PhoneAuthProvider.getCredential(verificationId, code);
        signinwithCredential(credential);
    }

    private void signinwithCredential(PhoneAuthCredential credential){
        auth.signInWithCredential(credential).addOnCompleteListener(task -> {
            if (task.isSuccessful()) {

                setLoadingInfo("Logging in");

                FirebaseFirestore.getInstance().collection("Phones")
                        .document(getPhone()).addSnapshotListener((documentSnapshot, e) -> {
                    if (documentSnapshot != null) {

                        setLoadingInfo("Getting Account Details");

                        if (documentSnapshot.exists()) {

                            SharedPreferences preferences = requireActivity().getSharedPreferences("User", Context.MODE_PRIVATE);
                            preferences.edit().putString("uid", Objects.requireNonNull(documentSnapshot.get("uid")).toString()).apply();
                            Common.uid = Objects.requireNonNull(documentSnapshot.get("uid")).toString();
                            preferences.edit().putBoolean("sync", true).apply();

                            Common.toastShort(getActivity(), "Login successful", ContextCompat.getColor(
                                    requireActivity(), R.color.dark_green
                            ), Color.BLACK);
                            startActivity(new Intent(getActivity(), MainActivity.class));
                            requireActivity().finish();

                        } else {
                            loading(false);
                            Common.toastShort(getActivity(), "Login failed", Color.RED, Color.WHITE);
                        }

                    } else {
                        loading(false);
                        Common.toastShort(getActivity(), "Login failed", Color.RED, Color.WHITE);
                    }
                });

            }else {
                Common.toastShort(getActivity(),"Otp verification failed", Color.RED, Color.WHITE);
                Log.e("Login Otp", Objects.requireNonNull(Objects.requireNonNull(task.getException()).getMessage()));
                loading(false);
            }
        });
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        View touchOutsideView = Objects.requireNonNull(Objects.requireNonNull(getDialog()).getWindow())
                .getDecorView()
                .findViewById(com.google.android.material.R.id.touch_outside);
        touchOutsideView.setOnClickListener(null);

    }

    private void loading(boolean value) {

        if (value){

            phoneInputContainer.setVisibility(View.GONE);
            otpContainer.setVisibility(View.GONE);
            loadingContainer.setVisibility(View.VISIBLE);

            AnimatedVectorDrawableCompat animatedVector = AnimatedVectorDrawableCompat.create(requireActivity(), R.drawable.heart_rate);
            loadingImg.setImageDrawable(animatedVector);
            final Handler mainHandler = new Handler(Looper.getMainLooper());
            assert animatedVector != null;
            animatedVector.registerAnimationCallback(new Animatable2Compat.AnimationCallback() {
                @Override
                public void onAnimationEnd(final Drawable drawable) {
                    mainHandler.post(animatedVector::start);
                }
            });
            animatedVector.start();

            return;
        }

        phoneInputContainer.setVisibility(View.VISIBLE);
        otpContainer.setVisibility(View.GONE);
        loadingContainer.setVisibility(View.GONE);

    }

    private void setLoadingInfo(String text){
        loadingInfo.setText(text);
    }

    private String getPhone() {
        return Objects.requireNonNull(phone.getEditText()).getText().toString();
    }

}