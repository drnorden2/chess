package perft.ttt;

import perft.Board;
import perft.BoardUI;
import perft.GameFactory;
import perft.Player;
import perft.Player.PlayerType;

public class TttGameFactory implements GameFactory{
	@Override
	public Board getInitialBoard() {
		return new TttBoard();
	}
	public Board getSpecificBoard(String FEN) {
		return new TttBoard(FEN);
	}
	@Override
	public BoardUI getBoardUI() {
		return new TttBoardUI();
	}
	@Override
	public Player getPlayer(PlayerType playerType) {
		return new TttPlayer(playerType);
	}

	
	
}
