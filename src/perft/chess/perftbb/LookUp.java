package perft.chess.perftbb;



public class LookUp {
	private BBMoveManager moveManager = new BBMoveManager();
	
	
	public long getMoveMask(int index) {
		return moveManager.moveMasks[index];
	}
	public Move[] getMoveMap(int index) {
		Move[] moves= moveManager.moves[index];
/*
		boolean found =false;
		for(int i=0;i<moves.length;i++) {
			if (moves[i]!=null) {
				found=true;
				break;
			}
		}
		if(!found) {
			throw new RuntimeException("Fishy stuff!");
		}*/
		return moves;
	}
	

}
