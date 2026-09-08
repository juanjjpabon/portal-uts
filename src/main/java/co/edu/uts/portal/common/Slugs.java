package co.edu.uts.portal.common;

import java.text.Normalizer;
import java.util.Locale;

/** Genera slugs legibles para URLs a partir de un texto (acentos fuera, espacios a guiones). */
public final class Slugs {

    private Slugs() {
    }

    public static String de(String texto) {
        if (texto == null || texto.isBlank()) {
            return "";
        }
        String sinAcentos = Normalizer.normalize(texto, Normalizer.Form.NFD)
                .replaceAll("\\p{M}", "");
        return sinAcentos.toLowerCase(Locale.ROOT)
                .replaceAll("[^a-z0-9\\s-]", "")
                .trim()
                .replaceAll("[\\s-]+", "-");
    }
}
