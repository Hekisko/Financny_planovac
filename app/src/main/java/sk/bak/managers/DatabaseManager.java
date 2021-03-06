package sk.bak.managers;

import android.content.Context;
import android.util.Log;


import androidx.annotation.NonNull;
import androidx.annotation.Nullable;


import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.math.BigDecimal;
import java.math.RoundingMode;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

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

/**
 *
 * Trieda určená na pŕacu s datbázou. Obsahuje základné metódy. Niektoré zložitejšie čítania sú definované aj priamo v daných triedach.
 *
 */
public class DatabaseManager {

    private static final String TAG = "DatabaseManager";

    //Premenne databázy
    private static FirebaseUser user;
    private static DatabaseReference db;
    private static FirebaseDatabase firebaseDatabase;

    //Premenne na dáta
    private static List<Ucet> ucty = new ArrayList<>();
    private static List<VlozenyZaznam> zaznamy = new ArrayList<>();


    //Premenne na listenery
    private static ValueEventListener zmenyZaznamovListener;
    private static ValueEventListener uctyListener;

    // Pomocné premenne
    private static boolean initDone;
    private static boolean suUctyNacitane = false;
    private static MySharedPreferences sharedPreferences;
    private static Context context;


    /**
     *
     * Inicializácia databázy. Prebieha vždy po prihlásení sa do Google účtu
     *
     * @param firebaseUser
     * @param contextI
     */
    public static void initDbManager(FirebaseUser firebaseUser, Context contextI) {
        user = firebaseUser;

        if (!initDone) {

            Log.i(TAG, "initDbManager: init firebase, set persistance");
            firebaseDatabase = FirebaseDatabase.getInstance("https://prehladova-aplikacia-default-rtdb.europe-west1.firebasedatabase.app/");
            firebaseDatabase.setPersistenceEnabled(true);
            initDone = true;
        }

        context = contextI;

        db = firebaseDatabase.getReference("users").child(user.getUid());
        db.keepSynced(true);

        //nech sa hned naplni premenna pre účty
        uctyListener = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                if (!dataSnapshot.exists()) {
                    Log.i(TAG, "onDataChange initDbManager: zatial ziadne data");
                    ucty = new ArrayList<>();
                    return;
                }

                Log.i(TAG, "onDataChange: initDB ziskane ucty");
                ucty = new ArrayList<>();

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

                suUctyNacitane = true;

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        };

        db.child("ucty").addValueEventListener(uctyListener);

        sharedPreferences = new MySharedPreferences(context);

        Log.i(TAG, "initDbManager: idem platit trvale prikazy");
        zaplatTrvalePrikazy();
    }

    /**
     *
     * Metóda volaná po odhlásení. Vyčistuje vytvorené trvalé listenery
     *
     */
    public static void clear() {
        db.child("ucty").removeEventListener(uctyListener);

    }

    /**
     *
     * Vracia referenciu na db pre vytvaranie potrebnej functionality
     *
     * @return
     */
    public static DatabaseReference getDb() {
        return db;
    }

    /**
     *
     * Vracia prihláseného používateľa
     *
     * @return
     */
    public static FirebaseUser getUser() {
        return user;
    }

    /**
     *
     * Ukladá účet do db
     *
     * @param ucet
     */
    public static void saveUcet(Ucet ucet) {
        Log.i(TAG, "saveUcet: ukladam ucet START");

        db.child("ucty").child(ucet.getNazov()).setValue(ucet);

        Log.i(TAG, "saveUcet: ukladam ucet DONE");
    }


    /**
     *
     * Ukladá záznam do db
     *
     * @param ucetNazov
     * @param zaznam
     */
    public static void saveZaznam(String ucetNazov, VlozenyZaznam zaznam) {

        Log.i(TAG, "saveZaznam: ukladam zaznam START");

        String id = db.child("zaznamy").child(ucetNazov).push().getKey();
        if (id == null) {
            Log.i(TAG, "onDataChange save zaznam: neexistuje ");
            return;
        }

        zaznam.setId(id);

        Calendar calendar = Calendar.getInstance();
        db.child("zaznamy").child(ucetNazov).child((calendar.get(Calendar.YEAR))+"_"+(calendar.get(Calendar.MONTH) + 1)).child(id).setValue(zaznam);

        Log.i(TAG, "saveZaznam: ukladam zaznam DONE");
    }

    /**
     *
     * Vracia účty z db
     *
     * @return
     */
    public static List<Ucet> getUcty() {
        return ucty;
    }


    /**
     *
     * Pridava sumu na ucet podla zadaneho nazvu
     *
     * @param nazovUctu
     * @param suma
     * @return
     */
    public static boolean addSumaToUcet(String nazovUctu, Double suma) {

        Log.i(TAG, "addSumaToUcet: START");

        DatabaseManager.getDb().child("ucty").child(nazovUctu).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                if (!dataSnapshot.exists()) {
                    Log.i(TAG, "onDataChange add suma to ucet: neexistuje ");
                    return;
                }

                Ucet ucet = dataSnapshot.getValue(Ucet.class);

                //Vzdy bude len jeden
                if (ucet.getTypUctu() == TypyUctov.BEZNY) {
                    BeznyUcet ucetZmeny = dataSnapshot.getValue(BeznyUcet.class);
                    ucetZmeny.setAktualnyZostatok(ucetZmeny.getAktualnyZostatok() + suma);
                    saveUcet(ucetZmeny);
                } else if (ucet.getTypUctu() == TypyUctov.SPORIACI) {
                    SporiaciUcet ucetZmeny = dataSnapshot.getValue(SporiaciUcet.class);
                    ucetZmeny.setAktualnyZostatok(ucetZmeny.getAktualnyZostatok() + suma);
                    saveUcet(ucetZmeny);
                } else {
                    CryptoUcet ucetZmeny = dataSnapshot.getValue(CryptoUcet.class);
                    ucetZmeny.setAktualnyZostatok(ucetZmeny.getAktualnyZostatok() + suma);
                    saveUcet(ucetZmeny);
                }

                Log.i(TAG, "addSumaToUcet: DONE");
            }



            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
        return true;
    }


    /**
     *
     * Vracia všetky záznamy
     *
     * @return
     */
    public static List<VlozenyZaznam> getZaznamy() {
        return zaznamy;
    }


    /**
     *
     * Maže trvalý príkaz
     *
     * @param nazovUctu
     * @param idZaznamu
     */
    public static void deleteTrvalyPrikaz(String nazovUctu, String idZaznamu) {

        Log.i(TAG, "deleteTrvalyPrikaz: START");
        db.child("trvalePrikazy").child(nazovUctu).child(idZaznamu).removeValue();
        Log.i(TAG, "deleteTrvalyPrikaz: DONE");
    }


    /**
     *
     * Maže obyčajný záznam
     *
     * @param deleteAt
     * @param id
     * @param suma
     * @param typZaznamu
     * @param nazovUctu
     */
    public static void deleteZaznam(DatabaseReference deleteAt, String id, Double suma, TypZaznamu typZaznamu, String nazovUctu) {

        Log.i(TAG, "deleteZaznam: START");

        Log.i(TAG, "deleteZaznam: pridava/odcitavam sumu z uctu START");
        if (typZaznamu == TypZaznamu.PRIJEM) {
            addSumaToUcet(nazovUctu, -suma);
        } else {
            addSumaToUcet(nazovUctu, suma);
        }
        Log.i(TAG, "deleteZaznam: pridava/odcitavam sumu z uctu DONE");

        deleteAt.child(id).removeValue();

        Log.i(TAG, "deleteZaznam: DONE");

    }


    /**
     *
     * Maže účet a jemu patriace veci
     *
     * @param nazov
     */
    public static void deleteUcet(String nazov) {

        Log.i(TAG, "deleteUcet: START");
        db.child("ucty").child(nazov).removeValue();
        db.child("zaznamy").child(nazov).removeValue();
        db.child("trvalePrikazy").child(nazov).removeValue();
        Log.i(TAG, "deleteUcet: DONE");
    }


    /**
     *
     * Uklada trvaly prikaz
     *
     * @param nazovUctu
     * @param zaznam
     */
    public static void saveTrvalyPrikaz(String nazovUctu, VlozenyZaznam zaznam) {
        Log.i(TAG, "saveTrvalyPrikaz: START");
        String id = db.child("trvalePrikazy").child(nazovUctu).push().getKey();
        if (id == null) {
            Log.i(TAG, "onDataChange trvaly prikaz: neexistuje ");
            return;
        }

        zaznam.setId(id);

        TrvalyPrikaz trvalyPrikaz = new TrvalyPrikaz();
        trvalyPrikaz.setZaznam(zaznam);
        trvalyPrikaz.setPoslednaKontrola(null);

        db.child("trvalePrikazy").child(nazovUctu).child(id).setValue(trvalyPrikaz);
        Log.i(TAG, "saveTrvalyPrikaz: DONE");
    }


    /**
     *
     * Uklada trvaly prikaz so zadaným dátumom poslednej zmeny
     *
     * @param nazovUctu
     * @param zaznam
     * @param datumZmeny
     */
    public static void saveTrvalyPrikaz(String nazovUctu, VlozenyZaznam zaznam, Date datumZmeny) {
        Log.i(TAG, "saveTrvalyPrikaz: START");
        String id = db.child("trvalePrikazy").child(nazovUctu).push().getKey();
        if (id == null) {
            Log.i(TAG, "onDataChange trvaly prikaz: neexistuje ");
            return;
        }

        zaznam.setId(id);

        TrvalyPrikaz trvalyPrikaz = new TrvalyPrikaz();
        trvalyPrikaz.setZaznam(zaznam);
        trvalyPrikaz.setPoslednaKontrola(datumZmeny);

        db.child("trvalePrikazy").child(nazovUctu).child(id).setValue(trvalyPrikaz);
        Log.i(TAG, "saveTrvalyPrikaz: DONE");
    }


    /**
     *
     * Uklada trvaly zaznam pre sporenie, percento zuctovania
     *
     * @param nazovUctu
     * @param zaznam
     * @param percentoZuctovania
     */
    public static void saveTrvalyPrikazSporiaci(String nazovUctu, VlozenyZaznam zaznam, Double percentoZuctovania) {

        Log.i(TAG, "saveTrvalyPrikazSporiaci: START");
        String id = db.child("trvalePrikazy").child(nazovUctu).push().getKey();
        if (id == null) {
            Log.i(TAG, "onDataChange trvaly prikaz: neexistuje ");
            return;
        }

        zaznam.setId(id);

        TrvalyPrikaz trvalyPrikaz = new TrvalyPrikaz();
        trvalyPrikaz.setZaznam(zaznam);
        trvalyPrikaz.setSporiaci(true);
        trvalyPrikaz.setPercentoZúčtovania(percentoZuctovania);
        trvalyPrikaz.setPoslednaKontrola(null);

        db.child("trvalePrikazy").child(nazovUctu).child(id).setValue(trvalyPrikaz);
        Log.i(TAG, "saveTrvalyPrikazSporiaci: DONE");
    }


    /**
     *
     * Uklada trvaly zaznam pre sporenie, percento zuctovania spolu s datumom poslednej kontroly
     *
     * @param nazovUctu
     * @param zaznam
     * @param percentoZuctovania
     * @param poslednaKontrola
     */
    public static void saveTrvalyPrikazSporiaci(String nazovUctu, VlozenyZaznam zaznam, Double percentoZuctovania, Date poslednaKontrola) {

        Log.i(TAG, "saveTrvalyPrikazSporiaci: START");
        String id = db.child("trvalePrikazy").child(nazovUctu).push().getKey();
        if (id == null) {
            Log.i(TAG, "onDataChange trvaly prikaz: neexistuje ");
            return;
        }

        zaznam.setId(id);

        TrvalyPrikaz trvalyPrikaz = new TrvalyPrikaz();
        trvalyPrikaz.setZaznam(zaznam);
        trvalyPrikaz.setSporiaci(true);
        trvalyPrikaz.setPercentoZúčtovania(percentoZuctovania);
        trvalyPrikaz.setPoslednaKontrola(poslednaKontrola);

        db.child("trvalePrikazy").child(nazovUctu).child(id).setValue(trvalyPrikaz);
        Log.i(TAG, "saveTrvalyPrikazSporiaci: DONE");
    }


    /**
     *
     * Zaplatí vśetky trvalé príkazy
     *
     */
    public static void zaplatTrvalePrikazy() {
        Log.i(TAG, "zaplatTrvalePrikazy: START");

        db.child("trvalePrikazy").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                if (!dataSnapshot.exists()) {
                    Log.i(TAG, "onDataChange zaplatTrvalePrikazy: zatial ziadne data");
                    return;
                }

                for (DataSnapshot ucetSnapshot: dataSnapshot.getChildren()) {

                    if (!ucetSnapshot.exists()) {
                        return;
                    }

                    String ucetNazov = ucetSnapshot.getKey();

                    int counter = 0;
                    while (ucty.isEmpty()) {
                        if (counter >= 5) {
                            Log.i(TAG, "zaplatTrvalePrikazy: ziadne ucty, KONCIM");
                            return;
                        }
                        try {
                            Thread.sleep(1000);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                        counter++;
                    }

                    Ucet aktualnyUcet = ucty.stream().filter(a -> a.getNazov().equals(ucetNazov)).collect(Collectors.toList()).get(0);

                    Log.i(TAG, "zaplatTrvalePrikazy: spracuvavam trevale prikazy uctu " + aktualnyUcet.getNazov());
                    for (DataSnapshot trvalySnapshot: ucetSnapshot.getChildren()) {

                        if (!trvalySnapshot.exists()) {
                            Log.i(TAG, "onDataChange trvaly prikaz: neexistuje ");
                            return;
                        }

                        TrvalyPrikaz trvalyPrikaz = trvalySnapshot.getValue(TrvalyPrikaz.class);

                        if (trvalyPrikaz.isSporiaci()) {

                            trvalyPrikaz.getZaznam().setSuma(aktualnyUcet.getAktualnyZostatok() * trvalyPrikaz.getPercentoZúčtovania());
                            uskutocniTranzakciu(aktualnyUcet, trvalyPrikaz);
                            continue;
                        }

                        for (DataSnapshot trvalyPrikazItem: trvalySnapshot.getChildren()) {

                            if (!trvalyPrikazItem.exists()) {
                                Log.i(TAG, "onDataChange trvaly prikaz: neexistuje ");
                                return;
                            }

                            if (trvalyPrikazItem.getKey().equals("zaznam")) {
                                if (trvalyPrikazItem.getValue(VlozenyZaznam.class).getTypZaznamu() == TypZaznamu.PRIJEM) {
                                    trvalyPrikaz.setZaznam(trvalyPrikazItem.getValue(Prijem.class));
                                } else {
                                    trvalyPrikaz.setZaznam(trvalyPrikazItem.getValue(Vydaj.class));
                                }

                            } else if (trvalyPrikazItem.getKey().equals("poslednaKontrola")) {
                                trvalyPrikaz.setPoslednaKontrola(trvalyPrikazItem.getValue(Date.class));
                            }
                        }
                        Log.i(TAG, "zaplatTrvalePrikazy: idem vyhodnotit trvaly prikaz so sumou" + trvalyPrikaz.getZaznam().getSuma());
                        uskutocniTranzakciu(aktualnyUcet, trvalyPrikaz);
                    }
                }
                Log.i(TAG, "onDataChange: ZaplatTrvalePrikazy trvale prikazy zaplatene");
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
        
    }

    /**
     *
     * Pomocna metóda pre zaplatTrvalePrikazy(). Skontroluje posledny datum kontroly a na zaklade toho bud urobi zaznam alebo nie
     *
     * @param aktualnyUcet
     * @param zaznam
     */
    private static void uskutocniTranzakciu(Ucet aktualnyUcet, TrvalyPrikaz zaznam) {

        Calendar poslednaTranzakcia = Calendar.getInstance();

        if (zaznam.getPoslednaKontrola() == null) {

            Calendar datumVykonaniaPrikazu = Calendar.getInstance();
            Calendar casZadaniaPrikazuDoSystemu = Calendar.getInstance();

            casZadaniaPrikazuDoSystemu.setTime(zaznam.getZaznam().getCasZadania());
            datumVykonaniaPrikazu.set(Calendar.HOUR_OF_DAY, 0);
            datumVykonaniaPrikazu.set(Calendar.MINUTE, 0);
            datumVykonaniaPrikazu.set(Calendar.SECOND, 0);
            datumVykonaniaPrikazu.set(casZadaniaPrikazuDoSystemu.get(Calendar.YEAR), casZadaniaPrikazuDoSystemu.get(Calendar.MONTH), zaznam.getZaznam().getDenSplatnosti());

            SimpleDateFormat df = new SimpleDateFormat("dd.MM.yyyy");

            if (datumVykonaniaPrikazu.get(Calendar.YEAR) == casZadaniaPrikazuDoSystemu.get(Calendar.YEAR) &&
                    datumVykonaniaPrikazu.get(Calendar.MONTH) == casZadaniaPrikazuDoSystemu.get(Calendar.MONTH) &&
                    datumVykonaniaPrikazu.get(Calendar.DAY_OF_MONTH) == casZadaniaPrikazuDoSystemu.get(Calendar.DAY_OF_MONTH)) {

                copyAndSave(aktualnyUcet, zaznam, datumVykonaniaPrikazu);
                datumVykonaniaPrikazu.set(Calendar.DAY_OF_MONTH, 1);
                datumVykonaniaPrikazu.add(Calendar.MONTH, 1);
            } else if (datumVykonaniaPrikazu.before(casZadaniaPrikazuDoSystemu)) {
                datumVykonaniaPrikazu.set(Calendar.DAY_OF_MONTH, 1);
                datumVykonaniaPrikazu.add(Calendar.MONTH, 1);
            } else {
                datumVykonaniaPrikazu.add(Calendar.DAY_OF_YEAR, -zaznam.getZaznam().getDenSplatnosti() +1);
            }

            datumVykonaniaPrikazu.set(Calendar.DAY_OF_MONTH, 1);
            zaznam.setPoslednaKontrola(datumVykonaniaPrikazu.getTime());
        }

        poslednaTranzakcia.setTime(zaznam.getPoslednaKontrola());
        Calendar aktualnyDatum = Calendar.getInstance();
        Calendar nasledujucaTranzakcia = Calendar.getInstance();
        nasledujucaTranzakcia.set(poslednaTranzakcia.get(Calendar.YEAR), poslednaTranzakcia.get(Calendar.MONTH), zaznam.getZaznam().getDenSplatnosti());
        nasledujucaTranzakcia.set(Calendar.HOUR_OF_DAY, 0);
        nasledujucaTranzakcia.set(Calendar.MINUTE, 0);
        nasledujucaTranzakcia.set(Calendar.SECOND, 0);

        while (nasledujucaTranzakcia.before(aktualnyDatum)) {

            poslednaTranzakcia.add(Calendar.MONTH, 1);
            nasledujucaTranzakcia.set(poslednaTranzakcia.get(Calendar.YEAR), poslednaTranzakcia.get(Calendar.MONTH), zaznam.getZaznam().getDenSplatnosti());
            nasledujucaTranzakcia.set(Calendar.HOUR_OF_DAY, 0);
            nasledujucaTranzakcia.set(Calendar.MINUTE, 0);
            nasledujucaTranzakcia.set(Calendar.SECOND, 0);

            Log.i(TAG, "uskutocniTranzakciu: platim trvaly prikaz so sumou " + zaznam.getZaznam().getSuma());
            copyAndSave(aktualnyUcet, zaznam, nasledujucaTranzakcia);
        }


        zaznam.setPoslednaKontrola(poslednaTranzakcia.getTime());
        Log.i(TAG, "uskutocniTranzakciu: update trvaleho prikazu - nastevnai casu poslednej zmeny");
        db.child("trvalePrikazy").child(aktualnyUcet.getNazov()).child(zaznam.getZaznam().getId()).setValue(zaznam);

    }

    /**
     *
     * Pomocna metoda pre zaplatTrvalePrikazy(). Kopíruje a ukladana zaznam podla trvalého prikazu
     *
     * @param aktualnyUcet
     * @param zaznam
     * @param datumVykonaniaPrikazu
     */
    private static void copyAndSave(Ucet aktualnyUcet, TrvalyPrikaz zaznam, Calendar datumVykonaniaPrikazu) {


        Double sumaVMeneUctu = transferIfNecessery(zaznam.getZaznam().getSuma(), aktualnyUcet.getMena(), zaznam.getZaznam().getMena());
        BigDecimal bd = new BigDecimal(sumaVMeneUctu).setScale(2, RoundingMode.HALF_UP);

        String nazovUctu = aktualnyUcet.getNazov();
        VlozenyZaznam zaznamNovy;

        Calendar datumZadniaPrikazu = Calendar.getInstance();
        datumZadniaPrikazu.set(datumVykonaniaPrikazu.get(Calendar.YEAR),
                datumVykonaniaPrikazu.get(Calendar.MONTH),
                datumVykonaniaPrikazu.get(Calendar.DAY_OF_MONTH));

        int denSplatnostiHelp = -1;

        if (zaznam.getZaznam().getTypZaznamu() == TypZaznamu.PRIJEM) {
            zaznamNovy = new Prijem(null, bd.doubleValue(), aktualnyUcet.getMena(), datumZadniaPrikazu.getTime(), null, nazovUctu, denSplatnostiHelp, zaznam.getZaznam().getPoznamka());
        } else {

            Vydaj vydajTest = (Vydaj) zaznam.getZaznam();

            zaznamNovy = new Vydaj(
                    vydajTest.getTypVydaju(),
                    bd.doubleValue(),
                    aktualnyUcet.getMena(),
                    datumZadniaPrikazu.getTime(),
                    null,
                    nazovUctu,
                    denSplatnostiHelp,
                    zaznam.getZaznam().getPoznamka());
        }

        DatabaseManager.saveZaznam(nazovUctu, zaznamNovy);

        if (zaznam.getZaznam().getTypZaznamu() == TypZaznamu.PRIJEM) {
            DatabaseManager.addSumaToUcet(nazovUctu, bd.doubleValue());

        } else {
            DatabaseManager.addSumaToUcet(nazovUctu, -bd.doubleValue());
        }

    }


    /**
     *
     * Pomocná metóda na prevod Sumy na inú menu
     *
     * @param sumaVoZvolenejMene
     * @param menaZvolenehoUctu
     * @param menaZaznamu
     * @return
     */
    private static double transferIfNecessery(Double sumaVoZvolenejMene, Meny menaZvolenehoUctu, Meny menaZaznamu) {

        if (menaZaznamu.getMena().equals(menaZvolenehoUctu.getMena())) {
            return sumaVoZvolenejMene;
        }

        double vyslednaSuma = 0.;

        int nacitavam = 0;

        while (sharedPreferences.getLong("kurzy_update_date") == 0) {

            try {
                Thread.sleep(1000);
            } catch (Exception exception) {

            }

        }

        try {
            double sumaAkoUSD = sumaVoZvolenejMene / Double.parseDouble(String.valueOf(sharedPreferences.getFloat(menaZaznamu.getSkratka().toLowerCase(Locale.ROOT))));

            vyslednaSuma = sumaAkoUSD * Double.parseDouble(String.valueOf(sharedPreferences.getFloat(menaZvolenehoUctu.getSkratka().toLowerCase(Locale.ROOT))));
        } catch (Exception e) {
            Log.i(TAG, "transferIfNecessery: nastala chyba " + e.getMessage() );
            return 0.;
        }

        return vyslednaSuma;

    }


    /**
     *
     * Metóda na vymaznie všetkých dát
     *
     */
    public static void vymazVsetkyData() {
        db.removeValue(new DatabaseReference.CompletionListener() {
            @Override
            public void onComplete(@Nullable DatabaseError error, @NonNull DatabaseReference ref) {
                Log.i(TAG, "onComplete mazanie vsetky dat: DONE");
            }
        });
    }

    /**
     *
     * Metóda pre nájdenie podla poznamky a následne vymaznie trvalého príkazu
     *
     * @param nazovUctu
     * @param vyhladavanyString
     * @param novyTrvalyPrikaz
     */
    public static void najdiAVymazTrvalyPrikaz(String nazovUctu, String vyhladavanyString, Vydaj novyTrvalyPrikaz) {

        db.child("trvalePrikazy").child(nazovUctu).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (!snapshot.exists()) {
                    Log.i(TAG, "onDataChange zaplatTrvalePrikazy: zatial ziadne data");
                    return;
                }

                for (DataSnapshot dataSnapshot: snapshot.getChildren()) {
                    TrvalyPrikaz trvalyPrikaz = dataSnapshot.getValue(TrvalyPrikaz.class);
                    if (trvalyPrikaz.getZaznam().getPoznamka().equals(vyhladavanyString)) {
                        saveTrvalyPrikaz(nazovUctu, novyTrvalyPrikaz, trvalyPrikaz.getPoslednaKontrola());
                        db.child("trvalePrikazy").child(nazovUctu).child(dataSnapshot.getKey()).removeValue();
                        return;
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

    }

    /**
     *
     * Metóda na najdenie a vymaznie trvalého príkazu zo sporiaceho účtu
     *
     * @param nazovUctu
     * @param isSporiaci
     * @param novyTrvalyPrikaz
     * @param percentoZuctovania
     */
    public static void najdiAVymazTrvalyPrikaz(String nazovUctu, boolean isSporiaci, Prijem novyTrvalyPrikaz, Double percentoZuctovania) {

        db.child("trvalePrikazy").child(nazovUctu).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (!snapshot.exists()) {
                    Log.i(TAG, "onDataChange zaplatTrvalePrikazy: zatial ziadne data");
                    return;
                }

                for (DataSnapshot dataSnapshot: snapshot.getChildren()) {
                    TrvalyPrikaz trvalyPrikaz = dataSnapshot.getValue(TrvalyPrikaz.class);
                    if (trvalyPrikaz.isSporiaci()) {
                        saveTrvalyPrikazSporiaci(nazovUctu, novyTrvalyPrikaz, percentoZuctovania, trvalyPrikaz.getPoslednaKontrola());
                        db.child("trvalePrikazy").child(nazovUctu).child(dataSnapshot.getKey()).removeValue();
                        return;
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

    }
}

