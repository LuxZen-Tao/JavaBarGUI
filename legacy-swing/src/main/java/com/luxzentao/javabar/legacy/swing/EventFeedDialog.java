package com.luxzentao.javabar.legacy.swing;

import com.luxzentao.javabar.core.*;

import javax.swing.*;
import java.awt.*;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;

public class EventFeedDialog {
    private final JDialog dialog;
    private final JTextArea area;
    private final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("HH:mm:ss");

    public EventFeedDialog(JFrame owner) {
        dialog = new JDialog(owner, "Event Feed", false);
        dialog.setLayout(new BorderLayout(8, 8));

        area = new JTextArea(18, 56);
        area.setEditable(false);
        area.setFont(new Font("Monospaced", Font.PLAIN, 12));
        area.setLineWrap(true);
        area.setWrapStyleWord(true);

        JScrollPane scroll = new JScrollPane(area);
        scroll.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        dialog.add(scroll, BorderLayout.CENTER);

        JButton close = new JButton("Close");
        close.addActionListener(e -> dialog.setVisible(false));
        JPanel bottom = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        bottom.add(close);
        dialog.add(bottom, BorderLayout.SOUTH);

        DialogSizing.packClampAndCenter(dialog, owner);
    }

    public void appendEvent(String text) {
        String timestamp = "[" + LocalTime.now().format(formatter) + "] ";
        area.append(timestamp + text + "\n");
        area.setCaretPosition(area.getDocument().getLength());
    }

    public void showDialog() {
        dialog.setVisible(true);
    }
}
