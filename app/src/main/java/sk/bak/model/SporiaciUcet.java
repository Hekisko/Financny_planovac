package sk.bak.model;

import sk.bak.model.abst.Ucet;

public class SporiaciUcet extends Ucet {

    private Double percentoZuctovania;

    public SporiaciUcet(Double percentoZuctovania) {
        super();
        this.percentoZuctovania = percentoZuctovania;
    }

    public SporiaciUcet() {
        super();
    }

    public Double getPercentoZuctovania() {
        return percentoZuctovania;
    }

    public void setPercentoZuctovania(Double percentoZuctovania) {
        this.percentoZuctovania = percentoZuctovania;
    }
}
