package it.unipr.aotlab.ds.chat.client;

import java.awt.BorderLayout;
import java.awt.Container;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;

/**
 *
 * Class implementing the chat GUI.
 *
 * Note that a chat client using such a GUI should create an instance
 * of this class and then call its "listen" method.
 *
 * @author Agostino Poggi - AOT Lab - DII - University of Parma
 *
**/

public class ChatGui extends JFrame implements ActionListener
{
  // Serialization identifier.
  private static final long serialVersionUID = 1L;
  // Sleep time.
  private static final int SLEEPTIME = 1000;
  // Rows number.
  private static final int ROWS = 32;
  // Columns number.
  private static final int COLUMNS = 80;

  // User nickname.
  private String nickname;

  // Chat client.
  private ChatClient client;

  // GUI stuff.
  private JTextArea  enteredText;
  private JTextField typedText;
  private JButton join;
  private JButton send;
  private JButton leave;

  // flag indicating that the GUI is still open.
  private boolean open;

  /**
   * Class constructor.
   *
   * @param c  the chat client.
   *
  **/
  public ChatGui(final ChatClient c)
  {
    this.client = c;

    this.enteredText = new JTextArea(ROWS, COLUMNS);
    this.typedText   = new JTextField(COLUMNS);

    this.open = false;

    // closes the GUI and, if necessary,
    // forces the client to send a leave message.
    addWindowListener(
        new WindowAdapter()
        {
          public void windowClosing(final WindowEvent e)
          {
            if (leave.isEnabled())
            {
              client.leave(nickname);
            }
            open = false;
          }
        }
    );

    this.enteredText.setEditable(false);
    this.typedText.addActionListener(this);

    JPanel buttonsPane = new JPanel(new BorderLayout());

    this.join = new JButton();
    this.join.setText("Join");
    this.join.setToolTipText("Write your nickname and then click.");
    this.join.addActionListener(this);
    this.join.setActionCommand("join");
    this.join.setEnabled(true);

    this.send = new JButton();
    this.send.setText("Send");
    this.send.setToolTipText("Write the chat message and then click.");
    this.send.addActionListener(this);
    this.send.setActionCommand("send");
    this.send.setEnabled(false);

    this.leave = new JButton();
    this.leave.setText("Leave");
    this.leave.setToolTipText("Click for leaving the chat.");
    this.leave.addActionListener(this);
    this.leave.setActionCommand("leave");
    this.leave.setEnabled(false);

    buttonsPane.add(this.join, BorderLayout.WEST);
    buttonsPane.add(this.send, BorderLayout.CENTER);
    buttonsPane.add(this.leave, BorderLayout.EAST);
    buttonsPane.add(typedText, BorderLayout.SOUTH);

    Container content = getContentPane();
    content.add(new JScrollPane(enteredText), BorderLayout.CENTER);
    content.add(buttonsPane, BorderLayout.SOUTH);

    // display the window, with focus on typing box
    setTitle("Chat Client");
    pack();
    typedText.requestFocusInWindow();
    setVisible(true);
    setDefaultCloseOperation(EXIT_ON_CLOSE);
  }

  /**
   * Processes user's actions.
   *
   * @param e  the action event.
   *
  **/
  public void actionPerformed(final ActionEvent e)
  {
    if (e.getActionCommand().equals("join"))
    {
      this.nickname = typedText.getText();
      this.join.setEnabled(false);
      typedText.setText("");
      typedText.requestFocusInWindow();

      if (this.client.join(this.nickname))
      {
        this.open = true;

        this.send.setEnabled(true);
        this.leave.setEnabled(true);
      }
      else
      {
        this.join.setEnabled(true);
      }
    }
    else if (e.getActionCommand().equals("send"))
    {
      this.client.send(this.nickname, typedText.getText());

      typedText.setText("");
      typedText.requestFocusInWindow();
    }
    else if (e.getActionCommand().equals("leave"))
    {
      this.client.leave(nickname);

      this.join.setEnabled(false);
      this.send.setEnabled(false);
      this.leave.setEnabled(false);

      this.open = false;

      System.exit(0);
    }
  }

  public void printMessage(String str){
	  //if(this.open){
		  enteredText.insert(str + "\n", enteredText.getText().length());
		  enteredText.setCaretPosition(enteredText.getText().length());
	  //}
  }
  /**
   * Gets and prints chat messages.
   *
  **/
  public void listen()
  {
    try
    {
      while (!this.open)
      {
        Thread.sleep(SLEEPTIME);
      }

      while (this.open)
      {
        String text = this.client.receive().toString();

        enteredText.insert(text + "\n", enteredText.getText().length());
        enteredText.setCaretPosition(enteredText.getText().length());

        Thread.sleep(SLEEPTIME);
      }
    }
    catch (Exception e)
    {
      e.printStackTrace();
    }
  }
}
