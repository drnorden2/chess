package perft.chess.perftbb;

import static perft.chess.Definitions.*;

import perft.chess.Position;
import perft.chess.core.baseliner.BLVariableInt;
import perft.chess.core.baseliner.BLVariableLong;
import perft.chess.core.baseliner.BaseLiner;
import perft.chess.core.datastruct.BitBoard;

public class BBPosition implements Position{
	public final BLVariableLong[][] pieces;
	public final BLVariableLong[] allOfOneColor= new BLVariableLong[2];	
	public final BLVariableLong untouched;
	public final BLVariableInt  enPassantePos;
	public BBAnalyzer analyzer = new BBAnalyzer(this);
	public LookUp lookUp = new LookUp();
	
	public final int depth =10;
	public final BaseLiner bl = new BaseLiner(1,1,20,depth,1000);
	private int colorAtTurn=COLOR_WHITE;
	
	public BBPosition() {
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
		allOfOneColor[color].setBitTouchless(pos);
	}

	
	
	
	@Override
	public void setUntouched(int rank, int file) {
		untouched.setBit(getPosForFileRank(file,rank));
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
		long[][] rawMoves = new long[9][];
		long allWhite = this.allOfOneColor[COLOR_WHITE].get();
		long allBlack = this.allOfOneColor[COLOR_BLACK].get();
		long notAll = ~(allWhite|allBlack);
		
		int color = COLOR_WHITE;
		{
			int type = PIECE_TYPE_PAWN;
			long pawns  = pieces[COLOR_WHITE][PIECE_TYPE_PAWN].get();
			int count = lookUp.getRawMoves(pawns, color, type, rawMoves);
		//	moveWhitePawns(rawMoves,count, pawns, notAll,allBlack,allWhite,color, type); 
		}
		{
			int type = PIECE_TYPE_ROOK;
			long rooks  = pieces[COLOR_WHITE][PIECE_TYPE_ROOK].get() |pieces[COLOR_WHITE][PIECE_TYPE_QUEEN].get();
			int count = lookUp.getRawMoves(rooks, color, type, rawMoves);
			moveWhiteRooks(rawMoves,count, rooks, notAll,allBlack,allWhite,color, type); 
		}
		
		
		
		
		
		/*
		for(int color=0;color<this.pieces.length;color++) {
			for(int type=0;type<this.pieces[color].length;type++) {
				int count = lookUp.getRawMoves(pieces[color][type].get(),color, type,rawMoves);
			
			}
		}*/
	}

	public void moveWhiteRooks(long[][] rawMoves,int count, long rooks, long notAll,long allBlack,long allWhite,int color, int type) {
		//do rook stuff
		System.out.println("before"+BitBoard.toString(rooks));
		long all = 0L;
		long up = rooks;
		long down =rooks;
		long left = rooks;
		long right = rooks;
		long notAllWhite =~allWhite;
		long notAllBlack =~allBlack;
		long allWAttacks1 =0L;
		long allWAttacks2 =0L;
		long[] pseudoMoves = new long[count];
		for(int j=0;j<count;j++) {
			System.out.println("all rook "+BitBoard.toString(rawMoves[j][0]));
		}
		
		for(int i=0;i<7;i++) {
			if(up!=0L) { 
				up = (up >> DIR_UP) & notAllWhite & MASK_NOT_1_RANK;
				allWAttacks2 |= up & allWAttacks1;
				allWAttacks1 |= up;
				for(int j=0;j<count;j++) {
					pseudoMoves[j] |= rawMoves[j][0]&up ;//Beats
				}
				//System.out.println("up"+BitBoard.toString(up));
				up &= notAllBlack;
			}
			if(right!=0L) { 
				right = (right << DIR_LEFT) & notAllWhite & MASK_NOT_A_FILE;
				allWAttacks2 |= right & allWAttacks1;
				allWAttacks1 |= right ;
				for(int j=0;j<count;j++) {
					pseudoMoves[j] |= rawMoves[j][0]&right ;//Beats
				}
				right &= notAllBlack;
			}
			if(down !=0L) { 
				down = (down << DIR_UP) & notAllWhite & MASK_NOT_8_RANK;
				allWAttacks2 |= down & allWAttacks1;
				allWAttacks1 |= down ;
				for(int j=0;j<count;j++) {
					pseudoMoves[j] |= rawMoves[j][0]&down;//Beats
				}
			
				//System.out.println("down"+BitBoard.toString(down));
				down &= notAllBlack;
			}
			if(left !=0L) { 
				left = (left >> DIR_LEFT) & notAllWhite & MASK_NOT_H_FILE;
				allWAttacks2 |= left & allWAttacks1;
				allWAttacks1 |= left ;
				for(int j=0;j<count;j++) {
					pseudoMoves[j] |= rawMoves[j][0]&left;
				}
			
				//System.out.println("left"+BitBoard.toString(left));
				left &= notAllBlack;
				
			}
			
			 
		}
		for(int j=0;j<count;j++) {
			System.out.println("all rook "+BitBoard.toString(pseudoMoves[j]));
		}
		
		
		///System.out.println("all rook moves"+BitBoard.toString(all));
		//System.out.println("all Pawn attacks 1"+BitBoard.toString(allWAttacks1));
		//System.out.println("all Pawn attacks 2"+BitBoard.toString(allWAttacks2));

	}

	
	
	public void moveWhitePawns(long[][] rawMoves,int count, long pawns, long notAll,long allBlack,long allWhite,int color, int type) {
		//do pawn stuff
		//System.out.println("before"+BitBoard.toString(pawns));
		if(color == COLOR_WHITE) {
			long up = pawns >> DIR_UP & notAll;
			//System.out.println("Up x1"+BitBoard.toString(up));
			long up2 = (up & MASK_3_RANK)>> DIR_UP & notAll;
			//System.out.println("Up x2"+BitBoard.toString(up2));
			long allUp = up|up2;
			//System.out.println("allUp"+BitBoard.toString(allUp));
			
			long all = 0L;
			long allWAttacks1 =0L;
			long allWAttacks2 =0L;
							
			for(int i=0;i<count;i++) {
				allWAttacks2 |= rawMoves[i][0]&allWAttacks1;
				allWAttacks1|=rawMoves[i][0];
				rawMoves[i][0]&=allBlack;//Beats
				//System.out.println("beat("+i+")"+BitBoard.toString(rawMoves[i][0]));
				rawMoves[i][1]&=allUp;//Beats
				//System.out.println("up("+i+")"+BitBoard.toString(rawMoves[i][1]));					
				all |= rawMoves[i][0]|rawMoves[i][1];
			}
			System.out.println("all Pawn moves"+BitBoard.toString(all));
			System.out.println("all Pawn attacks 1"+BitBoard.toString(allWAttacks1));
			System.out.println("all Pawn attacks 2"+BitBoard.toString(allWAttacks2));
			
		}
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
