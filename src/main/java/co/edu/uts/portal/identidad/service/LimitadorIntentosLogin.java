package co.edu.uts.portal.identidad.service;

import org.springframework.stereotype.Component;

import java.time.Duration;
import java.time.Instant;
import java.util.Locale;
import java.util.concurrent.ConcurrentHashMap;

/**
 * RS-10.4: limite de intentos de inicio de sesion (anti fuerza bruta, ASVS V6.1.1/V6.3.1).
 *
 * Estado en memoria, por correo normalizado. Valido porque el servicio corre en una
 * sola instancia (render.yaml: numInstances = 1) -- si en el futuro se escala a mas
 * de una instancia, este contador tendria que moverse a un almacen compartido (la
 * propia base de datos o un cache distribuido), porque cada instancia tendria su
 * copia separada y el limite dejaria de ser efectivo.
 *
 * No persiste entre reinicios del servicio a proposito: es una mitigacion de fuerza
 * bruta en caliente, no un registro de auditoria -- eso ya lo cubre RegistroBitacora
 * (RF-25/RS-12) por separado.
 */
@Component
public class LimitadorIntentosLogin {

    static final int MAX_INTENTOS = 5;
    static final Duration DURACION_BLOQUEO = Duration.ofMinutes(15);

    private final ConcurrentHashMap<String, Estado> intentos = new ConcurrentHashMap<>();

    /** true si el correo esta bloqueado temporalmente por demasiados intentos fallidos recientes. */
    public boolean estaBloqueado(String correo) {
        if (correo == null || correo.isBlank()) {
            return false;
        }
        Estado estado = intentos.get(normalizar(correo));
        return estado != null && estado.bloqueadoHasta != null && Instant.now().isBefore(estado.bloqueadoHasta);
    }

    /** Registra un intento fallido; al llegar a MAX_INTENTOS, bloquea por DURACION_BLOQUEO. */
    public void registrarFallo(String correo) {
        if (correo == null || correo.isBlank()) {
            return;
        }
        intentos.compute(normalizar(correo), (clave, actual) -> {
            Estado estado = (actual != null) ? actual : new Estado();
            estado.fallos++;
            if (estado.fallos >= MAX_INTENTOS) {
                estado.bloqueadoHasta = Instant.now().plus(DURACION_BLOQUEO);
            }
            return estado;
        });
    }

    /** Limpia el contador al iniciar sesion correctamente. */
    public void registrarExito(String correo) {
        if (correo != null && !correo.isBlank()) {
            intentos.remove(normalizar(correo));
        }
    }

    private String normalizar(String correo) {
        return correo.trim().toLowerCase(Locale.ROOT);
    }

    private static final class Estado {
        private int fallos = 0;
        private volatile Instant bloqueadoHasta;
    }
}
