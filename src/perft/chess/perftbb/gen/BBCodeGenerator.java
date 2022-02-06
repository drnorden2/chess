package perft.chess.perftbb.gen;


import static perft.chess.Definitions.*;


public class BBCodeGenerator {
	public static void main (String args[]) {
		BBCodeGenerator bbcg = new BBCodeGenerator ();
		bbcg.generateRookMasksCode();
		bbcg.generateBishopMasksCode();
		
	}
	public void generateRookMasksCode() {
		long [] rookMasks = new long[64];
		for(int i=0;i<64;i++) {
			int rank = getRankForPos(i);
			int file = getFileForPos(i);
			rookMasks[i]^= MASK_X_FILE[file];
			rookMasks[i]^= MASK_X_RANK[rank];
			if(file!=_A ) rookMasks[i] &= MASK_NOT_A_FILE;
			if(file!=_H ) rookMasks[i] &= MASK_NOT_H_FILE;
			if(rank!=_1 ) rookMasks[i] &= MASK_NOT_1_RANK;
			if(rank!=_8 ) rookMasks[i] &= MASK_NOT_8_RANK;
			//System.out.println(BitBoard.toString(rookMasks[i]));
		}
		
		//codeGenerator
		System.out.println("public static final long[] ROOK_MASKS= new long[]{");
		for(int i=0;i<64;i++) {
			System.out.print(String.format("0x%1$016X", rookMasks[i]));
			if(i!=63) {
				System.out.println("L,");
			}else {
				System.out.println("L};");
			}
		}	
	}
	public void generateBishopMasksCode() {
		long [] bishopMasks = new long[64];
		for(int i=0;i<64;i++) {
			long nw=1L <<i;
			long no=1L <<i;
			long sw=1L <<i;
			long so=1L <<i;
			
			for(int j=0;j<8;j++) {
				nw = (nw<<DIR_UP_LEFT) & MASK_NOT_1_RANK & MASK_NOT_H_FILE;
				no = (no<<DIR_UP_RIGHT) & MASK_NOT_1_RANK & MASK_NOT_A_FILE;
				sw = (sw>>>DIR_UP_LEFT) & MASK_NOT_8_RANK & MASK_NOT_A_FILE;
				so = (so>>>DIR_UP_RIGHT) & MASK_NOT_8_RANK & MASK_NOT_H_FILE;

				
				bishopMasks[i]|=nw|no|sw|so;
			}
			int rank = getRankForPos(i);
			int file = getFileForPos(i);
			if(file!=_A ) bishopMasks[i] &= MASK_NOT_A_FILE;
			if(file!=_H ) bishopMasks[i] &= MASK_NOT_H_FILE;
			if(rank!=_1 ) bishopMasks[i] &= MASK_NOT_1_RANK;
			if(rank!=_8 ) bishopMasks[i] &= MASK_NOT_8_RANK;
			out(bishopMasks[i]);
		}
		
		//codeGenerator
		System.out.println("public static final long[] BISHOP_MASKS= new long[]{");
		for(int i=0;i<64;i++) {
			System.out.print(String.format("0x%1$016X", bishopMasks[i]));
			if(i!=63) {
				System.out.println("L,");
			}else {
				System.out.println("L};");
			}
		}	
	}

}
