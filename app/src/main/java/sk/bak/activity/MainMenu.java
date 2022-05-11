package sk.bak.activity;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.navigation.NavigationBarView;
import com.google.firebase.auth.FirebaseAuth;

import sk.bak.fragmenty.HomeFragment;
import sk.bak.fragmenty.NastaveniaFragment;
import sk.bak.fragmenty.PrehladyFragment;
import sk.bak.dialogs.PridajZaznamDialog;
import sk.bak.R;
import sk.bak.fragmenty.UctyFragment;
import sk.bak.managers.DatabaseManager;
import sk.bak.utils.Constants;
import sk.bak.utils.MySharedPreferences;
import sk.bak.utils.SecurityCheck;
import sk.bak.utils.Utils;

public class MainMenu extends AppCompatActivity {

    private BottomNavigationView bottomNavigationView;
    private int aktualneZvolenyFragmentID = R.id.main_menu_navigation_domov;

    private FloatingActionButton pridajZaznam;

    private BroadcastReceiver changeFragmentReceiver;

    private static final String TAG = "Main menu";

    private MySharedPreferences sharedPreferences;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_menu);
        Log.i(TAG, "onCreate: spustam main menu");


        Log.i(TAG, "onCreate: inicializujem shared pref Start");
        PackageInfo packageInfoSingleton = null;
        try {
            packageInfoSingleton = getApplicationContext().getPackageManager().getPackageInfo(getApplicationContext().getPackageName(), 0);
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
        Constants.PREF_NAME = packageInfoSingleton.packageName;
        sharedPreferences = new MySharedPreferences(getApplicationContext());
        Utils.nacitajKurzy(sharedPreferences, getApplicationContext());
        Log.i(TAG, "onCreate: inicializujem shared pref DONE");


        Log.i(TAG, "onCreate: inicializujem db START");
        DatabaseManager.initDbManager(FirebaseAuth.getInstance().getCurrentUser(), getApplicationContext());
        Log.i(TAG, "onCreate: inicializujem db DONE");


        findViewById(R.id.main_menu_screen).setVisibility(View.INVISIBLE);

        Log.i(TAG, "onCreate: starting securuty check");
        SecurityCheck securityCheck = new SecurityCheck(this, R.id.main_menu_screen, getApplicationContext());
        securityCheck.isDeviceSecured();

        Fragment prvyFragment = new HomeFragment();

        getSupportFragmentManager().beginTransaction().replace(R.id.main_menu_frame_layout, prvyFragment).commit();

        pridajZaznam = findViewById(R.id.main_menu_pridaj_zaznam);

        pridajZaznam.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                PridajZaznamDialog pridajZaznamDialog = new PridajZaznamDialog(MainMenu.this);

                pridajZaznamDialog.show();
            }
        });

        bottomNavigationView = findViewById(R.id.main_menu_bottom_icons);
        bottomNavigationView.setBackground(null);
        ColorStateList myColorStateList = new ColorStateList(
                new int[][]{
                        new int[]{-android.R.attr.state_checked}, //1
                        new int[]{android.R.attr.state_checked}, //2
                },
                new int[] {
                        Color.GRAY, //1
                        Color.BLACK, //2
                }
        );
        bottomNavigationView.setItemIconTintList(myColorStateList);
        //bottomNavigationView.getMenu().getItem(0).setCheckable(false);

        bottomNavigationView.setOnItemSelectedListener(new NavigationBarView.OnItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {

                Fragment selectedFragment = null;

                if (item.getItemId() == aktualneZvolenyFragmentID) {
                    return false;
                }

                switch (item.getItemId()) {
                    case R.id.main_menu_navigation_domov:
                        selectedFragment = new HomeFragment();
                        break;
                    case R.id.main_menu_navigation_ucty:
                        selectedFragment = new UctyFragment();
                        break;
                    case R.id.main_menu_navigation_prehlady:
                        selectedFragment = new PrehladyFragment();
                        break;
                    case R.id.main_menu_navigation_nastavenia:
                        selectedFragment = new NastaveniaFragment();
                        break;
                }

                aktualneZvolenyFragmentID = item.getItemId();

                getSupportFragmentManager().beginTransaction().replace(R.id.main_menu_frame_layout, selectedFragment).commit();

                return true;
            }
        });

        changeFragmentReceiver = new FragmentToParentActivityChangeFragment();
        IntentFilter intentSFilter = new IntentFilter(Constants.CHANGE_FRAGMENT);
        registerReceiver(changeFragmentReceiver, intentSFilter);
    }



    public class FragmentToParentActivityChangeFragment extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {


            /*if (intent.getBooleanExtra(Constants.CREATE_NEW_ACCOUNT, false)) {
                Toast.makeText(getApplicationContext(), "aaaaaaaaaa", Toast.LENGTH_SHORT).show();

                aktualneZvolenyFragmentID = R.id.main_menu_frame_layout;

                getSupportFragmentManager().beginTransaction().replace(R.id.main_menu_frame_layout, new PridajNovyUcet()).commit();



            }

             */
        }
    }

}