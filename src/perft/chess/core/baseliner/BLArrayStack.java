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
		boolean touched =false;

		int level = this.bl.getLevel();
		if(head ==-1 || touchedInLevel[head]< level) {
			touchedInLevel[++head] = level;
			touched =true;
		}
		stack[head] = o;
		return touched;
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
