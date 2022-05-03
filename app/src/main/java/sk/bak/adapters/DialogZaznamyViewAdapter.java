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
import java.util.Calendar;
import java.util.List;

import sk.bak.R;
import sk.bak.managers.DatabaseManager;
import sk.bak.model.Vydaj;
import sk.bak.model.enums.Meny;
import sk.bak.model.enums.TypVydaju;
import sk.bak.model.enums.TypZaznamu;
import sk.bak.model.abst.VlozenyZaznam;

public class DialogZaznamyViewAdapter extends RecyclerView.Adapter<DialogZaznamyViewAdapter.ZaznamyViewHolder> {


    private Context context;

    private List<VlozenyZaznam> zoznamZaznamovRecyclerView;

    private static final String TAG = "DialogZaznamyViewAdapter";

    public DialogZaznamyViewAdapter(Context context, List<VlozenyZaznam> zoznamZaznamovRecyclerView) {
        this.context = context;
        this.zoznamZaznamovRecyclerView = zoznamZaznamovRecyclerView;

    }

    @NonNull
    @Override
    public ZaznamyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(context).inflate(R.layout.zaznam_item, parent, false);
        return new ZaznamyViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull ZaznamyViewHolder holder, int position) {

        VlozenyZaznam zaznam = zoznamZaznamovRecyclerView.get(position);


        if (zaznam.getTypZaznamu() == TypZaznamu.PRIJEM) {

            if (zaznam.getMena() == Meny.BTC || zaznam.getMena() == Meny.ETH) {
                holder.suma.setText("+ " + String.format("%.8f", zaznam.getSuma()) + " " + zaznam.getMena().getZnak());
            } else {
                holder.suma.setText("+ " + String.format("%.2f", zaznam.getSuma()) + " " + zaznam.getMena().getZnak());
            }
            holder.suma.setTextColor(ContextCompat.getColor(context, R.color.green_400));
            holder.icona.setVisibility(View.INVISIBLE);

        } else {

            if (zaznam.getMena() == Meny.BTC || zaznam.getMena() == Meny.ETH) {
                holder.suma.setText("- " + String.format("%.8f", zaznam.getSuma()) + " " + zaznam.getMena().getZnak());
            } else {
                holder.suma.setText("- " + String.format("%.2f", zaznam.getSuma()) + " " + zaznam.getMena().getZnak());
            }
            holder.suma.setTextColor(ContextCompat.getColor(context, R.color.red_400));

            Vydaj vydaj = (Vydaj)zaznam;
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


        holder.delete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Calendar calendar = Calendar.getInstance();
                calendar.setTime(zaznam.getCasZadania());

                Log.i(TAG, "onClick: spustam mazanie itemu");
                DatabaseManager.deleteZaznam(DatabaseManager.getDb().child("zaznamy").child(zaznam.getNazovUctu()).child(calendar.get(Calendar.YEAR) + "_" + (calendar.get(Calendar.MONTH) + 1)),
                        zaznam.getId(),
                        zaznam.getSuma(),
                        zaznam.getTypZaznamu(),
                        zaznam.getNazovUctu());

            }
        });

        if (zaznam.getPoznamka().equals("")) {
            holder.poznamkaLayout.setVisibility(View.GONE);
        } else {
            holder.poznamka.setText(zaznam.getPoznamka());
            holder.poznamkaLayout.setVisibility(View.VISIBLE);
        }


        holder.datumPridania.setText(new SimpleDateFormat("dd.MM.yyyy").format(zaznam.getCasZadania()));
    }

    @Override
    public int getItemCount() {
        return zoznamZaznamovRecyclerView.size();
    }

    public static class ZaznamyViewHolder extends RecyclerView.ViewHolder {

        TextView suma;
        ImageView icona;
        ImageButton delete;
        TextView poznamka;
        TextView datumPridania;
        LinearLayout poznamkaLayout;


        public ZaznamyViewHolder(@NonNull View itemView) {
            super(itemView);

            suma = itemView.findViewById(R.id.zaznam_item_suma);
            icona = itemView.findViewById(R.id.zaznam_item_icon);
            delete = itemView.findViewById(R.id.zaznam_item_delete);
            datumPridania = itemView.findViewById(R.id.zaznam_item_den_pridania);
            poznamka = itemView.findViewById(R.id.zaznam_item_poznamka);
            poznamkaLayout = itemView.findViewById(R.id.zaznam_item_poznamka_layout);
        }
    }
}
