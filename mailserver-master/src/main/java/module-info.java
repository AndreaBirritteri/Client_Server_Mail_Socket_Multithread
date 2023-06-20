module org.birrritteri.mailserver {
    requires javafx.controls;
    requires javafx.fxml;

    exports org.birritteri.main;
    opens org.birritteri.main to javafx.fxml;
}