package sk.bak.dialogs;

import android.app.Activity;
import android.app.Dialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Color;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextSwitcher;
import android.widget.TextView;
import android.widget.ViewSwitcher;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import sk.bak.R;
import sk.bak.adapters.DialogZaznamyViewAdapter;
import sk.bak.fragmenty.PrehladyFragment;
import sk.bak.managers.DatabaseManager;
import sk.bak.model.Prijem;
import sk.bak.model.enums.Meny;
import sk.bak.model.enums.TypPrijmu;
import sk.bak.model.enums.TypVydaju;
import sk.bak.model.abst.VlozenyZaznam;
import sk.bak.model.Vydaj;
import sk.bak.model.enums.TypZaznamu;

public class ZobrazenieZaznamovDialog extends Dialog {

    private final String[] MESIACE_SLOVENSKY = {"Január", "Február", "Marec", "Apríl", "Máj", "Jún", "Júl", "August", "September", "Október", "November", "Decemnber"};

    private RecyclerView recyclerView;
    private DialogZaznamyViewAdapter adapterRecyclerView;
    private List<VlozenyZaznam> zoznamZaznamov;

    private Activity parentActivity;

    //private TahanieDataZDBBradcastReciever serviceReceiver;

    private String nazovUctu;

    private LinearLayout datumLayout;
    private ImageButton datumSpat;
    private ImageButton datumDopredu;
    private TextSwitcher datum;
    private Animation animationInBack;
    private Animation animationOutBack;
    private Animation animationInForw;
    private Animation animationOuForw;
    private Calendar calendarPrePosun;
    private View.OnClickListener posunDatumuListener;

    private ValueEventListener valueEventListener;

    private static final String TAG = "ZobrazenieZaznamovDialog";

    public ZobrazenieZaznamovDialog(Activity activity, String nazovUctu) {
        super(activity);

        parentActivity = activity;
        this.nazovUctu = nazovUctu;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        final View customLayout = getLayoutInflater().inflate(R.layout.dialog_zobrazenie_zaznamov, null);
        setContentView(customLayout);

        DisplayMetrics displayMetrics = new DisplayMetrics();
        parentActivity.getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
        int displayWidth = displayMetrics.widthPixels;
        int displayHeight = displayMetrics.heightPixels;
        WindowManager.LayoutParams layoutParams = new WindowManager.LayoutParams();
        layoutParams.copyFrom(getWindow().getAttributes());
        int dialogWindowWidth = (int) (displayWidth * 0.99f);
        layoutParams.width = dialogWindowWidth;
        int dialogWindowHeight = (int) (displayHeight * 0.8f);
        layoutParams.height = dialogWindowHeight;

        getWindow().setAttributes(layoutParams);


        initPosunDatumu();

        recyclerView = findViewById(R.id.zobrazenie_zaznamov_recycler_view);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));


        zoznamZaznamov = new ArrayList<>();


       /* Prijem prijem = new Prijem(TypPrijmu.DAR, 100., Meny.EUR, Calendar.getInstance().getTime());
        Prijem prijem1 = new Prijem(TypPrijmu.DAR, 200., Meny.EUR, Calendar.getInstance().getTime());
        Vydaj vydaj = new Vydaj(TypVydaju.STRAVA, 25., Meny.EUR, Calendar.getInstance().getTime());

        zoznamZaznamov.add(prijem);
        zoznamZaznamov.add(prijem1);
        zoznamZaznamov.add(vydaj);

        */

        adapterRecyclerView = new DialogZaznamyViewAdapter(getContext(), zoznamZaznamov);

        recyclerView.setAdapter(adapterRecyclerView);

        //DatabaseManager.getVsetkyZaznamyZaMesiacUctu(nazovUctu, Calendar.getInstance(), null, getContext());
        fillRecyclerView();


        adapterRecyclerView.notifyDataSetChanged();

        //serviceReceiver = new TahanieDataZDBBradcastReciever();
        //IntentFilter intentSFilter = new IntentFilter("dataOk");
        //getContext().registerReceiver(serviceReceiver, intentSFilter);

    }

    private void initPosunDatumu() {

        datumLayout = findViewById(R.id.zobrazenie_zaznamov_layout_pre_posun_datumu);
        datumSpat = findViewById(R.id.zobrazenie_zaznamov_dozadu_datum);
        datumDopredu = findViewById(R.id.zobrazenie_zaznamov_dopredu_datum);
        datum = findViewById(R.id.zobrazenie_zaznamov_text_switcher);
        animationInBack = AnimationUtils.loadAnimation(getContext(), R.anim.slide_in_left);
        animationOutBack = AnimationUtils.loadAnimation(getContext(), R.anim.slide_out_right);
        animationInForw = AnimationUtils.loadAnimation(getContext(), R.anim.slide_in_right);
        animationOuForw = AnimationUtils.loadAnimation(getContext(), R.anim.slide_out_left);
        posunDatumuListener = new PosunDatumu();

        calendarPrePosun = Calendar.getInstance();

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

        datum.setText(MESIACE_SLOVENSKY[calendarPrePosun.get(Calendar.MONTH)] +
                " " +
                calendarPrePosun.get(Calendar.YEAR));

    }

    @Override
    protected void onStop() {
        super.onStop();

        Log.i(TAG, "onStop: odstranujem listner");

        if (valueEventListener != null) {
            DatabaseManager.getDb()
                    .child("zaznamy")
                    .child(nazovUctu)
                    .child(calendarPrePosun.get(Calendar.YEAR) + "_" + (calendarPrePosun.get(Calendar.MONTH) + 1))
                    .removeEventListener(valueEventListener);
        }
    }


    private class PosunDatumu implements View.OnClickListener {

        @Override
        public void onClick(View v) {

            DatabaseManager.getDb()
                    .child("zaznamy")
                    .child(nazovUctu)
                    .child(calendarPrePosun.get(Calendar.YEAR) + "_" + (calendarPrePosun.get(Calendar.MONTH) + 1))
                    .removeEventListener(valueEventListener);

            if (v.getId() == R.id.zobrazenie_zaznamov_dozadu_datum) {

                datum.setInAnimation(animationInBack);
                datum.setOutAnimation(animationOutBack);

                calendarPrePosun.add(Calendar.MONTH, -1);

            } else {

                datum.setInAnimation(animationInForw);
                datum.setOutAnimation(animationOuForw);

                calendarPrePosun.add(Calendar.MONTH, 1);

            }

            datum.setText(MESIACE_SLOVENSKY[calendarPrePosun.get(Calendar.MONTH)] +
                    " " +
                    calendarPrePosun.get(Calendar.YEAR));

            fillRecyclerView();
        }
    }

    private void fillRecyclerView() {

        valueEventListener = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                Log.i(TAG, "onDataChange: prisli nove data");

                List<VlozenyZaznam> zaznamyDb = new ArrayList<>();

                for (DataSnapshot dataSnapshot1 : dataSnapshot.getChildren()) {

                    VlozenyZaznam zaznam = dataSnapshot1.getValue(VlozenyZaznam.class);

                    if (zaznam.getTypZaznamu() == TypZaznamu.PRIJEM) {
                        zaznamyDb.add(dataSnapshot1.getValue(Prijem.class));
                    } else {
                        zaznamyDb.add(dataSnapshot1.getValue(Vydaj.class));
                    }

                }

                zoznamZaznamov.clear();
                zoznamZaznamov.addAll(zaznamyDb);
                adapterRecyclerView.notifyDataSetChanged();
                Log.i(TAG, "onDataChange: nove data su zobrazene");

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        };

        Log.i(TAG, "fillRecyclerView: nastavavujem novy listener");

        DatabaseManager.getDb()
                .child("zaznamy")
                .child(nazovUctu)
                .child(calendarPrePosun.get(Calendar.YEAR) + "_" + (calendarPrePosun.get(Calendar.MONTH) + 1))
                .addValueEventListener(valueEventListener);

    }

    //public class TahanieDataZDBBradcastReciever extends BroadcastReceiver {
//
    //    @Override
    //    public void onReceive(Context context, Intent intent) {
//
//
    //        if (intent.getBooleanExtra("ended", false)) {
//
    //            zoznamZaznamov.clear();
    //            zoznamZaznamov.addAll(DatabaseManager.getZaznamy());
    //            adapterRecyclerView.notifyDataSetChanged();
//
    //        }
    //    }
    //}



}

