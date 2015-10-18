/*
 * see license.txt
 */
package leola.web.templates;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;
import java.io.StringWriter;
import java.io.Writer;

/**
 * A simple template engine mechanism
 * 
 * @author Tony
 *
 */
public interface TemplateEngine extends AutoCloseable {

    /**
     * Template document represents a template.
     * 
     * @author Tony
     *
     */
    public static interface TemplateDocument {
        
        /**
         * Apply the data to the template
         * @param data
         * @return the template
         */
        default public String apply(Object data) throws IOException {
            StringWriter writer = new StringWriter();
            apply(writer, data);
            return writer.toString();
        }
        
        public void apply(Writer writer, Object data) throws IOException;
    }
    
    default public TemplateDocument getTemplate(File file) throws IOException {
        try(Reader reader = new BufferedReader(new FileReader(file))) {
            return getTemplate(reader);
        }
    }
    public TemplateDocument getTemplate(Reader reader) throws IOException;
}
