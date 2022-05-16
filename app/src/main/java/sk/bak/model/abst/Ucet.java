package sk.bak.model.abst;

import sk.bak.model.enums.Meny;
import sk.bak.model.enums.TypyUctov;


/**
 *
 * Základná trieda pre ucet. Je ďalej rozširovná
 *
 */
public class Ucet {

    private String nazov;
    private Meny mena;
    private Double aktualnyZostatok;
    private boolean jeHlavnyUcet;
    private Double poplatokZaVedenie;
    private TypyUctov typUctu;


    public Ucet() {
    }

    public TypyUctov getTypUctu() {
        return typUctu;
    }

    public void setTypUctu(TypyUctov typUctu) {
        this.typUctu = typUctu;
    }

    public String getNazov() {
        return nazov;
    }

    public void setNazov(String nazov) {
        this.nazov = nazov;
    }

    public Meny getMena() {
        return mena;
    }

    public void setMena(Meny mena) {
        this.mena = mena;
    }

    public Double getAktualnyZostatok() {
        return aktualnyZostatok;
    }

    public void setAktualnyZostatok(Double aktualnyZostatok) {
        this.aktualnyZostatok = aktualnyZostatok;
    }

    public Double getPoplatokZaVedenie() {
        return poplatokZaVedenie;
    }

    public void setPoplatokZaVedenie(Double poplatokZaVedenie) {
        this.poplatokZaVedenie = poplatokZaVedenie;
    }

    public boolean isJeHlavnyUcet() {
        return jeHlavnyUcet;
    }

    public void setJeHlavnyUcet(boolean jeHlavnyUcet) {
        this.jeHlavnyUcet = jeHlavnyUcet;
    }
}
