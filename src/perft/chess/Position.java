package perft.chess;

public interface Position {

	public int getMoveCount();
	
	public int getTotalCount();

	public void unSetMove(int move);

	public void setMove(int move);

	public String getNotation(int index);

	
	void initialUntouched(int i, int h);

	void initialEnPassantePos(int enpassantePos);

	void initialTurn(int colorBlack);
	
	public void initialAddToBoard(int color, int type, int pos);

	void initialEval();

	public boolean isCheck();


	
	
	void checkGameState(int colorAtTurn);

	void checkLegalMoves();
	public double evaluate(int i);
	
	int getColorAtTurn();

	int[] getAttacks(int color);

	public long getHash();

}
