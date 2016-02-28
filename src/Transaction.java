/**
 * Simple class that represents a Transaction
 */
public class Transaction {
    private char type; 
    private String variable;
    private int writeValue;

    public Transaction(char type, String variable, int writeValue) {
        this.type = type;
        this.variable = variable;
        this.writeValue = writeValue;
    }

    public boolean isRead() { return type == 'r'; }

    public char getType() { return type; }

    public String getVariable() { return variable; }

    public int getWriteValue() { return writeValue; }
}