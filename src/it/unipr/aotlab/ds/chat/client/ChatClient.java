package it.unipr.aotlab.ds.chat.client;

import it.unipr.aotlab.ds.chat.command.Command;

/**
 *
 * Interface defining a chat client.
 *
 * @author Agostino Poggi - AOT Lab - DII - University of Parma
 *
**/

public interface ChatClient
{
  /**
   * Receives a command.
   *
   * @return  the command.
   *
  **/
  Command receive();

  /**
   * Sends a join command and checks if the server accepts it.
   *
   * Note that the server cannot accept a join request only if there is
   * already a connected user with the same nickname.
   *
   * @param n  the user's nickname.
   *
   * @return  true if the server accept the command.
   *
  **/
  boolean join(final String n);

  /**
   * Sends a message command.
   *
   * @param n  the user's nickname.
   * @param m  the message command.
   *
  **/
  void send(final String n, final String m);

  /**
   * Sends a leave command.
   *
   * @param n  the user's nickname.
   *
  **/
  void leave(final String n);
}
