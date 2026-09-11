package co.edu.uts.portal.cuestionario;

import co.edu.uts.portal.bitacora.domain.AccionBitacora;
import co.edu.uts.portal.bitacora.service.BitacoraService;
import co.edu.uts.portal.cuestionario.domain.Cuestionario;
import co.edu.uts.portal.cuestionario.repository.CuestionarioRepository;
import co.edu.uts.portal.cuestionario.service.CuestionarioService;
import co.edu.uts.portal.cuestionario.service.ValidadorVersion;
import co.edu.uts.portal.cuestionario.web.dto.CuestionarioForm;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/** HU-22: creacion, edicion y ciclo de versiones de Cuestionario quedan en la bitacora. */
class CuestionarioServiceTest {

    private final CuestionarioRepository repo = mock(CuestionarioRepository.class);
    private final ValidadorVersion validador = mock(ValidadorVersion.class);
    private final BitacoraService bitacora = mock(BitacoraService.class);
    private final CuestionarioService service = new CuestionarioService(repo, validador, bitacora);

    private CuestionarioForm form() {
        CuestionarioForm f = new CuestionarioForm();
        f.setNombre("Tamizaje");
        f.setActivo(true);
        return f;
    }

    @Test
    void crearRegistraCuestionarioCreado() {
        when(repo.existsBySlug(any())).thenReturn(false);
        service.crear(form());
        verify(bitacora).registrar(eq(AccionBitacora.CUESTIONARIO_CREADO), eq("Cuestionario"), any(), any());
    }

    @Test
    void actualizarDatosRegistraCuestionarioActualizado() {
        Cuestionario c = new Cuestionario("Tamizaje");
        ReflectionTestUtils.setField(c, "id", 1L);
        when(repo.findById(1L)).thenReturn(Optional.of(c));

        service.actualizarDatos(1L, form());

        verify(bitacora).registrar(eq(AccionBitacora.CUESTIONARIO_ACTUALIZADO), eq("Cuestionario"), eq(1L), any());
    }

    @Test
    void crearNuevaVersionRegistraVersionCreada() {
        Cuestionario c = new Cuestionario("Tamizaje");
        ReflectionTestUtils.setField(c, "id", 2L);
        c.nuevaVersionVacia().archivar();   // v1 ya no editable
        when(repo.findById(2L)).thenReturn(Optional.of(c));

        service.crearNuevaVersion(2L);

        verify(bitacora).registrar(eq(AccionBitacora.VERSION_CREADA), eq("Cuestionario"), eq(2L), any());
    }

    @Test
    void publicarRegistraVersionPublicada() {
        Cuestionario c = new Cuestionario("Tamizaje");
        ReflectionTestUtils.setField(c, "id", 3L);
        c.nuevaVersionVacia();
        when(repo.findById(3L)).thenReturn(Optional.of(c));
        when(validador.problemas(any())).thenReturn(List.of());

        service.publicar(3L, 1);

        verify(bitacora).registrar(eq(AccionBitacora.VERSION_PUBLICADA), eq("Cuestionario"), eq(3L), any());
    }

    @Test
    void publicarUnaNuevaVersionArchivaLaAnteriorYLoRegistra() {
        Cuestionario c = new Cuestionario("Tamizaje");
        ReflectionTestUtils.setField(c, "id", 4L);
        c.nuevaVersionVacia().publicar(Instant.now());   // v1 publicada
        c.nuevaVersionVacia();                           // v2 borrador
        when(repo.findById(4L)).thenReturn(Optional.of(c));
        when(validador.problemas(any())).thenReturn(List.of());

        service.publicar(4L, 2);

        verify(bitacora).registrar(eq(AccionBitacora.VERSION_ARCHIVADA), eq("Cuestionario"), eq(4L), any());
        verify(bitacora).registrar(eq(AccionBitacora.VERSION_PUBLICADA), eq("Cuestionario"), eq(4L), any());
    }

    @Test
    void archivarRegistraVersionArchivada() {
        Cuestionario c = new Cuestionario("Tamizaje");
        ReflectionTestUtils.setField(c, "id", 5L);
        c.nuevaVersionVacia().publicar(Instant.now());
        when(repo.findById(5L)).thenReturn(Optional.of(c));

        service.archivar(5L, 1);

        verify(bitacora).registrar(eq(AccionBitacora.VERSION_ARCHIVADA), eq("Cuestionario"), eq(5L), any());
    }
}
