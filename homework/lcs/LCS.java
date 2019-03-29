// Author: Tyler Ilunga

package lcs;

import java.lang.Math;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

public class LCS {
    public static int[][] memoCheck;

    public static Set<String> bottomUpLCS (String rStr, String cStr) {
        int rowLength = rStr.length();
        int colLength = cStr.length();
        memoCheck = new int[rowLength + 1][colLength + 1];
        executeLCS("bu", memoCheck, rStr, cStr);
        return collectSolution(rStr, rowLength, cStr, colLength, memoCheck);
    }

    private static int[][] bottomUpTableFill (int[][] arr, String rStr, String cStr) {
        for (int row = 1; row < arr.length; row++) {
            char currentRChar = rStr.charAt(row - 1);
            for (int col = 1; col < arr[row].length; col++) {
                char currentCChar = cStr.charAt(col - 1);
                int validRow = handleValueInBounds(row);
                int validCol = handleValueInBounds(col);
                // Case 1: Mismatched Letters:
                if (currentRChar != currentCChar) {
                    arr[row][col] = Math.max(arr[validRow][col], arr[row][validCol]);
                    continue;
                }
                // Case 2: Matched Letters:
                arr[row][col] = arr[validRow][validCol] + 1;
            }
        }
        return arr;
    }

    private static Set<String> collectSolution (String rStr, int row, String cStr, int col, int[][] memo) {
        if (row == 0 || col == 0) {
            Set<String> empty = new HashSet<String>();
            empty.add("");
            return empty;
        }
        // Recursive Case - Matched Letters: if rStr[r] == cStr[c]
        if (rStr.charAt(row - 1) == cStr.charAt(col - 1)) {
            return getNewSet(rStr.charAt(row - 1), collectSolution(rStr, row - 1, cStr, col - 1, memo));
        }
        Set<String> result = new HashSet<String>();
        // Recursive Case - Mismatched Letters: if cell to left is greater than or equal
        // to cell above
        if (memo[row][col - 1] >= memo[row - 1][col]) {
            result.addAll(collectSolution(rStr, row, cStr, col - 1, memo));
        }
        // Recursive Case - Mismatched Letters: if cell above is greater than or equal
        // to cell to left
        if (memo[row - 1][col] >= memo[row][col - 1]) {
            result.addAll(new HashSet<String>(collectSolution(rStr, row - 1, cStr, col, memo)));
        }
        return result;
    }

    private static int handleValueInBounds (int value) {
        return value - 1 <= 0 ? 0 : value - 1;
    }

    private static Set<String> getNewSet (char currentChar, Set<String> currentSet) {
        ArrayList<String> toRemove = new ArrayList<String>();
        for (String str : currentSet) {
            toRemove.add(str);
        }
        for (String str : toRemove) {
            currentSet.add(str += currentChar);
        }
        currentSet.removeAll(toRemove);
        return currentSet;
    }

    public static Set<String> topDownLCS (String rStr, String cStr) {
        int rowLength = rStr.length();
        int colLength = cStr.length();
        memoCheck = new int[rowLength + 1][colLength + 1];
        executeLCS("tu", memoCheck, rStr, cStr);
        return collectSolution(rStr, rowLength, cStr, colLength, memoCheck);
    }

    private static int topDownTableFill (boolean[][] memo, int[][] arr, String rStr, String cStr) {
        if (rStr.length() == 0 || cStr.length() == 0) {
            return 0;
        }

        int currentRow = rStr.length();
        int currentCol = cStr.length();
        if (memo[currentRow][currentCol]) {
            return arr[currentRow][currentCol];
        }
        memo[currentRow][currentCol] = true;

        char currentRChar = rStr.charAt(currentRow - 1);
        char currentCChar = cStr.charAt(currentCol - 1);
        if (currentRChar == currentCChar) {
            arr[currentRow][currentCol] = 1 + topDownTableFill(memo, arr, removeChar(rStr), removeChar(cStr));
            return arr[currentRow][currentCol];
        }
        arr[currentRow][currentCol] = Math.max(
            topDownTableFill(memo, arr, removeChar(rStr), cStr),
            topDownTableFill(memo, arr, rStr, removeChar(cStr))
        );
        return arr[currentRow][currentCol];
    }

    private static String removeChar (String str) {
        if (str.length() == 1) {
            return "";
        }
        return String.join("", Arrays.copyOfRange(str.split(""), 0, str.length() - 1));
    }

    private static int checkForMemo (boolean[][] memo, int[][] arr, int currentRow, int currentCol) {
        if (arr[currentRow][currentCol] != 0) {
            return arr[currentRow][currentCol];
        }
        return currentRow;
    }

    private static void executeLCS (String type, int[][] memoCheck, String rStr, String cStr) {
        if (type.equals("bu")) {
            bottomUpTableFill(memoCheck, rStr, cStr);
            return;
        }
        topDownTableFill(new boolean[rStr.length() + 1][cStr.length() + 1], memoCheck, rStr, cStr);
    }

    private static void printArray(int[][] arr) {
        System.out.println(String.format("%s", "printArray()"));
        System.out.println(Arrays.deepToString(arr));
    }

    private static void printArray(boolean[][] arr) {
        System.out.println(String.format("%s", "printArray()"));
        System.out.println(Arrays.deepToString(arr));
    }
}
