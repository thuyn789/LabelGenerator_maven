module com.customapp.labelgenerator {
    requires javafx.controls;
    requires javafx.fxml;

    requires com.dlsc.formsfx;

    requires kernel;
    requires io;
    requires layout;

    opens com.customapp.labelgenerator to javafx.fxml;
    exports com.customapp.labelgenerator;
}