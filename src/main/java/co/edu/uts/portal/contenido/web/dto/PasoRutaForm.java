package co.edu.uts.portal.contenido.web.dto;

import co.edu.uts.portal.contenido.domain.PasoRuta;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import org.springframework.web.multipart.MultipartFile;

import java.util.UUID;

/**
 * Formulario de un paso de ruta ("Paso 2: escribe al correo del CAE..."), con
 * imagen opcional (por ejemplo, una foto de la oficina o del formulario a llenar).
 */
public class PasoRutaForm {

    private Long id;

    @NotBlank(message = "Escribe qué debe hacer la persona en este paso")
    @Size(max = 160)
    private String titulo;

    @Size(max = 4000)
    private String descripcion;

    private MultipartFile imagenArchivo;

    /** Solo para mostrar la imagen actual; el servicio usa la del paso guardado. */
    private UUID imagenIdActual;

    private boolean quitarImagen;

    @Size(max = 250)
    private String imagenAlt;

    public static PasoRutaForm de(PasoRuta p) {
        PasoRutaForm f = new PasoRutaForm();
        f.id = p.getId();
        f.titulo = p.getTitulo();
        f.descripcion = p.getDescripcion();
        f.imagenIdActual = p.getImagenId();
        f.imagenAlt = p.getImagenAlt();
        return f;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
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

    public MultipartFile getImagenArchivo() {
        return imagenArchivo;
    }

    public void setImagenArchivo(MultipartFile imagenArchivo) {
        this.imagenArchivo = imagenArchivo;
    }

    public UUID getImagenIdActual() {
        return imagenIdActual;
    }

    public void setImagenIdActual(UUID imagenIdActual) {
        this.imagenIdActual = imagenIdActual;
    }

    public boolean isQuitarImagen() {
        return quitarImagen;
    }

    public void setQuitarImagen(boolean quitarImagen) {
        this.quitarImagen = quitarImagen;
    }

    public String getImagenAlt() {
        return imagenAlt;
    }

    public void setImagenAlt(String imagenAlt) {
        this.imagenAlt = imagenAlt;
    }
}
