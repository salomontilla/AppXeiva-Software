package company.appxeiva;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.chart.AreaChart;
import javafx.scene.chart.XYChart;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Pane;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import net.sf.jasperreports.engine.*;
import net.sf.jasperreports.engine.design.JasperDesign;
import net.sf.jasperreports.engine.xml.JRXmlLoader;
import net.sf.jasperreports.view.JasperViewer;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.sql.*;
import java.text.NumberFormat;
import java.util.*;

import static java.lang.Integer.parseInt;


public class MainFormController implements Initializable {

    Locale userLocale = Locale.getDefault();
    NumberFormat format = NumberFormat.getCurrencyInstance(userLocale);



    //------------------------------------
    // DATABASE DECLARATIONS
    //------------------------------------

    Connection connect;
    PreparedStatement prepared;
    Statement statement;
    ResultSet result;


    //------------------------------------
    //NAV LEFT BAR
    //------------------------------------
    @FXML
    private AnchorPane main_form;

    @FXML
    private Button dash_btnClientes;

    @FXML
    private Button dash_btnEstadistcas;

    @FXML
    private Button dash_btnInventario;

    @FXML
    private Button dash_btnLogout;

    @FXML
    private Button dash_btnMenu;

    @FXML
    private Label dash_username;

    //------------------------------------
    // METHODS
    //------------------------------------

    @FXML
    public void displayUsername(){//SET LABEL NAME INTO USERNAME

        String user = DataName.username;
        user = user.substring(0,1).toUpperCase() + user.substring(1);
        dash_username.setText(user);
    }

    //------------------------------------
    // BUTTON ACTIONS
    //------------------------------------

    //SWITCH FORMS
    @FXML
    private void switchForm(ActionEvent event) {
        Button sourceButton = (Button) event.getSource();

        if (sourceButton == dash_btnEstadistcas) {
            setVisibleForm(stats_form);
            setNumClientes();
            setIngresosHoy();
            setTotIngresos();
            setProdVendidos();
            showIngresosChart();
            showProdVendChart();
        } else if (sourceButton == dash_btnMenu) {
            setVisibleForm(menu_form);
            menuDisplayCard();
            displayMenuOrder();
            menuGetTotal();
            menuSetTotal();
        } else if (sourceButton == dash_btnInventario) {
            setVisibleForm(inventory_form);
            showProdData();
        } else if (sourceButton == dash_btnClientes) {
            setVisibleForm(clientes_form);
            showClientes();
        }

        updateButtonStyles(sourceButton);
    }

    private void setVisibleForm(Pane visibleForm) {
        List<Pane> allForms = Arrays.asList(stats_form, menu_form, inventory_form, clientes_form);
        for (Pane form : allForms) {
            form.setVisible(form == visibleForm);
        }
    }

    private void updateButtonStyles(Button activeButton) {
        List<Button> allButtons = Arrays.asList(dash_btnMenu, dash_btnInventario, dash_btnClientes, dash_btnEstadistcas);
        for (Button button : allButtons) {
            button.getStyleClass().removeAll("nav-button", "nav-button2"); // Eliminar ambas clases de estilo
            if (button.equals(activeButton)) {
                button.getStyleClass().add("nav-button2");
            } else {
                button.getStyleClass().add("nav-button");
            }
        }
    }

    //BTN LOGOUT ------------------------------------
    @FXML
    public void logoutBtn() {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Cerrar Sesión");
        alert.setContentText("¿Deseas cerrar sesión?");
        Optional<ButtonType> option = alert.showAndWait();

        if (option.isPresent() && option.get() == ButtonType.OK) {
            try {
                // Cerrar la ventana actual
                Stage currentStage = (Stage) dash_btnLogout.getScene().getWindow();
                currentStage.hide();

                // Cargar la nueva ventana
                Parent root = FXMLLoader.load(Objects.requireNonNull(getClass().getResource("Login.fxml")));
                Stage newStage = new Stage();
                newStage.setScene(new Scene(root));
                newStage.setTitle("App Xeiva");
                newStage.show();
            } catch (IOException e) {
                showErrorDialog();
            }
        }
    }

    private void showErrorDialog() {
        Alert errorAlert = new Alert(Alert.AlertType.ERROR);
        errorAlert.setTitle("Error");
        errorAlert.setContentText("No se pudo cargar la pantalla de inicio de sesión. Por favor, inténtelo de nuevo.");
        errorAlert.showAndWait();
    }


    //------------------------------------
    // INVENTORY FORM
    //------------------------------------
    @FXML
    private AnchorPane inventory_form;

    @FXML
    private Button inventory_btnAgregar;

    @FXML
    private Button inventory_btnActualizar;

    @FXML
    private Button inventory_btnEliminar;

    @FXML
    private Button inventory_btnImportar;

    @FXML
    private Button inventory_btnLimpiar;
    @FXML
    private TableView<ProductData> inventory_tableview;

    @FXML
    private TableColumn<ProductData, String> inventory_col_ID;

    @FXML
    private TableColumn<ProductData, String> inventory_col_estado;

    @FXML
    private TableColumn<ProductData, String> inventory_col_nombreProd;

    @FXML
    private TableColumn<ProductData, String> inventory_col_precio;

    @FXML
    private TableColumn<ProductData, String> inventory_col_stock;

    @FXML
    private TableColumn<ProductData, String> inventory_col_tipo;

    @FXML
    private TableColumn<ProductData, java.sql.Date> inventory_col_fecha;

    @FXML
    private ImageView inventory_imageView;

    @FXML
    private TextField inventory_ID;

    @FXML
    private TextField inventory_nombreProducto;

    @FXML
    private TextField inventory_precio;

    @FXML
    private TextField inventory_stock;

    @FXML
    private ComboBox<?> inventory_tipo;
    //Charge data ComboBox Tipo
    private String [] tipoList = {"Carne", "Pescado", "Bebida", "Pollo"};
    public void showComboTipo (){
        ArrayList <String> tipos = new ArrayList<>();
        for(String data : tipoList){
            tipos.add(data);
        }
        ObservableList tipoCombo = FXCollections.observableArrayList(tipos);
        inventory_tipo.setItems(tipoCombo);

    }

    @FXML
    private ComboBox<?> inventory_estado;
    private String [] estadoList = {"Activo", "Inactivo"};
    public void showComboEstado (){
        ArrayList<String> estados = new ArrayList<>();
        for(String data : estadoList){
            estados.add(data);
        }
        ObservableList estadoCombo = FXCollections.observableArrayList(estados);
        inventory_estado.setItems(estadoCombo);

    }

    //------------------------------------
    // METHODS
    //------------------------------------

    //CHARGE VALUES TO EACH PRODUCT
    public ObservableList<ProductData> inventoryDataList() {
        ObservableList<ProductData> dataList = FXCollections.observableArrayList();
        connect = database.connectDb();

        try {
            prepared = connect.prepareStatement("SELECT * FROM productos");
            result = prepared.executeQuery();

            while (result.next()) {


                ProductData prodData = new ProductData(
                        result.getInt("id"),
                        result.getString("prod_id"),
                        result.getInt("stock"),
                        result.getDouble("precio"),
                        result.getString("nombre_prod"),
                        result.getString("tipo"),
                        result.getString("estado"),
                        result.getString("fecha"),
                        result.getString("imagen")
                );
                dataList.add(prodData);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            try {
                if (result != null) result.close();
                if (connect != null) connect.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
        return dataList;
    }

    //SHOW PRODUCTOS ON TABLEVIEW
    private ObservableList <ProductData> inventoryListData;

    public void showProdData(){
        inventoryListData = inventoryDataList();
        inventory_col_ID.setCellValueFactory(new PropertyValueFactory<>("prodId"));
        inventory_col_nombreProd.setCellValueFactory(new PropertyValueFactory<>("prodNombre"));
        inventory_col_tipo.setCellValueFactory(new PropertyValueFactory<>("tipo"));
        inventory_col_stock.setCellValueFactory(new PropertyValueFactory<>("stock"));
        inventory_col_precio.setCellValueFactory(new PropertyValueFactory<>("precio"));
        inventory_col_estado.setCellValueFactory(new PropertyValueFactory<>("estado"));
        inventory_col_fecha.setCellValueFactory(new PropertyValueFactory<>("fecha"));

        inventory_tableview.setItems(inventoryListData);
    }

    //GET SELECTED ITEM IN TABLE VIEW
    public void inventorySelectData(){

        ProductData product = inventory_tableview.getSelectionModel().getSelectedItem();
        int index = inventory_tableview.getSelectionModel().getSelectedIndex();
        if ( index < 0) return;

        inventory_ID.setText(product.getProdId());
        inventory_nombreProducto.setText(product.getProdNombre());
        inventory_stock.setText(product.getStock().toString());
        inventory_precio.setText(product.getPrecio().toString());

        DataName.path = product.getImagen();
        String path = "File:" + product.getImagen();

        DataName.id = product.getId();
        DataName.date = String.valueOf(product.getFecha());

        Image image = new Image(path, 130, 130, false, true);
        inventory_imageView.setImage(image);
    }

    //------------------------------------
    // BUTTON ACTIONS
    //------------------------------------

    //BTN ADD ------------------------------------
    public void inventoryAddBtn(){

        if(inventory_ID.getText().isEmpty()||
                inventory_nombreProducto.getText().isEmpty()||
                inventory_tipo.getSelectionModel().getSelectedItem() == null||
                inventory_precio.getText().isEmpty()||
                inventory_stock.getText().isEmpty()||
                DataName.path ==null||
                inventory_estado.getSelectionModel().getSelectedItem() == null){

            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Campos Vacíos");
            alert.setHeaderText(null);
            alert.setContentText("Debes llenar todos los campos");
            alert.showAndWait();

        }else{
            connect = database.connectDb();
            try{
                statement = connect.createStatement();
                result = statement.executeQuery("SELECT prod_id FROM productos where prod_id= '" + inventory_ID.getText() + "'" );
                if(result.next()){
                    Alert alert = new Alert(Alert.AlertType.ERROR);
                    alert.setTitle("En uso");
                    alert.setHeaderText(null);
                    alert.setContentText("El id: " + inventory_ID.getText() + " ya está en uso");
                    alert.showAndWait();
                }else{
                    prepared = connect.prepareStatement("INSERT INTO productos (prod_id, nombre_prod, tipo, stock, " +
                            "precio, estado, imagen, fecha) " +
                            "VALUES (?,?,?,?,?,?,?,?)");
                    prepared.setString(1, inventory_ID.getText());
                    prepared.setString(2, inventory_nombreProducto.getText());
                    prepared.setString(3, inventory_tipo.getSelectionModel().getSelectedItem().toString());
                    prepared.setString(4, inventory_stock.getText());
                    prepared.setString(5, inventory_precio.getText());
                    prepared.setString(6, inventory_estado.getSelectionModel().getSelectedItem().toString());

                    String path = DataName.path;
                    path.replace("//", "////");
                    prepared.setString(7, path);

                    java.util.Date date = new java.util.Date();
                    java.sql.Date sqlDate = new java.sql.Date(date.getTime());

                    prepared.setString(8, String.valueOf(sqlDate));
                    prepared.executeUpdate();

                    Alert alert = new Alert(Alert.AlertType.INFORMATION);
                    alert.setTitle("Registro exitoso");
                    alert.setHeaderText(null);
                    alert.setContentText("Producto registrado exitosamente!");
                    alert.showAndWait();

                    showProdData();
                    inventoryClearBtn();
                }
                connect.close();
                prepared.close();
                result.close();
                statement.close();
            }catch (Exception e){e.printStackTrace();}
        }
    }


    //BTN IMPORT ------------------------------------
    public void inventoryImportBtn(){
        FileChooser fileChooser = new FileChooser();
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Abrir archivo de imagen", "*.jpg","*.png"));

        File file = fileChooser.showOpenDialog(inventory_form.getScene().getWindow());

        if(file != null){
            DataName.path = file.getAbsolutePath();
            Image image = new Image(file.toURI().toString(), 130, 130, false, true);
            inventory_imageView.setImage(image);
        }
    }

    //BTN CLEAR--------------------------
    public void inventoryClearBtn(){
        inventory_ID.setText("");
        inventory_nombreProducto.setText("");
        inventory_tipo.getSelectionModel().clearSelection();
        inventory_precio.setText("");
        inventory_stock.setText("");
        inventory_estado.getSelectionModel().clearSelection();
        DataName.path = "";
        DataName.id = 0;
        inventory_imageView.setImage(null);
    }

    // BTN UPDATE --------------------------
    public void inventoryUpdateBtn(){

        if(inventory_ID.getText().isEmpty()||
                inventory_nombreProducto.getText().isEmpty()||
                inventory_tipo.getSelectionModel().getSelectedItem() == null||
                inventory_precio.getText().isEmpty()||
                inventory_stock.getText().isEmpty()||
                DataName.path ==null||
                inventory_estado.getSelectionModel().getSelectedItem() == null){

            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Campos Vacíos");
            alert.setHeaderText(null);
            alert.setContentText("Debes llenar todos los campos");
            alert.showAndWait();

        }else{
            connect = database.connectDb();

            String path = DataName.path;
            path.replace("//", "////");

            java.util.Date date = new java.util.Date();
            java.sql.Date sqlDate = new java.sql.Date(date.getTime());
            String fecha = sqlDate.toString();

            String sqlQuery = "UPDATE productos SET" +
                    " prod_id = '" + inventory_ID.getText() + "', " +
                    "nombre_prod = '" + inventory_nombreProducto.getText() + "', " +
                    "tipo = '" + inventory_tipo.getSelectionModel().getSelectedItem() + "', " +
                    "stock = '" + inventory_stock.getText() + "', " +
                    "precio = '" + inventory_precio.getText()  + "', " +
                    "estado = '" + inventory_estado.getSelectionModel().getSelectedItem() + "', " +
                    "imagen = '" + path + "', fecha ='" + fecha + "' " +
                    "WHERE id = '" + DataName.id + "'";
            try {

                Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
                alert.setTitle("Confirmación");
                alert.setHeaderText(null);
                alert.setContentText("¿Seguro que deseas modificar el producto " +inventory_ID.getText()+ "?");
                alert.showAndWait();

                if(alert.getResult() == ButtonType.OK){
                    prepared = connect.prepareStatement(sqlQuery);
                    prepared.executeUpdate();

                    alert = new Alert(Alert.AlertType.INFORMATION);
                    alert.setTitle("Modificación");
                    alert.setHeaderText(null);
                    alert.setContentText("Producto modificado!");
                    alert.showAndWait();

                    showProdData();
                    inventoryClearBtn();
                }
                connect.close();
                prepared.close();
            }catch(Exception e){e.printStackTrace();}

        }
    }

    //DELETE BTN
    public void inventoryDeleteBtn(){
        if(DataName.id == 0){
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Selecciona");
            alert.setHeaderText(null);
            alert.setContentText("Debes seleccionar un producto");
            alert.showAndWait();
        }else{

            String sqlQuery = "DELETE FROM productos WHERE id = '" + DataName.id + "'";
            connect = database.connectDb();

            Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
            alert.setTitle("Confirmación");
            alert.setHeaderText(null);
            alert.setContentText("¿Seguro que deseas eliminar el producto " +inventory_ID.getText()+ "?");
            alert.showAndWait();

            if(alert.getResult() == ButtonType.OK) {
                try {
                    prepared = connect.prepareStatement(sqlQuery);
                    prepared.executeUpdate();

                    alert = new Alert(Alert.AlertType.INFORMATION);
                    alert.setTitle("Eliminado");
                    alert.setHeaderText(null);
                    alert.setContentText("Producto eliminado!");
                    alert.showAndWait();

                    showProdData();
                    inventoryClearBtn();

                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

        }
    }

    //------------------------------------
    // MENU FORM
    //------------------------------------
    @FXML
    private Button menu_borrarBtn;

    @FXML
    private TableColumn<ProductData, Integer> menu_col_cantidad;

    @FXML
    private TableColumn<ProductData, Double> menu_col_precio;

    @FXML
    private TableColumn<ProductData, String> menu_col_producto;

    @FXML
    private Label menu_devuelvo;

    @FXML
    private Button menu_facturaBtn;

    @FXML
    private AnchorPane menu_form;

    @FXML
    private GridPane menu_gridPane;

    @FXML
    private Button menu_pagarBtn;

    @FXML
    private TextField menu_recibo;

    @FXML
    private ScrollPane menu_scrollPane;

    @FXML
    private TableView<ProductData> menu_tableView;

    @FXML
    private Label menu_total;

    @FXML
    private TextField menu_filter;
    //------------------------------------
    // METHODS
    //------------------------------------

    //SHOW PRODUCT CARD
    int row = 0, col = 0;
    ObservableList<ProductData> cardListData = FXCollections.observableArrayList();
    public void menuDisplayCard(){
        col = 0; row = 0;
        cardListData.clear();
        menu_gridPane.getChildren().clear();
        menu_gridPane.getRowConstraints().clear();
        menu_gridPane.getColumnConstraints().clear();
        cardListData.addAll(menuGetData());


        for (int i = 0; i < cardListData.size(); i++) {
            try {

                FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("CardProduct.fxml"));
                AnchorPane pane = fxmlLoader.load();
                CardProductController cardC = fxmlLoader.getController();
                cardC.setData(cardListData.get(i));
                cardC.setSpinnerQty();

                if (col == 4) {//ONLY ADMITS GRID PANE WITH 4 COLUMNS
                    col = 0;
                    row++;
                }

                menu_gridPane.add(pane, col++, row);//ADDS THE PANE
                GridPane.setMargin(pane, new Insets(10));

            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public void filterGridPane() throws IOException {
        System.out.println("in use");
        for (Node node : menu_gridPane.getChildren()) {
            if (node instanceof Pane) {
                for (Node child : ((Pane) node).getChildren()) {
                    if (child instanceof Pane) {
                        for (Node childV : ((Pane) child).getChildren()) {
                            for (Node childH : ((Pane) childV).getChildren()) {
                                if (childH instanceof Label) {
                                    Label label = (Label) childH;
                                    if (label.getText().toLowerCase().contains(menu_filter.getText().toLowerCase())) {
                                        node.setVisible(true);
                                        break;
                                    } else node.setVisible(false);
                                }
                            }
                        }
                    }
                }
            }
        }
        rearrangeGridPane();
    }

    public void rearrangeGridPane() throws IOException {
        List<Node> visiblePanes = new ArrayList<>();
        for (Node node : menu_gridPane.getChildren()) {
            if (node.isVisible()) {
                visiblePanes.add(node);
            }
        }
        menu_gridPane.getChildren().clear();
        col = 0;
        row = 0;

        for (Node node : visiblePanes) {
            col++;
            menu_gridPane.add(node, col, row);

            if (col == 4) {
                col = 0;
                row++;
            }
            menu_gridPane.setMargin(node, new Insets(10, 10, 10, 10));
        }
    }

    public void showPanes() throws IOException {
        if (menu_filter.getText().isEmpty()) {
            menu_gridPane.getChildren().clear();
            col = 0;
            row = 0;
            menuDisplayCard();

        } else {
            filterGridPane();
        }
    }

    //GET PRODUCT DATA TO FILL CARD PRODUCT

    ObservableList<ProductData> listData = FXCollections.observableArrayList();

    public ObservableList<ProductData> menuGetData() {
        listData.clear();

        String sql = "SELECT * FROM productos";
        connect = database.connectDb();

        try{

            prepared = connect.prepareStatement(sql);
            result = prepared.executeQuery();
            ProductData product;

            while (result.next()) {

                product = new ProductData(result.getInt("id"),
                        result.getString("prod_id"),
                        result.getDouble("precio"),
                        result.getString("nombre_prod"),
                        result.getString("imagen"));
                listData.add(product);

            }

            result.close();
            connect.close();

        }catch (Exception e){e.printStackTrace();}

        return listData;
    }



    //GETS CUSTOMER ID
    int cId;
    public void getCustomerId(){
        String sql = "SELECT MAX(cliente_id) FROM clientes";
        connect = database.connectDb();

        try {
            prepared = connect.prepareStatement(sql);
            result = prepared.executeQuery();

            if(result.next()){
                cId = result.getInt("MAX(cliente_id)");
            }
            connect.close();
            result.close();
            prepared.close();

            String checkId = "SELECT MAX(cliente_id) FROM factura";

            connect = database.connectDb();
            prepared = connect.prepareStatement(checkId);
            result = prepared.executeQuery();

            int checkCId = 0;
            if(result.next()){
                checkCId = result.getInt("MAX(cliente_id)");
            }

            if(checkCId == cId){
                cId++;
            }

            DataName.cId = cId;

            connect.close();
            prepared.close();
            result.close();


        }catch (Exception e){e.printStackTrace();}
    }

    //GETS PRODUCT DATA FROM CLIENTES
    private ObservableList<ProductData> orderListData = FXCollections.observableArrayList();
    public ObservableList<ProductData> menuGetOrder(){

        orderListData.clear();

        String sql = "SELECT * FROM clientes WHERE cliente_id = '" + cId + "'";
        connect = database.connectDb();
        try {
            prepared = connect.prepareStatement(sql);
            result = prepared.executeQuery();

            while (result.next()) {

                ProductData prodData = new ProductData(result.getString("nombre_prod"),
                        result.getInt("cantidad"),
                        result.getDouble("precio_tot"),
                        result.getInt("id"));
                orderListData.add(prodData);

            }
            connect.close();
            prepared.close();
            result.close();
        }catch (Exception e){e.printStackTrace();}

        return orderListData;
    }

    //SETS CUSTOMER DATA IN TABLE VIEW
    ObservableList <ProductData> listOrder = FXCollections.observableArrayList();
    public void displayMenuOrder(){
        listOrder = menuGetOrder();

        menu_col_producto.setCellValueFactory(new PropertyValueFactory<>("prodNombre"));
        menu_col_cantidad.setCellValueFactory(new PropertyValueFactory<>("qty"));
        menu_col_precio.setCellValueFactory(new PropertyValueFactory<>("precio"));

        menu_tableView.setItems(listOrder);
    }

    //GET TOTAL PRICE
    double total;
    public void menuGetTotal(){

        String sumSql = "SELECT SUM(precio_tot) FROM 'clientes' WHERE cliente_id = '" + cId+ "'";
        connect = database.connectDb();
        total = 0;
        try {
            prepared = connect.prepareStatement(sumSql);
            result = prepared.executeQuery();


            if (result.next()) {
                total = result.getDouble("SUM(precio_tot)");
            }

            connect.close();
            prepared.close();
            result.close();

        }catch (Exception e){e.printStackTrace();}
    }

    //SETS TOTAL LABEL TEXT
    public void menuSetTotal (){
        if(menu_total == null){
            menu_total = new Label();
        }
        menu_total.setText(format.format(total));

    }

    //SETS CHANGE LABEL TEXT

    public void setChange (){
        double change = received - total;
        if (received == total){
            menu_devuelvo.setText("COMPLETO");
        }else{
            menu_devuelvo.setText(format.format(change));
        }



    }

    //------------------------------------
    // BUTTON ACTIONS
    //------------------------------------

    //BTN PAY
    int received;
    public void payBtn(){
        menuGetTotal();
        getCustomerId();

        received = 0;
        String receiptS = menu_recibo.getText();

        if(!receiptS.isEmpty()){
            received = parseInt(receiptS);
        }


        if (total == 0 ){
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Error");
            alert.setHeaderText(null);
            alert.setContentText("Primero haz la orden!");
            alert.showAndWait();
            menu_devuelvo.setText("$0.000");

        }else if(menu_recibo.getText().isEmpty() || menu_recibo.equals("")){

            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Error");
            alert.setHeaderText(null);
            alert.setContentText("Ingresa un valor!");
            alert.showAndWait();
            menu_devuelvo.setText("$0.000");

        }else if (received < total || received == 0){

            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Error");
            alert.setHeaderText(null);
            alert.setContentText("Falta dinero!");
            alert.showAndWait();
            menu_devuelvo.setText("$0.000");

        }else{
            setChange();

            String insertSql = "INSERT INTO factura (cliente_id, total, fecha, em_nbrUsuario) VALUES (?,?,?,?)";
            connect = database.connectDb();
            try{
                Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
                alert.setTitle("Confirmación");
                alert.setHeaderText(null);
                alert.setContentText("¿Deseas confirmar la orden?");
                Optional<ButtonType> result = alert.showAndWait();

                if(result.get().equals(ButtonType.OK)){
                    java.util.Date date = new java.util.Date();
                    java.sql.Date sqlDate = new java.sql.Date(date.getTime());
                    prepared = connect.prepareStatement(insertSql);
                    prepared.setInt(1, cId);
                    prepared.setDouble(2, total);
                    prepared.setString(3, String.valueOf(sqlDate));
                    prepared.setString(4, DataName.username);
                    prepared.executeUpdate();
                    cId ++;

                    menuReceiptBtn();

                    menu_devuelvo.setText("$0.000");
                    menu_recibo.clear();
                    menu_total.setText("$0.000");
                    displayMenuOrder();


                }
            }catch (Exception e){e.printStackTrace();}
        }
    }

    //GET SELECTED ORDER
    Integer orderId = 0;
    public void getSelectedOrder(){
        ProductData prodData = menu_tableView.getSelectionModel().getSelectedItem();
        int index  = menu_tableView.getSelectionModel().getSelectedIndex();

        if (index < 0) return;

        orderId = prodData.getId();

    }

    //DELETE BTN
    public void menuDeleteBtn(){

        if(orderId == 0){
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Error");
            alert.setHeaderText(null);
            alert.setContentText("Selecciona un producto!");
            alert.showAndWait();
        }else{
            String sql = "DELETE FROM clientes WHERE id = '" + orderId + "'";
            connect = database.connectDb();
            try{
                Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
                alert.setTitle("Confirmación");
                alert.setHeaderText(null);
                alert.setContentText("¿Deseas eliminar este producto?");
                Optional<ButtonType> result = alert.showAndWait();


                if(result.get().equals(ButtonType.OK)){
                    prepared = connect.prepareStatement(sql);
                    prepared.executeUpdate();
                    displayMenuOrder();
                }
                orderId = 0;

            }catch (Exception e){e.printStackTrace();}
        }
    }

    //RECEIPT BTN
    public void menuReceiptBtn() {
        getCustomerId();
        HashMap<String, Object> map = new HashMap<>();
        map.put("getFactura", (cId - 1));

        try {
            connect = database.connectDb();

            // Cargar el archivo .jrxml desde el classpath
            InputStream inputStream = getClass().getResourceAsStream("/reports/xeivaFactura.jrxml");
            if (inputStream == null) {
                throw new IllegalArgumentException("Archivo .jrxml no encontrado en el classpath.");
            }

            // Cargar y compilar el diseño del informe
            JasperDesign jDesign = JRXmlLoader.load(inputStream);
            JasperReport jReport = JasperCompileManager.compileReport(jDesign);

            // Rellenar el informe
            JasperPrint jPrint = JasperFillManager.fillReport(jReport, map, connect);

            // Visualizar el informe
            JasperViewer jViewer = new JasperViewer(jPrint, false);
            jViewer.setVisible(true);

            // Cerrar la conexión
            connect.close();

        } catch (JRException | SQLException e) {
            e.printStackTrace();
        }
    }

    //MENU ACTUALIZAR BTN
    public void menuActualizarBtn(){
        menuGetTotal();
        menuSetTotal();
        displayMenuOrder();
    }

    //------------------------------------
    // CLIENTES FORM
    //------------------------------------
    @FXML
    private AnchorPane clientes_form;

    @FXML
    private TableColumn<CustomersData, String> clientes_col_admin;

    @FXML
    private TableColumn<CustomersData, Integer> clientes_col_clienteID;

    @FXML
    private TableColumn<CustomersData, String> clientes_col_fecha;

    @FXML
    private TableColumn<CustomersData, Integer> clientes_col_total;

    @FXML
    private TableView<CustomersData> clientes_tableView;

    //------------------------------------
    // METHODS
    //------------------------------------

    //GETS CLIENTES FROM DB
    public ObservableList<CustomersData> clientesGetData (){
        ObservableList<CustomersData> listClientes = FXCollections.observableArrayList();
        String sql = "SELECT * FROM factura";
        connect = database.connectDb();
        try{
            prepared = connect.prepareStatement(sql);
            result = prepared.executeQuery();

            while (result.next()){
                CustomersData customer = new CustomersData(result.getInt("cliente_id"),
                        result.getDouble("total"),
                        result.getString("fecha"),
                        result.getString("em_nbrUsuario"));
                listClientes.add(customer);
            }
        }catch (Exception e){e.printStackTrace();}
        return listClientes;
    }

    //SHOW CLIENTES
    public void showClientes() {

        ObservableList<CustomersData> listClientes = clientesGetData();
        clientes_col_clienteID.setCellValueFactory(new PropertyValueFactory<>("clienteId"));
        clientes_col_total.setCellValueFactory(new PropertyValueFactory<>("total"));
        clientes_col_fecha.setCellValueFactory(new PropertyValueFactory<>("fecha"));
        clientes_col_admin.setCellValueFactory(new PropertyValueFactory<>("emNbrUsuario"));
        clientes_tableView.setItems(listClientes);

    }

    //------------------------------------
    // BUTTON ACTIONS
    //------------------------------------

    //------------------------------------
    // STATS FORM
    //------------------------------------

    @FXML
    private Label stats_NumClient;

    @FXML
    private AreaChart<?, ?> stats_char_ingresos;

    @FXML
    private AreaChart<?, ?> stats_chart_clientes;

    @FXML
    private AnchorPane stats_form;

    @FXML
    private Label stats_ingresos;

    @FXML
    private Label stats_prodVend;

    @FXML
    private Label stats_totIngreso;

    //------------------------------------
    // METHODS
    //------------------------------------

    //STATS LABELS DATA
    //NUMBER OF CUSTOMERS
    public void setNumClientes(){
        java.util.Date date = new java.util.Date();
        java.sql.Date sqlDate = new java.sql.Date(date.getTime());
        String fechaHoy = String.valueOf(sqlDate);

        String sql = "SELECT COUNT(cliente_id) FROM factura WHERE fecha= '" + fechaHoy + "'";
        connect = database.connectDb();
        try{
            prepared = connect.prepareStatement(sql);
            result = prepared.executeQuery();
            if(result.next()){
                stats_NumClient.setText(result.getString("COUNT(cliente_id)"));
            }
            connect.close();
            prepared.close();
            result.close();

        }catch(Exception e){e.printStackTrace();}
    }
    //TODAY'S INCOMES
    public void setIngresosHoy(){
        java.util.Date date = new java.util.Date();
        java.sql.Date sqlDate = new java.sql.Date(date.getTime());
        String fechaHoy = String.valueOf(sqlDate);


        String sql = "SELECT SUM(total) FROM factura WHERE fecha= '" + fechaHoy + "'";
        connect = database.connectDb();
        try{
            prepared = connect.prepareStatement(sql);
            result = prepared.executeQuery();

            if(result.next()){
                stats_ingresos.setText(format.format(result.getInt("SUM(total)")));
            }else{
                stats_ingresos.setText("$0.000");
            }

            connect.close();
            prepared.close();
            result.close();
        }catch(Exception e){e.printStackTrace();}
    }

    //TOTAL INCOMES
    public void setTotIngresos(){
        String sql = "SELECT SUM(total) FROM factura";
        connect = database.connectDb();
        try{
            prepared = connect.prepareStatement(sql);
            result = prepared.executeQuery();

            if(result.next()){
                stats_totIngreso.setText(format.format(result.getInt("SUM(total)")));
            }

            connect.close();
            prepared.close();
            result.close();
        }catch(Exception e){e.printStackTrace();}
    }

    //TOTAL SOLD PRODUCTS
    public void setProdVendidos(){

        stats_char_ingresos.getData().clear();
        String sql = "SELECT SUM(cantidad) FROM clientes";
        connect = database.connectDb();
        try{
            prepared = connect.prepareStatement(sql);
            result = prepared.executeQuery();

            if(result.next() && result != null){
                stats_prodVend.setText(String.valueOf(result.getInt("SUM(cantidad)")));
            }else{
                stats_prodVend.setText("0");
            }

            connect.close();
            prepared.close();
            result.close();
        }catch(Exception e){e.printStackTrace();}
    }

    //INGRESOS CHART
    public void showIngresosChart(){
        stats_char_ingresos.getData().clear();

        String sql = "SELECT fecha, SUM(total) FROM factura GROUP BY fecha ORDER BY fecha";
        connect = database.connectDb();
        XYChart.Series chart = new XYChart.Series();

        try {
            prepared = connect.prepareStatement(sql);
            result = prepared.executeQuery();
            while (result.next()) {
                chart.getData().add(new XYChart.Data(result.getString(1), result.getInt(2)));
            }
            stats_char_ingresos.getData().add(chart);

            connect.close();
            prepared.close();
            result.close();
        }catch (Exception e) {e.printStackTrace();}
    }

    //CLIENTES CHART
    public void showProdVendChart(){
        stats_chart_clientes.getData().clear();

        String sql = "SELECT fecha, COUNT(cliente_id) FROM factura GROUP BY fecha ORDER BY fecha";
        connect = database.connectDb();
        XYChart.Series chart = new XYChart.Series();

        try {
            prepared = connect.prepareStatement(sql);
            result = prepared.executeQuery();
            while (result.next()) {
                chart.getData().add(new XYChart.Data(result.getString(1), result.getInt(2)));
            }
            stats_chart_clientes.getData().add(chart);

            connect.close();
            prepared.close();
            result.close();
        }catch (Exception e) {e.printStackTrace();}
    }

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        displayUsername();
        showComboTipo();
        showComboEstado();
        showProdData();
        menuDisplayCard();
        getCustomerId();
        displayMenuOrder();
        menuSetTotal();
        showClientes();
        showIngresosChart();
        showProdVendChart();
    }
}

