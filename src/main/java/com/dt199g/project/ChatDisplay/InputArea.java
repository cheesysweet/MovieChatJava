package com.dt199g.project.ChatDisplay;

import io.reactivex.rxjava3.core.Observable;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionListener;

/**
 * JPanel that displays the input area where the user can enter a message
 * @author Anton Byström
 */
public class InputArea extends JPanel {

    private final JTextField textField;
    private final Observable<String> textEvent;

    /**
     * sets up the text area and button
     */
    public InputArea() {
        super();

        JLabel question = new JLabel("Enter a question   ");
        question.setBackground(Color.decode("#666666"));

        JButton submit = new JButton("Submit");

        this.setBackground(Color.decode("#666666"));
        textField = new JTextField(20);
        textField.setSize(this.getWidth(), 40);


        textEvent = getActionListener(textField, submit);

        this.add(question);
        this.add(textField);
        this.add(submit);
    }

    /**
     * observable on action listener that listens to submit press och enter after typed the question
     * @param text JTextField that holds the user message
     * @param submit submit message
     * @return Observable<String> message that the user type
     */
    private Observable<String> getActionListener(JTextField text, JButton submit) {
        return Observable.create(subscribe -> {

            ActionListener actionListener = e ->
                    subscribe.onNext(e.getActionCommand().equals("Submit") ? text.getText().trim() : e.getActionCommand());

            text.addActionListener(actionListener);
            submit.addActionListener(actionListener);
        });
    }

    /**
     * gets the observable for the text
     * @return Observable<String> message that the user type
     */
    public Observable<String> getTextEvent() {
        return textEvent;
    }

    /**
     * clears the text area
     */
    public void clearTextField() {
        this.textField.setText("");
    }
}
