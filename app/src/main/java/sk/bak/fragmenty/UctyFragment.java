package sk.bak.fragmenty;

import android.content.DialogInterface;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

import sk.bak.R;
import sk.bak.adapters.UctyRecyclerViewAdapter;
import sk.bak.dialogs.PridajNovyUcet;
import sk.bak.managers.DatabaseManager;
import sk.bak.model.BeznyUcet;
import sk.bak.model.CryptoUcet;
import sk.bak.model.SporiaciUcet;
import sk.bak.model.enums.TypyUctov;
import sk.bak.model.abst.Ucet;


/**
 *
 * Trieda fragmentu Ucty
 *
 */
public class UctyFragment extends Fragment {

    private static final String  TAG = "UctyFragment";

    // Pomocne premenne
    private DatabaseReference database;
    private View currentView;


    // Datove premenne
    private List<Ucet> zoznamUctov;

    // UI premenne
    private RecyclerView recyclerView;
    private UctyRecyclerViewAdapter adapterRecyclerView;
    private FloatingActionButton pridajUcet;

    // Listenery db
    private ValueEventListener valueEventListener;


    /**
     *
     * Prebieha hlavne inizializacia a nastavovanie RecyclerView
     *
     * @param inflater
     * @param container
     * @param savedInstanceState
     * @return
     */
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        currentView = inflater.inflate(R.layout.ucty_fragment, container, false);

        // nastavovanie RecyclerView
        recyclerView = currentView.findViewById(R.id.ucty_fragment_recyclerView);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        zoznamUctov = new ArrayList<>();
        adapterRecyclerView = new UctyRecyclerViewAdapter(getContext(), zoznamUctov);
        recyclerView.setAdapter(adapterRecyclerView);

        pridajUcet = currentView.findViewById(R.id.ucty_fragment_pridaj_ucet);

        pridajUcet.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Log.i(TAG, "onClick: vyskajue fialog pridaj novy ucet");

                if (adapterRecyclerView.getItemCount() >= 5) {
                    Snackbar.make(currentView, "M????ete ma?? maxim??lne 5 ????tov", Snackbar.LENGTH_LONG)
                            .show();

                    return;
                }

                PridajNovyUcet pridajNovyUcet = new PridajNovyUcet(getActivity());

                pridajNovyUcet.setOnCancelListener(new DialogInterface.OnCancelListener() {
                    @Override
                    public void onCancel(DialogInterface dialog) {

                    }
                });

                pridajNovyUcet.show();
            }
        });


        return currentView;
    }


    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }


    /**
     *
     * onResume zakjlada listner a nasledne naplni komponenty UI
     *
     */
    @Override
    public void onResume() {
        super.onResume();

        valueEventListener = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                Log.i(TAG, "onDataChange: nove data ucty START");

                List<Ucet> ucty = new ArrayList<>();

                for (DataSnapshot dataSnapshot1: dataSnapshot.getChildren()) {

                    Ucet ucet = dataSnapshot1.getValue(Ucet.class);

                    if (ucet.getTypUctu() == TypyUctov.BEZNY) {
                        ucty.add(dataSnapshot1.getValue(BeznyUcet.class));
                    } else if (ucet.getTypUctu() == TypyUctov.SPORIACI) {
                        ucty.add(dataSnapshot1.getValue(SporiaciUcet.class));
                    } else {
                        ucty.add(dataSnapshot1.getValue(CryptoUcet.class));
                    }

                }

                zoznamUctov.clear();
                zoznamUctov.addAll(ucty);
                adapterRecyclerView.notifyDataSetChanged();
                Log.i(TAG, "onDataChange:nove data ucty DONE");
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        };

        DatabaseManager.getDb().child("ucty").addValueEventListener(valueEventListener);
    }


    /**
     *
     * Po skonceni je treba odregistrovat listenery
     *
     */
    @Override
    public void onPause() {
        super.onPause();
        DatabaseManager.getDb().child("ucty").removeEventListener(valueEventListener);
    }
}
