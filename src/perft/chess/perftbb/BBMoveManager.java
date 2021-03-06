package perft.chess.perftbb;
import static perft.chess.Definitions.*;

public class BBMoveManager {


		public static final Move [][] moves= new Move[1280][64];
		//private static final Move [][] pseudoMoveSets = new Move[1280][];
		public static final long [] moveMasks = new long[1280];

		public BBMoveManager() {
			setup();
		}
		
		private void setup() {
			for (int color = 0; color < 2; color++) {
				for (int pieceType = 0; pieceType < 7; pieceType++) {
					int offset = 64 * (pieceType * 2 + color);
						
					for (int rank = 0; rank < 8; rank++) {
						for (int file = 0; file < 8; file++) {
							Move[][] curMoves = new Move[18][7];
							switch (pieceType) {
							
							case PIECE_TYPE_PAWN:
							//	O.UT("PAWN"+color);
								int colorSwitch = color==0?-1:1;
								if((rank !=_1 && color ==COLOR_WHITE )||(rank != _8 && color ==COLOR_BLACK )) {
									
									//moves only starting from 2nd row
									{
										int moveType = MOVE_TYPE_PAWN_BEAT;
										int callbackType = CALLBACK_TYPE_BEAT_ONE_AS_PAWN;
										
										//special case enpassante indicated by Movetype
										if((rank ==_5 && color ==COLOR_WHITE )||(rank == _4 && color ==COLOR_BLACK)) {
											moveType = MOVE_TYPE_PAWN_BEAT_OR_ENPASSANTE;
											callbackType = CALLBACK_TYPE_OTHER;
										}
										generateMoves(pieceType,curMoves, color,new int[][]{{-1,colorSwitch},{1,colorSwitch}}, 0, file, rank,1, moveType,callbackType);
									}
									int steps =1;
									//2 steps if 2nd row
									if((rank ==_2 && color ==COLOR_WHITE )||(rank == _7 && color ==COLOR_BLACK )) {
										steps =2;
									}
									
									generateMoves(pieceType,curMoves, color,new int[][]{{0,colorSwitch}}, 2, file, rank,steps,MOVE_TYPE_PAWN_PUSH,CALLBACK_TYPE_PUSH_RAY);
									//if right before finish line
									if((rank ==_2 && color ==COLOR_BLACK)||(rank == _7 && color ==COLOR_WHITE )) {
										for(int i=0;i<3;i++) {
											int moveType = -1;
											int callbackType = -1;
										
											if(i==2) {//Push
												moveType=MOVE_TYPE_PAWN_PUSH_CONVERT;
												callbackType= CALLBACK_TYPE_PUSH_ONE;
											}else {//Beat
												moveType=MOVE_TYPE_PAWN_BEAT_CONVERT;											
												callbackType= CALLBACK_TYPE_BEAT_ONE_AS_PAWN;
											}
											if(curMoves[i][0]!=null) { // skip the sidewise beating 
												Move oldMove = curMoves[i][0];
												curMoves[i][0] =null; //deleted for potential promotions
												//4 options rook, bishop, knight and queen 
												int piece = PIECE_TYPE_KNIGHT;
												for(int j=3;j>=0;j--) {
													int promotePieceType = piece++; //knight,bishop,  rook, queen
													curMoves[3+(4*i)+j] = new Move[] {new Move(pieceType,(pieceType * 2 + color) << 6,color,callbackType ,oldMove.getOldPos(),oldMove.getNewPos(), moveType,0,0,promotePieceType)}; 
												}
											}
										}
									}										
								}
								//O.UT("Index: " + getIndex(file, rank) + " file: "+file+" Rank: "+rank+" color:"+color);
								break;
							case PIECE_TYPE_BISHOP:
								//O.UT("BISHOP "+color);
								generateMoves(pieceType,curMoves,color, new int[][]{{-1,-1},{-1,1},{1,-1},{1,1}}, 0, file, rank,7, MOVE_TYPE_PUSH_BEAT,CALLBACK_TYPE_BEAT_RAY);
								break;
							case PIECE_TYPE_KNIGHT:
								//O.UT("KNIGHT "+color);
								generateMoves(pieceType,curMoves, color,new int[][]{{2,1},{2,-1},{-2,1},{-2,-1},{1,2},{1,-2},{-1,2},{-1,-2}}, 0, file, rank,1, MOVE_TYPE_PUSH_BEAT,CALLBACK_TYPE_BEAT_ONE);
								break;
							
							case PIECE_TYPE_ROOK:
								//O.UT("ROOK_T "+color);
								generateMoves(pieceType,curMoves, color,new int[][]{{1,0},{-1,0},{0,-1},{0,1}}, 0, file, rank,7, MOVE_TYPE_PUSH_BEAT,CALLBACK_TYPE_BEAT_RAY);
								break;
								
							
							case PIECE_TYPE_KING:
								//O.UT("KING_T "+color);
								//O.UT("KING_T "+color+"file "+file +" Rank:"+rank+" Offset:"+offset);
								//generate the sensing moves to detect pinning

								//generateMoves(curMoves, color,new int[][]{{1,0},{-1,0},{-1,-1},{-1,1},{1,-1},{1,1},{0,-1},{0,1}}, 0, file, rank,7, MOVE_TYPE_KING_SENSING,CALLBACK_TYPE_KING_SENSING);
								generateMoves(pieceType,curMoves, color,new int[][]{{1,0},{-1,0},{-1,-1},{-1,1},{1,-1},{1,1},{0,-1},{0,1}}, 0, file, rank,1, MOVE_TYPE_PUSH_BEAT,CALLBACK_TYPE_BEAT_ONE_AS_KING);
								//generateMoves(curMoves, color,new int[][]{{2,1},{2,-1},{-2,1},{-2,-1},{1,2},{1,-2},{-1,2},{-1,-2}}, 8, file, rank,1, MOVE_TYPE_KING_SENSING,CALLBACK_TYPE_CHECK_KNIGHT_ATTACK);
								
								// add the rochade as ray moves to 1 and 2 options on the E1/E8 position
								if((rank ==_1 && file ==_E  && color ==COLOR_WHITE)||(rank == _8 && file ==_E && color ==COLOR_BLACK )) {
									for(int i=0;i<2;i++) {
										int dir = (i==0)?1:-1;
										int oldPos = getPosForFileRank(file,rank);
										int newPos = getPosForFileRank(file+2*dir,rank);
										int rookPos = getPosForFileRank(file+((i==0)?3:-4),rank);
										curMoves[i][1] = new Move(pieceType,((pieceType <<1) + color) << 6,color,CALLBACK_TYPE_ROCHADE_TEST , oldPos,newPos, MOVE_TYPE_ROCHADE, 0,0,-1,rookPos, dir);
									}
								}
								break;
							case PIECE_TYPE_QUEEN:
								//O.UT("QUEEN "+color+"file "+file +" Rank:"+rank+" Offset:"+offset);
								generateMoves(pieceType,curMoves, color,new int[][]{{-1,0},{1,0},{-1,-1},{-1,1},{1,-1},{1,1},{0,-1},{0,1}}, 0, file, rank,7, MOVE_TYPE_PUSH_BEAT,CALLBACK_TYPE_BEAT_RAY);
								break;
							
							case PIECE_TYPE_ANY:
								//public Move(BaseLiner bl,int callbackType, int oldPos, int newPos, int moveType) {
								curMoves[0] = new Move[] {new Move(pieceType,(pieceType * 2 + color) << 6,color,CALLBACK_TYPE_OTHER , getPosForFileRank(file,rank),getPosForFileRank(file,rank), MOVE_TYPE_INITAL_PLACEMENT,0,0)}; 
								break;						
							}
							this.addMoves(curMoves, offset, getPosForFileRank(file,rank),pieceType);
							this.addMoveMask(curMoves, pieceType,offset, getPosForFileRank(file,rank));
						}
					}
				}
			}
		}
		
		public Move[] getRawMoves (int index) {
			return this.moves[index];
		}

		
		private void generateMoves(int pieceType,Move[][] curMoves, int color, int[][]  dirs, int rayOffset,int file, int rank,int maxSteps,int moveType, int callbackType) {
			for (int ray = 0; ray < dirs.length; ray++) {
				int dirX = dirs[ray][0];
				int dirY = dirs[ray][1];
				int oldPos = getPosForFileRank(file,rank);
				int cursor = 0;
				
				
				for (int i = 0; i < maxSteps; i++) {
					boolean isInBounds = isInBounds(file + dirX * (i + 1), rank + dirY * (i + 1));
					/*
					if(moveType == MOVE_TYPE_KING_SENSING & !isInBounds(file + dirX * (i + 2), rank + dirY * (i + 2))) {
						break;
					}*/
					if (isInBounds) {
						int newPos = getPosForFileRank(file + dirX * (i + 1),rank + dirY * (i + 1));
						int cb = callbackType;
						
						// last in a row works link one that cannot mask out others
						// perft(6) w. feature: Opt:4196382 vs Opt:4000980
						boolean last = (i+1==maxSteps ||!isInBounds(file + dirX * (i + 2), rank + dirY * (i + 2)));
						if(cb == CALLBACK_TYPE_BEAT_RAY && last) {
							cb = CALLBACK_TYPE_BEAT_ONE;
						}
						Move curMove = new Move(pieceType,((pieceType << 1) + color) << 6,color,cb ,oldPos, newPos, moveType,dirX,dirY); ;				
						curMoves[ray+rayOffset][cursor++] = curMove;
					} else {
						break;
					}
				}
			}
		}

		
		
		
		private static boolean isInBounds(int file, int rank) {
			return (file >= 0 && file <= 7 && rank >= 0 && rank <= 7);
		}

		private void addMoveMask(Move curMoves[][], int type, int offset,int index) {
			long mask = 0L;
			
			for (int i = 0; i < curMoves.length; i++) {// MoveRays
				for(int j=0;j<curMoves[i].length;j++) {
					Move oldMove = curMoves[i][j];
					if(oldMove!=null) {
						if(!oldMove.isKingSensing() && !oldMove.isRochade()) {
							int newPos = oldMove.getNewPos();
							mask |= 1L << newPos;
						}
					}
				}
			}
			this.moveMasks[offset+index]=mask;
		}
		
		
		private void addMoves(Move curMoves[][], int offset,int index, int pieceType) {
			
			
			Move[] moveMap = new Move[64];
			
			
			for (int i = 0; i < curMoves.length; i++) {// MoveRays
				if(curMoves[i][0]==null) {
					continue;
				}
				int moveCounter=0;
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
					Move move = curMoves[i][j];
					int curPos = move.getNewPos();
					int file = getFileForPos(curPos);
					int rank =getRankForPos(curPos);
					while(moveMap[curPos]!=null) {
						rank=(rank-PAWN_MOVE_DIR[move.getColor()]);
						curPos = getPosForFileRank(file,rank);
					}
					moveMap[curPos] = move;
					
				}
			}
			
			this.moves[offset+index] = moveMap;
		}
	}

