/*
 * see license.txt
 */
package leola.web;

import java.io.File;
import java.net.URI;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.logging.Logger;


import javax.servlet.DispatcherType;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.websocket.server.ServerContainer;
import javax.websocket.server.ServerEndpointConfig;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;


import leola.vm.lib.LeolaIgnore;
import leola.vm.types.LeoInteger;
import leola.vm.types.LeoMap;
import leola.vm.types.LeoObject;
import leola.vm.types.LeoString;
import leola.web.RoutingTable.Route;


import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.handler.DefaultHandler;
import org.eclipse.jetty.server.handler.HandlerList;
import org.eclipse.jetty.server.handler.ResourceHandler;
import org.eclipse.jetty.servlet.FilterHolder;
import org.eclipse.jetty.servlet.ServletHolder;
import org.eclipse.jetty.servlets.MultiPartFilter;
import org.eclipse.jetty.webapp.WebAppContext;
import org.eclipse.jetty.websocket.jsr356.server.BasicServerEndpointConfig;
import org.eclipse.jetty.websocket.jsr356.server.deploy.WebSocketServerContainerInitializer;

/**
 * A {@link WebApp} is an instance of a web application. A {@link WebApp} contains
 * web {@link Route}s and our bound to {@link URI} and are executed to a corresponding {@link LeoObject} function.
 * 
 * @author Tony
 *
 */
public class WebApp {
        
    /**
     * The routing table
     */
    private RoutingTable routes;
    
    /**
     * The embedded web server
     */
    private Server server;
    
    /**
     * The application configuration
     */
    private LeoMap config;
    
    private Optional<LeoObject> errorHandler;
    private Optional<LeoObject> contextHandler;
    
    private List<ServerEndpointConfig> webSocketConfigs;
    
    /**
     * The supplied configuration should have properties:
     * 
     * <pre>
     *   {
     *      resourceBase -> "", // a String that denotes the home directory of where to look for html/css/javascript files
     *      context -> "", // a String that denotes the context of the web application: http://localhost:8121/context
     *      port -> 8181, // an Integer that denotes the port number the web server should use
     *   }
     * </pre>
     * 
     * @param suppliedConfig
     */
    public WebApp(LeoMap suppliedConfig) {
        this.routes = new RoutingTable();
        this.errorHandler = Optional.empty();
        this.contextHandler = Optional.empty();
        
        this.webSocketConfigs = new ArrayList<ServerEndpointConfig>();
        
        Runtime.getRuntime().addShutdownHook(new Thread() {
            public void run() {
                try {
                    shutdown();
                } catch (Exception e) {
                }
            }
        });
        
        if(suppliedConfig == null) {
            config = new LeoMap();
            config.putByString("resourceBase", LeoString.valueOf("/"));
            config.putByString("context", LeoString.valueOf(""));
            config.putByString("root", LeoString.valueOf("/"));
            config.putByString("welcomeFile", LeoString.valueOf("index.html"));
            config.putByString("port", LeoInteger.valueOf(8556));
        }
        else {
            config = suppliedConfig;
        }
    }

    /**
     * @return the root directory as to where the web application is installed.  In general, this is used
     * for various modules to start looking for files
     */
    public File getRootDirectory() {
        return new File(config.getString("resourceBase"));
    }
    
    /**
     * Binds a {@link URI} with the supplied {@link LeoObject} function.
     * 
     * @param config
     * @param function
     * @return the supplied function so that it can be assigned in the Leola script
     */
    public LeoObject route(LeoMap config, LeoObject function) {
       this.routes.addRoute(new Route(config, function)); 
       return function;
    }
    
    @LeolaIgnore
    public Optional<Route> getRoute(HttpServletRequest request) {
        return this.routes.getRoute(request.getMethod(), request.getRequestURI());
    }
    
    
    /**
     * Bind an error handler function.  The supplied function will be invoked whenever an Exception or
     * error occurs during a Routing request.
     * 
     * @param function
     * @return the supplied function
     */
    public LeoObject errorHandler(LeoObject function) {
        this.errorHandler = Optional.ofNullable(function);
        return function;
    }
    
    
    /**
     * Bind a context handler function.  The supplied function will be invoked after the creation of a 
     * {@link RequestContext}.  This allows the application to inject specific properties into the
     * request.
     * 
     * 
     * @param function
     * @return the supplied function
     */
    public LeoObject contextHandler(LeoObject function) {
        this.contextHandler = Optional.ofNullable(function);
        return function;
    }
    
    
    /**
     * Handles an exception and/or error during a request.  This will attempt to delegate the
     * handling of the error to the registered errorHandler.
     * 
     * @see WebApp#errorHandler
     * @param requestContext
     * @param exception
     * @return the {@link WebResponse} generated from the error handler
     */
    @LeolaIgnore
    public WebResponse handleException(LeoObject requestContext, Object exception) {
        return this.errorHandler.map(function -> {
            LeoObject result = function.call(requestContext, LeoObject.valueOf(exception));
            if(result.isError()) {
                return new WebResponse(result, Status.INTERNAL_SERVER_ERROR);
            }
            
            return (WebResponse) result.getValue(WebResponse.class);
        }).orElse(new WebResponse(Response.Status.INTERNAL_SERVER_ERROR));
    }
    
    /**
     * Builds the {@link RequestContext} off of the supplied request/response objects.
     * This will apply the registered contextHandler function if there is one.
     * 
     * @param req
     * @param resp
     * @return the {@link RequestContext} as a {@link LeoObject}
     */
    @LeolaIgnore
    public LeoObject buildContext(Route route, HttpServletRequest req, HttpServletResponse resp) {

        Map<String, String> pathValues = route.getRouteParameters(req.getRequestURI());
        LeoMap pathParams = LeoMap.toMap(pathValues);   
        
        RequestContext context = new RequestContext(req, resp, this, pathParams);
        LeoObject leoContext = LeoObject.valueOf(context);
        this.contextHandler.ifPresent(function -> function.call(leoContext));
        return leoContext;
    }
    
    /**
     * Bind a websocket server endpoint with the supplied configuration.
     * 
     * <p>
     * The supplied configuration should have the following properties:
     * 
     * <pre>
     *   {
     *     route -> "", // A String that denotes the path of the websocket (ex. "/socketTest", could map to: ws://localhost:8121/context/socketTest)
     *     onOpen -> def(session) {}, // a Function that will be invoked when a WebSocket is connected to the server
     *     onClose -> def(session, reason) {}, // a Function that will be invoked when a WebSocket is disconnected from the server
     *     onMessage -> def(session, message) {}, // a Function that will be invoked when a message comes in on a WebSocket
     *     onError -> def(session, exception) {}, // a Function that will be invoked when a problem occurs on a WebSocket
     *   }
     * </pre>
     * 
     * @param config
     * @return the passed in configuration
     */
    public LeoObject webSocket(LeoMap config) {
        ServerEndpointConfig c = new BasicServerEndpointConfig(WebSocketServerEndpoint.class, config.getString("route"));
        if(config.containsKeyByString(WebSocketServerEndpoint.ON_OPEN_KEY)) {
            c.getUserProperties().put(WebSocketServerEndpoint.ON_OPEN_KEY, config.getByString(WebSocketServerEndpoint.ON_OPEN_KEY));
        }
        if(config.containsKeyByString(WebSocketServerEndpoint.ON_CLOSE_KEY)) {
            c.getUserProperties().put(WebSocketServerEndpoint.ON_CLOSE_KEY, config.getByString(WebSocketServerEndpoint.ON_CLOSE_KEY));
        }
        if(config.containsKeyByString(WebSocketServerEndpoint.ON_MESSAGE_KEY)) {
            c.getUserProperties().put(WebSocketServerEndpoint.ON_MESSAGE_KEY, config.getByString(WebSocketServerEndpoint.ON_MESSAGE_KEY));
        }
        if(config.containsKeyByString(WebSocketServerEndpoint.ON_ERROR_KEY)) {
            c.getUserProperties().put(WebSocketServerEndpoint.ON_ERROR_KEY, config.getByString(WebSocketServerEndpoint.ON_ERROR_KEY));
        }
        
        this.webSocketConfigs.add(c);
        
        return config;
    }
    
    /**
     * Get the {@link Logger}
     * 
     * @param name
     * @return the {@link Logger}
     */
    public Logger logger(String name) {
        return Logger.getLogger(name);
    }
    
    
    /**
     * Start the application
     * 
     * @throws Exception
     */
    public void start() throws Exception {        
        String resourceBase = config.getString("resourceBase");
        String context = config.getString("context");
        String root = config.getString("root");
        String welcomeFile = config.getString("welcomeFile");
        int port = config.getInt("port");
        
        WebAppContext servletContext = new WebAppContext();
        servletContext.setInitParameter("org.eclipse.jetty.servlet.Default.useFileMappedBuffer", "false");
        servletContext.setContextPath("/" + context );
        servletContext.setResourceBase( resourceBase );
        
        ServletHolder leolaServlet = new ServletHolder(new WebServlet(this));
        servletContext.addServlet(leolaServlet, "/*" );

        
        // Filter for multipart 
        FilterHolder filterHolder = new FilterHolder(new MultiPartFilter());
        filterHolder.setInitParameter("deleteFiles", "true");
        servletContext.addFilter(filterHolder, "/" + root, EnumSet.allOf(DispatcherType.class));
                            
        servletContext.setWelcomeFiles(new String[] { welcomeFile });
        
        
        ResourceHandler resourceContext = new ResourceHandler();
        resourceContext.setResourceBase(resourceBase);
        resourceContext.setDirectoriesListed(true);
        
                
        HandlerList handlers = new HandlerList();
        handlers.addHandler(servletContext);
        handlers.addHandler(resourceContext);               
        handlers.addHandler(new DefaultHandler());
        
        this.server = new Server(port);
        this.server.setHandler(handlers);


        
        /* This adds the example web socket server endpoint         
         */
        if(!this.webSocketConfigs.isEmpty()) {
            ServerContainer wscontainer = WebSocketServerContainerInitializer.configureContext(servletContext);
            this.webSocketConfigs.forEach(config -> {
                try {
                    wscontainer.addEndpoint(config);
                }
                catch (Exception e) {
                    throw new RuntimeException(e);
                }
            });
        }

        this.server.start();
        this.server.join();
    }
    
    /**
     * Shutdown the web app
     * 
     * @throws Exception
     */
    public void shutdown() throws Exception {
        this.webSocketConfigs.clear();
        
        if ( this.server != null) {
            try {
                this.server.stop();
            }
            finally {
                this.server.destroy();
            }
        }
    }
}
