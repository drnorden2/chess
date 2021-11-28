package perft.chess.core;

import perft.chess.core.baseliner.BLVariableInt;
import perft.chess.core.baseliner.BaseLiner;

public class Zobrist {

    private final int[][] randomBitStr= new int[64][32];
    private final BaseLiner bl;
	public final BLVariableInt zobristHash;
	private final boolean isOff=true;
    public Zobrist(BaseLiner bl) {
    	this.bl = bl;
    	// generate an array of random bitStrings
        for (int i = 0; i < randomBitStr.length; i++) {
            for (int j = 0; j < randomBitStr[i].length; j++) {
            	randomBitStr[i][j] = (int) (((long) (Long.MAX_VALUE*Math.random())) & 0xFFFFFFFF);
            }
        }
        zobristHash= new BLVariableInt(bl,0);
		
    }
    
    public void HASH(int pos, Piece piece) {
    	if(isOff)return;
		int type = Piece.PIECE_TYPE_PAWN_ENPASSANTE_OFFER;
		int touched =0;
		int color=0;
		if(piece!=null) {
			type = piece.getType();
			touched = piece.isTouched()?0:1;
			color =piece.getColor();
		}
		int element = 16* color
				+8*touched
				+type;
		int bitStr = randomBitStr[pos][element];
		zobristHash.XOR(bitStr);		
	}

	public int getHash() {
		return zobristHash.get();
	}
	public void reset() {
		zobristHash.set(0);
	}

}
