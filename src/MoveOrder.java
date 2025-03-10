// !!! WARNING !!!
// This can only hold 16 moves!
//
// This is a FILO stack of 16x 4-bit integers bit-packed into a long.
public class MoveOrder {
	public static final int MV_UP = 0b0_000;
	public static final int MV_LEFT = 0b0_001;
	public static final int MV_FRONT = 0b0_010;
	public static final int MV_RIGHT = 0b0_011;
	public static final int MV_BACK = 0b0_100;
	public static final int MV_DOWN = 0b0_101;

	//have their 8s bit on
	public static final int MV_UP_PRIME = 0b1_000;
	public static final int MV_LEFT_PRIME = 0b1_001;
	public static final int MV_FRONT_PRIME = 0b1_010;
	public static final int MV_RIGHT_PRIME = 0b1_011;
	public static final int MV_BACK_PRIME = 0b1_100;
	public static final int MV_DOWN_PRIME = 0b1_101;

	//mask to XOR with MovesPacked for inversion
	//just the same nibble `1000` repeated to fill a long
	static final long MASK_INVERT = 0b1000_1000_1000_1000_1000_1000_1000_1000_1000_1000_1000_1000_1000_1000_1000_1000L;

	//bit-packed, 4 bits per move
	public long MovesPacked;
	public int MoveCount;

	public MoveOrder() {
		MoveCount = 0;
		MovesPacked = 0;
	}

	//returns the face that the move manipulates
	public static int moveFace(int move) {
		return move & 0b0_111;
	}

	public static boolean moveIsPrime(int move) {
		return (move & 0b1_000) == 0b1_000;
	}

	//inverts all moves in the array
	public void invert() {
		MovesPacked ^= MASK_INVERT;
	}

	//removes redundant moves
	public void resolve() {
		if (MoveCount >= 3) {
			int pruned;

			//if we prune moves, we should try to do so again!
			do {
				pruned = resolveRuns();
				pruned += resolveCounterMoves();
			} while (pruned != 0);
		} else if (MoveCount == 2) {
			resolveCounterMoves(); //needs at least 2 moves to have an effect
		}
	}

	//removes pairs of moves that undo each other
	private int resolveCounterMoves() {
		int startCount = MoveCount;

		loop:
		while (MoveCount > 1) {
			int foundAt = -1;

			long currentShift;
			long currentMask;
			long currentMoveMasked;
			long currentMove;

			long previousShift;
			long previousMask;
			long previousMoveMasked;
			long previousMove;

			find:
			for (int i = 1; i < MoveCount; i++) {
				currentShift = ((long) i) * 4L;
				currentMask = 0b1111L << currentShift;
				currentMoveMasked = MovesPacked & currentMask;
				currentMove = currentMoveMasked >> currentMask;

				previousShift = (((long) i) - 1L) * 4L;
				previousMask = 0b1111L << previousShift;
				previousMoveMasked = MovesPacked & previousMask;
				previousMove = previousMoveMasked >> previousMask;

				//if the moves counter
				if ((currentMove ^ previousMove) == 0b1000L) {
					foundAt = i - 1;

					break find;
				}
			}

			if (foundAt == -1) {
				//if no counter moves were found, we're done
				break loop;
			} else if (foundAt == 0) {
				//if it's at the start, the removal is trivial
				MovesPacked = MovesPacked >> 8L;
			} else {
				long shift = foundAt * 4L;
				long divisor = 1L << shift;
				long little = MovesPacked % divisor;
				long big = MovesPacked >> (shift + 8); //do `+ 8` to remove our two target 2 nibbles
				long bigBitsMask = -divisor; //SAME AS: ~(divisor - 1L)

				MovesPacked = (big & bigBitsMask) | little;
			}

			//because we just deleted 2
			//SAFETY: we know there is at least 2 items, otherwise the for loop doesn't run!
			MoveCount -= 2;
		}

		return startCount - MoveCount;
	}

	//shrinks n-long runs of moves down to 2 or less consecutive moves
	private int resolveRuns() {
		if (MoveCount <= 2)
			return 0;

		long newPacked = 0L;
		long currentMove = MovesPacked & 0b1111L;
		int currentShift = 0;
		int runLen = 0;
		int startCount = MoveCount;

		//find runs
		//we already have the first move in the vars above
		//that's why we start at 1 instead of 0
		for (int i = 1; i < MoveCount; i++) {
			runLen++;

			int shift = i * 4;
			long mask = 0b1111L << shift;
			long maskedMove = MovesPacked & mask;
			long move = maskedMove >> shift;

			if (move != currentMove) {
				//all this boilerplate to call a function
				//just because Java doesn't support mutable references
				int[] shiftMutRef = {currentShift};
				newPacked = resolveRunsInternal(newPacked, shiftMutRef, runLen, currentMove);
				currentShift = shiftMutRef[0];

				//start a new run
				currentMove = move;

				//runLen will be 1 on the start of our math and checks
				//if this is the end of the for loop,
				//we will be able to tell since this will stay 0
				//which means we don't have a run remaining
				runLen = 0;
			}
		}

		if (runLen == 0) {
			//the last run has been handled
			//we don't need to try packing the move
			MovesPacked = newPacked;
			MoveCount = currentShift / 4;
		} else {
			int[] shiftMutRef = {currentShift};
			MovesPacked = resolveRunsInternal(newPacked, shiftMutRef, runLen, currentMove);
			MoveCount = shiftMutRef[0] / 4;
		}

		return startCount - MoveCount;
	}

	//used internally by resolveRuns
	private static long resolveRunsInternal(long packed, int[] shiftMutRef, int runLen, long move) {
		int currentShift = shiftMutRef[0];

		switch (runLen % 4) {
			case 0: //0, 4, 8, ...
				//do nothing, nothing changes on a Rubik's Cube
				//when you repeat a move 4 times
				break;
			case 1: //1, 5, 9, ...
				//1 unique move is exactly what to look for
				packed |= move << currentShift;
				shiftMutRef[0] = currentShift + 4;
				break;
			case 2: //2, 6, 8, ...
				//standardize not using prime movements for 180s
				//by removing the prime bit if it is there
				move &= 0b0_111;
				packed |= (move | (move << 4)) << currentShift;
				shiftMutRef[0] = currentShift + 8;
				break;
			case 3: //3, 7, 11, ...
				//"why turn left 3 times when we can turn right once?"
				//invert the move and insert it once
				packed |= (move ^ 0b1_000) << currentShift;
				shiftMutRef[0] = currentShift + 4;
				break;
		}

		return packed;
	}

	public void reverse() {
		int count = MoveCount;
		int halfCount = Math.floorDiv(count, 2);

		//I'm sure this can be optimized,
		//but it's 2 am and I have an exam tomorrow
		for (int i = 0; i < halfCount; i++) {
			int revI = count - i - 1;

			int smallShift = i * 4;
			long smallMasked = MovesPacked & (0b1111L << smallShift);

			int bigShift = revI * 4;
			long bigMasked = MovesPacked & (0b1111L << bigShift);

			//zero-out positions where swap is happening
			MovesPacked ^= smallMasked | bigMasked;

			//both the nibbles are shifting the same amount
			//just in different directions
			int shiftDif = bigShift - smallShift;

			//place the nibbles
			MovesPacked |= (smallMasked << shiftDif) | (bigMasked >> shiftDif);
		}
	}

	public static int stringToMove(String moveStrRepr) throws Exception {
		return switch (moveStrRepr.toUpperCase()) {
			case "U" -> MV_UP;
			case "L" -> MV_LEFT;
			case "F" -> MV_FRONT;
			case "R" -> MV_RIGHT;
			case "B" -> MV_BACK;
			case "D" -> MV_DOWN;

			case "U'" -> MV_UP_PRIME;
			case "L'" -> MV_LEFT_PRIME;
			case "F'" -> MV_FRONT_PRIME;
			case "R'" -> MV_RIGHT_PRIME;
			case "B'" -> MV_BACK_PRIME;
			case "D'" -> MV_DOWN_PRIME;

			default -> throw new Exception("invalid movement string repr");
		};
	}

	//reverses and inverts the move order
	public void oppose() {
		reverse();
		invert();
	}

	public int pop() {
		if (MoveCount <= 0) {
			throw new RuntimeException("MoveOrder popped when empty!");
		}

		MoveCount--;
		long masked = MovesPacked & (0b1111L << (MoveCount * 4)); //get the move in-position
		MovesPacked ^= masked; //remove the move from the bit-packed data

		return (int) (masked >> MoveCount); //align to right so it matches the enums!
	}

	public void push(int move) {
		if (MoveCount >= 16) {
			throw new RuntimeException("MoveOrder max capacity (16) reached!");
		}

		MovesPacked |= ((long) move) << (MoveCount * 4);
		MoveCount++;
	}

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder(MoveCount * 3);

		for (int i = 0; i < MoveCount; i++) {
			int shift = i * 4;
			long move = (MovesPacked & (0b1111L << shift)) >> shift;

			builder.append(switch ((int) move) {
				case MV_UP -> "U";
				case MV_LEFT -> "L";
				case MV_FRONT -> "F";
				case MV_RIGHT -> "R";
				case MV_BACK -> "B";
				case MV_DOWN -> "D";
				case MV_UP_PRIME -> "U'";
				case MV_LEFT_PRIME -> "L'";
				case MV_FRONT_PRIME -> "F'";
				case MV_RIGHT_PRIME -> "R'";
				case MV_BACK_PRIME -> "B'";
				case MV_DOWN_PRIME -> "D'";
				default -> throw new RuntimeException("invalid mv repr");
			});

			builder.append(' ');
		}

		//pop last (it's a space)
		builder.deleteCharAt(builder.length() - 1);

		return builder.toString();
	}
}
