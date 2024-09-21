class WorldQueryLexer extends Lexer;

options {
    exportVocab=WorldQuery;
    k=2;
}

ID : ('a'..'z' | 'A'..'Z')+ ;
NUM : ('0'..'9')+ ;
WS  : (' ' | '\t' | '\n' | '\r') { $setType(Token.SKIP); } ;


class WorldQueryParser extends Parser;

options {
    importVocab=WorldQuery;
    k=2;
}

simple : ID NUM ;
