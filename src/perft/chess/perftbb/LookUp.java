package perft.chess.perftbb;

import perft.chess.core.datastruct.BitBoard;

public class LookUp {
	private BBMoveManager moveManager = new BBMoveManager();
	
	
	public long getMoveMask(int index) {
		return moveManager.moveMasks[index];
	}
	public Move[] getMoveMap(int index) {
		return moveManager.moves[index];
	}
	

}
