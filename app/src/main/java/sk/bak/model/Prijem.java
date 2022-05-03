package sk.bak.model;

import java.util.Date;

import sk.bak.model.abst.VlozenyZaznam;
import sk.bak.model.enums.Meny;
import sk.bak.model.enums.TypPrijmu;
import sk.bak.model.enums.TypZaznamu;

public class Prijem extends VlozenyZaznam {

    private TypPrijmu typPrijmu;


    public Prijem(TypPrijmu typPrijmu, Double suma, Meny mena, Date casZadania, String id, String nazovUctu, int denSplatnosti, String poznamka) {
        super(id, suma, TypZaznamu.PRIJEM, mena, casZadania, nazovUctu, denSplatnosti, poznamka);
        this.typPrijmu = typPrijmu;
    }

    public Prijem() {
    }

    public TypPrijmu getTypPrijmu() {
        return typPrijmu;
    }

    public void setTypPrijmu(TypPrijmu typPrijmu) {
        this.typPrijmu = typPrijmu;
    }

}
