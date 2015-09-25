/*
 * see license.txt
 */
package leola.web;

import java.util.List;
import java.util.Optional;

import leola.vm.types.LeoMap;

import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.websocket.server.WebSocketUpgradeFilter;
import org.eclipse.jetty.websocket.server.pathmap.ServletPathSpec;
import org.eclipse.jetty.websocket.servlet.ServletUpgradeRequest;
import org.eclipse.jetty.websocket.servlet.ServletUpgradeResponse;
import org.eclipse.jetty.websocket.servlet.WebSocketCreator;

/**
 * A factory for craeting {@link LeolaWebSocket}'s that contain the Leola configuration (which includes
 * the web socket route and callback functions for the web socket).
 * 
 * @see LeolaWebSocket
 * @author Tony
 *
 */
public class LeolaWebSocketCreator implements WebSocketCreator {

    /**
     * Configures the {@link ServletContextHandler} to enable WebSocket capabilities.  This will map to a{@link WebSocketCreator}'s for the registered
     * Web Socket configurations
     * 
     * @param webSocketServletContextHandler the {@link ServletContextHandler} to configure to use web-sockets
     * @param socketConfigs the web socket configurations defined in the Leola scripts
     */
    public static void configureWebsocketContextHandler(ServletContextHandler webSocketServletContextHandler, List<LeoMap> socketConfigs) throws Exception {
        if(!socketConfigs.isEmpty()) {
        
            WebSocketUpgradeFilter webSocketUpgradeFilter = WebSocketUpgradeFilter.configureContext(webSocketServletContextHandler);
            webSocketUpgradeFilter.getFactory().getPolicy().setIdleTimeout(0);
            
            socketConfigs.forEach( config -> {
                webSocketUpgradeFilter.addMapping(new ServletPathSpec(config.getString("route")), new LeolaWebSocketCreator(config));
            });
        }
        
    }
    
    
    /**
     * Create the {@link ServletContextHandler} that contains the {@link WebSocketCreator}'s for the registered
     * Web Socket configurations
     * 
     * @param socketConfigs the web socket configurations defined in the Leola scripts
     * @return the {@link ServletContextHandler} if there are configurations
     */
    public static Optional<ServletContextHandler> createContextHandler(Optional<ServletContextHandler> handler, List<LeoMap> socketConfigs) throws Exception {
        ServletContextHandler webSocketServletContextHandler = null;
        if(!socketConfigs.isEmpty()) {
            webSocketServletContextHandler = handler.isPresent() ? handler.get() : new ServletContextHandler(null, "/", true, false);
            WebSocketUpgradeFilter webSocketUpgradeFilter = WebSocketUpgradeFilter.configureContext(webSocketServletContextHandler);
            webSocketUpgradeFilter.getFactory().getPolicy().setIdleTimeout(0);
            
            socketConfigs.forEach( config -> {
                webSocketUpgradeFilter.addMapping(new ServletPathSpec(config.getString("route")), new LeolaWebSocketCreator(config));
            });
        }
        
        return Optional.ofNullable(webSocketServletContextHandler);
    }
    
    private LeoMap config;
    
    /**
     * @param config
     */
    public LeolaWebSocketCreator(LeoMap config) {
        this.config = config;
    }
    
    @Override
    public Object createWebSocket(ServletUpgradeRequest req, ServletUpgradeResponse resp) {        
        return new LeolaWebSocket(this.config);
    }

}
