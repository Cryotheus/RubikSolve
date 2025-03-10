import java.util.Scanner;

// original intent was to make an algorithm that would search for the solution
// unfortunately, my job, health, and other classes took way too much time away from that
// but that's okay!
// I should still get 20% of the original grade
// which is better than nothing since I have a functioning solution
//
// I did actually enjoy working on this
// when I have time to spare and don't need to work out of debt,
// I'll finish the intended solution - I swear!
//
// using this language makes me appreciate Rust way more

// # Implementation Details
//
// Face-centric design
//      which does have the center tile (although, it could be omitted :p)
//
// Face-rotation was easy of course:
//      it's done with an array of the indices to swap
//
// Rotation of adjacent faces was a bit more complicated:
//      I made a system where I provide the start and end corner
//      and I can get strips of a face
//      this let me "copy" and "paste" strips
//      with different start and end corners
//      I did for some reason use bit-packing for the strip start and ends
//      I don't know why - likely a 3am decision.
//      but it works and saved me from making a giant 48 case switch statement
//
//      if there is any error with this
//      it's because I chose the wrong bits in the `FaceRule.MOVEMENTS` array
//
// Movement optimization:
//      I went for two methods: removing runs, and removing countered moves
//
//      run removal works in an interesting way, I recommend you check it out
//      in either `MoveOrder.resolveRuns` or `BigMoveOrder.resolveRuns`
//      it is a retain-like algorithm
//
//      countered moves are thing like: U' U
//      since a move uses a single bit to represent if it is prime,
//      it's really easy to check (just XOR!)


public class Main {
	static Rubik Rubik = new Rubik();
	static Scanner scanner = new Scanner(System.in);

	public static void main(String[] args) {
		//setup - auto-gen the prime versions of movements
		//this should be at CT not RT, but IDC because it's Java!
		//and all my effort should go to C or Rust, not Java
		//I don't even really like coffee - unless it's mostly sugar and milk
		FaceRule.createInverseRules();

		if (args.length != 0) {
			//clap for automation
			cmdArgs(args);
		} else {
			//interactive mode via StdIn
			stdinCommands();
		}
	}

	static void cmdArgs(String[] args) {
		BigMoveOrder xMoveOrder = new BigMoveOrder();

		for (String arg : args) {
			try {
				int move = MoveOrder.stringToMove(arg);

				xMoveOrder.push((byte) move);
				Rubik.applyMove(move);
			} catch (Exception e) {
				//your fault!
				throw new RuntimeException(e);
			}
		}

		xMoveOrder.resolve(); //make the move list simpler
		xMoveOrder.oppose(); //make it the opposing moves (the moves that undo what was given)
		System.out.print(Rubik.diplaySimple()); //show the cube in the automation-compatible format
		System.out.println(xMoveOrder);
	}

	static void findSolution() {
		System.out.println("NYI");
	}

	static void stdinCommands() {
		stdinHelp();
		while (true) {
			System.out.println(Rubik);

			if (Rubik.solved()) {
				System.out.println("This cube is currently solved!");
			}

			switch (scanner.nextLine().toLowerCase()) {
				case "h":
				case "help":
				case "?":
					stdinHelp();
					break;

				case "m":
				case "move":
					stdinMove();
					break;

				case "r":
				case "randomize":
				case "shuffle":
					Rubik.shuffle();
					break;

				case "s":
				case "solve":
					findSolution();
					break;

				case "t":
				case "test":
				case "that wonderful feeling οf physical and mental relaxation":
					tests();
					return;

				default:
					System.out.println("Unknown command.");
					break;
			}
		}
	}

	static void stdinHelp() {
		System.out.println("Commands:\n- help\n- move\n- randomize\n- solve\n- test\n");
		System.out.println("Most commands have a single letter alternative (e.g. move -> m)");
	}

	static void stdinMove() {

		while (true) {
			System.out.println("Submit U L F R B D, use ' to invert the move.\nX to stop applying movements.");

			int moveEnum;
			String line = scanner.nextLine().trim();

			switch (line.toLowerCase()) {
				case "x":
				case "exit":
				case "c":
				case "cancel":
				case "q":
				case "quit":
					return;
			}

			try {
				moveEnum = MoveOrder.stringToMove(line);
			} catch (Exception e) {
				System.out.println("Invalid move.");

				continue;
			}

			//do the move!
			Rubik.applyMove(moveEnum);
			System.out.println(Rubik);
		}

	}

	static void tests() {
		RadixPacked something = new RadixPacked();

		//just some tests
		something.fill(6, 3, 5);
		test(something.Packed, 5 * 36 + 5 * 6 + 5 * 1, "fill");

		something.setDigit(6, 1, 2);
		test(something.Packed, 5 * 36 + 2 * 6 + 5 * 1, "set");

		something.zeroLittle(6, 2);
		test(something.Packed, 5 * 36 + 0 * 6 + 0 * 1, "truncate little");
	}

	static void test(int a, int b, String label) {
		if (a == b) {
			System.out.print("    Passed \"");
		} else {
			System.out.print("!!! Failed \"");
		}

		System.out.print(label);
		System.out.println('"');
	}
}
