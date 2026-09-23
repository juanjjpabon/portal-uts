package co.edu.uts.portal.contenido.domain;

import co.edu.uts.portal.common.domain.BaseAuditable;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.util.Objects;
import java.util.UUID;

/**
 * Un paso de una ruta institucional (reunion con la directora, 22/9/2026): una
 * ruta no es solo un contacto, debe decir que hacer y en que orden
 * ("Paso 1: acude a...", "Paso 2: escribe a...", "Paso 3: habla con...").
 *
 * La ruta se referencia por su id (recurso_id) y no con una relacion JPA, igual
 * que la imagen: asi listar rutas o pasos nunca arrastra datos de mas. Al borrar
 * la ruta, la base de datos borra sus pasos (ON DELETE CASCADE).
 */
@Entity
@Table(name = "paso_ruta")
public class PasoRuta extends BaseAuditable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "recurso_id", nullable = false, updatable = false)
    private Long rutaId;

    @Column(name = "orden", nullable = false)
    private int orden;

    @Column(name = "titulo", nullable = false, length = 160)
    private String titulo;

    @Column(name = "descripcion", columnDefinition = "text")
    private String descripcion;

    @Column(name = "imagen_id")
    private UUID imagenId;

    @Column(name = "imagen_alt", length = 250)
    private String imagenAlt;

    protected PasoRuta() {
    }

    public PasoRuta(Long rutaId, int orden) {
        this.rutaId = rutaId;
        this.orden = orden;
    }

    public Long getId() {
        return id;
    }

    public Long getRutaId() {
        return rutaId;
    }

    public int getOrden() {
        return orden;
    }

    public void setOrden(int orden) {
        this.orden = orden;
    }

    public String getTitulo() {
        return titulo;
    }

    public void setTitulo(String titulo) {
        this.titulo = titulo;
    }

    public String getDescripcion() {
        return descripcion;
    }

    public void setDescripcion(String descripcion) {
        this.descripcion = descripcion;
    }

    public UUID getImagenId() {
        return imagenId;
    }

    public void setImagenId(UUID imagenId) {
        this.imagenId = imagenId;
    }

    public String getImagenAlt() {
        return imagenAlt;
    }

    public void setImagenAlt(String imagenAlt) {
        this.imagenAlt = imagenAlt;
    }

    /** Texto alternativo efectivo: el que escribio el administrador, o el titulo del paso. */
    public String getTextoAlternativo() {
        return imagenAlt != null && !imagenAlt.isBlank() ? imagenAlt : titulo;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof PasoRuta otro)) {
            return false;
        }
        return id != null && id.equals(otro.id);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(id);
    }
}
