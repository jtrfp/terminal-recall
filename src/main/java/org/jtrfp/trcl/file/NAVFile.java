package org.jtrfp.trcl.file;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;

import org.jtrfp.jfdt.ThirdPartyParseable;

public class NAVFile implements NAVData {
    private NAVData delegate;
    
    public NAVFile(InputStream is) throws IOException, IllegalAccessException {
	final byte [] bytes = is.readAllBytes();
	//Try TVF3 version first
	try {
	    delegate = new TVF3NAVFile(new ByteArrayInputStream(bytes));
	} catch(Exception tvf3Exception) {
	    //Hellbender version
	    delegate = new HellbenderNAVFile(new ByteArrayInputStream(bytes));
	}
    }

    public ThirdPartyParseable getDelegate() {
        return (ThirdPartyParseable)delegate;
    }

    public void setDelegate(ThirdPartyParseable delegate) {
	if(!(delegate instanceof NAVData))
	    throw new IllegalArgumentException("Delegate must implement NAVData");
        this.delegate = (NAVData)delegate;
    }

    public int getNumNavigationPoints() {
	return delegate.getNumNavigationPoints();
    }

    public void setNumNavigationPoints(int numNavigationPoints) {
	delegate.setNumNavigationPoints(numNavigationPoints);
    }

    public List<? extends NAVSubObjectData> getNavObjects() {
	for(NAVSubObjectData nav : delegate.getNavObjects()) {
	    System.out.println("NAV: "+nav.getClass().getSimpleName());
	}
	return delegate.getNavObjects();
    }

    public void setNavObjects(List<? extends NAVSubObjectData> newObjects) {
	delegate.setNavObjects(newObjects);
    }

}//end NAVFile
