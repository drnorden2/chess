package perft.chess.core.datastruct;

public class BitBoard {
	private static final long ALL_ZEROS = (long)0b0000000000000000000000000000000000000000000000000000000000000000L;
	//private static final long[] SINGLE_BITS; 
	static {
	//	SINGLE_BITS 
	}
	
	int[] indices = new int[65];
	long bits = ALL_ZEROS;
	public void set(int pos) {
		
	}
	public void get(int pos) {
		
	}
	
	public int[] getIndicesReference() {
		return indices;
	}
	
	public void updateIndices() {
		int counter = 0;
		long copy = this.bits;
		while (copy!=ALL_ZEROS) {
			indices[counter]=Long.numberOfLeadingZeros(copy);
			
		}
		
	}
	
}
