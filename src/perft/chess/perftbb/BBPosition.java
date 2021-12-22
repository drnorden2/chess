package perft.chess.perftbb;

import static perft.chess.Definitions.*;

import java.util.ArrayList;

import perft.chess.Position;
import perft.chess.core.baseliner.BLVariableInt;
import perft.chess.core.baseliner.BLVariableLong;
import perft.chess.core.baseliner.BaseLiner;
import perft.chess.perftbb.gen.MagicNumberFinder;


public class BBPosition implements Position {
	public final int depth = 10;
	public final BaseLiner bl = new BaseLiner(1, 200, 200, depth, 1000);
	MagicNumberFinder mnf = new MagicNumberFinder();

	public final BLVariableLong[] allOfOneColor = new BLVariableLong[2];
	public final BLVariableLong[] allMoves = new BLVariableLong[64];
	public final BLVariableInt[] fields = new BLVariableInt[64];

	public final BLVariableLong untouched;
	public final BLVariableInt enPassantePos;
	public BBAnalyzer analyzer = new BBAnalyzer(this);
	public LookUp lookUp = new LookUp();

	private int colorAtTurn = COLOR_WHITE;

	private int[] indices1 = new int[64];
	private int[] indices2 = new int[64];

	private ArrayList<Move> list = null;

	public BBPosition() {

		for (int i = 0; i < allMoves.length; i++) {
			allMoves[i] = new BLVariableLong(bl, 0L);
			fields[i] = new BLVariableInt(bl, 0);
		}

		enPassantePos = new BLVariableInt(bl, -1);
		untouched = new BLVariableLong(bl, 0L);
		allOfOneColor[COLOR_WHITE] = new BLVariableLong(bl, 0L);
		allOfOneColor[COLOR_BLACK] = new BLVariableLong(bl, 0L);
	}

	@Override
	public void initialAddToBoard(int color, int type, int pos) {
		fields[pos].set((type * 2 + color) * 64);
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
		bl.undo();
		System.out.println("UNDONE!");
		this.colorAtTurn= OTHER_COLOR[colorAtTurn];
		
	}
	private int moveCounter=0;

	@Override
	public void setMove(int index) {
		
		getMoves();
		bl.startNextLevel();
		Move move = this.list.get(index);
	
		System.out.println("MoveCount"+(++moveCounter)+": "+move+" - "+move.getNotation());
		System.out.println(this);
		
		int oldPos = move.getOldPos();
		int newPos = move.getNewPos();
		
		fields[newPos].set(fields[oldPos].getAndSet(-1));
		this.allMoves[oldPos].set(0L);
		this.updatePseudoMoves(newPos, newPos);
		this.allOfOneColor[colorAtTurn].moveBit(oldPos,newPos);
		System.out.println(this);
		this.colorAtTurn= OTHER_COLOR[colorAtTurn];
	}

	@Override
	public void setInitialTurn(int color) {
		colorAtTurn = color;
	}

	@Override
	public int getMoves() {
		list = new ArrayList<Move>();
		// TODO Auto-generated method stub
		int color = this.colorAtTurn;
		long all = this.allOfOneColor[color].get();
		int count1 = updateIndices(indices1, all);
		for (int i = 0; i < count1; i++) {
			int pos = indices1[i];
			long moveMask = this.allMoves[pos].get();
			int index = pos + fields[pos].get();
			Move[] moves = lookUp.getMoveMap(index);
			int count2 = updateIndices(indices2, moveMask);
			for (int j = 0; j < count2; j++) {
				list.add(moves[indices2[j]]);

			}
		}
		return list.size();
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
		long own = this.allOfOneColor[colorAtTurn].get();
		int moveCount = 0;
		int count1 = updateIndices(indices1, own);
		for (int i = 0; i < count1; i++) {
			int pos = indices1[i];
			int typeColor = fields[pos].get();
			int count2 = updatePseudoMoves(typeColor, pos);
			System.out.println(((typeColor / 64) - colorAtTurn) / 2 + "(" + pos + ")" + ":" + count2);
			moveCount += count2;
		}
		System.out.println("Moves:" + moveCount);
		System.out.println("Moves:" + getMoves());
	}

	private int updatePseudoMoves(int typeColor, int pos) {
		long attacks = 0L;
		long moves= 0L;
		long own = this.allOfOneColor[colorAtTurn].get();
		long notOwn = ~own;

		switch (typeColor) {
		case PIECE_TYPE_WHITE_PAWN:
			{
				long other = this.allOfOneColor[OTHER_COLOR[colorAtTurn]].get();
				long occ = own | other;
				long notOcc = ~occ;
				long posMask = 1L << pos;
				moves = posMask << DIR_UP & notOcc;
				moves |= (moves & MASK_3_RANK) << DIR_UP & notOcc;
				attacks = ((posMask << DIR_UP_RIGHT & MASK_NOT_H_FILE) | (posMask << DIR_UP_LEFT & MASK_NOT_A_FILE))
						& other;
			}
			break;
		case PIECE_TYPE_BLACK_PAWN:
			{
				long other = this.allOfOneColor[OTHER_COLOR[colorAtTurn]].get();
				long occ = own | other;
				long notOcc = ~occ;
				long posMask = 1L << pos;
				moves = posMask >> DIR_UP & notOcc;
				moves |= (moves & MASK_6_RANK) >> DIR_UP & notOcc;
				attacks = ((posMask >> DIR_UP_RIGHT & MASK_NOT_H_FILE) | (posMask >> DIR_UP_LEFT & MASK_NOT_A_FILE))& other;
			}
			break;
		case PIECE_TYPE_BLACK_BISHOP:
		case PIECE_TYPE_WHITE_BISHOP:
			{
				long other = this.allOfOneColor[OTHER_COLOR[colorAtTurn]].get();
				long occ = own | other;
				attacks = mnf.getBishopAttacks(pos, occ);
				break;
			}
		case PIECE_TYPE_BLACK_ROOK:
		case PIECE_TYPE_WHITE_ROOK:
			{
				long other = this.allOfOneColor[OTHER_COLOR[colorAtTurn]].get();
				long occ = own | other;
				attacks = mnf.getRookAttacks(pos, occ);
				break;
			}
		case PIECE_TYPE_BLACK_QUEEN:
		case PIECE_TYPE_WHITE_QUEEN:
			{
				long other = this.allOfOneColor[OTHER_COLOR[colorAtTurn]].get();
				long occ = own | other;
				attacks = mnf.getRookAttacks(pos, occ);
				attacks |= mnf.getBishopAttacks(pos, occ);
				break;
			}
		case PIECE_TYPE_BLACK_KING:
			{
				long other = this.allOfOneColor[OTHER_COLOR[colorAtTurn]].get();
				long occ = own | other;
				long notOcc = ~occ;
				
				int index = pos + typeColor;
				attacks = lookUp.getMoveMask(index);
				long ntchd = untouched.get();
				if ((MASK_CASTLE_ALL_k ^ (notOcc & MASK_CASTLE_OCC_k | ntchd)) == 0L) {
					moves = MASK_CASTLE_KING_k;
				}
				if ((MASK_CASTLE_ALL_q ^ (notOcc & MASK_CASTLE_OCC_q | ntchd)) == 0L) {
					moves |= MASK_CASTLE_KING_q;
				}
			}
			break;
		case PIECE_TYPE_WHITE_KING:
			{
				long other = this.allOfOneColor[OTHER_COLOR[colorAtTurn]].get();
				long occ = own | other;
				long notOcc = ~occ;
				
				int index = pos + typeColor;
				attacks = lookUp.getMoveMask(index);
				long ntchd = untouched.get();
				if ((MASK_CASTLE_ALL_K ^ (notOcc & MASK_CASTLE_OCC_K | ntchd)) == 0L) {
					moves  = MASK_CASTLE_KING_K;
				}
				if ((MASK_CASTLE_ALL_Q ^ (notOcc & MASK_CASTLE_OCC_Q | ntchd)) == 0L) {
					moves  |= MASK_CASTLE_KING_Q;
				}
			}
			break;
		case PIECE_TYPE_WHITE_KNIGHT:
		case PIECE_TYPE_BLACK_KNIGHT:
			int index = pos + typeColor;
			attacks = lookUp.getMoveMask(index);
			break;
		}
		moves |= attacks & notOwn;
		allMoves[pos].set(moves);
		return Long.bitCount(moves);
	}

	@Override
	public int getColorAtTurn() {
		return this.colorAtTurn;
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
