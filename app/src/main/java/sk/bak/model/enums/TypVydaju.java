package sk.bak.model.enums;

/**
 *
 * Enum pre typ výdaju
 *
 */
public enum TypVydaju {

    STRAVA("Strava"),
    CESTOVANIE("Cestovanie"),
    ELEKTRO("Elektro"),
    SPORT("Sport"),
    DOPRAVA("Doprava"),
    RODINA("Rodina"),
    ZAVABA("Zabava"),
    OBLECENIE("Oblecenie"),
    ANIMAL("Animal"),
    HOUSE("House"),
    DROGERIA("Drogeria"),
    OSTATNE("Ostatne");


    private String nazov;

    TypVydaju(String nazov) {
        this.nazov = nazov;
    }

    public String getNazov() {
        return nazov;
    }
}
