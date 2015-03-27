/*******************************************************************************
 * This file is part of TERMINAL RECALL
 * Copyright (c) 2012-2015 Chuck Ritola
 * Part of the jTRFP.org project
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/gpl.html
 * 
 * Contributors:
 *     chuck - initial API and implementation
 ******************************************************************************/

package org.jtrfp.trcl.pool;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;

import org.jtrfp.trcl.coll.ListActionDispatcher;
import org.jtrfp.trcl.coll.ListActionTelemetry;
import org.jtrfp.trcl.coll.PartitionedList;

import com.ochafik.util.listenable.ListenableCollection;

public class PartitionedIndexPoolImpl<STORED_TYPE> implements
	PartitionedIndexPool<STORED_TYPE> {
    private final PartitionedList<EntryBasedIndexPool.Entry<STORED_TYPE>>     pl;
    private final ListActionTelemetry<EntryBasedIndexPool.Entry<STORED_TYPE>> lat;
    private final PropertyChangeSupport pcs = new PropertyChangeSupport(this);
    
    public PartitionedIndexPoolImpl(){
	lat = new ListActionTelemetry<EntryBasedIndexPool.Entry<STORED_TYPE>>();
	pl  = new PartitionedList<EntryBasedIndexPool.Entry<STORED_TYPE>>(lat);
    }
	
    @Override
    public org.jtrfp.trcl.pool.PartitionedIndexPool.Partition<STORED_TYPE> newPartition() {
	Partition<STORED_TYPE> result;
	result = new PartitionImpl(pl.newSubList());
	return result;
    }

    @Override
    public ListenableCollection<org.jtrfp.trcl.pool.PartitionedIndexPool.Partition<STORED_TYPE>> getPartitions() {
	//TODO
	return null;
    }

    @Override
    public ListActionDispatcher<Entry<STORED_TYPE>> getFlatEntries() {
	//TODO
	return null;
    }

    @Override
    public int getTotalUnusedIndices() {
	//TODO
	return -1;
    }

    @Override
    public PartitionedIndexPool<STORED_TYPE> defragment(int maxNumUnusedIndices)
	    throws IllegalArgumentException {
	//TODO
	return null;
    }

    @Override
    public org.jtrfp.trcl.pool.PartitionedIndexPool.UnusedIndexLimitBehavior setTotalUnusedLimitBehavior(
	    org.jtrfp.trcl.pool.PartitionedIndexPool.UnusedIndexLimitBehavior behavior) {
	// TODO Auto-generated method stub
	return null;
    }

    @Override
    public org.jtrfp.trcl.pool.PartitionedIndexPool.UnusedIndexLimitBehavior getTotalUnusedLimitBehavior() {
	// TODO Auto-generated method stub
	return null;
    }

    @Override
    public PartitionedIndexPool<STORED_TYPE> addPropertyChangeListener(
	    PropertyChangeListener l) {
	pcs.addPropertyChangeListener(l);
	return this;
    }

    @Override
    public PartitionedIndexPool<STORED_TYPE> addPropertyChangeListener(
	    String propertyName, PropertyChangeListener l) {
	pcs.addPropertyChangeListener(propertyName, l);
	return this;
    }

    @Override
    public PartitionedIndexPool<STORED_TYPE> removePropertyChangeListener(
	    PropertyChangeListener l) {
	pcs.removePropertyChangeListener(l);
	return this;
    }

    @Override
    public PartitionedIndexPool<STORED_TYPE> removePropertyChangeListener(
	    String propertyName, PropertyChangeListener l) {
	pcs.removePropertyChangeListener(propertyName, l);
	return this;
    }

    @Override
    public PropertyChangeListener[] getPropertyChangeListeners() {
	return pcs.getPropertyChangeListeners();
    }

    @Override
    public PropertyChangeListener[] getPropertyChangeListeners(
	    String propertyName) {
	return pcs.getPropertyChangeListeners(propertyName);
    }

    @Override
    public boolean hasListeners(String propertyName) {
	return pcs.hasListeners(propertyName);
    }
    
    private class PartitionImpl implements Partition<STORED_TYPE>{
	private final EntryBasedIndexPool<STORED_TYPE> ebip;
	private final PartitionedList<EntryBasedIndexPool.Entry<STORED_TYPE>>.ListPartition partitionedList;
	private final ListActionDispatcher<org.jtrfp.trcl.pool.PartitionedIndexPool.Entry<STORED_TYPE>> entries;
	//private final AdaptedList<org.jtrfp.trcl.pool.PartitionedIndexPool.Entry<CONTAINED_TYPE>,EntryBasedIndexPool.Entry<CONTAINED_TYPE>> entryAdapter;
	private boolean valid = true;

	public PartitionImpl(PartitionedList<EntryBasedIndexPool.Entry<STORED_TYPE>>.ListPartition list) {
	    ebip = new EntryBasedIndexPool<STORED_TYPE>();
	    this.partitionedList = list;
	    ebip.getListActionDispatcher().addTarget(list, true);
	    entries = new ListActionDispatcher<org.jtrfp.trcl.pool.PartitionedIndexPool.Entry<STORED_TYPE>>();
	}
	
	private void invalidate(){
	    valid = false;
	}

	@Override
	public PartitionedIndexPool<STORED_TYPE> getParent() {
	    return PartitionedIndexPoolImpl.this;
	}

	@Override
	public org.jtrfp.trcl.pool.PartitionedIndexPool.Entry<STORED_TYPE> newEntry(
		STORED_TYPE value) throws IllegalStateException,
		NullPointerException {
	  //TODO
	return null;
	    //return new EntryImpl(ebip.popEntry(value));
	}

	@Override
	public org.jtrfp.trcl.pool.PartitionedIndexPool.Partition<STORED_TYPE> remove()
		throws IllegalStateException {
	    PartitionedIndexPoolImpl.this.pl.removeSubList(partitionedList);
	    invalidate();
	    return this;
	}

	@Override
	public org.jtrfp.trcl.pool.PartitionedIndexPool.Partition<STORED_TYPE> removeAllEntries()
		throws IllegalStateException {
	    for(EntryBasedIndexPool.Entry<STORED_TYPE> entry: ebip.getListActionDispatcher())
		entry.free();
	    return null;
	}

	@Override
	public ListActionDispatcher<org.jtrfp.trcl.pool.PartitionedIndexPool.Entry<STORED_TYPE>> getEntries() {
	    return entries;
	}

	@Override
	public int getGlobalStartIndex() {
	    // TODO Auto-generated method stub
	    return 0;
	}

	@Override
	public int getLengthInIndices() {
	    // TODO Auto-generated method stub
	    return 0;
	}

	@Override
	public int defragment(int maxNumUnusedIndices)
		throws IllegalStateException, IllegalArgumentException {
	    // TODO Auto-generated method stub
	    return 0;
	}

	@Override
	public org.jtrfp.trcl.pool.PartitionedIndexPool.UnusedIndexLimitBehavior setUnusedLimitBehavior(
		org.jtrfp.trcl.pool.PartitionedIndexPool.UnusedIndexLimitBehavior mode) {
	    // TODO Auto-generated method stub
	    return null;
	}

	@Override
	public org.jtrfp.trcl.pool.PartitionedIndexPool.UnusedIndexLimitBehavior getUnusedLimitBehavior() {
	    // TODO Auto-generated method stub
	    return null;
	}

	@Override
	public boolean isValid() {
	    // TODO Auto-generated method stub
	    return false;
	}

	@Override
	public PartitionedIndexPool<STORED_TYPE> addPropertyChangeListener(
		PropertyChangeListener l) {
	    // TODO Auto-generated method stub
	    return null;
	}

	@Override
	public PartitionedIndexPool<STORED_TYPE> addPropertyChangeListener(
		String propertyName, PropertyChangeListener l) {
	    // TODO Auto-generated method stub
	    return null;
	}

	@Override
	public PartitionedIndexPool<STORED_TYPE> removePropertyChangeListener(
		PropertyChangeListener l) {
	    // TODO Auto-generated method stub
	    return null;
	}

	@Override
	public PartitionedIndexPool<STORED_TYPE> removePropertyChangeListener(
		String propertyName, PropertyChangeListener l) {
	    // TODO Auto-generated method stub
	    return null;
	}

	@Override
	public PropertyChangeListener[] getPropertyChangeListeners() {
	    // TODO Auto-generated method stub
	    return null;
	}

	@Override
	public PropertyChangeListener[] getPropertyChangeListeners(
		String propertyName) {
	    // TODO Auto-generated method stub
	    return null;
	}

	@Override
	public boolean hasListeners(String propertyName) {
	    // TODO Auto-generated method stub
	    return false;
	}
	
	
	protected class EntryImpl implements Entry<STORED_TYPE>{
		public EntryImpl(
		    org.jtrfp.trcl.pool.EntryBasedIndexPool.Entry<STORED_TYPE> popEntry) {
		// TODO Auto-generated constructor stub
	    }

		@Override
		public org.jtrfp.trcl.pool.PartitionedIndexPool.Partition<STORED_TYPE> getParent() {
		    // TODO Auto-generated method stub
		    return null;
		}

		@Override
		public STORED_TYPE get() {
		    // TODO Auto-generated method stub
		    return null;
		}

		@Override
		public int getLocalIndex() throws IllegalStateException {
		    // TODO Auto-generated method stub
		    return 0;
		}

		@Override
		public int getGlobalIndex() throws IllegalStateException {
		    // TODO Auto-generated method stub
		    return 0;
		}

		@Override
		public org.jtrfp.trcl.pool.PartitionedIndexPool.Entry<STORED_TYPE> remove()
			throws IllegalStateException {
		    // TODO Auto-generated method stub
		    return null;
		}

		@Override
		public boolean isValid() {
		    // TODO Auto-generated method stub
		    return false;
		}

		@Override
		public PartitionedIndexPool<STORED_TYPE> addPropertyChangeListener(
			PropertyChangeListener l) {
		    // TODO Auto-generated method stub
		    return null;
		}

		@Override
		public PartitionedIndexPool<STORED_TYPE> addPropertyChangeListener(
			String propertyName, PropertyChangeListener l) {
		    // TODO Auto-generated method stub
		    return null;
		}

		@Override
		public PartitionedIndexPool<STORED_TYPE> removePropertyChangeListener(
			PropertyChangeListener l) {
		    // TODO Auto-generated method stub
		    return null;
		}

		@Override
		public PartitionedIndexPool<STORED_TYPE> removePropertyChangeListener(
			String propertyName, PropertyChangeListener l) {
		    // TODO Auto-generated method stub
		    return null;
		}

		@Override
		public PropertyChangeListener[] getPropertyChangeListeners() {
		    // TODO Auto-generated method stub
		    return null;
		}

		@Override
		public PropertyChangeListener[] getPropertyChangeListeners(
			String propertyName) {
		    // TODO Auto-generated method stub
		    return null;
		}

		@Override
		public boolean hasListeners(String propertyName) {
		    // TODO Auto-generated method stub
		    return false;
		}
		
	    }//end EntryImpl
    }//end PartitionImpl

}//end PartitionedIndexPool
