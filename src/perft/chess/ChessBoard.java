package perft.chess;

import perft.Board;
import perft.chess.fen.Fen;

import perft.chess.Position;

public class ChessBoard extends Board{
	private Position position;

	public ChessBoard(Position position) {
		Fen fen = new Fen();
		fen.loadInitialPosition(position);
	}
	
	public ChessBoard(Position position,String fenStr) {
		Fen fen = new Fen();
		this.position = position;
		fen.loadCustomPosition(position,fenStr);
	}

	@Override
	public void setMove(int move,boolean isSim, boolean isLast) {
		position.setMove(move, isSim, isLast);
	}
	
	public int[] getAttacks(int color) {
		return position.getAttacks(color);
	}

	@Override
	public void unSetMove(int move) {
		position.unSetMove(move);
	}
	
	@Override
	public String toString() {
		return position.toString();
	}
	
	@Override
	public int getMoves() {
		return position.getMoveCount();
	}
	
	@Override
	public boolean isWon() {
		// TODO Auto-generated method stub
		return false;
	}	
	
	@Override
	public String getMoveStr(int index) {
		return position.getNotation(index);
	}
	
	@Override
	public long getHash() {
		return position.getHash();
	}
	
	@Override
	public int getTotalCount() {
		return position.getTotalCount();
	}

	@Override
	public boolean isCheck() {
		// TODO Auto-generated method stub
		return position.isCheck();
	}	
	public double evaluate(int i) {
		return position.evaluate(i);
	}

	@Override
	public int getTurn() {
		// TODO Auto-generated method stub
		return position.getColorAtTurn();
	}
	
}
