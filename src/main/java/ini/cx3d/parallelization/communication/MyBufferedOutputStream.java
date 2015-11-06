package ini.cx3d.parallelization.communication;

import ini.cx3d.gui.simulation.OutD;

import java.io.FilterOutputStream;
import java.io.IOException;
import java.io.OutputStream;

/**
 * The class implements a buffered output stream. By setting up such 
 * an output stream, an application can write bytes to the underlying 
 * output stream without necessarily causing a call to the underlying 
 * system for each byte written.
 *
 * @author  Arthur van Hoff
 * @version 1.34, 11/17/05
 * @since   JDK1.0
 */
public class MyBufferedOutputStream extends FilterOutputStream {
	/**
	 * The internal buffer where data is stored. 
	 */
	protected byte buf[];

	private long num_bytes;
	private long time_sending;
	
	/**
	 * in mbit/s
	 * @return
	 */
	public double getTransmitionSpeed()
	{
		return num_bytes*1.0/time_sending/1024/1024*1000*1000*1000;
	}
	
	public void resetMeasurements()
	{
		num_bytes=0;
		time_sending=0;
	}
	
	/**
	 * The number of valid bytes in the buffer. This value is always 
	 * in the range <tt>0</tt> through <tt>buf.length</tt>; elements 
	 * <tt>buf[0]</tt> through <tt>buf[count-1]</tt> contain valid 
	 * byte data.
	 */
	protected int count;

	/**
	 * Creates a new buffered output stream to write data to the
	 * specified underlying output stream.
	 *
	 * @param   out   the underlying output stream.
	 */
	public MyBufferedOutputStream(OutputStream out) {
		this(out, 8192);
	}

	/**
	 * Creates a new buffered output stream to write data to the 
	 * specified underlying output stream with the specified buffer 
	 * size. 
	 *
	 * @param   out    the underlying output stream.
	 * @param   size   the buffer size.
	 * @exception IllegalArgumentException if size &lt;= 0.
	 */
	public MyBufferedOutputStream(OutputStream out, int size) {
		super(out);
		if (size <= 0) {
			throw new IllegalArgumentException("Buffer size <= 0");
		}
		buf = new byte[size];
	}

	/** Flush the internal buffer */
	private void flushBuffer() throws IOException {
//		ShowConsoleOutput.print("flushsize "+count+" : "+count/1024.0+" kb " );
		long current = System.nanoTime();
		num_bytes+=count;
		if (count > 0) {
			out.write(buf, 0, count);
			count = 0;
		}
		double time = System.nanoTime()-current;
		time_sending+=time;
		
//		ShowConsoleOutput.println(" took : "+time);
	}

	/**
	 * Writes the specified byte to this buffered output stream. 
	 *
	 * @param      b   the byte to be written.
	 * @exception  IOException  if an I/O error occurs.
	 */
	public synchronized void write(int b) throws IOException {
		
		if (count >= buf.length) {
			
			flushBuffer();
		}
		buf[count++] = (byte)b;
	}

	/**
	 * Writes <code>len</code> bytes from the specified byte array 
	 * starting at offset <code>off</code> to this buffered output stream.
	 *
	 * <p> Ordinarily this method stores bytes from the given array into this
	 * stream's buffer, flushing the buffer to the underlying output stream as
	 * needed.  If the requested length is at least as large as this stream's
	 * buffer, however, then this method will flush the buffer and write the
	 * bytes directly to the underlying output stream.  Thus redundant
	 * <code>BufferedOutputStream</code>s will not copy data unnecessarily.
	 *
	 * @param      b     the data.
	 * @param      off   the start offset in the data.
	 * @param      len   the number of bytes to write.
	 * @exception  IOException  if an I/O error occurs.
	 */
	public synchronized void write(byte b[], int off, int len) throws IOException {
		
		if (len >= buf.length) {
			/* If the request length exceeds the size of the output buffer,
    	       flush the output buffer and then write the data directly.
    	       In this way buffered streams will cascade harmlessly. */
			OutD.println("package bigger then buffer!");
			flushBuffer();
			out.write(b, off, len);
			return;
		}
		if (len > buf.length - count) {
			flushBuffer();
		}
		System.arraycopy(b, off, buf, count, len);
		count += len;
	}

	/**
	 * Flushes this buffered output stream. This forces any buffered 
	 * output bytes to be written out to the underlying output stream. 
	 *
	 * @exception  IOException  if an I/O error occurs.
	 * @see        java.io.FilterOutputStream#out
	 */
	public synchronized void flush() throws IOException {
		//ShowConsoleOutput.println("flush");
		flushBuffer();
		out.flush();
	}

	public void setNum_bytes(long num_bytes) {
		this.num_bytes = num_bytes;
	}

	public long getNum_bytes() {
		return num_bytes;
	}

	public void setTime_sending(long time_sending) {
		this.time_sending = time_sending;
	}

	public long getTime_sending() {
		return time_sending;
	}
}

