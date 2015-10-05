/*
 * see license.txt
 */
package leola.web.filewatcher;

import java.io.File;
import java.nio.file.FileSystems;
import java.nio.file.Path;
import java.nio.file.StandardWatchEventKinds;
import java.nio.file.WatchEvent;
import java.nio.file.WatchKey;
import java.nio.file.WatchService;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import leola.frontend.listener.EventDispatcher;
import leola.web.filewatcher.FileModifiedEvent.ModificationType;

/**
 * Listens to a directory for any file changes.  If a file change occurs (file modification, creation of a file or deletion of a file) this will
 * create a new {@link FileModifiedEvent} and dispatch to any listening {@link FileModifiedListener}s.
 * 
 * @see EventDispatcher
 * @author Tony
 *
 */
public class FileWatcher {

    private WatchService watchService;
    private List<File> dirsToWatch;
    private Thread watchThread;
    
    private EventDispatcher dispatcher;
    private Lock lock;
    
    /**
     * @param watchDir
     * @param dispatcher
     */
    public FileWatcher(EventDispatcher dispatcher, File ... watchDir) {
        this.dirsToWatch = Arrays.asList(watchDir);
        this.dispatcher = dispatcher;
        this.lock = new ReentrantLock();
    }

    /**
     * Start watching the file system
     */
    public void startWatching() {
        try {
            this.lock.lockInterruptibly();
            try {
                this.watchThread = new Thread( new Runnable() { 
                    
                    @SuppressWarnings("unchecked")
                    @Override
                    public void run() {
                     
                        try {
                            watchService = FileSystems.getDefault().newWatchService();
                            for(File dir : dirsToWatch) {
                                Path path = dir.toPath();
                                path.register(watchService, StandardWatchEventKinds.ENTRY_CREATE, StandardWatchEventKinds.ENTRY_MODIFY);
                            }
                            
                            for (;;) {
            
                                // wait for key to be signaled
                                WatchKey key = null;
                                try {
                                    key = watchService.take();
                                } catch (InterruptedException x) {
                                    return;
                                }
            
                                for (WatchEvent<?> event: key.pollEvents()) {                            
                                    WatchEvent.Kind<?> kind = event.kind();
            
                                    if (kind == StandardWatchEventKinds.OVERFLOW) {
                                        continue;
                                    }
            
                                    File dirToWatch = ((Path)key.watchable()).toFile();
                                    
                                    // The filename is the
                                    // context of the event.
                                    WatchEvent<Path> ev = (WatchEvent<Path>)event;
                                    Path filename = ev.context();
                                    
                                    ModificationType modType = ModificationType.REMOVED;
                                    if(kind.equals(StandardWatchEventKinds.ENTRY_CREATE)) {
                                        modType = ModificationType.CREATED;
                                    }
                                    else if (kind.equals(StandardWatchEventKinds.ENTRY_MODIFY)) {
                                        modType = ModificationType.MODIFIED;
                                    }
                                    System.out.println("File modified: " + filename);
                                    dispatcher.queueEvent(new FileModifiedEvent(this, new File(dirToWatch, filename.toString()), modType));
                                }
            
                                dispatcher.processQueue();
                                
                                boolean valid = key.reset();
                                if (!valid) {
                                    break;
                                }
                            }
                        }                            
                        catch(Exception e) {
                            e.printStackTrace();
                        }
                    }
                }, "file-watcher-thread");
                
                this.watchThread.start();
            }
            finally {
                this.lock.unlock();
            }
        }
        catch(InterruptedException e) {
            /* break out */
        }
        
        
    }
    
    
    /**
     * Stop the watcher
     */
    public void stopWatching() {
        try {
            this.lock.lock();
        
            try {   
                if(this.watchService!=null) {
                    this.watchService.close();
                }
            }
            catch(Exception ignore) {                
            }
            finally {
                /*System.out.println("Watcher.join");
                try { this.watchThread.join(); }
                catch(Exception ignore) {}*/
            }
        }
        finally {
            this.lock.unlock();
        }
    }
}
