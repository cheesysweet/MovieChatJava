package com.dt199g.project.ChatDisplay;

import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;

/**
 * JPanel that contains all the messages
 * @author Anton Byström
 */
public class ChatPanel extends JTextArea {

    private final List<String> conversationHistory;

    /**
     * creates the JTextArea to display the messages
     */
    public ChatPanel() {
        this.setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
        this.setBackground(Color.decode("#00001a"));
        this.setForeground(Color.WHITE);
        this.setLineWrap(true);
        this.setWrapStyleWord(true);
        this.setEnabled(false);
        conversationHistory = new ArrayList<>();

        addBotGreeting();
    }

    /**
     * adds conversation to the textArea
     * @param user question
     * @param bot response
     */
    public void addConversation(String user, String bot) {
        conversationHistory.add("User: " + user + "\n" + "MovieBot: " + bot);
        this.setText(String.join("\n\n", conversationHistory));
    }

    /**
     * adds bot greeting at the start of the program
     */
    public void addBotGreeting() {
        conversationHistory.add("MovieBot: Hello and welcome to MovieBot! Type a question about a movie with the \"movie Title\" " +
                "in quotation you can also specify the year the movie was released.");
        this.setText(String.join("\n", conversationHistory));
    }
}
