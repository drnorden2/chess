package perft.chess.perftbb;

import perft.chess.core.datastruct.BitBoard;

public class LookUp {
	private int[] indices = new int[64];
	BitBoard bb = new BitBoard(0L);
	private BBMoveManager moveManager = new BBMoveManager();
	
	
	public int getRawMoves(long pieces, int color, int type, long[][] rawMoves) {
		bb.reset(pieces);
		//System.out.println("Pieces:\n"+bb.toString(pieces)+"______________\n");
		int count = bb.updateIndices(indices);		
		for(int i=0;i<count;i++) {
			int index = (indices[i]+(type * 2 + color) * 64);
			rawMoves[i]=moveManager.moveMasks[index];
			//System.out.println(bb.toString(rawMoves[i])+"\n");
			//System.out.println(bb.toStringLine(rawMoves[i])+"\n");
		}
		return count;
	}

}
