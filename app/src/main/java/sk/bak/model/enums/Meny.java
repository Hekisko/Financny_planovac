package sk.bak.model.enums;


/**
 *
 * Enum pre meny
 *
 */
public enum Meny {
    EUR("Euro", "EUR", 0, "€"),
    USD("Dolár", "USD", 1, "$"),
    CZK("Česká koruna", "CZK", 2, "Kč"),
    BTC("Bitcoin", "BTC", 3, "btc"),
    ETH("Etherum", "ETH", 4, "eth");

    private String mena;
    private String skratka;
    private final int position;
    private final String znak;

    private Meny(String mena, String skratka, int position, String znak) {
        this.mena = mena;
        this.skratka = skratka;
        this.position = position;
        this.znak = znak;
    }

    public String getSkratka() {
        return skratka;
    }

    public String getMena() {
        return mena;
    }

    public int getPosition() {
        return position;
    }

    public String getZnak() {
        return znak;
    }
}
