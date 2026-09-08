package co.edu.uts.portal.parametros.domain;

import co.edu.uts.portal.common.domain.BaseAuditable;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

/**
 * Parametro operativo del portal (HU-27): avisos de autoorientacion (HU-08/HU-11),
 * mensaje de urgencia (HU-06), datos del canal institucional (HU-07). Es un
 * catalogo fijo: el administrador funcional edita {@link #valor}, no crea ni borra
 * claves. Fecha y responsable de cada cambio vienen de {@link BaseAuditable}.
 */
@Entity
@Table(name = "parametro")
public class Parametro extends BaseAuditable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "clave", nullable = false, unique = true, length = 80)
    private String clave;

    @Column(name = "valor", columnDefinition = "text")
    private String valor;

    @Column(name = "descripcion", nullable = false, length = 200)
    private String descripcion;

    @Enumerated(EnumType.STRING)
    @Column(name = "tipo", nullable = false, length = 20)
    private TipoParametro tipo = TipoParametro.TEXTO_CORTO;

    @Column(name = "grupo", nullable = false, length = 60)
    private String grupo;

    @Column(name = "orden", nullable = false)
    private int orden = 0;

    protected Parametro() {
    }

    public void setValor(String valor) {
        this.valor = valor;
    }

    public Long getId() {
        return id;
    }

    public String getClave() {
        return clave;
    }

    public String getValor() {
        return valor;
    }

    public String getDescripcion() {
        return descripcion;
    }

    public TipoParametro getTipo() {
        return tipo;
    }

    public String getGrupo() {
        return grupo;
    }

    public int getOrden() {
        return orden;
    }
}
