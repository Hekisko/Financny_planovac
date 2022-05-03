package sk.bak.dialogs;

import android.app.Activity;
import android.app.Dialog;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.CheckBox;
import android.widget.ImageButton;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.android.material.textfield.TextInputLayout;

import java.util.Calendar;
import java.util.List;

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
import sk.bak.model.enums.TypVydaju;
import sk.bak.model.enums.TypZaznamu;
import sk.bak.model.enums.TypyUctov;

public class PridajNovyUcet extends Dialog {

    private Ucet ucet;
    private boolean prebiehaEdit = false;

    private TextInputLayout typSpinner;
    private TextInputLayout menaSpinner;
    private TextInputLayout nazov;
    private TextInputLayout usetrenaMesacnaSuma;
    private TextInputLayout percentoZuctovania;
    private TextInputLayout poplatokZaVedenieUctu;

    private TypyUctov zvolenyTypUctu;
    private Meny zvolenaMena;

    private Activity parentActivity;
    private Dialog currentDialog;

    private ImageButton back;
    private ImageButton save;

    private static final String TAG = "PridajNovyUcet";

    public PridajNovyUcet(@NonNull Activity activity) {
        super(activity);

        parentActivity = activity;
        currentDialog = this;
        this.ucet = null;
    }

    public PridajNovyUcet(@NonNull Activity activity, Ucet ucet) {
        super(activity);

        parentActivity = activity;
        currentDialog = this;
        this.ucet = ucet;
        this.prebiehaEdit = true;
    }



    @Nullable
    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        final View customLayout = getLayoutInflater().inflate(R.layout.pridaj_novy_ucet_fragment, null);
        setContentView(customLayout);

        DisplayMetrics displayMetrics = new DisplayMetrics();
        parentActivity.getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
        int displayWidth = displayMetrics.widthPixels;
        WindowManager.LayoutParams layoutParams = new WindowManager.LayoutParams();
        layoutParams.copyFrom(getWindow().getAttributes());
        int dialogWindowWidth = (int) (displayWidth * 0.99f);
        layoutParams.width = dialogWindowWidth;
        getWindow().setAttributes(layoutParams);

        initOstatne();
        initSpinnerTypUctu();
        initSpinnerMenaUctu();
        initButtony();

        if (ucet != null) {

            fillDialogWithData();
        }

    }

    private void fillDialogWithData() {

        nazov.getEditText().setText(ucet.getNazov());
        nazov.getEditText().setEnabled(false);
        ((AutoCompleteTextView)typSpinner.getEditText()).setText(ucet.getTypUctu().getName(),false);
        zvolenyTypUctu = ucet.getTypUctu();
        ((AutoCompleteTextView)menaSpinner.getEditText()).setText(ucet.getMena().getSkratka(), false);
        zvolenaMena = ucet.getMena();

        poplatokZaVedenieUctu.getEditText().setText(String.valueOf(ucet.getPoplatokZaVedenie()));

        if (ucet.getTypUctu() == TypyUctov.BEZNY) {
            usetrenaMesacnaSuma.getEditText().setText(String.valueOf(((BeznyUcet) ucet).getChcenaMesacneUsetrenaSuma()));
        }

        if (ucet.getTypUctu() == TypyUctov.SPORIACI) {
            percentoZuctovania.getEditText().setText(String.valueOf(((SporiaciUcet) ucet).getPercentoZuctovania()));
        }

        switch (zvolenyTypUctu) {
            case BEZNY:
                usetrenaMesacnaSuma.setVisibility(View.VISIBLE);
                poplatokZaVedenieUctu.setVisibility(View.VISIBLE);
                percentoZuctovania.setVisibility(View.GONE);
                break;
            case SPORIACI:
                poplatokZaVedenieUctu.setVisibility(View.VISIBLE);
                percentoZuctovania.setVisibility(View.VISIBLE);
                usetrenaMesacnaSuma.setVisibility(View.GONE);
                break;
            case KRYPTO:
                poplatokZaVedenieUctu.setVisibility(View.VISIBLE);
                percentoZuctovania.setVisibility(View.GONE);
                usetrenaMesacnaSuma.setVisibility(View.GONE);
                break;
            default:
                usetrenaMesacnaSuma.setVisibility(View.GONE);
                poplatokZaVedenieUctu.setVisibility(View.GONE);
                percentoZuctovania.setVisibility(View.GONE);
                break;
        }


    }

    private void initButtony() {

        back = findViewById(R.id.pridaj_novy_ucet_fragment_button_back);
        back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                currentDialog.cancel();
            }
        });


        save = findViewById(R.id.pridaj_novy_ucet_fragment_button_vytvor);
        save.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (validateData()) {

                    double aktualnyZozstatok = 0.;

                    if (prebiehaEdit) {
                        aktualnyZozstatok = ucet.getAktualnyZostatok();
                    }

                    Log.i(TAG, "onClick uloz ucet: ukladanie uctu START");
                    switch (zvolenyTypUctu) {
                        case BEZNY:
                            BeznyUcet novyUcet;
                            novyUcet = new BeznyUcet();
                            novyUcet.setNazov(nazov.getEditText().getText().toString());
                            novyUcet.setTypUctu(TypyUctov.BEZNY);
                            novyUcet.setAktualnyZostatok(aktualnyZozstatok);
                            novyUcet.setMena(zvolenaMena);
                            novyUcet.setChcenaMesacneUsetrenaSuma(Double.parseDouble(usetrenaMesacnaSuma.getEditText().getText().toString()));
                            novyUcet.setPoplatokZaVedenie(Double.parseDouble(poplatokZaVedenieUctu.getEditText().getText().toString()));

                            DatabaseManager.saveUcet(novyUcet);

                            break;
                        case SPORIACI:

                            SporiaciUcet novyUcetSpo;
                            novyUcetSpo = new SporiaciUcet();
                            novyUcetSpo.setNazov(nazov.getEditText().getText().toString());
                            novyUcetSpo.setTypUctu(TypyUctov.SPORIACI);
                            novyUcetSpo.setAktualnyZostatok(aktualnyZozstatok);
                            novyUcetSpo.setMena(zvolenaMena);
                            novyUcetSpo.setPoplatokZaVedenie(Double.parseDouble(poplatokZaVedenieUctu.getEditText().getText().toString()));
                            novyUcetSpo.setPercentoZuctovania(Double.parseDouble(percentoZuctovania.getEditText().getText().toString()));

                            DatabaseManager.saveUcet(novyUcetSpo);

                            Prijem zuctovanie = new Prijem();
                            zuctovanie.setMena(zvolenaMena);
                            zuctovanie.setNazovUctu(nazov.getEditText().getText().toString());
                            zuctovanie.setPoznamka("Zúčtovanie");
                            zuctovanie.setTypZaznamu(TypZaznamu.PRIJEM);
                            Calendar calendar = Calendar.getInstance();
                            calendar.add(Calendar.DAY_OF_MONTH, -1);
                            zuctovanie.setDenSplatnosti(calendar.get(Calendar.DAY_OF_MONTH));
                            zuctovanie.setCasZadania(Calendar.getInstance().getTime());

                            DatabaseManager.saveTrvalyPrikazSporiaci(nazov.getEditText().getText().toString(), zuctovanie,Double.parseDouble(percentoZuctovania.getEditText().getText().toString()));


                            break;
                        case KRYPTO:

                            CryptoUcet novyUcetCry;
                            novyUcetCry = new CryptoUcet();
                            novyUcetCry.setNazov(nazov.getEditText().getText().toString());
                            novyUcetCry.setTypUctu(TypyUctov.SPORIACI);
                            novyUcetCry.setAktualnyZostatok(aktualnyZozstatok);
                            novyUcetCry.setMena(zvolenaMena);
                            novyUcetCry.setPoplatokZaVedenie(Double.parseDouble(poplatokZaVedenieUctu.getEditText().getText().toString()));

                            DatabaseManager.saveUcet(novyUcetCry);

                            break;
                    }

                    Log.i(TAG, "onClick uloz ucet: ukladanie uctu DONE");

                    if (Double.parseDouble(poplatokZaVedenieUctu.getEditText().getText().toString()) != 0.) {

                        Vydaj vedenieUctu = new Vydaj();
                        vedenieUctu.setMena(zvolenaMena);
                        vedenieUctu.setSuma(Double.parseDouble(poplatokZaVedenieUctu.getEditText().getText().toString()));
                        vedenieUctu.setNazovUctu(nazov.getEditText().getText().toString());
                        vedenieUctu.setPoznamka("Poplatok za vedenie účtu");
                        vedenieUctu.setTypZaznamu(TypZaznamu.VYDAJ);
                        vedenieUctu.setTypVydaju(TypVydaju.OSTATNE);
                        vedenieUctu.setDenSplatnosti(Calendar.getInstance().get(Calendar.DAY_OF_MONTH));
                        vedenieUctu.setCasZadania(Calendar.getInstance().getTime());

                        DatabaseManager.saveTrvalyPrikaz( nazov.getEditText().getText().toString(),vedenieUctu);
                        Log.i(TAG, "onClick ucet bol sporiaci: vytvaranie trvaleho prikazu DONE");

                    }

                    currentDialog.cancel();

                } else {
                    currentDialog.cancel();
                }
            }
        });

    }

    private boolean validateData() {
        boolean result = true;


        if (nazov.getEditText().getText().toString().trim().equals("")) {
            nazov.setError("Názov nemôže byť prázdny");
            result = false;
        } else if (!prebiehaEdit && !validateNazov(nazov.getEditText().getText().toString().trim())) {
            nazov.setError("Názov účtu sa už využíva");
            result = false;
        } else {

            nazov.setError(null);
        }

        if (zvolenyTypUctu == null) {
            typSpinner.setError("Zvolte typ účtu");
            result = false;
        } else {
            typSpinner.setError(null);
        }

        if (zvolenaMena == null) {
            menaSpinner.setError("Zvolte typ účtu");
            result = false;
        } else {
            menaSpinner.setError(null);
        }

        if (zvolenyTypUctu == TypyUctov.BEZNY &&
                usetrenaMesacnaSuma.getEditText().getText().toString().trim().equals("")) {
            usetrenaMesacnaSuma.setError("Toto pole nemôže byť prázdne");
            result = false;
        } else {
            usetrenaMesacnaSuma.setError(null);
        }

        if (poplatokZaVedenieUctu.getEditText().getText().toString().trim().equals("")) {
            poplatokZaVedenieUctu.setError("Toto pole nemôže byť prázdne");
            result = false;
        } else {
            poplatokZaVedenieUctu.setError(null);
        }

        if (zvolenyTypUctu == TypyUctov.SPORIACI &&
                percentoZuctovania.getEditText().getText().toString().trim().equals("")) {
            percentoZuctovania.setError("Toto pole nemôže byť prázdne");
            result = false;
        } else {
            percentoZuctovania.setError(null);
        }

        return result;
    }

    private boolean validateNazov(String nazov) {

        List<Ucet> ucty = DatabaseManager.getUcty();

        for (Ucet ucet: ucty) {
            if (ucet.getNazov().equals(nazov)) {
                return false;
            }
        }

        return true;
    }

    private void initOstatne() {

        nazov = findViewById(R.id.pridaj_novy_ucet_fragment_nazov_layout);
        usetrenaMesacnaSuma = findViewById(R.id.pridaj_novy_ucet_fragment_odlozena_suma_layout);
        usetrenaMesacnaSuma.setVisibility(View.GONE);
        percentoZuctovania = findViewById(R.id.pridaj_novy_ucet_fragment_percento_zuctovania_layout);
        percentoZuctovania.setVisibility(View.GONE);
        poplatokZaVedenieUctu = findViewById(R.id.pridaj_novy_ucet_fragment_poplatok_za_vedenie_layout);
        poplatokZaVedenieUctu.setVisibility(View.GONE);

    }

    private void initSpinnerMenaUctu() {

        menaSpinner = findViewById(R.id.pridaj_novy_ucet_fragment_mena_spinner_layout);

        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(getContext(),
                R.array.meny_uctov, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        ((AutoCompleteTextView)menaSpinner.getEditText()).setAdapter(adapter);

        ((AutoCompleteTextView)menaSpinner.getEditText()).setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                String mena = parent.getItemAtPosition(position).toString();


                switch (mena) {
                    case "Euro":
                        zvolenaMena = Meny.EUR;
                        break;
                    case "Americký dolár":
                        zvolenaMena = Meny.USD;
                        break;
                    case "Česká koruna":
                        zvolenaMena = Meny.CZK;
                        break;
                    case "Bitcoin":
                        zvolenaMena = Meny.BTC;
                        break;
                    case "Etherum":
                        zvolenaMena = Meny.ETH;
                        break;
                }

            }
        });
    }

    private void initSpinnerTypUctu() {

        typSpinner = findViewById(R.id.pridaj_novy_ucet_fragment_typ_spinner_layout);


        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(getContext(),
                R.array.typy_uctov, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        ((AutoCompleteTextView)typSpinner.getEditText()).setAdapter(adapter);

        ((AutoCompleteTextView)typSpinner.getEditText()).setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                String typ = parent.getItemAtPosition(position).toString();


                switch (typ) {
                    case "Bežný účet":
                        zvolenyTypUctu = TypyUctov.BEZNY;
                        break;
                    case "Sporiaci účet":
                        zvolenyTypUctu = TypyUctov.SPORIACI;
                        break;
                    case "Krypto peňaženka":
                        zvolenyTypUctu = TypyUctov.KRYPTO;
                        break;
                }

                if (zvolenyTypUctu == null) {
                    return;
                }

                switch (zvolenyTypUctu) {
                    case BEZNY:
                        usetrenaMesacnaSuma.setVisibility(View.VISIBLE);
                        poplatokZaVedenieUctu.setVisibility(View.VISIBLE);
                        percentoZuctovania.setVisibility(View.GONE);
                        break;
                    case SPORIACI:
                        poplatokZaVedenieUctu.setVisibility(View.VISIBLE);
                        percentoZuctovania.setVisibility(View.VISIBLE);
                        usetrenaMesacnaSuma.setVisibility(View.GONE);
                        break;
                    case KRYPTO:
                        poplatokZaVedenieUctu.setVisibility(View.VISIBLE);
                        percentoZuctovania.setVisibility(View.GONE);
                        usetrenaMesacnaSuma.setVisibility(View.GONE);
                        break;
                    default:
                        usetrenaMesacnaSuma.setVisibility(View.GONE);
                        poplatokZaVedenieUctu.setVisibility(View.GONE);
                        percentoZuctovania.setVisibility(View.GONE);
                        break;
                }
            }
        });
    }
}
