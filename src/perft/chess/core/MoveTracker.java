package perft.chess.core;

import perft.chess.core.datastruct.IndexedElement;

class MoveTracker implements IndexedElement{
	private Move move;
	private int count;
	public MoveTracker (Move move) {
		this.move= move;
	}
	public int getElementIndex() {
		return move.getElementIndex();
	}
	public String toString () {
		return "" + move+": "+count;
	}
	public void incr() {
		count++;
	}
	public void decr() {
		count--;
	}
	public void setCount(int count) {
		this.count=count;
	}
	public int getCount() {
		return count;
	}
	
}
