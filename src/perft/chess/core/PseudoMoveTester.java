package perft.chess.core;
import perft.chess.core.datastruct.IndexedList;

public class PseudoMoveTester {
	final IndexedList <MoveTracker>allTracker;
	public PseudoMoveTester() {	
		allTracker = new IndexedList<MoveTracker>(new MoveTracker[16 * 28], 6000);// TBD reduce
		
	}
	
	
	
	public void reset() {
		allTracker.removeAll();
	}
}
