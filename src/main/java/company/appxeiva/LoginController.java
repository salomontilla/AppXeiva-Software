package company.appxeiva;

import javafx.animation.TranslateTransition;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Stage;
import javafx.util.Duration;
import org.mindrot.jbcrypt.BCrypt;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Objects;

public class LoginController {
    //------------------------------------
    //DATABASE CONNECTION
    //------------------------------------

    private Connection connect;
    private PreparedStatement prepared;
    private ResultSet result;

    // HASH
    public static String hashPassword (String pass){
        return BCrypt.hashpw(pass, BCrypt.gensalt());
    }

    public static Boolean checkPassword (String normalPass, String hashPass){
        return BCrypt.checkpw(normalPass, hashPass);
    }

    //LOGGER
    private static final Logger logger = LoggerFactory.getLogger("logback.xml");
    public void verlog (){
        logger.debug("creando clase");
    }

    //------------------------------------
    // Left Side
    //------------------------------------

    //CONTROLS
    //------------------------------------
    @FXML
    private AnchorPane ls_regiForm;

    @FXML
    private Button ls_btnRegistrar;

    @FXML
    private PasswordField ls_contra;

    @FXML
    private CheckBox ls_checkBoxContra;

    @FXML
    private TextField ls_contraVisible;

    @FXML
    private TextField ls_nbrUsuario;

    @FXML
    private ComboBox<?> ls_preguntaCombo;
    //creating data of comboBox
    private String listaPreguntas[] = {
            "Cuál es tu color favorito?", "Cuál es tu comida favorita?", "Cuál es tu fecha de nacimiento?"
    };
    public void regLlistaPreguntas(){
        List<String> preguntasList = new ArrayList<>();
        for(String dato : listaPreguntas){
            preguntasList.add(dato);
        }
        ObservableList listData = FXCollections.observableArrayList(preguntasList);
        ls_preguntaCombo.setItems(listData);
    }

    @FXML
    private TextField ls_respuesta;
    //------------------------------------
    // LEFT SIDE DB-QUERIES
    //------------------------------------
    //Fill register
    @FXML
    public void regist (){
        if (ls_contra.getText().isEmpty() || ls_nbrUsuario.getText().isEmpty()|| ls_respuesta.getText().isEmpty()
                || ls_preguntaCombo.getSelectionModel().getSelectedItem() == null) {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Campos Vacios");
            alert.setHeaderText("Campos Vacios");
            alert.setContentText("Debes llenar los campos!");
            alert.showAndWait();
        }else{
            String regData = ("INSERT INTO empleados (nbrUsuario, contra, pregunta, respuesta,fecha) " +
                    "VALUES (?,?,?,?,?);");
            connect = database.connectDb();

            //CHECKING IF THE USERNAME IS ALREADY CREATED
            try {
                //query
                String checkUserName = ("SELECT nbrUsuario FROM empleados WHERE nbrUsuario ='" + ls_nbrUsuario.getText() + "'");
                prepared = connect.prepareStatement(checkUserName);
                result = prepared.executeQuery();
                if(result.next()){
                    //show invalid name
                    Alert alert = new Alert(Alert.AlertType.ERROR);
                    alert.setTitle("Error");
                    alert.setHeaderText("Nombre no válido");
                    alert.setContentText("El nombre de usuario '" + ls_nbrUsuario.getText() + "', ya existe");
                    alert.showAndWait();
                }else if(ls_contra.getText().length()<8){
                    //Show invalid password
                    Alert alert = new Alert(Alert.AlertType.ERROR);
                    alert.setTitle("Error");
                    alert.setHeaderText("Contraseña no válida");
                    alert.setContentText("La contraseña debe tener minimo 8 caracteres!");
                    alert.showAndWait();
                }else{
                    //Insert data
                    prepared = connect.prepareStatement(regData);
                    prepared.setString(1, ls_nbrUsuario.getText());
                    prepared.setString(2, hashPassword(ls_contra.getText())); //HASH
                    prepared.setString(3, (String)ls_preguntaCombo.getSelectionModel().getSelectedItem());
                    prepared.setString(4, ls_respuesta.getText());

                    Date date = new Date();
                    java.sql.Date sqlDate = new java.sql.Date(date.getTime());
                    prepared.setString(5, String.valueOf(sqlDate));
                    prepared.executeUpdate();

                    //alert info
                    Alert alert = new Alert(Alert.AlertType.INFORMATION);
                    alert.setTitle("Registro");
                    alert.setHeaderText("Registro");
                    alert.setContentText("Registrado exitosamente");
                    alert.showAndWait();

                    //default info
                    ls_nbrUsuario.setText("");
                    ls_contra.setText("");
                    ls_preguntaCombo.getSelectionModel().clearSelection();
                    ls_respuesta.setText("");

                    //transition
                    TranslateTransition slider = new TranslateTransition();
                    slider.setNode(fls_crearForm);
                    slider.setToX(0);
                    slider.setDuration(Duration.seconds(.5));
                    slider.setOnFinished((ActionEvent e) -> {
                        fls_btnYaTienes.setVisible(true);
                        fls_btnCrear.setVisible(false);
                        regLlistaPreguntas();});
                    slider.play();
                }
                connect.close();
                result.close();
            } catch (Exception e){e.printStackTrace();}
        }
    }

    public void ls_verContra(){
        if (ls_checkBoxContra.isSelected()){
            ls_contraVisible.textProperty().bindBidirectional(ls_contra.textProperty());
            ls_contraVisible.setVisible(true);
            ls_contra.setVisible(false);
        }else{
            ls_contra.setVisible(true);
            ls_contraVisible.setVisible(false);
        }
    }

    //------------------------------------
    //Front Left side
    //------------------------------------
    @FXML
    private Button fls_btnYaTienes;
    @FXML
    private AnchorPane fls_crearForm;
    @FXML
    private Button fls_btnCrear;
    //------------------------------------
    //FLS METHODS
    //------------------------------------
    //animation
    @FXML
    public void slide (ActionEvent event) throws IOException {
        TranslateTransition slider = new TranslateTransition();
        if(event.getSource() == fls_btnCrear) {
            slider.setNode(fls_crearForm);
            slider.setToX(300);
            slider.setDuration(Duration.seconds(.5));


            slider.setOnFinished((ActionEvent e) -> {
                fls_btnYaTienes.setVisible(true);
                fls_btnCrear.setVisible(false);
                regLlistaPreguntas();
            });
            slider.play();
        }
        if (event.getSource() == fls_btnYaTienes) {
            slider.setNode(fls_crearForm);
            fls_btnYaTienes.setVisible(false);
            fls_btnCrear.setVisible(true);
            slider.setDuration(Duration.seconds(.5));
            slider.setToX(0);
            ls_preguntaCombo.getSelectionModel().clearSelection();
            slider.play();
        }
    }


    //------------------------------------
    //Right Side
    //------------------------------------
    //CONTROLS
    //------------------------------------
    @FXML
    private AnchorPane rs_iniciarForm;

    @FXML
    private Button rs_btnIniciar;

    @FXML
    private PasswordField rs_contra;

    @FXML
    private TextField rs_nbrUsuario;

    @FXML
    private Hyperlink rs_olvideLink;
    @FXML
    private CheckBox rs_mostrarCheckBox;
    @FXML
    private TextField rs_contraVer;

    //------------------------------------
    //RIGHT SIDE METHODS
    @FXML
    public void changeToOlvide(){
        frs_formOlvide.setVisible(true);
        cargarPreguntas();
        rs_nbrUsuario.setText("");
        rs_contra.setText("");

    }

    public void rs_verContra(){
        if (rs_mostrarCheckBox.isSelected()){
            rs_contraVer.textProperty().bindBidirectional(rs_contra.textProperty());
            rs_contraVer.setVisible(true);
            rs_contra.setVisible(false);
        }else{
            rs_contra.setVisible(true);
            rs_contraVer.setVisible(false);
        }
    }
    //------------------------------------
    //RIGHT SIDE DB-QUERIES
    //------------------------------------
    //Fill login form
    @FXML
    public void loginbtn(){
        if(rs_nbrUsuario.getText().isEmpty() || rs_contra.getText().isEmpty()){
            camposVaciosAlert();
        }else{
            try {
                connect = database.connectDb();

                prepared = connect.prepareStatement("SELECT contra FROM empleados where nbrUsuario = ?");
                prepared.setString(1, rs_nbrUsuario.getText());
                result = prepared.executeQuery();

                String hashedContra = null;
                boolean isPwChecked = false;
                if(result.next()){
                    hashedContra = result.getString("contra");
                    isPwChecked = checkPassword(rs_contra.getText(), hashedContra);

                    //IF SUCCESSFULLY LOGIN THEN OPEN NEW MAIN SCREEN FORM
                    if (isPwChecked) {
                        //TO GET USERNAME
                        DataName.username = rs_nbrUsuario.getText();


                        Alert alert = new Alert(Alert.AlertType.INFORMATION);
                        alert.setTitle("Inico de sesión");
                        alert.setHeaderText("Inicio de sesion exitoso");
                        alert.setContentText("Has iniciado sesión!");
                        alert.showAndWait();

                        logger.info("cuenta iniciada exitosamente");

                        Parent Root = FXMLLoader.load(Objects.requireNonNull(getClass().getResource("mainScreen.fxml")));
                        Scene scene = new Scene(Root);
                        Stage stage = new Stage();
                        stage.setMinHeight(600);
                        stage.setMinWidth(1100);
                        stage.setScene(scene);
                        stage.show();

                        rs_btnIniciar.getScene().getWindow().hide();

                    }else{
                        Alert alert = new Alert(Alert.AlertType.ERROR);
                        alert.setTitle("Inico de sesión");
                        alert.setHeaderText(null);
                        alert.setContentText("Usuario y/o contraseña incorrectos");
                        alert.showAndWait();
                    }

                }else{
                    Alert alert = new Alert(Alert.AlertType.ERROR);
                    alert.setTitle("Inico de sesión");
                    alert.setHeaderText("No se pudo inciar sesion");
                    alert.setContentText("Usuario y/o contraseña incorrectos");
                    alert.showAndWait();
                }

                connect.close();
                prepared.close();
                result.close();


            }catch (Exception e){e.printStackTrace();}

        }
    }
    //------------------------------------
    //Front Right Side
    //------------------------------------
    //CONTROLS
    //------------------------------------
    @FXML
    private Button frs_btnAtrás;

    @FXML
    private Button frs_btnSiguiente;

    @FXML
    private AnchorPane frs_formOlvide;

    @FXML
    private TextField frs_nbrUsuario;

    @FXML
    private ComboBox<?> frs_preguntaCombo;
    //upload of ComboBox
    public void cargarPreguntas(){
        List <String> preguntasArr = new ArrayList<>();
        for(String dato: listaPreguntas){
            preguntasArr.add(dato);
        }
        ObservableList preguntasObs = FXCollections.observableArrayList(preguntasArr);
        frs_preguntaCombo.setItems(preguntasObs);
    }
    @FXML
    private TextField frs_respuesta;
    //------------------------------------
    //FRS METHODS
    @FXML
    public void changeToCambiarForm(){
        f2rs_formCambiar.setVisible(true);


    }
    @FXML
    public void backToInicio(){
        frs_formOlvide.setVisible(false);
        clearOlvideForm();
    }
    //------------------------------------
    //FRS DB-QUERIES
    @FXML
    //Validation of data in FRS form
    public void frs_btnSiguienteVal(){
        if(frs_preguntaCombo.getSelectionModel().getSelectedItem() == null || frs_respuesta.getText().isEmpty() ||
            frs_nbrUsuario.getText().isEmpty()){
            camposVaciosAlert();
        }else{
            try {
                connect = database.connectDb();
                String cambioContra = "SELECT nbrUsuario, pregunta, respuesta FROM empleados WHERE nbrUsuario = ? " +
                                        " and pregunta = ? and respuesta = ?";
                prepared = connect.prepareStatement(cambioContra);
                prepared.setString(1, frs_nbrUsuario.getText().toLowerCase());
                prepared.setString(2, (String)frs_preguntaCombo.getSelectionModel().getSelectedItem());
                prepared.setString(3, frs_respuesta.getText().toLowerCase());
                result = prepared.executeQuery();
                if (result.next()) {
                    changeToCambiarForm();
                }else{
                        Alert alert = new Alert(Alert.AlertType.ERROR);
                        alert.setTitle("Datos incorrectos");
                        alert.setHeaderText(null);
                        alert.setContentText("Los datos no coinciden");
                        alert.showAndWait();
                }
                connect.close();
                result.close();
            }catch (Exception e){e.printStackTrace();}
        }
    }
    //------------------------------------
    //Front 2 Right Side
    //------------------------------------
    //CONTROLS
    @FXML
    private Button f2rs_btnAtras;

    @FXML
    private Button f2rs_btnCambiar;

    @FXML
    private PasswordField f2rs_confiContra;

    @FXML
    private AnchorPane f2rs_formCambiar;

    @FXML
    private TextField f2rs_nuevaContra;

    @FXML
    private TextField f2rs_confiContraVer;
    @FXML
    private CheckBox f2rs_checkMostrar;
    //------------------------------------
    //F2RS METHODS
    @FXML
    public void backToOlvide(){
        f2rs_formCambiar.setVisible(false);
        clearOlvideForm();
    }
    @FXML
    public void f2rs_verContra(){
        if (f2rs_checkMostrar.isSelected()){
            f2rs_confiContraVer.textProperty().bindBidirectional(f2rs_confiContra.textProperty());
            f2rs_confiContraVer.setVisible(true);
            f2rs_confiContra.setVisible(false);
        }else{
            f2rs_confiContra.setVisible(true);
            f2rs_confiContraVer.setVisible(false);
        }
    }
    //------------------------------------
    //F2RS DB-QUERIES
    //Changing password
    @FXML
    public void changeContra(){
        if (f2rs_nuevaContra.getText().isEmpty() || f2rs_confiContra.getText().isEmpty()){

            camposVaciosAlert();

        }else if(f2rs_nuevaContra.getText().equals(f2rs_confiContra.getText()) && f2rs_nuevaContra.getText().length()>7){
            //update dates

            connect = database.connectDb();

            try{
                prepared = connect.prepareStatement("Select fecha from empleados where nbrUsuario = ?");
                prepared.setString(1, frs_nbrUsuario.getText());
                result = prepared.executeQuery();
                String fecha = "";
                if (result.next()) {
                    fecha = result.getString("fecha");
                }

                String updateContra = "update empleados set contra=?, pregunta = ?,respuesta =?, fecha = ? where nbrUsuario = ?";
                prepared = connect.prepareStatement(updateContra);
                prepared.setString(1, hashPassword(f2rs_nuevaContra.getText()));
                prepared.setString(2, frs_preguntaCombo.getSelectionModel().getSelectedItem().toString());
                prepared.setString(3, frs_respuesta.getText());
                prepared.setString(4, fecha);
                prepared.setString(5, frs_nbrUsuario.getText());
                prepared.executeUpdate();

                //successfully updated alert
                Alert alert = new Alert(Alert.AlertType.INFORMATION);
                alert.setTitle("Cambiar Contraseña");
                alert.setHeaderText(null);
                alert.setContentText("Has cambiado tu contraseña!");
                alert.showAndWait();
                //clear
                f2rs_formCambiar.setVisible(false);
                frs_formOlvide.setVisible(false);
                f2rs_confiContra.setText("");
                f2rs_confiContraVer.setText("");
                f2rs_nuevaContra.setText("");
                connect.close();
                result.close();
            }catch (Exception e){e.printStackTrace();}


        } else{
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Error");
            alert.setHeaderText(null);
            alert.setContentText("Las contraseñas no coinciden o tienen menos de 8 caracteres!");
            alert.showAndWait();
        }



    }
    //------------------------------------
    //CLASS METHODS

    public void camposVaciosAlert() {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Campos vacíos");
        alert.setHeaderText("Campos vacíos");
        alert.setContentText("Debes llenar los campos vacios");
        alert.showAndWait();
    }
    public void clearOlvideForm() {
        frs_nbrUsuario.setText("");
        frs_respuesta.setText("");
        frs_preguntaCombo.getSelectionModel().clearSelection();
    }



}