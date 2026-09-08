package co.edu.uts.portal.common;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class SlugsTest {

    @Test
    void quitaAcentosYNormalizaEspacios() {
        assertThat(Slugs.de("Señales de alerta")).isEqualTo("senales-de-alerta");
        assertThat(Slugs.de("  Rutas   institucionales  ")).isEqualTo("rutas-institucionales");
        assertThat(Slugs.de("¿Qué hacer?")).isEqualTo("que-hacer");
    }

    @Test
    void textoVacioDaCadenaVacia() {
        assertThat(Slugs.de(null)).isEmpty();
        assertThat(Slugs.de("   ")).isEmpty();
    }
}
