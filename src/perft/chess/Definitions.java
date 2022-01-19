package perft.chess;


import perft.chess.core.datastruct.BitBoard;

public class Definitions {
	public static final long EMPTY_MASK =0L;
	public static final long FULL_MASK =~EMPTY_MASK;

	public static final int PIECE_TYPE_PAWN = 0;
	public static final int PIECE_TYPE_KING= 1;
	public static final int PIECE_TYPE_KNIGHT = 2;
	public static final int PIECE_TYPE_BISHOP = 3;
	public static final int PIECE_TYPE_ROOK = 4;
	public static final int PIECE_TYPE_QUEEN = 5;
	public static final int PIECE_TYPE_ANY = 6;
	public static final int PIECE_TYPE_PAWN_ENPASSANTE_OFFER= 7;
	
	public static final int COLOR_BLACK= 0;
	public static final int COLOR_WHITE= 1;

	public final static int NOTIFY_NOW_EMPTY =0;
	public final static int NOTIFY_NOW_OCCUPIED =1;
	public final static int NOTIFY_NOW_REPLACED =2;
	public final static int NOTIFY_NOW_OCCUPIED_ENPASSANTE_FIELD =3;
	public final static int NOTIFY_NOW_EMPTY_ENPASSANTE_FIELD =4;
	
	
	public final static int MOVE_IS_POSSIBLE= 0;
	public final static int MOVE_NOT_POSSIBLE=1;
	public final static int MOVE_IS_PINNED=2;
	public final static int MOVE_IS_UNCLEAR=3;

	
	public static final int MOVE_TYPE_PUSH_BEAT = 0;
	public static final int MOVE_TYPE_PAWN_BEAT= 1;
	public static final int MOVE_TYPE_PAWN_PUSH= 2;
	public static final int MOVE_TYPE_ROCHADE= 3;
	public static final int MOVE_TYPE_PAWN_BEAT_OR_ENPASSANTE= 4;
	public static final int MOVE_TYPE_PAWN_PUSH_CONVERT= 5;
	public static final int MOVE_TYPE_PAWN_BEAT_CONVERT= 6;
	public static final int MOVE_TYPE_KING_SENSING = 7;
	public static final int MOVE_TYPE_INITAL_PLACEMENT = 8;
	
	
	public static final int _A = 0;
	public static final int _B = 1;
	public static final int _C = 2;
	public static final int _D = 3;
	public static final int _E = 4;
	public static final int _F = 5;
	public static final int _G = 6;
	public static final int _H = 7;

	public static final int _8 = 7;
	public static final int _7 = 6;
	public static final int _6 = 5;
	public static final int _5 = 4;
	public static final int _4 = 3;
	public static final int _3 = 2;
	public static final int _2 = 1;
	public static final int _1 = 0;

	public static final int ROCHADE_W_ROOK_LONG = 2;
	public static final int ROCHADE_W_ROOK_SHORT = 6;
	public static final int ROCHADE_B_ROOK_LONG = 58;
	public static final int ROCHADE_B_ROOK_SHORT = 62;
			
	public static final int GAME_STATE_NORMAL=0;
	public static final int GAME_STATE_CHECK=1;
	
	public final static int CALLBACK_TYPE_PUSH_RAY =0;
	public final static int CALLBACK_TYPE_PUSH_ONE =1;
	public final static int CALLBACK_TYPE_KING_SENSING=2;
	public final static int CALLBACK_TYPE_ROCHADE_TEST = 3;
	public final static int CALLBACK_TYPE_CHECK_KNIGHT_ATTACK = 4;

	public final static int CALLBACK_TYPE_BEAT_ONE =5;
	public final static int CALLBACK_TYPE_BEAT_ONE_AS_PAWN=6;
	public final static int CALLBACK_TYPE_OTHER = 7;
	public final static int CALLBACK_TYPE_BEAT_RAY =8;	
	public final static int CALLBACK_TYPE_BEAT_ONE_AS_KING = 9;
	
	
	public final static long MASK_A_FILE = fileMask(_A);
	public final static long MASK_B_FILE = fileMask(_B);
	public final static long MASK_C_FILE = fileMask(_C);
	public final static long MASK_D_FILE = fileMask(_D);
	public final static long MASK_E_FILE = fileMask(_E);
	public final static long MASK_F_FILE = fileMask(_F);
	public final static long MASK_G_FILE = fileMask(_G);
	public final static long MASK_H_FILE = fileMask(_H);
	public final static long MASK_NONE = EMPTY_MASK;
	public final static long MASK_ALL = ~MASK_NONE;
	
	public static final int _A1=0;
	public static final int _B1=1;
	public static final int _C1=2;
	public static final int _D1=3;
	public static final int _E1=4;
	public static final int _F1=5;
	public static final int _G1=6;
	public static final int _H1=7;

	
	public static final long MASK_A1=1L<<_A1;
	public static final long MASK_B1=1L<<_B1;
	public static final long MASK_C1=1L<<_C1;
	public static final long MASK_D1=1L<<_D1;
	public static final long MASK_E1=1L<<_E1;
	public static final long MASK_F1=1L<<_F1;
	public static final long MASK_G1=1L<<_G1;
	public static final long MASK_H1=1L<<_H1;

	public static final int _A8=56;
	public static final int _B8=57;
	public static final int _C8=58;
	public static final int _D8=59;
	public static final int _E8=60;
	public static final int _F8=61;
	public static final int _G8=62;
	public static final int _H8=63;

	
	public static final long MASK_A8=1L<<_A8;
	public static final long MASK_B8=1L<<_B8;
	public static final long MASK_C8=1L<<_C8;
	public static final long MASK_D8=1L<<_D8;
	public static final long MASK_E8=1L<<_E8;
	public static final long MASK_F8=1L<<_F8;
	public static final long MASK_G8=1L<<_G8;
	public static final long MASK_H8=1L<<_H8;	
	
	public static final long MASK_E8_A8=MASK_E8|MASK_A8;
	public static final long MASK_E8_H8=MASK_E8|MASK_H8;
	public static final long MASK_E1_A1=MASK_E1|MASK_A1;
	public static final long MASK_E1_H1=MASK_E1|MASK_H1;
	
	public final static long MASK_NOT_A_FILE = not(MASK_A_FILE);
	public final static long MASK_NOT_B_FILE = not(MASK_B_FILE);
	public final static long MASK_NOT_C_FILE = not(MASK_C_FILE);
	public final static long MASK_NOT_D_FILE = not(MASK_D_FILE);
	public final static long MASK_NOT_E_FILE = not(MASK_E_FILE);
	public final static long MASK_NOT_F_FILE = not(MASK_F_FILE);
	public final static long MASK_NOT_G_FILE = not(MASK_G_FILE);
	public final static long MASK_NOT_H_FILE = not(MASK_H_FILE);
	
	
	public final static long MASK_1_RANK = rankMask(_1);
	public final static long MASK_2_RANK = rankMask(_2);
	public final static long MASK_3_RANK = rankMask(_3);
	public final static long MASK_4_RANK = rankMask(_4);
	public final static long MASK_5_RANK = rankMask(_5);
	public final static long MASK_6_RANK = rankMask(_6);
	public final static long MASK_7_RANK = rankMask(_7);
	public final static long MASK_8_RANK = rankMask(_8);
	public final static long MASK_OUTER_RANKS = MASK_8_RANK |MASK_1_RANK;
	public final static long MASK_NOT_ALL_ROOKS = ~(MASK_A1|MASK_A8|MASK_H1|MASK_H8);
	public final static long MASK_ALL_ROOKS = (MASK_A1|MASK_A8|MASK_H1|MASK_H8);
	public final static long MASK_ALL_ROOKS_KINGS = ~(MASK_A1|MASK_A8|MASK_H1|MASK_H8|MASK_E1|MASK_E8);
	public final static long MASK_NOT_ALL_ROOKS_KINGS = ~(MASK_A1|MASK_A8|MASK_H1|MASK_H8|MASK_E1|MASK_E8);
	public final static long MASK_ALL_KINGS = MASK_E1|MASK_E8;
	
	
	
	
	public final static long[] MASK_KING_X= new long[]{
			MASK_E1,
			MASK_E8};
	
	public final static long[] MASK_X_RANK = new long[]{
			MASK_1_RANK,
			MASK_2_RANK,
			MASK_3_RANK,
			MASK_4_RANK,
			MASK_5_RANK,
			MASK_6_RANK,
			MASK_7_RANK,
			MASK_8_RANK
	};
	public final static long[] MASK_X_FILE = new long[]{
			MASK_A_FILE,
			MASK_B_FILE,
			MASK_C_FILE,
			MASK_D_FILE,
			MASK_E_FILE,
			MASK_F_FILE,
			MASK_G_FILE,
			MASK_H_FILE
	};
	public final static long[] MASK_NOT_X_FILE = new long[]{
			MASK_NOT_A_FILE,
			MASK_NOT_B_FILE,
			MASK_NOT_C_FILE,
			MASK_NOT_D_FILE,
			MASK_NOT_E_FILE,
			MASK_NOT_F_FILE,
			MASK_NOT_G_FILE,
			MASK_NOT_H_FILE
	};
	public final static long[] MASK_NOT_X_FILE_FOR_POS = getMaskNotXFilesForPos();
	public final static long[] MASK_X_FILE_FOR_POS = getMaskXFilesForPos();
	
	
	
	public final static long MASK_NOT_1_RANK = not(MASK_1_RANK);
	public final static long MASK_NOT_2_RANK = not(MASK_2_RANK);
	public final static long MASK_NOT_3_RANK = not(MASK_3_RANK);
	public final static long MASK_NOT_4_RANK = not(MASK_4_RANK);
	public final static long MASK_NOT_5_RANK = not(MASK_5_RANK);
	public final static long MASK_NOT_6_RANK = not(MASK_6_RANK);
	public final static long MASK_NOT_7_RANK = not(MASK_7_RANK);
	public final static long MASK_NOT_8_RANK = not(MASK_8_RANK);

	public final static long[] MASK_NOT_BASE_RANK = new long[] {MASK_8_RANK,MASK_1_RANK};
	
	
	public final static long MASK_CASTLE_OCC_Q = MASK_B1|MASK_C1|MASK_D1;
	public final static long MASK_CASTLE_OCC_K = MASK_F1|MASK_G1;

	//public final static long MASK_CASTLE_ALL_Q = MASK_A1|MASK_B1|MASK_C1|/*MASK_D1|*/MASK_E1;
	//public final static long MASK_CASTLE_ALL_K = MASK_E1|/*|MASK_F1|*/MASK_G1|MASK_H1;
	//public final static long MASK_CASTLE_ALL_q = MASK_A8|MASK_B8|MASK_C8|/*MASK_D8|*/MASK_E8;
	//public final static long MASK_CASTLE_ALL_k = MASK_E8|/*|MASK_F8|*/MASK_G8|MASK_H8;
	
	public final static long MASK_CASTLE_ALL_Q = MASK_A1|MASK_B1|MASK_C1|MASK_D1|MASK_E1;
	public final static long MASK_CASTLE_ALL_K = MASK_E1|MASK_F1|MASK_G1|MASK_H1;
	public final static long MASK_CASTLE_ALL_q = MASK_A8|MASK_B8|MASK_C8|MASK_D8|MASK_E8;
	public final static long MASK_CASTLE_ALL_k = MASK_E8|MASK_F8|MASK_G8|MASK_H8;
		
	public final static long MASK_CASTLE_OCC_q = MASK_B8|MASK_C8|MASK_D8;
	public final static long MASK_CASTLE_OCC_k = MASK_F8|MASK_G8;
	
	public final static long MASK_CASTLE_KING_Q = MASK_C1;
	public final static long MASK_CASTLE_KING_K = MASK_G1;
	public final static long MASK_CASTLE_KING_q = MASK_C8;
	public final static long MASK_CASTLE_KING_k = MASK_G8;
	
	public final static long MASK_CASTLE_KING_KQkq = MASK_C1|MASK_G1|MASK_C8|MASK_G8;
	public final static long MASK_NOT_CASTLE_KING_KQkq = ~MASK_CASTLE_KING_KQkq ;
	
	

	public final static int DIR_UP_RIGHT = 9;
	public final static int DIR_UP_LEFT = 7;
	public final static int DIR_RIGHT = 1;
	public final static int DIR_UP = 8;
	public final static int DIR_2_UP = 16;

	public static final long[] PAWN_SECOND_LINE = new long[] {MASK_6_RANK,MASK_3_RANK};
	public static final long[] PAWN_LAST_LINE = new long[] {MASK_1_RANK,MASK_8_RANK};
	public static final int[] OTHER_COLOR = new int[] {COLOR_WHITE,COLOR_BLACK};
	public static final int[] PAWN_MOVE_DIR = new int[] {-1,1};
	public static final long[] MASK_ONE_UP_AND_DOWN = getAllOneUPandDown() ;
	
			
/*Static*/
	
	public static int getRankForPos(int pos) {
		return (int)(pos/8);
	}
	public static int getFileForPos(int pos) {
		return (int)(pos%8);
	}
	
	public static int getPosForFileRank(int file , int rank) {
 		int index = 8 * rank + file;
		return index;
	}

	
	
	
	private static long fileMask(int file) {
		BitBoard bb= new BitBoard(EMPTY_MASK);
		for(int i=0;i<8;i++) {
			bb.set(getPosForFileRank(file,i));
		}
		return bb.getBits();
	}
	
	private static long rankMask(int rank) {
		BitBoard bb= new BitBoard(EMPTY_MASK);
		for(int i=0;i<8;i++) {
			bb.set(getPosForFileRank(i,rank));
		}
		return bb.getBits();
	}
		
	private static long not(long mask) {
		BitBoard bb= new BitBoard(mask);
		bb.invert();
		return bb.getBits();
	}
	
	public static void out(long bits) {
		System.out.println(BitBoard.toString(bits));
	}
	public static String toStr(long bits) {
		return(BitBoard.toString(bits));
	}
	
	/*
	public static int updateIndices(int[] indices, long bits) {
		int counter =0;
		while (bits != 0){
			int idx = 63-Long.numberOfLeadingZeros(bits); 
			indices[counter++] = idx;
			bits &= ~(1L << idx);
		}
		return counter;
	}*/
	
	public static int updateIndices(int[] indices, long bits) {
		int retVal = Long.bitCount(bits);
		for(int i=0;i<retVal;i++) {
			indices[i] = Long.numberOfTrailingZeros(bits);
			bits &= bits - 1;			
		}
		return retVal ;
	}
	private static long[] getMaskNotXFilesForPos() {
		long[] retVal = new long[64];
		int counter=0;
		for(int i=0;i<8;i++) {
			for(int j=0;j<8;j++) {
				retVal[counter++]=MASK_NOT_X_FILE[j];
			}
		}
		return retVal ;
	}
	private static long[] getMaskXFilesForPos() {
		long[] retVal = new long[64];
		int counter=0;
		for(int i=0;i<8;i++) {
			for(int j=0;j<8;j++) {
				retVal[counter++]=MASK_X_FILE[j];
			}
		}
		return retVal ;
	}
	
	
	public static final int PIECE_TYPE_WHITE_PAWN = (PIECE_TYPE_PAWN  * 2 + COLOR_WHITE) * 64;
	public static final int PIECE_TYPE_WHITE_KNIGHT = (PIECE_TYPE_KNIGHT* 2 + COLOR_WHITE) * 64;
	public static final int PIECE_TYPE_WHITE_BISHOP = (PIECE_TYPE_BISHOP* 2 + COLOR_WHITE) * 64;
	public static final int PIECE_TYPE_WHITE_ROOK = (PIECE_TYPE_ROOK* 2 + COLOR_WHITE) * 64;
	public static final int PIECE_TYPE_WHITE_QUEEN = (PIECE_TYPE_QUEEN* 2 + COLOR_WHITE) * 64;
	public static final int PIECE_TYPE_WHITE_KING= (PIECE_TYPE_KING* 2 + COLOR_WHITE) * 64;

	public static final int PIECE_TYPE_BLACK_PAWN = (PIECE_TYPE_PAWN  * 2 + COLOR_BLACK) * 64;
	public static final int PIECE_TYPE_BLACK_KNIGHT = (PIECE_TYPE_KNIGHT* 2 + COLOR_BLACK) * 64;
	public static final int PIECE_TYPE_BLACK_BISHOP = (PIECE_TYPE_BISHOP* 2 + COLOR_BLACK) * 64;
	public static final int PIECE_TYPE_BLACK_ROOK = (PIECE_TYPE_ROOK* 2 + COLOR_BLACK) * 64;
	public static final int PIECE_TYPE_BLACK_QUEEN = (PIECE_TYPE_QUEEN* 2 + COLOR_BLACK) * 64;
	public static final int PIECE_TYPE_BLACK_KING= (PIECE_TYPE_KING* 2 + COLOR_BLACK) * 64;

	public static final int[] PIECE_TYPE_X_PAWN = new int[] {PIECE_TYPE_BLACK_PAWN,PIECE_TYPE_WHITE_PAWN};
	public static final int[] PIECE_TYPE_X_KNIGHT = new int[] {PIECE_TYPE_BLACK_KNIGHT,PIECE_TYPE_WHITE_KNIGHT};
	public static final int[] PIECE_TYPE_X_BISHOP = new int[] {PIECE_TYPE_BLACK_BISHOP,PIECE_TYPE_WHITE_BISHOP};
	public static final int[] PIECE_TYPE_X_ROOK = new int[] {PIECE_TYPE_BLACK_ROOK,PIECE_TYPE_WHITE_ROOK};
	public static final int[] PIECE_TYPE_X_QUEEN = new int[] {PIECE_TYPE_BLACK_QUEEN,PIECE_TYPE_WHITE_QUEEN};
	public static final int[] PIECE_TYPE_X_KING = new int[] {PIECE_TYPE_BLACK_KING,PIECE_TYPE_WHITE_KING};
	
	public static final long[] SHIFT = getShifts();
	public static final long[] NOT_SHIFT = not(SHIFT);
	
			
	
	public static final long[] KING_MASKS = getKingMasks();
	public static final long[] QUEEN_MASKS = getQueenMasks();
	public static final long[] KNIGHT_MASKS = getKnightMasks();
	public static final long[][] LINE_FOR_2_POINTS=getLineMasksFor2Points();
	public static final long[][] NOT_LINE_FOR_2_POINTS=not(LINE_FOR_2_POINTS);
	public static final long[][] CHECK_MASKS=getCheckMask();
	public static final long[] ENP_PAWN_MASK_FOR_X = getEnpassantePawnPos();
	
	public static final String CHESS_MAN_CHARS = "pknbrqPKNBRQ";
	
	public static char getPieceCharForTypeColor(int typeColor) {
		//PIECE * 2 + COLOR) * 64
		int color = typeColor >> 6 & 1;
		int type = typeColor >> 7;
		return CHESS_MAN_CHARS.charAt(type+6*color);
	}
	public static char getPieceCharForPieceType(int type) {
		return CHESS_MAN_CHARS.charAt(type);
	}
	
	private static long[][] not(long[][] input){
		long[][]output = new long[64][64];
		for (int i=0;i<64;i++) {
			for(int j=0;j<64;j++) {
				output[i][j]=~input[i][j];
			}
		}
		return output;
	}
	private static long[] not(long[] input){
		long[]output = new long[64];
		for (int i=0;i<64;i++) {
			output[i]=~input[i];
		}
		return output;
	}
	
	private static long[] getAllOneUPandDown() {
		long[] all = new long[64];
		for(int i=0;i<8;i++) {
			for (int j=0;j<8;j++) {
				int pos = getPosForFileRank(i, j);
				if(j!=0) {
					all[pos]|=(1L<<getPosForFileRank(i, j-1));
				}
				if(j!=8) {
					all[pos]|=(1L<<getPosForFileRank(i, j+1));
				}
			}
		}
		return all;
	}
	private static long[] getShifts() {
		long[] shifts = new long[64];
		for(int i=0;i<64;i++) {
			shifts[i]=1L<<i;
		}
		return shifts;
	}
	
	private static long[] getQueenMasks() {
		long[] all = new long[64];
		for(int i=0;i<64;i++) {
			long result = EMPTY_MASK;
			int rk = i / 8, fl = i % 8, r, f;
			for (r = rk + 1; r <= 7; r++)
				result |= SHIFT[fl + r * 8];
			for (r = rk - 1; r >= 0; r--)
				result |= SHIFT[fl + r * 8];
			for (f = fl + 1; f <= 7; f++)
				result |= SHIFT[f + rk * 8];
			for (f = fl - 1; f >= 0; f--)
				result |= SHIFT[f + rk * 8];
			
			for (r = rk + 1, f = fl + 1; r <= 7 && f <= 7; r++, f++)
				result |= SHIFT[f + r * 8];
			for (r = rk + 1, f = fl - 1; r <= 7 && f >= 0; r++, f--)
				result |= SHIFT[f + r * 8];
			for (r = rk - 1, f = fl + 1; r >= 0 && f <= 7; r--, f++)
				result |= SHIFT[f + r * 8];
			for (r = rk - 1, f = fl - 1; r >= 0 && f >= 0; r--, f--)
				result |= SHIFT[f + r * 8];
			all[i]=result;
		}			
		
		return all;
		
	}	
	
	
	
	
	private static long[][] getLineMasksFor2Points(){
		long[][] masks = new long[64][64];
		for(int i=0;i<64;i++) {
			for(int j=0;j<64;j++) {
				if(i==j)continue;
				long mask =0;
				int ri = i / 8, fi = i % 8;
				int rj = j / 8, fj = j % 8;
				int rD = ri-rj;
				int fD = fi-fj,f,r;
				if((rD!=0 && fD!=0) && (rD*rD!=fD*fD))continue;
				rD=(int)Math.signum(rD);
				fD=(int)Math.signum(fD);
				for (r = ri ,  f =fi;r >=0 && r <= 7 && f >=0 && f <= 7 ; r-=rD, f-=fD) {
					mask|= SHIFT[f + r * 8];
				} 
				masks[i][j]=mask;//&~SHIFT[i]&~SHIFT[j];
			}
		
		}
		return masks;
	}

	private static long[] getKnightMasks() {
		long[] all = new long[64];
		int[][] moves =new int[][]{{2,1},{2,-1},{-2,1},{-2,-1},{1,2},{1,-2},{-1,2},{-1,-2}};
		
		for(int i=0;i<64;i++) {
			int ri = i / 8, fi = i % 8;
			long result=0;
			for(int m=0;m<moves.length;m++) {
				int r= ri+moves[m][0];
				int f= fi+moves[m][1];
				if(r <= 7 && r >= 0 && f <= 7 && f >= 0) {
					result|=SHIFT[r*8+f];
				}
			}
			all[i]=result;
		}			
		return all;
		
	}	
	
	
	private static long[][] getCheckMask() {
		long[][] checkMasks = new long[64][64];
		
		for(int i=0;i<64;i++) {
			for(int j=0;j<64;j++) {
				long result =SHIFT[j];
				if(i!=j && (QUEEN_MASKS[i]&result)!=EMPTY_MASK) {
					
					int cur = j;
					int ri = i / 8, fi = i % 8;
					int rj = j / 8, fj = j % 8;
					int rD = ri-rj;
					int fD = fi-fj;
					if((rD!=0 && fD!=0) && (rD*rD!=fD*fD))continue;
					rD=(int)Math.signum(rD);
					fD=(int)Math.signum(fD);

					for (int r = rj, f =fj;r >=0 && r <= 7 && f >=0 && f <= 7 ; r+=rD, f+=fD) {
						cur = f + r * 8;
						if(cur==i) {
							break;
						}else {
							result|= SHIFT[cur];
						}
						
					}
				}
				checkMasks[i][j]=result;
			}
		}
		return checkMasks;
	}
	
	private static long[] getEnpassantePawnPos() {
		long[] notEnpPawnMask = new long[64];
		for(int i=0;i<64;i++) {
			int file = getFileForPos(i);
			int rank = getRankForPos(i);
			if(rank==_3||rank==_6) {
				if(rank==_3) {
					rank =_4;
				}
				if(rank ==_6) {
					rank =_5;
				}
				notEnpPawnMask[i]=SHIFT[getPosForFileRank(file,rank)];
			}else {
				notEnpPawnMask[i]= EMPTY_MASK;
			}
			
		}
		return notEnpPawnMask;
	}
		
	private static long[] getKingMasks() {
		long[] kingMasks = new long[64];
		for(int i=0;i<64;i++) {
			long mask = 0;
			int fi = getFileForPos(i);
			int ri = getRankForPos(i);
			int[][] dirs = {{0,1},{0,-1},{1,0},{-1,0},{1,1},{1,-1},{-1,1},{-1,-1}};
			for(int j=0;j<dirs.length;j++) {
				int f = fi+dirs[j][0];
				int r = ri+dirs[j][1];
				if(f>=0&&f<=7&&r>=0&&r<=7) {
					mask|=SHIFT[getPosForFileRank(f,r)];
				}
			}
			kingMasks[i]=mask;
			//out(kingMasks[i]);
		}
		return kingMasks;
	}

}
