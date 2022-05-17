package sk.bak.fragmenty;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.preference.Preference;
import androidx.preference.PreferenceFragmentCompat;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.firebase.auth.FirebaseAuth;

import java.util.Date;

import sk.bak.R;
import sk.bak.activity.AuthActivity;
import sk.bak.managers.DatabaseManager;
import sk.bak.utils.MySharedPreferences;


/**
 *
 * Trieda fragmentu Nastavenia
 *
 */
public class NastaveniaFragment extends PreferenceFragmentCompat {

    private static final String TAG = "NastaveniaFragment";


    /**
     *
     * Tu sa len postupne definuju onClickListenery pre dané nastavnia
     *
     */
    @Override
    public void onCreatePreferences(@Nullable Bundle savedInstanceState, @Nullable String rootKey) {
        setPreferencesFromResource(R.xml.settings_fragment, rootKey);


        Preference logOut = (Preference) findPreference("logOut");
        logOut.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            public boolean onPreferenceClick(Preference preference) {

                AlertDialog.Builder potvrdenieBuilder = new AlertDialog.Builder(getActivity());

                potvrdenieBuilder.setPositiveButton("Áno", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        Log.i(TAG, "onClick: ohlasujem sa");

                        GoogleSignInOptions gso = new GoogleSignInOptions
                                .Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                                .requestIdToken("975005148527-94d1hccka7q11ndh6psp5vac0p1hlrtf.apps.googleusercontent.com")
                                .requestEmail()
                                .build();

                        GoogleSignInClient mGoogleSignInClient = GoogleSignIn.getClient(getContext(), gso);

                        FirebaseAuth firebaseAuth = FirebaseAuth.getInstance();

                        firebaseAuth.signOut();
                        mGoogleSignInClient.signOut();
                        DatabaseManager.clear();

                        MySharedPreferences sharedPreferences = new MySharedPreferences(getContext());
                        sharedPreferences.clearPrefs();
                        Intent intent = new Intent(getContext(), AuthActivity.class);
                        startActivity(intent);
                        getActivity().finish();
                        Log.i(TAG, "onClick: odhlasovanie dokoncene");
                    }
                });

                potvrdenieBuilder.setNegativeButton("Nie", null);

                potvrdenieBuilder.setTitle("Potrdenie");
                potvrdenieBuilder.setMessage("Naozaj si želáte odhlásiť sa? Vaše dáta budú prístupné znova po opätovnom prihlásení.");

                potvrdenieBuilder.create().show();

                return true;
            }
        });

        Preference travalePrikazyPrepocitaj = (Preference) findPreference("trvalePrikazyPrepocitaj");
        travalePrikazyPrepocitaj.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(@NonNull Preference preference) {
                DatabaseManager.zaplatTrvalePrikazy();
                return true;
            }
        });

        Preference vymazVsetkyData = (Preference) findPreference("deleteAllData");
        vymazVsetkyData.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(@NonNull Preference preference) {
                AlertDialog.Builder potvrdenieBuilder = new AlertDialog.Builder(getActivity());

                potvrdenieBuilder.setPositiveButton("Áno", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        Log.i(TAG, "onClick: mazem vsetky data");

                        MySharedPreferences sharedPreferences = new MySharedPreferences(getContext());
                        sharedPreferences.clearPrefs();
                        DatabaseManager.vymazVsetkyData();

                        AlertDialog.Builder potvrdenieBuilder = new AlertDialog.Builder(getActivity());

                        potvrdenieBuilder.setPositiveButton("OK", null);

                        potvrdenieBuilder.setTitle("Potvrdenie");
                        potvrdenieBuilder.setMessage("Mazanie dát dokončené");

                        potvrdenieBuilder.create().show();

                        Log.i(TAG, "onClick: mazanie dokoncene dokoncene");
                    }
                });

                potvrdenieBuilder.setNegativeButton("Nie", null);

                potvrdenieBuilder.setTitle("Potrdenie");
                potvrdenieBuilder.setMessage("Naozaj si želáte vymazať všetky Vaše dáta? Tento krok je nenávratný.");

                potvrdenieBuilder.create().show();

                return true;
            }
        });

        Preference podpora = (Preference) findPreference("podpora");
        podpora.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(@NonNull Preference preference) {
                AlertDialog.Builder potvrdenieBuilder = new AlertDialog.Builder(getActivity());

                potvrdenieBuilder.setPositiveButton("OK", null);

                potvrdenieBuilder.setTitle("Kontaktovať podporu");
                potvrdenieBuilder.setMessage("Podporu kontaktujete cez email financny.planovac@gmail.com .");

                potvrdenieBuilder.create().show();

                return true;
            }
        });

    }
}
