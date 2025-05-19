package com.rodini.contestprocessor;

import java.util.ArrayList;
import java.util.List;

import org.apache.logging.log4j.core.LogEvent;
import org.apache.logging.log4j.core.appender.AbstractAppender;
import org.apache.logging.log4j.message.SimpleMessage;

public class MockedAppender extends AbstractAppender {

    List<String> messages = new ArrayList<>();

    protected MockedAppender() {
    	super("MockedAppender", null, null, false, null);
    }

    @Override
    public void append(LogEvent event) {
        messages.add(event.getMessage().getFormattedMessage());
        //System.out.println(event.getMessage().getFormattedMessage());
    }
}