package sk.bak.model;

import java.util.List;

import sk.bak.model.abst.Ucet;


/**
 *
 * Trieda bežného účtu
 *
 */
public class BeznyUcet extends Ucet {


    private Double chcenaMesacneUsetrenaSuma;

    public BeznyUcet(Double chcenaMesacneUsetrenaSuma) {
        super();
        this.chcenaMesacneUsetrenaSuma = chcenaMesacneUsetrenaSuma;
    }

    public BeznyUcet() {
        super();
    }

    public Double getChcenaMesacneUsetrenaSuma() {
        return chcenaMesacneUsetrenaSuma;
    }

    public void setChcenaMesacneUsetrenaSuma(Double chcenaMesacneUsetrenaSuma) {
        this.chcenaMesacneUsetrenaSuma = chcenaMesacneUsetrenaSuma;
    }

    @Override
    public String toString() {
        return "BeznyUcet{" +
                "chcenaMesacneUsetrenaSuma=" + chcenaMesacneUsetrenaSuma +
                '}';
    }
}
