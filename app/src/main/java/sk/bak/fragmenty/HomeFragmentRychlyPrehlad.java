package sk.bak.fragmenty;

import static android.view.View.GONE;

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


public class HomeFragmentRychlyPrehlad extends Fragment {


    private BarChart barChart;

    private View currentView;

    private int[] poslednych7Dni = new int[7];
    private List<String> dniTyzdnaNaX;

    private ScrollView scrollView;
    private TextView upozornenie;
    private TextView nacitavanieText;
    private ProgressBar nacitavanieProgressBar;
    private TextView celkovyZostatok;
    private TextView dnesnyDen;
    private TextView tyzden;
    private TextView mesiac;

    private List<Ucet> ucty;
    private BeznyUcet hlavnyUcet;

    private static final String TAG = "HomeFragmentRychlyPrehlad";

    Map<Integer, List<Double>> sumyMinuteZaPoslednych7Dni;


    ValueEventListener listenerNaUcty;
    ValueEventListener listenerNaTrvalePrikazy;
    ValueEventListener listenerNaZaznamyAktualnyMesiac;
    ValueEventListener listenerNaZaznamyMinulyMesiac;

    List<Prijem> zaznamyPrijem;
    List<Vydaj> zaznamyVydaj;

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
        // Inflate the layout for this fragment
        currentView = inflater.inflate(R.layout.fragment_home_rychly_prehlad, container, false);

        scrollView = currentView.findViewById(R.id.home_fragment_rychly_prehlad_scroll_view);
        upozornenie = currentView.findViewById(R.id.home_fragment_rychly_prehlad_upozornenie);
        nacitavanieText = currentView.findViewById(R.id.home_fragment_rychly_prehlad_nacitavanie);
        nacitavanieProgressBar = currentView.findViewById(R.id.home_fragment_rychly_prehlad_progressBar);
        celkovyZostatok = currentView.findViewById(R.id.home_fragment_rychly_prehlad_celkovy_suma);
        dnesnyDen = currentView.findViewById(R.id.home_fragment_rychly_prehlad_dnesny_den);
        tyzden = currentView.findViewById(R.id.home_fragment_rychly_prehlad_tyzden);
        mesiac = currentView.findViewById(R.id.home_fragment_rychly_prehlad_koniec_mesiaca);

        Log.i(TAG, "onCreateView: nastajuvem layout na gone");
        nacitavanieText.setVisibility(View.VISIBLE);
        nacitavanieProgressBar.setVisibility(View.VISIBLE);
        scrollView.setVisibility(GONE);
        upozornenie.setVisibility(GONE);

        initListners();

        return currentView;
    }

    private void initListners() {

        listenerNaUcty = new ValueEventListener() {
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

    }

    private void initView() {

        DatabaseManager.getDb().child("ucty").addValueEventListener(listenerNaUcty);
    }

    private void nastavSumy() {

        Calendar aktualnyDatum = Calendar.getInstance();

        DatabaseManager
                .getDb()
                .child("zaznamy")
                .child(hlavnyUcet.getNazov())
                .child(aktualnyDatum.get(Calendar.YEAR) + "_" + (aktualnyDatum.get(Calendar.MONTH) + 1))
                .addValueEventListener(listenerNaZaznamyAktualnyMesiac);



    }

    private Double spocitajPrijem(List<Prijem> zaznamyPrijem) {

        Double result = .0;

        for (Prijem prijem: zaznamyPrijem) {
            result += prijem.getSuma();
        }

        return result;
    }

    @Override
    public void onResume() {
        super.onResume();

        initView();
    }

    @Override
    public void onPause() {
        super.onPause();

        Calendar aktualnyDatum = Calendar.getInstance();
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

        nacitavanieProgressBar.setVisibility(View.VISIBLE);
        nacitavanieText.setVisibility(View.VISIBLE);
        upozornenie.setVisibility(GONE);
        scrollView.setVisibility(GONE);

    }

    private void initBarChart() {

        Log.i(TAG, "initBarChart: zacinam nastavovat bar chart");

        initPoslednych7Dni();

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

        BarDataSet newSet = new BarDataSet(barEntriesY, null);
        newSet.setColor(ContextCompat.getColor(getContext(), R.color.blue_800));
        BarData data = new BarData(newSet);
        data.setBarWidth(0.9f);
        data.setValueTextSize(15);
        data.setValueFormatter(new yAxisFormatter(hlavnyUcet.getMena()));
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

    private class yAxisFormatter extends ValueFormatter {

        private Meny mena;

        public yAxisFormatter(Meny mena) {
            this.mena = mena;
        }

        @Override
        public String getPointLabel(Entry entry) {
            if ((mena == Meny.BTC || mena == Meny.ETH) && entry.getY() != 0f) {
                return String.format("%.8f", entry.getY());
            }
            return String.format("%.2f", entry.getY());
        }
    }

    private class xAxisFormatter extends ValueFormatter {

        @Override
        public String getAxisLabel(float value, AxisBase axis) {
            return dniTyzdnaNaX.get((int)value);
        }
    }

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
}