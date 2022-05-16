package sk.bak.utils;

import android.content.Context;
import android.util.Log;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONObject;

import java.util.Calendar;


/**
 *
 * Trieda pre pomocne metody potrebne v celej aplikácii
 *
 */
public class Utils {


    /**
     *
     * Metóda na sťahovanie kurzov
     *
     * @param sharedPreferences
     * @param context
     */
    public static void nacitajKurzy(MySharedPreferences sharedPreferences, Context context) {

        long timePeriodToUpdate = 1000 * 60 * 60 * 48;  // 2 dni

        long poslednyUpdate = sharedPreferences.getLong("kurzy_update_date");
        long aktualnyCas = Calendar.getInstance().getTimeInMillis();
        if (poslednyUpdate == 0 ||
                poslednyUpdate + timePeriodToUpdate < aktualnyCas) {

            RequestQueue queue = Volley.newRequestQueue(context);
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

                            } catch (Exception ex) {
                                Log.i("Nastala chyba pri konverzii", ex.getMessage());
                            }

                        }
                    }, new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError error) {
                }
            });

            queue.add(stringRequest);


        }

    }

}
