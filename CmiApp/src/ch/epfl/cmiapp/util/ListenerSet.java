package ch.epfl.cmiapp.util;

import java.lang.ref.WeakReference;
import java.util.*;

public class ListenerSet<L>
	implements Iterable<L>
{
	
	private Set<WeakReference<L>> listeners = new HashSet<WeakReference<L>>(); 
	
	
	public void add(L l)
	{
		WeakReference<L> ref = new WeakReference<L>(l);
		listeners.add(ref);
	}

	public int count()
	{
		return listeners.size();
	}
	
	public Iterator<L> iterator()
	{
		Iterator<WeakReference<L>> iterator = listeners.iterator();
		List<L> list = new ArrayList<L>(listeners.size());
		
		while(iterator.hasNext())
		{
			WeakReference<L> ref = iterator.next();
			if (ref.get() == null)
				iterator.remove();
			else
				list.add(ref.get());
		}
		return list.iterator();
	}
}
