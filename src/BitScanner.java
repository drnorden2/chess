
public class BitScanner {
	public static final long[] ONES = new long[64];
	public static void main (String[] args) {
		//testBitScan ();
		testBranchless();
	}
	
	public static void testBranchless() {
		long rounds = 1000000000L;
		int color = 1;
		int otherColor = 0;
		
		{
			long timeStamp = System.currentTimeMillis();
			for(int i=0;i<rounds;i++) {
				withIf(color,otherColor);
			}
			double time = ((double)(long)((System.currentTimeMillis()-timeStamp)/10)/100);
			System.out.println("Run 1 in "  + time +"s");
		}
		
		
		{
			long timeStamp = System.currentTimeMillis();
			for(int i=0;i<rounds;i++) {
				withBLC(color,otherColor);
			}
			double time = ((double)(long)((System.currentTimeMillis()-timeStamp)/10)/100);
			System.out.println("Run 2 in "  + time +"s");
		}			
	
	}
	
	static long test = 0L;	
	public static long withIf(int color, int otherColor) {
		if(color==1) {
			test|=color &~otherColor;
		}else {
			test|=color &~otherColor;
		}
		return test; 
	}
	public static long withBLC(int color, int otherColor) {
		return color*(test|=color &~otherColor) + otherColor*(test|=color &~otherColor);
		
	}
	
	
	public static void testBitScan () {
		
		for(int i=0;i<ONES.length;i++) {
			ONES[i]=1L<<i; 
		}
		
		long[] ONES = BitScanner.ONES;
		int[] indices = new int[64];
		long rounds = 100000000L;
		{
			long timeStamp = System.currentTimeMillis();
			for(int i=0;i<rounds;i++) {
				updateIndices1(indices,(long)i);
			}
			double time = ((double)(long)((System.currentTimeMillis()-timeStamp)/10)/100);
			System.out.println("Run 1 in "  + time +"s");
		}
		
		
		{
			long timeStamp = System.currentTimeMillis();
			for(int i=0;i<rounds;i++) {
				updateIndices2(indices,(long)i);
			}
			double time = ((double)(long)((System.currentTimeMillis()-timeStamp)/10)/100);
			System.out.println("Run 1 in "  + time +"s");
		}			
	}

	
	public static int updateIndices1(int[] indices, long bits) {
		int retVal = Long.bitCount(bits);
		int count = 0;
		switch (64 - retVal) {
		case 0:
			indices[count++] = Long.numberOfTrailingZeros(bits);bits &= bits - 1;
		case 1:
			indices[count++] = Long.numberOfTrailingZeros(bits);bits &= bits - 1;
		case 2:
			indices[count++] = Long.numberOfTrailingZeros(bits);bits &= bits - 1;
		case 3:
			indices[count++] = Long.numberOfTrailingZeros(bits);bits &= bits - 1;
		case 4:
			indices[count++] = Long.numberOfTrailingZeros(bits);bits &= bits - 1;
		case 5:
			indices[count++] = Long.numberOfTrailingZeros(bits);bits &= bits - 1;
		case 6:
			indices[count++] = Long.numberOfTrailingZeros(bits);bits &= bits - 1;
		case 7:
			indices[count++] = Long.numberOfTrailingZeros(bits);bits &= bits - 1;
		case 8:
			indices[count++] = Long.numberOfTrailingZeros(bits);bits &= bits - 1;
		case 9:
			indices[count++] = Long.numberOfTrailingZeros(bits);bits &= bits - 1;
		case 10:
			indices[count++] = Long.numberOfTrailingZeros(bits);bits &= bits - 1;
		case 11:
			indices[count++] = Long.numberOfTrailingZeros(bits);bits &= bits - 1;
		case 12:
			indices[count++] = Long.numberOfTrailingZeros(bits);bits &= bits - 1;
		case 13:
			indices[count++] = Long.numberOfTrailingZeros(bits);bits &= bits - 1;
		case 14:
			indices[count++] = Long.numberOfTrailingZeros(bits);bits &= bits - 1;
		case 15:
			indices[count++] = Long.numberOfTrailingZeros(bits);bits &= bits - 1;
		case 16:
			indices[count++] = Long.numberOfTrailingZeros(bits);bits &= bits - 1;
		case 17:
			indices[count++] = Long.numberOfTrailingZeros(bits);bits &= bits - 1;
		case 18:
			indices[count++] = Long.numberOfTrailingZeros(bits);bits &= bits - 1;
		case 19:
			indices[count++] = Long.numberOfTrailingZeros(bits);bits &= bits - 1;
		case 20:
			indices[count++] = Long.numberOfTrailingZeros(bits);bits &= bits - 1;
		case 21:
			indices[count++] = Long.numberOfTrailingZeros(bits);bits &= bits - 1;
		case 22:
			indices[count++] = Long.numberOfTrailingZeros(bits);bits &= bits - 1;
		case 23:
			indices[count++] = Long.numberOfTrailingZeros(bits);bits &= bits - 1;
		case 24:
			indices[count++] = Long.numberOfTrailingZeros(bits);bits &= bits - 1;
		case 25:
			indices[count++] = Long.numberOfTrailingZeros(bits);bits &= bits - 1;
		case 26:
			indices[count++] = Long.numberOfTrailingZeros(bits);bits &= bits - 1;
		case 27:
			indices[count++] = Long.numberOfTrailingZeros(bits);bits &= bits - 1;
		case 28:
			indices[count++] = Long.numberOfTrailingZeros(bits);bits &= bits - 1;
		case 29:
			indices[count++] = Long.numberOfTrailingZeros(bits);bits &= bits - 1;
		case 30:
			indices[count++] = Long.numberOfTrailingZeros(bits);bits &= bits - 1;
		case 31:
			indices[count++] = Long.numberOfTrailingZeros(bits);bits &= bits - 1;
		case 32:
			indices[count++] = Long.numberOfTrailingZeros(bits);bits &= bits - 1;
		case 33:
			indices[count++] = Long.numberOfTrailingZeros(bits);bits &= bits - 1;
		case 34:
			indices[count++] = Long.numberOfTrailingZeros(bits);bits &= bits - 1;
		case 35:
			indices[count++] = Long.numberOfTrailingZeros(bits);bits &= bits - 1;
		case 36:
			indices[count++] = Long.numberOfTrailingZeros(bits);bits &= bits - 1;
		case 37:
			indices[count++] = Long.numberOfTrailingZeros(bits);bits &= bits - 1;
		case 38:
			indices[count++] = Long.numberOfTrailingZeros(bits);bits &= bits - 1;
		case 39:
			indices[count++] = Long.numberOfTrailingZeros(bits);bits &= bits - 1;
		case 40:
			indices[count++] = Long.numberOfTrailingZeros(bits);bits &= bits - 1;
		case 41:
			indices[count++] = Long.numberOfTrailingZeros(bits);bits &= bits - 1;
		case 42:
			indices[count++] = Long.numberOfTrailingZeros(bits);bits &= bits - 1;
		case 43:
			indices[count++] = Long.numberOfTrailingZeros(bits);bits &= bits - 1;
		case 44:
			indices[count++] = Long.numberOfTrailingZeros(bits);bits &= bits - 1;
		case 45:
			indices[count++] = Long.numberOfTrailingZeros(bits);bits &= bits - 1;
		case 46:
			indices[count++] = Long.numberOfTrailingZeros(bits);bits &= bits - 1;
		case 47:
			indices[count++] = Long.numberOfTrailingZeros(bits);bits &= bits - 1;
		case 48:
			indices[count++] = Long.numberOfTrailingZeros(bits);bits &= bits - 1;
		case 49:
			indices[count++] = Long.numberOfTrailingZeros(bits);bits &= bits - 1;
		case 50:
			indices[count++] = Long.numberOfTrailingZeros(bits);bits &= bits - 1;
		case 51:
			indices[count++] = Long.numberOfTrailingZeros(bits);bits &= bits - 1;
		case 52:
			indices[count++] = Long.numberOfTrailingZeros(bits);bits &= bits - 1;
		case 53:
			indices[count++] = Long.numberOfTrailingZeros(bits);bits &= bits - 1;
		case 54:
			indices[count++] = Long.numberOfTrailingZeros(bits);bits &= bits - 1;
		case 55:
			indices[count++] = Long.numberOfTrailingZeros(bits);bits &= bits - 1;
		case 56:
			indices[count++] = Long.numberOfTrailingZeros(bits);bits &= bits - 1;
		case 57:
			indices[count++] = Long.numberOfTrailingZeros(bits);bits &= bits - 1;
		case 58:
			indices[count++] = Long.numberOfTrailingZeros(bits);bits &= bits - 1;
		case 59:
			indices[count++] = Long.numberOfTrailingZeros(bits);bits &= bits - 1;
		case 60:
			indices[count++] = Long.numberOfTrailingZeros(bits);bits &= bits - 1;
		case 61:
			indices[count++] = Long.numberOfTrailingZeros(bits);bits &= bits - 1;
		case 62:
			indices[count++] = Long.numberOfTrailingZeros(bits);bits &= bits - 1;
		case 63:
			indices[count++] = Long.numberOfTrailingZeros(bits);bits &= bits - 1;
		}
					

		return retVal;
	}

	public static int updateIndices5(int[] indices, long bits) {
		int retVal = Long.bitCount(bits);
		for(int i=0;i<retVal;i++) {
			bits ^= 1L << (indices[i] = 63 - Long.numberOfLeadingZeros(bits));			
		}
		return retVal ;
	}
	
	public static int updateIndices2(int[] indices, long bits) {
		int retVal = Long.bitCount(bits);
		for(int i=0;i<retVal;i++) {
			indices[i] = Long.numberOfTrailingZeros(bits);
			bits &= bits - 1;			
		}
		return retVal ;
	}
	
	public static int updateIndices3(int[] indices, long bits) {
		int retVal = Long.bitCount(bits);
		for(int i=0;i<retVal;i++) {
			bits ^= ONES[indices[i] = Long.numberOfTrailingZeros(bits)];			
		}
		return retVal ;
	}
	
}
