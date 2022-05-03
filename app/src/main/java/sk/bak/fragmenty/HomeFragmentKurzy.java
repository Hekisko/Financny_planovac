package sk.bak.fragmenty;

import android.os.Bundle;

import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONObject;

import java.util.Calendar;
import java.util.List;
import java.util.Locale;

import sk.bak.R;
import sk.bak.managers.DatabaseManager;
import sk.bak.model.abst.Ucet;
import sk.bak.model.enums.Meny;
import sk.bak.utils.MySharedPreferences;


public class HomeFragmentKurzy extends Fragment {

    private MySharedPreferences sharedPreferences;

    private View currentView;

    private TextView usdHodnota;
    private TextView eurHodnota;
    private TextView czkHodnota;
    private TextView btcHodnota;
    private TextView ethHodnota;
    private TextView porovnavanaMena;


    private Ucet hlavnyUcet;

    private static  final String TAG = "HomeFragmentKurzy";

    public HomeFragmentKurzy() {
        // Required empty public constructor
    }


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        currentView = inflater.inflate(R.layout.fragment_home_kurzy, container, false);

        usdHodnota = currentView.findViewById(R.id.home_fragment_tab_kurzy_usd_hodnota);
        czkHodnota = currentView.findViewById(R.id.home_fragment_tab_kurzy_czk_hodnota);
        btcHodnota = currentView.findViewById(R.id.home_fragment_tab_kurzy_bitcoin_hodnota);
        ethHodnota = currentView.findViewById(R.id.home_fragment_tab_kurzy_etherum_hodnota);
        eurHodnota = currentView.findViewById(R.id.home_fragment_tab_kurzy_eur_hodnota);
        porovnavanaMena = currentView.findViewById(R.id.kuzry_vzhladom_k_mene);

        Log.i(TAG, "onCreateView: inicializujem shared pref");
        sharedPreferences = new MySharedPreferences(getContext());

        updateKurzy();
        updateUI();


        return currentView;
    }

    private void updateUI() {

        Log.i(TAG, "updateUI: zaciatok update ui");

        List<Ucet> ucty = DatabaseManager.getUcty();

        for (Ucet ucet: ucty) {
            if (ucet.isJeHlavnyUcet()) {
                hlavnyUcet = ucet;
                porovnavanaMena.setText(String.format("Prevody sa zobrazujú vzkľadom ku %s", hlavnyUcet.getMena().getMena()));
                Log.i(TAG, "updateUI: mam hlavny ucet");
            }
        }


        float menaUctuEqualsUsd = 1 / sharedPreferences.getFloat(hlavnyUcet != null && hlavnyUcet.isJeHlavnyUcet() ? hlavnyUcet.getMena().getSkratka().toLowerCase(Locale.ROOT) : "eur");
        if (sharedPreferences.getLong("kurzy_update_date") == 0f) {
            Log.i(TAG, "updateUI: nastala chyba pri nacitavani kurzov");
            AlertDialog.Builder chybaDialogBuilder = new AlertDialog.Builder(getContext());

            chybaDialogBuilder.setTitle("Nastala chyba");
            chybaDialogBuilder.setMessage("Nastala chyba pri získavaní aktuálnych kurzov. Ak problém pretrváva, prosím kontaktujte podporu");

            chybaDialogBuilder.setPositiveButton("Ok", null);

            chybaDialogBuilder.create().show();
            return;
        }

        Log.i(TAG, "updateUI: zaciatok nastavovania kurzov");
        usdHodnota.setText(String.format("%.10f", menaUctuEqualsUsd ));
        eurHodnota.setText(String.format("%.10f", menaUctuEqualsUsd * sharedPreferences.getFloat("eur")));
        czkHodnota.setText(String.format("%.10f", menaUctuEqualsUsd * sharedPreferences.getFloat("czk")));

        btcHodnota.setText(String.format("%.10f", menaUctuEqualsUsd * sharedPreferences.getFloat("btc")));
        ethHodnota.setText(String.format("%.10f", menaUctuEqualsUsd * sharedPreferences.getFloat("eth")));
        Log.i(TAG, "updateUI: kutzy nastavene");

    }

    private void updateKurzy() {

        Log.i(TAG, "updateKurzy: zaciatok stahovania kurzov");

        long timePeriodToUpdate = 1000 * 60 * 60 * 48;  // 2 dni

        long poslednyUpdate = sharedPreferences.getLong("kurzy_update_date");
        long aktualnyCas = Calendar.getInstance().getTimeInMillis();
        if (poslednyUpdate == 0 ||
                poslednyUpdate + timePeriodToUpdate < aktualnyCas) {

            RequestQueue queue = Volley.newRequestQueue(getContext());
            String url = "https://openexchangerates.org/api/latest.json?app_id=7b554313ef2f4c7995bcde37a5d31b22&base=USD&show_alternative=true";

            // Request a string response from the provided URL.
            StringRequest stringRequest = new StringRequest(Request.Method.GET, url,
                    new Response.Listener<String>() {
                        @Override
                        public void onResponse(String response) {
                            try {
                                JSONObject jsonAllData = new JSONObject(response);
                                JSONObject jsonRates = jsonAllData.getJSONObject("rates");

                                String jsonAllDataString = jsonAllData.toString();
                                sharedPreferences.saveString("kurzyAllData", jsonAllDataString);

                                float eur = (float) jsonRates.getDouble("EUR");
                                sharedPreferences.saveFloat("eur", eur);

                                float usd = (float) jsonRates.getDouble("USD");
                                sharedPreferences.saveFloat("usd", usd);

                                float btc = (float) jsonRates.getDouble("BTC");
                                sharedPreferences.saveFloat("btc", btc);

                                float eth = (float) jsonRates.getDouble("ETH");
                                sharedPreferences.saveFloat("eth", eth);

                                float czk = (float) jsonRates.getDouble("CZK");
                                sharedPreferences.saveFloat("czk", czk);

                                sharedPreferences.saveLong("kurzy_update_date", aktualnyCas);
                                updateUI();

                                Log.i(TAG, "onResponse: kurzy uspecne stiahnute");
                            } catch (Exception ex) {
                                Log.i("Nastala chyba pri konverzii", ex.getMessage());
                            }

                        }
                    }, new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError error) {
                    AlertDialog.Builder chybaDialogBuilder = new AlertDialog.Builder(getContext());

                    chybaDialogBuilder.setTitle("Nastala chyba");
                    chybaDialogBuilder.setMessage("Nastala chyba pri získavaní aktuálnych kurzov. Ak problém pretrváva, prosím kontaktujte podporu");

                    chybaDialogBuilder.setPositiveButton("Ok", null);

                    chybaDialogBuilder.create().show();
                }
            });

            queue.add(stringRequest);


        }

    }
}