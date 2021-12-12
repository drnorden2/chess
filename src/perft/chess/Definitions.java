package perft.chess;

public class Definitions {
	public static final int PIECE_TYPE_PAWN = 0;
	public static final int PIECE_TYPE_KNIGHT = 1;
	public static final int PIECE_TYPE_BISHOP = 2;
	public static final int PIECE_TYPE_ROOK = 3;
	public static final int PIECE_TYPE_QUEEN = 4;
	public static final int PIECE_TYPE_KING= 5;
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

	public static final int _8 = 0;
	public static final int _7 = 1;
	public static final int _6 = 2;
	public static final int _5 = 3;
	public static final int _4 = 4;
	public static final int _3 = 5;
	public static final int _2 = 6;
	public static final int _1 = 7;

	public static final int ROCHADE_W_ROOK_LONG = 2;
	public static final int ROCHADE_W_ROOK_SHORT = 6;
	public static final int ROCHADE_B_ROOK_LONG = 58;
	public static final int ROCHADE_B_ROOK_SHORT = 62;
			
	public static final int GAME_STATE_NORMAL=0;
	public static final int GAME_STATE_CHECK=1;

			
/*Static*/
	
	public static int getRankForPos(int pos) {
		return (int)(pos/8);
	}
	public static int getFileForPos(int pos) {
		return (int)(pos%8);
	}
	
	public static int getPosForRankFile(int rank, int file) {
 		int index = 8 * rank + file;
		return index;
	}

}
