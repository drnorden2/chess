package perft;

import java.util.ArrayList;
import java.util.List;


abstract public class Board {
	private int movesPlayed;
	private List<Integer> lastMoves =new ArrayList<Integer>();
	
	protected Board () {
		this.movesPlayed = 0;
	}

	protected Board (String gameAsString) {
		this();
	}
	
	
	public final int getMovesPlayed() {
		return movesPlayed;
	}
	
	public final int getTurn() {
		return movesPlayed%2;
	}
	
	public final boolean isGameOver() {
		return isWon()||isDraw();
	}
	public final boolean isDraw() {
		return getMoves() ==0;
	}
	
	public final void doMove(int move) {
		lastMoves.add(move); 
		setMove(move);
		movesPlayed++;
	}
	
	
	public final int getLastMove() {
		return (lastMoves.isEmpty()?null:lastMoves.get(lastMoves.size()-1));
	}
	
	public final int getMovesPlayedCount() {
		return lastMoves.size();
	}
	
	final void undoMove() {
		int move =lastMoves.get(lastMoves.size()-1); 
		lastMoves.remove(lastMoves.size()-1);
		unSetMove(move);
		movesPlayed--;
	}
	abstract public int[] getAttacks(int color);
	
	abstract public int getMoves();
	abstract public String getMoveStr(int move);
	abstract public int getHash();
	abstract public boolean isWon();
	abstract public void setMove(int move);
	abstract public int getTotalCount();

	public boolean setMoveByMoveStr(String notation) {
		int count = this.getMoves();
		for(int i=0;i<count;i++) {
			if(this.getMoveStr(i).equals(notation)) {
				this.doMove(i);
				return true;
			}
		}
		System.out.println("Move not found:"+notation);
		System.out.println(this);
		for(int i=0;i<count;i++) {
			System.out.println(i+":"+this.getMoveStr(i));
		}
		return false;
	}
	abstract public void unSetMove(int move);

}
