package sk.bak.adapters;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.media.Image;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

import sk.bak.R;
import sk.bak.dialogs.PridajNovyUcet;
import sk.bak.dialogs.TrvalePrikazyDialog;
import sk.bak.dialogs.ZobrazenieZaznamovDialog;
import sk.bak.managers.DatabaseManager;
import sk.bak.model.abst.Ucet;
import sk.bak.model.enums.Meny;
import sk.bak.model.enums.TypyUctov;
import sk.bak.utils.MySharedPreferences;

public class UctyRecyclerViewAdapter extends RecyclerView.Adapter<UctyRecyclerViewAdapter.UctyViewHolder> {


    private Context context;

    private List<Ucet> zoznamUctovRecyclerView;

    private MySharedPreferences sharedPreferences;

    private static final String TAG = "UctyRecyclerViewAdapter";

    public UctyRecyclerViewAdapter(Context context, List<Ucet> zoznamUctovRecyclerView) {
        this.context = context;
        this.zoznamUctovRecyclerView = zoznamUctovRecyclerView;

        sharedPreferences = new MySharedPreferences(context);

    }

    @NonNull
    @Override
    public UctyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(context).inflate(R.layout.ucty_item, parent, false);
        return new UctyViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull UctyViewHolder holder, int position) {

        Ucet ucet = zoznamUctovRecyclerView.get(position);
        holder.nazovUctu.setText(ucet.getNazov());
        holder.typUctu.setText(ucet.getTypUctu().getName());

        if (ucet.getMena() == Meny.BTC || ucet.getMena() == Meny.ETH) {
            holder.aktualnyZostatok.setText(String.format("%.8f", ucet.getAktualnyZostatok()));
        } else {
            holder.aktualnyZostatok.setText(String.format("%.2f", ucet.getAktualnyZostatok()));
        }

        holder.menaUctu.setText(ucet.getMena().getMena());

        if (ucet.getTypUctu() == TypyUctov.BEZNY) {
            holder.jeHlavny.setVisibility(View.VISIBLE);
            if (ucet.isJeHlavnyUcet()) {
                holder.jeHlavny.setImageDrawable(ContextCompat.getDrawable(context, R.drawable.ic_baseline_star_24_selected));
            } else {
                holder.jeHlavny.setImageDrawable(ContextCompat.getDrawable(context, R.drawable.ic_baseline_star_24));
            }

            List<Ucet> ucty = new ArrayList<>();
            int counter = 0;

            while((ucty == null || ucty.isEmpty()) && counter < 5) {

                ucty = DatabaseManager.getUcty();
                counter++;
            }

            if (counter > 4) {
                AlertDialog.Builder chyba = new AlertDialog.Builder(context);
                chyba.setPositiveButton("Ok", null);
                chyba.setTitle("Nastala chyba");
                chyba.setMessage("Nastala naočakávaná chyba. Pokus opakujte. Pokiaľ chyba pretrváva, kotaktujte podporu.");
                chyba.create().show();
                return;
            }

            Ucet aktualneHlavnyUcet = null;

            for (Ucet ucet1: ucty) {
                if (ucet1.isJeHlavnyUcet()) {
                    aktualneHlavnyUcet = ucet1;
                }
            }
            Ucet finalAktualneHlavnyUcet = aktualneHlavnyUcet;

            holder.jeHlavny.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (ucet.isJeHlavnyUcet()) {
                        holder.jeHlavny.setImageDrawable(ContextCompat.getDrawable(context, R.drawable.ic_baseline_star_24));
                        ucet.setJeHlavnyUcet(false);
                    } else if (finalAktualneHlavnyUcet != null){

                        Toast.makeText(context, "Len jeden účet môže byť označený ako hlavný", Toast.LENGTH_LONG).show();

                    } else {
                        holder.jeHlavny.setImageDrawable(ContextCompat.getDrawable(context, R.drawable.ic_baseline_star_24_selected));
                        ucet.setJeHlavnyUcet(true);
                    }

                    Log.i(TAG, "onClick: ukladam ucet po zmene ci je hlavny");
                    DatabaseManager.saveUcet(ucet);
                }
            });
        } else {
            holder.jeHlavny.setVisibility(View.INVISIBLE);
        }

        holder.zaznamy.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                ZobrazenieZaznamovDialog zobrazenieZaznamovDialog = new ZobrazenieZaznamovDialog((Activity) context, ucet.getNazov());

                zobrazenieZaznamovDialog.show();
            }
        });

        holder.trvalePrikazy.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                TrvalePrikazyDialog trvalePrikazyDialog = new TrvalePrikazyDialog((Activity) context, ucet.getNazov());

                trvalePrikazyDialog.show();

            }
        });

        holder.deleteUcet.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AlertDialog.Builder potvrdenieBuilder = new AlertDialog.Builder((Activity) context);

                potvrdenieBuilder.setTitle("Potrvdenie vymazania");
                potvrdenieBuilder.setMessage("Naozaj si želáte vymazať tento účet spolu so všetkými jeho záznamami?");

                potvrdenieBuilder.setPositiveButton("Áno", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        Log.i(TAG, "onClick: mazanie uctu START");
                        DatabaseManager.deleteUcet(ucet.getNazov());
                    }
                });

                potvrdenieBuilder.setNegativeButton("Nie", null);

                potvrdenieBuilder.create().show();
            }
        });

        holder.edit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                PridajNovyUcet pridajNovyUcet = new PridajNovyUcet((Activity) context, ucet);

                pridajNovyUcet.show();
            }
        });

    }

    @Override
    public int getItemCount() {
        return zoznamUctovRecyclerView.size();
    }

    public static class UctyViewHolder extends RecyclerView.ViewHolder {

        TextView nazovUctu;
        TextView typUctu;
        TextView aktualnyZostatok;
        TextView menaUctu;
        ImageButton jeHlavny;
        ImageButton zaznamy;
        ImageButton trvalePrikazy;
        ImageButton deleteUcet;
        ImageButton edit;


        public UctyViewHolder(@NonNull View itemView) {
            super(itemView);

            nazovUctu = itemView.findViewById(R.id.ucty_recyclerview_item_nazov_uctu);
            typUctu = itemView.findViewById(R.id.ucty_recyclerview_item_typ_uctu);
            aktualnyZostatok = itemView.findViewById(R.id.ucty_recyclerview_item_zostatok_uctu);
            menaUctu = itemView.findViewById(R.id.ucty_recyclerview_item_mena_uctu);
            jeHlavny = itemView.findViewById(R.id.ucty_recyclerview_item_je_hlavny_icon);
            zaznamy = itemView.findViewById(R.id.ucty_recyclerview_item_zaznamy_icon);
            trvalePrikazy = itemView.findViewById(R.id.ucty_recyclerview_item_trvale_prikazy_icon);
            deleteUcet = itemView.findViewById(R.id.ucty_recyclerview_item_delete_icon);
            edit = itemView.findViewById(R.id.ucty_recyclerview_item_edit);
        }
    }
}
