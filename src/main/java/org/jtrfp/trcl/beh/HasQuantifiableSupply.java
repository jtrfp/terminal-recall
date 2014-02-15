package org.jtrfp.trcl.beh;

import org.jtrfp.trcl.beh.DamageableBehavior.SupplyNotNeededException;

public interface HasQuantifiableSupply {
    public void addSupply(double amount) throws SupplyNotNeededException;
    public double getSupply();
}
