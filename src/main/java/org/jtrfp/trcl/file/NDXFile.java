package org.jtrfp.trcl.file;

import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class NDXFile {
    private List<Integer> widths = new ArrayList<Integer>();

    public NDXFile read(InputStream is) {
	final Scanner s = new Scanner(is);
	while (s.hasNext()) {
	    String line = s.nextLine();
	    widths.add(Integer.parseInt(line));
	}// end while(hasNext)
	return this;
    }// end read()

    public NDXFile write(OutputStream os) {
	final PrintStream ps = new PrintStream(os);
	for (Integer w : widths) {
	    ps.println(w);
	}
	return this;
    }

    /**
     * @return the widths
     */
    public List<Integer> getWidths() {
	return widths;
    }
    /**
     * Returns the width in pixels of the supplied ASCII value, if available.
     * @param asciiValue
     * @return	Width of the provided ASCII value, or -1 if unavailable.
     * @since Feb 27, 2014
     */
    public int asciiWidth(byte asciiValue){
	try{return widths.get(asciiValue-32);}
	catch(ArrayIndexOutOfBoundsException e){
	    return -1;
	}
    }//end asciiWidth

    /**
     * @param widths
     *            the widths to set
     */
    public void setWidths(List<Integer> widths) {
	this.widths = widths;
    }

}// end NDXFile
