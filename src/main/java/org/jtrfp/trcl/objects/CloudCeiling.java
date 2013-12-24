package org.jtrfp.trcl.objects;

import org.jtrfp.trcl.Model;
import org.jtrfp.trcl.core.TR;

public class CloudCeiling extends WorldObject {

    public CloudCeiling(TR tr, Model m) {
	super(tr, m);
	m.setDebugName("Cloud Ceiling");
    }

}
