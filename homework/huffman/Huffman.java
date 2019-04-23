package huffman;

import java.io.ByteArrayOutputStream;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Queue;
import java.util.Map;
import java.util.PriorityQueue;

/**
 * Huffman instances provide reusable Huffman Encoding Maps for compressing and
 * decompressing text corpi with comparable distributions of characters.
 */
public class Huffman {

    // -----------------------------------------------
    // Construction
    // -----------------------------------------------

    private HuffNode trieRoot;
    private Map<Character, String> encodingMap;

    /**
     * Creates the Huffman Trie and Encoding Map using the character distributions
     * in the given text corpus
     * 
     * @param corpus A String representing a message / document corpus with
     *               distributions over characters that are implicitly used
     *               throughout the methods that follow. Note: this corpus ONLY
     *               establishes the Encoding Map; later compressed corpi may
     *               differ.
     */
    Huffman(String corpus) {
        Queue<HuffNode> frontier = createFrontier(corpus);
        trieRoot = createTrie(frontier);
        encodingMap = new HashMap<Character, String>();
        populateEncodingMap(trieRoot, encodingMap, new String());
    }

    private Queue<HuffNode> createFrontier(String message) {
        Map<String, Integer> frequencyMap = new HashMap<String, Integer>();
        Queue<HuffNode> frontier = new PriorityQueue<HuffNode>(11, (n1, n2) -> n1.compareTo(n2));
        // Gathers character frequency
        Arrays.asList(message.split("")).stream().forEach(letter -> {
            if (!frequencyMap.containsKey(letter)) {
                frequencyMap.put(letter, 1);
            } else {
                frequencyMap.put(letter, frequencyMap.get(letter) + 1);
            }
        });
        // Populates frontier (Priority Queue)
        for (Map.Entry<String, Integer> entry : frequencyMap.entrySet()) {
            frontier.add(new HuffNode(entry.getKey().charAt(0), entry.getValue()));
        }
        return frontier;
    }

    private HuffNode createTrie(Queue<HuffNode> frontier) {
        // 1) Loop until frontier(Queue) is empty
        while (!frontier.isEmpty()) {
            HuffNode firstNode = frontier.poll();
            HuffNode secondNode = frontier.poll();
            if (firstNode == null || secondNode == null) {
                return firstNode == null ? secondNode : firstNode;
            }
            // 2) Parent Node: sum of two nodes popped from PriorityQueue
            HuffNode parent = new HuffNode('\u0000', firstNode.count + secondNode.count);
            parent.left = secondNode;
            parent.right = firstNode;
            // 3) add Parent Node to PriorityQueue
            frontier.add(parent);
        }
        return null;
    }

    private void populateEncodingMap(HuffNode node, Map<Character, String> encodingMap, String code) {
        if (node.isLeaf()) {
            encodingMap.put(node.character, code);
            return;
        }
        populateEncodingMap(node.right, encodingMap, code.concat("0"));
        populateEncodingMap(node.left, encodingMap, code.concat("1"));
    }

    // -----------------------------------------------
    // Compression
    // -----------------------------------------------

    /**
     * Compresses the given String message / text corpus into its Huffman coded
     * bitstring, as represented by an array of bytes. Uses the encodingMap field
     * generated during construction for this purpose.
     * 
     * @param message String representing the corpus to compress.
     * @return {@code byte[]} representing the compressed corpus with the Huffman
     *         coded bytecode. Formatted as 3 components: (1) the first byte
     *         contains the number of characters in the message, (2) the bitstring
     *         containing the message itself, (3) possible 0-padding on the final
     *         byte.
     */
    public byte[] compress(String message) {
        if (trieRoot == null) {
            return new byte[0];
        }

        ByteArrayOutputStream compressedByteStream = new ByteArrayOutputStream();
        List<String> messageArray = Arrays.asList(message.split(""));
        String byteString = toBinaryString((byte) message.length() & 0xFF);

        // Populate byteString("1010010..")
        for (int i = 0; i < messageArray.size(); i++) {
            String bitStringInEncodingMapForChar = encodingMap.get(messageArray.get(i).charAt(0));
            if (bitStringInEncodingMapForChar != null) {
                byteString = byteString.concat(bitStringInEncodingMapForChar);
            }
        }

        // Get # of bits in byteString
        List<String> byteStringArray = Arrays.asList(byteString.split(""));
        int checkForExtraPaddingByMod = byteStringArray.size() % 8;
        if (checkForExtraPaddingByMod != 0) {
            int padding = 8 - checkForExtraPaddingByMod;
            for (int i = 0; i < padding; i++) {
                byteString = byteString.concat("0");
            }
        }

        for (int i = 0; i < byteStringArray.size(); i += 8) {
            compressedByteStream.write((byte) (int) Integer.valueOf(byteString.substring(i, i + 8), 2));
        }
        return compressedByteStream.toByteArray();
    }

    // -----------------------------------------------
    // Decompression
    // -----------------------------------------------

    /**
     * Decompresses the given compressed array of bytes into their original, String
     * representation. Uses the trieRoot field (the Huffman Trie) that generated the
     * compressed message during decoding.
     * 
     * @param compressedMsg {@code byte[]} representing the compressed corpus with
     *                      the Huffman coded bytecode. Formatted as 3 components:
     *                      (1) the first byte contains the number of characters in
     *                      the message, (2) the bitstring containing the message
     *                      itself, (3) possible 0-padding on the final byte.
     * @return Decompressed String representation of the compressed bytecode
     *         message.
     */
    public String decompress(byte[] compressedMsg) {
        Map<String, String> compressedMsgInfoMap = gatherLengthAndByteString(compressedMsg,
                new HashMap<String, String>());
        return byteStringToString(compressedMsgInfoMap.get("byteString"),
                Integer.parseInt(compressedMsgInfoMap.get("length")));
    }

    private Map<String, String> gatherLengthAndByteString(byte[] compressedMsg, Map<String, String> infoMap) {
        String byteString = new String();
        for (int i = 1; i < compressedMsg.length; i++) {
            byteString = byteString.concat(toBinaryString(compressedMsg[i] & 0xFF));
        }
        infoMap.put("byteString", byteString);
        infoMap.put("length", String.valueOf((compressedMsg[0])));
        return infoMap;
    }

    private String byteStringToString(String byteString, int originalMessageLength) {
        String decompressedMessage = new String();
        int startIndex = 0;
        int endIndex = startIndex + 1;
        while ((decompressedMessage.length() < originalMessageLength) && (endIndex < byteString.length())) {
            String letterInMessage = findLetter(trieRoot, byteString.substring(startIndex, endIndex), new String());
            if (letterInMessage != null) {
                decompressedMessage = decompressedMessage.concat(letterInMessage);
                startIndex = endIndex;
                endIndex = startIndex + 1;
                continue;
            }
            endIndex++;
        }
        return decompressedMessage;
    }

    private String findLetter(HuffNode node, String byteString, String code) {
        if (node.isLeaf()) {
            return byteString.equals(code) ? Character.toString(node.character) : null;
        }
        String letter = findLetter(node.right, byteString, code.concat("0"));
        if (letter != null) {
            return letter;
        }
        letter = findLetter(node.left, byteString, code.concat("1"));
        return letter != null ? letter : null;
    }

    private String toBinaryString(int value) {
        return String.format("%8s", Integer.toBinaryString(value)).replace(' ', '0');
    }

    // -----------------------------------------------
    // Huffman Trie
    // -----------------------------------------------

    /**
     * Huffman Trie Node class used in construction of the Huffman Trie. Each node
     * is a binary (having at most a left and right child), contains a character
     * field that it represents (in the case of a leaf, otherwise the null character
     * \0), and a count field that holds the number of times the node's character
     * (or those in its subtrees) appear in the corpus.
     */
    private class HuffNode implements Comparable<HuffNode> {

        HuffNode left, right;
        char character;
        int count;

        HuffNode(char character, int count) {
            this.count = count;
            this.character = character;
        }

        public boolean isLeaf() {
            return left == null && right == null;
        }

        public int compareTo(HuffNode other) {
            return this.count - other.count;
        }

        public String toString() {
            return String.format("\ncharacter: %s\ncount: %d\n", this.character, this.count);
        }

    }

}
