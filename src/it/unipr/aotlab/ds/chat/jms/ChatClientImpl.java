package it.unipr.aotlab.ds.chat.jms;

import javax.jms.ObjectMessage;
import javax.jms.Queue;
import javax.jms.QueueReceiver;
import javax.jms.QueueSender;
import javax.jms.QueueSession;
import javax.jms.Session;
import javax.jms.Topic;
import javax.jms.TopicSession;
import javax.jms.TopicSubscriber;

import org.apache.activemq.ActiveMQConnection;
import org.apache.activemq.ActiveMQConnectionFactory;

import it.unipr.aotlab.ds.chat.client.ChatClient;
import it.unipr.aotlab.ds.chat.client.ChatGui;
import it.unipr.aotlab.ds.chat.command.Command;
import it.unipr.aotlab.ds.chat.command.Join;
import it.unipr.aotlab.ds.chat.command.Leave;
import it.unipr.aotlab.ds.chat.command.Send;

/**
 * 
 * Class providing two implementation of the chat client
 * for socket based communication.
 * 
 * @author Agostino Poggi - AOT Lab - DII - University of Parma
 * 
**/

public class ChatClientImpl implements ChatClient
{
  private final static String BROKER_URL    = "tcp://localhost:61616";
  private final static String SERVER_NAME   = "server";
  private final static String CLIENT_PREFIX = "client";

  // JMS connection
  private ActiveMQConnection connection;
  // JMS queue session
  private QueueSession qsession;
  // JMS queue receiver
  private QueueReceiver receiver;
  // JMS queue for incoming message
  private Queue iqueue;
  // incoming message queue name
  private String iname;
  // JMS queue sender
  private QueueSender sender;
  // JMS queue for outcoming message
  private Queue oqueue;
  // JMS topic session
  private TopicSession tsession;
  // JMS topic publisher
  private TopicSubscriber subscriber;
  // JMS topic
  private Topic topic;

  private ChatGui cg;
  /**
   * Class constructor. 
   *
  **/
  public ChatClientImpl() throws Exception
  {
    ActiveMQConnectionFactory cf = new ActiveMQConnectionFactory(ChatClientImpl.BROKER_URL);

    this.connection = (ActiveMQConnection) cf.createConnection();
    
    this.connection.start();
    
    this.qsession  =
      this.connection.createQueueSession(false, Session.AUTO_ACKNOWLEDGE);

    this.oqueue = this.qsession.createQueue(ChatClientImpl.SERVER_NAME);
    this.sender = this.qsession.createSender(this.oqueue);
  }

  /** {@inheritDoc} **/
  public Command receive()
  {
    try
    {
      Command c =
        (Command) ((ObjectMessage) this.subscriber.receive()).getObject();

      System.out.println("--> " + c.toString());
      
      return c;
    }
    catch (Exception e) // IO
    {
      e.printStackTrace();
        System.err.println(
            "Client might not be able to get data from the server!");
    }

    return null;
  }

  /** {@inheritDoc} **/
  public boolean join(final String n)
  {
	  
	boolean response = false;
    
	try
    {
      this.iname    = find(ChatClientImpl.CLIENT_PREFIX);
      this.iqueue   = this.qsession.createQueue(this.iname);
      this.receiver = this.qsession.createReceiver(this.iqueue);

      ObjectMessage sm = this.qsession.createObjectMessage();

      Object[] o = new Object[2]; 

      o[0] = this.iname;
      o[1] = new Join(n);

      sm.setObject(o);
      this.sender.send(sm);

      
      
      Object[] i =
        (Object[]) ((ObjectMessage) this.receiver.receive()).getObject();

      if (((String) i[0]).equals("accept"))
      {
        this.tsession =
          this.connection.createTopicSession(false, Session.AUTO_ACKNOWLEDGE);

        this.topic      = this.tsession.createTopic((String)i[1]);
        this.subscriber = this.tsession.createSubscriber(this.topic);
        
        System.out.println("-->" + this.topic.toString() + " - " + this.subscriber.toString());

        response = true;
      }
      
      if(response)
	      do{
	    	  i = (Object[]) ((ObjectMessage) this.receiver.receive()).getObject();
	
	    	  if (((String) i[0]).equals("old_message"))
	    	  {
	    		  System.out.println(i[1].toString());
	    		  //Send to console!!
	    		  cg.printMessage(i[1].toString());
	    		  
	    	  }
	    	  
	      }while(!((String) i[0]).equals("end"));
      
    }
    catch (Exception e)
    {
      e.printStackTrace();
      System.err.println(
            "Client might not be able to interact with the server!");
    }

    return response;
  }


  private String find(final String p) throws Exception
  {
    int i = 1;

    while (true)
    {
      boolean found = false;;

      String s = p + i++;

      for (Queue q : this.connection.getDestinationSource().getQueues())
      {
        if (q.getQueueName().equals(s))
        {
          found = true;

          break;
        }
      }

      if (!found)
      {
        return s;
      }
    }
  }

  /** {@inheritDoc} **/
  public void send(final String n, final String m)
  {
    try 
    {
      ObjectMessage sm = this.qsession.createObjectMessage();

      Object[] o = new Object[2]; 

      o[0] = this.iname;
      o[1] = new Send(n, m);

      sm.setObject(o);
      this.sender.send(sm);
    } 
    catch (Exception e) 
    {
      e.printStackTrace();
    }
  }


  /** {@inheritDoc} **/
  public void leave(final String n)
  {
    try 
    {
      ObjectMessage sm = this.qsession.createObjectMessage();

      Object[] o = new Object[2]; 

      o[0] = this.iname;
      o[1] = new Leave(n);

      sm.setObject(o);
      this.sender.send(sm);
      this.subscriber = null;
    } 
    catch (Exception e) 
    {
      e.printStackTrace();
    }
  }

  
  private void setGui(ChatGui gui) {
	  this.cg = gui;		
  }
  
  /**
   * Starts the client and its GUI.
   *
   * @param v  the arguments.
   *
   * It does not need arguments.
   *
  **/
  public static void main(String[] v)
  {
    try
    {
      ChatClientImpl cc = new ChatClientImpl();
      ChatGui cg = new ChatGui(cc);

      cc.setGui(cg);
      
      cg.listen();
      
      
    }
    catch (Exception e)
    {
      e.printStackTrace();
    }
  }


}