package perft.chess.core;
import static perft.chess.core.Move.*;
import perft.chess.core.baseliner.BLIndexedListBB;


import java.util.ArrayList;

import perft.chess.core.baseliner.BaseLiner;
import perft.chess.core.o.O;

public class MoveManager {
	private static Move[][][] moves= new Move[1280][][];
	private static BLIndexedListBB<Move>[] pseudoMoveSets = new BLIndexedListBB[1280];
	private final BaseLiner bl;
	private final Position position;
	public static final int[][] trackBack = new int[64][64]; 
	public final static boolean[][] isPieceAttacker= new boolean[1280][64];
	
	final Field[] fields = new Field[64];


	public MoveManager(BaseLiner bl, Position position) {
		this.bl = bl;
		this.position = position;
		setup();
	}
	
	private void setup() {
		for (int color = 0; color < 2; color++) {
			O.UT("MoveCounter:"+bl.getCounter(Move.INDEX_Counter_ID));
			bl.resetCounter(Move.INDEX_Counter_ID);
			for (int pieceType = 0; pieceType < 7; pieceType++) {
				int offset = 64 * (pieceType * 2 + color);

				for (int rank = 0; rank < 8; rank++) {
					for (int file = 0; file < 8; file++) {
						Move[][] curMoves = new Move[18][7];
						switch (pieceType) {
						
						case Piece.PIECE_TYPE_PAWN:
						//	O.UT("PAWN"+color);
							int colorSwitch = color==0?1:-1;
							if((rank !=_1 && color ==Piece.COLOR_WHITE )||(rank != _8 && color ==Piece.COLOR_BLACK )) {
								//moves only starting from 2nd row
								{
									int moveType = Move.MOVE_TYPE_PAWN_BEAT;
									int callbackType = FieldCallback.CALLBACK_TYPE_BEAT_AS_PAWN;
									
									//special case enpassante indicated by Movetype
									if((rank ==_5 && color ==Piece.COLOR_WHITE )||(rank == _4 && color ==Piece.COLOR_BLACK)) {
										moveType = Move.MOVE_TYPE_PAWN_BEAT_OR_ENPASSANTE;
										callbackType = FieldCallback.CALLBACK_TYPE_OTHER;
									}
									generateMoves(curMoves, color,new int[][]{{-1,colorSwitch},{1,colorSwitch}}, 0, file, rank,1, moveType,callbackType);
								}
								int steps =1;
								//2 steps if 2nd row
								if((rank ==_2 && color ==Piece.COLOR_WHITE )||(rank == _7 && color ==Piece.COLOR_BLACK )) {
									steps =2;
								}
								
								generateMoves(curMoves, color,new int[][]{{0,colorSwitch}}, 2, file, rank,steps,Move.MOVE_TYPE_PAWN_PUSH,FieldCallback.CALLBACK_TYPE_PUSH_RAY);
								//if right before finish line
								if((rank ==_2 && color ==Piece.COLOR_BLACK)||(rank == _7 && color ==Piece.COLOR_WHITE )) {
									for(int i=0;i<3;i++) {
										int moveType = -1;
										int callbackType = -1;
									
										if(i==2) {//Push
											moveType=Move.MOVE_TYPE_PAWN_PUSH_CONVERT;
											callbackType= FieldCallback.CALLBACK_TYPE_PUSH_ONE;
										}else {//Beat
											moveType=Move.MOVE_TYPE_PAWN_BEAT_CONVERT;											
											callbackType= FieldCallback.CALLBACK_TYPE_BEAT_AS_PAWN;
										}
										if(curMoves[i][0]!=null) { // skip the sidewise beating 
											Move oldMove = curMoves[i][0];
											curMoves[i][0] =null; //deleted for potential promotions
											//4 options rook, bishop, knight and queen 
											for(int j=0;j<4;j++) {
												int promotePieceType = j+1; //bishop, knight, rook, queen
												curMoves[3+(4*i)+j] = new Move[] {new Move(color,callbackType ,oldMove.getOldPos(),oldMove.getNewPos(), moveType,0,0,promotePieceType)}; 
											}
										}
									}
								}										
							}
							//O.UT("Index: " + getIndex(file, rank) + " file: "+file+" Rank: "+rank+" color:"+color);
							break;
						case Piece.PIECE_TYPE_BISHOP:
							//O.UT("BISHOP "+color);
							generateMoves(curMoves,color, new int[][]{{-1,-1},{-1,1},{1,-1},{1,1}}, 0, file, rank,7, Move.MOVE_TYPE_PUSH_BEAT,FieldCallback.CALLBACK_TYPE_BEAT_RAY);
							break;
						case Piece.PIECE_TYPE_KNIGHT:
							//O.UT("KNIGHT "+color);
							generateMoves(curMoves, color,new int[][]{{2,1},{2,-1},{-2,1},{-2,-1},{1,2},{1,-2},{-1,2},{-1,-2}}, 0, file, rank,1, Move.MOVE_TYPE_PUSH_BEAT,FieldCallback.CALLBACK_TYPE_BEAT_ONE);
							break;
						
						case Piece.PIECE_TYPE_ROOK:
							//O.UT("ROOK_T "+color);
							generateMoves(curMoves, color,new int[][]{{1,0},{-1,0},{0,-1},{0,1}}, 0, file, rank,7, Move.MOVE_TYPE_PUSH_BEAT,FieldCallback.CALLBACK_TYPE_BEAT_RAY);
							break;
							
						
						case Piece.PIECE_TYPE_KING:
							//O.UT("KING_T "+color);
							//O.UT("KING_T "+color+"file "+file +" Rank:"+rank+" Offset:"+offset);
							//generate the sensing moves to detect pinning

							generateMoves(curMoves, color,new int[][]{{1,0},{-1,0},{-1,-1},{-1,1},{1,-1},{1,1},{0,-1},{0,1}}, 0, file, rank,7, Move.MOVE_TYPE_KING_SENSING,FieldCallback.CALLBACK_TYPE_CHECK_PIN);
							generateMoves(curMoves, color,new int[][]{{1,0},{-1,0},{-1,-1},{-1,1},{1,-1},{1,1},{0,-1},{0,1}}, 0, file, rank,1, Move.MOVE_TYPE_PUSH_BEAT,FieldCallback.CALLBACK_TYPE_OTHER);
							generateMoves(curMoves, color,new int[][]{{2,1},{2,-1},{-2,1},{-2,-1},{1,2},{1,-2},{-1,2},{-1,-2}}, 8, file, rank,1, Move.MOVE_TYPE_KING_SENSING,FieldCallback.CALLBACK_TYPE_CHECK_KNIGHT_ATTACK);
							
							
							// add the rochade as ray moves to 1 and 2 options on the E1/E8 position
							if((rank ==_1 && file ==_E  && color ==Piece.COLOR_WHITE)||(rank == _8 && file ==_E && color ==Piece.COLOR_BLACK )) {
								for(int i=0;i<2;i++) {
									int dir = (i==0)?1:-1;
									int oldPos = getPos(rank,file);
									int newPos = getPos(rank,file+2*dir);
									int rookPos = getPos(rank,file+((i==0)?3:-4));
									curMoves[i][1] = new Move(color,FieldCallback.CALLBACK_TYPE_ROCHADE_TEST , oldPos,newPos, Move.MOVE_TYPE_ROCHADE, 0,0,-1,rookPos, dir);
									int counter =2;
									
									for (int j = oldPos + dir*3; j != rookPos+dir; j = j + dir) {
										curMoves[i][counter++] = new Move(color,FieldCallback.CALLBACK_TYPE_ROCHADE_TEST , oldPos,j, Move.MOVE_TYPE_KING_SENSING,dir, 0);
										//O.UT("added at "+i+","+(counter-1)+" "+curMoves[i+8][counter-1]);
									}								
								}
							}
							break;
						case Piece.PIECE_TYPE_QUEEN:
							//O.UT("QUEEN "+color+"file "+file +" Rank:"+rank+" Offset:"+offset);
							generateMoves(curMoves, color,new int[][]{{-1,0},{1,0},{-1,-1},{-1,1},{1,-1},{1,1},{0,-1},{0,1}}, 0, file, rank,7, Move.MOVE_TYPE_PUSH_BEAT,FieldCallback.CALLBACK_TYPE_BEAT_RAY);
							break;
						
						case Piece.PIECE_TYPE_ANY:
							//public Move(BaseLiner bl,int callbackType, int oldPos, int newPos, int moveType) {
							curMoves[0] = new Move[] {new Move(color,FieldCallback.CALLBACK_TYPE_OTHER , getPos(rank,file),getPos(rank,file), Move.MOVE_TYPE_INITAL_PLACEMENT,0,0)}; 
							break;
						
						}
						addMoves(curMoves, offset, getPos(rank, file),pieceType);
					}
				}
			}
		}
		generatePieceAttacker();
		generateTrackBack();
	}
	
	public Move[][] getRawMoves (int index) {
		return this.moves[index];
	}
	public BLIndexedListBB<Move> getPseudoMoves (int index) {
		return this.pseudoMoveSets[index];
	}

	private void generateMoves(Move[][] curMoves, int color, int[][]  dirs, int rayOffset,int file, int rank,int maxSteps,int moveType, int callbackType) {
		for (int ray = 0; ray < dirs.length; ray++) {
			int dirX = dirs[ray][0];
			int dirY = dirs[ray][1];
			int oldPos = getPos(rank,file);
			int cursor = 0;
			
			
			for (int i = 0; i < maxSteps; i++) {
				boolean isInBounds = isInBounds(file + dirX * (i + 1), rank + dirY * (i + 1));
				/*
				if(moveType == Move.MOVE_TYPE_KING_SENSING & !isInBounds(file + dirX * (i + 2), rank + dirY * (i + 2))) {
					break;
				}*/
				if (isInBounds) {
					int newPos = getPos(rank + dirY * (i + 1),file + dirX * (i + 1));
					int cb = callbackType;
					
					// last in a row works link one that cannot mask out others
					// perft(6) w. feature: Opt:4196382 vs Opt:4000980
					boolean last = (i+1==maxSteps ||!isInBounds(file + dirX * (i + 2), rank + dirY * (i + 2)));
					if(cb == FieldCallback.CALLBACK_TYPE_BEAT_RAY && last) {
						cb = FieldCallback.CALLBACK_TYPE_BEAT_ONE;
					}
					Move curMove = new Move(color,cb ,oldPos, newPos, moveType,dirX,dirY); ;
					
					curMoves[ray+rayOffset][cursor++] = curMove;
				} else {
					break;
				}
			}
			
		}
	}

	
	private void generatePieceAttacker() {
		for(int ii=0;ii<1280;ii++) {
			Move[][] moves = getRawMoves(ii);
			if(moves==null) {
				continue;
			}
			for(int jj=0;jj<64;jj++) {
				int kingPos = jj;	
				boolean isAttacker =false;
				for(int i=0;i<moves.length;i++) {
					for(int j=0;j<moves[i].length;j++) {
						Move move = moves[i][j];
						if(move.isAttackerMove()) {
							if(move.getNewPos()==kingPos) {
								isPieceAttacker[ii][jj]=true;
								isAttacker =true;
							}
						}
						if(isAttacker) {
							break;
						}
					}
					if(isAttacker) {
						break;
					}

				}
			}
		}
	}
	
	
	private static boolean isInBounds(int file, int rank) {
		return (file >= 0 && file <= 7 && rank >= 0 && rank <= 7);
	}

	
		
	private void addMoves(Move curMoves[][], int offset,int index, int pieceType) {
		int rayCounter=0;

		for (int i = 0; i < curMoves.length; i++) {// MoveRays
			if (curMoves[i][0] != null) {
				rayCounter++;
			}
		}
		
		Move[][] finalMoves=new Move[rayCounter][];
		int validRayCursor =0;
		ArrayList <Move>list = new ArrayList<Move> ();
		
		
		
		for (int i = 0; i < curMoves.length; i++) {// MoveRays
			if(curMoves[i][0]==null) {
				continue;
			}
			int moveCounter =0;
			for (int j = 0; j < curMoves[i].length; j++) {// MoveRays
				if (curMoves[i][j] != null) {
					moveCounter++;
				}else {
					break;
				}
			}
			if(moveCounter==0) {
				System.out.println("We need to talk!");
			}
			Move[] moves =new Move[moveCounter];
			for(int j=0;j<moves.length;j++) {
				FieldCallback cb = new FieldCallback(this.position.fields[curMoves[i][j].getOldPos()],curMoves[i][j],validRayCursor,j);
				moves[j] = new Move(curMoves[i][j],cb,validRayCursor,j,list.size());
				list.add(moves[j]);
			}
			finalMoves[validRayCursor++]=moves;
		}
		Move[] allElements = new Move[list.size()];
		list.toArray(allElements);		
		this.pseudoMoveSets[offset+index] = new BLIndexedListBB<Move>(bl,allElements,position.depth,pieceType);
		MoveManager.moves[offset+index]=finalMoves;
	}

	private void generateTrackBack() {
		for(int i=0;i<64;i++) {
			int x1 = Move.getFile(i);
			int y1 = Move.getRank(i);
			String out="";
			for(int j=0;j<64;j++) {
				int x2= Move.getFile(j);
				int y2 = Move.getRank(j);
				int dX = x2-x1;
				int dY = y2-y1;
				int dirX = (int)Math.signum(dX);
				int dirY = (int)Math.signum(dY);
				
				int xx = x2-dirX;
				int yy = y2-dirY;
				
				if(Math.abs(dX)==Math.abs(dY)||dX==0||dY==0) {
					trackBack[i][j]= Move.getPos(yy, xx);
				}else {
					trackBack[i][j]=j;
				}			
			}
		}	
	}
	

}

