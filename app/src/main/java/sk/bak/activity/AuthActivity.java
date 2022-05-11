package sk.bak.activity;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;

import sk.bak.R;

public class AuthActivity extends AppCompatActivity {

    private ImageButton login;

    private FirebaseAuth mAuth;
    private GoogleSignInClient mGoogleSignInClient;

    private ActivityResultLauncher<Intent> googleAuthActivityResultLauncher;

    private Activity currentActivity;

    private static final String TAG = "Auth activity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_auth);

        Log.i(TAG, "onCreate: spustam auth");

        currentActivity = this;

        GoogleSignInOptions gso = new GoogleSignInOptions
                .Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken("975005148527-94d1hccka7q11ndh6psp5vac0p1hlrtf.apps.googleusercontent.com")
                .requestEmail()
                .build();

        mGoogleSignInClient = GoogleSignIn.getClient(this, gso);

        login = findViewById(R.id.auth_login_button);
        mAuth = FirebaseAuth.getInstance();

        googleAuthActivityResultLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                new ActivityResultCallback<ActivityResult>() {
                    @Override
                    public void onActivityResult(ActivityResult result) {
                        Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(result.getData());
                        try {
                            Log.i(TAG, "onActivityResult: google sing in ok");
                            GoogleSignInAccount account = task.getResult(ApiException.class);
                            firebaseAuthWithGoogle(account.getIdToken());

                        } catch (ApiException e) {
                            Log.i(TAG, "onActivityResult: google sing in Nok" + e.getMessage());
                            AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(currentActivity);

                            alertDialogBuilder.setMessage("Nastala chyba pri pokuse o prihlásenie sa do Google účtu. Pokus opakujte. Overte si propojenie na internet");

                            alertDialogBuilder.setPositiveButton("Ok", null);
                            alertDialogBuilder.create().show();
                            // The ApiException status code indicates the detailed failure reason.
                            // Please refer to the GoogleSignInStatusCodes class reference for more information.
                        }
                    }
                });
    }


    private void signIn(boolean forceSingIn) {

        Log.i(TAG, "signIn: kliknute na prihlasit sa");
        mGoogleSignInClient.signOut();
        Intent intent = mGoogleSignInClient.getSignInIntent();
        GoogleSignInAccount lastSingIn = GoogleSignIn.getLastSignedInAccount(this);

        if (lastSingIn == null || lastSingIn.isExpired() || forceSingIn) {
            Log.i(TAG, "signIn: nemam aktivneho uzivatela, spustam googleAuthActivityResultLauncher ");
            googleAuthActivityResultLauncher.launch(intent);
        } else {
            Log.i(TAG, "signIn: mam aktivneho uzivatela, firebasesingin " + lastSingIn.getEmail());
            firebaseAuthWithGoogle(lastSingIn.getIdToken());
        }
    }

    @Override
    public void onStart() {
        super.onStart();
        FirebaseUser currentUser = mAuth.getCurrentUser();

        login.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                signIn(false);
            }
        });
    }


    private void firebaseAuthWithGoogle(String idToken) {
        AuthCredential credential = GoogleAuthProvider.getCredential(idToken, null);
        Log.i(TAG, "firebaseAuthWithGoogle: spustam firebase auth");
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            Log.i(TAG, "onComplete: firebase ok");
                            // Sign in success, update UI with the signed-in user's information
                            Intent menu = new Intent(getApplicationContext(), MainMenu.class);
                            startActivity(menu);
                            currentActivity.finish();
                        } else {
                            Log.i(TAG, "onComplete: firebase Nok");
                            AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(currentActivity);

                            alertDialogBuilder.setMessage("Nastala chyba pri pokuse o prihlásenie sa do databázy. Pokus opakujte. Overte si propojenie na internet, Pokus po odkliknutí tohto okna budeme automaticky opakovať");

                            alertDialogBuilder.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    signIn(true);
                                }
                            });
                            alertDialogBuilder.create().show();
                        }
                    }
                });
    }
}