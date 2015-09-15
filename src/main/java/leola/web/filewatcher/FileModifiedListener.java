/*
 * see license.txt
 */
package leola.web.filewatcher;

import leola.frontend.listener.EventListener;
import leola.frontend.listener.EventMethod;

/**
 * Listens for any {@link FileModifiedEvent}'s
 * 
 * @author Tony
 *
 */
public interface FileModifiedListener extends EventListener {

	/**
	 * A File was modified
	 * 
	 * @param event
	 */
	@EventMethod
	public void onFileModified(FileModifiedEvent event);
}
