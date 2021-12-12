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
	public void setMove(int move) {
		position.setMove(move);
	}
	

	@Override
	public void unSetMove(int move) {
		position.unSetMove(move);
	}

	
	
	public String toString() {
		return position.toString();
	}
	
	@Override
	public int getMoves() {
		return position.getMoves();
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
	public int getHash() {
		return position.getHash();
	}
}
