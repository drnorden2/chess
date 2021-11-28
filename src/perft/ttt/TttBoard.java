package perft.ttt;

import java.util.ArrayList;
import java.util.List;


import perft.Board;


public class TttBoard extends Board{
	private int[][] field = new int [3][3];
	private List<List<int[]>> moves = new ArrayList<List<int[]>>();
	private static int[] COLOR_LOOKUP = new int[]{-1,1};

	public TttBoard () {
		
	}
	
	public TttBoard (String FEN) {
		// do nothing
	}
	@Override
	public int getMoves() {
		if(this.moves.size()==getMovesPlayed()){	
			ArrayList<int[]> newMoves =   new ArrayList<int[]>();
			for(int i=0;i<field.length;i++) {
				for(int j=0;j<field[i].length;j++) {
					if(field[i][j]==0) {
						newMoves .add(new int[] {i,j});
					}
				}
			}
			moves.add(newMoves);
		}
		return moves.get(moves.size()-1).size();
	}

	@Override
	public boolean isWon() {	
		//vertical
		for(int i=0;i<field.length;i++) {
			if(field[i][0]!=0 && field[i][0]==field[i][1] && field[i][1]==field[i][2] ){
				return true;
			}
			if(field[0][i]!=0 && field[0][i]==field[1][i] && field[1][i]==field[2][i] ){
				return true;
			}	
		}
		if(field[0][0]!=0 && field[0][0]==field[1][1] && field[1][1]==field[2][2] ){
			return true;
		}
		if(field[0][2]!=0 && field[0][2]==field[1][1] && field[1][1]==field[2][0] ){
			return true;
		}
		return false;
	}

	public String toString() {
		String boardStr ="";
		for(int i=0;i<field.length;i++) {
			for(int j=0;j<field[i].length;j++) {
				String stone =" ";
				if (field[i][j]==-1) {
					stone ="x";
				}else if (field[i][j]==1) {
					stone ="o";
				}
				
				boardStr += "["+stone+"]";
			}
			boardStr +="\n";
		}
		return boardStr;
	}

	public void setStoneXY(int x, int y, int stone) {
		field[x][y]=stone;		
	}

	public int getStoneXY(int x, int y) {
		// TODO Auto-generated method stub
		return field[x][y];
	}
	@Override
	public void setMove(int move) {
		setStoneXY(moves.get(moves.size()-1).get(move)[0],moves.get(moves.size()-1).get(move)[1], (this.getTurn()==0)?-1:1);
	}
	@Override
	public void unSetMove(int move) {
		
		while(moves.size()!=getMovesPlayed()) {
			moves.remove(moves.size()-1);
		}
		List<int[]> newMoves = moves.get(moves.size()-1);
		setStoneXY(newMoves.get(move)[0],newMoves.get(move)[1] ,0);
	}
	@Override
	public String getMoveStr(int i) {
		return ""+i;
	}
	@Override
	public int getHash() {
		return this.toString().hashCode();
	}
	
}
