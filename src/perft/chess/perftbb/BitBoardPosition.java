package perft.chess.perftbb;

import perft.chess.Position;
import perft.chess.core.baseliner.BLVariableInt;
import perft.chess.core.baseliner.BLVariableLong;
import perft.chess.core.baseliner.BaseLiner;
import static perft.chess.Definitions.*;

public class BitBoardPosition implements Position{
	public final BLVariableLong[][] pieces;
	public final BLVariableLong[] allOfOneColor= new BLVariableLong[2];
	
	public final BLVariableLong untouched;
	public final BLVariableInt  enPassantePos;
	public BBAnalyzer analyzer = new BBAnalyzer(this);
	
	public final int depth =10;
	public final BaseLiner bl = new BaseLiner(1,1,20,depth,1000);
	private int colorAtTurn=COLOR_WHITE;
	
	public BitBoardPosition() {
		pieces  = new BLVariableLong[2][6];
		for(int i=0;i<pieces.length;i++) {
			for(int j=0;j<pieces[0].length;j++) {
				pieces[i][j]=new BLVariableLong(bl,0L);
			}
		}
		
		enPassantePos = new BLVariableInt(bl,-1); 
		untouched = new BLVariableLong(bl,0L);
		allOfOneColor[COLOR_WHITE] = new BLVariableLong(bl,0L);
		allOfOneColor[COLOR_BLACK] = new BLVariableLong(bl,0L);
		

	}
	
	@Override
	public void initialAddToBoard(int color, int type, int pos) {
		pieces[color][type].setBitTouchless(pos);
	}

	
	
	
	@Override
	public void setUntouched(int rank, int file) {
		untouched.setBit(getPosForRankFile(rank,file));
	}

	@Override
	public void setEnPassantePos(int enpassantePos) {
		this.enPassantePos.set(enpassantePos);
	}	
	
	@Override
	public void unSetMove(int move) {
		// TODO Auto-generated method stub
	}

	@Override
	public void setMove(int move) {
		// TODO Auto-generated method stub
		
	}
	
	@Override
	public void setInitialTurn(int color) {
		colorAtTurn = color;
	}

	
	
	
	
	
	@Override
	public int getMoves() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public int getHash() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public String getNotation(int index) {
		// TODO Auto-generated method stub
		return null;
	}


	
	
	@Override
	public void initialEval() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public int getColorAtTurn() {
		// TODO Auto-generated method stub
		return 0;
	}


	
	
	
	
	
	
	
	
	@Override
	public void checkGameState(int colorAtTurn) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void checkLegalMoves() {
		// TODO Auto-generated method stub
		
	}
	public String toString() {
		return analyzer.toString();
	}

}
