package org.jtrfp.trcl;

import java.util.Collection;

public abstract class AbstractSubmitter<T> implements Submitter<T> {
    @Override
    public void submit(Collection<T> items){for(T item:items){submit(item);}}
}
