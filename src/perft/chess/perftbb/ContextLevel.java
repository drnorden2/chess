package perft.chess.perftbb;

import java.util.Arrays;

import static perft.chess.Definitions.*;

public class ContextLevel {
	private Move lastMove =null;;

	private Move[][] allMoves = new Move[64][64];
	private int[] fieldList = new int[64];  

	private long[] idsMask = new long[64];
	private int[] idsTypeColor= new int[64];
	private int lastIndex=-1;
	private int fieldCounter=0;
	
	private int cursorFieldList=0;
	private int cursorMoves=0;
	public long[] pawns = new long[2];
	public long enpMask;
	public long prevOcc;
	public long[] allOfOneColor=new long[2]; 
	public long notOcc; 
	public long occ;
	public long ntchd; 
	public int enpPos; 
	public long[] mLeftOld=new long[2];
	public long[] mRightOld=new long[2];
	public long[] mOneUpOld=new long[2];
	public long[] mTwoUpOld=new long[2];
	public int oldCastleMoves;
	public long oldCastleMovesKQ;
	public long oldCastleMoveskq;
	
	
	public int pawnMovesOld=0;
	public int limit;

	
	public ContextLevel() {
		for(int i=0;i<64;i++) {
			fieldList[i]=-1;
		}
	}
	
	
	public void extractFromRawMoves(int pos, long idMask,int idTypeColor, Move[] rawMoves) {
		idsMask[pos]=idMask;
		idsTypeColor[pos]=idTypeColor;
		Move[] moves = allMoves[pos];
		fieldList[fieldCounter++]=pos;
		int retVal = Long.bitCount(idMask);
		for(int i=0;i<retVal;i++) {
			moves[i]=rawMoves[Long.numberOfTrailingZeros(idMask)];
			if(moves[i]==null) {
				System.out.println("ULTRA WTF!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!");
				System.out.println("Pos:"+pos+" "+getPieceCharForTypeColor(idTypeColor) +"."+ idTypeColor);
				out(1L<<24);
				
				System.out.println("Mask");
				out(idsMask[pos]);
				System.out.println("lookup");
				out(getMoveMask(moves));
				System.out.println("raw");
				out(getMoveMask(rawMoves));
				
				
				System.exit(-1);
			}
			idMask&= idMask- 1;			
		}
		moves[retVal]=null;//stopMove
	}
	public long getMoveMask(Move[] moves) {
		long mask =0;
		for(int i=0;i<moves.length;i++) {
			if(moves[i]!=null) {
				mask|=1<<i;
			}
		}
		return mask;
	}
	
	
	public void addMoves(int pos, long idMask,int idTypeColor, Move[] moves) {
		idsMask[pos]=idMask;
		idsTypeColor[pos]=idTypeColor;
		
		Move[]newMoves = allMoves[pos];
		int i=0;
		for(;moves[i]!=null;i++) {
			newMoves[i]=moves[i];
		}
		newMoves[i]=null;

		
		if(idMask!=0L) {
			fieldList[fieldCounter++]=pos;
		}
	}
	public boolean checkForReuseMoves(int pos, long idMask,int idTypeColor) {
		if(idsMask[pos]==idMask && idsTypeColor[pos]==idTypeColor) {
			if(idMask!=0L) {
				fieldList[fieldCounter++]=pos;
			}
			return true;
		}
		return false;
	}
	
	public Move[] getMoves(int pos, long idMask,int idTypeColor) {
		if(idsMask[pos]==idMask && idsTypeColor[pos]==idTypeColor) {
			return allMoves[pos];
		}
		return null;
	}
	public void resetIterator() {
		this.lastIndex =-1;
		this.cursorFieldList=0;
		this.cursorMoves=0;		
	}
	public void setLimit(int limit) {
		this.limit = limit;
		resetIterator();
	}
	
	public void reInit() {
		this.lastIndex =-1;
		this.cursorFieldList=0;
		this.cursorMoves=0;
		this.limit =0;
		this.fieldCounter=0;
	}

	public Move getMove(int index) {
		if(index ==lastIndex) {
			return lastMove;
		}
		if(index>=limit) {
			//@todo WTF
			throw new RuntimeException("Index("+index+")Off Limit:"+limit);
		}
		lastMove = allMoves[fieldList[cursorFieldList]][cursorMoves++];
		if(index-1==lastIndex) {
			if(lastMove==null) {
				cursorMoves=0;
				cursorFieldList++;
				int fieldCursor = fieldList[cursorFieldList];
				if(fieldCursor==-1) {
					System.out.println("WTF FieldCursor -1");
				}
				lastMove = allMoves[fieldCursor][cursorMoves++];
			}
			lastIndex = index;
			return lastMove;
		}else {
			System.out.println("Whooza Run!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!");
			for(int i=0;i<index;i++) {
				if(allMoves[fieldList[cursorFieldList]][cursorMoves]==null) {
					cursorMoves=0;
					cursorFieldList++;
				}
				cursorMoves++;
			}
			lastIndex = index;
			lastMove = allMoves[fieldList[cursorFieldList]][cursorMoves++];
			return lastMove;
		}
	}
}
