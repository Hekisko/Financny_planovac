package sk.bak.activity;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseAuth;

import java.util.Random;

import sk.bak.R;


/**
 *
 * Trieda pre obrazovku načítavania
 *
 */
public class MainActivity extends AppCompatActivity {

    private static final String TAG = "Main Activity";

    // UI komponenty
    private TextView citat;
    private TextView autor;

    // Pomocne premenne
    private Activity activity;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Log.i(TAG, "onCreate: starting app");

        activity = this;

        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);

        citat = findViewById(R.id.main_menu_item_citaty);
        autor = findViewById(R.id.main_menu_item_citaty_autor);

        int randomCislo = new Random().nextInt(55);
        String[] citatText = getResources().getStringArray(R.array.citaty);
        String[] citatAutor = getResources().getStringArray(R.array.autory);

        citat.setText(citatText[randomCislo]);
        autor.setText(citatAutor[randomCislo]);

        Handler handler = new Handler();
        Runnable runnable = new Runnable() {
            @Override
            public void run() {

                FirebaseAuth mAuth = FirebaseAuth.getInstance();

                if (mAuth.getCurrentUser() != null) {
                    Log.i(TAG, "onCreate: mam usera, spustam aplikaciu");
                    Intent menu = new Intent(activity, MainMenu.class);
                    startActivity(menu);
                    activity.finish();
                } else {
                    Log.i(TAG, "run: nemam usera, spustam autentifikaciu");
                    Intent auth = new Intent(activity, AuthActivity.class);
                    startActivity(auth);
                    activity.finish();
                }


            }
        };

        handler.postDelayed(runnable, 4000);

    }
}