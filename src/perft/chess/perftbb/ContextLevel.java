package perft.chess.perftbb;

import static perft.chess.Definitions.updateIndices;

import java.util.Arrays;

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

	public long enpMask;
	public long prevOcc;
	public long white; 
	public long black; 
	public long notOcc; 
	public long occ;
	public long ntchd; 
	public long correction;
	public int enpPos; 

	
	public ContextLevel() {
		for(int i=0;i<64;i++) {
			fieldList[i]=-1;
		}
	}
	
	
	public void extractFromRawMoves(int pos, long idMask,int idTypeColor, Move[] rawMoves) {
		idsMask[pos]=idMask;
		idsTypeColor[pos]=idTypeColor;
		Move[] moves = allMoves[pos];
		if(idMask!=0L) {
			fieldList[fieldCounter++]=pos;
		}	
		int retVal = Long.bitCount(idMask);
		for(int i=0;i<retVal;i++) {
			moves[i]=rawMoves[Long.numberOfTrailingZeros(idMask)];
			idMask&= idMask- 1;			
		}
		moves[retVal]=null;//stopMove
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
		lastIndex =-1;
		cursorFieldList=0;
		cursorMoves=0;
	}
	
	public void reInit() {
		lastIndex =-1;
		fieldCounter=0;
		cursorFieldList=0;
		cursorMoves=0;
	}

	public Move getMove(int index) {
		if(index ==lastIndex) {
			return lastMove;
		}
		lastMove = allMoves[fieldList[cursorFieldList]][cursorMoves++];
		if(index-1==lastIndex) {
			if(lastMove==null) {
				cursorMoves=0;
				cursorFieldList++;
				lastMove = allMoves[fieldList[cursorFieldList]][cursorMoves++];
			}
			lastIndex = index;
			return lastMove;
		}else {
			System.out.println("!");
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
