package sk.bak.dialogs;

import android.app.Activity;
import android.app.Dialog;
import android.content.ClipData;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.DragEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.CheckBox;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.core.content.ContextCompat;

import com.google.android.material.tabs.TabLayout;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import sk.bak.R;
import sk.bak.fragmenty.UctyFragment;
import sk.bak.managers.DatabaseManager;
import sk.bak.model.BeznyUcet;
import sk.bak.model.CryptoUcet;
import sk.bak.model.Prijem;
import sk.bak.model.SporiaciUcet;
import sk.bak.model.Vydaj;
import sk.bak.model.abst.Ucet;
import sk.bak.model.abst.VlozenyZaznam;
import sk.bak.model.enums.Meny;
import sk.bak.model.enums.TypVydaju;
import sk.bak.model.enums.TypZaznamu;
import sk.bak.model.enums.TypyUctov;
import sk.bak.utils.MySharedPreferences;
import sk.bak.utils.Utils;

/**
 *
 * Dialog pre pridanie zaznamu
 *
 */
public class PridajZaznamDialog extends Dialog {

    private static final String TAG = "PridajZaznamDialog";

    //Pomocne premenne
    private Dialog thisDialog;
    private boolean isTransfer;
    private MySharedPreferences sharedPreferences;
    private Activity parentActivity;

    // UI premenne
    private CardView ucet1;
    private TextView ucet1_text;
    private CardView ucet2;
    private TextView ucet2_text;
    private CardView ucet3;
    private TextView ucet3_text;
    private CardView ucet4;
    private TextView ucet4_text;
    private CardView ucet5;
    private TextView ucet5_text;
    private CardView[] zoznamCardView = {ucet1, ucet2, ucet3, ucet4, ucet5};
    private TextView[] zoznamNazvovUctov = {ucet1_text, ucet2_text, ucet3_text, ucet4_text, ucet5_text};
    private CardView prijem;
    private CardView utrata;
    private CardView transfer;
    private TextInputLayout suma;
    private TextInputLayout mena;
    private CheckBox trvalyPrikaz;
    private TextInputLayout trvylayPrikazSplatnost;
    private CardView jedlo;
    private CardView cestovanie;
    private CardView elektro;
    private CardView sport;
    private CardView auto;
    private CardView rodina;
    private CardView hry;
    private CardView oblecenie;
    private CardView animal;
    private CardView house;
    private CardView drogeria;
    private CardView ostatne;
    private LinearLayout ucelLine1;
    private LinearLayout ucelLine2;
    private LinearLayout ucelLine3;
    private View ucelSeparator;
    private ImageButton back;
    private ImageButton saveZaznam;
    private TextInputLayout poznamka;
    private TextView ziadneUcty;


    // Datove premenne
    private CardView[] selectedUcetZaznamu = {null};
    private CardView[] selectedUcetPrevodZ = {null};
    private CardView[] selectedUcetPrevodNa = {null};
    private CardView[] selectedTypZaznamu = {null};
    private Meny menaZaznamu;
    private int denSplatnosti = 0;
    private TypVydaju typVydaju;
    private CardView[] selectedUcelZaznamu = {null};



    // Listenery
    private View.OnClickListener uctyListner;
    private View.OnClickListener typZaznamuListener;
    private View.OnClickListener iconyListner;

    public PridajZaznamDialog(Activity activity) {
        super(activity);

        parentActivity = activity;

    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        final View customLayout = getLayoutInflater().inflate(R.layout.dialog_pridaj_zaznam, null);
        setContentView(customLayout);

        DisplayMetrics displayMetrics = new DisplayMetrics();
        parentActivity.getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
        int displayWidth = displayMetrics.widthPixels;
        WindowManager.LayoutParams layoutParams = new WindowManager.LayoutParams();
        layoutParams.copyFrom(getWindow().getAttributes());
        int dialogWindowWidth = (int) (displayWidth * 0.99f);
        layoutParams.width = dialogWindowWidth;
        getWindow().setAttributes(layoutParams);


        sharedPreferences = new MySharedPreferences(getContext());
        thisDialog = this;

        ziadneUcty = findViewById(R.id.pridaj_zaznam_ziadne_ucty);
        ziadneUcty.setVisibility(View.GONE);


        initListnery();
        initUcty();
        initTypZaznamu();
        initTrvalyPrikaz();
        initInputSumy();
        initIcony();
        initTlacidla();

        // Skrytie filtru
        ucelLine1.setVisibility(View.GONE);
        ucelLine2.setVisibility(View.GONE);
        ucelLine3.setVisibility(View.GONE);
        ucelSeparator.setVisibility(View.GONE);

    }

    /**
     *
     * Pomocna metona na init listenerov
     *
     */
    private void initListnery() {

        uctyListner = new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (isTransfer) {
                    if (selectedUcetPrevodZ[0] != null) {

                        selectedUcetPrevodZ[0].setBackgroundTintList(ColorStateList.valueOf(Color.WHITE));
                        selectedUcetPrevodZ[0] = null;
                        selectedUcetPrevodNa[0].setBackgroundTintList(ColorStateList.valueOf(Color.WHITE));
                        selectedUcetPrevodNa[0] = null;

                    }
                }

                isTransfer = false;
                transfer.setBackgroundTintList(ColorStateList.valueOf(Color.WHITE));

                if (selectedUcetZaznamu[0] != null) {

                    selectedUcetZaznamu[0].setBackgroundTintList(ColorStateList.valueOf(Color.WHITE));

                    if (selectedUcetZaznamu[0] == v) {
                        selectedUcetZaznamu[0] = null;
                    } else {
                        selectedUcetZaznamu[0] = (CardView) v;
                        v.setBackgroundTintList(ColorStateList.valueOf(ContextCompat.getColor(getContext(),R.color.blue_600)));
                    }
                } else {
                    selectedUcetZaznamu[0] = (CardView) v;
                    v.setBackgroundTintList(ColorStateList.valueOf(ContextCompat.getColor(getContext(),R.color.blue_600)));
                }
            }
        };

        typZaznamuListener = new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (isTransfer) {
                    if (selectedUcetPrevodZ[0] != null) {

                        selectedUcetPrevodZ[0].setBackgroundTintList(ColorStateList.valueOf(Color.WHITE));
                        selectedUcetPrevodZ[0] = null;
                        selectedUcetPrevodNa[0].setBackgroundTintList(ColorStateList.valueOf(Color.WHITE));
                        selectedUcetPrevodNa[0] = null;
                    }
                }

                isTransfer = false;
                transfer.setBackgroundTintList(ColorStateList.valueOf(Color.WHITE));

                if (v.getId() == R.id.pridaj_zaznam_utrata_card) {

                    ucelLine1.setVisibility(View.VISIBLE);
                    ucelLine2.setVisibility(View.VISIBLE);
                    ucelLine3.setVisibility(View.VISIBLE);
                    ucelSeparator.setVisibility(View.VISIBLE);

                } else if (v.getId() == R.id.pridaj_zaznam_prijem_card) {

                    ucelLine1.setVisibility(View.GONE);
                    ucelLine2.setVisibility(View.GONE);
                    ucelLine3.setVisibility(View.GONE);
                    ucelSeparator.setVisibility(View.GONE);
                }

                if (selectedTypZaznamu[0] != null) {

                    selectedTypZaznamu[0].setBackgroundTintList(ColorStateList.valueOf(Color.WHITE));

                    if (selectedTypZaznamu[0] == v) {
                        selectedTypZaznamu[0] = null;
                    } else {
                        selectedTypZaznamu[0] = (CardView) v;
                        v.setBackgroundTintList(ColorStateList.valueOf(ContextCompat.getColor(getContext(),R.color.blue_600)));
                    }
                } else {
                    selectedTypZaznamu[0] = (CardView) v;
                    v.setBackgroundTintList(ColorStateList.valueOf(ContextCompat.getColor(getContext(),R.color.blue_600)));
                }


            }
        };

        iconyListner = new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                switch (v.getId()) {
                    case R.id.pridaj_zaznam_ucel_jedlo_card:
                        typVydaju = TypVydaju.STRAVA;
                        break;
                    case R.id.pridaj_zaznam_ucel_cestovanie_card:
                        typVydaju = TypVydaju.CESTOVANIE;
                        break;
                    case R.id.pridaj_zaznam_ucel_elektro_card:
                        typVydaju = TypVydaju.ELEKTRO;
                        break;
                    case R.id.pridaj_zaznam_ucel_sport_card:
                        typVydaju = TypVydaju.SPORT;
                        break;
                    case R.id.pridaj_zaznam_ucel_car_card:
                        typVydaju = TypVydaju.DOPRAVA;
                        break;
                    case R.id.pridaj_zaznam_ucel_family_card:
                        typVydaju = TypVydaju.RODINA;
                        break;
                    case R.id.pridaj_zaznam_ucel_games_card:
                        typVydaju = TypVydaju.ZAVABA;
                        break;
                    case R.id.pridaj_zaznam_ucel_cloth_card:
                        typVydaju = TypVydaju.OBLECENIE;
                        break;
                    case R.id.pridaj_zaznam_ucel_animal_card:
                        typVydaju = TypVydaju.ANIMAL;
                        break;
                    case R.id.pridaj_zaznam_ucel_house_card:
                        typVydaju = TypVydaju.HOUSE;
                        break;
                    case R.id.pridaj_zaznam_ucel_drogeria_card:
                        typVydaju = TypVydaju.DROGERIA;
                        break;
                    case R.id.pridaj_zaznam_ucel_ostatne_card:
                        typVydaju = TypVydaju.OSTATNE;
                        break;
                }

                if (selectedUcelZaznamu[0] != null) {

                    selectedUcelZaznamu[0].setBackgroundTintList(ColorStateList.valueOf(Color.WHITE));

                    if (selectedUcelZaznamu[0] == v) {
                        selectedUcelZaznamu[0] = null;
                    } else {
                        selectedUcelZaznamu[0] = (CardView) v;
                        v.setBackgroundTintList(ColorStateList.valueOf(ContextCompat.getColor(getContext(),R.color.blue_600)));
                    }
                } else {
                    selectedUcelZaznamu[0] = (CardView) v;
                    v.setBackgroundTintList(ColorStateList.valueOf(ContextCompat.getColor(getContext(),R.color.blue_600)));
                }
            }
        };


    }

    /**
     *
     * Pomocna metona pre init casti Trvaly Prikaz
     *
     */
    private void initTrvalyPrikaz() {

        trvalyPrikaz = findViewById(R.id.pridaj_zaznam_trvaly_prikaz);
        trvylayPrikazSplatnost = findViewById(R.id.pridaj_zaznam_splatnost_layout);


        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(getContext(),
                R.array.dni_pre_splatnost, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        ((AutoCompleteTextView)trvylayPrikazSplatnost.getEditText()).setAdapter(adapter);

        ((AutoCompleteTextView)trvylayPrikazSplatnost.getEditText()).setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                String denSplatnostiString = parent.getItemAtPosition(position).toString();

                denSplatnosti = Integer.parseInt(denSplatnostiString);

            }
        });

    }

    /**
     *
     * Pomocna metoda pre init tlacidiel
     *
     */
    private void initTlacidla() {

        back = findViewById(R.id.pridaj_zaznam_spat);
        back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                thisDialog.cancel();
            }
        });


        saveZaznam = findViewById(R.id.pridaj_zaznam_uloz);

        saveZaznam.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {


                if (isTransfer) {
                    savaToDB(selectedUcetPrevodZ[0], utrata);
                    savaToDB(selectedUcetPrevodNa[0], prijem);
                } else {
                    savaToDB(selectedUcetZaznamu[0], selectedTypZaznamu[0]);

                }

            }
        });
    }

    /**
     *
     * Pomocna metoda na validovanie inputu
     *
     * @return
     */
    private boolean validateInput() {

        boolean result = true;

        if (selectedUcetZaznamu[0] == null && !isTransfer) {
            Toast.makeText(getContext(), "Musí byť zvolený účet", Toast.LENGTH_SHORT).show();
            result = false;
        } else if (selectedTypZaznamu[0] == null) {
            Toast.makeText(getContext(), "Musí byť zvolený príjem, výdaj alebo prevod", Toast.LENGTH_SHORT).show();
            result = false;
        }

        if (suma.getEditText().getText().toString().equals("")) {
            suma.setError("Musí byť určená suma");
            result = false;
        } else {
            suma.setError(null);
        }

        if (menaZaznamu == null) {
            mena.setError("Zvoľte menu");
            result = false;
        } else {
            mena.setError(null);
        }

        if (trvalyPrikaz.isChecked() && denSplatnosti == 0) {
            trvylayPrikazSplatnost.setError("Zvoľte deň splatnosti");
            result = false;
        } else {
            trvylayPrikazSplatnost.setError(null);
        }

        return result;
    }


    /**
     *
     * Pomocna metoda pre init ikon
     *
     */
    private void initIcony() {


        ucelLine1 = findViewById(R.id.pridaj_zaznam_ucel_line1_layout);
        ucelLine2 = findViewById(R.id.pridaj_zaznam_ucel_line2_layout);
        ucelLine3 = findViewById(R.id.pridaj_zaznam_ucel_line3_layout);
        ucelSeparator = findViewById(R.id.pridaj_zaznam_ucel_sepator);

        jedlo = findViewById(R.id.pridaj_zaznam_ucel_jedlo_card);
        jedlo.setOnClickListener(iconyListner);

        cestovanie = findViewById(R.id.pridaj_zaznam_ucel_cestovanie_card);
        cestovanie.setOnClickListener(iconyListner);

        elektro = findViewById(R.id.pridaj_zaznam_ucel_elektro_card);
        elektro.setOnClickListener(iconyListner);

        sport = findViewById(R.id.pridaj_zaznam_ucel_sport_card);
        sport.setOnClickListener(iconyListner);

        auto = findViewById(R.id.pridaj_zaznam_ucel_car_card);
        auto.setOnClickListener(iconyListner);

        rodina = findViewById(R.id.pridaj_zaznam_ucel_family_card);
        rodina.setOnClickListener(iconyListner);

        hry = findViewById(R.id.pridaj_zaznam_ucel_games_card);
        hry.setOnClickListener(iconyListner);

        oblecenie = findViewById(R.id.pridaj_zaznam_ucel_cloth_card);
        oblecenie.setOnClickListener(iconyListner);

        animal = findViewById(R.id.pridaj_zaznam_ucel_animal_card);
        animal.setOnClickListener(iconyListner);

        house = findViewById(R.id.pridaj_zaznam_ucel_house_card);
        house.setOnClickListener(iconyListner);

        drogeria = findViewById(R.id.pridaj_zaznam_ucel_drogeria_card);
        drogeria.setOnClickListener(iconyListner);

        ostatne = findViewById(R.id.pridaj_zaznam_ucel_ostatne_card);
        ostatne.setOnClickListener(iconyListner);

    }


    /**
     *
     * Pomocna metoda pre init casti suma
     *
     */
    private void initInputSumy() {

        poznamka = findViewById(R.id.pridaj_zaznam_poznamka_layout);
        suma = findViewById(R.id.pridaj_zaznam_suma_layout);
        initSpinnerMenaUctu();
    }

    /**
     *
     * Pomocna metoda pre init casti typZaznamu
     *
     */
    private void initTypZaznamu() {

        prijem = findViewById(R.id.pridaj_zaznam_prijem_card);
        prijem.setOnClickListener(typZaznamuListener);

        utrata = findViewById(R.id.pridaj_zaznam_utrata_card);
        utrata.setOnClickListener(typZaznamuListener);

        transfer = findViewById(R.id.pridaj_zaznam_transfer_card);
        transfer.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(getContext(), "Pre transfer potiahnite jeden účet na druhý", Toast.LENGTH_LONG).show();
            }
        });
    }

    /**
     *
     * Pomocna metoda pre init zobrazenia uctov
     *
     */
    private void initUcty() {

        List<Ucet> aktivneUcty = DatabaseManager.getUcty();

        MyOnDragListener myOnDragListener = new MyOnDragListener();
        MyOnTouchListener myOnTouchListener = new MyOnTouchListener();
        MyOnLongClickListener myOnLongClickListener = new MyOnLongClickListener();

        ucet1 = findViewById(R.id.pridaj_zaznam_ucet_1_layout);
        ucet1.setOnClickListener(uctyListner);
        ucet1_text = findViewById(R.id.pridaj_zaznam_ucet_1_text_view);
        ucet1.setOnDragListener(myOnDragListener);
        ucet1.setOnLongClickListener(myOnLongClickListener);
        //ucet1.setOnTouchListener(myOnTouchListener);

        ucet2 = findViewById(R.id.pridaj_zaznam_ucet_2_layout);
        ucet2.setOnClickListener(uctyListner);
        ucet2_text = findViewById(R.id.pridaj_zaznam_ucet_2_text_view);
        ucet2.setOnDragListener(myOnDragListener);
        ucet2.setOnLongClickListener(myOnLongClickListener);

        ucet3 = findViewById(R.id.pridaj_zaznam_ucet_3_layout);
        ucet3.setOnClickListener(uctyListner);
        ucet3_text = findViewById(R.id.pridaj_zaznam_ucet_3_text_view);
        ucet3.setOnDragListener(myOnDragListener);
        ucet3.setOnLongClickListener(myOnLongClickListener);

        ucet4 = findViewById(R.id.pridaj_zaznam_ucet_4_layout);
        ucet4.setOnClickListener(uctyListner);
        ucet4_text = findViewById(R.id.pridaj_zaznam_ucet_4_text_view);
        ucet4.setOnDragListener(myOnDragListener);
        ucet4.setOnLongClickListener(myOnLongClickListener);

        ucet5 = findViewById(R.id.pridaj_zaznam_ucet_5_layout);
        ucet5.setOnClickListener(uctyListner);
        ucet5_text = findViewById(R.id.pridaj_zaznam_ucet_5_text_view);
        ucet5.setOnDragListener(myOnDragListener);
        ucet5.setOnLongClickListener(myOnLongClickListener);

        List<CardView> layouty = new ArrayList<>(Arrays.asList(ucet1, ucet2, ucet3, ucet4, ucet5));
        List<TextView> textViews = new ArrayList<>(Arrays.asList(ucet1_text, ucet2_text, ucet3_text, ucet4_text, ucet5_text));


        if (aktivneUcty.size() == 0) {
            ziadneUcty.setVisibility(View.VISIBLE);
        } else {
            ziadneUcty.setVisibility(View.GONE);
        }

        for (int i = 0; i < 5; i++) {

            if (i < aktivneUcty.size()) {

                layouty.get(i).setVisibility(View.VISIBLE);
                textViews.get(i).setText(aktivneUcty.get(i).getNazov());

            } else {

                layouty.get(i).setVisibility(View.GONE);
                textViews.get(i).setText("");

            }
        }
    }


    /**
     *
     * Trida potreb pre pohyb s UI komponentami
     *
     */
    private class MyOnDragListener implements View.OnDragListener {

        @Override
        public boolean onDrag(View v, DragEvent event) {
            switch (event.getAction()) {
                case DragEvent.ACTION_DROP:


                    if (selectedUcetZaznamu[0] != null) {
                        selectedUcetZaznamu[0].setBackgroundTintList(ColorStateList.valueOf(Color.WHITE));
                        selectedUcetZaznamu[0] = null;
                    }


                    if (selectedTypZaznamu[0] != null) {
                        selectedTypZaznamu[0].setBackgroundTintList(ColorStateList.valueOf(Color.WHITE));
                        selectedTypZaznamu[0] = null;
                    }

                    transfer.setBackgroundTintList(ColorStateList.valueOf(ContextCompat.getColor(getContext(),R.color.blue_600)));

                    isTransfer = true;
                    selectedTypZaznamu[0] = transfer;

                    selectedUcetPrevodNa[0] = (CardView) v;
                    selectedUcetPrevodNa[0].setBackgroundTintList(ContextCompat.getColorStateList(getContext(), R.color.green_400));

                    selectedUcetPrevodZ[0] = (CardView) event.getLocalState();
                    selectedUcetPrevodZ[0].setBackgroundTintList(ContextCompat.getColorStateList(getContext(), R.color.red_400));


                    ucelLine1.setVisibility(View.VISIBLE);
                    ucelLine2.setVisibility(View.VISIBLE);
                    ucelLine3.setVisibility(View.VISIBLE);
                    ucelSeparator.setVisibility(View.VISIBLE);
            }
            return true;
        }
    }

    /**
     *
     * Pomocna metoda pre ulozenie zaznamu do databazy
     *
     * @param selectedUcetSave
     * @param selectedTypSave
     */
    private void savaToDB(CardView selectedUcetSave,
                          CardView selectedTypSave) {

        Log.i(TAG, "savaToDB: zacinam ukladanie do DB");

        if (!validateInput()) {
            return;
        }

        Log.i(TAG, "savaToDB: po validacii inputu");

        Meny menaZvolenehoUctu = getMenaUctu(selectedUcetSave);

        if (menaZvolenehoUctu == null) {
            Toast.makeText(getContext(), "Niečo sa pokazilo. Akciu opakujte neskôr.", Toast.LENGTH_SHORT).show();
            return;
        }

        if (sharedPreferences.getLong("kurzy_update_date") == 0) {
            Utils.nacitajKurzy(sharedPreferences, getContext());
            Toast.makeText(getContext(), "Chvíľu strpenia. Sťahujem dáta pre kurzy. Pokus opakujte neskôr.", Toast.LENGTH_SHORT).show();
            return;
        }

        Double sumaVMeneUctu = transferIfNecessery(Double.parseDouble(suma.getEditText().getText().toString()), menaZvolenehoUctu, menaZaznamu);

        BigDecimal bd;

        if (menaZvolenehoUctu != Meny.BTC && menaZvolenehoUctu != Meny.ETH) {
            bd = new BigDecimal(sumaVMeneUctu).setScale(2, RoundingMode.HALF_UP);
        } else {
            bd = new BigDecimal(sumaVMeneUctu).setScale(10, RoundingMode.HALF_UP);
        }

        String nazovUctu = (String) ((TextView)selectedUcetSave.getChildAt(0)).getText();
        VlozenyZaznam zaznam;

        int denSplatnostiHelp = -1;

        if (trvalyPrikaz.isChecked()) {
            denSplatnostiHelp = denSplatnosti;
        }

        if (selectedTypSave.getId() == R.id.pridaj_zaznam_prijem_card) {
            zaznam = new Prijem(null, bd.doubleValue(), menaZvolenehoUctu, Calendar.getInstance().getTime(), null, nazovUctu, denSplatnostiHelp, poznamka.getEditText().getText().toString());
        } else {
            if (typVydaju == null) {
                typVydaju = TypVydaju.OSTATNE;
            }
            zaznam = new Vydaj(typVydaju, bd.doubleValue(), menaZvolenehoUctu, Calendar.getInstance().getTime(), null, nazovUctu, denSplatnostiHelp, poznamka.getEditText().getText().toString());
        }

        Log.i(TAG, "savaToDB: zacina ukladanie do db");
        if (trvalyPrikaz.isChecked()) {
            zaznam.setSuma(Double.parseDouble(suma.getEditText().getText().toString()));
            zaznam.setMena(menaZaznamu);
            DatabaseManager.saveTrvalyPrikaz(nazovUctu, zaznam);
            Log.i(TAG, "savaToDB: pridavam ako trvaly prikaz");
        } else {

            DatabaseManager.saveZaznam(nazovUctu, zaznam);
            Log.i(TAG, "savaToDB: pridavam ako obycajny zaznamy");

            if (selectedTypSave.getId() == R.id.pridaj_zaznam_prijem_card) {
                DatabaseManager.addSumaToUcet(nazovUctu, bd.doubleValue());
                Log.i(TAG, "savaToDB: pridavam sumu na ucet");

            } else {
                DatabaseManager.addSumaToUcet(nazovUctu, -bd.doubleValue());
                Log.i(TAG, "savaToDB: odpocitavam sumu z uctu");
            }

        }
        Log.i(TAG, "savaToDB: konci ukladanie do db");

        thisDialog.cancel();
    }


    /**
     *
     * Pomocna metoda pre tranfer medzi menami
     *
     * @param sumaVoZvolenejMene
     * @param menaZvolenehoUctu
     * @param menaZaznamu
     * @return
     */
    private double transferIfNecessery(Double sumaVoZvolenejMene, Meny menaZvolenehoUctu, Meny menaZaznamu) {

        if (menaZaznamu.getMena().equals(menaZvolenehoUctu.getMena())) {
            return sumaVoZvolenejMene;
        }

        double sumaAkoUSD = sumaVoZvolenejMene / Double.parseDouble(String.valueOf(sharedPreferences.getFloat(menaZaznamu.getSkratka().toLowerCase(Locale.ROOT))));

        double vyslednaSuma = sumaAkoUSD * Double.parseDouble(String.valueOf(sharedPreferences.getFloat(menaZvolenehoUctu.getSkratka().toLowerCase(Locale.ROOT))));

        return vyslednaSuma;

    }


    /**
     *
     * Pomocna metoda pre ziskanie meny zvolene uctu
     *
     * @param selectedUcetSave
     * @return
     */
    private Meny getMenaUctu(CardView selectedUcetSave) {

        String nazovUctu = (String) ((TextView)selectedUcetSave.getChildAt(0)).getText();
        List<Ucet> ucty = DatabaseManager.getUcty();

        for (Ucet ucet: ucty) {
            if (ucet.getNazov().equals(nazovUctu)) {
                return ucet.getMena();
            }
        }

        return null;
    }


    /**
     *
     * Trieda pre ucty co sa ma stat po dlhom stlaceni
     *
     */
    private class MyOnLongClickListener implements View.OnLongClickListener {

        @Override
        public boolean onLongClick(View v) {
            ClipData clipData = ClipData.newPlainText(String.valueOf(v.getId()), "");
            View.DragShadowBuilder builder = new View.DragShadowBuilder(v);
            v.startDragAndDrop(clipData, builder, v, 0);
            return true;
        }
    }


    /**
     *
     * Trieda pre ucty co sa ma stat po kliknuti na ne
     *
     */
    private class MyOnTouchListener implements View.OnTouchListener {

        @Override
        public boolean onTouch(View v, MotionEvent event) {
            ClipData clipData = ClipData.newPlainText(String.valueOf(v.getId()), "");
            View.DragShadowBuilder builder = new View.DragShadowBuilder(v);
            v.startDragAndDrop(clipData, builder, v, 0);
            return true;
        }
    }

    /**
     *
     * Pomocna trieda pre init spinneru pre menu
     *
     */
    private void initSpinnerMenaUctu() {

        mena = findViewById(R.id.pridaj_zaznam_mena_layout);

        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(getContext(),
                R.array.meny_uctov_skratka, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        ((AutoCompleteTextView)mena.getEditText()).setAdapter(adapter);

        ((AutoCompleteTextView)mena.getEditText()).setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                String mena = parent.getItemAtPosition(position).toString();


                switch (mena) {
                    case "EUR":
                        menaZaznamu = Meny.EUR;
                        break;
                    case "USD":
                        menaZaznamu = Meny.USD;
                        break;
                    case "CZK":
                        menaZaznamu = Meny.CZK;
                        break;
                    case "BTC":
                        menaZaznamu = Meny.BTC;
                        break;
                    case "ETH":
                        menaZaznamu = Meny.ETH;
                        break;
                }
            }
        });
    }

}
