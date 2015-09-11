/*
 * see license.txt
 */
package leola.web;

import java.io.IOException;
import java.io.InputStreamReader;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import leola.vm.lib.LeolaMethod;
import leola.vm.types.LeoArray;
import leola.vm.types.LeoMap;
import leola.vm.types.LeoNull;
import leola.vm.types.LeoObject;
import leola.vm.types.LeoString;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;

/**
 * The Request context contains all the information from a http request/response cycle.
 * 
 * @author Tony
 *
 */
public class RequestContext {

    private HttpServletRequest request;
    private HttpServletResponse response;
    private WebApp webapp;
    
    private LeoMap contents;
    private LeoMap pathParams;

    /**
     * @param request
     * @param response
     * @param webapp
     * @param pathParams
     */
    public RequestContext(HttpServletRequest request, HttpServletResponse response, WebApp webapp, LeoMap pathParams) {
        this.request = request;
        this.response = response;
        this.webapp = webapp;
        this.pathParams = pathParams;
        
        this.contents = new LeoMap();
    }

    /**
     * Allows to index into this object for a content in leola code
     * 
     * <pre>
     *   var x = context["x"]
     * </pre>
     *
     * @see RequestContext#content(String)
     * @param reference the key in which the contents is bound (key/value)
     * @return the contents bounded at the supplied reference
     */
    @LeolaMethod(alias="$index")
    public LeoObject get(String reference) {
        return content(reference);
    }
    
    
    /**
     * Allows to set a content by reference in leola code
     *
     * <pre>
     *   context["x"] = "timmah"
     * </pre>
     * 
     * @see RequestContext#setContent(String,LeoObject)
     * @param reference the key in which the contents is bound (key/value)
     * @param value the value to set the reference to
     */
    @LeolaMethod(alias="$sindex")
    public void set(String reference, LeoObject value) {
        this.contents.put(LeoString.valueOf(reference), value);
    }
    
    
    /**
     * @return the {@link HttpServletRequest}
     */
    public HttpServletRequest request() {
        return this.request;
    }
    
    
    /**
     * @return the {@link HttpServletResponse}
     */
    public HttpServletResponse response() {
        return this.response;
    }
    
    
    /**
     * Gets the body of the request as a JSON payload
     * 
     * @return the body of the request as a JSON payload, represented as a {@link LeoObject}
     * @throws IOException
     */
    public LeoObject json() throws IOException {
        Gson gson = new GsonBuilder().create();        
        JsonElement element = gson.fromJson(new InputStreamReader(this.request.getInputStream()), JsonElement.class);
        return toLeoObject(element);
    }
    
    
    /**
     * Converts the {@link JsonElement} into the equivalent {@link LeoObject}
     * 
     * @param element
     * @return the {@link LeoObject}
     */
    private LeoObject toLeoObject(JsonElement element) {
        if(element==null||element.isJsonNull()) {
            return LeoNull.LEONULL;
        }
        
        if(element.isJsonArray()) {
            JsonArray array = element.getAsJsonArray();
            LeoArray leoArray = new LeoArray(array.size());
            array.forEach(e -> leoArray.add(toLeoObject(e)));
            return leoArray;
        }
        
        if(element.isJsonObject()) {
            JsonObject object = element.getAsJsonObject();
            LeoMap leoMap = new LeoMap();
            object.entrySet().forEach( entry -> {
                leoMap.putByString(entry.getKey(), toLeoObject(entry.getValue()));
            });
            
            return leoMap;
        }
        
        if(element.isJsonPrimitive()) {
            JsonPrimitive primitive = element.getAsJsonPrimitive();
            if(primitive.isBoolean()) {
                return LeoObject.valueOf(primitive.getAsBoolean());
            }
            
            if(primitive.isNumber()) {
                return LeoObject.valueOf(primitive.getAsDouble());
            }
            
            if(primitive.isString()) {
                return LeoString.valueOf(primitive.getAsString());
            }
        }
        
        return LeoNull.LEONULL;
    }
    
    
    /**
     * The path parameter at the supplied name.
     * 
     * <pre>
     *   path = "/users/{userid}"
     *   requestPath = "/users/tonys"
     *   
     *   var name = request.pathParam("userid")
     *   println(name) // 'tonys'
     * </pre>
     * 
     * @param name
     * @return
     */
    public LeoObject pathParam(String name) {
        return this.pathParams.getByString(name);
    }
    
    
    /**
     * @return all of the path parameters in the format of: pathVariableName -> Value
     */
    public LeoMap pathParams() {
        return this.pathParams;
    }
        
    /**
     * Retrieve the content referenced by the supplied name
     * 
     * @see RequestContext#get(String)
     * @param reference
     * @return the content value stored by the referenced name
     */
    public LeoObject content(String reference) {
        return this.contents.getByString(reference);
    }
    
    /**
     * Binds the supplied reference content to the supplied value
     * 
     * @param reference the name of the content
     * @param value the value to bind to the reference
     */
    public void setContent(String reference, LeoObject value) {
        this.contents.putByString(reference, value);
    }
    
    
    /**
     * @see WebApp#buildContext(leola.web.RoutingTable.Route, HttpServletRequest, HttpServletResponse)
     * @return the contents {@link LeoMap} which binds references to values.  This can be used
     * to store custom data elements in a {@link RequestContext}.
     */
    public LeoMap contents() {
        return this.contents;
    }
    
    
    /**
     * @return the {@link WebApp} that this {@link RequestContext} was constructed from.
     */
    public WebApp webapp() {
        return this.webapp;
    }
}
