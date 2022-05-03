package sk.bak.model.abst;

import java.util.Date;

import sk.bak.model.enums.Meny;
import sk.bak.model.enums.TypZaznamu;

public class VlozenyZaznam {

    private String id;
    private Double suma;
    private TypZaznamu typZaznamu;
    private Meny mena;
    private Date casZadania;
    private String nazovUctu;
    private int denSplatnosti;
    private String poznamka;

    public VlozenyZaznam(String id, Double suma, TypZaznamu typZaznamu, Meny mena, Date casZadania, String nazovUctu, int denSplatnosti, String poznamka) {
        this.id = id;
        this.suma = suma;
        this.typZaznamu = typZaznamu;
        this.mena = mena;
        this.casZadania = casZadania;
        this.nazovUctu = nazovUctu;
        this.denSplatnosti = denSplatnosti;
        this.poznamka = poznamka;
    }

    public VlozenyZaznam() {
    }

    public Double getSuma() {
        return suma;
    }

    public void setSuma(Double suma) {
        this.suma = suma;
    }

    public TypZaznamu getTypZaznamu() {
        return typZaznamu;
    }

    public void setTypZaznamu(TypZaznamu typZaznamu) {
        this.typZaznamu = typZaznamu;
    }

    public Meny getMena() {
        return mena;
    }

    public void setMena(Meny mena) {
        this.mena = mena;
    }

    public Date getCasZadania() {
        return casZadania;
    }

    public void setCasZadania(Date casZadania) {
        this.casZadania = casZadania;
    }


    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getNazovUctu() {
        return nazovUctu;
    }

    public void setNazovUctu(String nazovUctu) {
        this.nazovUctu = nazovUctu;
    }

    public int getDenSplatnosti() {
        return denSplatnosti;
    }

    public void setDenSplatnosti(int denSplatnosti) {
        this.denSplatnosti = denSplatnosti;
    }

    public String getPoznamka() {
        return poznamka;
    }

    public void setPoznamka(String poznamka) {
        this.poznamka = poznamka;
    }
}
