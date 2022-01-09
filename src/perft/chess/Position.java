package perft.chess;

public interface Position {

	public int getMoveCount();

	public void unSetMove(int move);

	public void setMove(int move);

	public String getNotation(int index);

	
	void initialUntouched(int i, int h);

	void initialEnPassantePos(int enpassantePos);

	void initialTurn(int colorBlack);
	
	public void initialAddToBoard(int color, int type, int pos);

	void initialEval();


	
	
	void checkGameState(int colorAtTurn);

	void checkLegalMoves();
	
	int getColorAtTurn();

	int[] getAttacks(int color);

	public int getHash();

}
