package com.logistica.dto;

import lombok.Data;

import java.time.LocalDate;

@Data
public class AsignarEntregaForm {
    private Long repartidorId;
    private Long vehiculoId;
    private LocalDate fechaEntregaPrevista;
    private String notas;
}
