package sk.bak.model.enums;

public enum TypyUctov {
    BEZNY("Bežný účet", 0),
    SPORIACI("Sporiaci účet", 1),
    KRYPTO("Kypto peňaženka", 2);


    private final String name;
    private final int position;

    private TypyUctov(String s, int position) {
        name = s;
        this.position = position;
    }

    public String getName() {
        return name;
    }

    public boolean equalsName(String otherName) {
        return name.equals(otherName);
    }

    public String toString() {
        return this.name;
    }

    public int getPosition() {
        return position;
    }
}


