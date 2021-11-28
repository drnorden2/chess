package perft;

import perft.Player.PlayerType;

public interface GameFactory {
	public Board getInitialBoard();
	public Board getSpecificBoard(String FEN);
	public BoardUI getBoardUI();
	public Player getPlayer(PlayerType playerType);
	
}
