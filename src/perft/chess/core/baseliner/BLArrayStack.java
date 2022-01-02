package perft.chess.core.baseliner;

public class BLArrayStack {
	private int[] touchedInLevel;
	private Object[]  stack;
	private int head=-1;
	private BaseLiner bl;
	public BLArrayStack(BaseLiner bl,int size) {
		touchedInLevel = new int[size];
		stack = new Object[size];
		this.bl = bl;
	}
	
	public boolean addAndTouched(Object o) {
		if(head !=-1 )  {
			if(touchedInLevel[head]>= bl.level ) {
				stack[head] = o;
				return false;
			}
			if(stack[head] == o) {
				return false;
			}
		}
		touchedInLevel[++head] = bl.level;
		stack[head] = o;
		return true;
	
	}
	public Object get() {
		return stack[head];
	}
	public Object remove() {
		Object val = stack[head];
		touchedInLevel[head--]=0;
		return val;
	}
	public int stackSize() {
		return head+1;
	}
	
}
