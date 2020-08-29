package a.gautham.smartswitchboard.fragments;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
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
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.Objects;

import a.gautham.smartswitchboard.Common;
import a.gautham.smartswitchboard.R;
import a.gautham.smartswitchboard.activity.Login;

public class DeleteAccountFragment extends BottomSheetDialogFragment {

    private TextInputLayout pass;
    private LinearLayout passContainer, loadingContainer;
    private ImageView loadingImg;
    private TextView loadingInfo;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.delete_account_fragment, container, false);

        pass = root.findViewById(R.id.pass);
        passContainer = root.findViewById(R.id.pass_container);
        loadingContainer = root.findViewById(R.id.loading_container);
        loadingImg = root.findViewById(R.id.loading);
        loadingInfo = root.findViewById(R.id.info);

        Button delete = root.findViewById(R.id.delete_bt);
        delete.setOnClickListener(view -> {
            if (getPass().isEmpty()) {
                pass.setError(getString(R.string.password_cant_empty));
            } else if (getPass().length() < 6) {
                pass.setError(getString(R.string.password_lenght_error));
            } else {
                pass.setError(null);
                loading(true);
                setLoadingInfo(getString(R.string.verifying_password));

                FirebaseAuth.getInstance().signInWithEmailAndPassword(Common.EMAIL, getPass())
                        .addOnCompleteListener(task -> {
                            if (task.isSuccessful()) {

                                setLoadingInfo(getString(R.string.deleting_account));

                                FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
                                if (user != null) {
                                    FirebaseFirestore.getInstance().collection("Phones")
                                            .document(Common.PHONE_NUMBER).delete().addOnCompleteListener(task12 -> {
                                        //
                                    });
                                    FirebaseFirestore.getInstance().collection("Users")
                                            .document(Common.uid).delete().addOnCompleteListener(task1 -> {
                                        if (task1.isSuccessful()) {
                                            logOut();
                                            user.delete();
                                            Common.toastShort(getContext(), getString(R.string.acount_deleted), 0, 0);
                                            startActivity(new Intent(getActivity(), Login.class));
                                        } else {
                                            loading(false);
                                            Common.toastShort(getContext(), getString(R.string.failed_to_delete_account), 0, 0);
                                        }
                                    });
                                }

                            } else {
                                loading(false);
                                Common.toastShort(getContext(),
                                        Objects.requireNonNull(task.getException()).getMessage(),
                                        Color.RED, Color.WHITE);
                            }
                        });

            }
        });

        return root;
    }

    private void logOut() {
        FirebaseAuth.getInstance().signOut();
        SharedPreferences userPreferences = requireActivity().getSharedPreferences("User", Context.MODE_PRIVATE);
        int theme = userPreferences.getInt("theme", 0);
        userPreferences.edit().clear().apply();
        userPreferences.edit().putInt("theme", theme).apply();
        SharedPreferences prefs = requireActivity().getSharedPreferences("DB_temp", Context.MODE_PRIVATE);
        prefs.edit().clear().apply();
        Common.uid = "default";
        Common.PHONE_NUMBER = "default";
        Common.EMAIL = "default";
        Common.NAME = "default";
    }

    private void loading(boolean value) {

        if (value) {

            passContainer.setVisibility(View.GONE);
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

        passContainer.setVisibility(View.VISIBLE);
        loadingContainer.setVisibility(View.GONE);

    }

    private void setLoadingInfo(String text) {
        loadingInfo.setText(text);
    }

    private String getPass() {
        return Objects.requireNonNull(pass.getEditText()).getText().toString();
    }

}
