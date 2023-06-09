module com.example.btl_demo {
    exports Model;
    requires javafx.controls;
    requires javafx.fxml;

    requires org.controlsfx.controls;
    requires com.dlsc.formsfx;
    requires org.kordamp.bootstrapfx.core;
    requires java.sql;
    requires mysql.connector.j;
    requires java.desktop;
    requires com.google.zxing;
    requires com.google.protobuf;
    requires com.google.zxing.javase;
    requires javafx.media;
    requires org.bytedeco.javacpp;
    requires org.bytedeco.javacv;
    requires org.bytedeco.opencv;
    requires jasperreports;
    requires java.mail;
    requires itextpdf;
    requires org.apache.commons.collections4;
    requires pdfbox;
    requires javafx.swing;
    opens Controller to javafx.fxml;
    exports Controller;
    opens Model to javafx.fxml;
}