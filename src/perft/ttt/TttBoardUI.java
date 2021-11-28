package perft.ttt;

import perft.Board;
import perft.BoardUI;

public class TttBoardUI implements BoardUI {
	public void show(Board board) {
		System.out.println(board);
	}
}
