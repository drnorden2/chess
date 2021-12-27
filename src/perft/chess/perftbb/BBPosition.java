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

	public final BLVariableInt[] fields = new BLVariableInt[64];
	public final BLVariableLong[] allOfOneColor = new BLVariableLong[2];
	public final BLVariableLong[] allMoves = new BLVariableLong[64];
	public final BLVariableLong[] rawCBs = new BLVariableLong[64];
	public final BLVariableLong[] tCallBacks = new BLVariableLong[64];
	public final BLVariableLong[] correctors = new BLVariableLong[2];

	public final BLVariableLong untouched;
	public final BLVariableInt enPassantePos;

	public BBAnalyzer analyzer = new BBAnalyzer(this);
	public LookUp lookUp = new LookUp();

	private int colorAtTurn = COLOR_WHITE;

	private int[] indices1 = new int[64];
	private int[] indices2 = new int[64];
	private int[] indices3 = new int[64];
	private int[] indices4 = new int[64];

	private ArrayList<Move> list = null;

	public BBPosition() {

		for (int i = 0; i < allMoves.length; i++) {
			allMoves[i] = new BLVariableLong(bl, 0L);
			tCallBacks[i] = new BLVariableLong(bl, 0L);
			rawCBs[i] = new BLVariableLong(bl, 0L);
			fields[i] = new BLVariableInt(bl, -1);

		}

		enPassantePos = new BLVariableInt(bl, -1);
		untouched = new BLVariableLong(bl, 0L);
		allOfOneColor[COLOR_WHITE] = new BLVariableLong(bl, 0L);
		allOfOneColor[COLOR_BLACK] = new BLVariableLong(bl, 0L);
		correctors[COLOR_WHITE] = new BLVariableLong(bl, 0L);
		correctors[COLOR_BLACK] = new BLVariableLong(bl, 0L);

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
		this.colorAtTurn = OTHER_COLOR[colorAtTurn];
		// System.out.println("UNDONE!");

	}

	private int moveCounter = 0;

	public Move getMoveObj(int index) {
		getMoves();
		return this.list.get(index);
	}

	@Override
	public void setMove(int index) {
		/** MODIFY **/

		getMoves();
		bl.startNextLevel();
		Move move = this.list.get(index);
//		System.out.println(move.getNotation()+this);
		/*
		 * if(move ==null) { System.out.println("WTF Move is null"+index); getMoves();
		 * move = this.list.get(index); }
		 */
		// System.out.println("MoveCount"+(++moveCounter)+": "+move+" -
		// "+move.getNotation());
		// System.out.println(this);

		int oldPos = move.getOldPos();
		int newPos = move.getNewPos();
		int color = this.colorAtTurn;
		int otherColor = OTHER_COLOR[color];

		// update fields
		int typeColor = fields[oldPos].getAndSet(-1);
		fields[newPos].set(typeColor);

		// update occupancy
		this.allOfOneColor[otherColor].unsetBit(newPos);// might be empty anyways
		this.allOfOneColor[color].moveBit(oldPos, newPos);
		// update moves
		this.allMoves[oldPos].set(0L);
		this.updatePseudoMoves(typeColor, newPos);

		// get callbacks
		long cbs = this.tCallBacks[newPos].get() | this.tCallBacks[oldPos].get();
		cbs &= this.allOfOneColor[color].get() | this.allOfOneColor[otherColor].get();

		int count = updateIndices(indices4, cbs);

		for (int i = 0; i < count; i++) {
			int cbPos = indices4[i];
			if (cbPos == newPos) {
				continue;// @todo Wtf
			}

			int cbTypeColor = fields[cbPos].get();
			this.updatePseudoMoves(cbTypeColor, cbPos);
		}

//		System.out.println("TypeColor"+typeColor+this);
		this.colorAtTurn = OTHER_COLOR[colorAtTurn];
	}

	@Override
	public void setInitialTurn(int color) {
		colorAtTurn = color;
	}

	@Override
	public int getMoves() {
		/** READ ONLY **/
		list = new ArrayList<Move>();
		int color = this.colorAtTurn;
		long own = this.allOfOneColor[color].get();
		// long notOwn = ~own;
		int count1 = updateIndices(indices1, own);
		for (int i = 0; i < count1; i++) {
			int pos = indices1[i];
			long moveMask = this.allMoves[pos].get();// * & notOwn;
			int index = pos + fields[pos].get();
			Move[] moves = lookUp.getMoveMap(index);
			int count2 = updateIndices(indices2, moveMask);
			// System.out.println(i+":"+count2);
			// out(moveMask);
			for (int j = 0; j < count2; j++) {
				Move move = moves[indices2[j]];// TBD
				if(move.isPromotion()) {
					int curPos = indices2[j];
					int rank = getRankForPos(curPos);
					int file = getFileForPos(curPos);
					while(moves[curPos]!=null) {
						list.add(moves[curPos]);
						rank=(rank-PAWN_MOVE_DIR[move.getColor()]);
						curPos = getPosForFileRank(file,rank);
					}
				}else {
					list.add(move);
				}
				
				if (move == null) {
					System.out.println("Color:" + color);
					System.out.println("Index:" + index);
					System.out.println("Type:" + (index / 64 - 1) / 2);

					System.out.println("selected Moves");
					out(moveMask);
					System.out.println("raw Moves");
					out(this.allMoves[pos].get());
					System.out.println("Available Moves");
					String out = "";
					for (int k = 0; k < moves.length; k++) {
						if (moves[k] == null) {
							out += ".";
						} else {
							out += 'm';
						}
						if ((k + 1) % 8 == 0) {
							out += "\n";
						}
					}
					System.out.println(out);
					throw new RuntimeException("WTF");

				}
			}
		}
		/*
		 * if(list.size()>30) { System.out.println("moves:"+list.size());
		 * System.out.println(this); out(all); getMoves(); System.exit(1); }
		 */
		return list.size();
	}

	@Override
	public int getHash() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public String getNotation(int index) {
		getMoves();
		return this.list.get(index).getNotation();
	}

	@Override
	public void initialEval() {
		for (int i = 0; i < 2; i++) {
			long own = this.allOfOneColor[i].get();
			// int moveCount = 0;
			int count1 = updateIndices(indices1, own);
			for (int j = 0; j < count1; j++) {
				int pos = indices1[j];
				int typeColor = fields[pos].get();
				int count2 = updatePseudoMoves(typeColor, pos);
				// System.out.println(((typeColor / 64) - i) / 2 + "(" + pos + ")" + ":" +
				// count2);
				// moveCount += count2;
			}
			// System.out.println("Moves:" + moveCount);
			// System.out.println("Moves:" + getMoves());

		}
	}

	/*
	 * 
	 * PSEUDO_MOVES we need: - callBacks[64] - rawMoves[64] - correctors[2]
	 * 
	 * 
	 * pawn ==> callBacks = getCallBacks() <= CALLBACKS_PAWN; ==> push() corrector
	 * for push (1x and 2x -regardlessly) ==> realMoves = getrawMoves ():
	 * getCallBacks() && pawnAttackMoveLookup(other) |getCallBacks() &&
	 * pawnPushMovelookup (occ) ==> realAttacks = getAttacks() <=
	 * getCallBacks()-corrector;
	 * 
	 * king ==> callBacks = getCallBacks() <= lookup king ATTACKS() + rochade_push()
	 * ==> corrector ==> realMoves = getrawMoves ():getCallBacks() & (notown) &
	 * not_rochade_possible) ==> realAttacks =
	 * getAttacks():getCallBacks()-corrector;
	 * 
	 * Knight ==> callBacks = getCallBacks() <= KNIGHT_ATTACK ==> realMoves =
	 * getrawMoves ():getCallBacks() & notown ==> realAttacks =
	 * getAttacks():getCallBacks()-corrector;
	 * 
	 * other ==> callBacks = getCallBacks() <= lookup ray-attacks(); ==> realMoves =
	 * getrawMoves ():getCallBacks() & notown ==> realAttacks =
	 * getAttacks():getCallBacks()-corrector;
	 * 
	 */

	private int updatePseudoMoves(int typeColor, int pos) {

		long moves = 0L;
		long callbacks = 0L;
		//typecolor ((type * 2 + color) * 64);
		int color = typeColor >> 6 & 1;
		//int type = (typeColor >> 6 - color)>>1;
			
		long correction =this.correctors[color].get();

		int otherColor = OTHER_COLOR[color];
		long own = this.allOfOneColor[color].get();// @todo WTF too expensive

		switch (typeColor) {
		case PIECE_TYPE_WHITE_PAWN: {
			
			long other = this.allOfOneColor[otherColor].get();
			long notOcc = ~(own | other);
			int file = getFileForPos(pos);
			callbacks = lookUp.getMoveMask(pos + typeColor);
			correction  = correction &~(MASK_X_FILE[file] & MASK_NOT_1_RANK);
			correction |= callbacks & MASK_X_FILE[file];
			moves = ((1L << pos) << DIR_UP) & notOcc;
			moves |= ((moves & MASK_3_RANK) << DIR_UP) & notOcc;
			moves |= callbacks & MASK_NOT_X_FILE[file] & other;
			break;
		}

		case PIECE_TYPE_BLACK_PAWN: {
			long other = this.allOfOneColor[otherColor].get();
			long notOcc = ~(own | other);
			int file = getFileForPos(pos);
			callbacks = lookUp.getMoveMask(pos + typeColor);
			correction  = correction &~(MASK_X_FILE[file] & MASK_NOT_8_RANK);
			correction |= callbacks & MASK_X_FILE[file];
			moves = ((1L << pos) >> DIR_UP) & notOcc;
			moves |= ((moves & MASK_6_RANK) >> DIR_UP) & notOcc;
			moves |= callbacks & MASK_NOT_X_FILE[file] & other;
			break;
		}
		case PIECE_TYPE_BLACK_BISHOP:
		case PIECE_TYPE_WHITE_BISHOP: {
			long other = this.allOfOneColor[otherColor].get();
			long occ = own | other;
			long notOwn = ~own;
			callbacks = mnf.getBishopAttacks(pos, occ);
			moves = callbacks & notOwn;
			break;
		}
		case PIECE_TYPE_BLACK_ROOK:
		case PIECE_TYPE_WHITE_ROOK: {
			long other = this.allOfOneColor[otherColor].get();
			long occ = own | other;
			long notOwn = ~own;
			callbacks = mnf.getRookAttacks(pos, occ);
			moves = callbacks & notOwn;
			break;
		}
		case PIECE_TYPE_BLACK_QUEEN:
		case PIECE_TYPE_WHITE_QUEEN: {
			long other = this.allOfOneColor[otherColor].get();
			long occ = own | other;
			long notOwn = ~own;
			callbacks = mnf.getBishopAttacks(pos, occ);
			callbacks |= mnf.getRookAttacks(pos, occ);
			moves = callbacks & notOwn;
			break;
		}
		case PIECE_TYPE_WHITE_KNIGHT:
		case PIECE_TYPE_BLACK_KNIGHT: {
			long notOwn = ~own;
			callbacks = lookUp.getMoveMask(pos + typeColor);
			moves = callbacks & notOwn;
			break;
		}
		case PIECE_TYPE_BLACK_KING: {
			long other = this.allOfOneColor[otherColor].get();
			long notOwn = ~own;
			long occ = own | other;
			//long notOcc = ~occ;

			callbacks = lookUp.getMoveMask(pos + typeColor);
			moves = callbacks & notOwn; // callbacks are still just the 8 direct fields
			
			long ntchd = untouched.get();
			correction&= ~MASK_8_RANK; //delete old corrections
			if ((ntchd & MASK_E8) == MASK_E8) {
				if ((ntchd & MASK_H8) == MASK_H8) {
					if ((occ & MASK_CASTLE_OCC_k) == 0L) {
						moves |= MASK_CASTLE_KING_k;
					}
					correction |= MASK_CASTLE_OCC_k&~callbacks;
					callbacks |=MASK_CASTLE_OCC_k;
				}
				if ((ntchd & MASK_A8) ==MASK_A8) {
					if ((occ & MASK_CASTLE_OCC_q) == 0L) {
						moves |= MASK_CASTLE_KING_q;
					}
					correction |= MASK_CASTLE_OCC_q&~callbacks;
					callbacks |=MASK_CASTLE_OCC_q;
				}
			}
			break;
		}
		case PIECE_TYPE_WHITE_KING: {
			long other = this.allOfOneColor[otherColor].get();
			long notOwn = ~own;
			long occ = own | other;
			
			callbacks = lookUp.getMoveMask(pos + typeColor);
			moves = callbacks & notOwn; // callbacks are still just the 8 direct fields

			long ntchd = untouched.get();
			correction&= ~MASK_1_RANK; //delete old corrections
			if ((ntchd & MASK_E1) == MASK_E1) {
				if ((ntchd & MASK_H1) ==MASK_H1 ) {
					if ((occ & MASK_CASTLE_OCC_K) == 0L) {
						moves |= MASK_CASTLE_KING_K;
					}
					correction |= MASK_CASTLE_OCC_K&~callbacks;
					callbacks |= MASK_CASTLE_OCC_K;
				}
				if ((ntchd & MASK_A1) == MASK_A1) {
					if ((occ & MASK_CASTLE_OCC_Q) == 0L) {
						moves |= MASK_CASTLE_KING_Q;
					}
					correction |= MASK_CASTLE_OCC_Q&~callbacks;//@@tbd wtf
					callbacks |= MASK_CASTLE_OCC_Q;
				}
			}
			break;
		}
		}
		long oldCBs = this.rawCBs[pos].get();
		this.rawCBs[pos].set(callbacks);
		long deltaCallbacks = oldCBs ^ callbacks;
		
		int count = updateIndices(indices3, deltaCallbacks);
		for (int i = 0; i < count; i++) {
			this.tCallBacks[indices3[i]].toggleBit(pos);
		}
		allMoves[pos].set(moves);
		this.correctors[color].set(correction);
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
