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
		ArrayList<String[]> roundKeys = makeRoundKeys(sBoxMatrix(true),
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
						for (int i = 0; i < 4; i++) {
							for (int j = 0; j < 4; j++) {
								if(encrypt)
									state[i][j] = keyString.substring(
										8 * i + 2 * j, 8 * i + 2 * j + 2);
								else
									state[j][i] = keyString.substring(
										8 * i + 2 * j, 8 * i + 2 * j + 2);
							}
						}

						// Rounds
						for (int roundNumber = 0; roundNumber < 11; roundNumber++) {
							if (encrypt)
								state = encrypt(state, roundKeys, roundNumber,
										sBoxMatrix, encrypt);
							else
								state = decrypt(state, roundKeys, roundNumber,
										sBoxMatrix, encrypt);
						}

						if(!encrypt){
							String[][] temp = new String[4][4];
							String[][] state2 = new String[4][4];
							for(int row = 0; row < 4; row++){
								for(int col=0; col < 4; col++){
									temp[col][row] = state[row][col];
								}
							}
							for(int row = 0; row < 4; row++){
								for(int col=0; col < 4; col++){
									state[row][col] = temp[row][col];
								}
							}
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

	private static String[][] rconMatrix(){
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
		}
		else {
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

		for(int i = 0; i < 10; i++) {
			
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
				intRow = Integer.parseInt(lastCol[hexVals/2].substring(0, 1), 16);
				intCol = Integer.parseInt(lastCol[hexVals/2].substring(1, 2), 16);
				lastCol[hexVals / 2 % 4] = sBox[intRow][intCol];
				hexVals++;
			}
			
			// Adding columns
			
			for (int row = 0; row < 4; row++) {
				byte firstColData = (byte) ((Character.digit(
						originalFirstCol[row].charAt(0), 16) << 4) + Character
						.digit(originalFirstCol[row].charAt(1), 16));
				byte modifiedData = (byte) ((Character.digit(
						lastCol[row].charAt(0), 16) << 4) + Character
						.digit(lastCol[row].charAt(1), 16));
				byte rconData = (byte) ((Character.digit(
						rconArr[row].charAt(0), 16) << 4) + Character
						.digit(rconArr[row].charAt(1), 16));

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

				for(int col = 1; col < 4; col++) {
					byte specialKeyData = (byte) ((Character.digit(
							lastCol[row].charAt(0), 16) << 4) + Character
							.digit(lastCol[row].charAt(1), 16));
					byte colData = (byte) ((Character.digit(
							roundKeys.get(i)[row * 4 + col].charAt(0), 16) << 4) + Character
							.digit(roundKeys.get(i)[row * 4 + col].charAt(1), 16));
					
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
						lastCol[row] = nextKey[row * 4 + col] = "0" + replacement;
					else
						lastCol[row] = nextKey[row * 4 + col] = replacement;
				}
			}
			roundKeys.add(nextKey);
		}
		return roundKeys;
	}
	
	private static String[][] encrypt(String[][] state, ArrayList<String[]> roundKeys, int roundNumber, String[][] sBoxMatrix, boolean encrypt){
		state = addRoundKey(state, roundKeys, roundNumber, encrypt);
		if(roundNumber < 10) {
			state = subBytes(state, sBoxMatrix);
			state = shiftRows(state);
			if(roundNumber != 9) {
				state = mixColumns(state, encrypt);
			}
		}
		return state;
	}
	
	private static String[][] decrypt(String[][] state, ArrayList<String[]> roundKeys, int roundNumber, String[][] sBoxMatrix, boolean encrypt){
		state = addRoundKey(state, roundKeys, roundNumber, encrypt);
		if(roundNumber < 10) {
			if(roundNumber != 0) {
				for(int i=0; i<4; i++)
					state = invMixColumns(i, state);
			}
			state = invShiftRows(state);
			state = subBytes(state, sBoxMatrix);;
		}

		return state;
	}
	
	private static String[][] addRoundKey(String[][] state, ArrayList<String[]> roundKeys, int roundNumber, boolean encrypt) {
		String keyMatrix[][] = new String[4][4];
		String[] roundArray;
		
		if(encrypt)
			roundArray = roundKeys.get(roundNumber);
		else
			roundArray = roundKeys.get(10-roundNumber);
		
		for (int row = 0; row < 4; row++)
			for (int col = 0; col < 4; col++)
				keyMatrix[row][col] = roundArray[row * 4 + col];

		for (int row = 0; row < 4; row++)
			for (int col = 0; col < 4; col++) {
				byte data = (byte) ((Character.digit(
						state[row][col].charAt(0), 16) << 4) + Character
						.digit(state[row][col].charAt(1), 16));
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
	
	private static String[][] subBytes(String[][] state, String[][] sBoxMatrix){
		int intRow, intCol;
		for (int hexVals = 0; hexVals < 32; hexVals++) {
			intRow = Integer
					.parseInt(state[hexVals / 8][hexVals / 2 % 4].substring(0, 1), 16);
			intCol = Integer.parseInt(
					state[hexVals / 8][hexVals / 2 % 4].substring(1, 2), 16);
			state[hexVals / 8][hexVals / 2 % 4] = sBoxMatrix[intRow][intCol];
			hexVals++;
		}
		return state;
	}

	private static String[][] shiftRows(String[][] state){
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
	
	private static String[][] invShiftRows(String[][] state){
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
		if(encrypt)
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
					data = (byte) ((Character.digit(
							state[row][col].charAt(0), 16) << 4) + Character
							.digit(state[row][col].charAt(1), 16));

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

		////////////////////////the invMixColumns Tranformation ////////////////////////


	final static int[] LogTable = {
		0,   0,  25,   1,  50,   2,  26, 198,  75, 199,  27, 104,  51, 238, 223,   3, 
		100,   4, 224,  14,  52, 141, 129, 239,  76, 113,   8, 200, 248, 105,  28, 193, 
		125, 194,  29, 181, 249, 185,  39, 106,  77, 228, 166, 114, 154, 201,   9, 120, 
		101,  47, 138,   5,  33,  15, 225,  36,  18, 240, 130,  69,  53, 147, 218, 142, 
		150, 143, 219, 189,  54, 208, 206, 148,  19,  92, 210, 241,  64,  70, 131,  56, 
		102, 221, 253,  48, 191,   6, 139,  98, 179,  37, 226, 152,  34, 136, 145,  16, 
		126, 110,  72, 195, 163, 182,  30,  66,  58, 107,  40,  84, 250, 133,  61, 186, 
		43, 121,  10,  21, 155, 159,  94, 202,  78, 212, 172, 229, 243, 115, 167,  87, 
		175,  88, 168,  80, 244, 234, 214, 116,  79, 174, 233, 213, 231, 230, 173, 232, 
		44, 215, 117, 122, 235,  22,  11, 245,  89, 203,  95, 176, 156, 169,  81, 160, 
		127,  12, 246, 111,  23, 196,  73, 236, 216,  67,  31,  45, 164, 118, 123, 183, 
		204, 187,  62,  90, 251,  96, 177, 134,  59,  82, 161, 108, 170,  85,  41, 157, 
		151, 178, 135, 144,  97, 190, 220, 252, 188, 149, 207, 205,  55,  63,  91, 209, 
		83,  57, 132,  60,  65, 162, 109,  71,  20,  42, 158,  93,  86, 242, 211, 171, 
		68,  17, 146, 217,  35,  32,  46, 137, 180, 124, 184,  38, 119, 153, 227, 165, 
		103,  74, 237, 222, 197,  49, 254,  24,  13,  99, 140, 128, 192, 247, 112,   7};

	final static int[] AlogTable = {
		1,   3,   5,  15,  17,  51,  85, 255,  26,  46, 114, 150, 161, 248,  19,  53, 
		95, 225,  56,  72, 216, 115, 149, 164, 247,   2,   6,  10,  30,  34, 102, 170, 
		229,  52,  92, 228,  55,  89, 235,  38, 106, 190, 217, 112, 144, 171, 230,  49, 
		83, 245,   4,  12,  20,  60,  68, 204,  79, 209, 104, 184, 211, 110, 178, 205, 
		76, 212, 103, 169, 224,  59,  77, 215,  98, 166, 241,   8,  24,  40, 120, 136, 
		131, 158, 185, 208, 107, 189, 220, 127, 129, 152, 179, 206,  73, 219, 118, 154, 
		181, 196,  87, 249,  16,  48,  80, 240,  11,  29,  39, 105, 187, 214,  97, 163, 
		254,  25,  43, 125, 135, 146, 173, 236,  47, 113, 147, 174, 233,  32,  96, 160, 
		251,  22,  58,  78, 210, 109, 183, 194,  93, 231,  50,  86, 250,  21,  63,  65, 
		195,  94, 226,  61,  71, 201,  64, 192,  91, 237,  44, 116, 156, 191, 218, 117, 
		159, 186, 213, 100, 172, 239,  42, 126, 130, 157, 188, 223, 122, 142, 137, 128, 
		155, 182, 193,  88, 232,  35, 101, 175, 234,  37, 111, 177, 200,  67, 197,  84, 
		252,  31,  33,  99, 165, 244,   7,   9,  27,  45, 119, 153, 176, 203,  70, 202, 
		69, 207,  74, 222, 121, 139, 134, 145, 168, 227,  62,  66, 198,  81, 243,  14, 
		18,  54,  90, 238,  41, 123, 141, 140, 143, 138, 133, 148, 167, 242,  13,  23, 
		57,  75, 221, 124, 132, 151, 162, 253,  28,  36, 108, 180, 199,  82, 246,   1};

	private static byte mul (int a, byte b) {
		int inda = (a < 0) ? (a + 256) : a;
		int indb = (b < 0) ? (b + 256) : b;

		if ( (a != 0) && (b != 0) ) {
			int index = (LogTable[inda] + LogTable[indb]);
			byte val = (byte)(AlogTable[ index % 255 ] );
			return val;
		}
		else 
			return 0;
	} // mul

	public static String[][] invMixColumns (int c, String[][] state) {
		byte[][] st = new byte[4][4];
		byte a[] = new byte[4];
		for (int col = 0; col < 4; col++) {
			for (int row = 0; row < 4; row++) {
				st[row][col] = (byte) ((Character.digit(
						state[row][col].charAt(0), 16) << 4) + Character
						.digit(state[row][col].charAt(1), 16));
			}
		}

		// note that a is just a copy of st[.][c]
		for (int i = 0; i < 4; i++) 
			a[i] = st[i][c];

		st[0][c] = (byte)(mul(0xE,a[0]) ^ mul(0xB,a[1]) ^ mul(0xD, a[2]) ^ mul(0x9,a[3]));
		st[1][c] = (byte)(mul(0xE,a[1]) ^ mul(0xB,a[2]) ^ mul(0xD, a[3]) ^ mul(0x9,a[0]));
		st[2][c] = (byte)(mul(0xE,a[2]) ^ mul(0xB,a[3]) ^ mul(0xD, a[0]) ^ mul(0x9,a[1]));
		st[3][c] = (byte)(mul(0xE,a[3]) ^ mul(0xB,a[0]) ^ mul(0xD, a[1]) ^ mul(0x9,a[2]));
		
		byte[] replace = new byte[1];
		for (int row = 0; row < 4; row++) {
			replace[0] = st[row][c];
			state[row][c] = String.format("%02X", replace[0]).toLowerCase();
		}
		return state;
	} // invMixColumns

	private static String printArray(String[][] array){
		String s="";
		for(int i=0; i<array.length;i++){
			for(int j=0; j<array.length;j++){
				s+=array[j][i];
			}
		}
		return s;
	}
}