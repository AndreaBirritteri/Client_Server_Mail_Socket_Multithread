package org.birritteri.main;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.TextField;
import javafx.scene.text.Text;
import javafx.stage.Stage;
import org.birritteri.client.Client;
import org.birritteri.mail.Email;

import java.io.IOException;
import java.net.Socket;

public class LoginController {
    @FXML
    public Text loginError;
    @FXML
    private TextField username;

    private static Client client;


    public void onLoginButtonClick(){
        String emailAddress = username.getText();
        if (Email.emailValidation(emailAddress)) { //email format error
            System.out.println("[" + emailAddress + "] formato email errato.");
            if(!loginError.isVisible())
                loginError.setVisible(true);
        } else {
            try {
                client = new Client(emailAddress, new Socket("localhost", 60000));
                if (client.tryLogin()) {
                    //recupero email tramite metodo
                    System.out.println("[" + client.getEmailAddress() + "] connesso!");
                    client.createUpdateInboxThread();
                    inboxFxmlLoader();
                } else { //server ritorna che non apparteniamo al dominio
                    System.out.println("[" + client.getEmailAddress() + "] email errata.");
                    if(!loginError.isVisible())
                        loginError.setVisible(true);
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public void onRegisterButtonClick() {
        String emailAddress = username.getText();
        if (Email.emailValidation(emailAddress)) { //formato email errato
            System.out.println("[" + emailAddress + "] formato email errato.");
            if(!loginError.isVisible())
                loginError.setVisible(true);
        } else {
            try {
                client = new Client(emailAddress, new Socket("localhost", 60000));
                if (client.signIn()) {
                    System.out.println("[" + client.getEmailAddress() + "] connesso!");
                    inboxFxmlLoader();
                } else { //server ritorna che non apparteniamo al dominio
                    System.out.println("[" + username.getText() + "] email errata.");

                    if(!loginError.isVisible())
                        loginError.setVisible(true);
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private void inboxFxmlLoader() {
        FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("inbox.fxml"));
        Stage stage = (Stage) username.getScene().getWindow();

        try {
            stage.setScene(new Scene(fxmlLoader.load()));
        } catch (IOException e) {
            e.printStackTrace();
        }

        stage.centerOnScreen();
        stage.setTitle("Inbox - " + client.getEmailAddress());


    }

    protected static Client getClient() {
        return client;
    }
}
