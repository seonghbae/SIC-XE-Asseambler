import java.util.ArrayList;

/**
 * ����ڰ� �ۼ��� ���α׷� �ڵ带 �ܾ�� ���� �� ��, �ǹ̸� �м��ϰ�, ���� �ڵ�� ��ȯ�ϴ� ������ �Ѱ��ϴ� Ŭ�����̴�. <br>
 * pass2���� object code�� ��ȯ�ϴ� ������ ȥ�� �ذ��� �� ���� symbolTable�� instTable�� ������ �ʿ��ϹǷ� �̸� ��ũ��Ų��.<br>
 * section ���� �ν��Ͻ��� �ϳ��� �Ҵ�ȴ�.
 *
 */
public class TokenTable {
	public static final int MAX_OPERAND=3;
	
	/* bit ������ �������� ���� ���� */
	public static final int nFlag=32;
	public static final int iFlag=16;
	public static final int xFlag=8;
	public static final int bFlag=4;
	public static final int pFlag=2;
	public static final int eFlag=1;
	
	/* Token�� �ٷ� �� �ʿ��� ���̺���� ��ũ��Ų��. */
	SymbolTable symTab;
	LiteralTable literalTab;
	InstTable instTab;
	
	
	/** �� line�� �ǹ̺��� �����ϰ� �м��ϴ� ����. */
	ArrayList<Token> tokenList;
	
	/**
	 * �ʱ�ȭ�ϸ鼭 symTable,literalTable�� instTable�� ��ũ��Ų��.
	 * @param symTab : �ش� section�� ����Ǿ��ִ� symbol table
	 * @param literalTab : �ش� section�� ����Ǿ��ִ� literal table
	 * @param instTab : instruction ���� ���ǵ� instTable
	 */
	public TokenTable(SymbolTable symTab, LiteralTable literalTab, InstTable instTab){
		this.symTab = symTab;
		this.literalTab = literalTab;
		this.instTab = instTab;
		tokenList = new ArrayList<Token>();
		}
	/**
	 * �Ϲ� ���ڿ��� �޾Ƽ� Token������ �и����� tokenList�� �߰��Ѵ�.
	 * @param line : �и����� ���� �Ϲ� ���ڿ�
	 */
	public void putToken(String line) {
		tokenList.add(new Token(line));
	}
	
	/**
	 * tokenList���� index�� �ش��ϴ� Token�� �����Ѵ�.
	 * @param index
	 * @return : index��ȣ�� �ش��ϴ� �ڵ带 �м��� Token Ŭ����
	 */
	public Token getToken(int index) {
		return tokenList.get(index);
	}
	
	/**
	 * Pass2 �������� ����Ѵ�.
	 * 2������ ���, register�� �̸��� �Է¹޾� �������� ��ȣ�� ����Ѵ�.
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
	 * Pass2 �������� ����Ѵ�.
	 * instruction table, symbol table literal table ���� �����Ͽ� objectcode�� �����ϰ�, �̸� �����Ѵ�.
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
				// BYTE�� ��� ���� ó���Ѵ�.
				if(oper.compareTo("BYTE")==0) {
					token.objectCode = opand.substring(2, opand.length()-1);
					token.byteSize = (opand.length()-3)/2;
				}
				// WORD�� ��� ���� ó���Ѵ�.
				else if(oper.compareTo("WORD")==0) {
					token.objectCode = String.format("%06X", 0);
					token.byteSize = 3;
				}
				// ORG, ���ͷ����̺� �ش��ϴ� objectcode�� �����Ѵ�.
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
				// 4���� ��ɾ��� ��� '+'�� �����Ѵ�.
				if(oper.charAt(0)=='+') {
					oper = oper.substring(1);
				}
				// ��ɾ instruction�� ���
				if (oper.compareTo(value.instruction)==0) {
					// 2����
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
					// ������ ��� �⺻������ SIC/XE�ӽ��̱� ������ ni�� p�� 1�� �����Ѵ�.
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
						// RSUB��ɾ��� ��� �ٸ� ��ɾ�� �ٸ��� ������ ���� ó���Ѵ�.
						if(oper.compareTo("RSUB")==0) {
							token.setFlag(pFlag, 0);
							temp = (int)value.opcode<<16;
							temp += token.nixbpe<<12;
						}
						// 4���� (extended addressing)
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
						// ORG, ���ͷ��� ������ ���ͷ����̺��� �����Ѵ�.
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
							// ��κ��� ���(3����)
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
	 * index��ȣ�� �ش��ϴ� object code�� �����Ѵ�.
	 * @param index
	 * @return : object code
	 */
	public String getObjectCode(int index) {
		return tokenList.get(index).objectCode;
	}
	
}

/**
 * �� ���κ��� ����� �ڵ带 �ܾ� ������ ������ ��  �ǹ̸� �ؼ��ϴ� ���� ���Ǵ� ������ ������ �����Ѵ�. 
 * �ǹ� �ؼ��� ������ pass2���� object code�� �����Ǿ��� ���� ����Ʈ �ڵ� ���� �����Ѵ�.
 */
class Token{
	//�ǹ� �м� �ܰ迡�� ���Ǵ� ������
	int location;
	String label;
	String operator;
	String[] operand;
	String comment;
	char nixbpe;

	// object code ���� �ܰ迡�� ���Ǵ� ������ 
	String objectCode;
	int byteSize;
	
	/**
	 * Ŭ������ �ʱ�ȭ �ϸ鼭 �ٷ� line�� �ǹ� �м��� �����Ѵ�. 
	 * @param line ��������� ����� ���α׷� �ڵ�
	 */
	public Token(String line) {
		//initialize �߰�
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
	 * line�� �������� �м��� �����ϴ� �Լ�. Token�� �� ������ �м��� ����� �����Ѵ�.
	 * @param line ��������� ����� ���α׷� �ڵ�.
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
	 * n,i,x,b,p,e flag�� �����Ѵ�. 
	 * 
	 * ��� �� : setFlag(nFlag, 1); 
	 *   �Ǵ�     setFlag(TokenTable.nFlag, 1);
	 * 
	 * @param flag : ���ϴ� ��Ʈ ��ġ
	 * @param value : ����ְ��� �ϴ� ��. 1�Ǵ� 0���� �����Ѵ�.
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
	 * ���ϴ� flag���� ���� ���� �� �ִ�. flag�� ������ ���� ���ÿ� �������� �÷��׸� ��� �� ���� �����ϴ� 
	 * 
	 * ��� �� : getFlag(nFlag)
	 *   �Ǵ�     getFlag(nFlag|iFlag)
	 * 
	 * @param flags : ���� Ȯ���ϰ��� �ϴ� ��Ʈ ��ġ
	 * @return : ��Ʈ��ġ�� �� �ִ� ��. �÷��׺��� ���� 32, 16, 8, 4, 2, 1�� ���� ������ ����.
	 */
	public int getFlag(int flags) {
		return nixbpe & flags;
	}
}
