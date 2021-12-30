package perft.chess;

public interface Position {

	public void unSetMove(int move);

	public void setMove(int move);

	public int getMoves();

	public int getHash();

	//public Object getMove(int index);

	public String getNotation(int index);

	public void initialAddToBoard(int color, int type, int pos);
		
	void setUntouched(int i, int h);

	void setEnPassantePos(int enpassantePos);

	void initialEval();

	int getColorAtTurn();

	void checkGameState(int colorAtTurn);

	void checkLegalMoves();

	void setInitialTurn(int colorBlack);
	
	int[] getAttacks(int color);


}
