package co.edu.uts.portal.common;

/** Escapado minimo de CSV (RFC 4180): comillas si el valor trae coma, comilla o salto de linea. */
public final class CsvUtil {

    private CsvUtil() {
    }

    public static String celda(Object valor) {
        String s = valor == null ? "" : valor.toString();
        if (s.contains(",") || s.contains("\"") || s.contains("\n") || s.contains("\r")) {
            return "\"" + s.replace("\"", "\"\"") + "\"";
        }
        return s;
    }
}
