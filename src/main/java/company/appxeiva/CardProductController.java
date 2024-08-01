package company.appxeiva;

import com.sun.tools.javac.Main;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.AnchorPane;

import java.net.URL;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.text.NumberFormat;
import java.util.Date;
import java.util.Locale;
import java.util.ResourceBundle;

public class CardProductController implements Initializable {

    @FXML
    private Button prod_agregarBtn;

    @FXML
    private AnchorPane prod_form;

    @FXML
    private ImageView prod_imageView;

    @FXML
    private Label prod_name;

    @FXML
    private Label prod_price;

    @FXML
    private Spinner<Integer> prod_spinner;

    private ProductData productData;

    private Image image;

    private SpinnerValueFactory <Integer> spinner;

    private String prodId;

    private String type;

    private Connection connect;
    private ResultSet result;
    private PreparedStatement prepared;

    private MainFormController mFormController = new MainFormController();
    private ObservableList <ProductData> prodList = FXCollections.observableArrayList();


    //SETS SPINNER QTY TO SPINNER
    public void setSpinnerQty(){
        spinner = new SpinnerValueFactory.IntegerSpinnerValueFactory(0, 100, 0);
        spinner.setValue(0);
        prod_spinner.setValueFactory(spinner);

    }

    //Sets Product Card data
    Locale userLocale = Locale.getDefault();
    NumberFormat formatted = NumberFormat.getCurrencyInstance(userLocale);

    public void setData(ProductData prodData) {

        this.productData = prodData;

        this.prodId = prodData.getProdId();
        prod_name.setText(prodData.getProdNombre());
        prod_price.setText(formatted.format(prodData.getPrecio()));
        this.type = prodData.getTipo();

        String path = "File:" + prodData.getImagen();
        path.replace("//", "////");
        image = new Image(path, 130, 130, false, true);
        prod_imageView.setImage(image);

    }

    //BTN ADD
    int qty;
    public void addBtn (){

        String check = "";
        String sqlCheck = "SELECT * FROM productos where nombre_prod='" + prod_name.getText() + "'";
        connect = database.connectDb();

        try{
            prepared = connect.prepareStatement(sqlCheck);
            result = prepared.executeQuery();

            if(result.next()){
                check = result.getString("estado");
            }

            connect.close();
            result.close();
            prepared.close();



        }catch (Exception e){e.printStackTrace();}

        qty = prod_spinner.getValue();
        if(qty == 0 || check.equals("Inactivo") ){

            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Error");
            alert.setHeaderText(null);
            alert.setContentText("No se puede agregar este producto!");
            alert.showAndWait();

        }else{
            //Check Stock
            int checkStock = 0;
            String checkStockSql = "SELECT * FROM productos where nombre_prod= '" + prod_name.getText() + "'";
            connect = database.connectDb();

            try{
                prepared = connect.prepareStatement(checkStockSql);
                result = prepared.executeQuery();

                if(result.next()){
                    checkStock = result.getInt("stock");
                }

                connect.close();
                result.close();
                prepared.close();

                if (checkStock < qty){
                    Alert alert = new Alert(Alert.AlertType.ERROR);
                    alert.setTitle("Error");
                    alert.setHeaderText(null);
                    alert.setContentText("Este producto está agotado!");
                    alert.showAndWait();
                }else{
                    String sql = "INSERT INTO clientes (cliente_id, nombre_prod, cantidad, precio_tot, fecha, em_nbrUsuario, prod_id, precio_uni) " +
                            "VALUES (?,?,?,?,?,?,?,?)";
                    connect = database.connectDb();

                    try {

                        Date date = new Date();
                        java.sql.Date sqlDate = new java.sql.Date(date.getTime());


                        prepared = connect.prepareStatement(sql);
                        prepared.setInt(1, DataName.cId);
                        prepared.setString(2, prod_name.getText());
                        prepared.setInt(3, qty);
                        prepared.setDouble(4, productData.getPrecio() * qty);
                        prepared.setString(5, String.valueOf(sqlDate));
                        prepared.setString(6, DataName.username);
                        prepared.setString(7, prodId);
                        prepared.setDouble(8, productData.getPrecio());
                        prepared.executeUpdate();

                        Alert alert = new Alert(Alert.AlertType.INFORMATION);
                        alert.setTitle("Información");
                        alert.setHeaderText(null);
                        alert.setContentText("Producto agregado!");
                        alert.showAndWait();

                        spinner.setValue(0);

                        prepared.close();
                        connect.close();

                        connect = database.connectDb();
                        int stockUp = checkStock - qty;
                        String sqlUpdate = "UPDATE productos SET stock= '" + stockUp + "' WHERE prod_id= '" + prodId + "'";

                        prepared = connect.prepareStatement(sqlUpdate);
                        prepared.executeUpdate();

                        connect.close();
                        prepared.close();

                    }catch (Exception e){e.printStackTrace();}
                }

            }catch (Exception e){e.printStackTrace();}



        }
    }





    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        setSpinnerQty();
    }
}
