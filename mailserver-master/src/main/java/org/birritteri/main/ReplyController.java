package org.birritteri.main;


import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import org.birritteri.client.ClientException;
import org.birritteri.mail.Email;

import java.util.ArrayList;
import java.util.Objects;

import static org.birritteri.mail.Email.emailValidation;

public class ReplyController {
    public TextField sendTo;
    public TextField object;
    public TextArea body;
    public Label emailError;

    private ClientController clientController;

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


        if (sendTo.getText().isEmpty() || !b ) {
            if (!emailError.isVisible())
                emailError.setVisible(true);
        }else {
            Stage replyStage = (Stage) sendTo.getScene().getWindow();
            clientController.onSendFromReply(sendTo, object, body, emailError, (VBox) replyStage.getUserData());
            replyStage.close();
        }
    }

    public void loadReplyFields(Email email){
        sendTo.setText(email.getSender());
        sendTo.setEditable(false);
        object.setText("Re:"+email.getObject());
        object.setEditable(false);
        body.setText("\n\n--From:"+ email.getSender() +"--\n"+email.getBody());
    }

    public void loadReplyAllFields(Email email, String clientEmailAddress){
        ArrayList<String> addresses = email.getAddresses();

        if(addresses.size() > 1)
            if(email.getSender().equals(clientEmailAddress))
                sendTo.setText(email.printAddresses(clientEmailAddress));
            else
                sendTo.setText(email.getSender() + "; " + email.printAddresses(clientEmailAddress));
        else
            sendTo.setText(email.getSender());

        sendTo.setEditable(false);
        object.setText("ReAll:"+email.getObject());
        object.setEditable(false);
        body.setText("\n\n--From:"+ email.getSender() +"--\n"+email.getBody());
    }

    public void loadForwardFields(Email email){
        sendTo.setText("");
        sendTo.setEditable(true);
        object.setText("Fwd:"+email.getObject());
        object.setEditable(false);
        body.setText("\n\n--From:"+ email.getSender() +"--\n"+email.getBody());
    }



    public Stage getStage(){
        return (Stage) sendTo.getScene().getWindow();
    }

    public void setClientController(ClientController clientController) {
        this.clientController = clientController;
    }
}
