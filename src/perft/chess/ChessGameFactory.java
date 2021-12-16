package perft.chess;

import perft.*;
import perft.Player.PlayerType;
import perft.chess.perftbb.BBPosition;
import perft.chess.perftmb.MailBoxPosition;


public class ChessGameFactory implements GameFactory {
	private final static boolean bitBoard = true;
	@Override
	public Board getInitialBoard() {
		return new ChessBoard(bitBoard?new BBPosition():new MailBoxPosition());
	}
	public Board getSpecificBoard(String FEN) {
		return new ChessBoard(bitBoard?new BBPosition():new MailBoxPosition(), FEN);
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
