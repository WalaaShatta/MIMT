package MIMT;
import java.util.*;

public class MIMT {

    // ================= Caesar =================

    public static String caesarEncrypt(String text, int shift) {
        StringBuilder result = new StringBuilder();

        for (char ch : text.toCharArray()) {
            if (Character.isUpperCase(ch)) {
                result.append((char)((ch - 'A' + shift) % 26 + 'A'));
            } else if (Character.isLowerCase(ch)) {
                result.append((char)((ch - 'a' + shift) % 26 + 'a'));
            } else {
                result.append(ch);
            }
        }

        return result.toString();
    }

    public static String caesarDecrypt(String text, int shift) {
        return caesarEncrypt(text, 26 - (shift % 26));
    }

    // ================= Affine =================

    public static String affineEncrypt(String text, int a, int b) {
        StringBuilder result = new StringBuilder();

        for (char ch : text.toCharArray()) {
            if (Character.isUpperCase(ch)) {
                int x = ch - 'A';
                int encrypted = (a * x + b) % 26;
                result.append((char)(encrypted + 'A'));
            } else if (Character.isLowerCase(ch)) {
                int x = ch - 'a';
                int encrypted = (a * x + b) % 26;
                result.append((char)(encrypted + 'a'));
            } else {
                result.append(ch);
            }
        }

        return result.toString();
    }

    public static String affineDecrypt(String text, int a, int b) {
        StringBuilder result = new StringBuilder();
        int aInverse = modInverse(a, 26);

        for (char ch : text.toCharArray()) {
            if (Character.isUpperCase(ch)) {
                int x = ch - 'A';
                int decrypted = aInverse * (x - b + 26) % 26;
                result.append((char)(decrypted + 'A'));
            } else if (Character.isLowerCase(ch)) {
                int x = ch - 'a';
                int decrypted = aInverse * (x - b + 26) % 26;
                result.append((char)(decrypted + 'a'));
            } else {
                result.append(ch);
            }
        }

        return result.toString();
    }

    // Modular inverse finder
    public static int modInverse(int a, int m) {
        for (int i = 1; i < m; i++) {
            if ((a * i) % m == 1) {
                return i;
            }
        }
        throw new ArithmeticException("No modular inverse exists.");
    }

    // ================= Main =================

    public static void main(String[] args) {

    	//=== Example of encryption and decryption ============
        String plaintext = "hello";

        int caesarKey = 12;   // shift
        int a = 9;           // must be coprime with 26
        int b = 1;

        System.out.println("Original Text: " + plaintext);

        // Step 1: Caesar
        String afterCaesar = caesarEncrypt(plaintext, caesarKey);
        // Step 2: Affine
        String ciphertext = affineEncrypt(afterCaesar, a, b);
        System.out.println("After Double Encryption (Caesar -> Affine): " + ciphertext);

        // ---------------- Decryption ----------------

        String afterAffineDecrypt = affineDecrypt(ciphertext, a, b);
        String recovered = caesarDecrypt(afterAffineDecrypt, caesarKey);

        System.out.println("Recovered Text: " + recovered);
        
        //====TODO=========
        //add your code here to perform man in the middle attack: for affine you can assume b is 1 to simplify the search
    
      // ================= MITM Attack =================
     // Known pair
     String knownPlaintext  = "secret";
     String knownCiphertext = "ckaxkh";

     int affineb = 1;

     //  a values must be coprime with 26 
     int[] validAffineAValues = {1, 3, 5, 7, 9, 11, 15, 17, 19, 21, 23, 25};


     // Build Caesar table 
     Map<String, List<Integer>> caesarMiddleTable = new HashMap<>();

     for (int caesarShift = 0; caesarShift < 26; caesarShift++) {

         String middleText = caesarEncrypt(knownPlaintext, caesarShift);

         // If this middleText is not in the map, create a new list for it
         caesarMiddleTable.computeIfAbsent(middleText, k -> new ArrayList<>())
                          .add(caesarShift);
     }


     // Print Caesar table 
     System.out.println("\nTABLE 1: CaesarEncrypt(plaintext, shift)   ");
     System.out.println("shift : middleText");
     for (int caesarShift = 0; caesarShift < 26; caesarShift++) {
         System.out.println(caesarShift + " : " + caesarEncrypt(knownPlaintext, caesarShift));
     }



     // Build Affine table 
     Map<String, List<Integer>> affineMiddleTable = new HashMap<>();

     System.out.println("\nTABLE 2: AffineDecrypt(ciphertext, a, b=1)   ");
     System.out.println("a : middleText");
     for (int affineA : validAffineAValues) {

         String middleText = affineDecrypt(knownCiphertext, affineA, affineb);

         affineMiddleTable.computeIfAbsent(middleText, k -> new ArrayList<>()).add(affineA);

         System.out.println(affineA + " : " + middleText);
     }



     // --------  Meet in the middle + Verify --------
     System.out.println("\n ========== Matches ==========");

     boolean keysFound = false;

     for (Map.Entry<String, List<Integer>> caesarEntry : caesarMiddleTable.entrySet()) {

         String middleText = caesarEntry.getKey();

         // If the same middleText appears in the affine table, we found candidates
         if (affineMiddleTable.containsKey(middleText)) {

             for (int caesarShift : caesarEntry.getValue()) {
                 for (int affineA : affineMiddleTable.get(middleText)) {

                     // Verification: re-encrypt plaintext using both steps
                     String verifyCipher = affineEncrypt( caesarEncrypt(knownPlaintext, caesarShift),affineA,affineb );

                     if (verifyCipher.equals(knownCiphertext)) {
                         keysFound = true;
                         System.out.println(" Middle text: " + middleText);
                         System.out.println(" Keys: Caesar shift = " + caesarShift+ ", Affine a value = " + affineA);
                     }
                 }
             }
         }
     }

    
     if (!keysFound) {
    	    System.out.println("No matches found");
     }
        
    }//end of main
}//end of class
