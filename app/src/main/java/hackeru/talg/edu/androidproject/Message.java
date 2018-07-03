package hackeru.talg.edu.androidproject;

public class Message {
    public String phoneNumber;
    public String messageContent;
    public String date;
    public String time;
    public String personId;

    public Message () {
    }

    public Message (String phoneNumber, String messageContent, String date, String time, String personId) {
        this.phoneNumber = phoneNumber;
        this.messageContent = messageContent;
        this.date = date;
        this.time = time;
        this.personId = personId;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public String getMessageContent() {
        return messageContent;
    }

    public String getDate() {
        return date;
    }

    public String getTime() {
        return time;
    }

    public String getPersonId() {
        return personId;
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    public void setMessageContent(String messageContent) {
        this.messageContent = messageContent;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public void setTime(String time) {
        this.time = time;
    }

    public void setPersonId(String personId) {
        this.personId = personId;
    }
}
