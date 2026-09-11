package co.edu.uts.portal.bitacora;

import co.edu.uts.portal.bitacora.domain.AccionBitacora;
import co.edu.uts.portal.bitacora.domain.RegistroBitacora;
import co.edu.uts.portal.bitacora.repository.RegistroBitacoraRepository;
import co.edu.uts.portal.bitacora.service.BitacoraService;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.data.domain.AuditorAware;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.jpa.domain.Specification;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
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

    @Test
    void buscarDelegaEnElRepositorioConLaMismaPagina() {
        // Se arma con Specification (no un @Query con "is null") justamente para que un
        // filtro ausente no envie un parametro de fecha nulo a Postgres; aqui solo se
        // verifica la delegacion, el filtrado real lo cubre la verificacion end-to-end.
        when(repo.findAll(org.mockito.ArgumentMatchers.<Specification<RegistroBitacora>>any(),
                org.mockito.ArgumentMatchers.<org.springframework.data.domain.Pageable>any()))
                .thenReturn(Page.empty());
        BitacoraService service = new BitacoraService(repo, Optional::empty);
        var pageable = PageRequest.of(0, 20);

        service.buscar("Usuario", Instant.parse("2026-01-01T00:00:00Z"), null, pageable);

        verify(repo).findAll(any(Specification.class), eq(pageable));
    }

    @Test
    void tiposObjetoDelegaEnElRepositorio() {
        when(repo.findDistinctTipoObjeto()).thenReturn(List.of("Recurso", "Usuario"));
        BitacoraService service = new BitacoraService(repo, Optional::empty);

        assertThat(service.tiposObjeto()).containsExactly("Recurso", "Usuario");
    }
}
