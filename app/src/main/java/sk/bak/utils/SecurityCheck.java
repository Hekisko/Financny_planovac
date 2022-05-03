package sk.bak.utils;

import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.biometric.BiometricManager;
import androidx.biometric.BiometricPrompt;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.FragmentActivity;

import java.util.concurrent.Executor;

public class SecurityCheck {

    private BiometricPrompt biometricPrompt;
    private BiometricPrompt.PromptInfo promptInfo;

    public SecurityCheck(Activity activity, int layoutId, Context context) {
        Executor executor;
        executor = ContextCompat.getMainExecutor(context);
        biometricPrompt = new BiometricPrompt((FragmentActivity) activity,
                executor,
                vygenerujCallback(activity, layoutId));

        promptInfo = vygenerujPromtInfo();
    }

    private BiometricPrompt.AuthenticationCallback vygenerujCallback(Activity activity, int layoutId) {
        return new androidx.biometric.BiometricPrompt.AuthenticationCallback() {
            @Override
            public void onAuthenticationError(int errorCode,
                                              @NonNull CharSequence errString) {
                super.onAuthenticationError(errorCode, errString);

                AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(activity);

                alertDialogBuilder.setMessage("Pre používanie aplikácie musíte mať nastavené zabezpečenie zariadenia");

                alertDialogBuilder.setOnCancelListener(new DialogInterface.OnCancelListener() {
                    @Override
                    public void onCancel(DialogInterface dialog) {
                        activity.finish();
                        System.exit(0);
                    }
                });

                alertDialogBuilder.create().show();
            }

            @Override
            public void onAuthenticationSucceeded(
                    @NonNull androidx.biometric.BiometricPrompt.AuthenticationResult result) {
                super.onAuthenticationSucceeded(result);
                activity.findViewById(layoutId).setVisibility(View.VISIBLE);
            }

            @Override
            public void onAuthenticationFailed() {
                super.onAuthenticationFailed();

                AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(activity);

                alertDialogBuilder.setMessage("Autentifikácia nebola úspešná.");

                alertDialogBuilder.setOnCancelListener(new DialogInterface.OnCancelListener() {
                    @Override
                    public void onCancel(DialogInterface dialog) {
                        activity.finish();
                        System.exit(0);
                    }
                });

                alertDialogBuilder.create().show();

            }
        };
    }

    private BiometricPrompt.PromptInfo vygenerujPromtInfo() {
        if (android.os.Build.VERSION.SDK_INT >= 30){
            return new BiometricPrompt.PromptInfo.Builder()
                    .setTitle("Potvrdenie zabezpečenia")
                    .setAllowedAuthenticators(BiometricManager.Authenticators.DEVICE_CREDENTIAL | BiometricManager.Authenticators.BIOMETRIC_STRONG)
                    .setConfirmationRequired(true)
                    .build();
        } else {
            return new BiometricPrompt.PromptInfo.Builder()
                    .setTitle("Potvrdenie zabezpečenia")
                    .setDeviceCredentialAllowed(true)
                    .setConfirmationRequired(true)
                    .build();
        }
    }

    public void isDeviceSecured() {
        biometricPrompt.authenticate(promptInfo);
    }
}
