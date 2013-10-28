import java.util.BitSet;

public class AES {

	public static void main(String[] args) {
		// TODO Auto-generated method stub
		String input = "19a09ae93df4c6f8e3e28d48be2b2a08";
		String key = "00112233445566778899AABBCCDDEEFF";
		
		String sBox = "63, 7c, 77, 7b, f2, 6b, 6f, c5, 30, 01, 67, 2b, fe, d7, ab, 76, "
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
		String[] sBoxArray = sBox.replace(" ", "").split(",");
		
		// Forming matrix
		String sBoxMatrix[][] = new String[16][16];
		
		for(int row = 0; row < 16; row++)
			for(int col = 0; col < 16; col++)
				sBoxMatrix[row][col] = sBoxArray[row * 16 + col];
		
		// Round one
		
		// subBytes
		String roundOne[][] = new String[4][4];
		int intRow, intCol;
		
		for(int hexVals = 0; hexVals < 32; hexVals++)
		{
			intRow = Integer.parseInt(input.substring(hexVals, hexVals + 1), 16);
			intCol = Integer.parseInt(input.substring(hexVals + 1, hexVals + 2), 16);
			roundOne[hexVals / 8][hexVals / 2 % 4] = sBoxMatrix[intRow][intCol];
			hexVals ++;
		}
		
		// shiftRows
		String movingIndex;
		for(int row = 0; row < 4; row++)
			for(int numberMoves = 0; numberMoves < row; numberMoves++)
			{
				movingIndex = roundOne[row][0];
				
				for (int i = 0; i < 3; i++)
					roundOne[row][i] = roundOne[row][i + 1];
				
				roundOne[row][3] = movingIndex;
			}
		
		// shiftColumns
		
		String multiplier = "02, 03, 01, 01, 01, 02, 03, 01, 01, 01, 02, 03, 03, 01, 01, 02";
		String[] multiplierArray = multiplier.replace(" ", "").split(",");
		
		// Forming matrix
		String multiplierMatrix[][] = new String[4][4];
		
		for(int row = 0; row < 4; row++)
			for(int col = 0; col < 4; col++)
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
		for(int col = 0; col < 4; col++) 
		{
			BitSet dataSum = new BitSet(8);  
			for(int multiplierRow = 0; multiplierRow < 4; multiplierRow ++) 
			{
				for(int row = 0; row < 4; row++)
				{
					byte data;
				    data = (byte) ((Character.digit(roundOne[row][col].charAt(0), 16) << 4) + Character.digit(roundOne[row][col].charAt(1), 16));
				    
				    BitSet bits = new BitSet(8);  
				    for (int i = 0; i < 8; i++)  
				    {  
				        bits.set(i, (data & 1) == 1);  
				        data >>= 1;  
				    }  
				    
			    	BitSet shifted;
				    if(!multiplierMatrix[multiplierRow][row].equals("01")) {
				    	shifted = bits.get(0, bits.length());
				    	for(int shiftBits = 7; shiftBits > 0; shiftBits --) {
				    		if(shifted.get(shiftBits - 1))
				    			shifted.set(shiftBits);
				    		else
				    			shifted.set(shiftBits, false);
				    	}
				    	shifted.set(0, false);
				    	
				    	if(bits.get(7))
				    		shifted.xor(offset);
					    if(multiplierMatrix[multiplierRow][row].equals("03")) {
					    	shifted.xor(bits);
					    }
					    if(row == 0)
					    	dataSum = shifted;
					    else
					    	dataSum.xor(shifted);
				    } else {
				    	if(row == 0)
					    	dataSum = bits;
					    else
					    	dataSum.xor(bits);
				    }

				}
				int bitInteger = 0;
			    for(int i = 0 ; i < 8; i++)
			        if(dataSum.get(i))
			            bitInteger |= (1 << i);
				result[multiplierRow] = bitInteger;
			}
			for(int replaceRound = 0; replaceRound < 4; replaceRound++)
			{
				String replacement = Integer.toHexString(result[replaceRound]);
				if(replacement.length() == 1)
					roundOne[replaceRound][col] = "0" + replacement;
				else		
					roundOne[replaceRound][col] = replacement;
			}
		}
	}
}
