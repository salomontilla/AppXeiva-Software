package company.appxeiva;

import java.sql.Date;

public class CustomersData {

    private Integer clienteId;
    private String fecha;
    private Double total;
    private String emNbrUsuario;

    public CustomersData(Integer clienteId, Double total, String fecha, String emNbrUsuario) {
        this.clienteId = clienteId;
        this.fecha = fecha;
        this.emNbrUsuario = emNbrUsuario;
        this.total = total;
    }

    public Integer getClienteId() {
        return clienteId;
    }
    public void setClienteId(Integer clienteId) {
        this.clienteId = clienteId;
    }

    public String getFecha() {
        return fecha;
    }
    public void setFecha(String fecha) {
        this.fecha = fecha;
    }

    public Double getTotal() {
        return total;
    }
    public void setTotal(Double total) {
        this.total = total;
    }

    public String getEmNbrUsuario() {
        return emNbrUsuario;
    }
    public void setEmNbrUsuario(String emNbrUsuario) {
        this.emNbrUsuario = emNbrUsuario;
    }
}
