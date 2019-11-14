/*
 * Copyright (c) 1995, 2008, Oracle and/or its affiliates. All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 *
 *   - Redistributions of source code must retain the above copyright
 *     notice, this list of conditions and the following disclaimer.
 *
 *   - Redistributions in binary form must reproduce the above copyright
 *     notice, this list of conditions and the following disclaimer in the
 *     documentation and/or other materials provided with the distribution.
 *
 *   - Neither the name of Oracle or the names of its
 *     contributors may be used to endorse or promote products derived
 *     from this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS
 * IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO,
 * THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR
 * PURPOSE ARE DISCLAIMED.  IN NO EVENT SHALL THE COPYRIGHT OWNER OR
 * CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL,
 * EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO,
 * PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR
 * PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF
 * LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING
 * NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */ 

package edu.cmu.cs.cs214.hw5.gui;

import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;
import java.awt.BorderLayout;
import java.awt.Dimension;

/**
 * Component to be used as tabComponent. Contains a JLabel to show the text and a JButton to close
 * the tab it belongs to.
 */ 
class CloseButtonTab extends JPanel {

    /**
     * Width of button tab panel.
     */
    private static final int BUTTON_TAB_WIDTH = 140;

    /**
     * Height of button tab panel.
     */
    private static final int BUTTON_TAB_HEIGHT = 0;

    /**
     * Error message shown when tabbedPane is a null pointer.
     */
    private static final String NULL_TAB_PANE_MSG = "TabbedPane is null";

    /**
     * Closing tab confirmation message.
     */
    private static final String CONFIRM_CLOSE_MSG = "Close this tab?";
    
    /**
     * Create and initialize a CloseButtonTab.
     * 
     * @param parent parent container.
     * @param title title of this component.
     */
    CloseButtonTab(JTabbedPane parent, String title) {
        super();
        setOpaque(false);
        setLayout(new BorderLayout(0, 0));
        setPreferredSize(new Dimension(BUTTON_TAB_WIDTH, BUTTON_TAB_HEIGHT));
        if (parent == null) {
            throw new NullPointerException(NULL_TAB_PANE_MSG);
        }

        // close button
        JButton button = new CloseButton();
        add(button, BorderLayout.EAST);
        button.addActionListener(l -> {
            int response = JOptionPane.showConfirmDialog(
                    null, CONFIRM_CLOSE_MSG, "", JOptionPane.YES_NO_OPTION);
            if (response == JOptionPane.YES_OPTION) {
                int i = parent.indexOfTabComponent(CloseButtonTab.this);
                if (i != -1) {
                    parent.remove(i);
                }
            }
        });

        //make JLabel display title
        JLabel label = new JLabel(title);
        add(label, BorderLayout.CENTER);
    }
}


