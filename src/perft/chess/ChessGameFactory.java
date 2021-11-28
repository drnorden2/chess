package perft.chess;

import perft.*;
import perft.Player.PlayerType;

public class ChessGameFactory implements GameFactory {
	@Override
	public Board getInitialBoard() {
		return new ChessBoard();
	}
	public Board getSpecificBoard(String FEN) {
		return new ChessBoard(FEN);
	}
	@Override
	public BoardUI getBoardUI() {
		return new ChessBoardUI();
	}
	@Override
	public Player getPlayer(PlayerType playerType) {
		return new ChessPlayer(playerType);
	}

	
	
}
