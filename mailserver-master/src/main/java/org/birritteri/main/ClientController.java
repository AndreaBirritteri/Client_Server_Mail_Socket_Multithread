package org.birritteri.main;

import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Cursor;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import javafx.scene.text.TextAlignment;
import javafx.scene.text.TextFlow;
import javafx.stage.Stage;
import org.birritteri.client.Client;
import org.birritteri.client.ClientException;
import org.birritteri.mail.Email;
import org.birritteri.server.Server;

import java.io.IOException;
import java.net.URL;
import java.util.*;

import static org.birritteri.mail.Email.emailValidation;

public class ClientController implements Initializable {
    @FXML
    public Label emailError;
    @FXML
    public TextField sendTo, object;
    @FXML
    public TextArea body;
    @FXML
    private VBox vBoxEmails;

    private Client client;

    /*
        TODO
            - action bottoni inbox
     */

    @FXML
    public void onWriteButtonClick() {
        System.out.println("[" + this.client.getEmailAddress() + "] scrivo...");
        FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("message.fxml"));
        Stage newMessage = new Stage();
        try {
            newMessage.setScene(new Scene(fxmlLoader.load()));
            newMessage.centerOnScreen();
            newMessage.setTitle("Nuovo messaggio - " + this.client.getEmailAddress());
            newMessage.setUserData(vBoxEmails);
            newMessage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @FXML
    public void onSendButtonClick() throws ClientException {
        boolean b = true;
        String[] s;
        if (!sendTo.getText().isEmpty()) {
            String text = sendTo.getText().trim();
            s = text.split(";");
            for (int i = 0; i < s.length; i++) {
                s[i] = s[i].trim();
                if (emailValidation(s[i])) {
                    b = false;
                }
            }
        }


        if (sendTo.getText().isEmpty() || !b || Objects.equals(sendTo.getText(), client.getEmailAddress())) {
            if (!emailError.isVisible())
                emailError.setVisible(true);
        } else {
            ArrayList<String> addressees = new ArrayList<>(Arrays.asList(sendTo.getText().split("; |;")));
            Email email = this.client.newEmail(this.client.getEmailAddress(), addressees, object.getText(), body.getText());
            boolean sent = this.client.sendToServer(email);
            System.out.println("[" + this.client.getEmailAddress() + "] " + (sent ? "email spedita." : "email non spedita."));

            if (sent) {
                Stage newMessage = (Stage) sendTo.getScene().getWindow();
                addEmail(email, (VBox) newMessage.getUserData());
                client.addEmail(email);
                newMessage.close();
            } else if (!emailError.isVisible())
                emailError.setVisible(true);
        }
    }

    @FXML
    public void onSendFromReply(TextField sendTo, TextField object, TextArea body, Label emailError, VBox vBoxEmails) throws ClientException {

            ArrayList<String> addressees = new ArrayList<>(Arrays.asList(sendTo.getText().split("; |;")));
            Email email = this.client.newEmail(this.client.getEmailAddress(), addressees, object.getText(), body.getText());
            boolean sent = this.client.sendToServer(email);
            System.out.println("[" + this.client.getEmailAddress() + "] " + (sent ? "email spedita." : "email non spedita."));

            if (sent) {
                addEmail(email, vBoxEmails);
                client.addEmail(email);
            } else if (!emailError.isVisible())
                emailError.setVisible(true);

    }

    private synchronized void addEmail(Email email, VBox vBoxEmails) throws ClientException {
        //load email view
        FXMLLoader fxmlEmail = new FXMLLoader(getClass().getResource("email.fxml"));
        Stage openEmail = new Stage();
        try {
            openEmail.setScene(new Scene(fxmlEmail.load()));

            EmailController emailController = fxmlEmail.getController();
            emailController.loadFields(email);
            emailController.setClientController(this);

            openEmail.centerOnScreen();
            openEmail.setUserData(vBoxEmails);
            if (email.getObject().isBlank())
                openEmail.setTitle("Email");
            else
                openEmail.setTitle("Email - " + email.getObject());
        } catch (IOException e) {
            e.printStackTrace();
        }

        FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("reply.fxml"));
        Stage replyStage = new Stage();
        ReplyController replyController = null;
        try {
            replyStage.setScene(new Scene(fxmlLoader.load()));
            replyController = fxmlLoader.getController();
            replyController.setClientController(this);
            replyStage.centerOnScreen();
            replyStage.setUserData(vBoxEmails);
        } catch (IOException e) {
            e.printStackTrace();
        }

        if (replyController == null || email == null)
            throw new ClientException("Controller is null.");

        ReplyController finalReplyController = replyController;
        Button reply, replyAll, forward, delete;

        HBox emailHBox = new HBox();
        emailHBox.setAlignment(Pos.CENTER_RIGHT);
        emailHBox.setCursor(Cursor.HAND);
        emailHBox.setOnMouseClicked(mouseEvent -> openEmail.show());
        emailHBox.setSpacing(10);
        emailHBox.setStyle("-fx-border-style: solid inside;"
                + "-fx-border-width: 1;" + "-fx-border-radius: 5;"
                + "-fx-border-color: black;" + "-fx-border-insets: 1;");

        Text emailText = new Text(email.emailFormatted(true));
        TextFlow textFlow = new TextFlow(emailText);
        textFlow.setPadding(new Insets(5, 10, 5, 10));
        textFlow.setPrefWidth(770);

        reply = newButton(new ImageView(Objects.requireNonNull(getClass().getResource("images/reply.png")).toExternalForm()));
        reply.setOnAction(actionEvent -> {
            finalReplyController.loadReplyFields(email);
            replyStage.setTitle("Reply");
            replyStage.show();
        });

        replyAll = newButton(new ImageView(Objects.requireNonNull(getClass().getResource("images/replyAll.png")).toExternalForm()));
        replyAll.setOnAction(actionEvent -> {
            finalReplyController.loadReplyAllFields(email, client.getEmailAddress());
            replyStage.setTitle("Reply all");
            replyStage.show();
        });

        forward = newButton(new ImageView(Objects.requireNonNull(getClass().getResource("images/forward.png")).toExternalForm()));
        forward.setOnAction(actionEvent -> {
            finalReplyController.loadForwardFields(email);
            replyStage.setTitle("Forward");
            replyStage.show();
        });

        delete = newButton(new ImageView(Objects.requireNonNull(getClass().getResource("images/bin.png")).toExternalForm()));
        delete.setOnAction(new EventHandler<>() {
            private final HBox hBox = emailHBox;

            @Override
            public void handle(ActionEvent actionEvent) {
                vBoxEmails.getChildren().remove(hBox);
                if (!client.removeFromServer(email.getId()))
                    try {
                        throw new ClientException("cannot remove email.");
                    } catch (ClientException e) {
                        e.printStackTrace();
                    }

                client.rmvEmail(email);
            }
        });

        emailHBox.getChildren().add(textFlow);
        emailHBox.getChildren().add(reply);
        emailHBox.getChildren().add(replyAll);
        emailHBox.getChildren().add(forward);
        emailHBox.getChildren().add(delete);

        if(vBoxEmails!= null)
            vBoxEmails.getChildren().add(0, emailHBox);
    }

    public Client getClient() {
        return this.client;
    }

    private Button newButton(ImageView imageView) {
        imageView.setFitHeight(30);
        imageView.setPreserveRatio(true);

        Button button = new Button("");
        button.setAlignment(Pos.CENTER_RIGHT);
        button.setTextAlignment(TextAlignment.CENTER);
        button.setPrefSize(45, 45);
        button.setCursor(Cursor.HAND);
        button.setGraphic(imageView);

        return button;
    }

    public synchronized void updateInbox() {
        ArrayList<Email> emailList = client.getEmailList();
        if (vBoxEmails != null)
            vBoxEmails.getChildren().clear();

        for (Email email :
                emailList) {

            //System.out.println(email);
            try {
                addEmail(email, vBoxEmails);
            } catch (ClientException e) {
                throw new RuntimeException(e);
            }

        }
    }

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        this.client = LoginController.getClient();
        for (Email email :
                client.getEmailList()) {
            try {
                addEmail(email, vBoxEmails);
            } catch (ClientException | NullPointerException ignored) {
            }
        }
        Timer timer = new Timer();
        timer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                Platform.runLater(() -> {
                    updateInbox();
                    // Update the state of the FXML file here
                });
            }
        }, 0, 3000);
    }
    }


