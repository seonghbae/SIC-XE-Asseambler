import java.util.ArrayList;

/**
 * 사용자가 작성한 프로그램 코드를 단어별로 분할 한 후, 의미를 분석하고, 최종 코드로 변환하는 과정을 총괄하는 클래스이다. <br>
 * pass2에서 object code로 변환하는 과정은 혼자 해결할 수 없고 symbolTable과 instTable의 정보가 필요하므로 이를 링크시킨다.<br>
 * section 마다 인스턴스가 하나씩 할당된다.
 *
 */
public class TokenTable {
	public static final int MAX_OPERAND=3;
	
	/* bit 조작의 가독성을 위한 선언 */
	public static final int nFlag=32;
	public static final int iFlag=16;
	public static final int xFlag=8;
	public static final int bFlag=4;
	public static final int pFlag=2;
	public static final int eFlag=1;
	
	/* Token을 다룰 때 필요한 테이블들을 링크시킨다. */
	SymbolTable symTab;
	LiteralTable literalTab;
	InstTable instTab;
	
	
	/** 각 line을 의미별로 분할하고 분석하는 공간. */
	ArrayList<Token> tokenList;
	
	/**
	 * 초기화하면서 symTable,literalTable과 instTable을 링크시킨다.
	 * @param symTab : 해당 section과 연결되어있는 symbol table
	 * @param literalTab : 해당 section과 연결되어있는 literal table
	 * @param instTab : instruction 명세가 정의된 instTable
	 */
	public TokenTable(SymbolTable symTab, LiteralTable literalTab, InstTable instTab){
		this.symTab = symTab;
		this.literalTab = literalTab;
		this.instTab = instTab;
		tokenList = new ArrayList<Token>();
		}
	/**
	 * 일반 문자열을 받아서 Token단위로 분리시켜 tokenList에 추가한다.
	 * @param line : 분리되지 않은 일반 문자열
	 */
	public void putToken(String line) {
		tokenList.add(new Token(line));
	}
	
	/**
	 * tokenList에서 index에 해당하는 Token을 리턴한다.
	 * @param index
	 * @return : index번호에 해당하는 코드를 분석한 Token 클래스
	 */
	public Token getToken(int index) {
		return tokenList.get(index);
	}
	
	/**
	 * Pass2 과정에서 사용한다.
	 * 2형식의 경우, register의 이름을 입력받아 레지스터 번호를 출력한다.
	 */
	public int register(String reg) {
		int reg_num = -1;
		switch(reg) {
		case "A":
			reg_num=0;
			break;
		case "X":
			reg_num=1;
			break;
		case "L":
			reg_num=2;
			break;
		case "B":
			reg_num=3;
			break;
		case "S":
			reg_num=4;
			break;
		case "T":
			reg_num=5;
			break;
		case "F":
			reg_num=6;
			break;
		case "PC":
			reg_num=8;
			break;
		case "SW":
			reg_num=9;
			break;
		}
		return reg_num;
	}
	
	/**
	 * Pass2 과정에서 사용한다.
	 * instruction table, symbol table literal table 등을 참조하여 objectcode를 생성하고, 이를 저장한다.
	 * @param index
	 */
	public void makeObjectCode(int index){
		Token token = tokenList.get(index);
		if(token.operator.compareTo("")!=0) {
			String oper = token.operator;
			String opand = token.operand[0];
			
			for (String key : instTab.instMap.keySet()) {
				Instruction value = instTab.instMap.get(key);
				int temp = 0;
				// BYTE의 경우 따로 처리한다.
				if(oper.compareTo("BYTE")==0) {
					token.objectCode = opand.substring(2, opand.length()-1);
					token.byteSize = (opand.length()-3)/2;
				}
				// WORD의 경우 따로 처리한다.
				else if(oper.compareTo("WORD")==0) {
					token.objectCode = String.format("%06X", 0);
					token.byteSize = 3;
				}
				// ORG, 리터럴테이블에 해당하는 objectcode를 생성한다.
				else if(oper.contains(literalTab.literalList.get(0))) {
					for (int i=0; i<literalTab.literalList.get(0).length(); i++) {
						temp += (int) literalTab.literalList.get(0).charAt(i)<<8*(literalTab.literalList.get(0).length()-i-1);
					}
					token.byteSize = 3;
				}else if(oper.contains(literalTab.literalList.get(1))) {
					temp = Integer.parseInt(literalTab.literalList.get(1));
					token.objectCode = String.format("%02X", temp);
					temp = 0;
					token.byteSize = 1;
				}
				// 4형식 명령어의 경우 '+'를 제거한다.
				if(oper.charAt(0)=='+') {
					oper = oper.substring(1);
				}
				// 명령어가 instruction일 경우
				if (oper.compareTo(value.instruction)==0) {
					// 2형식
					if(value.format==2) {
						temp = (int)value.opcode<<8;
						temp += register(opand)<<4;
						if(token.operand.length==2) {
							temp += register(token.operand[1]);
						}
						token.objectCode = String.format("%X", temp);
						temp = 0;
						token.byteSize = 2;
					}
					// 별개의 경우 기본적으로 SIC/XE머신이기 때문에 ni와 p를 1로 설정한다.
					else if(token.operand.length>0) {
						token.setFlag(nFlag, 1);
						token.setFlag(iFlag, 1);
						token.setFlag(pFlag, 1);
						token.byteSize = 3;
						// indexed addressing
						if(token.operand.length==2) {
							if(token.operand[1].compareTo("X")==0) {
								token.setFlag(xFlag, 1);
							}
						}
						// RSUB명령어의 경우 다른 명령어와 다르기 때문에 따로 처리한다.
						if(oper.compareTo("RSUB")==0) {
							token.setFlag(pFlag, 0);
							temp = (int)value.opcode<<16;
							temp += token.nixbpe<<12;
						}
						// 4형식 (extended addressing)
						else if(token.operator.charAt(0)=='+') {
							token.setFlag(pFlag, 0);
							token.setFlag(eFlag, 1);
							temp = (int)value.opcode<<24;
							temp += token.nixbpe<<20;
							token.byteSize = 4;
						}
						// immediate addressing
						else if(opand.charAt(0)=='#') {
							token.setFlag(nFlag, 0);
							token.setFlag(pFlag, 0);
							temp = (int)value.opcode<<16;
							temp += token.nixbpe<<12;
							temp += Integer.parseInt(opand.substring(1));
						}
						// ORG, 리터럴에 저장해 리터럴테이블을 생성한다.
						else if(opand.charAt(0)=='=') {
							opand = opand.substring(3, opand.length()-1);
							for(int i=0; i<literalTab.literalList.size(); i++) {
								if(opand.compareTo(literalTab.literalList.get(i))==0) {
									temp = literalTab.locationList.get(i);
									temp -= tokenList.get(index+1).location;
									if(literalTab.locationList.get(i)<tokenList.get(index+1).location) {
										temp &= 0x00000FFF; 
									}
									break;
								}
							}
							temp += (int)value.opcode<<16;
							temp += token.nixbpe<<12;
						}else {
							// indirect addressing
							if(opand.charAt(0)=='@') {
								token.setFlag(iFlag, 0);
								opand = opand.substring(1);
							}
							// 대부분의 경우(3형식)
							for(int i=0; i<symTab.symbolList.size(); i++) {
								if(opand.compareTo(symTab.symbolList.get(i))==0) {
									temp = symTab.locationList.get(i);
									temp -= tokenList.get(index+1).location;
									if(symTab.locationList.get(i)<tokenList.get(index+1).location) {
										temp &= 0x00000FFF; 
									}
									break;
								}
							}
							temp += (int)value.opcode<<16;
							temp += token.nixbpe<<12;
						}
					}
				}
				if(temp !=0) {
					token.objectCode = String.format("%06X", temp);
				}
			}
		}
	}
	
	/** 
	 * index번호에 해당하는 object code를 리턴한다.
	 * @param index
	 * @return : object code
	 */
	public String getObjectCode(int index) {
		return tokenList.get(index).objectCode;
	}
	
}

/**
 * 각 라인별로 저장된 코드를 단어 단위로 분할한 후  의미를 해석하는 데에 사용되는 변수와 연산을 정의한다. 
 * 의미 해석이 끝나면 pass2에서 object code로 변형되었을 때의 바이트 코드 역시 저장한다.
 */
class Token{
	//의미 분석 단계에서 사용되는 변수들
	int location;
	String label;
	String operator;
	String[] operand;
	String comment;
	char nixbpe;

	// object code 생성 단계에서 사용되는 변수들 
	String objectCode;
	int byteSize;
	
	/**
	 * 클래스를 초기화 하면서 바로 line의 의미 분석을 수행한다. 
	 * @param line 문장단위로 저장된 프로그램 코드
	 */
	public Token(String line) {
		//initialize 추가
		location = -1;
		label = "\u0000";
		operator = "\u0000";
		operand = new String[3];
		comment = "\u0000";
		nixbpe = '\u0000';
		objectCode = null;
		byteSize = 0;
		
		parsing(line);
	}
	
	/**
	 * line의 실질적인 분석을 수행하는 함수. Token의 각 변수에 분석한 결과를 저장한다.
	 * @param line 문장단위로 저장된 프로그램 코드.
	 */
	public void parsing(String line) {
		String[] str = line.split("\t");
		switch (str.length) {
		case 1:
			label = new String(str[0]);
			break;
		case 2:
			label = new String(str[0]);
			operator = new String(str[1]);
			break;
		case 3:
			label = new String(str[0]);
			operator = new String(str[1]);
			if (str[2].contains("-")) {
				operand = str[2].split("-");
			}else operand = str[2].split(",");
			break;
		case 4:
			label = new String(str[0]);
			operator = new String(str[1]);
			if (str[2].contains("-")) {
				operand = str[2].split("-");
			}else operand = str[2].split(",");
			comment = new String(str[3]);
			break;
		}
	}
	
	/** 
	 * n,i,x,b,p,e flag를 설정한다. 
	 * 
	 * 사용 예 : setFlag(nFlag, 1); 
	 *   또는     setFlag(TokenTable.nFlag, 1);
	 * 
	 * @param flag : 원하는 비트 위치
	 * @param value : 집어넣고자 하는 값. 1또는 0으로 선언한다.
	 */
	public void setFlag(int flag, int value) {
		if(value==1) {
			if((int)nixbpe==0) {
				nixbpe = (char)flag;
			}else {
				int temp = (int)nixbpe + flag;
				nixbpe = (char)temp;
			}
		}
		if(value==0) {
			int temp = (int)nixbpe - flag;
			nixbpe = (char)temp;
		}
		
	}
	
	/**
	 * 원하는 flag들의 값을 얻어올 수 있다. flag의 조합을 통해 동시에 여러개의 플래그를 얻는 것 역시 가능하다 
	 * 
	 * 사용 예 : getFlag(nFlag)
	 *   또는     getFlag(nFlag|iFlag)
	 * 
	 * @param flags : 값을 확인하고자 하는 비트 위치
	 * @return : 비트위치에 들어가 있는 값. 플래그별로 각각 32, 16, 8, 4, 2, 1의 값을 리턴할 것임.
	 */
	public int getFlag(int flags) {
		return nixbpe & flags;
	}
}
