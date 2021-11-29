package perft.chess.core.datastruct;

public class ArrayStack <T>{
	private T[]  stack;
	private int head=-1;
	public ArrayStack(T[] stack) {
		this.stack = stack;
	}
	
	public void add(T val) {
		stack[++head] = val;
	}
	
	public T get(int i) {
		return stack[i];
	}
	public T get() {
		return stack[head];
	}
	public T remove() {
		if(head ==-1) {
			System.out.println("HFGAIDL!");
			return null;
		}
		T val = stack[head];
		head--;
		return val;
	}
	public ArrayStack reset() {
		head=-1;
		return this;
	}
	
	public int size() {
		return head+1;
	}
}
