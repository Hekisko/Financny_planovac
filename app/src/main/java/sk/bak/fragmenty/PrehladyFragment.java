package sk.bak.fragmenty;

import android.content.res.ColorStateList;
import android.graphics.Color;
import android.graphics.DashPathEffect;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.AdapterView;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextSwitcher;
import android.widget.TextView;
import android.widget.ViewSwitcher;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.cardview.widget.CardView;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.charts.PieChart;
import com.github.mikephil.charting.components.AxisBase;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.components.LimitLine;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.data.PieData;
import com.github.mikephil.charting.data.PieDataSet;
import com.github.mikephil.charting.data.PieEntry;
import com.github.mikephil.charting.formatter.ValueFormatter;
import com.github.mikephil.charting.interfaces.datasets.ILineDataSet;
import com.github.mikephil.charting.utils.Utils;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicReferenceFieldUpdater;

import sk.bak.R;
import sk.bak.managers.DatabaseManager;
import sk.bak.model.Prijem;
import sk.bak.model.Vydaj;
import sk.bak.model.abst.Ucet;
import sk.bak.model.abst.VlozenyZaznam;
import sk.bak.model.enums.TypVydaju;
import sk.bak.model.enums.TypZaznamu;



public class PrehladyFragment extends Fragment {

    private final String[] MESIACE_SLOVENSKY = {"Január", "Február", "Marec", "Apríl", "Máj", "Jún", "Júl", "August", "September", "Október", "November", "Decemnber"};


    private static final String TAG = "PrehladyFragment";
    private View currentView;
    private TextView ziadneData;
    private TextView ziadneUcty;

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
    private CardView[] selectedUcetPrehladu = {null};

    private CardView pieChartCardButton;
    private CardView lineChartCardButton;
    private CardView[] selectedTypGrafu = {null};

    private LinearLayout datumLayout;
    private ImageButton datumSpat;
    private ImageButton datumDopredu;
    private TextSwitcher datum;
    Animation animationInBack;
    Animation animationOutBack;
    Animation animationInForw;
    Animation animationOuForw;
    Calendar calendarPreGrafy;
    View.OnClickListener posunDatumuListener;


    private ImageButton filterExpandButton;
    private boolean isFilterOn = false;
    private LinearLayout filterExpandButtonLayout;

    private CardView jedlo;
    private CardView cestovanie;
    private CardView elektro;
    private CardView sport;
    private CardView auto;
    private CardView rodina;
    private CardView hry;
    private CardView oblecenie;
    private CardView house;
    private CardView drogeria;
    private CardView ostatne;
    private CardView animal;
    private CardView[] selectedUcelZaznamu = {null};
    private LinearLayout ucelIconyLayout;

    private PieChart pieChart;

    private LineChart lineChart;


    private View.OnClickListener ucetClick;
    private View.OnClickListener grafClick;
    private View.OnClickListener filterClick;

    private Set<String> zvoleneFiltre;

    private DatabaseReference databaseReferenceListeningAt;
    private ValueEventListener pouzivanyListner;


    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        currentView = inflater.inflate(R.layout.prehlady_fragment, container, false);
        
        zvoleneFiltre = new HashSet<>();
        ziadneData = currentView.findViewById(R.id.prehlady_ziadne_data);
        ziadneData.setVisibility(View.GONE);

        ziadneUcty = currentView.findViewById(R.id.prehlady_ziadne_ucty);
        ziadneUcty.setVisibility(View.GONE);

        initListenery();
        initUcty();
        initPosunDatumu();
        initTypGraphSelection();
        initFilter();
        initIcony();
        initPieChart();
        initLineChart();

        calendarPreGrafy = Calendar.getInstance();

        return currentView;
    }

    private void initListenery() {

        ucetClick = new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (selectedUcetPrehladu[0] != null) {

                    v.setBackgroundTintList(ColorStateList.valueOf(ContextCompat.getColor(getContext(),R.color.blue_600)));
                    selectedUcetPrehladu[0].setBackgroundTintList(ColorStateList.valueOf(Color.WHITE));

                    if (selectedUcetPrehladu[0].getId() == v.getId()) {
                        selectedUcetPrehladu[0] = null;
                    } else {
                        selectedUcetPrehladu[0] = (CardView) v;
                    }

                } else {

                    v.setBackgroundTintList(ColorStateList.valueOf(ContextCompat.getColor(getContext(),R.color.blue_600)));
                    selectedUcetPrehladu[0] = (CardView) v;

                }

                skryGrafy();

                if (selectedUcetPrehladu[0] != null && selectedTypGrafu[0] != null) {

                    fillGrafy();
                }

            }
        };
        
        
        grafClick = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (selectedTypGrafu[0] != null) {

                    v.setBackgroundTintList(ColorStateList.valueOf(ContextCompat.getColor(getContext(),R.color.blue_600)));
                    selectedTypGrafu[0].setBackgroundTintList(ColorStateList.valueOf(Color.WHITE));

                    if (selectedTypGrafu[0].getId() == v.getId()) {
                        selectedTypGrafu[0] = null;
                    } else {
                        selectedTypGrafu[0] = (CardView) v;
                    }

                } else {

                    v.setBackgroundTintList(ColorStateList.valueOf(ContextCompat.getColor(getContext(),R.color.blue_600)));
                    selectedTypGrafu[0] = (CardView) v;

                }

                skryGrafy();

                if (selectedUcetPrehladu[0] != null && selectedTypGrafu[0] != null) {

                    fillGrafy();
                    
                }
            }
        };

        
        filterClick = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                
                switch (v.getId()) {
                    case R.id.prehlady_ucel_jedlo_card:
                        vlozAleboVymaz(TypVydaju.STRAVA.getNazov(), v);
                        break;
                    case R.id.prehlady_ucel_cestovanie_card:
                        vlozAleboVymaz(TypVydaju.CESTOVANIE.getNazov(), v);
                        break;
                    case R.id.prehlady_ucel_elektro_card:
                        vlozAleboVymaz(TypVydaju.ELEKTRO.getNazov(), v);
                        break;
                    case R.id.prehlady_ucel_sport_card:
                        vlozAleboVymaz(TypVydaju.SPORT.getNazov(), v);
                        break;
                    case R.id.prehlady_ucel_car_card:
                        vlozAleboVymaz(TypVydaju.DOPRAVA.getNazov(), v);
                        break;
                    case R.id.prehlady_ucel_family_card:
                        vlozAleboVymaz(TypVydaju.RODINA.getNazov(), v);
                        break;
                    case R.id.prehlady_ucel_games_card:
                        vlozAleboVymaz(TypVydaju.ZAVABA.getNazov(), v);
                        break;
                    case R.id.prehlady_ucel_cloth_card:
                        vlozAleboVymaz(TypVydaju.OBLECENIE.getNazov(), v);
                        break;
                    case R.id.prehlady_ucel_house_card:
                        vlozAleboVymaz(TypVydaju.HOUSE.getNazov(), v);
                        break;
                    case R.id.prehlady_ucel_animal_card:
                        vlozAleboVymaz(TypVydaju.ANIMAL.getNazov(), v);
                        break;
                    case R.id.prehlady_ucel_drogeria_card:
                        vlozAleboVymaz(TypVydaju.DROGERIA.getNazov(), v);
                        break;
                    case R.id.prehlady_ucel_ostatne_card:
                        vlozAleboVymaz(TypVydaju.OSTATNE.getNazov(), v);
                        break;
                }
                
            }
        };
        
    }

    private void fillGrafy() {

        Log.i(TAG, "fillGrafy: START");
        
        if (selectedTypGrafu[0] != null && selectedTypGrafu[0].getId() == R.id.prehlady_piechart_card) {

            initPieChartWithData();

        } else if (selectedTypGrafu[0] != null && selectedTypGrafu[0].getId() == R.id.prehlady_linechart_card) {

            initLineChartWithData();

        }

        Log.i(TAG, "fillGrafy: DONE");
    }

    private void showGrafy() {

        Log.i(TAG, "showGrafy: START");
        
        if (selectedTypGrafu[0] != null && selectedTypGrafu[0].getId() == R.id.prehlady_piechart_card) {

            datumLayout.setVisibility(View.VISIBLE);
            pieChart.setVisibility(View.VISIBLE);
            filterExpandButtonLayout.setVisibility(View.VISIBLE);
            nastavDatum(true);
            
        } else if (selectedTypGrafu[0] != null && selectedTypGrafu[0].getId() == R.id.prehlady_linechart_card) {

            datumLayout.setVisibility(View.VISIBLE);
            lineChart.setVisibility(View.VISIBLE);
            nastavDatum(false);
        }


        Log.i(TAG, "showGrafy: DONE");
    }

    private void skryGrafy() {
        Log.i(TAG, "skryGrafy: START");
        datumLayout.setVisibility(View.GONE);
        filterExpandButtonLayout.setVisibility(View.GONE);
        ucelIconyLayout.setVisibility(View.GONE);
        pieChart.setVisibility(View.GONE);
        lineChart.setVisibility(View.GONE);
        changeFilterBool(false);
        Log.i(TAG, "skryGrafy: DONE");
    }

    private void vlozAleboVymaz(String typFiltru, View v) {
        
        if (zvoleneFiltre.contains(typFiltru)) {
            v.setBackgroundTintList(ColorStateList.valueOf(Color.WHITE));
            zvoleneFiltre.remove(typFiltru);
        } else {
            zvoleneFiltre.add(typFiltru);
            v.setBackgroundTintList(ColorStateList.valueOf(ContextCompat.getColor(getContext(),R.color.blue_600)));
        }
        
        fillGrafy();
        
    }

    private void initLineChart() {

        lineChart = currentView.findViewById(R.id.prehlady_linechart);

        lineChart.setVisibility(View.GONE);

    }

    private void initPieChart() {

        pieChart = currentView.findViewById(R.id.prehlady_piechart);

        pieChart.setVisibility(View.GONE);
    }

    private void initIcony() {

        ucelIconyLayout = currentView.findViewById(R.id.prehlady_layout_pre_filter_icony);

        jedlo = currentView.findViewById(R.id.prehlady_ucel_jedlo_card);
        jedlo.setOnClickListener(filterClick);

        cestovanie = currentView.findViewById(R.id.prehlady_ucel_cestovanie_card);
        cestovanie.setOnClickListener(filterClick);

        elektro = currentView.findViewById(R.id.prehlady_ucel_elektro_card);
        elektro.setOnClickListener(filterClick);

        sport = currentView.findViewById(R.id.prehlady_ucel_sport_card);
        sport.setOnClickListener(filterClick);

        auto = currentView.findViewById(R.id.prehlady_ucel_car_card);
        auto.setOnClickListener(filterClick);

        rodina = currentView.findViewById(R.id.prehlady_ucel_family_card);
        rodina.setOnClickListener(filterClick);

        hry = currentView.findViewById(R.id.prehlady_ucel_games_card);
        hry.setOnClickListener(filterClick);

        oblecenie = currentView.findViewById(R.id.prehlady_ucel_cloth_card);
        oblecenie.setOnClickListener(filterClick);

        house = currentView.findViewById(R.id.prehlady_ucel_house_card);
        house.setOnClickListener(filterClick);

        drogeria = currentView.findViewById(R.id.prehlady_ucel_drogeria_card);
        drogeria.setOnClickListener(filterClick);

        animal = currentView.findViewById(R.id.prehlady_ucel_animal_card);
        animal.setOnClickListener(filterClick);

        ostatne = currentView.findViewById(R.id.prehlady_ucel_ostatne_card);
        ostatne.setOnClickListener(filterClick);

        ucelIconyLayout.setVisibility(View.GONE);
    }

    private void initFilter() {

        filterExpandButtonLayout = currentView.findViewById(R.id.prehlady_filter_button_layout);
        filterExpandButton = currentView.findViewById(R.id.prehlady_filter_expand_button);


        filterExpandButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (isFilterOn) {

                    changeFilterBool(false);

                } else {

                    changeFilterBool(true);

                }
            }
        });

        filterExpandButtonLayout.setVisibility(View.GONE);

    }

    private void changeFilterBool(boolean isOpen) {

        if (!zvoleneFiltre.isEmpty()) {
            zvoleneFiltre = new HashSet<>();
            fillGrafy();
        }

        zvoleneFiltre = new HashSet<>();
        if (isOpen) {
            isFilterOn = true;
            filterExpandButton.setImageDrawable(ContextCompat.getDrawable(getContext(), R.drawable.ic_expand_up_warehouse));
            ucelIconyLayout.setVisibility(View.VISIBLE);
        } else {
            isFilterOn = false;
            filterExpandButton.setImageDrawable(ContextCompat.getDrawable(getContext(), R.drawable.ic_expand_down_warehouse));
            ucelIconyLayout.setVisibility(View.GONE);
        }
    }


    private void nastavDatum(boolean isPieChart) {

        Log.i(TAG, "nastavDatum: START");
        if (((TextView) datum.getCurrentView()).getText().toString().contains(" ") && !isPieChart) {
            datum.setInAnimation(null);
            datum.setOutAnimation(null);
        }

        if (isPieChart) {
            datum.setText(MESIACE_SLOVENSKY[calendarPreGrafy.get(Calendar.MONTH)] +
                    " " +
                    calendarPreGrafy.get(Calendar.YEAR));
        } else {
            datum.setText(String.valueOf(calendarPreGrafy.get(Calendar.YEAR)));
        }

        Log.i(TAG, "nastavDatum: START");
    }

    private void initPosunDatumu() {

        datumLayout = currentView.findViewById(R.id.prehlady_layout_pre_posun_datumu);
        datumLayout.setVisibility(View.GONE);
        datumSpat = currentView.findViewById(R.id.prehlady_dozadu_datum);
        datumDopredu = currentView.findViewById(R.id.prehlady_dopredu_datum);
        datum = currentView.findViewById(R.id.prehlady_text_switcher);
        animationInBack = AnimationUtils.loadAnimation(getContext(), R.anim.slide_in_left);
        animationOutBack = AnimationUtils.loadAnimation(getContext(), R.anim.slide_out_right);
        animationInForw = AnimationUtils.loadAnimation(getContext(), R.anim.slide_in_right);
        animationOuForw = AnimationUtils.loadAnimation(getContext(), R.anim.slide_out_left);
        posunDatumuListener = new PosunDatumu();


        datumDopredu.setOnClickListener(posunDatumuListener);
        datumSpat.setOnClickListener(posunDatumuListener);

        datum.setFactory(new ViewSwitcher.ViewFactory() {
            @Override
            public View makeView() {
                TextView textView = new TextView(getContext());
                textView.setTextColor(Color.BLACK);
                textView.setGravity(Gravity.CENTER);
                textView.setTextSize(15);
                return textView;
            }
        });
    }


    private class PosunDatumu implements View.OnClickListener {

        @Override
        public void onClick(View v) {

            if (v.getId() == R.id.prehlady_dozadu_datum) {

                datum.setInAnimation(animationInBack);
                datum.setOutAnimation(animationOutBack);

                if (selectedTypGrafu[0] != null) {
                    if (selectedTypGrafu[0].getId() == R.id.prehlady_piechart_card) {
                        calendarPreGrafy.add(Calendar.MONTH, -1);
                        nastavDatum(true);
                    } else {
                        calendarPreGrafy.add(Calendar.YEAR, -1);
                        nastavDatum(false);
                    }
                }
            } else {

                datum.setInAnimation(animationInForw);
                datum.setOutAnimation(animationOuForw);

                if (selectedTypGrafu[0] != null) {
                    if (selectedTypGrafu[0].getId() == R.id.prehlady_piechart_card) {
                        calendarPreGrafy.add(Calendar.MONTH, 1);
                        nastavDatum(true);
                    } else {
                        calendarPreGrafy.add(Calendar.YEAR, 1);
                        nastavDatum(false);
                    }
                }
            }

            fillGrafy();

        }
    }

    private void initTypGraphSelection() {

        pieChartCardButton = currentView.findViewById(R.id.prehlady_piechart_card);
        pieChartCardButton.setOnClickListener(grafClick);

        lineChartCardButton = currentView.findViewById(R.id.prehlady_linechart_card);
        lineChartCardButton.setOnClickListener(grafClick);
    }

    private void initUcty() {
        List<Ucet> aktivneUcty = new ArrayList<>();

        aktivneUcty = DatabaseManager.getUcty();

        ucet1 = currentView.findViewById(R.id.prehlady_ucet_1_layout);
        ucet1.setOnClickListener(ucetClick);
        ucet1_text = currentView.findViewById(R.id.prehlady_ucet_1_text_view);

        ucet2 = currentView.findViewById(R.id.prehlady_ucet_2_layout);
        ucet2.setOnClickListener(ucetClick);
        ucet2_text = currentView.findViewById(R.id.prehlady_ucet_2_text_view);

        ucet3 = currentView.findViewById(R.id.prehlady_ucet_3_layout);
        ucet3.setOnClickListener(ucetClick);
        ucet3_text = currentView.findViewById(R.id.prehlady_ucet_3_text_view);

        ucet4 = currentView.findViewById(R.id.prehlady_ucet_4_layout);
        ucet4.setOnClickListener(ucetClick);
        ucet4_text = currentView.findViewById(R.id.prehlady_ucet_4_text_view);

        ucet5 = currentView.findViewById(R.id.prehlady_ucet_5_layout);
        ucet5.setOnClickListener(ucetClick);
        ucet5_text = currentView.findViewById(R.id.prehlady_ucet_5_text_view);

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

    /*
    private void nastavPozadie(CardView newSelectedCardView, CardView[] oldSelectedCardView) {

        if (newSelectedCardView.getId() == R.id.prehlady_linechart_card) {

            if (newSelectedCardView.equals(oldSelectedCardView[0])) {
                datumLayout.setVisibility(View.GONE);
                lineChart.setVisibility(View.GONE);
            } else {
                datumLayout.setVisibility(View.VISIBLE);
                initLineChartWithData();
            }

            filterExpandButtonLayout.setVisibility(View.GONE);
            ucelIconyLayout.setVisibility(View.GONE);
            pieChart.setVisibility(View.GONE);
            lineChart.setVisibility(View.VISIBLE);
            calendarPreGrafy = Calendar.getInstance();
            datum.setInAnimation(null);
            datum.setOutAnimation(null);
            nastavDatum(false);


        } else if (newSelectedCardView.getId() == R.id.prehlady_piechart_card) {


            if (newSelectedCardView.equals(oldSelectedCardView[0])) {
                datumLayout.setVisibility(View.GONE);
                filterExpandButtonLayout.setVisibility(View.GONE);
                ucelIconyLayout.setVisibility(View.GONE);
                pieChart.setVisibility(View.GONE);
                changeFilterBool(false);
            } else {
                datumLayout.setVisibility(View.VISIBLE);
                filterExpandButtonLayout.setVisibility(View.VISIBLE);
                if (isFilterOn) {
                    ucelIconyLayout.setVisibility(View.VISIBLE);
                }
                initPieChartWithData();
            }

            pieChart.setVisibility(View.VISIBLE);
            lineChart.setVisibility(View.GONE);
            calendarPreGrafy = Calendar.getInstance();
            datum.setInAnimation(null);
            datum.setOutAnimation(null);
            nastavDatum(true);
        }


        if (newSelectedCardView.equals(oldSelectedCardView[0])) {
            oldSelectedCardView[0] = null;
            newSelectedCardView.setBackgroundTintList(ColorStateList.valueOf(Color.WHITE));
        } else {
            if (oldSelectedCardView[0] != null) {
                oldSelectedCardView[0].setBackgroundTintList(ColorStateList.valueOf(Color.WHITE));
            }

            oldSelectedCardView[0] = newSelectedCardView;
            newSelectedCardView.setBackgroundTintList(ColorStateList.valueOf(ContextCompat.getColor(getContext(),R.color.blue_600)));
        }

        if (selectedTypGrafu[0] == null || selectedUcetPrehladu[0] == null) {
            datumLayout.setVisibility(View.GONE);
            filterExpandButtonLayout.setVisibility(View.GONE);
            ucelIconyLayout.setVisibility(View.GONE);
            pieChart.setVisibility(View.GONE);
            lineChart.setVisibility(View.GONE);
        }

    }

     */

    private void initPieChartWithData() {

        Log.i(TAG, "initPieChartWithData: START");
        
        if (databaseReferenceListeningAt != null && pouzivanyListner != null) {
            databaseReferenceListeningAt.removeEventListener(pouzivanyListner);
            databaseReferenceListeningAt = null;
            pouzivanyListner = null;
        }

        String nazovUctu = ((TextView)selectedUcetPrehladu[0].getChildAt(0)).getText().toString();

        databaseReferenceListeningAt = DatabaseManager
                .getDb()
                .child("zaznamy")
                .child(nazovUctu)
                .child(calendarPreGrafy.get(Calendar.YEAR) + "_" + (calendarPreGrafy.get(Calendar.MONTH) + 1));

        pouzivanyListner = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                Log.i(TAG, "onDataChange: piechart nove data");
                
                if (!dataSnapshot.exists()) {
                    ziadneData.setVisibility(View.VISIBLE);
                    pieChart.setVisibility(View.GONE);
                    return;
                } else {
                    pieChart.setVisibility(View.VISIBLE);
                    ziadneData.setVisibility(View.GONE);
                }

                Map<String, Double> sumarVydajov = new HashMap<>();
                Double sumarPrijmov = .0;
                String mena = null;

                for (DataSnapshot dataSnapshot1: dataSnapshot.getChildren()) {

                    VlozenyZaznam zaznam = dataSnapshot1.getValue(VlozenyZaznam.class);
                    mena = zaznam.getMena().getZnak();
                    
                    if (zaznam.getTypZaznamu() == TypZaznamu.PRIJEM) {
                        Prijem prijem = dataSnapshot1.getValue(Prijem.class);
                        sumarPrijmov += prijem.getSuma();
                    } else {
                        Vydaj vydaj = dataSnapshot1.getValue(Vydaj.class);

                        String typVydaju = vydaj.getTypVydaju().getNazov();

                        Double sumaPreVydaj = sumarVydajov.getOrDefault(typVydaju, .0) + vydaj.getSuma();
                        sumarVydajov.put(typVydaju, sumaPreVydaj);

                    }
                }

                ArrayList<PieEntry> pieEntries = new ArrayList<PieEntry>();
                if (sumarPrijmov != 0.) {
                    pieEntries.add(new PieEntry(sumarPrijmov.floatValue(), (Drawable) null));
                }
                // count is the number of values you need to display into graph
                for (Map.Entry<String,Double> entry : sumarVydajov.entrySet()) {

                    Drawable icon = null;
                    Double suma = entry.getValue();

                    if (suma == 0.) {
                        continue;
                    }

                    if (!zvoleneFiltre.isEmpty() && !zvoleneFiltre.contains(entry.getKey())) {
                        continue;
                    }

                    switch (entry.getKey()) {
                        case "Strava":
                            icon = ContextCompat.getDrawable(getContext(), R.drawable.ic_food_smaller);
                            break;
                        case "Cestovanie":
                            icon = ContextCompat.getDrawable(getContext(), R.drawable.ic_mountins_smaller);
                            break;
                        case "Elektro":
                            icon = ContextCompat.getDrawable(getContext(), R.drawable.ic_elektro_smaller);
                            break;
                        case "Sport":
                            icon = ContextCompat.getDrawable(getContext(), R.drawable.ic_cinka_smaller);
                            break;
                        case "Doprava":
                            icon = ContextCompat.getDrawable(getContext(), R.drawable.ic_car_smaller);
                            break;
                        case "Rodina":
                            icon = ContextCompat.getDrawable(getContext(), R.drawable.ic_family_smaller);
                            break;
                        case "Zabava":
                            icon = ContextCompat.getDrawable(getContext(), R.drawable.ic_gamepad_smaller);
                            break;
                        case "Oblecenie":
                            icon = ContextCompat.getDrawable(getContext(), R.drawable.ic_tshirt_smaller);
                            break;
                        case "Ostatne":
                            icon = ContextCompat.getDrawable(getContext(), R.drawable.ic_question_mark_smaller);
                            break;
                        case "House":
                            icon = ContextCompat.getDrawable(getContext(), R.drawable.ic_house_smaller);
                            break;
                        case "Drogeria":
                            icon = ContextCompat.getDrawable(getContext(), R.drawable.ic_drogeria_smaller);
                            break;
                        case "Animal":
                            icon = ContextCompat.getDrawable(getContext(), R.drawable.ic_animal_smaller);
                            break;
                    }


                    pieEntries.add(new PieEntry(suma.floatValue(), icon));
                }

                // formatovanie textu
                // https://medium.com/@makkenasrinivasarao1/line-chart-implementation-with-mpandroidchart-af3dd11804a7
                ArrayList<Integer> colors = new ArrayList<Integer>();
                if (sumarPrijmov != 0.) {
                    colors.add(ContextCompat.getColor(getContext(), R.color.green_400));
                }
                colors.add(ContextCompat.getColor(getContext(), R.color.red_400));
                colors.add(ContextCompat.getColor(getContext(), R.color.red_400));
                colors.add(ContextCompat.getColor(getContext(), R.color.red_400));
                colors.add(ContextCompat.getColor(getContext(), R.color.red_400));
                colors.add(ContextCompat.getColor(getContext(), R.color.red_400));
                colors.add(ContextCompat.getColor(getContext(), R.color.red_400));
                colors.add(ContextCompat.getColor(getContext(), R.color.red_400));
                colors.add(ContextCompat.getColor(getContext(), R.color.red_400));
                colors.add(ContextCompat.getColor(getContext(), R.color.red_400));
                colors.add(ContextCompat.getColor(getContext(), R.color.red_400));
                colors.add(ContextCompat.getColor(getContext(), R.color.red_400));
                colors.add(ContextCompat.getColor(getContext(), R.color.red_400));
                colors.add(ContextCompat.getColor(getContext(), R.color.red_400));



                PieDataSet dataset = new PieDataSet(pieEntries, "");
                dataset.setSliceSpace(5);
                dataset.setXValuePosition(PieDataSet.ValuePosition.OUTSIDE_SLICE);
                dataset.setYValuePosition(PieDataSet.ValuePosition.OUTSIDE_SLICE);
                dataset.setValueLinePart1OffsetPercentage(100f);
                dataset.setValueLinePart1Length(0.4f);
                dataset.setValueLinePart2Length(0.3f);
                dataset.setValueFormatter(new MenaValueFormatter(mena));


                PieData data = new PieData(dataset);
                data.setValueTextSize(15);

                pieChart.setData(data);
                dataset.setColors(colors);

                pieChart.setMinAngleForSlices(25);
                pieChart.setExtraOffsets(50.f, 5.f, 50.f, 5.f);
                pieChart.getLegend().setEnabled(false);
                pieChart.getDescription().setEnabled(false);
                pieChart.animateY(1000);

                showGrafy();
                Log.i(TAG, "onDataChange: DONE - zobrazujem graf");

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
            }
        };

        databaseReferenceListeningAt.addValueEventListener(pouzivanyListner);

    }

    private void initLineChartWithData() {

        Log.i(TAG, "initLineChartWithData: START");
        
        if (databaseReferenceListeningAt != null && pouzivanyListner != null) {
            databaseReferenceListeningAt.removeEventListener(pouzivanyListner);
            databaseReferenceListeningAt = null;
            pouzivanyListner = null;
        }

        String nazovUctu = ((TextView)selectedUcetPrehladu[0].getChildAt(0)).getText().toString();

        databaseReferenceListeningAt = DatabaseManager
                .getDb()
                .child("zaznamy")
                .child(nazovUctu);

        pouzivanyListner = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                Log.i(TAG, "onDataChange: linechart nove data");
                if (!dataSnapshot.exists()) {
                    ziadneData.setVisibility(View.VISIBLE);
                    lineChart.setVisibility(View.GONE);
                    return;
                } else {
                    ziadneData.setVisibility(View.GONE);
                    lineChart.setVisibility(View.VISIBLE);
                }

                Double[] vydajePoMesiacoch = {0., 0., 0., 0., 0., 0., 0., 0., 0., 0., 0., 0.};
                Double[] prijmyPoMesiacoch = {0., 0., 0., 0., 0., 0., 0., 0., 0., 0., 0., 0.};
                String menaZnak = null;
                int najmensiRok = Integer.MAX_VALUE;

                for (DataSnapshot mesiac: dataSnapshot.getChildren()) {

                    String cisloRokuString = mesiac.getKey().substring(0, 4);
                    najmensiRok = Math.min(Integer.parseInt(cisloRokuString), najmensiRok);

                    if (!mesiac.getKey().contains(String.valueOf(calendarPreGrafy.get(Calendar.YEAR)))) {
                        continue;
                    }

                    String cisloMesiacaString = mesiac.getKey().substring(5);

                    for (DataSnapshot zaznamSnapshot: mesiac.getChildren()) {
                        VlozenyZaznam vlozenyZaznam = zaznamSnapshot.getValue(VlozenyZaznam.class);

                        menaZnak = vlozenyZaznam.getMena().getZnak();
                        if (vlozenyZaznam.getTypZaznamu() == TypZaznamu.PRIJEM) {
                            Prijem prijem = zaznamSnapshot.getValue(Prijem.class);

                            Double povodnaSuma = prijmyPoMesiacoch[Integer.parseInt(cisloMesiacaString) - 1];
                            prijmyPoMesiacoch[Integer.parseInt(cisloMesiacaString) - 1] = povodnaSuma + prijem.getSuma();

                        } else {
                            Vydaj vydaj = zaznamSnapshot.getValue(Vydaj.class);

                            Double povodnaSuma = vydajePoMesiacoch[Integer.parseInt(cisloMesiacaString) - 1];
                            vydajePoMesiacoch[Integer.parseInt(cisloMesiacaString) - 1] = povodnaSuma + vydaj.getSuma();
                        }
                    }
                }

                List<Entry> vydajePoMesiacochList = new ArrayList<>();
                List<Entry> prijmyPoMesiacochList = new ArrayList<>();
                Calendar aktualnyCalendar =  Calendar.getInstance();


                if (najmensiRok > calendarPreGrafy.get(Calendar.YEAR) ||
                        aktualnyCalendar.get(Calendar.YEAR) < calendarPreGrafy.get(Calendar.YEAR)) {

                    ziadneData.setVisibility(View.VISIBLE);
                    lineChart.setVisibility(View.GONE);
                    return;

                }

                for (int i = 0; i < 12; i++) {

                    if (aktualnyCalendar.get(Calendar.YEAR) == calendarPreGrafy.get(Calendar.YEAR) && i > aktualnyCalendar.get(Calendar.MONTH)) {
                        continue;
                    }

                    vydajePoMesiacochList.add(new Entry(i, vydajePoMesiacoch[i].floatValue()));
                    prijmyPoMesiacochList.add(new Entry(i, prijmyPoMesiacoch[i].floatValue()));

                }


                LineDataSet set1 = new LineDataSet(vydajePoMesiacochList, "");
                set1.setDrawCircles(true);
                set1.setColor(ContextCompat.getColor(getContext(), R.color.red_400));
                set1.setCircleColor(ContextCompat.getColor(getContext(), R.color.red_800));
                set1.setLineWidth(5f);//line size
                set1.setCircleRadius(5f);
                set1.setDrawCircleHole(true);
                set1.setValueTextSize(15f);
                set1.setDrawFilled(true);
                set1.setFormLineWidth(5f);
                set1.setFillColor(Color.WHITE);
                set1.setDrawValues(true);
                set1.setValueFormatter(new yAxisFormatter(menaZnak));

                LineDataSet set2 = new LineDataSet(prijmyPoMesiacochList, "");
                set2.setDrawCircles(true);
                set2.setColor(ContextCompat.getColor(getContext(), R.color.green_400));
                set2.setCircleColor(ContextCompat.getColor(getContext(), R.color.green_800));
                set2.setLineWidth(5f);//line size
                set2.setCircleRadius(5f);
                set2.setDrawCircleHole(true);
                set2.setValueTextSize(15f);
                set2.setDrawFilled(true);
                set2.setFormLineWidth(5f);
                set2.setFillColor(Color.WHITE);
                set2.setDrawValues(true);
                set2.setValueFormatter(new yAxisFormatter(menaZnak));

                ArrayList<ILineDataSet> dataSets = new ArrayList<>();
                dataSets.add(set1);
                dataSets.add(set2);

                LineData data = new LineData(dataSets);


                lineChart.removeAllViews();

                lineChart.setData(data);

                lineChart.setPinchZoom(false);
                lineChart.setExtraOffsets(30.f, 0.f, 30.f, 20.f);

                lineChart.getDescription().setEnabled(false);
                lineChart.getLegend().setEnabled(false);

                lineChart.getAxisLeft().setDrawGridLines(false);
                lineChart.getXAxis().setDrawGridLines(false);
                lineChart.getAxisRight().setDrawGridLines(false);
                lineChart.getAxisRight().setEnabled(false);

                lineChart.getAxisLeft().setEnabled(false);
                lineChart.getXAxis().setLabelRotationAngle(315f);
                lineChart.getXAxis().setPosition(XAxis.XAxisPosition.BOTTOM);

                lineChart.getXAxis().setGranularity(1f);
                lineChart.getXAxis().setGranularityEnabled(true);
                lineChart.setVisibleXRangeMaximum(4);
                lineChart.setVisibleXRangeMinimum(4);

                lineChart.getXAxis().setValueFormatter(new xAxisFormatter());
                lineChart.animateXY(1000, 1000);

                showGrafy();
                Log.i(TAG, "onDataChange: DONE - zobrazujem graf");
                
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        };

        databaseReferenceListeningAt.addValueEventListener(pouzivanyListner);
    }

    private class xAxisFormatter extends ValueFormatter {

        @Override
        public String getAxisLabel(float value, AxisBase axis) {
            return MESIACE_SLOVENSKY[(int)value];
        }
    }

    private class yAxisFormatter extends ValueFormatter {

        private String znak;

        public yAxisFormatter(String znak) {
            this.znak = znak;
        }

        @Override
        public String getPointLabel(Entry entry) {
            if ((znak.equals("btc") || znak.equals("eth")) && entry.getY() != 0f) {
                return String.format("%.8f%s", entry.getY(), znak);
            }
            return String.format("%.2f%s", entry.getY(), znak);
        }

    }

    private class MenaValueFormatter extends ValueFormatter {

        private String znak;
        
        public MenaValueFormatter(String znak) {
            this.znak = znak;
        }

        @Override
        public String getFormattedValue(float value) {

            if ((znak.equals("btc") || znak.equals("eth")) && value != 0f) {
                return String.format("%.8f%s", value, znak);
            }
            return String.format("%.2f%s", value, znak);
        }
    }

    /*
    private class SelectedItemListener implements View.OnClickListener {

        @Override
        public void onClick(View v) {

            switch (v.getId()) {
                case R.id.prehlady_ucet_1_layout:
                    nastavPozadie(ucet1, selectedUcetPrehladu);
                    break;
                case R.id.prehlady_ucet_2_layout:
                    nastavPozadie(ucet2, selectedUcetPrehladu);
                    break;
                case R.id.prehlady_ucet_3_layout:
                    nastavPozadie(ucet3, selectedUcetPrehladu);
                    break;
                case R.id.prehlady_ucet_4_layout:
                    nastavPozadie(ucet4, selectedUcetPrehladu);
                    break;
                case R.id.prehlady_ucet_5_layout:
                    nastavPozadie(ucet5, selectedUcetPrehladu);
                    break;
                case R.id.prehlady_piechart_card:
                    nastavPozadie(pieChartCardButton, selectedTypGrafu);
                    break;
                case R.id.prehlady_linechart_card:
                    nastavPozadie(lineChartCardButton, selectedTypGrafu);
                    break;
                case R.id.prehlady_ucel_jedlo_card:
                    nastavPozadie(jedlo, selectedUcelZaznamu);
                    break;
                case R.id.prehlady_ucel_cestovanie_card:
                    nastavPozadie(cestovanie, selectedUcelZaznamu);
                    break;
                case R.id.prehlady_ucel_elektro_card:
                    nastavPozadie(elektro, selectedUcelZaznamu);
                    break;
                case R.id.prehlady_ucel_sport_card:
                    nastavPozadie(sport, selectedUcelZaznamu);
                    break;
                case R.id.prehlady_ucel_car_card:
                    nastavPozadie(auto, selectedUcelZaznamu);
                    break;
                case R.id.prehlady_ucel_family_card:
                    nastavPozadie(rodina, selectedUcelZaznamu);
                    break;
                case R.id.prehlady_ucel_games_card:
                    nastavPozadie(hry, selectedUcelZaznamu);
                    break;
                case R.id.prehlady_ucel_cloth_card:
                    nastavPozadie(oblecenie, selectedUcelZaznamu);
                    break;
            }
        }

    }

     */

}
