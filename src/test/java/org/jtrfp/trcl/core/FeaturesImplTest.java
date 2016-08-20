package org.jtrfp.trcl.core;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

import org.junit.Test;
import org.mockito.Mockito;

public class FeaturesImplTest {
    private FeaturesImpl subject;
    
    protected FeaturesImpl getSubject(){
	if(subject == null)
	    subject = new FeaturesImpl();
	return subject;
    }

    @Test
    public void testRegisterFeature() {
	final FeatureFactory factory = Mockito.mock(FeatureFactory.class);
	when(factory.getFeatureClass()).thenReturn (DummyF1.class);
	when(factory.getTargetClass()) .thenReturn (DummyType1.class);
	getSubject().registerFeature(factory);
    }
    
    @Test
    public void testGetFeatureMulti(){
	final FeaturesImpl subject = getSubject();
	final FeatureFactory factory = Mockito.mock(FeatureFactory.class);
	when(factory.getFeatureClass()).thenReturn (DummyF1.class);
	when(factory.getTargetClass()) .thenReturn (DummyType1.class);
	final DummyType1 dummyTarget1 = mock(DummyType1.class);
	final DummyF1 dummyFeature1   = mock(DummyF1.class);
	
	try{when(factory.newInstance(dummyTarget1)).thenReturn (dummyFeature1);}
	catch(FeatureNotApplicableException e){}
	subject.registerFeature(factory);
	
	subject.init(dummyTarget1);
	
	//Ensure multiple calls works
	assertEquals(dummyFeature1, subject.get(dummyTarget1, DummyF1.class));
	assertEquals(dummyFeature1, subject.get(dummyTarget1, DummyF1.class));
	try{verify(factory,times(1)).newInstance(dummyTarget1);}
	catch(FeatureNotApplicableException e){}
    }//end testGetFeature()
    
    private static interface DummyType1 {
    }//end DummyType1

    public static interface DummyF1 extends Feature<DummyType1> {
    }//end DummyF1
}//end FeaturesImplTest
