package sk.bak.model;

import java.util.Date;

import sk.bak.model.abst.VlozenyZaznam;
import sk.bak.model.enums.Meny;
import sk.bak.model.enums.TypVydaju;
import sk.bak.model.enums.TypZaznamu;

public class TrvalyPrikaz {

    private boolean isSporiaci;
    private Double percentoZúčtovania;
    private Date poslednaKontrola;
    private VlozenyZaznam zaznam;


    public TrvalyPrikaz(Date poslednaKontrola, VlozenyZaznam zaznam) {
        this.poslednaKontrola = poslednaKontrola;
        this.zaznam = zaznam;
        this.isSporiaci = false;
    }

    public TrvalyPrikaz(boolean isSporiaci, Double percentoZúčtovania, Date poslednaKontrola, VlozenyZaznam zaznam) {
        this.isSporiaci = isSporiaci;
        this.percentoZúčtovania = percentoZúčtovania;
        this.poslednaKontrola = poslednaKontrola;
        this.zaznam = zaznam;
    }

    public TrvalyPrikaz() {
    }

    public Date getPoslednaKontrola() {
        return poslednaKontrola;
    }

    public void setPoslednaKontrola(Date poslednaKontrola) {
        this.poslednaKontrola = poslednaKontrola;
    }

    public VlozenyZaznam getZaznam() {
        return zaznam;
    }

    public void setZaznam(VlozenyZaznam zaznam) {
        this.zaznam = zaznam;
    }

    public boolean isSporiaci() {
        return isSporiaci;
    }

    public void setSporiaci(boolean sporiaci) {
        isSporiaci = sporiaci;
    }

    public Double getPercentoZúčtovania() {
        return percentoZúčtovania;
    }

    public void setPercentoZúčtovania(Double percentoZúčtovania) {
        this.percentoZúčtovania = percentoZúčtovania;
    }
}
