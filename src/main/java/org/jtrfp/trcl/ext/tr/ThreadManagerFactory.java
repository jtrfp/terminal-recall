package org.jtrfp.trcl.ext.tr;

import org.jtrfp.trcl.core.Feature;
import org.jtrfp.trcl.core.FeatureFactory;
import org.jtrfp.trcl.core.TRFactory.TR;
import org.jtrfp.trcl.core.ThreadManager;
import org.springframework.stereotype.Component;

@Component
public class ThreadManagerFactory implements FeatureFactory<TR> {
 public static final class ThreadManagerFeature extends ThreadManager implements Feature<TR>{

    ThreadManagerFeature(TR tr) {
	super();
    }

    @Override
    public void apply(TR target) {
	setTr(target);
	start();
    }

    @Override
    public void destruct(TR target) {
	// TODO Auto-generated method stub
	
    }
     
 }//end ThreadManagerFeature

@Override
public Feature<TR> newInstance(TR target) {
    return new ThreadManagerFeature(target);
}

@Override
public Class<TR> getTargetClass() {
    return TR.class;
}

@Override
public Class<? extends Feature> getFeatureClass() {
    return ThreadManagerFeature.class;
}
}//end ThreadManagerFactory
