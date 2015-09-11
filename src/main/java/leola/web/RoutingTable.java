/*
 * see license.txt
 */
package leola.web;

import java.util.Collection;
import java.util.HashSet;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedDeque;

import leola.vm.types.LeoArray;
import leola.vm.types.LeoMap;
import leola.vm.types.LeoObject;

/**
 * A means for binding {@link LeoObject} functions to HTTP requests.  The {@link Route}s expect a path (which may contain path variables) and
 * a list of valid method request types (i.e., GET, POST, etc.).  When a request comes to the web server, it will use this RoutingTable to determine
 * which {@link Route} to use.
 * 
 * @author Tony
 *
 */
public class RoutingTable {

    /**
     * A route into the web server
     * 
     * @author Tony
     *
     */
    public static class Route {
        private LeoMap config;
        private LeoObject function;
        
        private UriTemplate template;
        private Set<String> methodTypes;
        
        /**
         * @param config
         * @param function
         */
        public Route(LeoMap config, LeoObject function) {
            this.config = config;
            this.function = function;
            
            if(!config.containsKeyByString("path")) {
                throw new IllegalArgumentException("The supplied route does not specify a 'path' : " + config);
            }
            
            String path = config.getString("path");
            this.template = new UriTemplate(path);
            
            this.methodTypes = new HashSet<String>();
            if(config.containsKeyByString("methods")) {
                LeoObject methods = config.getByString("methods");
                if(methods.isArray()) {
                    LeoArray array = methods.as();
                    array.forEach(method -> methodTypes.add(method.toString()));
                }
            }
            
            if(this.methodTypes.isEmpty()) {
                this.methodTypes.add("GET");
            }
            
        }
        
        /**
         * @return the config
         */
        public LeoMap getConfig() {
            return config;
        }
        
        /**
         * @return the function
         */
        public LeoObject getFunction() {
            return function;
        }
        
        public UriTemplate getUriTemplate() {                        
            return this.template;
        }
        
        /**
         * The route parameters from the supplied path
         * @param path
         * @return the key/value pair where the key is the name of the variable name, and the value is what was
         * in place in the supplied path
         */
        public Map<String, String> getRouteParameters(String path) {
            return this.template.match(path);
        }
        
        /**
         * Determines if the supplied method type is supported by this route.
         * 
         * @param methodType
         * @return true if and only if the supplied method type is supported
         */
        public boolean isMethodTypeSupported(String methodType) {
            return this.methodTypes.contains(methodType.toUpperCase());
        }
    }
    
    private Map<UriTemplate, Route> routes;
    private Collection<UriTemplate> routePatterns;
    
    /**
     * 
     */
    public RoutingTable() {
        this.routes = new ConcurrentHashMap<UriTemplate, RoutingTable.Route>();
        this.routePatterns = new ConcurrentLinkedDeque<UriTemplate>();
    }

    /**
     * Adds the {@link Route} to the routing table
     * 
     * @param route
     */
    public void addRoute(Route route) {
        UriTemplate pattern = route.getUriTemplate();
        this.routes.put(pattern, route);
        this.routePatterns.add(pattern);
    }

    /**
     * Attempts to find the best route path for the supplied request path
     * @param requestPath
     * @return the best pattern that matches the request path
     */
    private Optional<UriTemplate> findBestRoute(String methodType, String requestPath) {
        return this.routePatterns.stream()
                .filter(p -> p.matches(requestPath))
                .filter(p -> {
                    Route r = routes.get(p);
                    if(r!=null) return r.isMethodTypeSupported(methodType);
                    return false;
                })
                .findFirst();
    }
    
    /**
     * Attempts to retrieve the {@link Route} bounded to the supplied
     * path.
     * 
     * @param path
     * @return the Route if present
     */
    public Optional<Route> getRoute(String methodType, String path) {
        return findBestRoute(methodType, path).map(r -> this.routes.get(r));
    }
}
