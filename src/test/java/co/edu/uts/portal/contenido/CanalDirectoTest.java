package co.edu.uts.portal.contenido;

import co.edu.uts.portal.contenido.domain.CanalDirecto;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * HU-07, hallazgo de la reunion con la directora (22/9/2026): "Abrir canal no lleva a
 * ningun lado". El contacto directo se interpreta, se normaliza y se muestra a la
 * vista; solo se generan enlaces con esquemas conocidos.
 */
class CanalDirectoTest {

    @Test
    void correoEscritoTalCualSeGuardaComoMailto() {
        assertThat(CanalDirecto.normalizar("  bienestar@correo.uts.edu.co ")).hasValue("mailto:bienestar@correo.uts.edu.co");
        assertThat(CanalDirecto.normalizar("mailto:asesoriapsicologica@correo.uts.edu.co"))
                .hasValue("mailto:asesoriapsicologica@correo.uts.edu.co");
        assertThat(CanalDirecto.normalizar("MAILTO:cae@correo.uts.edu.co?subject=Hola"))
                .hasValue("mailto:cae@correo.uts.edu.co");
    }

    @Test
    void telefonosSeGuardanSoloConDigitos() {
        assertThat(CanalDirecto.normalizar("123")).hasValue("tel:123");
        assertThat(CanalDirecto.normalizar("tel:123")).hasValue("tel:123");
        assertThat(CanalDirecto.normalizar("(607) 691 7700")).hasValue("tel:6076917700");
        assertThat(CanalDirecto.normalizar("+57 300 123 4567")).hasValue("tel:+573001234567");
    }

    @Test
    void enlacesWebYWhatsappSinEsquemaSeCompletanConHttps() {
        assertThat(CanalDirecto.normalizar("www.uts.edu.co/sitio/bienestar-institucional/"))
                .hasValue("https://www.uts.edu.co/sitio/bienestar-institucional/");
        assertThat(CanalDirecto.normalizar("wa.me/573001234567")).hasValue("https://wa.me/573001234567");
        assertThat(CanalDirecto.normalizar("https://www.uts.edu.co")).hasValue("https://www.uts.edu.co");
    }

    @Test
    void esquemasPeligrososOTextoLibreSeRechazan() {
        assertThat(CanalDirecto.normalizar("javascript:alert(1)")).isEmpty();
        assertThat(CanalDirecto.normalizar("data:text/html,<script>alert(1)</script>")).isEmpty();
        assertThat(CanalDirecto.normalizar("file:///C:/secreto.txt")).isEmpty();
        assertThat(CanalDirecto.normalizar("Oficina del CAE, edificio B")).isEmpty();
        assertThat(CanalDirecto.normalizar("12")).isEmpty();
        assertThat(CanalDirecto.normalizar("   ")).isEmpty();
        assertThat(CanalDirecto.normalizar(null)).isEmpty();
    }

    @Test
    void correoSeMuestraConCopiaYEnlaceAGmail() {
        CanalDirecto cd = CanalDirecto.desde("mailto:asesoriapsicologica@correo.uts.edu.co").orElseThrow();

        assertThat(cd.isCorreo()).isTrue();
        assertThat(cd.valor()).isEqualTo("asesoriapsicologica@correo.uts.edu.co");
        assertThat(cd.getTextoCopiable()).isEqualTo("asesoriapsicologica@correo.uts.edu.co");
        assertThat(cd.getHrefGmail())
                .startsWith("https://mail.google.com/mail/")
                .contains("to=asesoriapsicologica%40correo.uts.edu.co");
    }

    @Test
    void telefonoSeMuestraAgrupadoYSeCopiaSinEspacios() {
        CanalDirecto cd = CanalDirecto.desde("tel:6076917700").orElseThrow();

        assertThat(cd.isTelefono()).isTrue();
        assertThat(cd.valor()).isEqualTo("607 691 7700");
        assertThat(cd.getTextoCopiable()).isEqualTo("6076917700");
        assertThat(cd.href()).isEqualTo("tel:6076917700");
        assertThat(CanalDirecto.desde("tel:123").orElseThrow().valor()).isEqualTo("123");
    }

    @Test
    void whatsappYWebSeClasificanPorElDominio() {
        CanalDirecto wa = CanalDirecto.desde("https://wa.me/573001234567").orElseThrow();
        assertThat(wa.isWhatsapp()).isTrue();
        assertThat(wa.valor()).isEqualTo("+57 300 123 4567");
        assertThat(wa.getTextoCopiable()).isEqualTo("+573001234567");

        CanalDirecto web = CanalDirecto.desde("https://www.uts.edu.co/sitio/").orElseThrow();
        assertThat(web.isWeb()).isTrue();
        assertThat(web.valor()).isEqualTo("uts.edu.co");
    }

    @Test
    void enElPanelSeEditaSinElPrefijoTecnico() {
        assertThat(CanalDirecto.paraEditar("mailto:cae@correo.uts.edu.co")).isEqualTo("cae@correo.uts.edu.co");
        assertThat(CanalDirecto.paraEditar("tel:123")).isEqualTo("123");
        assertThat(CanalDirecto.paraEditar("https://www.uts.edu.co")).isEqualTo("https://www.uts.edu.co");
        assertThat(CanalDirecto.paraEditar(null)).isNull();
    }

    @Test
    void correosConPorcentajeSeRechazanYConMasSeConservan() {
        // Revision previa al despliegue: un '%' hacia fallar URLDecoder al pintar /rutas y /buscar.
        assertThat(CanalDirecto.normalizar("cae%@correo.uts.edu.co")).isEmpty();
        assertThat(CanalDirecto.normalizar("mailto:x%zz@correo.uts.edu.co")).isEmpty();
        assertThat(CanalDirecto.normalizar("bienestar+cae@correo.uts.edu.co"))
                .hasValue("mailto:bienestar+cae@correo.uts.edu.co");
    }

    @Test
    void mostrarNuncaLanzaAunqueElDatoGuardadoSeaRaro() {
        assertThat(CanalDirecto.desde("mailto:x%zz@correo.uts.edu.co")).isEmpty();
        assertThat(CanalDirecto.desde("https://exa mple.com/con espacio")).isEmpty();
        assertThat(CanalDirecto.desde("https://ejemplo.com/a|b")).isEmpty();
        assertThat(CanalDirecto.desde("#")).isEmpty();
    }
}
