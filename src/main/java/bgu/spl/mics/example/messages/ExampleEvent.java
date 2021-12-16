package bgu.spl.mics.example.messages;

import bgu.spl.mics.Event;
import bgu.spl.mics.Future;
import bgu.spl.mics.Message;
import bgu.spl.mics.application.objects.Model;

public class ExampleEvent implements Event<String>{

    private String senderName;

    public ExampleEvent(String senderName) {
        this.senderName = senderName;
    }

    public String getSenderName() {
        return senderName;
    }
    public void action(String future){}

    @Override
    public Future getFuture() {
        return null;
    }
}