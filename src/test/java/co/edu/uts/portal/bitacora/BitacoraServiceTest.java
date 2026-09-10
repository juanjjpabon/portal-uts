package co.edu.uts.portal.bitacora;

import co.edu.uts.portal.bitacora.domain.AccionBitacora;
import co.edu.uts.portal.bitacora.domain.RegistroBitacora;
import co.edu.uts.portal.bitacora.repository.RegistroBitacoraRepository;
import co.edu.uts.portal.bitacora.service.BitacoraService;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.data.domain.AuditorAware;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class BitacoraServiceTest {

    private final RegistroBitacoraRepository repo = mock(RegistroBitacoraRepository.class);

    @Test
    void registrarFijaActorAccionYObjeto() {
        AuditorAware<String> auditor = () -> Optional.of("tec@uts.edu.co");
        BitacoraService service = new BitacoraService(repo, auditor);

        service.registrar(AccionBitacora.ROL_ASIGNADO, "Usuario", 7L, "Asigno el rol X a y@uts.edu.co");

        ArgumentCaptor<RegistroBitacora> cap = ArgumentCaptor.forClass(RegistroBitacora.class);
        verify(repo).save(cap.capture());
        RegistroBitacora r = cap.getValue();
        assertThat(r.getActor()).isEqualTo("tec@uts.edu.co");
        assertThat(r.getAccion()).isEqualTo(AccionBitacora.ROL_ASIGNADO);
        assertThat(r.getTipoObjeto()).isEqualTo("Usuario");
        assertThat(r.getObjetoId()).isEqualTo("7");
        assertThat(r.getOcurridoEn()).isNotNull();
    }

    @Test
    void sinAuditorElActorEsSistema() {
        BitacoraService service = new BitacoraService(repo, Optional::empty);
        service.registrar(AccionBitacora.USUARIO_CREADO, "Usuario", 1L, "x");

        ArgumentCaptor<RegistroBitacora> cap = ArgumentCaptor.forClass(RegistroBitacora.class);
        verify(repo).save(cap.capture());
        assertThat(cap.getValue().getActor()).isEqualTo("sistema");
    }
}
