package sk.bak.model;

import java.util.Date;

import sk.bak.managers.DatabaseManager;
import sk.bak.model.abst.VlozenyZaznam;
import sk.bak.model.enums.Meny;
import sk.bak.model.enums.TypVydaju;
import sk.bak.model.enums.TypZaznamu;


/**
 *
 * Trieda zázanmu typu výdaj
 *
 */
public class Vydaj extends VlozenyZaznam {

    private TypVydaju typVydaju;

    public Vydaj(TypVydaju typVydaju, Double suma, Meny mena, Date casZadania, String id, String nazovUctu, int denSplatnosti, String poznamka) {
        super(id, suma, TypZaznamu.VYDAJ, mena, casZadania, nazovUctu, denSplatnosti, poznamka);
        this.typVydaju = typVydaju;
    }

    public Vydaj() {
        super();
    }

    public TypVydaju getTypVydaju() {
        return typVydaju;
    }

    public void setTypVydaju(TypVydaju typVydaju) {
        this.typVydaju = typVydaju;
    }
}
