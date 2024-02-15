package com.dt199g.project.ChatDisplay;

import com.dt199g.project.Response.Respond;
import io.reactivex.rxjava3.schedulers.Schedulers;

import javax.swing.*;
import java.awt.*;

/**
 * Main frame for user interaction
 * @author Anton Byström
 */
public class ChatFrame extends JFrame {

    /**
     * creates the frame subscribes to the text area
     */
    public ChatFrame() {
        super("MovieBot");
        this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        this.setPreferredSize(new Dimension(600, 800));

        ChatPanel chatPanel = new ChatPanel();
        JScrollPane scrollPane = new JScrollPane(chatPanel);
        InputArea inputArea = new InputArea();

        Respond respond = new Respond();

        inputArea.getTextEvent()
                .subscribeOn(Schedulers.io())
                .subscribe(res -> {
                    chatPanel.addConversation(res, respond.getResponse(res));
                    inputArea.clearTextField();
                }, err -> System.err.println(err.getMessage()));

        this.add(BorderLayout.CENTER, scrollPane);
        this.add(BorderLayout.SOUTH, inputArea);
        this.pack();
        this.setVisible(true);
    }
}
