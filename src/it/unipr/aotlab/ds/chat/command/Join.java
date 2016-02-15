package it.unipr.aotlab.ds.chat.command;

/**
 *
 * Class implementing the join command.
 *
 * Moreover, it provides an implementation of the {@code toString[]} method
 * that builds a textual representation of commands.
 *
 * @author  Agostino Poggi - AOT Lab - DII - University of Parma
 *
**/

public final class Join implements Command
{
  // Serialization identifier
  private static final long serialVersionUID = 1L;

  // User's nickname.
  private String name;

  /**
   * Class constructor.
   *
   * @param n  the user's nickname.
   *
  **/
  public Join(final String n)
  {
    this.name = n;
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

  /** {@inheritDoc} **/
  public String toString()
  {
    return this.name + " joins the chat";
  }
}
