package company.appxeiva;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

public class ProductData {
    private Integer id;
    private String prodId;
    private Integer stock;
    private Double precio;
    private String prodNombre;
    private String tipo;
    private String estado;
    private String imagen;
    private String fecha;
    private Integer qty;
    private static ProductData product;
    private ObservableList<ProductData> products;

    private ProductData() {
        products = FXCollections.observableArrayList();
    }

    public ProductData(Integer id, String prodId, Integer stock, Double precio,
                       String prodNombre, String tipo, String estado, String fecha, String imagen) {
        this.id = id;
        this.prodId = prodId;
        this.stock = stock;
        this.precio = precio;
        this.prodNombre = prodNombre;
        this.tipo = tipo;
        this.estado = estado;
        this.imagen = imagen;
        this.fecha = fecha;
    }
    public ProductData(Integer id, String prodId, Double precio,
                       String prodNombre, String imagen) {
        this.id = id;
        this.prodId = prodId;
        this.precio = precio;
        this.prodNombre = prodNombre;
        this.imagen = imagen;
    }

    public ProductData (String prodNombre, Integer qty , Double precio, int id){
        this.prodNombre = prodNombre;
        this.qty = qty;
        this.precio = precio;
        this.id = id;
    }

    public Integer getQty(){
        return qty;
    }

    public Integer getId() {
        return id;
    }

    public String getProdId() {
        return prodId;
    }

    public Integer getStock(){
        return stock;
    }

    public Double getPrecio(){
        return precio;
    }

    public String getProdNombre(){
        return prodNombre;
    }

    public String getTipo(){
        return tipo;
    }

    public String getEstado(){
        return estado;
    }

    public String getImagen(){
        return imagen;
    }

    public String getFecha(){
        return fecha;
    }

}
