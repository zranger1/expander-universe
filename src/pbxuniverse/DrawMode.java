package pbxuniverse;

/**
 * Supported LED pixel drawing modes.<p>
 * FAST mode corrects for brigthness and gamma, and converts RGB to RGBW if necessary.<p>
 * ENHANCED mode adds user configurable per-channel color correction and expansion of 
 * Processing's 8 bits/pixel color to enhanced color for LED types that support it.
 * <p>  
 * TODO - support simple scale/rotate/translate in ENHANCED mode too?  Or point users to the
 * processing API to handle this.  Can we sneakily get Processing to do the work for us,
 * complete w/pushMatrix & popMatrix?  That'd be cool.
 * 
 */
public enum DrawMode {
	FAST,
	ENHANCED
}
