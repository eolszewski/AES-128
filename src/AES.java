import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.net.URL;
import java.util.ArrayList;
import java.util.BitSet;

public class AES {

	public static void main(String[] args) {
		// TODO Auto-generated method stub

		// String input = "00000000000000000000000000000000";
		// String input = "66ef88cae98a4c344b2cfa2bd43b592e";
		String keyString = "";// =
								// "00, 00, 00, 00, 00, 00, 00, 00, 00, 00, 00, 00, 00, 00, 00, 00";

		// encrypt flag
		boolean encrypt = true;
		boolean error = false;

		if (args[0].equals("e"))
			encrypt = true;
		else if (args[0].equals("d"))
			encrypt = false;
		else
			error = true;

		try {
			URL path = ClassLoader.getSystemResource(args[1]);
			if (path == null) {
				error = true;
			}
			File f = new File(path.toURI());

			BufferedReader br = new BufferedReader(new FileReader(f));
			String line = br.readLine();

			while (line != null) {
				keyString = line;
				line = br.readLine();
			}
			br.close();
		} catch (Exception e) {
			error = true;
		}

		String key = "";
		for (int i = 0; i < keyString.length(); i += 2)
			key += keyString.substring(i, i + 2).toLowerCase() + ", ";
		key = key.substring(0, key.length() - 2);

		// Build RconMatrix
		String[][] RconMatrix = rconMatrix();

		// Build sBoxMatrix
		String sBoxMatrix[][] = sBoxMatrix(encrypt);

		// Build array of round keys
		ArrayList<String[]> roundKeys = makeRoundKeys(sBoxMatrix(encrypt),
				RconMatrix, key);

		if (!error) {
			try {
				URL path = ClassLoader.getSystemResource(args[2]);
				if (path == null) {
					error = true;
				}
				File f = new File(path.toURI());

				BufferedReader br = new BufferedReader(new FileReader(f));

				String line = br.readLine();

				File file;
				if (encrypt)
					file = new File(args[2] + ".enc");
				else
					file = new File(args[2] + ".dec");

				file.createNewFile();

				// true = append file
				FileWriter fileWritter = new FileWriter(file.getName(), true);
				BufferedWriter bufferWritter = new BufferedWriter(fileWritter);

				while (line != null) {
					keyString = line.toLowerCase();
					while (keyString.length() < 31)
						keyString += "0";
					if (keyString.matches("[0-9a-f]+")) {
						// Create a state array
						String state[][] = new String[4][4];
						for (int i = 0; i < 4; i++)
							for (int j = 0; j < 4; j++)
								state[i][j] = keyString.substring(
										8 * i + 2 * j, 8 * i + 2 * j + 2);

						// Rounds
						for (int roundNumber = 0; roundNumber < 11; roundNumber++) {
							if (encrypt)
								state = encrypt(state, roundKeys, roundNumber,
										sBoxMatrix, encrypt);
							else
								state = decrypt(state, roundKeys, roundNumber,
										sBoxMatrix, encrypt);
						}
						String writer = "";
						for (int i = 0; i < 4; i++) {
							for (int j = 0; j < 4; j++)
								writer += state[j][i];
						}
						bufferWritter.write(writer
								+ System.getProperty("line.separator"));
					}
					line = br.readLine();
				}
				br.close();
				bufferWritter.close();
			} catch (Exception e) {
				error = true;
				System.out.println("Invalid file name");
			}
		} else {
			System.out.println("Invalid input");
		}
	}

	private static String[][] rconMatrix() {
		String Rcon = "01, 02, 04, 08, 10, 20, 40, 80, 1b, 36,"
				+ "00, 00, 00, 00, 00, 00, 00, 00, 00, 00,"
				+ "00, 00, 00, 00, 00, 00, 00, 00, 00, 00,"
				+ "00, 00, 00, 00, 00, 00, 00, 00, 00, 00";

		String[] RconArray = Rcon.replace(" ", "").split(",");
		String RconMatrix[][] = new String[4][10];

		for (int row = 0; row < 4; row++)
			for (int col = 0; col < 10; col++)
				RconMatrix[row][col] = RconArray[row * 10 + col];

		return RconMatrix;
	}

	private static String[][] sBoxMatrix(boolean encrypt) {
		String sBox;
		if (encrypt) {
			sBox = "63, 7c, 77, 7b, f2, 6b, 6f, c5, 30, 01, 67, 2b, fe, d7, ab, 76, "
					+ "ca, 82, c9, 7d, fa, 59, 47, f0, ad, d4, a2, af, 9c, a4, 72, c0, "
					+ "b7, fd, 93, 26, 36, 3f, f7, cc, 34, a5, e5, f1, 71, d8, 31, 15, "
					+ "04, c7, 23, c3, 18, 96, 05, 9a, 07, 12, 80, e2, eb, 27, b2, 75, "
					+ "09, 83, 2c, 1a, 1b, 6e, 5a, a0, 52, 3b, d6, b3, 29, e3, 2f, 84, "
					+ "53, d1, 00, ed, 20, fc, b1, 5b, 6a, cb, be, 39, 4a, 4c, 58, cf, "
					+ "d0, ef, aa, fb, 43, 4d, 33, 85, 45, f9, 02, 7f, 50, 3c, 9f, a8, "
					+ "51, a3, 40, 8f, 92, 9d, 38, f5, bc, b6, da, 21, 10, ff, f3, d2, "
					+ "cd, 0c, 13, ec, 5f, 97, 44, 17, c4, a7, 7e, 3d, 64, 5d, 19, 73, "
					+ "60, 81, 4f, dc, 22, 2a, 90, 88, 46, ee, b8, 14, de, 5e, 0b, db, "
					+ "e0, 32, 3a, 0a, 49, 06, 24, 5c, c2, d3, ac, 62, 91, 95, e4, 79, "
					+ "e7, c8, 37, 6d, 8d, d5, 4e, a9, 6c, 56, f4, ea, 65, 7a, ae, 08, "
					+ "ba, 78, 25, 2e, 1c, a6, b4, c6, e8, dd, 74, 1f, 4b, bd, 8b, 8a, "
					+ "70, 3e, b5, 66, 48, 03, f6, 0e, 61, 35, 57, b9, 86, c1, 1d, 9e, "
					+ "e1, f8, 98, 11, 69, d9, 8e, 94, 9b, 1e, 87, e9, ce, 55, 28, df, "
					+ "8c, a1, 89, 0d, bf, e6, 42, 68, 41, 99, 2d, 0f, b0, 54, bb, 16 ";
		} else {
			sBox = "52, 09, 6a, d5, 30, 36, a5, 38, bf, 40, a3, 9e, 81, f3, d7, fb, "
					+ "7c, e3, 39, 82, 9b, 2f, ff, 87, 34, 8e, 43, 44, c4, de, e9, cb, "
					+ "54, 7b, 94, 32, a6, c2, 23, 3d, ee, 4c, 95, 0b, 42, fa, c3, 4e, "
					+ "08, 2e, a1, 66, 28, d9, 24, b2, 76, 5b, a2, 49, 6d, 8b, d1, 25, "
					+ "72, f8, f6, 64, 86, 68, 98, 16, d4, a4, 5c, cc, 5d, 65, b6, 92, "
					+ "6c, 70, 48, 50, fd, ed, b9, da, 5e, 15, 46, 57, a7, 8d, 9d, 84, "
					+ "90, d8, ab, 00, 8c, bc, d3, 0a, f7, e4, 58, 05, b8, b3, 45, 06, "
					+ "d0, 2c, 1e, 8f, ca, 3f, 0f, 02, c1, af, bd, 03, 01, 13, 8a, 6b, "
					+ "3a, 91, 11, 41, 4f, 67, dc, ea, 97, f2, cf, ce, f0, b4, e6, 73, "
					+ "96, ac, 74, 22, e7, ad, 35, 85, e2, f9, 37, e8, 1c, 75, df, 6e, "
					+ "47, f1, 1a, 71, 1d, 29, c5, 89, 6f, b7, 62, 0e, aa, 18, be, 1b, "
					+ "fc, 56, 3e, 4b, c6, d2, 79, 20, 9a, db, c0, fe, 78, cd, 5a, f4, "
					+ "1f, dd, a8, 33, 88, 07, c7, 31, b1, 12, 10, 59, 27, 80, ec, 5f, "
					+ "60, 51, 7f, a9, 19, b5, 4a, 0d, 2d, e5, 7a, 9f, 93, c9, 9c, ef, "
					+ "a0, e0, 3b, 4d, ae, 2a, f5, b0, c8, eb, bb, 3c, 83, 53, 99, 61, "
					+ "17, 2b, 04, 7e, ba, 77, d6, 26, e1, 69, 14, 63, 55, 21, 0c, 7d ";
		}

		String[] sBoxArray = sBox.replace(" ", "").split(",");

		// Forming matrix
		String sBoxMatrix[][] = new String[16][16];

		for (int row = 0; row < 16; row++)
			for (int col = 0; col < 16; col++)
				sBoxMatrix[row][col] = sBoxArray[row * 16 + col];

		return sBoxMatrix;
	}

	private static ArrayList<String[]> makeRoundKeys(String[][] sBox,
			String[][] rcon, String key) {
		ArrayList<String[]> roundKeys = new ArrayList<String[]>();
		roundKeys.add(key.replace(" ", "").split(","));

		for (int i = 0; i < 10; i++) {

			// Shift the rotword
			String nextKey[] = new String[16];
			String originalFirstCol[] = new String[4];
			String lastCol[] = new String[4];
			String rconArr[] = new String[4];

			lastCol[0] = roundKeys.get(i)[7];
			lastCol[1] = roundKeys.get(i)[11];
			lastCol[2] = roundKeys.get(i)[15];
			lastCol[3] = roundKeys.get(i)[3];

			originalFirstCol[0] = roundKeys.get(i)[0];
			originalFirstCol[1] = roundKeys.get(i)[4];
			originalFirstCol[2] = roundKeys.get(i)[8];
			originalFirstCol[3] = roundKeys.get(i)[12];

			rconArr[0] = rcon[0][i];
			rconArr[1] = rcon[1][i];
			rconArr[2] = rcon[2][i];
			rconArr[3] = rcon[3][i];
			// subBytes

			int intRow, intCol;

			for (int hexVals = 0; hexVals < 8; hexVals++) {
				intRow = Integer.parseInt(lastCol[hexVals / 2].substring(0, 1),
						16);
				intCol = Integer.parseInt(lastCol[hexVals / 2].substring(1, 2),
						16);
				lastCol[hexVals / 2 % 4] = sBox[intRow][intCol];
				hexVals++;
			}

			// Adding columns

			for (int row = 0; row < 4; row++) {
				byte firstColData = (byte) ((Character.digit(
						originalFirstCol[row].charAt(0), 16) << 4) + Character
						.digit(originalFirstCol[row].charAt(1), 16));
				byte modifiedData = (byte) ((Character.digit(
						lastCol[row].charAt(0), 16) << 4) + Character.digit(
						lastCol[row].charAt(1), 16));
				byte rconData = (byte) ((Character.digit(
						rconArr[row].charAt(0), 16) << 4) + Character.digit(
						rconArr[row].charAt(1), 16));

				BitSet firstColBits = new BitSet(8);
				BitSet modifiedBits = new BitSet(8);
				BitSet rconBits = new BitSet(8);
				for (int j = 0; j < 8; j++) {
					firstColBits.set(j, (firstColData & 1) == 1);
					firstColData >>= 1;
					modifiedBits.set(j, (modifiedData & 1) == 1);
					modifiedData >>= 1;
					rconBits.set(j, (rconData & 1) == 1);
					rconData >>= 1;
				}
				modifiedBits.xor(firstColBits);
				modifiedBits.xor(rconBits);

				int bitInteger = 0;
				for (int j = 0; j < 8; j++)
					if (modifiedBits.get(j))
						bitInteger |= (1 << j);

				String replacement = Integer.toHexString(bitInteger);
				if (replacement.length() == 1)
					lastCol[row] = "0" + replacement;
				else
					lastCol[row] = replacement;
			}

			nextKey[0] = lastCol[0];
			nextKey[4] = lastCol[1];
			nextKey[8] = lastCol[2];
			nextKey[12] = lastCol[3];

			for (int row = 0; row < 4; row++) {

				for (int col = 1; col < 4; col++) {
					byte specialKeyData = (byte) ((Character.digit(
							lastCol[row].charAt(0), 16) << 4) + Character
							.digit(lastCol[row].charAt(1), 16));
					byte colData = (byte) ((Character.digit(
							roundKeys.get(i)[row * 4 + col].charAt(0), 16) << 4) + Character
							.digit(roundKeys.get(i)[row * 4 + col].charAt(1),
									16));

					BitSet colBits = new BitSet(8);
					BitSet specialKeyBits = new BitSet(8);

					for (int j = 0; j < 8; j++) {
						colBits.set(j, (colData & 1) == 1);
						colData >>= 1;
						specialKeyBits.set(j, (specialKeyData & 1) == 1);
						specialKeyData >>= 1;
					}

					colBits.xor(specialKeyBits);

					int bitInteger = 0;
					for (int j = 0; j < 8; j++)
						if (colBits.get(j))
							bitInteger |= (1 << j);

					String replacement = Integer.toHexString(bitInteger);
					if (replacement.length() == 1)
						lastCol[row] = nextKey[row * 4 + col] = "0"
								+ replacement;
					else
						lastCol[row] = nextKey[row * 4 + col] = replacement;
				}
			}
			roundKeys.add(nextKey);
		}
		return roundKeys;
	}

	private static String[][] encrypt(String[][] state,
			ArrayList<String[]> roundKeys, int roundNumber,
			String[][] sBoxMatrix, boolean encrypt) {
		state = addRoundKey(state, roundKeys, roundNumber, encrypt);
		if (roundNumber < 10) {
			state = subBytes(state, sBoxMatrix);
			state = shiftRows(state);
			if (roundNumber != 9) {
				state = mixColumns(state, encrypt);
			}
		}
		return state;
	}

	private static String[][] decrypt(String[][] state,
			ArrayList<String[]> roundKeys, int roundNumber,
			String[][] sBoxMatrix, boolean encrypt) {
		state = addRoundKey(state, roundKeys, roundNumber, encrypt);
		System.out.println("After addRoundKey(" + (10 - roundNumber) + ")");
		System.out.println(printArray(state));
		if (roundNumber < 10) {
			if (roundNumber != 0) {
				state = mixColumns(state, encrypt);
				System.out.println("After mixColumns");
				System.out.println(printArray(state));
			}
			state = invShiftRows(state);
			state = subBytes(state, sBoxMatrix);
			System.out.println("After subBytes");
			System.out.println(printArray(state));
		}
		return state;
	}

	private static String[][] addRoundKey(String[][] state,
			ArrayList<String[]> roundKeys, int roundNumber, boolean encrypt) {
		String keyMatrix[][] = new String[4][4];
		String[] roundArray;

		if (encrypt)
			roundArray = roundKeys.get(roundNumber);
		else
			roundArray = roundKeys.get(10 - roundNumber);

		for (int row = 0; row < 4; row++)
			for (int col = 0; col < 4; col++)
				keyMatrix[row][col] = roundArray[row * 4 + col];

		for (int row = 0; row < 4; row++)
			for (int col = 0; col < 4; col++) {
				byte data = (byte) ((Character.digit(state[row][col].charAt(0),
						16) << 4) + Character.digit(state[row][col].charAt(1),
						16));
				byte keyData = (byte) ((Character.digit(
						keyMatrix[row][col].charAt(0), 16) << 4) + Character
						.digit(keyMatrix[row][col].charAt(1), 16));

				BitSet dataBits = new BitSet(8);
				BitSet keyBits = new BitSet(8);
				for (int i = 0; i < 8; i++) {
					dataBits.set(i, (data & 1) == 1);
					data >>= 1;
					keyBits.set(i, (keyData & 1) == 1);
					keyData >>= 1;
				}
				dataBits.xor(keyBits);

				int bitInteger = 0;
				for (int i = 0; i < 8; i++)
					if (dataBits.get(i))
						bitInteger |= (1 << i);

				String replacement = Integer.toHexString(bitInteger);
				if (replacement.length() == 1)
					state[row][col] = "0" + replacement;
				else
					state[row][col] = replacement;
			}
		return state;
	}

	private static String[][] subBytes(String[][] state, String[][] sBoxMatrix) {
		int intRow, intCol;
		for (int hexVals = 0; hexVals < 32; hexVals++) {
			intRow = Integer.parseInt(
					state[hexVals / 8][hexVals / 2 % 4].substring(0, 1), 16);
			intCol = Integer.parseInt(
					state[hexVals / 8][hexVals / 2 % 4].substring(1, 2), 16);
			state[hexVals / 8][hexVals / 2 % 4] = sBoxMatrix[intRow][intCol];
			hexVals++;
		}
		return state;
	}

	private static String[][] shiftRows(String[][] state) {
		String movingIndex;
		for (int row = 0; row < 4; row++)
			for (int numberMoves = 0; numberMoves < row; numberMoves++) {
				movingIndex = state[row][0];

				for (int i = 0; i < 3; i++)
					state[row][i] = state[row][i + 1];

				state[row][3] = movingIndex;
			}

		return state;
	}

	private static String[][] invShiftRows(String[][] state) {
		String movingIndex;
		for (int row = 0; row < 4; row++)
			for (int numberMoves = 0; numberMoves < row; numberMoves++) {
				movingIndex = state[row][3];

				for (int i = 3; i > 0; i--)
					state[row][i] = state[row][i - 1];

				state[row][0] = movingIndex;
			}

		return state;
	}

	private static String[][] mixColumns(String[][] state, boolean encrypt) {
		String multiplier;
		if (encrypt)
			multiplier = "02, 03, 01, 01, 01, 02, 03, 01, 01, 01, 02, 03, 03, 01, 01, 02";
		else
			multiplier = "0e, 0b, 0d, 09, 09, 0e, 0b, 0d, 0d, 09, 0e, 0b, 0b, 0d, 09, 0e";
		String[] multiplierArray = multiplier.replace(" ", "").split(",");

		// Forming matrix
		String multiplierMatrix[][] = new String[4][4];

		for (int row = 0; row < 4; row++)
			for (int col = 0; col < 4; col++)
				multiplierMatrix[row][col] = multiplierArray[row * 4 + col];

		// BitSet to offset carrying of 1s
		BitSet offset = new BitSet(8);
		offset.set(0);
		offset.set(1);
		offset.set(3);
		offset.set(4);

		// String array for storing result
		int result[] = new int[4];

		// Converting hexadecimal to bits
		for (int col = 0; col < 4; col++) {
			BitSet dataSum = new BitSet(8);
			for (int multiplierRow = 0; multiplierRow < 4; multiplierRow++) {
				for (int row = 0; row < 4; row++) {
					byte data;
					data = (byte) ((Character.digit(state[row][col].charAt(0),
							16) << 4) + Character.digit(
							state[row][col].charAt(1), 16));

					BitSet bits = new BitSet(8);
					for (int i = 0; i < 8; i++) {
						bits.set(i, (data & 1) == 1);
						data >>= 1;
					}

					BitSet shifted;
					if (!multiplierMatrix[multiplierRow][row].equals("01")) {
						shifted = bits.get(0, bits.length());
						for (int shiftBits = 7; shiftBits > 0; shiftBits--) {
							if (shifted.get(shiftBits - 1))
								shifted.set(shiftBits);
							else
								shifted.set(shiftBits, false);
						}
						shifted.set(0, false);

						if (bits.get(7))
							shifted.xor(offset);
						if (multiplierMatrix[multiplierRow][row].equals("03")) {
							shifted.xor(bits);
						}
						if (row == 0)
							dataSum = shifted;
						else
							dataSum.xor(shifted);
					} else {
						if (row == 0)
							dataSum = bits;
						else
							dataSum.xor(bits);
					}

				}
				int bitInteger = 0;
				for (int i = 0; i < 8; i++)
					if (dataSum.get(i))
						bitInteger |= (1 << i);
				result[multiplierRow] = bitInteger;
			}
			for (int replaceRound = 0; replaceRound < 4; replaceRound++) {
				String replacement = Integer.toHexString(result[replaceRound]);
				if (replacement.length() == 1)
					state[replaceRound][col] = "0" + replacement;
				else
					state[replaceRound][col] = replacement;
			}
		}

		return state;
	}

	private static String printArray(String[][] array) {
		String s = "";
		for (int i = 0; i < array.length; i++) {
			for (int j = 0; j < array.length; j++) {
				s += array[j][i];
			}
		}
		return s;
	}
}