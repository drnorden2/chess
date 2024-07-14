package perft.chess;

public interface Position {

	public int getMoveCount();
	
	public int getTotalCount();

	public void unSetMove(int move);

	public void setMove(int move,boolean isSim, boolean isLast);

	public String getNotation(int index);

	public int getPiece(int index);
	
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
	
	public boolean isUntouched(int index);
	
	public long getHash();
	public int getEnpassantePos();
}
