package a.gautham.smartswitchboard;

import android.content.Intent;
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
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.vectordrawable.graphics.drawable.Animatable2Compat;
import androidx.vectordrawable.graphics.drawable.AnimatedVectorDrawableCompat;

import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.FirebaseAuth;

import java.util.Objects;

public class ChangePasswordFragment extends BottomSheetDialogFragment {

    private LinearLayout oldPassContainer, newPassContainer, phoneContainer, optContainer,
            loadingContainer;
    private TextInputLayout oldPass, newPass, phoneInput;
    private ImageView loadingImg;
    private TextView loadingInfo;

    private Animation slideIn, slideOut, backSlideIn, backSlideOut;

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
        phoneInput = root.findViewById(R.id.phoneInput);
        loadingImg = root.findViewById(R.id.loading);
        loadingInfo = root.findViewById(R.id.info);

        slideIn = AnimationUtils.loadAnimation(getActivity(), R.anim.slide_in_right);
        slideOut = AnimationUtils.loadAnimation(getActivity(), R.anim.slide_out_left);

        backSlideIn = AnimationUtils.loadAnimation(getActivity(), R.anim.slide_in_left);
        backSlideOut = AnimationUtils.loadAnimation(getActivity(), R.anim.slide_out_right);

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

            out(oldPassContainer);
            in(newPassContainer);

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

            try {
                Objects.requireNonNull(FirebaseAuth.getInstance().getCurrentUser())
                        .updatePassword(getNewPass()).addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        Common.toastShort(getActivity(), "Password Updated, Re-Login",
                                0, 0);
                        FirebaseAuth.getInstance().signOut();
                        loading(false);
                        startActivity(new Intent(getActivity(), Login.class));
                        requireActivity().finish();
                    } else {
                        loading(false);
                        Common.toastShort(getActivity(), "Failed to change password",
                                0, 0);
                    }
                });
            } catch (Exception e) {
                loading(false);
                Log.e("Change Password Error: ", e.toString());
            }

        });

        Button backNewPass = root.findViewById(R.id.back_new_pass);
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
        backPhone.setOnClickListener(view -> {
            back_out(phoneContainer);
            back_in(oldPassContainer);
        });

        return root;
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

        oldPassContainer.setVisibility(View.VISIBLE);
        newPassContainer.setVisibility(View.VISIBLE);
        phoneContainer.setVisibility(View.VISIBLE);
        optContainer.setVisibility(View.VISIBLE);
        loadingContainer.setVisibility(View.GONE);

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

}
