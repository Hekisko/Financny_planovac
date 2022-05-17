package sk.bak.fragmenty;

import static android.view.View.GONE;

import android.app.AlertDialog;
import android.graphics.Color;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import android.service.notification.ZenPolicy;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.ScrollView;
import android.widget.TextSwitcher;
import android.widget.TextView;

import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.components.AxisBase;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.formatter.IAxisValueFormatter;
import com.github.mikephil.charting.formatter.ValueFormatter;
import com.github.mikephil.charting.utils.ViewPortHandler;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;

import org.w3c.dom.Text;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.TreeMap;
import java.util.stream.Collectors;

import sk.bak.R;
import sk.bak.managers.DatabaseManager;
import sk.bak.model.BeznyUcet;
import sk.bak.model.CryptoUcet;
import sk.bak.model.Prijem;
import sk.bak.model.SporiaciUcet;
import sk.bak.model.TrvalyPrikaz;
import sk.bak.model.Vydaj;
import sk.bak.model.abst.Ucet;
import sk.bak.model.abst.VlozenyZaznam;
import sk.bak.model.enums.Meny;
import sk.bak.model.enums.TypZaznamu;
import sk.bak.model.enums.TypyUctov;
import sk.bak.utils.MySharedPreferences;
import sk.bak.utils.Utils;


/**
 *
 * Trieda fragmentu rychly prehlad
 *
 */
public class HomeFragmentRychlyPrehlad extends Fragment {

    private static final String TAG = "HomeFragmentRychlyPrehlad";

    // UI premenne
    private BarChart barChart;
    private ScrollView scrollView;
    private TextView upozornenie;
    private TextView nacitavanieText;
    private ProgressBar nacitavanieProgressBar;
    private TextView celkovyZostatok;
    private TextView dnesnyDen;
    private TextView tyzden;
    private TextView mesiac;

    // Pomocne premenne
    private View currentView;
    private MySharedPreferences sharedPreferences;


    // Datove premenne
    private int[] poslednych7Dni = new int[7];
    private List<String> dniTyzdnaNaX;
    private List<Ucet> ucty;
    private BeznyUcet hlavnyUcet;
    private Map<Integer, List<Double>> sumyMinuteZaPoslednych7Dni;
    private List<Prijem> zaznamyPrijem;
    private List<Vydaj> zaznamyVydaj;
    private Double vydajeCelkovo = .0;
    private Double prijmyCelkovo = .0;
    private Double chcemaUsetrenaSumaCelkovo = .0;
    private Double zostatokCelkovo = .0;
    private Meny zvolenaMena;


    // Listenery db
    private ValueEventListener celkovyListener;
    private ValueEventListener listenerNaUcty;
    private ValueEventListener listenerNaTrvalePrikazy;
    private ValueEventListener listenerNaZaznamyAktualnyMesiac;
    private ValueEventListener listenerNaZaznamyMinulyMesiac;


    public HomeFragmentRychlyPrehlad() {
        // Required empty public constructor
    }


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        currentView = inflater.inflate(R.layout.fragment_home_rychly_prehlad, container, false);

        sharedPreferences = new MySharedPreferences(getContext());

        // init UI
        scrollView = currentView.findViewById(R.id.home_fragment_rychly_prehlad_scroll_view);
        upozornenie = currentView.findViewById(R.id.home_fragment_rychly_prehlad_upozornenie);
        nacitavanieText = currentView.findViewById(R.id.home_fragment_rychly_prehlad_nacitavanie);
        nacitavanieProgressBar = currentView.findViewById(R.id.home_fragment_rychly_prehlad_progressBar);
        celkovyZostatok = currentView.findViewById(R.id.home_fragment_rychly_prehlad_celkovy_suma);
        dnesnyDen = currentView.findViewById(R.id.home_fragment_rychly_prehlad_dnesny_den);
        tyzden = currentView.findViewById(R.id.home_fragment_rychly_prehlad_tyzden);
        mesiac = currentView.findViewById(R.id.home_fragment_rychly_prehlad_koniec_mesiaca);

        // nastav spravnu vidtelnost
        Log.i(TAG, "onCreateView: nastajuvem layout na gone");
        nacitavanieText.setVisibility(View.VISIBLE);
        nacitavanieProgressBar.setVisibility(View.VISIBLE);
        scrollView.setVisibility(GONE);
        upozornenie.setVisibility(GONE);

        initListners();

        return currentView;
    }

    /**
     *
     * Tu zacina vsetko
     *
     */
    @Override
    public void onResume() {
        super.onResume();

        initView();
    }


    /**
     *
     * Po ukonceni treba odregistrovat vsetky pouzivane listenery
     *
     */
    @Override
    public void onPause() {
        super.onPause();

        /*Calendar aktualnyDatum = Calendar.getInstance();
        Calendar minulyMesaic = Calendar.getInstance();
        minulyMesaic.add(Calendar.MONTH, -1);

        DatabaseManager.getDb().child("ucty").removeEventListener(listenerNaUcty);
        if (hlavnyUcet != null) {
            DatabaseManager
                    .getDb()
                    .child("zaznamy")
                    .child(hlavnyUcet.getNazov())
                    .child(aktualnyDatum.get(Calendar.YEAR) + "_" + (aktualnyDatum.get(Calendar.MONTH) + 1))
                    .removeEventListener(listenerNaZaznamyAktualnyMesiac);


            DatabaseManager
                    .getDb()
                    .child("zaznamy")
                    .child(hlavnyUcet.getNazov())
                    .child(minulyMesaic.get(Calendar.YEAR) + "_" + (minulyMesaic.get(Calendar.MONTH) + 1))
                    .removeEventListener(listenerNaZaznamyMinulyMesiac);

            DatabaseManager
                    .getDb()
                    .child("trvalePrikazy")
                    .child(hlavnyUcet.getNazov())
                    .removeEventListener(listenerNaTrvalePrikazy);
        }

         */

        DatabaseManager.getDb().removeEventListener(celkovyListener);

        nacitavanieProgressBar.setVisibility(View.VISIBLE);
        nacitavanieText.setVisibility(View.VISIBLE);
        upozornenie.setVisibility(GONE);
        scrollView.setVisibility(GONE);


    }

    private void initListners() {

        // listner na vsetky data pouzivatela pre zobrazenie celkoveho prehladu
        celkovyListener = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {

                Calendar aktualnyDatum = Calendar.getInstance();
                String rokMesiacAktualny = aktualnyDatum.get(Calendar.YEAR) + "_" + (aktualnyDatum.get(Calendar.MONTH) + 1);

                Double chcemaUsetrenaSumaCelkovo = .0;
                Double zostatokCelkovo = .0;

                zaznamyPrijem = new ArrayList<>();
                zaznamyVydaj = new ArrayList<>();
                ucty = new ArrayList<>();

                if (!snapshot.exists()) {
                    nacitavanieText.setVisibility(GONE);
                    nacitavanieProgressBar.setVisibility(GONE);
                    scrollView.setVisibility(GONE);
                    upozornenie.setVisibility(View.VISIBLE);
                    upozornenie.setText("Nie sú k dispozícii žiadne dáta.");
                    return;
                }


                //hladam hlavny ucet pre urcenie meny v akej sa to bude zobrazovat, ak hlavny ucet nie je, tak je to Euro
                for (DataSnapshot dataSnapshot: snapshot.getChildren()) {
                    if (dataSnapshot.getKey().equals("ucty")) {
                        for (DataSnapshot uctySnapshot: dataSnapshot.getChildren()) {
                            Ucet ucet = uctySnapshot.getValue(Ucet.class);
                            if (ucet.isJeHlavnyUcet()) {
                                hlavnyUcet = uctySnapshot.getValue(BeznyUcet.class);
                                break;
                            }
                        }
                    }

                }

                if (hlavnyUcet == null) {
                    zvolenaMena = Meny.EUR;
                } else {
                    zvolenaMena = hlavnyUcet.getMena();
                }

                // idem prechadzat vsetky data
                for (DataSnapshot dataSnapshot: snapshot.getChildren()) {

                    // vetva ucty
                    if (dataSnapshot.getKey().equals("ucty")) {

                        for (DataSnapshot uctySnapshot: dataSnapshot.getChildren()) {
                            Ucet ucet = uctySnapshot.getValue(Ucet.class);

                            zostatokCelkovo += transferIfNecessery(ucet.getAktualnyZostatok(), zvolenaMena, ucet.getMena());

                            if (ucet.getTypUctu() == TypyUctov.BEZNY) {

                                BeznyUcet beznyUcet = uctySnapshot.getValue(BeznyUcet.class);
                                chcemaUsetrenaSumaCelkovo += transferIfNecessery(beznyUcet.getChcenaMesacneUsetrenaSuma(), zvolenaMena, beznyUcet.getMena());

                            } else if (ucet.getTypUctu() == TypyUctov.SPORIACI) {

                                SporiaciUcet sporiaciUcet = uctySnapshot.getValue(SporiaciUcet.class);

                            } else if (ucet.getTypUctu() == TypyUctov.KRYPTO) {

                                CryptoUcet cryptoUcet = uctySnapshot.getValue(CryptoUcet.class);
                            }
                        }

                    }

                    // vetvy trvale prikazy
                    if (dataSnapshot.getKey().equals("trvalePrikazy")) {

                        for (DataSnapshot trvalePrikazyUcty: dataSnapshot.getChildren()) {

                            for (DataSnapshot trvalyPrikaz: trvalePrikazyUcty.getChildren()) {

                                TrvalyPrikaz trvalyPrikazAktualny = trvalyPrikaz.getValue(TrvalyPrikaz.class);

                                if (!trvalyPrikazAktualny.isSporiaci()) {
                                    if (trvalyPrikazAktualny.getZaznam().getDenSplatnosti() > aktualnyDatum.get(Calendar.DAY_OF_MONTH)) {
                                        if (trvalyPrikazAktualny.getZaznam().getTypZaznamu() == TypZaznamu.PRIJEM) {
                                            Prijem novyPrijem = new Prijem();
                                            novyPrijem.setSuma(trvalyPrikazAktualny.getZaznam().getSuma());
                                            novyPrijem.setMena(trvalyPrikazAktualny.getZaznam().getMena());

                                            zaznamyPrijem.add(novyPrijem);
                                        } else {
                                            Vydaj novyVydaj =  new Vydaj();
                                            novyVydaj.setSuma(trvalyPrikazAktualny.getZaznam().getSuma());
                                            novyVydaj.setMena(trvalyPrikazAktualny.getZaznam().getMena());

                                            zaznamyVydaj.add(novyVydaj);
                                        }
                                    }
                                }
                            }

                        }

                    }

                    // vetva zaznamy
                    if (dataSnapshot.getKey().equals("zaznamy")) {

                        for (DataSnapshot zaznamyUcty: dataSnapshot.getChildren()) {

                            for (DataSnapshot zaznamyDatumy: zaznamyUcty.getChildren()) {

                                if (zaznamyDatumy.getKey().equals(rokMesiacAktualny)) {

                                    for (DataSnapshot zaznamy: zaznamyDatumy.getChildren()) {

                                        VlozenyZaznam zaznam = zaznamy.getValue(VlozenyZaznam.class);

                                        if (zaznam.getTypZaznamu() == TypZaznamu.PRIJEM) {
                                            zaznamyPrijem.add(zaznamy.getValue(Prijem.class));
                                        } else {
                                            zaznamyVydaj.add(zaznamy.getValue(Vydaj.class));
                                        }

                                        /*
                                        if (zaznam.getTypZaznamu() == TypZaznamu.PRIJEM) {
                                            prijmyCelkovo += transferIfNecessery(zaznam.getSuma(), zvolenaMena, zaznam.getMena());
                                        } else {
                                            vydajeCelkovo += transferIfNecessery(zaznam.getSuma(), zvolenaMena, zaznam.getMena());
                                        }

                                         */

                                    }

                                }

                            }

                        }

                    }

                }

                // zaciatok vypoctov potrebnych sum
                Log.i(TAG, "onDataChange: zacinam pocitat sumy");
                Double mesacnaSumaPrijem = spocitajPrijem(zaznamyPrijem);

                Double mesacnaSumaNaMinanie = mesacnaSumaPrijem - chcemaUsetrenaSumaCelkovo;

                Double sumaNaMinutieNaJedenDen = mesacnaSumaNaMinanie / (double) aktualnyDatum.getActualMaximum(Calendar.DAY_OF_MONTH);

                Double dnesMinutaSuma = .0;
                Double sumyMinutaZaCelyMesaic = .0;

                sumyMinuteZaPoslednych7Dni = new TreeMap<>();
                Calendar kalendarNaPosunDatumu = Calendar.getInstance();

                for (int i = 7; i > 0; i--) {
                    sumyMinuteZaPoslednych7Dni.put(kalendarNaPosunDatumu.get(Calendar.DAY_OF_MONTH), new ArrayList<>());
                    kalendarNaPosunDatumu.add(Calendar.DAY_OF_MONTH, -1);
                }

                // posledny den kedy sa ma brat zaznam
                kalendarNaPosunDatumu.add(Calendar.DAY_OF_MONTH, 1);

                Calendar casZadania = Calendar.getInstance();
                for (Vydaj vydaj: zaznamyVydaj) {

                    sumyMinutaZaCelyMesaic += transferIfNecessery(vydaj.getSuma(), zvolenaMena, vydaj.getMena());

                    casZadania.setTime(vydaj.getCasZadania());

                    if (casZadania.get(Calendar.DAY_OF_MONTH) == aktualnyDatum.get(Calendar.DAY_OF_MONTH)) {
                        dnesMinutaSuma += transferIfNecessery(vydaj.getSuma(), zvolenaMena, vydaj.getMena());
                    }

                    List<Double> sumyZaDanyDen = sumyMinuteZaPoslednych7Dni.get(casZadania.get(Calendar.DAY_OF_MONTH));
                    if (sumyZaDanyDen != null) {
                        sumyZaDanyDen.add(transferIfNecessery(vydaj.getSuma(), zvolenaMena, vydaj.getMena()));
                    }

                }

                int pocetOstavajucichDniVMesiaci = aktualnyDatum.getActualMaximum(Calendar.DAY_OF_MONTH) - aktualnyDatum.get(Calendar.DAY_OF_MONTH) + 1;
                Double ostavajucaSumaNaMesiac = mesacnaSumaNaMinanie - sumyMinutaZaCelyMesaic;


                // nastvatovanie hodnot
                if (zvolenaMena == Meny.BTC || zvolenaMena == Meny.ETH) {
                    celkovyZostatok.setText(String.format("%.8f %s", zostatokCelkovo, zvolenaMena.getZnak()));
                    dnesnyDen.setText(String.format("%.8f %s", ostavajucaSumaNaMesiac / pocetOstavajucichDniVMesiaci, zvolenaMena.getZnak()));
                    mesiac.setText(String.format("%.8f %s", mesacnaSumaNaMinanie - sumyMinutaZaCelyMesaic, zvolenaMena.getZnak()));
                } else {
                    celkovyZostatok.setText(String.format("%.02f %s", zostatokCelkovo, zvolenaMena.getZnak()));
                    dnesnyDen.setText(String.format("%.02f %s", ostavajucaSumaNaMesiac / pocetOstavajucichDniVMesiaci, zvolenaMena.getZnak()));
                    mesiac.setText(String.format("%.02f %s", mesacnaSumaNaMinanie - sumyMinutaZaCelyMesaic, zvolenaMena.getZnak()));
                }

                // kontrola ci je treba ziskat udaje aj z predosleho mesiaca  pre zobrazenie poslednych 7 dni
                if (aktualnyDatum.get(Calendar.MONTH) == kalendarNaPosunDatumu.get(Calendar.MONTH)) {

                    Log.i(TAG, "onDataChange: trvaleprikazy init barchart");
                    initBarChart();

                } else {

                    Calendar minulyMesiac = Calendar.getInstance();
                    minulyMesiac.add(Calendar.MONTH, -1);

                    String rokMesiacPredosli = minulyMesiac.get(Calendar.YEAR) + "_" + (minulyMesiac.get(Calendar.MONTH) + 1);

                    int pocetDniZMinulehoMesiaca = 7 - aktualnyDatum.get(Calendar.DAY_OF_MONTH);
                    int odDna = minulyMesiac.getActualMaximum(Calendar.DAY_OF_MONTH) - pocetDniZMinulehoMesiaca;

                    // treba ziskat aj zazany z predosleho mesiacu
                    for (DataSnapshot dataSnapshot: snapshot.getChildren()) {

                        if (dataSnapshot.getKey().equals("zaznamy")) {

                            for (DataSnapshot zaznamyUcty: dataSnapshot.getChildren()) {

                                for (DataSnapshot zaznamyDatumy: zaznamyUcty.getChildren()) {

                                    if (zaznamyDatumy.getKey().equals(rokMesiacPredosli)) {


                                        for (DataSnapshot zaznamy: zaznamyDatumy.getChildren()) {

                                            VlozenyZaznam zaznam = zaznamy.getValue(VlozenyZaznam.class);

                                            if (zaznam.getTypZaznamu() == TypZaznamu.VYDAJ) {
                                                Vydaj zaznamVydaj = zaznamy.getValue(Vydaj.class);
                                                Calendar casZadaniaMinulyMesiac = Calendar.getInstance();
                                                casZadaniaMinulyMesiac.setTime(zaznamVydaj.getCasZadania());

                                                List<Double> sumyZaDanyDen = sumyMinuteZaPoslednych7Dni.get(casZadaniaMinulyMesiac.get(Calendar.DAY_OF_MONTH));
                                                if (sumyZaDanyDen != null && casZadaniaMinulyMesiac.get(Calendar.DAY_OF_MONTH) > odDna) {
                                                    sumyZaDanyDen.add(transferIfNecessery(zaznamVydaj.getSuma(), zvolenaMena, zaznamVydaj.getMena()));
                                                }

                                            }

                                        }

                                    }

                                }

                            }

                        }

                    }

                    Log.i(TAG, "onDataChange: data minuly mesaic init bar chart");
                    initBarChart();

                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        };


        /*listenerNaUcty = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                Log.i(TAG, "onDataChange: ucty nove data");
                ucty = new ArrayList<>();

                for (DataSnapshot dataSnapshot1: dataSnapshot.getChildren()) {

                    Ucet ucet = dataSnapshot1.getValue(Ucet.class);
                    if (ucet.isJeHlavnyUcet()) {
                        hlavnyUcet = dataSnapshot1.getValue(BeznyUcet.class);
                        break;
                    }
                }

                if (hlavnyUcet != null) {
                    Log.i(TAG, "onDataChange: mam hlavny ucet, nastavujem sumy");
                    nastavSumy();
                } else {
                    Log.i(TAG, "onDataChange: nemam hlavny ucet zozbrazujem upozornenia");
                    nacitavanieProgressBar.setVisibility(GONE);
                    nacitavanieText.setVisibility(GONE);
                    upozornenie.setVisibility(View.VISIBLE);
                }

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        };

        listenerNaZaznamyAktualnyMesiac = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                zaznamyPrijem = new ArrayList<>();
                zaznamyVydaj = new ArrayList<>();

                Log.i(TAG, "onDataChange: mam nove data zaznamy tento mesiac");

                for (DataSnapshot dataSnapshot1: dataSnapshot.getChildren()) {

                    VlozenyZaznam zaznam = dataSnapshot1.getValue(VlozenyZaznam.class);

                    if (zaznam.getTypZaznamu() == TypZaznamu.PRIJEM) {
                        zaznamyPrijem.add(dataSnapshot1.getValue(Prijem.class));
                    } else {
                        zaznamyVydaj.add(dataSnapshot1.getValue(Vydaj.class));
                    }
                }


                Log.i(TAG, "onDataChange: inicializujem listner na trvale prikazy");
                DatabaseManager
                        .getDb()
                        .child("trvalePrikazy")
                        .child(hlavnyUcet.getNazov())
                        .addValueEventListener(listenerNaTrvalePrikazy);

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        };


        listenerNaTrvalePrikazy =  new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                Log.i(TAG, "onDataChange: noda data trvale prikazy");

                Calendar aktualnyDatum = Calendar.getInstance();

                for (DataSnapshot trvalyPrikazSnapshot: dataSnapshot.getChildren()) {

                    TrvalyPrikaz trvalyPrikazAktualny = trvalyPrikazSnapshot.getValue(TrvalyPrikaz.class);

                    if (!trvalyPrikazAktualny.isSporiaci()) {
                        if (trvalyPrikazAktualny.getZaznam().getDenSplatnosti() > aktualnyDatum.get(Calendar.DAY_OF_MONTH)) {
                            if (trvalyPrikazAktualny.getZaznam().getTypZaznamu() == TypZaznamu.PRIJEM) {
                                Prijem novyPrijem = new Prijem();
                                novyPrijem.setSuma(trvalyPrikazAktualny.getZaznam().getSuma());

                                zaznamyPrijem.add(novyPrijem);
                            } else {
                                Vydaj novyVydaj =  new Vydaj();
                                novyVydaj.setSuma(trvalyPrikazAktualny.getZaznam().getSuma());

                                zaznamyVydaj.add(novyVydaj);
                            }
                        }
                    }

                }

                Log.i(TAG, "onDataChange: zacinam pocitat sumy");
                Double mesacnaSumaPrijem = spocitajPrijem(zaznamyPrijem);

                Double mesacnaSumaNaMinanie = mesacnaSumaPrijem - hlavnyUcet.getChcenaMesacneUsetrenaSuma();

                Double sumaNaMinutieNaJedenDen = mesacnaSumaNaMinanie / (double) aktualnyDatum.getActualMaximum(Calendar.DAY_OF_MONTH);

                Double dnesMinutaSuma = .0;
                Double sumyMinutaZaCelyMesaic = .0;

                sumyMinuteZaPoslednych7Dni = new TreeMap<>();
                Calendar kalendarNaPosunDatumu = Calendar.getInstance();

                for (int i = 7; i > 0; i--) {
                    sumyMinuteZaPoslednych7Dni.put(kalendarNaPosunDatumu.get(Calendar.DAY_OF_MONTH), new ArrayList<>());
                    kalendarNaPosunDatumu.add(Calendar.DAY_OF_MONTH, -1);
                }

                // posledny den kedy sa ma brat zaznam
                kalendarNaPosunDatumu.add(Calendar.DAY_OF_MONTH, 1);

                Calendar casZadania = Calendar.getInstance();
                for (Vydaj vydaj: zaznamyVydaj) {

                    sumyMinutaZaCelyMesaic += vydaj.getSuma();

                    casZadania.setTime(vydaj.getCasZadania());

                    if (casZadania.get(Calendar.DAY_OF_MONTH) == aktualnyDatum.get(Calendar.DAY_OF_MONTH)) {
                        dnesMinutaSuma += vydaj.getSuma();
                    }

                    List<Double> sumyZaDanyDen = sumyMinuteZaPoslednych7Dni.get(casZadania.get(Calendar.DAY_OF_MONTH));
                    if (sumyZaDanyDen != null) {
                        sumyZaDanyDen.add(vydaj.getSuma());
                    }

                }

                int pocetOstavajucichDniVMesiaci = aktualnyDatum.getActualMaximum(Calendar.DAY_OF_MONTH) - aktualnyDatum.get(Calendar.DAY_OF_MONTH) + 1;
                Double ostavajucaSumaNaMesiac = mesacnaSumaNaMinanie - sumyMinutaZaCelyMesaic;



                if (hlavnyUcet.getMena() == Meny.BTC || hlavnyUcet.getMena() == Meny.ETH) {
                    celkovyZostatok.setText(String.format("%.8f %s", hlavnyUcet.getAktualnyZostatok(), hlavnyUcet.getMena().getZnak()));
                    dnesnyDen.setText(String.format("%.8f %s", ostavajucaSumaNaMesiac / pocetOstavajucichDniVMesiaci, hlavnyUcet.getMena().getZnak()));
                    mesiac.setText(String.format("%.8f %s", mesacnaSumaNaMinanie - sumyMinutaZaCelyMesaic, hlavnyUcet.getMena().getZnak()));
                } else {
                    celkovyZostatok.setText(String.format("%.02f %s", hlavnyUcet.getAktualnyZostatok(), hlavnyUcet.getMena().getZnak()));
                    dnesnyDen.setText(String.format("%.02f %s", ostavajucaSumaNaMesiac / pocetOstavajucichDniVMesiaci, hlavnyUcet.getMena().getZnak()));
                    mesiac.setText(String.format("%.02f %s", mesacnaSumaNaMinanie - sumyMinutaZaCelyMesaic, hlavnyUcet.getMena().getZnak()));
                }


                if (aktualnyDatum.get(Calendar.MONTH) == kalendarNaPosunDatumu.get(Calendar.MONTH)) {

                    Log.i(TAG, "onDataChange: trvaleprikazy init barchart");
                    initBarChart();

                } else {

                    Log.i(TAG, "onDataChange: potrebujem data z minuleho mesiaca, nastavujem listner na minuly mesiac");
                    DatabaseManager
                            .getDb()
                            .child("zaznamy")
                            .child(hlavnyUcet.getNazov())
                            .child(kalendarNaPosunDatumu.get(Calendar.YEAR) + "_" + (kalendarNaPosunDatumu.get(Calendar.MONTH) + 1))
                            .addValueEventListener(listenerNaZaznamyMinulyMesiac);
                }

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        };

        listenerNaZaznamyMinulyMesiac = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                Log.i(TAG, "onDataChange: nove dat minuly mesaic");
                Calendar aktualnyDatum = Calendar.getInstance();
                Calendar minulyMesiac = Calendar.getInstance();
                minulyMesiac.add(Calendar.MONTH, -1);

                int pocetDniZMinulehoMesiaca = 7 - aktualnyDatum.get(Calendar.DAY_OF_MONTH);
                int odDna = minulyMesiac.getActualMaximum(Calendar.DAY_OF_MONTH) - pocetDniZMinulehoMesiaca;

                for (DataSnapshot dataSnapshot1: dataSnapshot.getChildren()) {

                    VlozenyZaznam zaznam = dataSnapshot1.getValue(VlozenyZaznam.class);

                    if (zaznam.getTypZaznamu() == TypZaznamu.VYDAJ) {
                        Vydaj zaznamVydaj = dataSnapshot1.getValue(Vydaj.class);
                        Calendar casZadania = Calendar.getInstance();
                        casZadania.setTime(zaznamVydaj.getCasZadania());

                        List<Double> sumyZaDanyDen = sumyMinuteZaPoslednych7Dni.get(casZadania.get(Calendar.DAY_OF_MONTH));
                        if (sumyZaDanyDen != null && casZadania.get(Calendar.DAY_OF_MONTH) > odDna) {
                            sumyZaDanyDen.add(zaznamVydaj.getSuma());
                        }

                    }
                }

                Log.i(TAG, "onDataChange: data minuly mesaic init bar chart");
                initBarChart();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        };


         */
    }

    /**
     *
     * Pomocna metoda na inicialozovanie
     *
     */
    private void initView() {

        DatabaseManager.getDb().addValueEventListener(celkovyListener);
        //DatabaseManager.getDb().child("ucty").addValueEventListener(listenerNaUcty);
    }

    /**
     *
     * Nastavuje suma
     *
     * @deprecated
     */
    private void nastavSumy() {

        Calendar aktualnyDatum = Calendar.getInstance();

        DatabaseManager
                .getDb()
                .child("zaznamy")
                .child(hlavnyUcet.getNazov())
                .child(aktualnyDatum.get(Calendar.YEAR) + "_" + (aktualnyDatum.get(Calendar.MONTH) + 1))
                .addValueEventListener(listenerNaZaznamyAktualnyMesiac);



    }

    /**
     *
     * Pomocna metoda na zratanie vsetkych prijmov
     *
     * @param zaznamyPrijem
     * @return
     */
    private Double spocitajPrijem(List<Prijem> zaznamyPrijem) {

        Double result = .0;

        for (Prijem prijem: zaznamyPrijem) {
            result += transferIfNecessery(prijem.getSuma(), zvolenaMena, prijem.getMena());
        }

        return result;
    }

    /**
     *
     * Pomocna metoda na nastvenie dat do grafu
     *
     */
    private void initBarChart() {

        Log.i(TAG, "initBarChart: zacinam nastavovat bar chart");

        initPoslednych7Dni();

        // nastavnie parametrov pre barChart
        barChart = currentView.findViewById(R.id.home_fragment_rychly_prehlad_bar_chart);
        barChart.setDrawBarShadow(false);
        barChart.setDrawValueAboveBar(true);
        barChart.setDrawGridBackground(false);
        barChart.setPinchZoom(false);
        barChart.setMaxVisibleValueCount(50);
        //barChart.setBackground(ContextCompat.getDrawable(getContext(), R.drawable.bar_chart_background));
        barChart.getAxisLeft().setDrawGridLines(false);
        barChart.getXAxis().setDrawGridLines(false);
        barChart.getAxisRight().setDrawGridLines(false);
        barChart.getAxisRight().setEnabled(false);
        barChart.getAxisLeft().setEnabled(false);
        barChart.getDescription().setEnabled(false);
        barChart.animateXY(1000, 1000);
        barChart.setTouchEnabled(false);

        // priprava dat pre barChart
        ArrayList<BarEntry> barEntriesY = new ArrayList<>();

        ArrayList<String> dniTyzdna = new ArrayList<>(Arrays.asList("Po", "Ut", "St", "Št", "Pi", "So", "Ne"));
        ArrayList<Double> minuteSumyZa7Dni = new ArrayList<>();
        Calendar kalendarPreGraf = Calendar.getInstance();

        for (int i = 0; i < 7; i++) {

            List<Double> vydajkyZaZvolenyDenList = sumyMinuteZaPoslednych7Dni.get(kalendarPreGraf.get(Calendar.DAY_OF_MONTH));
            minuteSumyZa7Dni.add(vydajkyZaZvolenyDenList.stream().reduce(0., Double::sum));
            kalendarPreGraf.add(Calendar.DAY_OF_MONTH, -1);
        }

        Collections.reverse(minuteSumyZa7Dni);

        dniTyzdnaNaX = new ArrayList<>();
        for (int i = 0; i < 7; i++) {
            barEntriesY.add(new BarEntry(i, minuteSumyZa7Dni.get(i).floatValue()));
            dniTyzdnaNaX.add(dniTyzdna.get(poslednych7Dni[i]));
        }

        // vkladanie dat do barChartu
        BarDataSet newSet = new BarDataSet(barEntriesY, null);
        newSet.setColor(ContextCompat.getColor(getContext(), R.color.blue_800));
        BarData data = new BarData(newSet);
        data.setValueFormatter(new yAxisFormatter(zvolenaMena));
        data.setBarWidth(0.9f);
        data.setValueTextSize(15);
        barChart.setData(data);
        barChart.getLegend().setEnabled(false);

        XAxis xAxis = barChart.getXAxis();
        xAxis.setValueFormatter(new xAxisFormatter());
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM_INSIDE);
        xAxis.setTextSize(18);
        xAxis.setDrawAxisLine(false);

        nacitavanieProgressBar.setVisibility(GONE);
        nacitavanieText.setVisibility(GONE);
        upozornenie.setVisibility(GONE);
        scrollView.setVisibility(View.VISIBLE);
        Log.i(TAG, "initBarChart: graf nastaveny, menim viditelnost ");

    }

    /**
     *
     * Formatter pre sumy z grafu
     *
     */
    private class yAxisFormatter extends ValueFormatter {

        private Meny mena;

        public yAxisFormatter(Meny mena) {
            this.mena = mena;
        }

        @Override
        public String getFormattedValue(float value) {

            if ((mena == Meny.BTC || mena == Meny.ETH) && value != 0f) {
                return String.format("%.8f", value);
            }
            return String.format("%.2f", value);
        }
    }


    /**
     *
     * Formatter pre dni tyzdna
     *
     */
    private class xAxisFormatter extends ValueFormatter {

        @Override
        public String getAxisLabel(float value, AxisBase axis) {
            return dniTyzdnaNaX.get((int)value);
        }
    }

    /**
     *
     * Pomocna metoda na ziskanie poslednych 7 dni
     *
     */
    private void initPoslednych7Dni() {

        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.DAY_OF_YEAR, -7);

        int aktualnyDenPredTyznom;

        for (int i = 0; i <= 6; i++) {

            aktualnyDenPredTyznom = calendar.get(Calendar.DAY_OF_WEEK);
            poslednych7Dni[i] = aktualnyDenPredTyznom - 1;

            calendar.add(Calendar.DAY_OF_YEAR, 1);
        }

        System.out.println(Arrays.toString(poslednych7Dni));

    }

    /**
     *
     * Pomocna metoda na zobrazenie chyby
     *
     * @param chyba
     */
    private void nastalaChyba(String chyba) {

        AlertDialog.Builder chybaBuilder = new AlertDialog.Builder(getContext());

        chybaBuilder.setTitle("Neočakávaná chyba");
        chybaBuilder.setMessage("Nastala neočakávaná chyba - zobrazené údaje nemusia byť správne. Pokus o danú akcie prosím opakujte");

        chybaBuilder.setPositiveButton("Ok", null);
        chybaBuilder.create().show();
    }


    /**
     *
     * Pomocna metoda na prevody medzi menami
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

        double vyslednaSuma = 0.;

        int counter = 0;

        while (sharedPreferences.getLong("kurzy_update_date") == 0) {
            try {
                Thread.sleep(1000);
            } catch (Exception e) {
            }

            if (counter > 5) {
                nastalaChyba("");
            }

            counter++;
        }

        try {
            double sumaAkoUSD = sumaVoZvolenejMene / Double.parseDouble(String.valueOf(sharedPreferences.getFloat(menaZaznamu.getSkratka().toLowerCase(Locale.ROOT))));

            vyslednaSuma = sumaAkoUSD * Double.parseDouble(String.valueOf(sharedPreferences.getFloat(menaZvolenehoUctu.getSkratka().toLowerCase(Locale.ROOT))));
        } catch (Exception e) {
            Log.i(TAG, "transferIfNecessery: nastala chyba " + e.getMessage() );
            nastalaChyba("");
            return 0.;
        }

        return vyslednaSuma;

    }
}