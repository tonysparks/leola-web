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
import java.util.function.Function;
import java.util.logging.Logger;

import javax.servlet.DispatcherType;
import javax.servlet.MultipartConfigElement;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import leola.frontend.listener.EventDispatcher;
import leola.vm.Leola;
import leola.vm.lib.LeolaIgnore;
import leola.vm.types.LeoInteger;
import leola.vm.types.LeoLong;
import leola.vm.types.LeoMap;
import leola.vm.types.LeoObject;
import leola.vm.types.LeoString;
import leola.vm.types.LeoUserFunction;
import leola.web.RoutingTable.Route;
import leola.web.filewatcher.FileModifiedEvent;
import leola.web.filewatcher.FileModifiedEvent.ModificationType;
import leola.web.filewatcher.FileModifiedListener;
import leola.web.filewatcher.FileWatcher;

import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.handler.DefaultHandler;
import org.eclipse.jetty.server.handler.HandlerList;
import org.eclipse.jetty.server.handler.ResourceHandler;
import org.eclipse.jetty.servlet.FilterHolder;
import org.eclipse.jetty.servlet.ServletHolder;
import org.eclipse.jetty.webapp.WebAppContext;

/**
 * A {@link WebApp} is an instance of a web application. A {@link WebApp} contains
 * web {@link Route}s and our bound to {@link URI} and are executed to a corresponding {@link LeoObject} function.
 * 
 * @author Tony
 *
 */
public class WebApp {
        
    /**
     * The bounded Leola runtime
     */
    private Leola runtime;
    
    
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
    private Optional<LeoObject> shutdownHandler;
    
    private List<LeoMap> webSocketConfigs;
    private List<WebFilter> filters;
    
    /**
     * For Auto-Reload enabled applications, this will
     * watch the Resource Directory for any leola scripts to
     * be reloaded
     */
    private FileWatcher fileWatcher;
        
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
     * @param runtime
     * @param suppliedConfig
     */
    public WebApp(final Leola runtime, LeoMap suppliedConfig) {    
        this.routes = new RoutingTable();
        this.errorHandler = Optional.empty();
        this.contextHandler = Optional.empty();
        this.shutdownHandler = Optional.empty();                
        
        this.webSocketConfigs = new ArrayList<LeoMap>();
        this.filters = new ArrayList<WebFilter>();
        
        Runtime.getRuntime().addShutdownHook(new Thread() {
            public void run() {
                try {
                    shutdown();
                } catch (Exception e) {
                }
            }
        });
        
        config = (suppliedConfig==null) ? new LeoMap() : suppliedConfig;
        
        if(!config.containsKeyByString("resourceBase")) {
        	File executionScript = runtime.getExecutionScript();
        	if(executionScript != null) {
        		config.putByString("resourceBase", LeoString.valueOf(executionScript.getParentFile().getAbsolutePath()));
        	}
        	else {
        		config.putByString("resourceBase", LeoString.valueOf(runtime.getWorkingDirectory()));
        	}
        }
        if(!config.containsKeyByString("context")) 
            config.putByString("context", LeoString.valueOf(""));
                
        if(!config.containsKeyByString("welcomeFile")) 
            config.putByString("welcomeFile", LeoString.valueOf("index.html"));
        
        if(!config.containsKeyByString("port")) 
            config.putByString("port", LeoInteger.valueOf(8121));
        
        if(!config.containsKeyByString("multiPart")) {
            LeoMap multipart = new LeoMap();
            config.putByString("multiPart", multipart);
        }
        
        LeoMap multipart = config.getByString("multiPart").as();
        if(!multipart.containsKeyByString("maxFileSize")) {
            multipart.putByString("maxFileSize", LeoLong.valueOf(1024 * 1024 * 100));
        }
        if(!multipart.containsKeyByString("maxRequestSize")) {
            multipart.putByString("maxRequestSize", LeoLong.valueOf(1024 * 1024 * 200));
        }
        
        initializeWatcher(runtime);
    }

    
    /**
     * Initializes the {@link FileWatcher} if enabled.
     * 
     * @param runtime
     */
    private void initializeWatcher(final Leola runtime) {
        EventDispatcher eventDispatcher = new EventDispatcher();
        
        final File executionScript = runtime.getExecutionScript() != null ? 
                                        runtime.getExecutionScript() : 
                                        runtime.getWorkingDirectory();
        
        
        this.fileWatcher = new FileWatcher(eventDispatcher, getRootDirectory(), executionScript.getParentFile());
        
        /* If we have autoReload enabled, let's go ahead and register
         * a FileModificationListener so that we can reload our application
         */
        if(config.containsKeyByString("autoReload") && runtime.getExecutionScript() != null ) {
            
            if(LeoObject.isTrue(config.getByString("autoReload"))) {
                eventDispatcher.addEventListener(FileModifiedEvent.class, new FileModifiedListener() {
                    
                    @Override
                    public void onFileModified(FileModifiedEvent event) {                        
                        if( event.getModType().equals(ModificationType.MODIFIED)) {

                            File file = event.getFile();
                            if(file.isFile() && file.getName().toLowerCase().endsWith(".leola")) {
                                try {
                                    System.out.println("Restarting the server.");
                                    
                                    System.out.println("Shutdown of current server.");
                                    shutdown();
                                    System.out.println("Shutdown complete.");
                                    
                                    System.out.println("Starting new server.");
                                    
                                    /* Spawn in a new thread so that we actually
                                     * return from this function call -- the executionScript
                                     * blocks because it spawns the web-server
                                     */
                                    new Thread( () -> {
                                        try {
                                            LeoObject result = runtime.eval(executionScript);
                                            if(result.isError()) {
                                                System.out.println("Error restarting: " + result);
                                            }    
                                        }
                                        catch(Exception e) {
                                            e.printStackTrace();
                                        }
                                    }, "leola-web-app-thread").start();
                                }
                                catch (Exception e) {
                                    e.printStackTrace();
                                }
                            }
                        }
                    }
                });
                this.fileWatcher.startWatching();                
            }
        }
    }
    
    /**
     * @return the {@link Leola} runtime bound to this {@link WebApp}
     */
    public Leola getRuntime() {
        return this.runtime;
    }
    
    /**
     * @see WebApp#getRootDirectory()
     * @return the root directory's absolute path
     */
    public String rootDir() {
        return getRootDirectory().getAbsolutePath();
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
    
    /**
     * Binds a {@link URI} with the supplied {@link LeoObject} function.
     * 
     * @param path the route path
     * @param function the function to run for the route
     * @return the {@link LeoObject} representation of the supplied function
     */
    @LeolaIgnore
    public LeoObject route(String path, Function<RequestContext, WebResponse> function) {
        LeoMap config = new LeoMap();
        config.putByString("path", LeoString.valueOf(path));
        return route(config, new LeoUserFunction() {
           
            @Override
            public LeoObject call(LeoObject[] args) {
                return LeoObject.valueOf(function.apply((RequestContext) args[0].getValue(RequestContext.class)));
            } 
        });
    }
    
    
    @LeolaIgnore
    public Optional<Route> getRoute(HttpServletRequest request) {
        return this.routes.getRoute(request.getMethod(), request.getRequestURI());
    }
    
    /**
     * Bind a shutdown handler function.  The supplied function will be invoked when the system shuts down,
     * this allows the application code to safely free and close opened resources.
     * 
     * @param function
     * @return the supplied function
     */
    public LeoObject shutdownHandler(LeoObject function) {
        this.shutdownHandler = Optional.ofNullable(function);
        return function;
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
                return new WebResponse(result, HttpStatus.INTERNAL_SERVER_ERROR);
            }
            
            return (WebResponse) result.getValue(WebResponse.class);
        }).orElse(new WebResponse(HttpStatus.INTERNAL_SERVER_ERROR));
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
        this.webSocketConfigs.add(config);
        return config;
    }
    
    /**
     * Add's a {@link WebFilter} to this server's configuration.
     * 
     * @param config
     * @param function
     * @return the passed in configuration
     */
    public LeoObject filter(LeoMap config, LeoObject function) {
        this.filters.add(new WebFilter(this, config, function));
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
     * @return this {@link WebApp} configuration
     */
    public LeoMap config() {
        return this.config;
    }
    
    /**
     * Start the application
     * 
     * @throws Exception
     */
    public void start() throws Exception {      
        if(this.server == null || this.server.isStopped()) {
            String resourceBase = config.getString("resourceBase");
            String context = config.getString("context");
            String welcomeFile = config.getString("welcomeFile");
            int port = config.getInt("port");
            
            
            // Handles the servlet requests (routes)
            WebAppContext servletContext = new WebAppContext();
            servletContext.setInitParameter("org.eclipse.jetty.servlet.Default.useFileMappedBuffer", "false");
            servletContext.setContextPath("/" + context );
            servletContext.setResourceBase( resourceBase );
            servletContext.setWelcomeFiles(new String[] { welcomeFile });
            
            this.filters.forEach(filter -> {            
                FilterHolder holder = new FilterHolder(filter);                 
                servletContext.addFilter(holder, filter.getPathSpec(), EnumSet.allOf(DispatcherType.class));
            });
            
            
            WebServlet webServlet = new WebServlet(this);
            ServletHolder leolaServlet = new ServletHolder(webServlet);
            leolaServlet.getRegistration().setMultipartConfig(new MultipartConfigElement(webServlet.getMultipartConfig()));
            servletContext.addServlet(leolaServlet, "/*");            
            
            
            // Serves up Static content
            ResourceHandler resourceContext = new ResourceHandler();
            resourceContext.setResourceBase(resourceBase);
            resourceContext.setDirectoriesListed(true);
            
            
            // Handles Web Sockets (if there are any)
            LeolaWebSocketCreator.configureWebsocketContextHandler(servletContext, this.webSocketConfigs);
                                
            HandlerList handlers = new HandlerList();
            handlers.addHandler(resourceContext);      
            handlers.addHandler(servletContext);
                     
                        
            // generic 404 handler -- TODO: remove with
            // leola function callback
            handlers.addHandler(new DefaultHandler());
            
            
            this.server = new Server(port);
            this.server.setHandler(handlers);
        
            this.server.start();
            this.server.join();
        }
    }
    
    /**
     * Shutdown the web app
     * 
     * @throws Exception
     */
    public void shutdown() {        
        this.fileWatcher.stopWatching();
        this.webSocketConfigs.clear();
        
        try {
            this.shutdownHandler.ifPresent( function -> function.call() ); 
        }
        finally {
            /* This will always ensure that we terminate the server
             * if it is currently running, regardless of if the 
             * user supplied shutdown handler fails or not.
             */
            
            if (this.server != null && this.server.isRunning()) {
                try {
                    try { this.server.stop(); }
                    catch(Exception ignore) {}
                }
                finally {
                    this.server.destroy();
                }
            }
        }
    }
}
