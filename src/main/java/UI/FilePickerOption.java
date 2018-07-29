package UI;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class FilePickerOption extends JPanel {
    private JTextField fileField;
    private JLabel fileLabel;
    private JButton fileOpenButton;
    private JButton fileClearButton;
    public String file = "";

    public FilePickerOption(String text, Runnable start, Runnable finish){
        super(new GridBagLayout());
        GridBagConstraints c = new GridBagConstraints();
        c.anchor = GridBagConstraints.PAGE_START;
        c.fill = GridBagConstraints.BOTH;
        c.weighty=0;

        fileLabel = new JLabel(text,JLabel.LEFT);
        fileLabel.setBorder(new EmptyBorder(0,0,0,5));
        fileField = new JTextField();
        fileField.setEditable(false);
        fileOpenButton = new JButton("Choose...");
        fileClearButton = new JButton("Clear");
        setBorder(new EmptyBorder(5,5,5,5));

        fileOpenButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                SwingUtilities.invokeLater(() -> {
                    start.run();
                    FileDialog fd = new java.awt.FileDialog((Frame)null,"Choose Input File",FileDialog.LOAD);
                    fd.setDirectory(System.getProperty("user.dir"));
                    fd.setVisible(true);
                    String filename = fd.getFile();
                    if (filename == null){
                        finish.run();
                    } else {
                        file = fd.getDirectory() + fd.getFile();
                        System.out.println(file);
                        finish.run();
                        fileField.setText(file);
                    }
                });
            }
        });

        fileClearButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                SwingUtilities.invokeLater(() -> {
                    file = "";
                    fileField.setText(file);
                    FloatUtilUI.checkState();
                });
            }
        });

        c.weightx=0;
        c.gridx = 0;
        c.gridy = 0;
        add(fileLabel,c);
        c.weightx=1;
        c.gridx = 1;
        c.gridy = 0;
        add(fileField,c);
        c.weightx=0;
        c.gridx = 2;
        c.gridy = 0;
        add(fileOpenButton,c);
        c.weightx=0;
        c.gridx = 3;
        c.gridy = 0;
        add(fileClearButton,c);

    }

    public void setButtonEnabled(boolean isEnabled){
        fileOpenButton.setEnabled(isEnabled);
    }
}
