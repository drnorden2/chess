package perft.chess;

import perft.*;
import perft.Player.PlayerType;
import perft.chess.perftmb.MBPosition;

public class ChessGameFactory implements GameFactory {
	@Override
	public Board getInitialBoard() {
		return new ChessBoard(new MBPosition());
	}
	public Board getSpecificBoard(String FEN) {
		return new ChessBoard(new MBPosition(), FEN);
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
