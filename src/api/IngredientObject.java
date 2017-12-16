package api;

import javax.swing.*;
import java.awt.*;

public class IngredientObject {

    private String ingredient;
    private JCheckBox checkBox;

    /**
     * Constructor for Recipe Object
     * @param ingredient
     */
    public IngredientObject(String ingredient){
        this.ingredient = ingredient;

        this.checkBox = new JCheckBox();
    }

    public JPanel getIngredientJPanelObject(){

        JPanel mainPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));

        mainPanel.setBorder(BorderFactory.createLineBorder(Color.BLACK));
        mainPanel.add(checkBox);
        mainPanel.add(new JLabel(ingredient));

        return mainPanel;
    }

    public boolean isCheckBoxChecked(){
        return checkBox.isSelected();
    }

    public String getIngredientTitle() {
        return ingredient;
    }
}
