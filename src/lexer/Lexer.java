package lexer;


/**
 *  The Lexer class is responsible for scanning the source file
 *  which is a stream of characters and returning a stream of 
 *  tokens; each token object will contain the string (or access
 *  to the string) that describes the token along with an
 *  indication of its location in the source program to be used
 *  for error reporting; we are tracking line numbers; white spaces
 *  are space, tab, newlines
*/
public class Lexer {
    private boolean atEOF = false;
    private char ch;     // next character to process
    private SourceReader source;
    private int lineNo;
    
    // positions in line of current token
    private int startPosition, endPosition; 

    public Lexer(String sourceFile) throws Exception {
        new TokenType();  // init token table
        source = new SourceReader(sourceFile);
        ch = source.read();
        lineNo=0;
    }

/*
    public static void main(String args[]) {
        Token tok;
        //Prompt user to add correct file to run lexer
        System.out.println("Enter the file name you wish Lexer to run on:");

        try {
            
          //arg[0] will store file name from command line
            Lexer lex = new Lexer("args[0]");
            
            while (true) {
                tok = lex.nextToken(); //nextToken() calls source.read()
                //prints out the whole program in source.read()
                String p =tok.toString();
               
                // Token class calls get line number method
                p= p + "\t\t"+    "Left: " + tok.getLeftPosition() +
                   " Right: " + tok.getRightPosition()+": "+"Line: "+ tok.getTokenLineNo(); 
                System.out.println(p);
            }
            
                
        } catch (Exception e) {
          if(e.getMessage()!=null){ //if error is caused by return null from another method, then dont print out
              //otherwise, print out error for incorrect files/IO problems
              System.out.println(e.getMessage());
          }
        }
    }
*/
 
/**
 *  newIdTokens are either ids or reserved words; new id's will be inserted
 *  in the symbol table with an indication that they are id's
 *  @param id is the String just scanned - it's either an id or reserved word
 *  @param startPosition is the column in the source file where the token begins
 *  @param endPosition is the column in the source file where the token ends
 *  @return the Token; either an id or one for the reserved words
*/
    public Token newIdToken(String id,int startPosition,int endPosition, int lineNum) {
        return new Token(startPosition,endPosition,Symbol.symbol(id,Tokens.Identifier),lineNum);
    }

/**
 *  number tokens are inserted in the symbol table; we don't convert the 
 *  numeric strings to numbers until we load the bytecodes for interpreting;
 *  this ensures that any machine numeric dependencies are deferred
 *  until we actually run the program; i.e. the numeric constraints of the
 *  hardware used to compile the source program are not used
 *  @param number is the int String just scanned
 *  @param startPosition is the column in the source file where the int begins
 *  @param endPosition is the column in the source file where the int ends
 *  @return the int Token
*/
    public Token newNumberToken(String number,int startPosition,int endPosition, int lineNum) {
        return new Token(startPosition,endPosition,
            Symbol.symbol(number,Tokens.INTeger),lineNum);
    }

    /**
 *  float tokens are inserted in the symbol table; we don't convert the 
 *  numeric strings to numbers until we load the bytecodes for interpreting;
 *  this ensures that any machine numeric dependencies are deferred
 *  until we actually run the program; i.e. the numeric constraints of the
 *  hardware used to compile the source program are not used
 *  @param number is the float String just scanned
 *  @param startPosition is the column in the source file where the int begins
 *  @param endPosition is the column in the source file where the int ends
 *  @return the float Token
*/
    public Token newFloatToken(String number,int startPosition,int endPosition,int lineNum) {
        return new Token(startPosition,endPosition, Symbol.symbol(number,Tokens.FLOat),lineNum);
    }
/**
 *  build the token for operators (+ -) or separators (parens, braces)
 *  filter out comments which begin with two slashes
 *  @param s is the String representing the token
 *  @param startPosition is the column in the source file where the token begins
 *  @param endPosition is the column in the source file where the token ends
 *  @return the Token just found
*/
    public Token makeToken(String s,int startPosition,int endPosition, int lineNum) {
        if (s.equals("//")) {  // filter comment
            try {
               int oldLine = source.getLineno();
               do {
                   ch = source.read();
               } while (oldLine == source.getLineno());
            } catch (Exception e) {
                    atEOF = true;
            }
            return nextToken();
        }
        Symbol sym = Symbol.symbol(s,Tokens.BogusToken); // be sure it's a valid token
        if (sym == null) { // Symbol is not valid token
             System.out.println("******** illegal character: " + s+" at Line: "+ source.getLineno());// Error message
             //Print out the lines of the program uptil the line with error
             System.out.println(source.getWholeProgram());
             atEOF = true;
             return nextToken();
        }
        return new Token(startPosition,endPosition,sym,lineNum);
        }

/**
 *  @return the next Token found in the source file
*/
    public Token nextToken() { // ch is always the next char to process
        if (atEOF) {
            if (source != null) {
                source.close();
                source = null;
            }
            return null;
        }
        try {
            while (Character.isWhitespace(ch)) {  // scan past whitespace
                ch = source.read();
            }
        } catch (Exception e) {
            atEOF = true;
            return nextToken();
        }
        startPosition = source.getPosition();
        endPosition = startPosition - 1;

        if (Character.isJavaIdentifierStart(ch)) {
            // return tokens for ids and reserved words
            String id = "";
            try {
                do {
                    endPosition++;
                    id += ch;
                    lineNo= source.getLineno();
                    ch = source.read();
                } while (Character.isJavaIdentifierPart(ch));
            } catch (Exception e) {
                atEOF = true;
            }
            return newIdToken(id,startPosition,endPosition,lineNo);
        }
        if (Character.isDigit(ch)) {
            // return number token or float token
            String number = "";
            try {
                do {
                    endPosition++;
                    number += ch;
                    lineNo= source.getLineno();
                    ch = source.read(); // Read the next char
                } while (Character.isDigit(ch));
                if(ch=='.'){
                    endPosition++;
                    number += ch;
                    ch = source.read(); // check if next char is digit
                    while(Character.isDigit(ch)){ //read all the digiits
                        endPosition++;
                        number += ch;
                        lineNo= source.getLineno();
                        ch = source.read(); 
                    }// if no digits after '.', then return float token
                    return newFloatToken(number,startPosition,endPosition,lineNo);
                }// if no '.', then return Number token  
                return newNumberToken(number,startPosition,endPosition, lineNo);
            } catch (Exception e) { //exception if End of File
                atEOF = true;
            }
            
        }// if first char of token is not digit, check if char is '.'
        if(ch=='.'){
            String decimal= "";
            int decPlaces=0; //counter for decimal places
            try {
                do {
                    endPosition++;
                    decimal += ch;
             
                    
                    if(Character.isDigit(ch)){
                        decPlaces++; //increment decPlaces for all digits after '.'
                    }
                    lineNo= source.getLineno();
                    ch = source.read(); // Read the next char
                } while (Character.isDigit(ch));//check whether the next char is digit
                if(decPlaces>0){ //if 1 or more digits after '.', then return float token
                    return newFloatToken(decimal,startPosition,endPosition,lineNo);
                }else{
                    //else ERROR .a or . or .&
                    return makeToken(decimal,startPosition,endPosition,lineNo);
                    //make token decides whether this token is valid and makes program reach EOF
                }
            }catch (Exception e) { //exception if End of File
                atEOF = true;
            }
        }
        
        // At this point the only tokens to check for are one or two
        // characters; we must also check for comments that begin with
        // 2 slashes
        String charOld = "" + ch;
        String op = charOld;
        Symbol sym;
        try {
            endPosition++;
            lineNo= source.getLineno();
            ch = source.read();
            op += ch;
            // check if valid 2 char operator; if it's not in the symbol
            // table then don't insert it since we really have a one char
            // token
            sym = Symbol.symbol(op, Tokens.BogusToken); 
            if (sym == null) {  // it must be a one char token
                return makeToken(charOld,startPosition,endPosition,lineNo);
            }
            endPosition++;
            lineNo= source.getLineno();
            ch = source.read();
            return makeToken(op,startPosition,endPosition,lineNo);
        } catch (Exception e) {}
        atEOF = true;
        if (startPosition == endPosition) {
            op = charOld;
        }
        return makeToken(op,startPosition,endPosition,lineNo);
    }
}