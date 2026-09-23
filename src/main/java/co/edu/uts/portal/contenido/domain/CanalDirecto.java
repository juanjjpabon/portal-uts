package co.edu.uts.portal.contenido.domain;

import java.net.URI;
import java.net.URISyntaxException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Locale;
import java.util.Optional;
import java.util.regex.Pattern;

/**
 * Accion de contacto directo de una ruta o contacto (HU-07), derivada del campo
 * "contacto directo" (columna url_canal).
 *
 * Hallazgo de la reunion con la directora (22/9/2026): el boton "Abrir canal" "no
 * llevaba a nada". El enlace estaba bien (mailto:/tel:), pero un mailto: solo
 * funciona si el equipo tiene un programa de correo configurado, y en la mayoria
 * de PCs de estudiantes o de la universidad no lo hay: el navegador no hace nada
 * visible. Por eso ahora el portal muestra el correo o telefono a la vista, con
 * un boton para copiarlo, y ofrece los enlaces como alternativa (Gmail, llamar
 * desde el celular, abrir la pagina).
 *
 * Tambien es una defensa: solo se generan enlaces con esquemas conocidos
 * (mailto, tel, http, https); cualquier otra cosa (p. ej. "javascript:") se
 * descarta y no se muestra como enlace.
 */
public record CanalDirecto(Tipo tipo, String valor, String href) {

    public enum Tipo { CORREO, TELEFONO, WHATSAPP, WEB }

    // Sin '%': un correo real no lo necesita y evita cualquier decodificacion de URL al mostrarlo.
    private static final Pattern CORREO =
            Pattern.compile("^[A-Za-z0-9._+\\-]+@[A-Za-z0-9.\\-]+\\.[A-Za-z]{2,}$");
    private static final Pattern TELEFONO = Pattern.compile("^\\+?\\(?[0-9][0-9 ()\\-.]{1,24}$");
    private static final Pattern DOMINIO = Pattern.compile("^[a-z0-9.\\-]+\\.[a-z]{2,}(/.*)?$");

    /**
     * Interpreta lo que escribio el administrador y lo deja como una URL canonica:
     * <ul>
     *   <li>"bienestar@correo.uts.edu.co" o "mailto:..." &rarr; "mailto:bienestar@correo.uts.edu.co"</li>
     *   <li>"123", "(607) 691 7700", "tel:123" &rarr; "tel:123", "tel:6076917700"</li>
     *   <li>"wa.me/573001234567" &rarr; "https://wa.me/573001234567"</li>
     *   <li>"www.uts.edu.co", "https://..." &rarr; "https://www.uts.edu.co", "https://..."</li>
     * </ul>
     * Vacio si esta en blanco o si no se reconoce (otro esquema, texto libre...).
     */
    public static Optional<String> normalizar(String entrada) {
        if (entrada == null || entrada.isBlank()) {
            return Optional.empty();
        }
        String s = entrada.trim();
        String minus = s.toLowerCase(Locale.ROOT);

        if (minus.startsWith("mailto:")) {
            String correo = s.substring("mailto:".length());
            int q = correo.indexOf('?');
            if (q >= 0) {
                correo = correo.substring(0, q);
            }
            correo = correo.trim();
            return CORREO.matcher(correo).matches() ? Optional.of("mailto:" + correo) : Optional.empty();
        }
        if (CORREO.matcher(s).matches()) {
            return Optional.of("mailto:" + s);
        }

        String posibleTelefono = minus.startsWith("tel:") ? s.substring("tel:".length()).trim() : s;
        if (TELEFONO.matcher(posibleTelefono).matches()) {
            String marcado = posibleTelefono.replaceAll("[^0-9+]", "");
            long digitos = marcado.chars().filter(Character::isDigit).count();
            if (marcado.indexOf('+', 1) >= 0 || digitos < 3 || digitos > 15) {
                return Optional.empty();
            }
            return Optional.of("tel:" + marcado);
        }
        if (minus.startsWith("tel:")) {
            return Optional.empty();
        }

        String url = s;
        if (!minus.startsWith("http://") && !minus.startsWith("https://")) {
            // Sin esquema: se acepta solo si parece un dominio ("www.uts.edu.co",
            // "wa.me/57..."). Cualquier otro esquema (javascript:, data:, file:) se rechaza.
            if (minus.contains(":") || !DOMINIO.matcher(minus).matches()) {
                return Optional.empty();
            }
            url = "https://" + s;
        }
        try {
            URI uri = new URI(url);
            if (uri.getHost() == null || uri.getHost().isBlank()) {
                return Optional.empty();
            }
        } catch (URISyntaxException e) {
            return Optional.empty();
        }
        return Optional.of(url);
    }

    /**
     * Como se muestra en el formulario del panel para editarlo: sin "mailto:" ni "tel:"
     * (el personal del CAE escribe el correo o el numero tal cual; al guardar se
     * vuelve a normalizar).
     */
    public static String paraEditar(String guardado) {
        if (guardado == null) {
            return null;
        }
        String minus = guardado.toLowerCase(Locale.ROOT);
        if (minus.startsWith("mailto:")) {
            return guardado.substring("mailto:".length());
        }
        if (minus.startsWith("tel:")) {
            return guardado.substring("tel:".length());
        }
        return guardado;
    }

    /**
     * Clasifica un contacto guardado para mostrarlo en el portal. Vacio si no hay o no es
     * valido. Se llama al pintar la pagina: nunca lanza excepcion (un dato raro en la base
     * de datos no puede tumbar /rutas, /buscar ni /urgencia; simplemente no se muestra).
     */
    public static Optional<CanalDirecto> desde(String url) {
        try {
            return normalizar(url).map(CanalDirecto::clasificar);
        } catch (RuntimeException e) {
            return Optional.empty();
        }
    }

    private static CanalDirecto clasificar(String normalizado) {
        String minus = normalizado.toLowerCase(Locale.ROOT);
        if (minus.startsWith("mailto:")) {
            return new CanalDirecto(Tipo.CORREO, normalizado.substring("mailto:".length()), normalizado);
        }
        if (minus.startsWith("tel:")) {
            return new CanalDirecto(Tipo.TELEFONO, formatearTelefono(normalizado.substring("tel:".length())),
                    normalizado);
        }
        String host = URI.create(normalizado).getHost().toLowerCase(Locale.ROOT);
        if (host.equals("wa.me") || host.endsWith("whatsapp.com")) {
            String numero = URI.create(normalizado).getPath() == null
                    ? "" : URI.create(normalizado).getPath().replaceAll("[^0-9]", "");
            return new CanalDirecto(Tipo.WHATSAPP, numero.isEmpty() ? "WhatsApp" : formatearTelefono("+" + numero),
                    normalizado);
        }
        return new CanalDirecto(Tipo.WEB, host.startsWith("www.") ? host.substring(4) : host, normalizado);
    }

    /** "6076917700" -> "607 691 7700"; "+573001234567" -> "+57 300 123 4567"; lo demas, igual. */
    static String formatearTelefono(String marcado) {
        String digitos = marcado.replaceAll("[^0-9]", "");
        if (marcado.startsWith("+57") && digitos.length() == 12) {
            return "+57 " + digitos.substring(2, 5) + " " + digitos.substring(5, 8) + " " + digitos.substring(8);
        }
        if (!marcado.startsWith("+") && digitos.length() == 10) {
            return digitos.substring(0, 3) + " " + digitos.substring(3, 6) + " " + digitos.substring(6);
        }
        return marcado;
    }

    public boolean isCorreo() {
        return tipo == Tipo.CORREO;
    }

    public boolean isTelefono() {
        return tipo == Tipo.TELEFONO;
    }

    public boolean isWhatsapp() {
        return tipo == Tipo.WHATSAPP;
    }

    public boolean isWeb() {
        return tipo == Tipo.WEB;
    }

    /** Nombre del icono de fragments/iconos.html que acompana el dato. */
    public String getIcono() {
        return switch (tipo) {
            case CORREO -> "correo";
            case TELEFONO -> "telefono";
            case WHATSAPP -> "chat";
            case WEB -> "web";
        };
    }

    /** Redactar el correo en Gmail web: funciona en cualquier navegador, sin programa de correo instalado. */
    public String getHrefGmail() {
        return isCorreo()
                ? "https://mail.google.com/mail/?view=cm&fs=1&to=" + URLEncoder.encode(valor, StandardCharsets.UTF_8)
                : null;
    }

    /** Lo que copia el boton "Copiar": el correo, o el numero sin espacios. */
    public String getTextoCopiable() {
        return switch (tipo) {
            case CORREO -> valor;
            case TELEFONO -> href.substring("tel:".length());
            case WHATSAPP -> {
                String digitos = valor.replaceAll("[^0-9]", "");
                yield digitos.isEmpty() ? href : "+" + digitos;
            }
            case WEB -> href;
        };
    }
}
