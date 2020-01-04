/*
 * see license.txt
 */
package leola.web.filewatcher;

import java.io.File;

import leola.web.event.Event;


/**
 * A File has been modified (either created, deleted or the contents modified).
 * 
 * @author Tony
 *
 */
public class FileModifiedEvent extends Event {

	public static enum ModificationType {
		CREATED,
		MODIFIED,
		REMOVED
	}

	private File file;
	private ModificationType modType;

	/**
	 * @param source
	 */
	public FileModifiedEvent(Object source, File file, ModificationType modType) {
		super(source);
		this.file = file;
		this.modType = modType;
	}

	/**
	 * @return the modType
	 */
	public ModificationType getModType() {
		return modType;
	}

	/**
	 * @return the file
	 */
	public File getFile() {
		return file;
	}

}
