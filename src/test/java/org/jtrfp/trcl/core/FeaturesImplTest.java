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
    public void testGetFeatureMultiIdenticalGets() throws Throwable{
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
	verify(factory,times(1)).newInstance(dummyTarget1);
    }//end testGetFeature()
    
    @Test
    public void testGetFeatureMultiInterfaceSameFeatureGets() throws Throwable{
	final FeaturesImpl subject = getSubject();
	final FeatureFactory factory = Mockito.mock(FeatureFactory.class);
	when(factory.getFeatureClass()).thenReturn (DummyMultiIfaceFeature1.class);
	when(factory.getTargetClass()) .thenReturn (DummyType1.class);
	final DummyType1 dummyTarget1 = mock(DummyType1.class);
	final DummyMultiIfaceFeature1 dummyFeature1   = mock(DummyMultiIfaceFeature1.class);
	
	when(factory.newInstance(dummyTarget1)).thenReturn (dummyFeature1);
	subject.registerFeature(factory);
	
	subject.init(dummyTarget1);
	
	assertEquals(dummyFeature1, subject.get(dummyTarget1, DummyF2.class));
	assertEquals(dummyFeature1, subject.get(dummyTarget1, DummyF3.class));
	try{verify(factory,times(1)).newInstance(dummyTarget1);}
	catch(FeatureNotApplicableException e){}
    }
    
    @Test
    public void testGetFeatureMultiInterfaceSameFeatureGetsWithImposter() throws Throwable{
	final FeaturesImpl subject = getSubject();
	final DummyType1 dummyTarget1 = mock(DummyType1.class);
	
	final FeatureFactory desiredFactory = Mockito.mock(FeatureFactory.class);
	when(desiredFactory.getFeatureClass()).thenReturn (DummyMultiIfaceFeature1.class);
	when(desiredFactory.getTargetClass()) .thenReturn (DummyType1.class);
	final DummyMultiIfaceFeature1 desiredFeature1   = mock(DummyMultiIfaceFeature1.class);
	when(desiredFactory.newInstance(dummyTarget1)).thenReturn (desiredFeature1);
	
	final FeatureFactory impostorFactory = Mockito.mock(FeatureFactory.class);
	when(impostorFactory.getFeatureClass()).thenReturn (DummyF1.class);
	when(impostorFactory.getTargetClass()) .thenReturn (DummyType1.class);
	final DummyF1 impostorFeature1   = mock(DummyF1.class);
	when(impostorFactory.newInstance(dummyTarget1)).thenReturn (impostorFeature1);
	
	subject.registerFeature(desiredFactory);
	subject.registerFeature(impostorFactory);
	
	subject.init(dummyTarget1);
	
	assertEquals(desiredFeature1, subject.get(dummyTarget1, DummyF2.class));
	assertEquals(desiredFeature1, subject.get(dummyTarget1, DummyF3.class));
	assertEquals(impostorFeature1, subject.get(dummyTarget1, DummyF1.class));
	verify(desiredFactory,times(1)).newInstance(dummyTarget1);
	verify(impostorFactory,times(1)).newInstance(dummyTarget1);
    }
    
    private static interface DummyType1 {
    }//end DummyType1

    public static interface DummyF1 extends Feature<DummyType1> {
    }//end DummyF1
    
    public static interface DummyF2 extends Feature<DummyType1> {
    }//end DummyF2
    
    public static interface DummyF3 extends Feature<DummyType1> {
    }//end DummyF3
    
    public static interface DummyMultiIfaceFeature1 extends DummyF2, DummyF3 {
    }//end DummyMultiIfaceFeature1
}//end FeaturesImplTest
