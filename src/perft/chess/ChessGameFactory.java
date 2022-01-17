package perft.chess;

import perft.*;
import perft.Player.PlayerType;
import perft.chess.perftbb.BBPosition;
import perft.chess.perftmb.MBPosition;


public class ChessGameFactory implements GameFactory {
	public static boolean bitBoard = true;
	@Override
	public Board getInitialBoard(int depth) {
		return new ChessBoard(bitBoard?new BBPosition(depth):new MBPosition());
	}
	public Board getSpecificBoard(String FEN,int depth) {
		return new ChessBoard(bitBoard?new BBPosition(depth):new MBPosition(), FEN);
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
