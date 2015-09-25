/*
 * see license.txt
 */
package leola.web;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.channels.Channels;
import java.nio.channels.FileChannel;
import java.nio.channels.FileLock;
import java.util.Random;

import javax.servlet.http.Part;

/**
 * @author Tony
 *
 */
public class Util {

    public static final int DEFAULT_BUFFER_SIZE = 1024 * 4;
    
    /**
     * Writes out a file to disk from the supplied {@link InputStream}.
     * 
     * @param file
     * @param iStream
     * @return the number of bytes read
     * @throws IOException
     */
    public static long writeFile(File file, InputStream iStream) throws IOException {
        FileOutputStream oStream = new FileOutputStream(file, false);
        FileChannel fileChannel = oStream.getChannel();
        FileLock lock = fileChannel.lock();
        
        long bytesRead = 0;
        long totalBytesRead = 0;
        try {   
            try {
                do {
                    bytesRead = fileChannel.transferFrom(Channels.newChannel(iStream), totalBytesRead, DEFAULT_BUFFER_SIZE);
                    totalBytesRead += bytesRead;
                }
                while( bytesRead > 0);
            }
            finally {
                if ( lock != null ) {
                    lock.release();      
                }
            }
                
        }
        finally {
            oStream.close();
        }
        
        return totalBytesRead;
    }

    /**
     * Copy the {@link InputStream} to the {@link OutputStream}.  This will not close any of the supplied streams ({@link OutputStream} nor
     * the {@link InputStream}).
     * 
     * @param iStream
     * @param oStream
     * @return the number of bytes read
     * @throws IOException
     */
    public static long copy(InputStream iStream, OutputStream oStream) throws IOException {        
        byte[] buf = new byte[DEFAULT_BUFFER_SIZE];
        
        int bytesRead = 0;
        long totalBytesRead = 0;
        
        do {
            bytesRead = iStream.read(buf);
            oStream.write(buf, 0, bytesRead);
            
            totalBytesRead += bytesRead;
        }
        while(bytesRead > 0);
    
        
        return totalBytesRead;
    }
    
    /**
     * Utility method to get file name from HTTP header content-disposition
     * 
     * @param part
     * @return the file name 
     */
    public static String getFileName(Part part) {
        String contentDisp = part.getHeader("content-disposition");     
        String[] tokens = contentDisp.split(";");
        for (String token : tokens) {
            if (token.trim().toLowerCase().startsWith("filename")) {
                return token.substring(token.indexOf("=") + 2, token.length()-1);
            }
        }
        return Long.toHexString(new Random().nextLong());
    }
}
