package perft.chess;

import perft.Board;
import perft.BoardUI;

public class ChessBoardUI implements BoardUI {
	public void show(Board board) {
		System.out.println(board);
	}
	
}