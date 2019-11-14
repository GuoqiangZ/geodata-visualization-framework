package edu.cmu.cs.cs214.hw5.gui;

import edu.cmu.cs.cs214.hw5.core.UserInputConfig;
import edu.cmu.cs.cs214.hw5.core.UserInputType;

import javax.swing.BoxLayout;
import javax.swing.JPanel;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Control Panel consists of a list of UserInputComponent.
 */
class UserInputPanel extends JPanel {
    
    /**
     * List of UserInputComponent in the panel.
     */
    private final List<UserInputComponent> inputComponentList;

    /**
     * Create and initialize the panel.
     * 
     * @param configs a list of configurations which will show as UserInputComponents in the panel.
     */
    UserInputPanel(List<UserInputConfig> configs) {
        super();
        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));

        inputComponentList = new ArrayList<>();

        for (UserInputConfig config : configs) {
            String optionName = config.getName();
            UserInputType inputType = config.getInputType();
            switch (inputType) {
                case SINGLE_SELECTION:
                    this.inputComponentList.add(new UserInputComponent.SingleSelectionInput(optionName, config.getSelectionList()));
                    break;
                case MULTI_SELECTION:
                    this.inputComponentList.add(new UserInputComponent.MultiSelectionInput(optionName, config.getSelectionList()));
                    break;
                case TEXT_FIELD:
                    this.inputComponentList.add(new UserInputComponent.TextFieldInput(optionName));
                    break;
                default:
                    throw new IllegalArgumentException();
            }
        }

        for (UserInputComponent component : inputComponentList) {
            add(component);
        }
    }

    /**
     * Return a mapping from input component name to input specified by user.
     * 
     * @return mapping from input component name to input specified by user.
     */
    Map<String, List<String>> getUserInput() {
        Map<String, List<String>> res = new HashMap<>();
        inputComponentList.forEach(o -> res.put(o.getComponentName(), o.getUserInput()));
        return res;
    }

    /**
     * Return a List of user inputs lists.
     * 
     * @return a List of user inputs lists.
     */
    List<List<String>> getUserInputList() {
        return inputComponentList.stream().map(UserInputComponent::getUserInput).collect(Collectors.toList());
    }

    /**
     * Add an action listener to the panel.
     * 
     * @param l action listener.
     */
    void addActionListener(ActionListener l) {
        for (UserInputComponent component : inputComponentList) {
            component.addActionListener(l);
        }
    }
}
