package org.example.fronted.dto;

public class SubirFormatoAResponseDTO {

    private Long idProyecto;
    private String formatoAToken;
    private String cartaToken; // puede venir null

    public Long getIdProyecto() { return idProyecto; }
    public void setIdProyecto(Long idProyecto) { this.idProyecto = idProyecto; }

    public String getFormatoAToken() { return formatoAToken; }
    public void setFormatoAToken(String formatoAToken) { this.formatoAToken = formatoAToken; }

    public String getCartaToken() { return cartaToken; }
    public void setCartaToken(String cartaToken) { this.cartaToken = cartaToken; }
}
