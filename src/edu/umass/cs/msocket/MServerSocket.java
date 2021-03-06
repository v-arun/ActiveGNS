/*******************************************************************************
 *
 * Mobility First - mSocket library
 * Copyright (C) 2013, 2014 - University of Massachusetts Amherst
 * Contact: arun@cs.umass.edu
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * http://www.apache.org/licenses/LICENSE-2.0
 *  
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License. 
 *
 * Initial developer(s): Arun Venkataramani, Aditya Yadav, Emmanuel Cecchet.
 * Contributor(s): ______________________.
 *
 *******************************************************************************/

package edu.umass.cs.msocket;

import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.SocketAddress;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.Iterator;
import java.util.List;
import java.util.Vector;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import org.apache.log4j.Logger;

import edu.umass.cs.msocket.common.CommonMethods;
import edu.umass.cs.msocket.common.policies.NoProxyPolicy;
import edu.umass.cs.msocket.common.policies.ProxySelectionPolicy;
import edu.umass.cs.msocket.gns.Integration;
import edu.umass.cs.msocket.mobility.MobilityManagerServer;

/**
 * This class implements the MSeverSocket. Applications can use MServerSoceket
 * to start a server and start accepting connections.
 * 
 * @author aditya
 */
public class MServerSocket extends ServerSocket
{
  /** The underlying ServerSocketChannel */
  private ServerSocketChannel     ssc                 = null;
  /** Shortcut to the socket in the ServerSocketChannel */
  private ServerSocket            ss                  = null;
  /**
   * The name of the service/server to be registered in the GNS, this is the
   * name the client will use to lookup the server
   */
  private String                  serverAlias         = "";
  private int                     serverListeningPort = -1;

  /** The proxy selection policy */
  private ProxySelectionPolicy    proxySelection;

  private MServerSocketController controller          = null;
  private AcceptConnectionQueue   AcceptConnectionQueueObj;
  private AcceptThreadPool        AcceptThreadPoolObj;

  private final Object            monitor             = new Object();

  private final Object            migrateMonitor      = new Object();

  /** Set to 0 which means infinite timeout */
  private int                     soTimeout           = 0;
  /** GUID correspoinding to server alias */
  private String                  serverGUID          = "";
  private boolean                 isBound;
  private boolean                 isClosed;

  private static Logger           log                 = Logger.getLogger(MServerSocket.class.getName());

  /**
   * Creates an unbound server socket.
   * 
   * @throws IOException IO error when loading implementation
   */
  public MServerSocket() throws IOException
  {
	  ssc = ServerSocketChannel.open();
  }
  
  /**
   * Create a server with the specified port, listen backlog, and local IP address to bind to. 
   * The bindAddr argument can be used on a multi-homed host for a ServerSocket that will only accept connect requests to one of its addresses.
   *  If bindAddr is null, it will default accepting connections on any/all local addresses. 
   *  The port must be between 0 and 65535, inclusive. 
   *  A port number of 0 means that the port number is automatically allocated, typically from an ephemeral port range. 
   *  This port number can then be retrieved by calling getLocalPort.
   *  
   *  The backlog argument is the requested maximum number of pending connections on the socket. 
   *  Its exact semantics are implementation specific. In particular, an implementation may impose a maximum length or may choose to ignore the parameter altogther. 
   *  The value provided should be greater than 0. If it is less than or equal to 0, then an implementation specific default will be used. 
   * 
   * @param port - the port number, or 0 to use a port number that is automatically allocated.
   * @param backlog - requested maximum length of the queue of incoming connections.
   * @param bindAddr - the local InetAddress the server will bind to
   * @throws IOException - if an I/O error occurs when opening the socket.
   */
  public MServerSocket(int port, int backlog, InetAddress bindAddr) throws IOException
  {
	  this();
	  bind(new InetSocketAddress(bindAddr, port), backlog);
  }
  
  /**
   * Creates a server socket and binds it to the specified local port number, with the specified backlog. 
   * A port number of 0 means that the port number is automatically allocated, typically from an ephemeral port range. 
   * This port number can then be retrieved by calling getLocalPort.
   * The maximum queue length for incoming connection indications (a request to connect) is set to the backlog parameter. 
   * If a connection indication arrives when the queue is full, the connection is refused.
   * The backlog argument is the requested maximum number of pending connections on the socket. 
   * Its exact semantics are implementation specific. 
   * In particular, an implementation may impose a maximum length or may choose to ignore the parameter altogther. 
   * The value provided should be greater than 0. If it is less than or equal to 0, then an implementation specific default will be used. 
   * 
   * @param port - the port number, or 0 to use a port number that is automatically allocated.
   * @param backlog - requested maximum length of the queue of incoming connections.
   * @throws IOException - if an I/O error occurs when opening the socket.
   */
  public MServerSocket(int port, int backlog) throws IOException
  {
	  this();
	  bind(new InetSocketAddress(InetAddress.getLocalHost(), port), backlog);
  }
  
  /**
   * Creates a server socket, bound to the specified port. 
   * A port number of 0 means that the port number is automatically allocated, typically from an ephemeral port range. 
   * This port number can then be retrieved by calling getLocalPort.
   * The maximum queue length for incoming connection indications (a request to connect) is set to 50. 
   * If a connection indication arrives when the queue is full, the connection is refused.
   * 
   * @param port - the port number, or 0 to use a port number that is automatically allocated.
   * @throws IOException - if an I/O error occurs when opening the socket.
   */
  public MServerSocket(int port) throws IOException
  {
	  this();
	  bind(new InetSocketAddress(InetAddress.getLocalHost(), port), 0);
  }

  /**
   * Creates a new MServerSocket using the given GNS credentials, using no proxies, 
   * a random port number chosen automatically by the system. The
   * given name is published in the GNS as an alias to be looked up by clients.
   * 
   * @param serverName the name to give to this socket in the GNS
   * @param gnsCredentials the GNS credentials to use to register the name in
   *          the GNS
   * @throws Exception
   */
  public MServerSocket(String serverName) throws IOException
  {
    this(serverName, Integration.getDefaultProxyPolicy(), null, 0);
  }

  /**
   * Creates a new MServerSocket using the default GNS credentials, the given
   * proxy policy and a random port number chosen automatically by the system.
   * The given name is published in the GNS as an alias to be looked up by
   * clients.
   * 
   * @param serverName the name to give to this socket in the GNS
   * @param proxySelection a {@link ProxySelectionPolicy} defining how to choose
   *          a proxy
   * @throws Exception
   */
  public MServerSocket(String serverName, ProxySelectionPolicy proxySelection) throws IOException
  {
    this(serverName, proxySelection, null, 0);
  }

  /**
   * Creates a new MServerSocket using the given GNS credentials and the default
   * proxy policy. The given port number is bound locally. The given name is
   * published in the GNS as an alias to be looked up by clients.
   * 
   * @param serverName the name of the service/server to register in the GNS
   * @param serverPort the port number to bind
   * @param proxyPolicy a {@link ProxySelectionPolicy} defining how to choose a
   *          proxy
   * @param gnsCredentials GNS credentials to use for this connection
   * @param endpoint The IP address & port number to bind to (null to let the
   *          system choose).
   * @param backlog The listen backlog length (0 for default)
   * @throws Exception If NoProxyPolicy is used and the machine IP is behind a
   *           NAT
   */
  public MServerSocket(String serverName, ProxySelectionPolicy proxyPolicy,
      SocketAddress endpoint, int backlog) throws IOException
  {
    initializeServerSocket(serverName, proxyPolicy, endpoint, backlog);
  }

  /**
   * Call accept to start accepting connections
   * 
   * @see java.net.ServerSocket#accept()
   */
  public MSocket accept() throws IOException, SocketTimeoutException
  {
	  // now proxy accept and no proxy accept happens in Executor service
    //if (!proxySelection.hasAvailableProxies())
    {
      if (soTimeout == 0)
      {
        BlockForAccept();
        return (MSocket) AcceptConnectionQueueObj.getFromQueue(AcceptConnectionQueue.GET, null);
      }
      else
      {
        try
        {
          TimeOutWaitForaccept();
          return (MSocket) AcceptConnectionQueueObj.getFromQueue(AcceptConnectionQueue.GET, null);
        }
        catch (final InterruptedException e)
        {
          e.printStackTrace();
          // The thread was interrupted during sleep, wait or join
        }
        catch (final TimeoutException e)
        {
          SocketTimeoutException ste = new SocketTimeoutException("socket time out exception");
          throw ste;
          // Took too long!
        }
        catch (final ExecutionException e)
        {
          e.printStackTrace();
          // An exception from within the Runnable task
        }
      }
    }
    /*else
    {
      return controller.getProxyConnObj().accept();
    }*/
    return null;
  }

  /**
   * Closes the MServerSocket, closes all accepted connections and frees the
   * state. MServerSocket close doesn't close MobilityManagerServer
   * 
   * @see java.net.ServerSocket#close()
   */
  public void close() throws IOException
  {
    ssc.close();
    ss.close();
    controller.closeChildren();
    AcceptThreadPoolObj.StopAcceptPool();

    controller.close();
    MobilityManagerServer.unregisterWithManager(controller);
    try
    {
      Integration.unregisterWithGNS(getServerName());
    }
    catch (Exception e)
    {
      log.warn("Failed to unregister server socket " + getServerName() + " from GNS", e);
    }

    isClosed = true;
  }

  /**
   * Binds the MServerSocket to the given endpoint. The given name is published
   * in the GNS as an alias to be looked up by clients using the given GNS
   * credentials. Note that the endpoint should be an IP address reachable by
   * clients.
   * 
   * @param serverName the name of the service/server to register in the GNS
   * @param endpoint The IP address & port number to bind to.
   * @param backlog The listen backlog length.
   * @param gnsCredentials GNS credentials to use for this connection
   * @throws Exception If the socket is already bound
   * @see java.net.ServerSocket#bind(java.net.SocketAddress, int)
   */
  public void bind(String serverName, SocketAddress endpoint, int backlog)
      throws IOException
  {
    if (isBound)
      throw new IOException("MServerSocket already bound");
    
    initializeServerSocket(serverName, new NoProxyPolicy(), endpoint, backlog);
  }

  /**
   * 
   * @see java.net.ServerSocket#bind(java.net.SocketAddress)
   */
  public void bind(SocketAddress endpoint) throws IOException
  {
	  bind(endpoint, 0);
  }

  /**
   * Binds the socket to the address specified, since there is no name or GUID
   * given, so the server is not registered in GNS, nor is connected to the
   * proxies. Mainly, to support legacy operations.
   * 
   * @see java.net.ServerSocket#bind(java.net.SocketAddress, int)
   */
  public void bind(SocketAddress endpoint, int backlog) throws IOException
  {
	    proxySelection = new NoProxyPolicy();
	  	ss = ssc.socket();
	    ss.bind(endpoint, backlog);
	    serverListeningPort = ss.getLocalPort();
	
	    if (controller == null)
	    { // enabling UDP control socket
	      controller = new MServerSocketController(this);
	      // moving thread here
	      (new Thread(controller)).start();
	      controller.startKeepAliveThread();
	    }
	    //controller.setGNSCredential(gnsCredentials);
	
	    AcceptConnectionQueueObj = new AcceptConnectionQueue();
	    AcceptThreadPoolObj = new AcceptThreadPool(ssc);
	
	    (new Thread(AcceptThreadPoolObj)).start();
	    
	    // application has not opened it with server name
	    if(!serverAlias.equals(""))
	    {
	    	try
	    	{
	    		Integration.registerWithGNS(serverAlias, (InetSocketAddress) ss.getLocalSocketAddress());
	    	}
	    	catch (Exception ex)
	    	{
	    		log.trace("registration with GNS failed "+ex);
	    		//ex.printStackTrace();
	    	}
	    }
	    
	    MobilityManagerServer.registerWithManager(controller);
	    isBound = true;
  }

  /**
   * Returns the address to which the server socket is locally bounded
   * 
   * @see java.net.ServerSocket#getInetAddress()
   */
  public InetAddress getInetAddress()
  {
    return ss.getInetAddress();
  }

  /**
   * @see java.net.ServerSocket#getLocalPort()
   */
  public int getLocalPort()
  {
    return ss.getLocalPort();
  }

  /**
   * @see java.net.ServerSocket#getLocalSocketAddress()
   */
  public SocketAddress getLocalSocketAddress()
  {
    return ss.getLocalSocketAddress();
  }

  /**
   * MServerSocket can't return channel to the application, this method will
   * always return null
   * 
   * @see java.net.ServerSocket#getChannel()
   */
  public ServerSocketChannel getChannel()
  {
	  throw new UnsupportedOperationException("getChannel() not supported by MServerSocket");
  }

  /**
   * @see java.net.ServerSocket#isBound()
   */
  public boolean isBound()
  {
    return isBound;
  }

  /**
   * @see java.net.ServerSocket#isClosed()
   */
  public boolean isClosed()
  {
    return isClosed;
  }

  /**
   * Sets SO_TIMEOUT of accept for the MServerSocket MServerSocket accept will
   * throw an exception after SO_TIMEOUT
   * 
   * @see java.net.ServerSocket#setSoTimeout(int)
   */
  public void setSoTimeout(int timeout) throws SocketException
  {
    ss.setSoTimeout(timeout);
    soTimeout = timeout;
  }

  /**
   * @see java.net.ServerSocket#getSoTimeout()
   */
  public int getSoTimeout() throws IOException
  {
    return soTimeout;
  }

  /**
   * mSockets currently do not support timeouts. This method systematically
   * throws a SocketException.
   * 
   * @see java.net.ServerSocket#setReuseAddress(boolean)
   */
  public void setReuseAddress(boolean on) throws SocketException
  {
    throw new SocketException("setReuseAddress is not supported by mSocket");
  }

  /**
   * mSockets currently do not support reuse address. This method systematically
   * returns false.
   * 
   * @see java.net.ServerSocket#getReuseAddress()
   */
  public boolean getReuseAddress() throws SocketException
  {
    return false;
  }

  /**
   * @see java.net.ServerSocket#toString()
   */
  public String toString()
  {
    return this.toString();
  }

  /**
   * @see java.net.ServerSocket#setReceiveBufferSize(int)
   */
  public void setReceiveBufferSize(int size) throws SocketException
  {
    ss.setReceiveBufferSize(size);
  }

  /**
   * @see java.net.ServerSocket#getReceiveBufferSize()
   */
  public int getReceiveBufferSize() throws SocketException
  {
    return ss.getReceiveBufferSize();
  }

  /**
   * @see java.net.ServerSocket#setPerformancePreferences(int, int, int)
   */
  public void setPerformancePreferences(int connectionTime, int latency, int bandwidth)
  {
    ss.setPerformancePreferences(connectionTime, latency, bandwidth);
  }

  //
  // mSocket specific methods
  //

  /**
   * Returns the server alias, if it is set.
   * 
   * @return
   */
  public String getServerName()
  {
    return serverAlias;
  }

  /**
   * Returns the proxySelection value.
   * 
   * @return Returns the proxySelection.
   */
  public ProxySelectionPolicy getProxySelection()
  {
    return proxySelection;
  }

  /**
   * Migrates the server's listening address and also migrates all the already
   * accepted connections using the current proxy selection policy.
   * 
   * @param localAddress new local address to bind the listening socket
   * @param localPort new local port to bind the listening socket
   * @throws Exception if no proxy can be found or the migration failed
   */
  public void migrate(InetAddress localAddress, int localPort) throws IOException
  {
    migrate(localAddress, localPort, proxySelection);
  }

  /**
   * Migrates the server's listening address and also migrates all the already
   * accepted connections
   * 
   * @param localAddress new local address to bind the listening socket
   * @param localPort new local port to bind the listening socket
   * @param proxySelectionPolicy the proxy selection policy to use when
   *          migrating the sockets
   * @throws Exception
   */
  public void migrate(InetAddress localAddress, int localPort, ProxySelectionPolicy proxySelectionPolicy)
      throws IOException
  {
    /*if (isServerBehindNAT())
    {
      if (!proxySelectionPolicy.hasAvailableProxies())
        throw new SocketException(
            "Cannot migrate MServerSocket with no available proxy behind a NAT. Select a different proxy policy.");
    }*/

    synchronized (migrateMonitor)
    {
      ssc.close();
      ss.close();

      if (this.proxySelection.hasAvailableProxies())
      {
        Vector<ProxyInfo> vect = new Vector<ProxyInfo>();
        
        vect.addAll( controller.getAllProxyInfo());
        
        for (int i = 0; i < vect.size(); i++)
        {
          ProxyInfo Obj = vect.get(i);
          log.trace("removing proxy " + Obj.getProxyInfo());
          try
          {
        	  controller.getProxyConnObj().removeProxy(Obj.getProxyInfo(), Obj);
          } catch(Exception ex)
          {
        	  ex.printStackTrace();
          }
        }
      }

      controller.closeChildren();

      ssc = ServerSocketChannel.open();
      ss = ssc.socket();
      ss.bind(new InetSocketAddress(localAddress, localPort));
      serverListeningPort = ss.getLocalPort();

      int UDPPort = controller.renewControlSocket(localAddress);

      AcceptThreadPoolObj.StopAcceptPool();
      AcceptThreadPoolObj = new AcceptThreadPool(ssc);
      (new Thread(AcceptThreadPoolObj)).start();

      if (proxySelection.hasAvailableProxies())
      {
        List<InetSocketAddress> proxyVector = Integration.getNewProxy(proxySelectionPolicy);

        Integration.unregisterWithGNS(serverAlias);

        for (Iterator<InetSocketAddress> iterator = proxyVector.iterator(); iterator.hasNext();)
        {
          InetSocketAddress retProxy = iterator.next();
          Integration.registerWithGNS(serverAlias, retProxy);

          ProxyInfo proxyInfo = new ProxyInfo(retProxy.getHostName(), retProxy.getPort());
          // just setting it to current time
          proxyInfo.setLastKeepAlive(controller.getLocalClock());
          proxyInfo.setActive(true);
          controller.getProxyConnObj().addProxy(proxyInfo.getProxyInfo(), proxyInfo);
        }
      }
      else
      // No proxy available
      {
        if(!serverAlias.equals(""))
	      {
		      try
		      {
		    	  Integration.registerWithGNS(serverAlias, (InetSocketAddress) ss.getLocalSocketAddress());
		      }
		      catch (Exception ex)
		      {
		        log.trace("registration with GNS failed "+ex);
		        //ex.printStackTrace();
		      }
	      }
        
        controller.initMigrateChildren(localAddress, serverListeningPort, UDPPort);
      }

      log.trace("MServerSocket new UDP port of server " + UDPPort);
    }
  }
  
  //
  // private methods
  //
  private void initializeServerSocket(String serverName, ProxySelectionPolicy proxyPolicy
		  , SocketAddress endpoint, int backlog) throws IOException
	{
	    this.serverAlias = serverName;
	    proxySelection = proxyPolicy;

	    // Find out which IP to bind if we were not provided with one
	    if (endpoint == null)
	    {
	      InetAddress serverAddr = CommonMethods.returnLocalPublicIP();
	      endpoint = new InetSocketAddress(serverAddr, 0);
	      /*if (isServerBehindNAT())
	      {
	        if (!proxySelection.hasAvailableProxies())
	          throw new SocketException(
	              "Cannot create MServerSocket with no available proxy behind a NAT. Select a different proxy policy.");
	      }*/
	    }

	    // Create the underlying server socket
	    ssc = ServerSocketChannel.open();
	    ss = ssc.socket();
	    ss.bind(endpoint, backlog);
	    serverListeningPort = ss.getLocalPort();

	    if (controller == null)
	    { // enabling UDP control socket
	      controller = new MServerSocketController(this);
	      // moving thread here
	      (new Thread(controller)).start();
	      controller.startKeepAliveThread();
	    }

	    //controller.setGNSCredential(gnsCredentials);

	    List<InetSocketAddress> proxyVector = Integration.getNewProxy(proxyPolicy);
	    
	    
	    if (proxyVector != null)
	    {
	    	try 
	    	{
	    		Integration.unregisterWithGNS(serverAlias);
	    	}
	    	catch(IOException ex)
	    	{
	    			log.trace("Unregister failed, contuining to register");
	    			ex.printStackTrace();
	    	}
	      boolean firstTime = true;

	      for (Iterator<InetSocketAddress> iterator = proxyVector.iterator(); iterator.hasNext();)
	      {
	        InetSocketAddress retProxy = iterator.next();
	        Integration.registerWithGNS(serverAlias, retProxy);
	        
	        if(firstTime)
	        {
	        	serverGUID = Integration.getGUIDOfAlias(serverAlias);
	            controller.setProxyConnObj(new ConnectionToProxyServer(serverGUID, controller));
	            firstTime = false;
	        }
	        
	        ProxyInfo proxyInfo = new ProxyInfo(retProxy.getHostName(), retProxy.getPort());
	        log.info("proxy host name "+retProxy.getHostName() +" port "+retProxy.getPort());
	        proxyInfo.setActive(true);
	        controller.getProxyConnObj().addProxy(proxyInfo.getProxyInfo(), proxyInfo);
	      }
	    }
	    else
	    // We have no proxy
	    {
	      // application has not opened it with server name
	      if(!serverAlias.equals(""))
	      {
		      try
		      {
		        Integration.registerWithGNS(serverAlias, (InetSocketAddress) ss.getLocalSocketAddress());
		      }
		      catch (Exception ex)
		      {
		        System.err.println("registration with GNS failed "+ex);
		        //ex.printStackTrace();
		      }
	      }
	    }
	    MobilityManagerServer.registerWithManager(controller);
	    
	    AcceptConnectionQueueObj = new AcceptConnectionQueue();
	    AcceptThreadPoolObj = new AcceptThreadPool(ssc);

	    (new Thread(AcceptThreadPoolObj)).start();
	    
	    isBound = true;
	}

  private void BlockForAccept()
  {
    log.trace("accept called");
    synchronized (monitor)
    {
      while ((Integer) AcceptConnectionQueueObj.getFromQueue(AcceptConnectionQueue.GET_SIZE, null) == 0)
      {
        try
        {
          monitor.wait();
        }
        catch (InterruptedException e)
        {
          e.printStackTrace();
        }
      }
    }
    log.trace("new connection socket ready");
  }

  private void TimeOutWaitForaccept() throws InterruptedException, TimeoutException, ExecutionException
  {
    ExecutorService service = Executors.newSingleThreadExecutor();

    try
    {
      Runnable r = new Runnable()
      {
        @Override
        public void run()
        {
          BlockForAccept();
        }
      };
      Future<?> f = service.submit(r);
      f.get(soTimeout, TimeUnit.MILLISECONDS); // attempt the task for two
                                               // minutes
    }

    finally
    {
      service.shutdown();
    }
  }

  /**
   * for implementing executor service
   * 
   * @author ayadav
   */
  private class Handler implements Runnable
  {
    private final SocketChannel connectionSocketChannel;
    private final MSocket proxyMSocket;

    Handler(SocketChannel connectionSocketChannel, MSocket proxyMSocket)
    {
      this.connectionSocketChannel = connectionSocketChannel;
      this.proxyMSocket = proxyMSocket;
    }

    public void run()
    {
    	if (!proxySelection.hasAvailableProxies())
    	{
    		// read and service request on socket
    		// FIXME: check for how to handle exceptions here
    		log.trace("new connection accepted by socket channel");

    		InternalMSocket ms = null;
    		try
    		{
    			ms = new InternalMSocket(connectionSocketChannel, controller, null);
    		}
    		catch (IOException e)
    		{
    			e.printStackTrace();
    			// FIXME: exception for newly accepted socket
    			// close and reject socket so that client reconnects again
    			// do not put in active queue as currently done
    			// transition into all ready state as well
    			log.warn("Failed to accept new connection", e);
    			return;
    		}

    		log.info("Accepted connection from " + ms.getInetAddress() + ":" + ms.getPort());
		      if (ms.isNew())
		      {
		
		        // FIXME: what to do here for exception
		        controller.setConnectionInfo(ms);
		
		        AcceptConnectionQueueObj.getFromQueue(AcceptConnectionQueue.PUT, ms);
		        synchronized (monitor)
		        {
		          monitor.notifyAll();
		        }
		      }
		      log.trace("MServerSocket Handler thread exits");
    	} else
    	{
    		if(proxyMSocket != null)
    		{
    			AcceptConnectionQueueObj.getFromQueue(AcceptConnectionQueue.PUT, proxyMSocket);
		        synchronized (monitor)
		        {
		          monitor.notifyAll();
		        }
    		}
    	}
    }
  }

  /**
    *
    */
  private class AcceptThreadPool implements Runnable
  {
    private final ServerSocketChannel serverSocketChannel;
    private final ExecutorService     pool;
    private boolean                   runstatus = true;

    public AcceptThreadPool(ServerSocketChannel ssc)
    {
      serverSocketChannel = ssc;
      pool = Executors.newCachedThreadPool();
      // pool = Executors.newFixedThreadPool(THREAD_POOL_SIZE);
    }

    public void run()
    { // run the service
    	//FIXME: right now accept throughtput from proxy is 1 conn/2RTT.
    	// needs to be fixed later on, to do accept in executor service.
      while (runstatus)
      {
        try
        {
        	if (!proxySelection.hasAvailableProxies())
        	{
        		pool.execute(new Handler(serverSocketChannel.accept(), null));
        	}else
        	{
        		MSocket proxyMSocket = controller.getProxyConnObj().accept();
        		if(proxyMSocket!=null)
        		{
        			pool.execute(new Handler(null, proxyMSocket));
        		}
        	}
        }
        catch (IOException ex)
        {
          // pool.shutdown();
          // FIXME: correct here, shouldn't ignore the exception every time
          continue;
        }
      }
      log.trace("AcceptThreadPool exits");
    }

    public void StopAcceptPool()
    {
      runstatus = false;
      pool.shutdownNow();
    }
  }
  
}