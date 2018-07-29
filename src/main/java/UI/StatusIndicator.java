package UI;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;

public class StatusIndicator extends JPanel {
    private JLabel labelText;
    private JLabel labelImage;
    private String text;
    private Icon icon;

    public void setIcon(Icon newIcon) {
        this.icon = newIcon;
        labelImage.setIcon(this.icon);
    }

    public StatusIndicator(String text, Icon icon){
        super(new BorderLayout());
        this.text = text;
        this.icon = icon;

        labelText = new JLabel(text,JLabel.LEFT);
        labelImage = new JLabel(icon, JLabel.RIGHT);
        labelText.setFont(labelText.getFont().deriveFont(Font.ITALIC));

        labelImage.setBorder(new EmptyBorder(0,0,0,5));
        setBorder(new RoundedBorder());

        add(labelText,BorderLayout.CENTER);
        add(labelImage,BorderLayout.WEST);

    }
}
