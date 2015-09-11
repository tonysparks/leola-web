/*
 * see license.txt
 */
package leola.web;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;

import javax.ws.rs.core.MultivaluedMap;

/**
 * Very simple implementation of a {@link MultivaluedMap}
 * 
 * @author Tony
 *
 */
public class MultivaluedMapImpl extends HashMap<String, List<String>> implements MultivaluedMap<String, String> {

    /**
     * SUID
     */
    private static final long serialVersionUID = 528400173846253754L;

    public MultivaluedMapImpl() {         
    }

    public MultivaluedMapImpl(MultivaluedMap<String, String> that) {
        that.forEach( (key, values) -> {
            put(key, values);
        });        
    }

    /*
     * (non-Javadoc)
     * @see javax.ws.rs.core.MultivaluedMap#putSingle(java.lang.Object, java.lang.Object)
     */
    @Override
    public final void putSingle(String key, String value) {
        List<String> l = getValueList(key);
        l.clear();

        l.add(value != null ? value : "");
    }

    /*
     * (non-Javadoc)
     * @see javax.ws.rs.core.MultivaluedMap#add(java.lang.Object, java.lang.Object)
     */
    @Override
    public final void add(String key, String value) {
        List<String> l = getValueList(key);
        l.add(value != null ? value : "");
    }

    /*
     * (non-Javadoc)
     * @see javax.ws.rs.core.MultivaluedMap#getFirst(java.lang.Object)
     */
    @Override
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
