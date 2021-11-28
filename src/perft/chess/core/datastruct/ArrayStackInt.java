package perft.chess.core.datastruct;


public class ArrayStackInt {
	private int[]  stack;
	private int head=-1;
	public ArrayStackInt(int size) {
		stack = new int[size];
	}
	
	public void add(int val) {
		stack[++head] = val;
	}
	public int get() {
		return stack[head];
	}
	public int remove() {
		if(head ==-1) {
			System.out.println("HFGAIDL!");
			return -1;
		}
		int val = stack[head];
		head--;
		return val;
	}
	public ArrayStackInt reset() {
		head=-1;
		return this;
	}
	
	public int size() {
		return head+1;
	}
}
