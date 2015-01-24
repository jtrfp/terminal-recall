package org.jtrfp.trcl.math;

public interface Normalized {
    /**
     * Property for the normalized value of this angle, as represented by toNormalized()
     */
    public static final String NORMALIZED = "NORMALIZED";
    
    /**
     * Returns this object's normalized value in the range of [-Integer.MIN_VALUE,Integer.MAX_VALUE].
     * @return
     * @since Jan 23, 2015
     */
    public int toNormalized();
}
