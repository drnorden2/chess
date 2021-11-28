package perft.chess;


import perft.Board;
import perft.chess.core.Position;
import perft.chess.fen.Fen;

public class ChessBoard extends Board{
	private Position position;
	public ChessBoard() {
		Fen fen = new Fen();
		position = fen.getInitialPosition();
	}
	
	public ChessBoard(String fenStr) {
		Fen fen = new Fen();
		position = fen.getCustomPosition(fenStr);
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
		return position.getMove(index).getNotation();
	}
	@Override
	public int getHash() {
		return position.getHash();
	}
}
