/*
 * see license.txt
 */
package leola.web.templates.handlebars;

import java.io.*;

import com.github.jknack.handlebars.*;
import com.github.jknack.handlebars.io.FileTemplateLoader;

import leola.vm.types.*;
import leola.web.*;
import leola.web.templates.TemplateEngine;

/**
 * @author Tony
 *
 */
public class HandlebarsTemplateEngine implements TemplateEngine {

    public static class HandlebarsTemplateDocument implements TemplateDocument {
        
        private Template template;
        
        HandlebarsTemplateDocument(Template template) {
            this.template = template;
        }
        
        @Override
        public void apply(Writer writer, Object data) throws IOException {
            if(data instanceof LeoObject) {                
                data = WebLeolaLibrary.toJsonJavaObject((LeoObject)data);
            }
            this.template.apply(data, writer);
        }
    }
    
    private Handlebars handlebars;
    private final String suffix;
    private final String baseDir;
    /**
     * @param basedir the base directory for where templates reside
     * @param config the configuration
     */
    public HandlebarsTemplateEngine(File basedir, LeoMap config) {
        String s = config.getString("suffix");
        this.suffix = (s == null || s.isEmpty()) ? ".hbs" : s;
        this.baseDir = basedir.getAbsolutePath();
        this.handlebars = new Handlebars(new FileTemplateLoader(basedir, s){            
            
            @Override
            public String resolve(String uri) {
                return getPrefix() + normalize(uri);
            }  
        });
    }
    
    private String leolaNormalize(String templatePath) {        
        //if(templatePath.endsWith(suffix)) {
        //    templatePath = templatePath.substring(0, templatePath.length() - suffix.length());
        //}
        if(templatePath.startsWith(this.baseDir)) {
            templatePath = templatePath.substring(this.baseDir.length());
        }
        return templatePath;
    }

    @Override
    public TemplateDocument getTemplate(File file) throws IOException {        
        String templatePath = file.getAbsolutePath();
        templatePath = leolaNormalize(templatePath);
        Template template = this.handlebars.compile(templatePath);
        return new HandlebarsTemplateDocument(template);
    }
    
    @Override
    public TemplateDocument getTemplate(Reader reader) throws IOException {        
        Template template = this.handlebars.compileInline(Util.readString(reader));
        return new HandlebarsTemplateDocument(template);
    }
    
    @Override
    public void close() throws Exception {        
    }
    
}
