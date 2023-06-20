package org.birritteri.main;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import org.birritteri.client.ClientException;
import org.birritteri.mail.Email;
import org.birritteri.server.Server;

import java.io.IOException;

public class EmailController {
        @FXML
        public TextArea bodyReader;
        @FXML
        public TextField objReader, sendToReader, fromReader;

        private ClientController clientController;
        private Email email;

        public void loadFields(Email email) {
                bodyReader.setText(email.getBody());
                bodyReader.setEditable(false);

                objReader.setText(email.getObject());
                objReader.setEditable(false);

                sendToReader.setText(email.printAddresses());
                sendToReader.setEditable(false);

                fromReader.setText(email.getSender());
                fromReader.setEditable(false);

                this.email = email;
        }

        public void onReplyClick() {
                Stage thisStage = (Stage) bodyReader.getScene().getWindow();
                ReplyController replyController = fxmlLoadReply();
                Stage replyStage = replyController.getStage();

                replyController.loadReplyFields(this.email);
                replyStage.setTitle("Reply");
                replyStage.show();
                thisStage.close();
        }

        public void onReplyAllClick() {
                Stage thisStage = (Stage) bodyReader.getScene().getWindow();
                ReplyController replyController = fxmlLoadReply();
                Stage replyStage = replyController.getStage();

                replyController.loadReplyAllFields(this.email, clientController.getClient().getEmailAddress());
                replyStage.setTitle("ReplyAll");
                replyStage.show();
                thisStage.close();
        }

        public void onForwardClick() {
                Stage thisStage = (Stage) bodyReader.getScene().getWindow();
                ReplyController replyController = fxmlLoadReply();
                Stage replyStage = replyController.getStage();

                replyController.loadForwardFields(this.email);
                replyStage.setTitle("Forward");
                replyStage.show();
                thisStage.close();
        }

        public void onDeleteClick() throws IOException {
                Stage thisStage = (Stage) bodyReader.getScene().getWindow();
                if(!clientController.getClient().removeFromServer(email.getId()))
                        try {
                                throw new ClientException("cannot remove email.");
                        } catch (ClientException e) {
                                e.printStackTrace();
                        }

                clientController.getClient().rmvEmail(email);
                clientController.updateInbox();
                thisStage.close();
        }

        private ReplyController fxmlLoadReply(){
                Stage thisStage = (Stage) bodyReader.getScene().getWindow();
                FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("reply.fxml"));
                Stage replyStage = new Stage();
                ReplyController replyController = null;
                try {
                        replyStage.setScene(new Scene(fxmlLoader.load()));
                        replyController = fxmlLoader.getController();
                        replyController.setClientController(clientController);
                        replyStage.centerOnScreen();
                        replyStage.setUserData(thisStage.getUserData());
                } catch (IOException e) {
                        e.printStackTrace();
                }

                return replyController;
        }

        public void setClientController(ClientController clientController) {
                this.clientController = clientController;
        }

}
