package ini.cx3d.utilities;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.zip.DataFormatException;
import java.util.zip.Deflater;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;
import java.util.zip.Inflater;

public class Compressor{
    public static byte[] compress(byte[] content){
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        try{
            GZIPOutputStream gzipOutputStream = new GZIPOutputStream(byteArrayOutputStream);
            gzipOutputStream.write(content);
            gzipOutputStream.close();
        } catch(IOException e){
            throw new RuntimeException(e);
        }
       
        return byteArrayOutputStream.toByteArray();
    }

    public static byte[] decompress(byte[] contentBytes){
    	
        byte [] buff=null;
        try{
        	GZIPInputStream in = new GZIPInputStream(new ByteArrayInputStream(contentBytes),1024*1024*10);
        	buff = new byte[in.available()];
        	in.read(buff);
        	
        } catch(IOException e){
            throw new RuntimeException(e);
        }
        return buff;
 
    }
    
    public static byte[] compress2(byte[] bytesToCompress) 
    {
        // Compressor with highest level of compression.
        Deflater compressor = new Deflater(Deflater.BEST_COMPRESSION);
        compressor.setInput(bytesToCompress); // Give the compressor the data to compress.
        compressor.finish();
 
        // Create an expandable byte array to hold the compressed data.
        // It is not necessary that the compressed data will be smaller than
        // the uncompressed data.
        ByteArrayOutputStream bos = new ByteArrayOutputStream(bytesToCompress.length);
 
        // Compress the data
        byte[] buf = new byte[bytesToCompress.length + 100];
        while (!compressor.finished())
        {
            bos.write(buf, 0, compressor.deflate(buf));
        }
 
        try {
			bos.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		// System.out.printf("Compression ratio %f\n", (1.0f * bytesToCompress.length/bos.size()));
        // Get the compressed data
        return bos.toByteArray();
    }
 
    /**
     * Decompress data.
     * @param compressedBytes is the compressed byte array.
     * @return decompressed byte array.
     * @throws java.io.IOException
     * @throws java.util.zip.DataFormatException
     */
    public static byte[] decompress2(byte[] compressedBytes)
    {
        // Initialize decompressor.
        Inflater decompressor = new Inflater();
        decompressor.setInput(compressedBytes);  // Give the decompressor the data to decompress.
        decompressor.finished();
 
        // Create an expandable byte array to hold the decompressed data.
        // It is not necessary that the decompressed data will be larger than
        // the compressed data.
        ByteArrayOutputStream bos = new ByteArrayOutputStream(compressedBytes.length);
 
        // Decompress the data
        byte[] buf = new byte[compressedBytes.length + 100];
        while (!decompressor.finished())
        {
            try {
				bos.write(buf, 0, decompressor.inflate(buf));
			} catch (DataFormatException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
        }
 
        try {
			bos.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
 
        // Get the decompressed data.
        return bos.toByteArray();
    }

}