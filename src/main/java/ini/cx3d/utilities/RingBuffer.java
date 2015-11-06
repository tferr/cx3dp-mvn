package ini.cx3d.utilities;

import java.io.Serializable;

public class RingBuffer<Item> implements Serializable {
    /**
	 * 
	 */
	private static final long serialVersionUID = 2103170451641936365L;
	private Item[] a;            // queue elements
    private int N = 0;           // number of elements on queue
    private int first = 0;       // index of first element of queue
    private int last  = 0;       // index of next available slot

    // cast needed since no generic array creation in Java
    public RingBuffer(int capacity) {
        a = (Item[]) new Object[capacity];
    }
    public int size()        { return N;      }

    public  void enqueue(Item item) {
        a[last%a.length] = item;
        last = (last + 1);     // wrap-around
        first = Math.max(0, last-a.length);
        if(a.length>N)
        {
        	N++;
        }
    }
    
    public  void add(Item item) {
    	enqueue(item);
    }

    public int getFirst()
    {
    	return first;
    }
    
    public int length()
    {
    	return N;
    }
    
    public Item get(int i)
    {
    	return a[(first+i)%a.length];
    }
    
    
}
