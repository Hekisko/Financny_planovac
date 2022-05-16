package sk.bak.utils;

import android.app.Activity;
import android.app.KeyguardManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.view.View;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.biometric.BiometricManager;
import androidx.biometric.BiometricPrompt;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.FragmentActivity;

import java.util.concurrent.Executor;


/**
 *
 * Trieda na detekciu a následne spustanie kontroly hesla/patternu/pinu ... od zariadenia
 *
 */
public class SecurityCheck {

    // UI komponenty
    private Context context;
    private int layoutId;
    private Activity activity;

    private BiometricPrompt biometricPrompt;
    private BiometricPrompt.PromptInfo promptInfo;
    private KeyguardManager keyguardManager;


    /**
     *
     *  Init tejto triedy prebieha v MainMenu.java. Pred každým spustením musi prebehnut init.
     *
     *
     * @param activity
     * @param layoutId
     * @param context
     */
    public SecurityCheck(Activity activity, int layoutId, Context context) {
        this.context = context;
        this.layoutId = layoutId;
        this.activity = activity;

        if (android.os.Build.VERSION.SDK_INT >= 30) {
            Executor executor;
            executor = ContextCompat.getMainExecutor(context);
            biometricPrompt = new BiometricPrompt((FragmentActivity) activity,
                    executor,
                    vygenerujCallback(activity, layoutId));

            promptInfo = vygenerujPromtInfo();
        } else {
            keyguardManager = (KeyguardManager) context.getSystemService(Context.KEYGUARD_SERVICE);
        }
    }

    private BiometricPrompt.AuthenticationCallback vygenerujCallback(Activity activity, int layoutId) {
        return new androidx.biometric.BiometricPrompt.AuthenticationCallback() {
            @Override
            public void onAuthenticationError(int errorCode,
                                              @NonNull CharSequence errString) {
                super.onAuthenticationError(errorCode, errString);

                showError();
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


    private void showError() {
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


    /**
     *
     * Metoda volana pri potrebe potrdenia zabezpecenia
     *
     */
    public void isDeviceSecured() {
        if (android.os.Build.VERSION.SDK_INT >= 30){
            biometricPrompt.authenticate(promptInfo);
        } else {
            if (!keyguardManager.isKeyguardSecure()) {
                showError();
            }
            else {
                ActivityResultLauncher<Intent> activityResultLauncherKeyguardManager = ((AppCompatActivity)activity).registerForActivityResult(
                        new ActivityResultContracts.StartActivityForResult(),
                        result -> {
                            if (result.getResultCode() == Activity.RESULT_OK) {
                                activity.findViewById(layoutId).setVisibility(View.VISIBLE);
                            }
                            else {
                                showError();
                            }
                        }
                );

                Intent intent = keyguardManager.createConfirmDeviceCredentialIntent(null, null);
                activityResultLauncherKeyguardManager.launch(intent);
            }
        }
    }
}


