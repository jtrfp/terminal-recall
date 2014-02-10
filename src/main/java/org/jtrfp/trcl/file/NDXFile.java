package org.jtrfp.trcl.file;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class NDXFile{
    private List<Integer> widths = new ArrayList<Integer>();
    public NDXFile(InputStream is){
	final Scanner s = new Scanner(is);
	while(s.hasNext()){
	    String line = s.nextLine();
	    widths.add(Integer.parseInt(line));
	}//end while(hasNext)
    }//end describeFormat(...)
    /**
     * @return the widths
     */
    public List<Integer> getWidths() {
        return widths;
    }
    /**
     * @param widths the widths to set
     */
    public void setWidths(List<Integer> widths) {
        this.widths = widths;
    }

}//end NDXFile
