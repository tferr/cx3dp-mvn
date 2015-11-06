

package ini.cx3d.gui.simulation;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
 
/**
 * @author kirk
 * @since 5:54:30 AM, Sep 10, 2011
 */
public class TestPattern extends JFrame {
 
    private final JTextArea testString = new JTextArea();
    private final JTextArea testPattern = new JTextArea();
    private final JTextArea output = new JTextArea();
    private JButton findButton;
    private JButton groupButton;
    private JButton matchButton;
 
    public TestPattern() {
        super.setTitle("Java Regular Expression Evaluator");
        super.setLayout(new BorderLayout());
        JPanel listPane = new JPanel();
        listPane.setLayout(new BoxLayout(listPane, BoxLayout.PAGE_AXIS));
        //listPane.setBorder(BorderFactory.createEmptyBorder(30, 70, 10, 70));
        listPane.add(buildTestStringPanel());
        listPane.add(buildExpressionStringPanel());
        //listPane.add(buildSettingsPanel());
        listPane.add(buildResultsPanel());
        listPane.add(buildButtonsPanel());
        super.add(listPane, BorderLayout.CENTER);
    }
 
    private JPanel buildButtonsPanel() {
        JPanel buttons = new JPanel();
        buttons.setOpaque(false);
        findButton = new JButton("find");
        findButton.setBackground(Color.GREEN);
        buttons.add(findButton);
 
        groupButton = new JButton("group");
        groupButton.setBackground(Color.GREEN);
        buttons.add(groupButton);
 
        matchButton = new JButton("match");
        matchButton.setBackground(Color.RED);
        buttons.add(matchButton);
 
        JButton exitButton = new JButton("Exit");
        buttons.add(exitButton);
 
        findButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                executeFind();
            }
        });
 
        groupButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                executeGroup();
            }
        });
 
        matchButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                executeMatch();
            }
        });
 
        exitButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                System.exit(0);
            }
        });
 
        return buttons;
    }
 
    private JPanel buildResultsPanel() {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.add(new JLabel("Regular Expression Evaluation Result"));
        output.setRows(10);
        output.setEditable(false);
        panel.add(new JScrollPane(output));
        return panel;
    }
 
    private JPanel buildTestStringPanel() {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.add(new JLabel("Test String"));
        testString.setRows(1);
        testString.setEditable(true);
        panel.add(new JScrollPane(testString));
        return panel;
    }
 
    private JPanel buildExpressionStringPanel() {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.add(new JLabel("Regular Expression"));
        testPattern.setRows(1);
        testPattern.setEditable(true);
        panel.add(new JScrollPane(testPattern));
        return panel;
    }
 
    private JPanel buildSettingsPanel() {
        JLabel label;
        JPanel settingPanel = new JPanel(new GridLayout(0, 2, 10, 10));
 
        settingPanel.setBorder(BorderFactory.createTitledBorder("Settings:"));
        settingPanel.setOpaque(false);
 
        label = new JLabel("Test String:", JLabel.RIGHT);
        label.setToolTipText("String to match pattern against");
        settingPanel.add(label);
        settingPanel.add(testString);
 
        label = new JLabel("Regular Expression:", JLabel.RIGHT);
        label.setToolTipText("regular expression to match against test string");
        settingPanel.add(label);
        settingPanel.add(testPattern);
 
        return settingPanel;
    }
 
    public void executeFind() {
        String pattern = testPattern.getText();
        String targetString = testString.getText();
        try {
            if ( Pattern.compile( pattern).matcher( targetString).find())
                output.setText( "pattern matched");
            else
                output.setText( "pattern failed to match");
        } catch(Throwable t) {
            StringBuilder text = new StringBuilder(t.getClass().getName());
            text.append('\n').append( t.getMessage());
            output.setText( text.toString());
        }
    }
 
    public void executeGroup() {
        String pattern = testPattern.getText();
        String targetString = testString.getText();
        try {
            Pattern p = Pattern.compile( pattern);
            Matcher m = p.matcher( targetString);
            String group = "";
            if ( m.find()) {
                int groups = m.groupCount();
                StringBuffer results = new StringBuffer();
                for ( int i = 0; i <= groups; i++) {
                    results.append(i + ": `" + m.group(i) + "`\n" );
                }
                output.setText(results.toString());
            }
        } catch(Throwable t) {
            StringBuilder text = new StringBuilder(t.getClass().getName());
            text.append('\n').append( t.getMessage());
            output.setText( text.toString());
        }
    }
 
    public void executeMatch() {
        String pattern = testPattern.getText();
        String targetString = testString.getText();
        try {
            if ( Pattern.compile( pattern).matcher( targetString).matches())
                output.setText( "pattern matched");
            else
                output.setText( "pattern failed to match");
        } catch(Throwable t) {
            StringBuilder text = new StringBuilder(t.getClass().getName());
            text.append('\n').append( t.getMessage());
            output.setText( text.toString());
        }
    }
 
    public static void main(String[] args) {
        final TestPattern me = new TestPattern();
        me.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
        me.addWindowListener( new WindowAdapter() {
            public void windowClosing( WindowEvent evt) {
                int response = JOptionPane.showConfirmDialog( me, "Exit?\n",
                  "Exit?", JOptionPane.YES_NO_OPTION);
                if ( JOptionPane.YES_OPTION == response)
                    System.exit(0);
            }
        });
        me.pack();
        me.setVisible(true);
    }
}