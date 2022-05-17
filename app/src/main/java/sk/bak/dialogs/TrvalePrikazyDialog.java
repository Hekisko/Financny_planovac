package sk.bak.dialogs;

import android.app.Activity;
import android.app.Dialog;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;

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
import sk.bak.adapters.DialogTrvalePrikazyViewAdapter;
import sk.bak.managers.DatabaseManager;
import sk.bak.model.Prijem;
import sk.bak.model.TrvalyPrikaz;
import sk.bak.model.Vydaj;
import sk.bak.model.abst.VlozenyZaznam;
import sk.bak.model.enums.TypVydaju;
import sk.bak.model.enums.TypZaznamu;

/**
 *
 * Trieda dialog pre trvale prikazy
 *
 */
public class TrvalePrikazyDialog extends Dialog {

    private static final String TAG = "TrvalePrikazyDialog";

    // Pomocne premenne
    private Activity parentActivity;
    private String nazovUctu;

    // Datove premenne
    private List<VlozenyZaznam> zoznamTrvalychPrikazov;

    // UI premenne
    private RecyclerView recyclerView;
    private DialogTrvalePrikazyViewAdapter adapterRecyclerView;

    // Listenry db
    private ValueEventListener valueEventListener;


    public TrvalePrikazyDialog(Activity activity, String nazovUctu) {
        super(activity);

        parentActivity = activity;
        this.nazovUctu = nazovUctu;

    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        final View customLayout = getLayoutInflater().inflate(R.layout.dialog_trvale_prikazy, null);
        setContentView(customLayout);

        // Nastavenie vyskakovacieho okna
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

        // RecyclerView fill
        recyclerView = findViewById(R.id.dialog_trvale_prikazy_recycler_view);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        zoznamTrvalychPrikazov = new ArrayList<>();
        adapterRecyclerView = new DialogTrvalePrikazyViewAdapter(getContext(), zoznamTrvalychPrikazov);
        recyclerView.setAdapter(adapterRecyclerView);
        fillRecyclerView();


    }


    /**
     *
     * Po ukonceni je treba listener odregistrovat
     *
     */
    @Override
    protected void onStop() {
        super.onStop();
        if (valueEventListener != null) {
            DatabaseManager.getDb()
                    .child("trvalePrikazy")
                    .child(nazovUctu)
                    .removeEventListener(valueEventListener);
        }
    }

    /**
     *
     * Pomocna metoda na naplenie recyclerView
     *
     */
    private void fillRecyclerView() {

        valueEventListener = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                Log.i(TAG, "onDataChange: ziskal som nove data");

                List<VlozenyZaznam> zaznamyDb = new ArrayList<>();

                for (DataSnapshot trvalyPrikaz: dataSnapshot.getChildren()) {

                    TrvalyPrikaz trvalyPrikazSporiaci = trvalyPrikaz.getValue(TrvalyPrikaz.class);

                    if (trvalyPrikazSporiaci.isSporiaci()) {
                        continue;
                    }

                    for (DataSnapshot zaznam: trvalyPrikaz.getChildren()) {

                        if (zaznam.getKey().equals("zaznam")) {
                            VlozenyZaznam zaznamVlozeny = zaznam.getValue(VlozenyZaznam.class);

                            if (zaznamVlozeny.getTypZaznamu() == TypZaznamu.PRIJEM) {
                                zaznamyDb.add(zaznam.getValue(Prijem.class));
                            } else {
                                zaznamyDb.add(zaznam.getValue(Vydaj.class));
                            }
                        }
                    }
                }

                zoznamTrvalychPrikazov.clear();
                zoznamTrvalychPrikazov.addAll(zaznamyDb);
                adapterRecyclerView.notifyDataSetChanged();
                Log.i(TAG, "onDataChange: data su aktualizovane");

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        };


        // priraduje dany listener
        DatabaseManager.getDb()
                .child("trvalePrikazy")
                .child(nazovUctu)
                .addValueEventListener(valueEventListener);

    }
}
