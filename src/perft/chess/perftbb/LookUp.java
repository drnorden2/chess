package perft.chess.perftbb;

import perft.chess.core.datastruct.BitBoard;

public class LookUp {
	private int[] indices = new int[64];
	BitBoard bb = new BitBoard(0L);
	private BBMoveManager moveManager = new BBMoveManager();
	
	
	public long getRawMoves(int index) {
		return moveManager.moveMasks[index];
	}

}
