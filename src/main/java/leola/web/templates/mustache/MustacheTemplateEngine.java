/*
 * see license.txt
 */
package leola.web.templates.mustache;

import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.util.Random;

import leola.web.WebApp;
import leola.web.templates.TemplateEngine;

import com.github.mustachejava.DefaultMustacheFactory;
import com.github.mustachejava.Mustache;
import com.github.mustachejava.MustacheFactory;

/**
 * Uses Mustache as the template engine
 * 
 * @author Tony
 *
 */
public class MustacheTemplateEngine implements TemplateEngine {
    private MustacheFactory mf;
    private Random rand;
    /**
     * @param webapp
     */
    public MustacheTemplateEngine(WebApp webapp) {
        mf = new DefaultMustacheFactory(webapp.getRootDirectory());
        rand = new Random();
    }

    @Override
    public TemplateDocument getTemplate(Reader reader) throws IOException {
        Mustache mustache = mf.compile(reader, "inline-template-" + rand.nextInt());                
        return new TemplateDocument() {
            
            @Override
            public void apply(Writer writer, Object data) throws IOException {                
                mustache.execute(writer, data);
            }
        };
    }
    
    @Override
    public void close() throws Exception {        
    }

}
