package org.example.game.map;

public enum Biome {
    FOREST (new String[]{ "012", "123", "234", "321" }),
    DESERT (new String[]{ "540", "552", "555", "451" }),
    ICE    (new String[]{ "004", "113", "225", "334" }),
    LAVA   (new String[]{ "500", "511", "522", "533" }),
    SWAMP  (new String[]{ "032", "142", "253", "341" });

    private final String[] paletteCodes;
    Biome(String[] codes) { this.paletteCodes = codes; }
    public String[] getPaletteCodes() { return paletteCodes; }

    public static Biome randomBiome() {
        Biome[] vals = values();
        return vals[(int)(Math.random() * vals.length)];
    }
}
