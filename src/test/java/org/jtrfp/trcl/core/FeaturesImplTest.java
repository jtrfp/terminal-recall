package org.jtrfp.trcl.core;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

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
    public void testDeRegisterFeature() {
	final FeatureFactory factory = Mockito.mock(FeatureFactory.class);
	when(factory.getFeatureClass()).thenReturn (DummyF1.class);
	when(factory.getTargetClass()) .thenReturn (DummyType1.class);
	final FeaturesImpl subject = getSubject();
	subject.deRegisterFeature(factory);
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
    
    @Test
    public void testGetFeatureByPath() throws Throwable {
	final FeaturesImpl subject = getSubject();
	final DummyType1 dummyTarget0 = mock(DummyType1.class);
	
	final FeatureFactory level0Factory = Mockito.mock(FeatureFactory.class);
	when(level0Factory.getFeatureClass()).thenReturn (DummyF3.class);
	when(level0Factory.getTargetClass()) .thenReturn (DummyType1.class);
	final DummyF3 level0Feature        = mock(DummyF3.class);
	when(level0Factory.newInstance(dummyTarget0)).thenReturn (level0Feature);
	
	final FeatureFactory level1Factory = Mockito.mock(FeatureFactory.class);
	when(level1Factory.getFeatureClass()).thenReturn (DummyF3F1.class);
	when(level1Factory.getTargetClass()) .thenReturn (DummyF3.class);
	final DummyF3F1 level1Feature   = mock(DummyF3F1.class);
	when(level1Factory.newInstance(level0Feature)).thenReturn (level1Feature);
	
	subject.registerFeature(level0Factory);
	subject.registerFeature(level1Factory);
	
	subject.init(dummyTarget0);
	
	assertEquals(level1Feature, subject.getByPath(dummyTarget0, DummyF3F1.class, DummyF3.class));
	//assertEquals(desiredFeature1, subject.get(dummyTarget1, DummyF3.class));
	//assertEquals(impostorFeature1, subject.get(dummyTarget1, DummyF1.class));
	verify(level0Factory,times(1)).newInstance(dummyTarget0);
	verify(level1Factory,times(1)).newInstance(level0Feature);
    }//end testGetFeatureByPath()
    
    private static interface DummyType1 {
    }//end DummyType1

    public static interface DummyF1 extends Feature<DummyType1> {
    }//end DummyF1
    
    public static interface DummyF2 extends Feature<DummyType1> {
    }//end DummyF2
    
    public static interface DummyF3 extends Feature<DummyType1> {
    }//end DummyF3
    
    public static interface DummyF3F1 extends Feature<DummyF3> {
    }//end DummyF3
    
    public static interface DummyMultiIfaceFeature1 extends DummyF2, DummyF3 {
    }//end DummyMultiIfaceFeature1
}//end FeaturesImplTest
