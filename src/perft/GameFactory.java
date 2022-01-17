package perft;

import perft.Player.PlayerType;

public interface GameFactory {
	public Board getInitialBoard(int depth);
	public Board getSpecificBoard(String FEN, int depth);
	public BoardUI getBoardUI();
	public Player getPlayer(PlayerType playerType);
	
}
