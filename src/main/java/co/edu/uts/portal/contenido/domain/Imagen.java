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
 * Imagen subida desde el panel (reunion con la directora, 22/9/2026: folletos del
 * CAE, banners de la portada y fotos de los pasos de una ruta).
 *
 * Se guarda en la base de datos y no en disco: Render borra el disco del
 * contenedor en cada despliegue. Los recursos y los pasos la referencian solo por
 * su id (columna imagen_id), asi que listar recursos nunca carga los bytes; solo
 * se leen al servir /imagenes/{id}.
 *
 * Es inmutable: cambiar la imagen de un recurso crea una nueva (con otro id), lo
 * que permite cachearla en el navegador de forma indefinida.
 */
@Entity
@Table(name = "imagen")
public class Imagen extends BaseAuditable {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "tipo_contenido", nullable = false, updatable = false, length = 40)
    private String tipoContenido;

    @Column(name = "nombre_original", updatable = false, length = 200)
    private String nombreOriginal;

    @Column(name = "ancho", nullable = false, updatable = false)
    private int ancho;

    @Column(name = "alto", nullable = false, updatable = false)
    private int alto;

    @Column(name = "tamano_bytes", nullable = false, updatable = false)
    private int tamanoBytes;

    @Column(name = "datos", nullable = false, updatable = false)
    private byte[] datos;

    protected Imagen() {
    }

    public Imagen(ImagenProcesada procesada) {
        this.tipoContenido = procesada.tipoContenido();
        this.nombreOriginal = recortar(procesada.nombreOriginal(), 200);
        this.ancho = procesada.ancho();
        this.alto = procesada.alto();
        this.datos = procesada.datos();
        this.tamanoBytes = procesada.datos().length;
    }

    private static String recortar(String texto, int max) {
        if (texto == null) {
            return null;
        }
        return texto.length() <= max ? texto : texto.substring(0, max);
    }

    public UUID getId() {
        return id;
    }

    public String getTipoContenido() {
        return tipoContenido;
    }

    public String getNombreOriginal() {
        return nombreOriginal;
    }

    public int getAncho() {
        return ancho;
    }

    public int getAlto() {
        return alto;
    }

    public int getTamanoBytes() {
        return tamanoBytes;
    }

    public byte[] getDatos() {
        return datos;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof Imagen otra)) {
            return false;
        }
        return id != null && id.equals(otra.id);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(id);
    }
}
