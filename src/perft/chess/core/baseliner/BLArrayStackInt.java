package perft.chess.core.baseliner;

public class BLArrayStackInt {
	private int[] touchedInLevel;
	private int[]  stack;
	private int head=-1;
	private BaseLiner bl;
	public BLArrayStackInt(BaseLiner bl,int size) {
		touchedInLevel = new int[size];
		stack = new int[size];
		this.bl = bl;
	}
	
	public boolean addAndTouched(int val) {
		boolean touched =false;

		int level = this.bl.getLevel();
		if(head ==-1 || touchedInLevel[head]==0 ||touchedInLevel[head]< level) {
			head++;
			touchedInLevel[head] = level;
			touched =true;
		}
		stack[head] = val;
		return touched;
	}

	public boolean incrAndTouched() {
		int val = stack[head]+1;
		return addAndTouched(val);
	}
	public boolean additionAndTouched(int value) {
		int val = stack[head]+value;
		return addAndTouched(val);
	}
	public boolean subtractionAndTouched(int value) {
		int val = stack[head]-value;
		return addAndTouched(val);
	}

	
	
	public boolean decrAndTouched() {
		int val = stack[head]-1;
		return addAndTouched(val);
	}
	
	
	public boolean xorAndTouched(int value) {
		int val = stack[head]^value;
		return addAndTouched(val);
	}

	
	public int get() {
		return stack[head];
	}
	public int remove() {
		int val = stack[head];
		touchedInLevel[head]=0;
		head--;
		return val;
	}
	public int stackSize() {
		return head+1;
	}
	
}
