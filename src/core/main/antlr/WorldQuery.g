class WorldQueryLexer extends Lexer;

options {
    exportVocab=WorldQuery;
    k=2;
}

COLON: ':';
DOT: '.';

WORD: ('a'..'z' | 'A'..'Z')+;
ID: (WORD | '_' | '-')+;
INTEGER: ('0'..'9')+;
DECIMAL: INTEGER DOT INTEGER;
CONST: DECIMAL | INTEGER | ID;

EQ: '=';
SIMEQ: '~';
GT: '>';
GTEQ: GT EQ;
LT: '<';
LTEQ: LT EQ;
COMPARATOR: EQ | SIMEQ | GTEQ | LTEQ | GT | LT;

LOOKUP: "lookup";
DENY: "deny";
ALLOW: "allow";
FORCE: "force";
PASSTHROUGH: "passthrough";
VERB: DENY | ALLOW | FORCE | PASSTHROUGH;

RESOURCE: ID COLON ID;
RANGE: CONST DOT DOT CONST;
EXPR: RESOURCE | RANGE | CONST;
GROUPS: "groups";

WS: ' ';



class WorldQueryParser extends Parser;

options {
    importVocab=WorldQuery;
    k=2;
}

condition: WORD COMPARATOR EXPR;
context: "in" (WS GROUPS)? WS ID;
queryDeclare: VERB (WS condition)+ (WS context)?;
queryLookup: LOOKUP (WS condition)+ (WS context)?;
query: queryDeclare | queryLookup;
