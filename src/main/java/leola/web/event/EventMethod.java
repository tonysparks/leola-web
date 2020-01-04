package leola.web.event;

import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Defines an event method.  An {@link EventMethod} must have the
 * method signature of:
 * 
 * <pre> 
 * public void myEventName( Event event )
 * </pre>
 * Where myEventName can be anything and event class must inherit from {@link Event}. 
 * 
 * @author Tony
 *
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
@Inherited
public @interface EventMethod {
}