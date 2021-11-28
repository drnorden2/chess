package perft;

public abstract class Player {
	private PlayerType playerType;
	public enum PlayerType {
		HUMAN, RANDOM, AI_WEAK, AI_MEDIUM, AI_HARD
	}
	protected Player(PlayerType playerType) {
		this.playerType = playerType;
		initPlayer();
	}
	abstract public void initPlayer();
	abstract public int play(Board board);

}
