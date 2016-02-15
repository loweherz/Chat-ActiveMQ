package it.unipr.aotlab.ds.chat.command;

/**
 *
 * Class implementing the send command.
 *
 * Moreover, it provides an implementation of the {@code toString[]} method
 * that builds a textual representation of commands.
 *
 * @author  Agostino Poggi - AOT Lab - DII - University of Parma
 *
**/

public final class Send implements Command
{
  // Serialization identifier
  private static final long serialVersionUID = 1L;

  // User's nickname.
  private String name;
  // Message to be sent.
  private String message;

  /**
   * Class constructor.
   *
   * @param n  the user's nickname.
   * @param m  the message to be sent.
   *
  **/
  public Send(final String n, final String m)
  {
    this.name    = n;
    this.message = m;
  }

  /**
   * Gets the user's nickname.
   *
   * @return the user's nickname.
   *
  **/
  public String getName()
  {
    return this.name;
  }

  /**
   * Gets the user's nickname.
   *
   * @return the user's nickname.
   *
  **/
  public String getMessage()
  {
    return this.message;
  }

  /** {@inheritDoc} **/
  public String toString()
  {
    return this.name + " sends the message: " + this.message;
  }
}
