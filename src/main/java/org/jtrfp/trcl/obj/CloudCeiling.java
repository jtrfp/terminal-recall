package org.jtrfp.trcl.obj;

import org.jtrfp.trcl.core.TR;
import org.jtrfp.trcl.gpu.Model;

public class CloudCeiling extends WorldObject {

    public CloudCeiling(TR tr, Model m) {
	super(tr, m);
	m.setDebugName("Cloud Ceiling");
    }

}
