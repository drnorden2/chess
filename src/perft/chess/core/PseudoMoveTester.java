package perft.chess.core;
import perft.chess.core.datastruct.IndexedListInt;

public class PseudoMoveTester {
	final IndexedListInt moves;
	final IndexedListInt cbs;
	Position position;
	public PseudoMoveTester(Position position) {	
		this.position=position;
		moves = new IndexedListInt(16 * 28, 6000);
		cbs = new IndexedListInt(16 * 28, 6000);
	}
	
	public void addMove(int pos, int moveIndex) {
		moves.add(moveIndex);
	}
	public void removeMove(int pos, int moveIndex) {
		if(!moves.contains(moveIndex)) {
			position.fields[pos].containsPseudoMove(moveIndex);
		}
		moves.remove(moveIndex);
	}
	
	
	
	public void reset() {
		cbs.removeAll();
		moves.removeAll();
		
	}
}
