import java.util.ArrayList;

// !!! WARNING !!!
// this has way fewer comments and features, because it was made
// to accommodate more than 16 moves, which was not the initial plan
// but necessary as I didn't have time to make an algorithm to actually simulate and find solutions
//
// MoveOrder still exists, but isn't used
public class BigMoveOrder {
	public ArrayList<Byte> Moves = new ArrayList<>();

	public void invert() {
		for (int i = 0; i < Moves.size(); i++) {
			byte move = Moves.get(i);
			move ^= 0b1_000; //invert bit that represents the move being prime

			Moves.set(i, move);
		}
	}

	public void oppose() {
		reverse();
		invert();
	}

	public void push(byte move) {
		Moves.add(move);
	}

	// same as `MoveOrder.resolve`
	public void resolve() {
		int count = Moves.size();

		if (count >= 3) {
			int pruned;

			//if we prune moves, we should try to do so again!
			do {
				pruned = resolveRuns();
				pruned += resolveCounterMoves();
			} while (pruned != 0);
		} else if (count == 2) {
			resolveCounterMoves(); //needs at least 2 moves to have an effect
		}
	}

	// returns how many moves were pruned
	public int resolveCounterMoves() {
		int startSize = Moves.size();

		//loop until we didn't find counter moves
		while (Moves.size() > 1) {
			byte previousMove = Moves.getFirst();
			int foundAt = -1; //sentinel value since 0 is valid

			for (int i = 1; i < Moves.size(); i++) {
				byte move = Moves.get(i);

				//because `previousMove ^ 0b1_000` is not possible with byte primitives apparently
				//thanks, java
				previousMove ^= 0b1_000;

				if (previousMove == move) {
					foundAt = i - 1;

					break;
				}

				previousMove = move;
			}

			//sentinel of -1 means no counter moves were found
			if (foundAt == -1) {
				break;
			}

			//remove the pair of countering moves
			Moves.remove(foundAt);
			Moves.remove(foundAt);
		}

		return startSize - Moves.size();
	}

	// same as `MoveOrder.resolveRuns` but simpler
	// code comments available there
	// returns how many moves were pruned
	public int resolveRuns() {
		if (Moves.size() <= 2)
			return 0;

		boolean changed = false;
		byte currentMove = Moves.getFirst();
		int runLen = 0;
		ArrayList<Byte> newMoves = new ArrayList<>();

		for (int i = 1; i < Moves.size(); i++) {
			runLen++;
			byte move = Moves.get(i);

			if (move != currentMove) {
				resolveRunsInternal(newMoves, runLen, currentMove);

				changed = true;
				currentMove = move;
				runLen = 0;
			} else {
				changed = false;
			}
		}

		if (changed)
			resolveRunsInternal(newMoves, 1, currentMove);
		else if (runLen != 0)
			resolveRunsInternal(newMoves, runLen, currentMove);

		int diff = Moves.size() - newMoves.size();

		Moves = newMoves;

		return diff;
	}

	// same as `MoveOrder.resolveRunsInternal` but simpler
	// code comments available there
	private static void resolveRunsInternal(ArrayList<Byte> newMoves, int runLen, byte move) {
		switch (runLen % 4) {
			case 0:
				break;
			case 1:
				newMoves.add(move);
				break;
			case 2:
				move &= 0b0_111;
				newMoves.add(move);
				newMoves.add(move);
				break;
			case 3:
				//because `move ^ 0b1_000` is not possible with byte primitives apparently
				//thanks, java
				move ^= 0b1_000;
				newMoves.add(move);
				break;
		}
	}

	public void reverse() {
		int count = Moves.size();
		int halfCount = Math.floorDiv(count, 2);

		for (int i = 0; i < halfCount; i++) {
			int revI = count - i - 1;

			//normally I'd xor swap in I-langs so that I don't need a temporary variable,
			//but I heard javac is smart with this and does it for me
			byte temp = Moves.get(i);

			Moves.set(i, Moves.get(revI));
			Moves.set(revI, temp);
		}
	}

	public MoveOrder shrink() throws Exception {
		if (Moves.size() > 16)
			throw new Exception("BigMoveOrder.shrink cannot create a MoveOrder with more than 16 moves, due to memory limitations");

		MoveOrder moves = new MoveOrder();

		for (Byte move : Moves) {
			moves.push((int) move);
		}

		return moves;
	}

	@Override
	public String toString() {
		int count = Moves.size();
		StringBuilder builder = new StringBuilder(count * 3);

		for (Byte move : Moves) {
			builder.append(switch ((int) move) {
				case MoveOrder.MV_UP -> "U";
				case MoveOrder.MV_LEFT -> "L";
				case MoveOrder.MV_FRONT -> "F";
				case MoveOrder.MV_RIGHT -> "R";
				case MoveOrder.MV_BACK -> "B";
				case MoveOrder.MV_DOWN -> "D";
				case MoveOrder.MV_UP_PRIME -> "U'";
				case MoveOrder.MV_LEFT_PRIME -> "L'";
				case MoveOrder.MV_FRONT_PRIME -> "F'";
				case MoveOrder.MV_RIGHT_PRIME -> "R'";
				case MoveOrder.MV_BACK_PRIME -> "B'";
				case MoveOrder.MV_DOWN_PRIME -> "D'";
				default -> throw new RuntimeException("invalid mv repr");
			});

			builder.append(' ');
		}

		//pop last (it's a space)
		builder.deleteCharAt(builder.length() - 1);

		return builder.toString();
	}
}
