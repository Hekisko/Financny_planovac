package sk.bak.adapters;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import java.text.SimpleDateFormat;
import java.util.List;

import sk.bak.R;
import sk.bak.managers.DatabaseManager;
import sk.bak.model.TrvalyPrikaz;
import sk.bak.model.Vydaj;
import sk.bak.model.enums.Meny;
import sk.bak.model.enums.TypZaznamu;
import sk.bak.model.abst.VlozenyZaznam;

public class DialogTrvalePrikazyViewAdapter extends RecyclerView.Adapter<DialogTrvalePrikazyViewAdapter.TrvalePrikazyViewHolder> {

    private static final String TAG = "DialogTrvalePrikazyViewAdapter";

    private Context context;

    private List<VlozenyZaznam> zoznamTrvalychPrikazovRecyclerView;


    public DialogTrvalePrikazyViewAdapter(Context context, List<VlozenyZaznam> zoznamTrvalychPrikazovRecyclerView) {
        this.context = context;
        this.zoznamTrvalychPrikazovRecyclerView = zoznamTrvalychPrikazovRecyclerView;

    }

    @NonNull
    @Override
    public TrvalePrikazyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(context).inflate(R.layout.trvaly_prikaz_item, parent, false);
        return new TrvalePrikazyViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull TrvalePrikazyViewHolder holder, int position) {

        VlozenyZaznam trvalyPrikaz = zoznamTrvalychPrikazovRecyclerView.get(position);

        if (trvalyPrikaz.getTypZaznamu() == TypZaznamu.PRIJEM) {

            if (trvalyPrikaz.getMena() == Meny.BTC || trvalyPrikaz.getMena() == Meny.ETH) {
                holder.suma.setText("+ " + String.format("%.8f", trvalyPrikaz.getSuma()) + " " + trvalyPrikaz.getMena().getZnak());
            } else {
                holder.suma.setText("+ " + String.format("%.2f", trvalyPrikaz.getSuma()) + " " + trvalyPrikaz.getMena().getZnak());
            }
            holder.suma.setTextColor(ContextCompat.getColor(context, R.color.green_400));
            holder.icona.setVisibility(View.INVISIBLE);

        } else {

            if (trvalyPrikaz.getMena() == Meny.BTC || trvalyPrikaz.getMena() == Meny.ETH) {
                holder.suma.setText("- " + String.format("%.8f", trvalyPrikaz.getSuma()) + " " + trvalyPrikaz.getMena().getZnak());
            } else {
                holder.suma.setText("- " + String.format("%.2f", trvalyPrikaz.getSuma()) + " " + trvalyPrikaz.getMena().getZnak());
            }
            holder.suma.setTextColor(ContextCompat.getColor(context, R.color.red_400));

            Vydaj vydaj = (Vydaj)trvalyPrikaz;
            if (vydaj.getTypVydaju() != null) {

                holder.icona.setVisibility(View.VISIBLE);

                switch (vydaj.getTypVydaju()) {
                    case STRAVA:
                        holder.icona.setImageDrawable(ContextCompat.getDrawable(context, R.drawable.ic_food));
                        break;
                    case CESTOVANIE:
                        holder.icona.setImageDrawable(ContextCompat.getDrawable(context, R.drawable.ic_mountins));
                        break;
                    case ELEKTRO:
                        holder.icona.setImageDrawable(ContextCompat.getDrawable(context, R.drawable.ic_elektro));
                        break;
                    case SPORT:
                        holder.icona.setImageDrawable(ContextCompat.getDrawable(context, R.drawable.ic_cinka));
                        break;
                    case DOPRAVA:
                        holder.icona.setImageDrawable(ContextCompat.getDrawable(context, R.drawable.ic_car));
                        break;
                    case RODINA:
                        holder.icona.setImageDrawable(ContextCompat.getDrawable(context, R.drawable.ic_family));
                        break;
                    case ZAVABA:
                        holder.icona.setImageDrawable(ContextCompat.getDrawable(context, R.drawable.ic_gamepad));
                        break;
                    case OBLECENIE:
                        holder.icona.setImageDrawable(ContextCompat.getDrawable(context, R.drawable.ic_tshirt));
                        break;
                    case HOUSE:
                        holder.icona.setImageDrawable(ContextCompat.getDrawable(context, R.drawable.ic_house));
                        break;
                    case DROGERIA:
                        holder.icona.setImageDrawable(ContextCompat.getDrawable(context, R.drawable.ic_drogeria));
                        break;
                    case OSTATNE:
                        holder.icona.setImageDrawable(ContextCompat.getDrawable(context, R.drawable.ic_question_mark));
                        break;
                    case ANIMAL:
                        holder.icona.setImageDrawable(ContextCompat.getDrawable(context, R.drawable.ic_animal));
                        break;
                }
            } else {
                holder.icona.setVisibility(View.INVISIBLE);
            }
        }

        holder.denSplatnosti.setText(String.valueOf(trvalyPrikaz.getDenSplatnosti()));

        if (trvalyPrikaz.getPoznamka().equals("")) {
            holder.poznamkaLayout.setVisibility(View.GONE);
        } else {
            holder.poznamka.setText(trvalyPrikaz.getPoznamka());
            holder.poznamkaLayout.setVisibility(View.VISIBLE);
        }

        holder.delete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.i(TAG, "onClick: idem mazat trvaly prikaz");
                DatabaseManager.deleteTrvalyPrikaz(trvalyPrikaz.getNazovUctu(), trvalyPrikaz.getId());
            }
        });


        holder.datumPridania.setText(new SimpleDateFormat("dd.MM.yyyy").format(trvalyPrikaz.getCasZadania()));

    }

    @Override
    public int getItemCount() {
        return zoznamTrvalychPrikazovRecyclerView.size();
    }

    public static class TrvalePrikazyViewHolder extends RecyclerView.ViewHolder {

        TextView suma;
        ImageView icona;
        TextView denSplatnosti;
        ImageButton delete;
        TextView poznamka;
        TextView datumPridania;
        LinearLayout poznamkaLayout;

        public TrvalePrikazyViewHolder(@NonNull View itemView) {
            super(itemView);

            suma = itemView.findViewById(R.id.trvaly_prikaz_item_suma);
            icona = itemView.findViewById(R.id.trvaly_prikaz_item_icon);
            denSplatnosti = itemView.findViewById(R.id.trvaly_prikaz_item_den_splatnosti);
            delete = itemView.findViewById(R.id.trvaly_prikaz_item_delete);
            poznamka = itemView.findViewById(R.id.trvaly_prikaz_item_poznamka);
            datumPridania = itemView.findViewById(R.id.trvaly_prikaz_item_den_pridania);
            poznamkaLayout = itemView.findViewById(R.id.trvaly_prikaz_item_poznamka_layout);
        }
    }
}

