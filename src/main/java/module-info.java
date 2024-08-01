module company.appxeiva {
    requires jdk.compiler;
    requires javafx.base;
    requires javafx.controls;
    requires java.sql;
    requires javafx.fxml;
    requires jasperreports;
    requires jBCrypt;
    requires org.slf4j;


    opens company.appxeiva to javafx.fxml;
    exports company.appxeiva;
}