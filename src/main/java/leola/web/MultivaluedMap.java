/*
 * see license.txt
 */
package leola.web;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;

/**
 * Very simple implementation of a Multi Valued Map
 * 
 * @author Tony
 *
 */
public class MultivaluedMap extends HashMap<String, List<String>> {

    /**
     * SUID
     */
    private static final long serialVersionUID = 528400173846253754L;

    public MultivaluedMap() {         
    }

    public MultivaluedMap(MultivaluedMap that) {
        that.forEach( (key, values) -> {
            put(key, values);
        });        
    }

    public final void putSingle(String key, String value) {
        List<String> l = getValueList(key);
        l.clear();

        l.add(value != null ? value : "");
    }

    public final void add(String key, String value) {
        List<String> l = getValueList(key);
        l.add(value != null ? value : "");
    }

    
    public final String getFirst(String key) {
        List<String> values = get(key);
        if (values != null && values.size() > 0) {
            return values.get(0);
        }
        
        return null;           
    }
  
    private List<String> getValueList(String key) {
        List<String> l = get(key);
        if (l == null) {
            l = new LinkedList<String>();
            put(key, l);
        }
        return l;
    }  
}
