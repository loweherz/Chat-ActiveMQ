package it.unipr.aotlab.ds.chat.jms;

import it.unipr.aotlab.ds.chat.command.Command;
import it.unipr.aotlab.ds.chat.command.Join;
import it.unipr.aotlab.ds.chat.command.Leave;
import it.unipr.aotlab.ds.chat.command.Send;

import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.Map.Entry;

import javax.jms.JMSException;
import javax.jms.ObjectMessage;
import javax.jms.Queue;
import javax.jms.QueueReceiver;
import javax.jms.QueueSession;
import javax.jms.Session;
import javax.jms.Topic;
import javax.jms.TopicPublisher;
import javax.jms.TopicSession;

import org.apache.activemq.ActiveMQConnection;
import org.apache.activemq.ActiveMQConnectionFactory;
import org.apache.activemq.broker.BrokerFactory;
import org.apache.activemq.broker.BrokerService;

/**
 * 
 * Class providing two implementation of the chat client
 * for socket based communication.
 * 
 * @author Agostino Poggi - AOT Lab - DII - University of Parma
 * 
**/

public class ChatServer
{
  private final static String BROKER_URL   = "tcp://localhost:61616";
  private final static String BROKER_PROPS = "persistent=false&useJmx=false";
  private final static String SERVER_NAME  = "server";
  private final static String TOPIC_NAME   = "chat";
  
  //old_messages
  private LinkedHashMap<Integer, Send> old_messages = new LinkedHashMap<Integer, Send>();
  private int id = 0;
  
  // JMS broker
  private BrokerService broker;
  // JMS connection
  private ActiveMQConnection connection;
  // JMS queue session
  private QueueSession qsession;
  // JMS queue receiver
  private QueueReceiver receiver;
  // JMS queue for incoming message
  private Queue queue;
  // JMS topic session
  private TopicSession tsession;
  // JMS topic publisher
  private TopicPublisher publisher;
  // JMS topic
  private Topic topic;
  // client nicknames
  private HashSet<String> nicknames;

  /**
   * Class constructor. 
   *
  **/
  public ChatServer()
  {
    this.nicknames = new HashSet<String>();
  }

  /** {@inheritDoc} **/
  public void run()
  {
    try
    {
      /*this.broker = new BrokerService();

   // configure the broker
   this.broker.addConnector("tcp://localhost:61616");

  /* broker.start();*/
      this.broker = BrokerFactory.createBroker(
          "broker:(" + ChatServer.BROKER_URL + ")?" +
          ChatServer.BROKER_PROPS);

      this.broker.start();

      ActiveMQConnectionFactory cf = new ActiveMQConnectionFactory(ChatServer.BROKER_URL);
      this.connection = (ActiveMQConnection) cf.createConnection();
      
      this.connection.start();
      
      this.qsession  =
        this.connection.createQueueSession(false, Session.AUTO_ACKNOWLEDGE);

      
      //point-to-point
      this.queue    = this.qsession.createQueue(ChatServer.SERVER_NAME);
      this.receiver = this.qsession.createReceiver(this.queue);

      
      //Publish-subscribe
      this.tsession =
        this.connection.createTopicSession(false, Session.AUTO_ACKNOWLEDGE);

      this.topic     = this.tsession.createTopic(ChatServer.TOPIC_NAME);
      this.publisher = this.tsession.createPublisher(this.topic);

      while(true)
      {
        try 
        {
          Object[] o = (Object[]) ((ObjectMessage) this.receiver.receive()).getObject();

          String q  = (String) o[0];
          Command c = (Command) o[1];

          System.out.println("--> " + q + " - " + c.toString());
          
          if (c instanceof Join)
          {
            Join j = (Join) c;

            boolean accepted = false;

            Object[] r = new Object[2];

            if (this.nicknames.contains(j.getName()))
            {
              r[0] = "refuse";
            }
            else
            {
              accepted = true;

              this.nicknames.add(j.getName());

              r[0] = "accept";
              r[1] = ChatServer.TOPIC_NAME;
            }
              
            if (!reply(q, r))
            {
              return;
            }

            if (accepted)
            {
            	
              ObjectMessage cm = this.qsession.createObjectMessage();

              cm.setObject(j);
              this.publisher.send(cm);
              
              System.out.println("Invio vecchi messaggi");
              //Print all values stored in ConcurrentHashMap instance
              for (Entry<Integer, Send> e : old_messages.entrySet())
              {
            	  Send tmp = e.getValue();
            	  
            	  					//nick		//message
            	  System.out.println(tmp.getName()+" = "+tmp.getMessage());
            	  /*
            	  ObjectMessage replyMessage = this.qsession.createObjectMessage();
            	  replyMessage.setObject(tmp);
            	  Queue sender_q = find(q);
            	  this.qsession.createSender(sender_q).send(replyMessage);
            	  */
            	  Object[] old = new Object[2];
                  old[0] = "old_message";
                  old[1] = "(PREVIOUS) -> " + tmp.getName()+" : "+tmp.getMessage();
                  
                  if (!reply(q, old))
                  {
                    return;
                  }
            	  
            	  //c.send(new Send(tmp[0], tmp[1]));
              }
              
              Object[] end = new Object[2];
              end[0] = "end";
              end[1] = ChatServer.TOPIC_NAME;
              
              if (!reply(q, end))
              {
                return;
              }
              
            }
          }
          else if (c instanceof Send)
          {
        	//save messages
        	old_messages.put(id, (Send) c);
        	id++;
        	
            ObjectMessage cm = this.qsession.createObjectMessage();

            cm.setObject(c);
            this.publisher.send(cm);
          }
          else if (c instanceof Leave)
          {
            Leave l = (Leave) c;

            ObjectMessage cm = this.qsession.createObjectMessage();

            if (this.nicknames.contains(l.getName()))
            {
              this.nicknames.remove(l.getName());

              cm.setObject(l);
              this.publisher.send(cm);
            }
          }
        }
        catch (Exception e) 
        {
          return; 
        }
      }
    }
    catch (Exception e)
    {
      e.printStackTrace();

      return;
    }
  }

  // forward reply message 
  private boolean reply(final String c, final Object[] m)
  {
    Queue q = find(c);

    if (q != null)
    {
      try 
      {
        ObjectMessage replyMessage = this.qsession.createObjectMessage();

        replyMessage.setObject(m);
        this.qsession.createSender(q).send(replyMessage);
        
        return true;
      } 
      catch (JMSException e) 
      {
      }
    }

    return false;
  }

  private Queue find(final String n)
  {
    try
    {
      for (Queue q : this.connection.getDestinationSource().getQueues())
      {
        if (q.getQueueName().equals(n))
        {
          return q;
        }
      }

      return null;
    }
    catch (Exception e)
    {
      return null;
    }
  }

  /**
   * Starts the server.
   *
   * @param args  the arguments.
  **/
  public static void main(String[] args)
  {
    new ChatServer().run();   
  }
}