package leola.web;

import java.io.File;
import java.io.FileNotFoundException;

import org.junit.Test;

import leola.vm.Args;
import leola.vm.Leola;
import leola.vm.types.LeoBoolean;
import leola.vm.types.LeoObject;
import leola.vm.types.LeoUserFunction;


/**
 * Integration test
 * 
 * @author Tony
 *
 */
public class WebIT {

	@Test
	public void test() throws Exception {
	    File file = new File("examples/upload/app.leola");
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
	}

}
