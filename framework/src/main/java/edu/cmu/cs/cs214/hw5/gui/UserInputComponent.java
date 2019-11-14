package edu.cmu.cs.cs214.hw5.gui;

import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Component to get inputs from user.
 */
abstract class UserInputComponent extends JPanel {
    
    /**
     * Width of input component.
     */
    private static final int INPUT_COMPONENT_WIDTH = 150;

    /**
     * Height of input component.
     */
    private static final int INPUT_COMPONENT_HEIGHT = 27;

    /**
     * Name of this component.
     */
    private final String componentName;

    /**
     * Create and initialize this component.
     * 
     * @param name name of this component.
     */
    private UserInputComponent(String name) {
        this.componentName = name;
        setLayout(new BorderLayout());
        JLabel nameLabel = new JLabel(name + ":");
        nameLabel.setPreferredSize(new Dimension(INPUT_COMPONENT_WIDTH, INPUT_COMPONENT_HEIGHT));
        nameLabel.setToolTipText(name);
        add(nameLabel, BorderLayout.WEST);
    }

    /**
     * Return the name of this component.
     * 
     * @return name of this component.
     */
    String getComponentName() {
        return this.componentName;
    }

    /**
     * Add an action listener to this component.
     * 
     * @param l action listener.
     */
    abstract void addActionListener(ActionListener l);

    /**
     * Return a list of inputs specified by user.
     * 
     * @return a list of inputs specified by user.
     */
    abstract List<String> getUserInput();

    /**
     * Component to let user specify inputs as text.
     */
    static class TextFieldInput extends UserInputComponent {
        
        /**
         * Text field where user can type text.
         */
        private final JTextField textField;

        /**
         * Create and initialize a TextFieldInput.
         * 
         * @param name name of this component.
         */
        TextFieldInput(String name) {
            super(name);
            textField = new JTextField();
            add(textField, BorderLayout.CENTER);
        }

        /**
         * Add an action listener to this component.
         *
         * @param l action listener.
         */
        @Override
        void addActionListener(ActionListener l) {
            textField.addActionListener(l);
        }

        /**
         * Return a list of inputs specified by user.
         *
         * @return a list of inputs specified by user.
         */
        @Override
        public List<String> getUserInput() {
            return List.of(textField.getText());
        }
    }

    /**
     * Component to let user specify input by selecting a single value.
     */
    static class SingleSelectionInput extends UserInputComponent {
        
        /**
         * A JComboBox which contains the potential values can be selected.
         */
        private final JComboBox<String> comboBox;

        /**
         * Create and initialize a SingleSelectionInput.
         *
         * @param name name of this component.
         * @param choiceList a list of potential values can be selected.
         */
        SingleSelectionInput(String name, List<String> choiceList) {
            super(name);
            this.comboBox = new JComboBox<>();
            add(comboBox, BorderLayout.CENTER);
            comboBox.addItem(null);
            choiceList.forEach(comboBox::addItem);
        }

        /**
         * Add an action listener to this component.
         *
         * @param l action listener.
         */
        @Override
        public void addActionListener(ActionListener l) {
            comboBox.addActionListener(l);
        }

        /**
         * Return a list of inputs specified by user.
         *
         * @return a list of inputs specified by user.
         */
        @Override
        public List<String> getUserInput() {
            if (comboBox.getSelectedItem() == null)
                return new ArrayList<>();
            return Collections.singletonList((String) comboBox.getSelectedItem());
        }
    }

    /**
     * Component to let user specify input by selecting one or multiple value(s).
     */
    static class MultiSelectionInput extends UserInputComponent {
        
        /**
         * List of JCheckBox.
         */
        private final List<JCheckBox> checkBoxesList;

        /**
         * JPopupMenu.
         */
        private final JPopupMenu popupMenu;

        /**
         * Set of values selected by user.
         */
        private final Set<String> selected;

        /**
         * List of action listeners.
         */
        private final List<ActionListener> actionListeners;

        private final JButton button;


        /**
         * Create and initialize a MultiSelectionInput.
         * 
         * @param name name of this component.
         * @param choiceList a list of potential values can be selected.
         */
        MultiSelectionInput(String name, List<String> choiceList) {
            super(name);
            this.checkBoxesList = new ArrayList<>();
            this.selected = new HashSet<>();
            actionListeners = new ArrayList<>();
            JCheckBox selectAll = new JCheckBox("Select All");

            for (String s : choiceList) {
                JCheckBox box = new JCheckBox(s);
                checkBoxesList.add(box);
                box.addActionListener(e -> {
                    if (box.isSelected())
                        selected.add(s);
                    else {
                        selected.remove(s);
                        selectAll.setSelected(false);
                    }
                    for (ActionListener l : actionListeners) {
                        l.actionPerformed(e);
                    }
                    refreshText();
                });
            }

            JPanel innerPanel = new JPanel();
            innerPanel.setLayout(new BoxLayout(innerPanel, BoxLayout.Y_AXIS));
            innerPanel.add(selectAll);
            checkBoxesList.forEach(innerPanel::add);

            JScrollPane pane = new JScrollPane(innerPanel);

            popupMenu = new JPopupMenu();
            popupMenu.add(pane);

            button = new JButton();
            button.setPreferredSize(new Dimension(0, 27));
            button.addActionListener(e -> {
                pane.setPreferredSize(new Dimension(button.getWidth(), Math.min(400, 27 + 27 * checkBoxesList.size())));
                popupMenu.show(button, 0, button.getHeight());
            });
            add(button, BorderLayout.CENTER);

            selectAll.addActionListener(e -> {
                if (selectAll.isSelected()) {
                    checkBoxesList.forEach(b -> b.setSelected(true));
                    selected.addAll(choiceList);
                } else {
                    checkBoxesList.forEach(b -> b.setSelected(false));
                    selected.clear();
                }
                refreshText();
                for (ActionListener l : actionListeners) {
                    l.actionPerformed(e);
                }
            });

        }

        /**
         * Add an action listener to this component.
         *
         * @param l action listener.
         */
        @Override
        void addActionListener(ActionListener l) {
            actionListeners.add(l);
        }

        /**
         * Return a list of inputs specified by user.
         *
         * @return a list of inputs specified by user.
         */
        @Override
        public List<String> getUserInput() {
            return new ArrayList<>(selected);
        }

        private void refreshText() {
            String s = String.join(", ", selected.stream().limit(4).collect(Collectors.toList()));
            if (selected.size() > 4)
                s = s + "...";
            button.setText(s);
        }
    }

}
