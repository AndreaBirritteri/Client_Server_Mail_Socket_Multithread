package org.birritteri.mail;

import java.util.ArrayList;
import java.util.Random;

public class Inbox {
    private final String emailAddress;
    private ArrayList<Email> emailList;

    public Inbox(String emailAddress) {
        this.emailAddress = emailAddress;
        this.emailList = new ArrayList<>();
    }

    private String generateEmailId() {
        int leftLimit = 48; // numeral '0'
        int rightLimit = 122; // letter 'z'
        int targetStringLength = 5;
        Random random = new Random();

        return random.ints(leftLimit, rightLimit + 1)
                .filter(i -> (i <= 57 || i >= 65) && (i <= 90 || i >= 97))
                .limit(targetStringLength)
                .collect(StringBuilder::new, StringBuilder::appendCodePoint, StringBuilder::append)
                .toString();
    }

    public void setEmailList(ArrayList<Email> emailList){
        this.emailList = emailList;
    }

    public Email newEmail(String sender, ArrayList<String> addressees, String object, String body) {
        Email email = new Email(generateEmailId(), sender, addressees, object, body);

        return email;
    }

    public void addEmail(Email email) {
        emailList.add(email);
    }

    public void rmvEmail(Email email) {
        emailList.remove(email);
    }

    public boolean containsEmail(Email email) {
        return emailList.contains(email);
    }

    public void updateList(ArrayList<Email> newList) {
        if(newList != null) {
            emailList.clear();
            emailList = newList;
        }
    }

    public String getEmailAddress() {
        return emailAddress;
    }

    public ArrayList<Email> getEmailList() {
        return emailList;
    }

    public int getNumEmails() {
        return emailList.size();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Inbox inbox = (Inbox) o;

        return getEmailAddress() != null ? getEmailAddress().equals(inbox.getEmailAddress()) : inbox.getEmailAddress() == null;
    }

    @Override
    public int hashCode() {
        return getEmailAddress() != null ? getEmailAddress().hashCode() : 0;
    }
}
