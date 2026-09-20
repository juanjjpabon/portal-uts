package co.edu.uts.portal.analitica.service;

import co.edu.uts.portal.common.CsvUtil;
import co.edu.uts.portal.contenido.domain.EstadoPublicacion;
import co.edu.uts.portal.contenido.domain.Recurso;
import co.edu.uts.portal.contenido.domain.TipoRecurso;
import co.edu.uts.portal.contenido.repository.RecursoRepository;
import co.edu.uts.portal.cuestionario.repository.NivelResultadoRepository;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * HU-23/HU-24: estadisticas basicas agregadas, siempre sobre los contadores simples
 * de Recurso/NivelResultado -- nunca sobre un registro individual (no existe tal
 * cosa, HU-13). Ninguna fila con menos de {@link #UMBRAL_MINIMO} se expone, ni en el
 * reporte ni en el CSV (HU-24): es el mismo filtro para ambos.
 */
@Service
public class AnaliticaService {

    static final int UMBRAL_MINIMO = 5;

    private final RecursoRepository recursoRepository;
    private final NivelResultadoRepository nivelResultadoRepository;

    public AnaliticaService(RecursoRepository recursoRepository, NivelResultadoRepository nivelResultadoRepository) {
        this.recursoRepository = recursoRepository;
        this.nivelResultadoRepository = nivelResultadoRepository;
    }

    @PreAuthorize("hasRole('ADMIN_FUNCIONAL')")
    @Transactional(readOnly = true)
    public ReporteAnalitica generarReporte() {
        var contenidos = recursoRepository.masConsultados(TipoRecurso.CONTENIDO, EstadoPublicacion.PUBLICADO,
                        UMBRAL_MINIMO).stream()
                .map(r -> new ReporteAnalitica.FilaContenido(r.getTitulo(), r.getVistas()))
                .toList();

        var niveles = nivelResultadoRepository.resumenPorNivel(UMBRAL_MINIMO).stream()
                .map(n -> new ReporteAnalitica.FilaNivel(n.getCuestionario(), n.getNivel(), n.getVeces()))
                .toList();

        var valoraciones = recursoRepository.valoradosConSuficientesVotos(EstadoPublicacion.PUBLICADO, UMBRAL_MINIMO)
                .stream()
                .map(this::aFilaValoracion)
                .toList();

        return new ReporteAnalitica(contenidos, niveles, valoraciones);
    }

    @PreAuthorize("hasRole('ADMIN_FUNCIONAL')")
    @Transactional(readOnly = true)
    public String generarCsv() {
        ReporteAnalitica r = generarReporte();
        StringBuilder csv = new StringBuilder("﻿");   // BOM UTF-8, para que Excel no rompa tildes/enye
        csv.append("seccion,nombre,indicador,valor\r\n");

        for (var f : r.contenidosMasConsultados()) {
            fila(csv, "Contenidos más consultados", f.titulo(), "vistas", f.vistas());
        }
        for (var f : r.autoorientacionPorNivel()) {
            fila(csv, "Autoorientación por nivel", f.cuestionario() + " - " + f.nivel(), "veces", f.veces());
        }
        for (var f : r.valoraciones()) {
            fila(csv, "Valoraciones de utilidad", f.titulo(), "util", f.util());
            fila(csv, "Valoraciones de utilidad", f.titulo(), "no_util", f.noUtil());
        }
        return csv.toString();
    }

    private void fila(StringBuilder csv, Object... valores) {
        for (int i = 0; i < valores.length; i++) {
            if (i > 0) {
                csv.append(',');
            }
            csv.append(CsvUtil.celda(valores[i]));
        }
        csv.append("\r\n");
    }

    private ReporteAnalitica.FilaValoracion aFilaValoracion(Recurso r) {
        return new ReporteAnalitica.FilaValoracion(r.getTitulo(), r.getTipo().getEtiquetaSingular(),
                r.getValoracionesUtil(), r.getValoracionesNoUtil());
    }
}
