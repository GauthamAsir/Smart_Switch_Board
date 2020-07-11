package a.gautham.smartswitchboard;

import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
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

import java.util.Objects;

public class ChangePasswordFragment extends BottomSheetDialogFragment {

    private LinearLayout oldPassContainer, newPassContainer, phoneContainer, optContainer, loadingContainer;
    private TextInputLayout oldPass;
    private ImageView loadingImg;
    private TextView loadingInfo;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.change_password_fragment, container, false);

        oldPassContainer = root.findViewById(R.id.email_container);
        newPassContainer = root.findViewById(R.id.new_pass_container);
        phoneContainer = root.findViewById(R.id.phone_container);
        optContainer = root.findViewById(R.id.otp_container);
        loadingContainer = root.findViewById(R.id.loading_container);
        oldPass = root.findViewById(R.id.oldPass);
        loadingImg = root.findViewById(R.id.loading);
        loadingInfo = root.findViewById(R.id.info);

        Button nextOldPass = root.findViewById(R.id.next_old_pass);
        nextOldPass.setOnClickListener(view -> {


        });

        return root;
    }

    private void loading(boolean value) {

        if (value) {

            //emailContainer.setVisibility(View.GONE);
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

        //emailContainer.setVisibility(View.VISIBLE);
        loadingContainer.setVisibility(View.GONE);

    }

    private void setLoadingInfo(String text) {
        loadingInfo.setText(text);
    }

    private String getOldPass() {
        return Objects.requireNonNull(oldPass.getEditText()).getText().toString();
    }

}
