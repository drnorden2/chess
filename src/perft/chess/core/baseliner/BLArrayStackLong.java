package perft.chess.core.baseliner;

public class BLArrayStackLong {
	private int[] touchedInLevel;
	private long[]  stack;
	private int head=-1;
	private BaseLiner bl;
	public BLArrayStackLong(BaseLiner bl,int size) {
		touchedInLevel = new int[size];
		stack = new long[size];
		this.bl = bl;
	}
	
	public boolean addAndTouched(long val) {
		if(head !=-1 )  {
			if(touchedInLevel[head]>= bl.level ) {
				stack[head] = val;
				return false;
			}
			if(stack[head] == val) {
				return false;
			}
		}
		touchedInLevel[++head] = bl.level;
		stack[head] = val;
		return true;
	
	}

	public boolean incrAndTouched() {
		long val = stack[head]+1L;
		return addAndTouched(val);
	}
	
	//get::((bits >> bitIndex) & 1L)==1L;
	public boolean getBit(int bitIndex) {
		return ((stack[head] >> bitIndex) & 1L)==1L;
	}
	
	
	public int updateIndices(int[] indices) {
		long copy = stack[head];
		int counter =0;
		while (copy != 0){
			int idx = 63-Long.numberOfLeadingZeros(copy); 
			indices[counter++] = idx;
			copy &= ~(1L << idx);
		}
		return counter;
	}
	
	
	//toggle::bits ^= 1L << bitIndex;
	public boolean toggleBitTouched(int bitIndex) {
		return addAndTouched(stack[head]^1L << bitIndex);
	}
	
	//set::bits |= 1L << bitIndex;
	public boolean setBitTouched(int bitIndex) {
		return addAndTouched(stack[head]|1L << bitIndex);
	}
	
	//unset::bits &= ~(1L << bitIndex);
	public boolean unsetBitTouched(int bitIndex) {
		return addAndTouched(stack[head]&~(1L << bitIndex));
	}
	//unset::bits &= ~(1L << bitIndex);
	public boolean moveBitTouched(int oldIndex, int newIndex) {
		return addAndTouched(((stack[head]&~(1L << oldIndex))|1L << newIndex));
	}
	
		
	public boolean decrAndTouched() {
		long val = stack[head]-1L;
		return addAndTouched(val);
	}
	
	
	public boolean xorAndTouched(long value) {
		long val = stack[head]^value;
		return addAndTouched(val);
	}

	public boolean orAndTouched(long value) {
		long val = stack[head]|value;
		return addAndTouched(val);
	}

	public boolean andNotTouched(long value) {
		long val = stack[head]&~value;
		return addAndTouched(val);
	}
	
	public long get() {
		return stack[head];
	}
	public long remove() {
		long val = stack[head];
		touchedInLevel[head--]=0;
		return val;
	}
	public int stackSize() {
		return head+1;
	}
	
}
