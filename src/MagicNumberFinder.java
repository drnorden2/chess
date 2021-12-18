import perft.chess.core.datastruct.BitBoard;

import static perft.chess.Definitions.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;

public class MagicNumberFinder {
	
	public static final long[] ROOK_MASKS = new long[] { -143832609275707135L, -215607624513486334L,
			-359157654989044732L, -646257715940161528L, -1220457837842395120L, -2368858081646862304L,
			-4665658569255796672L, 9187484529235886208L, 143553341945872641L, 215330564830528002L, 358885010599838724L,
			645993902138460168L, 1220211685215703056L, 2368647251370188832L, 4665518383679160384L,
			-9187483425412448128L, 72618349279904001L, 144956323094725122L, 289632270724367364L, 578984165983651848L,
			1157687956502220816L, 2315095537539358752L, 4629910699613634624L, -9187203049947365248L, 72341259464802561L,
			144681423712944642L, 289361752209228804L, 578722409201797128L, 1157443723186933776L, 2314886351157207072L,
			4629771607097753664L, -9187201954730704768L, 72340177082712321L, 144680349887234562L, 289360695496279044L,
			578721386714368008L, 1157442769150545936L, 2314885534022901792L, 4629771063767613504L,
			-9187201950452514688L, 72340172854657281L, 144680345692602882L, 289360691368494084L, 578721382720276488L,
			1157442765423841296L, 2314885530830970912L, 4629771061645230144L, -9187201950435803008L, 72340172838141441L,
			144680345676217602L, 289360691352369924L, 578721382704674568L, 1157442765409283856L, 2314885530818502432L,
			4629771061636939584L, -9187201950435737728L, 72340172838076926L, 144680345676153597L, 289360691352306939L,
			578721382704613623L, 1157442765409226991L, 2314885530818453727L, 4629771061636907199L,
			-9187201950435737473L };

	public static final long[] BISHOP_MASKS = new long[] { -9205322385119247872L, 36099303471056128L, 141012904249856L,
			550848566272L, 6480472064L, 1108177604608L, 283691315142656L, 72624976668147712L, 4620710844295151618L,
			-9205322385119182843L, 36099303487963146L, 141017232965652L, 1659000848424L, 283693466779728L,
			72624976676520096L, 145249953336262720L, 2310355422147510788L, 4620710844311799048L, -9205322380790986223L,
			36100411639206946L, 424704217196612L, 72625527495610504L, 145249955479592976L, 290499906664153120L,
			1155177711057110024L, 2310355426409252880L, 4620711952330133792L, -9205038694072573375L,
			108724279602332802L, 145390965166737412L, 290500455356698632L, 580999811184992272L, 577588851267340304L,
			1155178802063085600L, 2310639079102947392L, 4693335752243822976L, -9060072569221905919L,
			326598935265674242L, 581140276476643332L, 1161999073681608712L, 288793334762704928L, 577868148797087808L,
			1227793891648880768L, 2455587783297826816L, 4911175566595588352L, -8624392940535152127L,
			1197958188344280066L, 2323857683139004420L, 144117404414255168L, 360293502378066048L, 720587009051099136L,
			1441174018118909952L, 2882348036221108224L, 5764696068147249408L, -6917353036926680575L,
			4611756524879479810L, 567382630219904L, 1416240237150208L, 2833579985862656L, 5667164249915392L,
			11334324221640704L, 22667548931719168L, 45053622886727936L, 18049651735527937L };

	public static void main(String args[]) {
		HashMap<Long, ArrayList<Long>> rookSets = getRookAttackSets();
		MagicNumberFinder mnf = new MagicNumberFinder ();

		mnf.rookMaskGenerator();
		mnf.bishopMaskGenerator();
		if(true)return;
		int pos = 34;
		long occ =0L;
		for(int i=0;i<30;i++) {
			occ|= 1L<<((int)(Math.random()*64));
		}
		long bishop = 1L<<pos;
		System.out.println(BitBoard.toString(bishop));
		long mask = BISHOP_MASKS[pos];
		System.out.println(BitBoard.toString(mask));
		System.out.println(BitBoard.toString(occ));
		
		
	}
		
	public void rookMaskGenerator() {
		long [] rookMasks = new long[64];
		for(int i=0;i<64;i++) {
			int rank = getRankForPos(i);
			int file = getFileForPos(i);
			rookMasks[i]^= MASK_X_FILE[file];
			rookMasks[i]^= MASK_X_RANK[rank];
			//if(file!=_A ) rookMasks[i] &= MASK_NOT_A_FILE;
			//if(file!=_H ) rookMasks[i] &= MASK_NOT_A_FILE;
			if(rank!=_1 ) rookMasks[i] &= MASK_NOT_1_RANK;
			if(rank!=_8 ) rookMasks[i] &= MASK_NOT_8_RANK;
			System.out.println("Rank"+rank +"="+_1);
			System.out.println(BitBoard.toString(MASK_NOT_1_RANK));	
			System.out.println(BitBoard.toString(rookMasks[i]));
		}
		
		//codeGenerator
		System.out.println("public static final long[] ROOK_MASKS= new long[]{");
		for(int i=0;i<64;i++) {
			System.out.print(rookMasks[i]);
			if(i!=63) {
				System.out.println("L,");
			}else {
				System.out.println("L};");
			}
		}	
	}
	public void bishopMaskGenerator() {
		long [] bishopMasks = new long[64];
		for(int i=0;i<64;i++) {
			long nw=1L <<i;
			long no=1L <<i;
			long sw=1L <<i;
			long so=1L <<i;
			int rank = getRankForPos(i);
			int file = getFileForPos(i);
			
			for(int j=1;j<=8;j++) {
				nw = (nw<<DIR_UP_LEFT) & MASK_NOT_8_RANK & MASK_NOT_A_FILE;
				no = (no<<DIR_UP_RIGHT) & MASK_NOT_8_RANK & MASK_NOT_H_FILE;
				sw = (sw>>DIR_UP_LEFT) & MASK_NOT_1_RANK & MASK_NOT_H_FILE;
				so = (so>>DIR_UP_RIGHT) & MASK_NOT_1_RANK & MASK_NOT_A_FILE;

				bishopMasks[i]|=nw|no|sw|so;
			}
			if(file!=_A ) bishopMasks[i] &= MASK_A_FILE;
			if(file!=_H ) bishopMasks[i] &= MASK_A_FILE;
			if(rank!=_1 ) bishopMasks[i] &= MASK_1_RANK;
			if(rank!=_8 ) bishopMasks[i] &= MASK_8_RANK;

		}
		
		//codeGenerator
		System.out.println("public static final long[] BISHOP_MASKS= new long[]{");
		for(int i=0;i<64;i++) {
			System.out.print(bishopMasks[i]);
			if(i!=63) {
				System.out.println("L,");
			}else {
				System.out.println("L};");
			}
		}	
	}
	
	
	
	public static HashMap<Long, ArrayList<Long>>  getRookAttackSets(){
		int total = (int)Math.pow(2, 12);
		long[] allBits = new long[total];
		HashMap<Long, ArrayList<Long>> all = new HashMap<Long, ArrayList<Long>>();
		
		
		for(int i=0;i<total;i++) {
			allBits[i]=(long)i;
		}
		
		for(int i=0;i<1;i++) {
			
			int rank = getRankForPos(i);
			int file = getFileForPos(i);
		
			for(int j=0;j<total;j++) {
				BitBoard rook = new BitBoard(0L);
				BitBoard curBits = new BitBoard(allBits[j]);
				
				int cursor =0;
				for(int f=0;f<8;f++) {
					if(f==file) {
						continue;
					}					
					if(curBits.get(cursor++)){
						rook.set(getPosForFileRank(f,rank));
					}
				}
				for(int r=0;r<8;r++) {
					if(r==rank) {
						continue;
					}
					if(curBits.get(cursor++)){
						rook.set(getPosForFileRank(file,r));
					}
				}
				long cur = rook.getBits();
				long key = getKey(rook.getBits(), i);
				//System.out.println("\norig:"+BitBoard.toString(cur));
				//System.out.println("\nclip:"+BitBoard.toString(key));
				ArrayList<Long> list = all.get(key);
				if(list ==null) {
					all.put(key, new ArrayList<Long>());
				}
				all.get(key).add(cur);
			}	
		}
		/*
		for(Long key:all.keySet()) {
			ArrayList<Long>same = all.get(key);
			System.out.println(key+": "+ same);
			System.out.println("\nOrig:"+BitBoard.toString(key));
			for(Long var:all.get(key)) {
				System.out.println("\nvar:"+BitBoard.toString(var));
							
			}
		}*/
		return all;
	}

	private static long getKey(long bits, int pos) {
		BitBoard rook = new BitBoard(bits);
		int rank = getRankForPos(pos);
		int file = getFileForPos(pos);

		boolean gap = false;
		gap = false;
		for(int f=file+1;f<7;f++) {
			if(!gap) {
				if(!rook.get(getPosForFileRank(f,rank))){
					gap = true;
				}
				continue;
			}
			rook.unset(getPosForFileRank(f,rank));
		}
		gap = false;
		for(int f=file-1;f>=1;f--) {
			if(!gap) {
				if(!rook.get(getPosForFileRank(f,rank))){
					gap = true;
				}
				continue;
			}
			rook.unset(getPosForFileRank(f,rank));
		}
		
		gap = false;
		for(int r=rank+1;r<7;r++) {
			if(!gap) {
				if(!rook.get(getPosForFileRank(file,r))){
					gap = true;
				}
				continue;
			}
			rook.unset(getPosForFileRank(file,r));
		}
		gap = false;
		for(int r=rank-1;r>=1;r--) {
			if(!gap) {
				if(!rook.get(getPosForFileRank(file,r))){
					gap = true;
				}
				continue;
			}
			rook.unset(getPosForFileRank(file,r));
		}
		
		return rook.getBits();
	}
}
