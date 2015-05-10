package org.jtrfp.trcl.coll;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.ochafik.util.listenable.AdaptedCollection;
import com.ochafik.util.listenable.Adapter;
import com.ochafik.util.listenable.Pair;

public class CollectionActionPackerTest {
    private CollectionActionPacker<String> subject;
    private CollectionActionDispatcher<CollectionActionDispatcher<Pair<Integer,String>>> subjectDelegate;
    Collection<String> flatCollection;
    Pair<Integer,String> zero,one,two,A,B,C;
    Adapter<Pair<Integer,String>,String> strippingAdapter = new Adapter<Pair<Integer,String>,String>(){
	@Override
	public String adapt(Pair<Integer, String> value) {
	    return value.getValue();
	}
    };
    Adapter<String,Pair<Integer,String>> dummyAdapter = new  Adapter<String,Pair<Integer,String>>(){
	@Override
	public Pair<Integer, String> adapt(String value) {
	    throw new UnsupportedOperationException();
	}
    };
    private CollectionActionUnpacker<Pair<Integer,String>> unpacker;

    @Before
    public void setUp() throws Exception {
	subjectDelegate = new CollectionActionDispatcher<CollectionActionDispatcher<Pair<Integer,String>>>(new ArrayList<CollectionActionDispatcher<Pair<Integer,String>>>());
	subject         = new CollectionActionPacker<String>(subjectDelegate);
	
	subject.add(zero=new Pair<Integer,String>(0,"zero"));
	subject.add(one =new Pair<Integer,String>(0,"one"));
	subject.add(two =new Pair<Integer,String>(0,"two"));
	
	subject.add(A=new Pair<Integer,String>(1,"A"));
	subject.add(B=new Pair<Integer,String>(1,"B"));
	subject.add(C=new Pair<Integer,String>(1,"C"));
	
	flatCollection = new ArrayList<String>();
	unpacker = new CollectionActionUnpacker<Pair<Integer,String>>(new AdaptedCollection<String,Pair<Integer,String>>(flatCollection, dummyAdapter,strippingAdapter));
    }

    @After
    public void tearDown() throws Exception {
	unpacker = null; flatCollection = null;
	subject = null; subjectDelegate = null;
    }

    @Test
    public void testAdd() {
	assertEquals(2,subjectDelegate.size());
	
	for(CollectionActionDispatcher<Pair<Integer,String>> collection:subjectDelegate){
	    Pair<Integer,String> first = collection.iterator().next();
	    if(first.getKey()==0 || first.getKey()==1){
		//ok
	    }else{fail("Got unexpected key: "+first.getKey());}
	}//end testAdd()
    }//end testAdd()

    @Test
    public void testClear() {
	assertEquals(2,subjectDelegate.size());//Control test
	subject.clear();
	assertEquals(0,subjectDelegate.size());
    }

    @Test
    public void testContains() {
	assertTrue(subject.contains(zero));
	assertFalse(subject.contains(null));
	assertTrue(subject.contains(A));
	assertTrue(subject.contains(B));
	assertTrue(subject.contains(C));
	assertTrue(subject.contains(one));
	assertTrue(subject.contains(two));
    }

    @Test
    public void testContainsAll() {
	assertTrue(subject.containsAll(Arrays.asList(zero,one,two,A,B,C)));
    }

    @Test
    public void testIsEmpty() {
	assertFalse(subject.isEmpty());
	subject.clear();
	assertTrue(subject.isEmpty());
    }

    @Test
    public void testRemove() {
	subject.remove(zero);
	assertFalse(subject.contains(zero));
    }

    @Test
    public void testRemoveAll() {
	assertTrue(subject.removeAll(Arrays.asList(one,two)));
	assertFalse(subject.contains(one));
	assertFalse(subject.contains(two));
	assertEquals(4,subject.size());
    }

    @Test
    public void testRetainAll() {
	assertTrue(subject.retainAll(Arrays.asList(one,two,A)));
	assertEquals(3,subject.size());
    }

    @Test
    public void testSize() {
	assertEquals(6,subject.size());
    }

    @Test
    public void testToArray() {
	Object [] array = subject.toArray();
	Collection c = Arrays.asList(array);
	assertEquals(6,c.size());
	assertTrue(c.contains(zero));
	assertTrue(c.contains(one));
	assertTrue(c.contains(two));
	
	assertTrue(c.contains(A));
	assertTrue(c.contains(B));
	assertTrue(c.contains(C));
    }//end testToArray()

    @Test
    public void testAddAll() {
	Pair<Integer,String> three, D;
	Collection<Pair<Integer,String>> temp = new ArrayList<Pair<Integer,String>>();
	temp.add(three=new Pair<Integer,String>(0,"three"));
	temp.add(D    =new Pair<Integer,String>(1,"D"));
	subject.addAll(temp);
    }//end tsetAddAll()

    @Test
    public void testIterator() {
	Iterator it = subject.iterator();
	assertNotNull(it);
	for(int i = 0; i<6; i++){
	    assertTrue(it.hasNext());
	    it.next();
	}
	assertFalse(it.hasNext());
    }//end testIterator()

}//end CollectionActionPacker
