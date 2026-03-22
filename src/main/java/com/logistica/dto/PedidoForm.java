package com.logistica.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class PedidoForm {

    @NotBlank(message = "La dirección de entrega es obligatoria")
    private String direccionEntrega;

    private String observaciones;

    @Valid
    private List<ItemPedidoForm> items = new ArrayList<>();
}
