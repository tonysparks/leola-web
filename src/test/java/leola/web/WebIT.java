package leola.web;

import java.io.File;
import java.io.FileNotFoundException;

import leola.vm.Args;
import leola.vm.Leola;
import leola.vm.types.LeoBoolean;
import leola.vm.types.LeoObject;
import leola.vm.types.LeoUserFunction;

import org.junit.Test;


/**
 * Integration test
 * 
 * @author Tony
 *
 */
public class WebIT {

	@Test
	public void test() throws Exception {
	    File file = new File("examples/notFound/app.leola");
        if(!file.exists()) {
            throw new FileNotFoundException(file.getAbsolutePath());
        }
	    
		Args args = new Args.ArgsBuilder()
		                    .setIsDebugMode(true)
		                    .setFileName(file.getAbsolutePath())
		                    .build();
		Leola runtime = new Leola(args);
		
		// override the 'require', so that we don't
		// include this lib
		runtime.put("require", new LeoUserFunction() {			
			@Override
			public LeoObject call(LeoObject[] args) {
				return LeoBoolean.LEOTRUE;
			}
		});
		
		runtime.loadLibrary(new WebLeolaLibrary(), "web");
		
		
		
		LeoObject result = runtime.eval(file);
		if(result.isError()) {
			System.out.println("An error has occured: " + result);
		}
		
		/*  The JUnit framework doesn't care about the (non)Daemon threads,
		 *  so our 'autoReload' feature does not work here
		 */
		
		/*
		Thread[] activeThreads = new Thread[Thread.activeCount()];
		Thread.enumerate(activeThreads);
		for(Thread t : activeThreads) {
		    if(t!=null) {
		        if(t.isDaemon()) {
		            
		        }
		    }
		}*/
	}

}
