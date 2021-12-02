package perft.chess.core.datastruct;

import java.util.Arrays;

public class BitBoard {
	private static final long ALL_ZEROS = (long) 0b0000000000000000000000000000000000000000000000000000000000000000L;
	private static final long SOME_ONES = (long) 0b0101010101010101010101010101010101010101010101010101010101010101L;

	long bits = ALL_ZEROS;

	public static void main(String args[]) {
		System.out.println("Bitboard: 1234567812345678123456781234567812345678123456781234567812345678");

		BitBoard bb = new BitBoard(ALL_ZEROS);
		System.out.println("Bitboard: " + bb);
		bb.set(1-1);
		bb.set(2-1);
		bb.set(4-1);
		bb.set(8-1);
		bb.set(16-1);
		bb.set(32-1);
		bb.set(64-1);
		System.out.println("toString2:"+bb);
		System.out.println("toString1:"+bb.toString2());
	}

	public BitBoard(long bits) {
		this.bits = bits;
	}

	public void set(int pos) {
		bits |= 1L << pos;
	}

	public void unset(int pos) {
		bits &= ~(1L << pos);

	}
	public boolean get(int pos) {
		return (int)((bits >> pos) & 1L)==1;
	}

	public void toggle(int pos) {
		bits ^= 1L << pos;
	}
	public int popCount() {
		return Long.bitCount(bits);
	}
	
	public void updateIndices(int[] indices) {
		indices[0] = 0;
		long copy = this.bits;

		while (copy != 0){
			int idx = 63-Long.numberOfLeadingZeros(copy); 
			indices[++indices[0]] = idx;
			copy &= ~(1L << idx);
		}
	}
	
	public String toString() {
		String out = "";
		for(int i = 0; i < Long.numberOfLeadingZeros((long)bits); i++) {
	      out+='0';
		}
		if(bits!=0) {
			out +=(Long.toBinaryString((long)bits));
		}
		return out;
	}
	
	public String toString2() {
		int[] indices = new int[65];
		this.updateIndices(indices);
		//System.out.println("Indices:" + Arrays.toString(indices));
		String out = "";
		int next1 =-1;
		int count = indices[0];
		if(count>0) {
			next1 = indices[count--];
		}
		for (int i = 0; i < 64; i++) {
			if(next1!=i) {
				out = "0"+out;
			}else {
				out = "1"+out;
				if(count>0) {
					next1 = indices[count--];
				}
			}
		}
		
		int size = 64 - out.length();
		for (int j = 0; j < size; j++) {
			out += "0";
		}
		return out;
	}
	public void reset() {
		this.bits=0L;
	}
	public void reset(long bits) {
		this.bits=bits;
	}
	public long getBits() {
		return bits;
	}
	
}
