package co.edu.uts.portal.contenido.domain;

/**
 * Imagen ya validada y normalizada (tamano, orientacion, metadatos), lista para
 * guardarse. La produce ProcesadorImagen a partir del archivo que sube el
 * administrador; todavia no existe en la base de datos.
 */
public record ImagenProcesada(String tipoContenido, byte[] datos, int ancho, int alto, String nombreOriginal) {
}
