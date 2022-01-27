package perft.chess.perftmb;



import perft.chess.core.baseliner.BLArrayInt;

import perft.chess.core.baseliner.BLVariableInt;
import perft.chess.core.baseliner.BaseLiner;
import perft.chess.core.baseliner.BLIndexedList;
import perft.chess.core.o.O;

import static perft.chess.Definitions.*;

import perft.chess.Position;
import perft.chess.core.datastruct.ArrayStack;


public class MBPosition implements Position{
	private int totalCount;
	private MBAnalyzer analyzer;
	public static MBPosition position;
	public LegalMoveTester legalMoveTest;
	
	
	//private final Zobrist zobrist;
  
	
	final int depth = 8;
	public final BaseLiner bl = new BaseLiner(10000,10000,65,depth,1000);
	final MoveManager moveManager;
	int color=COLOR_WHITE;
    int movesPlayed = 0;
	public static int registerCount =0;
	public static int unRegisterCount =0;
	
	
	/**under base line**/
	final Field[] fields = new Field[64];
	private final Piece[] kingPieces = new Piece[2];	
	ArrayStack<ArrayStack<Move>> allMovesLists = new ArrayStack<ArrayStack<Move>>(new ArrayStack[depth]);
	
	final BLArrayInt[] attackTable = new BLArrayInt[2];
	final BLIndexedList<Piece>[] allPieces= new BLIndexedList[2];
	public final BLVariableInt  enPassantePos;
	public final BLVariableInt[]  isCheck = new BLVariableInt[2];
	
	

	
	
	public MBPosition() {
		MBPosition.position = this;
		analyzer = new MBAnalyzer(this);
		this.legalMoveTest = new LegalMoveTester(position);
		for(int i=0;i<fields.length;i++) {
			fields[i] = new Field(bl,this,i);
		}
		
		moveManager = new MoveManager(bl,this);
		
		allPieces[COLOR_BLACK] = new BLIndexedList<Piece>(bl, 16, 32);
		allPieces[COLOR_WHITE] = new BLIndexedList<Piece>(bl, 16, 32);

		attackTable[COLOR_BLACK] = new BLArrayInt(bl, 64, 0);
		attackTable[COLOR_WHITE] = new BLArrayInt(bl, 64, 0);

		
		
		isCheck[COLOR_BLACK] = new BLVariableInt(bl,GAME_STATE_NORMAL);
		isCheck[COLOR_WHITE] = new BLVariableInt(bl,GAME_STATE_NORMAL);
		//zobrist = new Zobrist(bl);
		
		for(int i=0;i<depth;i++) {
			ArrayStack<Move> allMoves = new ArrayStack<Move>(new Move[36*16]);
			this.allMovesLists.add(allMoves);
		}
		
		
		this.enPassantePos = new BLVariableInt(bl,-1);
	}
	
	public long getHash() {
		return 0;//zobrist.getHash();
	}
	
	public void initialEval() {
		System.out.println("WARNING!!!!!!!!!!!!!EXPENSIVE");
		
		for(int i=0;i<64;i++) {
			Piece piece = this.fields[i].getPiece();
			if(piece!=null) {
				int movesIndex = piece.getMoveIndex();
				Move[][] moves = position.moveManager.getRawMoves(movesIndex);
				this.fields[i].addRemovePseudoMoves(piece,moves, -1,-1,true);
			}
			this.fields[i].callBacks.removeAll();
		}
		for (int i = 0; i < 2; i++) {
			for (int j = 0; j < 64; j++) {
				this.attackTable[i].set(j,0);
			}
		}
		
		
		for (int i = 0; i < 2; i++) {
			for (int j = 0; j < allPieces[i].size(); j++) {
				Piece piece = allPieces[i].getElement(j);
				if(piece.getType()==PIECE_TYPE_KING && i==COLOR_WHITE) {
			//		O.UT("Adding the King should cause a pin: BEFORE:"+this);
				}
				if(piece.getPosition()==-1) {
					// no longer on board 
					
					O.VER("Piece is gone!"+piece);
				}
				int moveIndex = piece.getPosition() +(PIECE_TYPE_ANY* 2 + i) * 64;
				Move[][] moves = this.moveManager.getRawMoves(moveIndex);
				this.moveBeforeBaseLine(moves[0][0]);
			}
		}
		//zobrist.reset();

		int enpassante = this.enPassantePos.get();
		if(enpassante !=-1) {
			//zobrist.HASH(enpassante,null);
		}
		for (int i = 0; i < 64; i++) {
			Piece piece = fields[i].getPiece();
			//zobrist.HASH(i,piece);
		}
		//O.EXIT("initialEval");
		int level = getLevel();
		this.allMovesLists.get(level).reset();
		this.checkLegalMoves();
		
	}
	
	
	//by game under baseline!
	private void finalTakeFromBoard(Piece piece) {
		if(piece!=null) {
			allPieces[piece.getColor()].remove(piece);
		}
	}
	//by Fen(does not know BL

	@Override
	public void initialAddToBoard(int color, int type, int pos) {
		Piece piece = new Piece(bl,this, type, color, true);
		fields[pos].stagePiece(piece);
		allPieces[piece.getColor()].add(piece);
	}

	public void takeTurn() {
		color = (color+1)%2;
	}
	
	public void initialTurn(int color) {
		this.color = color;
	}
	public void initialUntouched(int rank, int file) {
		fields[getPosForFileRank(file,rank)].getPiece().isUntouched();
	}
		
	
	public void unSetMove(int index) {
		allMovesLists.get(getLevel()).reset();; 
		bl.undo();
		takeTurn();		
		movesPlayed--;

	}
		
	public static int counter=0;
	boolean experimental = false;

	public void setMove(int index) {
		totalCount++;
		Move move= getMove(index);
		/*
				
		//String start =this.toString();
		if(this.wtfIteration==3 ) {
			System.out.println("
 - Move("+ this.wtfIteration+ "):"+move);
			String cur  = this.toString();
			System.out.println(cur);
		}

		
		if(this.wtfIteration==2 ) {
			System.out.println("WTF - Move("+ this.wtfIteration+ "):"+move);
			String cur  = this.toString();
			System.out.println(cur);
		}
		String before = this.toString(); 

		
		System.out.println("Move start:"+this.wtfIteration+"-----------------------------"+this);
*/
		
		
		bl.startNextLevel();
		this.moveBeforeBaseLine(move);				
		this.takeTurn();
		checkGameState(color);			
		this.checkLegalMoves();
	
/*		
		System.out.println("Move End:"+this.wtfIteration+"-----------------------------"+this);
	
		
		
		String cur  = this.toString();
		
		
		bl.undo();
		allMovesLists.get(getLevel()).reset(); 		
		this.takeTurn();
		
		bl.startNextLevel();
		this.moveBeforeBaseLine(move);				
		movesPlayed++;
		this.takeTurn();
		this.initialEval();
		checkGameState(position.getColorAtTurn());
		this.legalMoveTest.checkLegalMovesReference();
		
		String ref = this.toString();
		
		
		if(!cur.equals(ref)) {
			counter++;
			System.out.println("WARNING: Mismatch in round "+ this.wtfIteration +" after move "+move+"!!!");
			
			System.out.println(
					"Before:\n"+before+
					"-------------------\n"+
					"Reference:\n"+ref+
					"-------------------\n"+
					"DuT:\n"+cur+
					"-------------------\n"+
					analyzer.diffPositions(ref, cur));

			throw new RuntimeException("different"+this.wtfIteration);
		}
	*/	
	/*		
		}catch(Exception e) {
			O.UT("Error Moving: is it Simulated?" +bl.isAlreadyInSimulation);
			throw e;
		} 
*/
	}

	public void addLegalMove(Move move) {
		int level = getLevel();
		allMovesLists.get(level).add(move);
	}
	
	
	
	public void moveBeforeBaseLine(Move move) {
		int oldPos = move.getOldPos();
		int newPos = move.getNewPos();
		Field oldField = fields[oldPos];
		
		Field newField = fields[newPos];
		boolean replace = false;

		Piece piece = oldField.getPiece();
		boolean isPieceTouched = piece.isTouched();
		
		/*
		if(oldPos!=newPos && move.getNewPos()==position.getKingPos(1) && this.color==0) {
			System.out.println("WTF"+this.wtfIteration+" "+move+":" +this);
		}*/
		
		int oldEnpassantePos =enPassantePos.get();
		if(oldPos!=newPos) {
			if(oldEnpassantePos !=-1) {
				//zobrist.HASH(oldEnpassantePos,null);		
				enPassantePos.set(-1);
				fields[oldEnpassantePos].notifyCallBacks(NOTIFY_NOW_EMPTY_ENPASSANTE_FIELD,(color+1)%2,oldPos,false,-1);
			}
		}
		
		
		if(oldPos==newPos) {
			//newField.removePseudoMoves(piece,-1, -1);
			newField.stagePseudoMoves();
		}else {	
			int moveType = move.getMoveType();
			
			Field enPassanteField  = null;
			//remove old
			//zobrist.HASH(oldPos,piece);
			oldField.unstagePiece(piece);
				
			
			
			
			Piece otherPiece = newField.getPiece();
			boolean isKnight = false;
			oldField.notifyCallBacks(NOTIFY_NOW_EMPTY,this.color,newPos,isKnight,-1); 
			
			if(otherPiece!=null) {
				//zobrist.HASH(newPos,otherPiece);
				replace =true;
				
			 	newField.unstagePiece(otherPiece);
				finalTakeFromBoard(otherPiece);
			 	
			 	isKnight  = ((otherPiece.getType()==PIECE_TYPE_KNIGHT));

			 	//flip type prior to setting it on field => for moveIndex!
			} else {
				if(piece.getType()==PIECE_TYPE_PAWN) {
					if (move.isEnpassanteMove()&& move.getNewPos()==oldEnpassantePos){// other field is empty =>ergo enpassante
						enPassanteField = fields[move.getEnPassantePawnPos()];				
						otherPiece = enPassanteField.getPiece();
						//zobrist.HASH(move.getEnPassantePawnPos(),otherPiece);
						enPassanteField.unstagePiece(otherPiece);
						finalTakeFromBoard(otherPiece);
						enPassanteField.notifyCallBacks(NOTIFY_NOW_EMPTY,otherPiece.getColor(),oldPos, false,-1);
						
					}
				}		
			}
			checkPromotion(move, piece);
			newField.stagePiece(piece);
			//newField.unStageAllMoves(piece);
			newField.stagePseudoMoves();
			//zobrist.HASH(newPos,piece);
			
			newField.notifyCallBacks(replace?NOTIFY_NOW_REPLACED:NOTIFY_NOW_OCCUPIED,piece.getColor(),oldPos,false,-1); 
			
			// Move rook to new position in rochade before reevaluation of King (new rook position will block one king move!
			if(moveType==MOVE_TYPE_ROCHADE) {
				this.moveBeforeBaseLine(move.getRookMove());
			}
		}
		//dynamic enPassante handling							
		if(move.isTwoSquarePush()) {
			enPassantePos.set(move.getEnPassanteSquare());
			//zobrist.HASH(move.getEnPassanteSquare(),null);
			fields[move.getEnPassanteSquare()].notifyCallBacks(NOTIFY_NOW_OCCUPIED_ENPASSANTE_FIELD, piece.getColor(),oldPos,false,-1);
		}
	}

	
	private void checkPromotion(Move move, Piece piece) {
		if(move.getMoveType()==MOVE_TYPE_PAWN_BEAT_CONVERT||move.getMoveType()==MOVE_TYPE_PAWN_PUSH_CONVERT) {
			piece.setType(move.getPromotePieceType());
		}
				
	}

	
	public void checkGameState(int color){
		int kingPos = this.getKingPos(color);
		int otherColor = (color+1)%2;
		if(kingPos ==-1) {
			//System.out.println("WTF Kingone!");
			isCheck[otherColor].set(GAME_STATE_NORMAL);		
			isCheck[color].set(GAME_STATE_NORMAL);			
			return;
		}
		isCheck[otherColor].set(GAME_STATE_NORMAL);		
		if(attackTable[otherColor].get(kingPos)!=0) {
			isCheck[color].set(GAME_STATE_CHECK);
		}else {
			isCheck[color].set(GAME_STATE_NORMAL);			
		}
	}
		
	public int getMoveCount() {
		return allMovesLists.get(getLevel()).size();
	}
	
	public Move getMove(int index) {
		return this.allMovesLists.get(getLevel()).get(index);
	}

	public boolean isCheck(int color){
		return (isCheck[color].get()==GAME_STATE_CHECK);
	}
	public void setKing(int color, Piece kingPiece) {
		this.kingPieces[color] = kingPiece;
	}
	
	public int getKingPos(int color) {
		if(this.kingPieces[color]!=null) {
			return this.kingPieces[color].getPosition();
		}else {
			return -1;
		}
	}
	int getLevel(){
		return bl.getLevel()-1;
	}

	@Override
	public String toString() {
		return analyzer.toString();
	}

	@Override
	public int getColorAtTurn() {
		return color;
	}

	
	@Override
	public String getNotation(int index) {
		return this.getMove(index).getNotation();
	}


	@Override
	public void initialEnPassantePos(int enpassantePos) {
		this.enPassantePos.set(enpassantePos);
	}

	
	@Override
	public void checkLegalMoves() {
		this.legalMoveTest.checkLegalMovesOpt();
	}
	public int[] getAttacks (int color){
		int[] attacks = new int[64];
		for (int j = 0; j < 64; j++) {
			attacks[j] = this.attackTable[color].get(j);
		}
		return attacks;
	}
	@Override
	public int getTotalCount() {
		return totalCount;
	}
	public boolean isCheck() {
		return false;
	}
	public double evaluate(int i) {
		return -1;
	}

}
