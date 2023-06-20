package org.birritteri.main;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import javafx.scene.text.TextFlow;
import javafx.stage.Stage;
import org.birritteri.server.Server;
import org.birritteri.server.ServerException;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.URL;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.ResourceBundle;

public class ServerController implements Initializable {
    private Server server;
    @FXML
    public ScrollPane logArea;
    @FXML
    public VBox vboxMessages;

    private ArrayList<String> loadFile() {
        File file = new File("C:\\Users\\Utente\\Desktop\\mailserver-master\\mailserver-master\\src\\main\\java\\org\\birritteri\\server\\files\\email_addresses.txt");
        String filePath = file.getAbsolutePath();
        Charset ENCODING = StandardCharsets.UTF_8;
        Path inputFilePath = Paths.get(filePath);

        ArrayList<String> emailListFromText = null;
        try (BufferedReader fileInputReader = Files.newBufferedReader(inputFilePath, ENCODING)) {
            String line;
            emailListFromText = new ArrayList<>();
            while ((line = fileInputReader.readLine()) != null)
                emailListFromText.add(line);
        } catch (IOException exception) {
            System.err.println("MyIOException: " + exception);
        }

        return emailListFromText;
    }

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        try {
            ArrayList<String> emailList = loadFile();
            if (emailList == null)
                throw new ServerException("nessuna email trovata.");

            ServerSocket serverSocket = new ServerSocket(60000);
            serverSocket.setReuseAddress(true);
            server = new Server(emailList, serverSocket);

        } catch (ServerException | IOException e) {
            e.printStackTrace();
        }

        vboxMessages.heightProperty().addListener((observableValue, oldValue, newValue) -> logArea.setVvalue((Double) newValue));

        server.clientLogin(vboxMessages);
    }

    public void onShutdownButtonClick(){
        addLog("Server shutdown...", vboxMessages);
        //Thread.sleep(5000);
        if(server.shutdown()){
            Stage serverStage = (Stage) logArea.getScene().getWindow();
            serverStage.close();
        }
    }

    public static void addLog(String log, VBox vbox) {
        LocalDateTime dateTime = LocalDateTime.now();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm:ss");
        String date = dateTime.format(formatter);
        log = "[" + date + "] " + log;

        HBox boxMessage = new HBox();
        boxMessage.setAlignment(Pos.CENTER_LEFT);
        boxMessage.setPadding(new Insets(1, 1, 1, 5));

        Text text = new Text(log);
        TextFlow textFlow = new TextFlow(text);
        textFlow.setPadding(new Insets(5, 10, 5, 10));

        boxMessage.getChildren().add(textFlow);

        Platform.runLater(() -> vbox.getChildren().add(boxMessage));
    }
}
