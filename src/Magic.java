import perft.chess.core.datastruct.BitBoard;

import static perft.chess.Definitions.*;
import perft.chess.perftbb.gen.*;


public class Magic {
	
	
	public static void main(String args[]) {
		long test = 254L;
		int[] indices = new int[64];
		int count = updateIndices(indices,test);
		for(int i=0;i<count;i++) {
			System.out.println(i+":"+indices[i]);
		}
		
		if(true)return;
		MagicNumberFinder mnf = new MagicNumberFinder ();
		int pos = 34;
		long occ =0L;
		occ|= 1L<<35;
		occ|= 1L<<38;
		
		long bishop = 1L<<pos;
		out(bishop);
		out(occ);
		long attacks = mnf.getBishopAttacks(pos,occ);
		attacks|= mnf.getRookAttacks(pos,occ);
		out(attacks);
	}
	
		
}
