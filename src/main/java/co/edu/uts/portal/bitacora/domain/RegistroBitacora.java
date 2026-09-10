package co.edu.uts.portal.bitacora.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.time.Instant;

/**
 * Registro append-only de un cambio administrativo (HU-22): actor, accion, fecha y
 * objeto afectado. La descripcion es texto legible y NUNCA contiene datos sensibles
 * (contrasenas, respuestas de autoorientacion).
 *
 * La aplicacion no expone edicion ni borrado de estos registros.
 */
@Entity
@Table(name = "registro_bitacora")
public class RegistroBitacora {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "ocurrido_en", nullable = false, updatable = false)
    private Instant ocurridoEn;

    @Column(name = "actor", nullable = false, updatable = false, length = 160)
    private String actor;

    @Enumerated(EnumType.STRING)
    @Column(name = "accion", nullable = false, updatable = false, length = 40)
    private AccionBitacora accion;

    @Column(name = "tipo_objeto", nullable = false, updatable = false, length = 40)
    private String tipoObjeto;

    @Column(name = "objeto_id", updatable = false, length = 40)
    private String objetoId;

    @Column(name = "descripcion", nullable = false, updatable = false, length = 500)
    private String descripcion;

    protected RegistroBitacora() {
    }

    public RegistroBitacora(String actor, AccionBitacora accion, String tipoObjeto,
                            String objetoId, String descripcion) {
        this.ocurridoEn = Instant.now();
        this.actor = actor;
        this.accion = accion;
        this.tipoObjeto = tipoObjeto;
        this.objetoId = objetoId;
        this.descripcion = descripcion;
    }

    public Long getId() {
        return id;
    }

    public Instant getOcurridoEn() {
        return ocurridoEn;
    }

    public String getActor() {
        return actor;
    }

    public AccionBitacora getAccion() {
        return accion;
    }

    public String getTipoObjeto() {
        return tipoObjeto;
    }

    public String getObjetoId() {
        return objetoId;
    }

    public String getDescripcion() {
        return descripcion;
    }
}
