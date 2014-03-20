package org.jtrfp.trcl.math;

/**
 * Set of frqeuency-origneted math utilities. DCT functions are adapted from <a
 * href=http://unix4lyfe.org/dct/listing3.c/>. Special thanks to Emil Mikulic
 * Arai, Agui, Nakajima and Tim Kientzle for making these fast DCT algorithms
 * openly available.
 * 
 * @author Chuck Ritola
 * 
 */
public class Freq {
    /**
     * 
     * @param inVals
     * @param outCoeffs
     * @param xpos
     * @param ypos
     * @since Mar 20, 2014
     */

    public void dct8b8x8(byte[] inVals, byte[][]outCoeffs, final int inRasterWidth, final int xpos,
	    final int ypos) {
	int i;
	final int[][] rows = new int[8][8];
	final int c1 = 1004, s1 = 200, c3 = 851, s3 = 569, r2c6 = 554, r2s6 = 1337, r2 = 181;
	int x0, x1, x2, x3, x4, x5, x6, x7, x8;

	// Transform rows
	for (i = 0; i < 8; i++) {
	    final int rasterOffset = ypos + i * inRasterWidth;
	    x0 = inVals[xpos + 0 + rasterOffset];
	    x1 = inVals[xpos + 1 + rasterOffset];
	    x2 = inVals[xpos + 2 + rasterOffset];
	    x3 = inVals[xpos + 3 + rasterOffset];
	    x4 = inVals[xpos + 4 + rasterOffset];
	    x5 = inVals[xpos + 5 + rasterOffset];
	    x6 = inVals[xpos + 6 + rasterOffset];
	    x7 = inVals[xpos + 7 + rasterOffset];

	    // Stage 1
	    x8 = x7 + x0;
	    x0 -= x7;
	    x7 = x1 + x6;
	    x1 -= x6;
	    x6 = x2 + x5;
	    x2 -= x5;
	    x5 = x3 + x4;
	    x3 -= x4;

	    // Stage 2
	    x4 = x8 + x5;
	    x8 -= x5;
	    x5 = x7 + x6;
	    x7 -= x6;
	    x6 = c1 * (x1 + x2);
	    x2 = (-s1 - c1) * x2 + x6;
	    x1 = (s1 - c1) * x1 + x6;
	    x6 = c3 * (x0 + x3);
	    x3 = (-s3 - c3) * x3 + x6;
	    x0 = (s3 - c3) * x0 + x6;

	    // Stage 3
	    x6 = x4 + x5;
	    x4 -= x5;
	    x5 = r2c6 * (x7 + x8);
	    x7 = (-r2s6 - r2c6) * x7 + x5;
	    x8 = (r2s6 - r2c6) * x8 + x5;
	    x5 = x0 + x2;
	    x0 -= x2;
	    x2 = x3 + x1;
	    x3 -= x1;

	    // Stage 4 and output
	    rows[i][0] = x6;
	    rows[i][4] = x4;
	    rows[i][2] = x8 >> 10;
	    rows[i][6] = x7 >> 10;
	    rows[i][7] = (x2 - x5) >> 10;
	    rows[i][1] = (x2 + x5) >> 10;
	    rows[i][3] = (x3 * r2) >> 17;
	    rows[i][5] = (x0 * r2) >> 17;
	}

	// Transform columns
	for (i = 0; i < 8; i++) {
	    x0 = rows[0][i];
	    x1 = rows[1][i];
	    x2 = rows[2][i];
	    x3 = rows[3][i];
	    x4 = rows[4][i];
	    x5 = rows[5][i];
	    x6 = rows[6][i];
	    x7 = rows[7][i];

	    /* Stage 1 */
	    x8 = x7 + x0;
	    x0 -= x7;
	    x7 = x1 + x6;
	    x1 -= x6;
	    x6 = x2 + x5;
	    x2 -= x5;
	    x5 = x3 + x4;
	    x3 -= x4;

	    // Stage 2
	    x4 = x8 + x5;
	    x8 -= x5;
	    x5 = x7 + x6;
	    x7 -= x6;
	    x6 = c1 * (x1 + x2);
	    x2 = (-s1 - c1) * x2 + x6;
	    x1 = (s1 - c1) * x1 + x6;
	    x6 = c3 * (x0 + x3);
	    x3 = (-s3 - c3) * x3 + x6;
	    x0 = (s3 - c3) * x0 + x6;

	    // Stage 3
	    x6 = x4 + x5;
	    x4 -= x5;
	    x5 = r2c6 * (x7 + x8);
	    x7 = (-r2s6 - r2c6) * x7 + x5;
	    x8 = (r2s6 - r2c6) * x8 + x5;
	    x5 = x0 + x2;
	    x0 -= x2;
	    x2 = x3 + x1;
	    x3 -= x1;

	    // Stage 4 and output
	    outCoeffs[0][i] = (byte) ((x6 + 16) >> 3);
	    outCoeffs[4][i] = (byte) ((x4 + 16) >> 3);
	    outCoeffs[2][i] = (byte) ((x8 + 16384) >> 13);
	    outCoeffs[6][i] = (byte) ((x7 + 16384) >> 13);
	    outCoeffs[7][i] = (byte) ((x2 - x5 + 16384) >> 13);
	    outCoeffs[1][i] = (byte) ((x2 + x5 + 16384) >> 13);
	    outCoeffs[3][i] = (byte) (((x3 >> 8) * r2 + 8192) >> 12);
	    outCoeffs[5][i] = (byte) (((x0 >> 8) * r2 + 8192) >> 12);
	}
    }// end dct8b8x8

    void quantize(double[][] dct_buf) {
	int x, y;

	for (y = 0; y < 8; y++)
	    for (x = 0; x < 8; x++)
		if (x > 3 || y > 3)
		    dct_buf[y][x] = 0.0;
    }

    private static double coeffs(int n) {
	return n == 0 ? 1.0 / Math.sqrt(2.0) : 1.0;
    }

    public static void iDCT8b8x8(byte[][] inCoeffs, byte[] outVals, final int outRasterWidth,
	    final int xpos, final int ypos) {
	int u, v, x, y;
	for (y = 0; y < 8; y++)
	    for (x = 0; x < 8; x++) {
		double z = 0.0;

		for (v = 0; v < 8; v++)
		    for (u = 0; u < 8; u++) {
			double S, q;
			double Cu, Cv;

			Cu = coeffs(u);
			Cv = coeffs(v);
			S = inCoeffs[v][u];

			q = Cu
				* Cv
				* S
				* Math.cos((double) (2 * x + 1) * (double) u
					* Math.PI / 16.0)
				* Math.cos((double) (2 * y + 1) * (double) v
					* Math.PI / 16.0);

			z += q;
		    }

		z /= 4.0;
		if (z > 255.0)
		    z = 255.0;
		if (z < 0)
		    z = 0.0;

		outVals[x + xpos+(y + ypos)*outRasterWidth] = (byte) z;
	    }
    }// end iDCT8b8x8(...)
}// end Freq
