import java.util.Random;

public class Rubik {
	//I understand we are supposed to use 2D arrays
	//if you consider `ArrayList<Integer>[]` or `ArrayList<ArrayList<Integer>>`
	//to be 2D arrays
	//then `Face[]` should be too!
	//a `Face` uses a `RadixPacked` as a more memory-efficient alternative to an int array
	public Face[] Faces;

	public Rubik() {
		Faces = new Face[6];

		for (int i = 0; i < 6; i++) {
			Faces[i] = new Face(i);
		}
	}

	public void applyMove(int move) {
		int faceIndex = MoveOrder.moveFace(move);
		Face face = Faces[faceIndex];
		FaceRule moveRule = FaceRule.MOVEMENTS[move];
		int[] adjFaceOrder = moveRule.AdjacentFaceOrder;
		int[] adjFaceStrips = moveRule.AdjacentFaceStrips;

		int lastIndex = adjFaceStrips.length - 1;

		//we use the previous face iterated
		//the tile strip is strored before the face is modified
		int[] previousStrip = Faces[adjFaceOrder[lastIndex]].getStrip(adjFaceStrips[lastIndex]);

		//the face itself is simply rotated
		face.rotate(moveRule.Clockwise);

		//all the adjacent faces have to have specific strips moved around
		//cloumns and rows need to be moved around
		for (int i = 0; i < adjFaceOrder.length; i++) {
			int adjFaceIndex = adjFaceOrder[i];

			//save everything we need for the "previous" variables now
			Face adjFace = Faces[adjFaceIndex];
			int adjStripCorners = adjFaceStrips[i];
			int[] currentStrip = adjFace.getStrip(adjStripCorners);

			//make the change
			adjFace.setStrip(adjStripCorners, previousStrip);

			//then set those variables
			previousStrip = currentStrip;
		}
	}

	public String diplaySimple() {
		StringBuilder builder = new StringBuilder(13 * 6);

		for (int i = 0; i < 6; i++) {
			int mappedIndex = Tile.SIMPLE_DISPLAY_ORDER[i];

			builder.append(Faces[mappedIndex].displaySimple());
			builder.append('\n');
		}

		return builder.toString();
	}

	public void shuffle() {
		Random rand = new Random();

		for (int i = 0; i < 30; i++) {
			int move =  Math.abs(rand.nextInt()) % 12;

			if (move > 5) {
				//a single bit is dedicated to usage as a bitflag
				//we do this to ensure prime moves are properly  setup with that bit flag
				//moves [0, 5] are valid normal moves,
				//moves [8, 13] are valid prime moves
				//see MoveOrder for binary representations of the move integers
				move += 2;
			}

			applyMove(move);
		}
	}

	public boolean solved() {
		for (int i = 0; i < 6; i++) {
			if (!Faces[i].solved())
				return false;
		}

		return true;
	}

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();

		builder.append("           0\n");

		//first face (at the top/up position)
		for (int rowI = 0; rowI < 3; rowI++) {
			builder.append("        ");
			builder.append(Faces[0].displayRow(rowI));
			builder.append('\n');
		}

		//left, front, right, and back faces
		for (int rowI = 0; rowI < 3; rowI++) {
			builder.append(' ');

			if (rowI == 1)
				builder.append('1');
			else
				builder.append(' ');

			for (int faceI = 1; faceI < 5; faceI++) {
				builder.append(Faces[faceI].displayRow(rowI));
			}

			if (rowI == 1)
				builder.append(" 4\n");
			else
				builder.append('\n');
		}

		//last face (at the bottom/down position)
		for (int rowI = 0; rowI < 3; rowI++) {
			builder.append("        ");
			builder.append(Faces[5].displayRow(rowI));
			builder.append('\n');
		}

		builder.append("           5\n");

		return builder.toString();
	}
}
