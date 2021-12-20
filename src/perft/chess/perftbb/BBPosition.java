package perft.chess.perftbb;

import static perft.chess.Definitions.*;

import perft.chess.Position;
import perft.chess.core.baseliner.BLVariableInt;
import perft.chess.core.baseliner.BLVariableLong;
import perft.chess.core.baseliner.BaseLiner;
import perft.chess.core.datastruct.BitBoard;
import perft.chess.perftbb.gen.MagicNumberFinder;

public class BBPosition implements Position {
	MagicNumberFinder mnf = new MagicNumberFinder();

	public final BLVariableLong[][] pieces;

	public final BLVariableLong[] allOfOneColor = new BLVariableLong[2];
	public final BLVariableLong untouched;
	public final BLVariableInt enPassantePos;
	public BBAnalyzer analyzer = new BBAnalyzer(this);
	public LookUp lookUp = new LookUp();

	public final int depth = 10;
	public final BaseLiner bl = new BaseLiner(1, 1, 20, depth, 1000);
	private int colorAtTurn = COLOR_WHITE;

	public BBPosition() {
		pieces = new BLVariableLong[2][6];
		for (int i = 0; i < pieces.length; i++) {
			for (int j = 0; j < pieces[i].length; j++) {
				pieces[i][j] = new BLVariableLong(bl, 0L);
			}
		}

		enPassantePos = new BLVariableInt(bl, -1);
		untouched = new BLVariableLong(bl, 0L);
		allOfOneColor[COLOR_WHITE] = new BLVariableLong(bl, 0L);
		allOfOneColor[COLOR_BLACK] = new BLVariableLong(bl, 0L);
	}

	@Override
	public void initialAddToBoard(int color, int type, int pos) {
		pieces[color][type].setBit(pos);
		allOfOneColor[color].setBit(pos);
	}

	@Override
	public void setUntouched(int rank, int file) {
		untouched.setBit(getPosForFileRank(file, rank));
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
		int color = 1;
		int otherColor = OTHER_COLOR[color];

		int[] positionIndices = new int[9];

		long[] allOfCol = new long[] { this.allOfOneColor[COLOR_BLACK].get(),this.allOfOneColor[COLOR_WHITE].get() };
		long occ = allOfCol[COLOR_WHITE] | allOfCol[COLOR_BLACK];
		long notOcc = ~occ;
		long notOwn = ~allOfCol[color];

		int moveCount = 0;

		for (int type = 0; type < 6; type++) {
/*
			switch (type) {
			case PIECE_TYPE_PAWN:
				System.out.println("PAWN");
				break;
			case PIECE_TYPE_BISHOP:
				System.out.println("BISHOP");
				break;
			case PIECE_TYPE_ROOK:
				System.out.println("ROOK");
				break;
			case PIECE_TYPE_QUEEN:
				System.out.println("QUEEN");
				break;
			case PIECE_TYPE_KING:
				System.out.println("KING");
				break;
			case PIECE_TYPE_KNIGHT:
				System.out.println("KNIGHT");
				break;
			}
*/		
			
			long pieces = this.pieces[color][type].get();
			long attacks = 0L;
			long pawnPushes = 0L;
			long pawnAttacksRight = 0L;
			long pawnAttacksLeft = 0L;

			int index = 0;
			long moves = 0L;
			if (type == PIECE_TYPE_PAWN && pieces != 0L) {
				//out(pieces);
				
				pawnPushes = color*(pieces << DIR_UP & notOcc) 
						+ otherColor*(pieces >> DIR_UP & notOcc);
				//out(pawnPushes);
				pawnPushes |= color*((pawnPushes & PAWN_SECOND_LINE[color]) << DIR_UP & notOcc)
						+ otherColor*((pawnPushes & PAWN_SECOND_LINE[color]) >> DIR_UP & notOcc);
				//out(pawnPushes);
				
				pawnAttacksRight = color & pieces << DIR_UP_RIGHT & MASK_NOT_A_FILE|(1-color) & pieces >> DIR_UP_LEFT & MASK_NOT_A_FILE;
				pawnAttacksLeft = color & pieces << DIR_UP_LEFT & MASK_NOT_A_FILE|(1-color) & pieces >> DIR_UP_RIGHT & MASK_NOT_A_FILE;
				
				moveCount += Long.bitCount(pawnPushes);
				moveCount += Long.bitCount(pawnAttacksRight & allOfCol[otherColor]);
				moveCount += Long.bitCount(pawnAttacksLeft & allOfCol[otherColor]);
				continue;
			}
			int pieceCount = updateIndices(positionIndices, pieces);

			for (int i = 0; i < pieceCount; i++) {
				int pos = positionIndices[i];
				switch (type) {
				case PIECE_TYPE_BISHOP:
					attacks = mnf.getBishopAttacks(pos, occ);
					break;
				case PIECE_TYPE_ROOK:
					attacks = mnf.getRookAttacks(pos, occ);		
					break;
				case PIECE_TYPE_QUEEN:
					attacks = mnf.getRookAttacks(pos, occ);
					attacks |= mnf.getBishopAttacks(pos, occ);
					break;
				case PIECE_TYPE_KING:
					index = (pos + (type * 2 + color) * 64);
					attacks = lookUp.getRawMoves(index);
				
					long ntchd = untouched.get();
					boolean test_k = false;
					boolean test_q = false;
					long castleMoves=0L;
					if(color==COLOR_BLACK) {
						if((MASK_CASTLE_ALL_k ^ (notOcc & MASK_CASTLE_OCC_k | ntchd))==0L) {
							castleMoves |= MASK_CASTLE_KING_k;
						}
						if((MASK_CASTLE_ALL_q ^ (notOcc & MASK_CASTLE_OCC_q | ntchd))==0L){
							castleMoves |= MASK_CASTLE_KING_q;
						}
						
					
					}else {
						if((MASK_CASTLE_ALL_K ^ (notOcc & MASK_CASTLE_OCC_K | ntchd))==0L){
							castleMoves |= MASK_CASTLE_KING_K;
						}
						if((MASK_CASTLE_ALL_Q ^ (notOcc & MASK_CASTLE_OCC_Q | ntchd))==0L){
							castleMoves |= MASK_CASTLE_KING_Q;
						}						
					}
					moveCount += Long.bitCount(castleMoves);
					break;
							
					
				case PIECE_TYPE_KNIGHT:
					index = (pos + (type * 2 + color) * 64);
					attacks = lookUp.getRawMoves(index);
					break;
				}
				moves = attacks & notOwn;
				//out(moves);
				moveCount += Long.bitCount(moves);
			}
		}
		System.out.println("Moves:" + moveCount);
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
