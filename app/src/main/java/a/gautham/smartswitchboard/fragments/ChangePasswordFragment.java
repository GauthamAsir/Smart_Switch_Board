package a.gautham.smartswitchboard.fragments;

import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
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
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.ListenerRegistration;

import java.util.List;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

import a.gautham.smartswitchboard.Common;
import a.gautham.smartswitchboard.R;
import a.gautham.smartswitchboard.activity.Login;

public class ChangePasswordFragment extends BottomSheetDialogFragment {

    private boolean forgotPass;
    private ListenerRegistration passRegister;

    private LinearLayout oldPassContainer, newPassContainer, phoneContainer, optContainer,
            loadingContainer;
    private TextInputLayout oldPass, newPass, phoneInput;
    private EditText optCode;
    private ImageView loadingImg;
    private TextView loadingInfo;

    private String verificationId, uid, email;

    private Animation slideIn, slideOut, backSlideIn, backSlideOut;

    public ChangePasswordFragment(boolean forgotPass) {
        this.forgotPass = forgotPass;
    }

    private PhoneAuthProvider.OnVerificationStateChangedCallbacks
            mCallBack = new PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
        @Override
        public void onVerificationCompleted(@NonNull PhoneAuthCredential phoneAuthCredential) {
            String code = phoneAuthCredential.getSmsCode();
            if (code != null) {
                verifyCode(code);
            }
        }

        @Override
        public void onCodeSent(@NonNull String s, @NonNull PhoneAuthProvider.ForceResendingToken forceResendingToken) {
            super.onCodeSent(s, forceResendingToken);
            verificationId = s;
            out(loadingContainer);
            in(optContainer);
        }

        @Override
        public void onVerificationFailed(@NonNull FirebaseException e) {
            loading(false);
            Log.d("Phone Verification : ", Objects.requireNonNull(e.getMessage()));
            Common.toastShort(requireActivity(), e.getMessage(), Color.RED, Color.WHITE);
        }
    };

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.change_password_fragment, container, false);

        oldPassContainer = root.findViewById(R.id.old_pass_container);
        newPassContainer = root.findViewById(R.id.new_pass_container);
        phoneContainer = root.findViewById(R.id.phone_container);
        optContainer = root.findViewById(R.id.otp_container);
        loadingContainer = root.findViewById(R.id.loading_container);
        oldPass = root.findViewById(R.id.oldPass);
        newPass = root.findViewById(R.id.newPass);
        optCode = root.findViewById(R.id.optView);
        phoneInput = root.findViewById(R.id.phoneInput);
        loadingImg = root.findViewById(R.id.loading);
        loadingInfo = root.findViewById(R.id.info);

        getEmail();

        slideIn = AnimationUtils.loadAnimation(getActivity(), R.anim.slide_in_right);
        slideOut = AnimationUtils.loadAnimation(getActivity(), R.anim.slide_out_left);

        backSlideIn = AnimationUtils.loadAnimation(getActivity(), R.anim.slide_in_left);
        backSlideOut = AnimationUtils.loadAnimation(getActivity(), R.anim.slide_out_right);

        if (forgotPass) {
            oldPassContainer.setVisibility(View.GONE);
            phoneContainer.setVisibility(View.VISIBLE);
        } else {
            oldPassContainer.setVisibility(View.VISIBLE);
            phoneContainer.setVisibility(View.GONE);
        }

        Button nextOldPass = root.findViewById(R.id.next_old_pass);
        nextOldPass.setOnClickListener(view -> {
            if (getOldPass().isEmpty()) {
                oldPass.setError(getString(R.string.password_cant_empty));
                return;
            }

            if (getOldPass().length() < 6) {
                oldPass.setError(getString(R.string.password_lenght_error));
                return;
            }

            oldPass.setError(null);

            loading(true);
            setLoadingInfo("Verifying Credentials");

            FirebaseAuth.getInstance().signInWithEmailAndPassword(Objects.requireNonNull(
                    Objects.requireNonNull(FirebaseAuth.getInstance().getCurrentUser()).getEmail()), getOldPass())
                    .addOnCompleteListener(task -> {

                        if (task.isSuccessful()) {
                            out(loadingContainer);
                            in(newPassContainer);
                        } else {
                            loading(false);
                            Common.toastShort(requireActivity(), "Wrong Password", 0, 0);
                        }

                    });

        });

        Button changePassBt = root.findViewById(R.id.changePassBt);
        changePassBt.setOnClickListener(view -> {
            if (getNewPass().isEmpty()) {
                newPass.setError(getString(R.string.password_cant_empty));
                return;
            }

            if (getNewPass().length() < 6) {
                newPass.setError(getString(R.string.password_lenght_error));
                return;
            }
            newPass.setError(null);
            loading(true);
            setLoadingInfo("Changing Password");
            changePass();

        });

        Button backNewPass = root.findViewById(R.id.back_new_pass);
        if (forgotPass) {
            backNewPass.setVisibility(View.GONE);
        } else {
            backNewPass.setVisibility(View.VISIBLE);
        }
        backNewPass.setOnClickListener(view -> {
            back_out(newPassContainer);
            back_in(oldPassContainer);
        });

        Button tryAnotherWay = root.findViewById(R.id.try_another_way);
        tryAnotherWay.setOnClickListener(view -> {
            out(oldPassContainer);
            in(phoneContainer);
        });

        Button backPhone = root.findViewById(R.id.back_phone);
        if (forgotPass) {
            backPhone.setVisibility(View.GONE);
        } else {
            backPhone.setVisibility(View.VISIBLE);
        }
        backPhone.setOnClickListener(view -> {
            back_out(phoneContainer);
            back_in(oldPassContainer);
        });

        Button phoneNext = root.findViewById(R.id.next_phone);

        phoneNext.setOnClickListener(view -> {
            if (getPhone().trim().length() != 10) {
                phoneInput.setError(getString(R.string.enter_valid_number));
                return;
            }

            phoneInput.setError(null);
            loading(true);
            setLoadingInfo("Checking User Existence");

            FirebaseFirestore.getInstance().collection("Phones")
                    .get().addOnCompleteListener(task -> {
                if (task.isSuccessful()) {
                    List<DocumentSnapshot> list = Objects.requireNonNull(task.getResult()).getDocuments();
                    for (int i = 0; i < list.size(); i++) {
                        if (list.get(i).getId().equals(getPhone())) {
                            uid = (String) list.get(i).get("uid");
                            getEmail();
                            setLoadingInfo("Verifying");
                            setLoadingInfo("Requesting Otp");
                            sendVerificationCode("+91" + getPhone());
                            break;
                        }
                    }
                }
            });
        });

        Button verify = root.findViewById(R.id.verify);
        verify.setOnClickListener(view -> {
            if (getCode().trim().length() != 6) {
                optCode.setError(getString(R.string.enter_valid_otp));
                return;
            }
            optCode.setError(null);
            verifyCode(getCode().trim());

        });

        return root;
    }

    private void getEmail() {
        if (forgotPass) {
            if (uid != null) {
                FirebaseFirestore.getInstance().collection("Users").document(uid)
                        .get().addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        email = (String) Objects.requireNonNull(task.getResult()).get("email");
                    }
                });
            }
            return;
        }

        uid = FirebaseAuth.getInstance().getUid();
        email = Objects.requireNonNull(FirebaseAuth.getInstance().getCurrentUser()).getEmail();
    }

    private void in(LinearLayout l1) {
        l1.startAnimation(slideIn);
        l1.setVisibility(View.VISIBLE);
    }

    private void out(LinearLayout l1) {
        l1.startAnimation(slideOut);
        l1.setVisibility(View.GONE);
    }

    private void back_in(LinearLayout l1) {
        l1.startAnimation(backSlideIn);
        l1.setVisibility(View.VISIBLE);
    }

    private void back_out(LinearLayout l1) {
        l1.startAnimation(backSlideOut);
        l1.setVisibility(View.GONE);
    }

    private void changePass() {

        loading(true);
        setLoadingInfo("Changing Password");

        try {
            Objects.requireNonNull(FirebaseAuth.getInstance().getCurrentUser())
                    .updatePassword(getNewPass()).addOnCompleteListener(task -> {
                if (task.isSuccessful()) {
                    FirebaseFirestore.getInstance().collection("Users")
                            .document(FirebaseAuth.getInstance().getCurrentUser().getUid())
                            .update("password", getNewPass());
                    if (email != null) {
                        FirebaseAuth.getInstance().signInWithEmailAndPassword(email, getNewPass())
                                .addOnCompleteListener(task1 -> {
                                    if (task.isSuccessful()) {
                                        Common.toastShort(getActivity(), "Password Updated", 0, 0);
                                    } else {
                                        Log.d("Error", Objects.requireNonNull(Objects.requireNonNull(task.getException()).getMessage()));
                                        Common.toastShort(getActivity(), "Password Updated, Re-Login",
                                                0, 0);
                                    }
                                    FirebaseAuth.getInstance().signOut();
                                    dismiss();
                                    startActivity(new Intent(requireActivity(), Login.class));
                                    requireActivity().finish();
                                });
                    } else {
                        Log.d("Error", "E-mail is null");
                        Common.toastShort(getActivity(), "Failed to update password",
                                0, 0);
                    }
                } else {
                    loading(false);
                    Log.e("Change Password : ", Objects.requireNonNull(Objects.requireNonNull(task.getException()).getMessage()));
                    Common.toastShort(getActivity(), Objects.requireNonNull(task.getException().getMessage()),
                            0, 0);
                }
            });
        } catch (Exception e) {
            loading(false);
            Log.e("Change Password Error: ", e.toString());
            Common.toastShort(getActivity(), e.getMessage(), 0, 0);
        }

    }

    private void setLoadingInfo(String text) {
        loadingInfo.setText(text);
    }

    private String getOldPass() {
        return Objects.requireNonNull(oldPass.getEditText()).getText().toString();
    }

    private String getNewPass() {
        return Objects.requireNonNull(newPass.getEditText()).getText().toString();
    }

    private String getPhone() {
        return Objects.requireNonNull(phoneInput.getEditText()).getText().toString();
    }

    private void loading(boolean value) {

        if (value) {

            oldPassContainer.setVisibility(View.GONE);
            newPassContainer.setVisibility(View.GONE);
            phoneContainer.setVisibility(View.GONE);
            optContainer.setVisibility(View.GONE);
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

        if (forgotPass) {
            oldPassContainer.setVisibility(View.GONE);
            newPassContainer.setVisibility(View.GONE);
            phoneContainer.setVisibility(View.VISIBLE);
            optContainer.setVisibility(View.GONE);
            loadingContainer.setVisibility(View.GONE);
            return;
        }

        oldPassContainer.setVisibility(View.VISIBLE);
        newPassContainer.setVisibility(View.GONE);
        phoneContainer.setVisibility(View.GONE);
        optContainer.setVisibility(View.GONE);
        loadingContainer.setVisibility(View.GONE);

    }

    private String getCode() {
        return optCode.getText().toString();
    }

    private void sendVerificationCode(String number) {

        PhoneAuthProvider.getInstance().verifyPhoneNumber(
                number,
                60,
                TimeUnit.SECONDS,
                TaskExecutors.MAIN_THREAD,
                mCallBack
        );
    }

    private void verifyCode(String code) {
        loading(true);
        PhoneAuthCredential credential = PhoneAuthProvider.getCredential(verificationId, code);
        signInWithCredential(credential);
    }

    private void signInWithCredential(PhoneAuthCredential credential) {
        FirebaseAuth.getInstance().signInWithCredential(credential).addOnCompleteListener(task -> {

            DocumentReference docRef = FirebaseFirestore.getInstance()
                    .collection("Users").document(uid);

            passRegister = docRef.addSnapshotListener((documentSnapshot, e) -> {
                if (documentSnapshot != null) {
                    FirebaseAuth.getInstance().signInWithEmailAndPassword(Objects.requireNonNull(documentSnapshot.get("email")).toString(),
                            Objects.requireNonNull(documentSnapshot.get("password")).toString())
                            .addOnCompleteListener(task1 -> {
                                if (task1.isSuccessful()) {
                                    out(loadingContainer);
                                    in(newPassContainer);
                                } else {
                                    passRegister.remove();
                                    loading(false);
                                    Common.toastShort(requireActivity(), "Phone verification failed", 0, 0);
                                }
                            });

                } else {
                    loading(false);
                    Common.toastShort(requireActivity(), "Phone verification failed", 0, 0);
                }
            });
        });
    }

}
