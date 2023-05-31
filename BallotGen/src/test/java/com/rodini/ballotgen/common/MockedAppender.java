package com.rodini.ballotgen.common;

import java.util.ArrayList;
import java.util.List;

import org.apache.logging.log4j.core.LogEvent;
import org.apache.logging.log4j.core.appender.AbstractAppender;
import org.apache.logging.log4j.message.SimpleMessage;

public class MockedAppender extends AbstractAppender {

    public List<String> messages = new ArrayList<>();

    public MockedAppender() {
    	super("MockedAppender", null, null, false, null);
    }

    @Override
    public void append(LogEvent event) {
        messages.add(event.getMessage().getFormattedMessage());
        //System.out.println(event.getMessage().getFormattedMessage());
    }
}