package com.logistica.dto;

import com.logistica.model.Entrega;
import lombok.Data;

@Data
public class ActualizarEstadoForm {
    private Entrega.EstadoEntrega estado;
    private String notas;
}
