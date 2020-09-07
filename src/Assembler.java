import java.util.ArrayList;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;


/**
 * Assembler : 
 * �� ���α׷��� SIC/XE �ӽ��� ���� Assembler ���α׷��� ���� ��ƾ�̴�.
 * ���α׷��� ���� �۾��� ������ ����. 
 * 1) ó�� �����ϸ� Instruction ���� �о�鿩�� assembler�� �����Ѵ�. 
 * 2) ����ڰ� �ۼ��� input ������ �о���� �� �����Ѵ�. 
 * 3) input ������ ������� �ܾ�� �����ϰ� �ǹ̸� �ľ��ؼ� �����Ѵ�. (pass1) 
 * 4) �м��� ������ �������� ��ǻ�Ͱ� ����� �� �ִ� object code�� �����Ѵ�. (pass2) 
 * 
 * 
 * �ۼ����� ���ǻ��� : 
 *  1) ���ο� Ŭ����, ���ο� ����, ���ο� �Լ� ������ �󸶵��� ����. ��, ������ ������ �Լ����� �����ϰų� ������ ��ü�ϴ� ���� �ȵȴ�.
 *  2) ���������� �ۼ��� �ڵ带 �������� ������ �ʿ信 ���� ����ó��, �������̽� �Ǵ� ��� ��� ���� ����.
 *  3) ��� void Ÿ���� ���ϰ��� ������ �ʿ信 ���� �ٸ� ���� Ÿ������ ���� ����.
 *  4) ����, �Ǵ� �ܼ�â�� �ѱ��� ��½�Ű�� �� ��. (ä������ ����. �ּ��� ���Ե� �ѱ��� ��� ����)
 * 
 *     
 *  + �����ϴ� ���α׷� ������ ��������� �����ϰ� ���� �е��� ������ ��� �޺κп� ÷�� �ٶ��ϴ�. ���뿡 ���� �������� ���� �� �ֽ��ϴ�.
 */
public class Assembler {
	/** instruction ���� ������ ���� */
	InstTable instTable;
	/** �о���� input ������ ������ �� �� �� �����ϴ� ����. */
	ArrayList<String> lineList;
	/** ���α׷��� section���� symbol table�� �����ϴ� ����*/
	ArrayList<SymbolTable> symtabList;
	/** ���α׷��� section���� literal table�� �����ϴ� ����*/
	ArrayList<LiteralTable> literaltabList;
	/** ���α׷��� section���� ���α׷��� �����ϴ� ����*/
	ArrayList<TokenTable> TokenList;
	/** 
	 * Token, �Ǵ� ���þ ���� ������� ������Ʈ �ڵ���� ��� ���·� �����ϴ� ����.   
	 * �ʿ��� ��� String ��� ������ Ŭ������ �����Ͽ� ArrayList�� ��ü�ص� ������.
	 */
	ArrayList<String> codeList;
	
	/**
	 * Ŭ���� �ʱ�ȭ. instruction Table�� �ʱ�ȭ�� ���ÿ� �����Ѵ�.
	 * 
	 * @param instFile : instruction ���� �ۼ��� ���� �̸�. 
	 */
	public Assembler(String instFile) {
		instTable = new InstTable(instFile);
		lineList = new ArrayList<String>();
		symtabList = new ArrayList<SymbolTable>();
		literaltabList = new ArrayList<LiteralTable>();
		TokenList = new ArrayList<TokenTable>();
		codeList = new ArrayList<String>();
	}

	/** 
	 * ������� ���� ��ƾ
	 */
	public static void main(String[] args) {
		Assembler assembler = new Assembler("inst.data");
		
		assembler.loadInputFile("input.txt");
		assembler.pass1();

		assembler.printSymbolTable("symtab_20150944");
		assembler.printLiteralTable("literaltab_20150944");
		assembler.pass2();
		assembler.printObjectCode("output_20150944");
		
	}

	/**
	 * inputFile�� �о�鿩�� lineList�� �����Ѵ�.
	 * @param inputFile : input ���� �̸�.
	 */
	private void loadInputFile(String inputFile) {
		// TODO Auto-generated method stub
		try {
			//������
			String path = Assembler.class.getResource("").getPath();
			// ȿ�������� �Է��ϱ� ���� �Է� ���� ����
			BufferedReader bufReader = new BufferedReader(new FileReader(path+File.separator+inputFile));
			String line = "";
			while((line = bufReader.readLine()) != null) {
				lineList.add(line);
			}
			// .readLine()�� ���� ���๮�ڸ� ���� �ʴ´�.
			bufReader.close();
		}catch (FileNotFoundException e) {
		}catch(IOException e) {
		}
	}

	/** 
	 * pass1 ������ �����Ѵ�.
	 *   1) ���α׷� �ҽ��� ��ĵ�Ͽ� ��ū������ �и��� �� ��ū���̺� ����
	 *   2) label�� symbolTable�� ����
	 *   
	 *    ���ǻ��� : SymbolTable�� TokenTable�� ���α׷��� section���� �ϳ��� ����Ǿ�� �Ѵ�.
	 */
	private void pass1() {
		// TODO Auto-generated method stub
		SymbolTable symboltable = new SymbolTable();
		LiteralTable literaltable = new LiteralTable();
		TokenTable tokentable = new TokenTable(symboltable, literaltable, instTable);
		ArrayList<Token> tok = new ArrayList<Token>();
		// sect���� �� ��° ��ɾ����� �����Ѵ�.
		int locctr = 0;
		// sect���� �� ��° �ɺ����̺����� �����Ѵ�.
		int symctr = 0;
		// ���� �ּ�
		int locator = 0;
		for(int i=0; i<lineList.size();i++) {
			String[] str = lineList.get(i).split("\t");
			// '.'���� �����ϴ� ��� �����Ѵ�.
			if(str[0].compareTo(".")==0) {
				tokentable.putToken(lineList.get(i));
				locctr++;
				continue;
			}
			// CSECT�� ���� ��� ���ο� sect�̱� ������ �ʱ�ȭ�Ѵ�.
			if (str[1].compareTo("CSECT")==0) {
				symtabList.add(symboltable);
				TokenList.add(tokentable);
				symboltable = new SymbolTable();
				tokentable = new TokenTable(symboltable, literaltable, instTable);
				tokentable.putToken(lineList.get(i));
				locctr = 0;
				symctr = 0;
				locator = 0;
				tokentable.tokenList.get(locctr).location = locator;
				symboltable.putSymbol(str[0], locator);
				locctr++;
				continue;
			}else {
				tokentable.putToken(lineList.get(i));
			}
			
			if((str[1].compareTo("CSECT")!=0)&&(str[1].compareTo("EXTDEF")!=0)&&(str[1].compareTo("EXTREF")!=0)&&(str[1].compareTo("LTORG")!=0)&&(str[1].compareTo("END")!=0)) {
				tokentable.tokenList.get(locctr).location = locator;
			}
			
			if((str[1].compareTo("EQU")==0)&&(tokentable.tokenList.get(i).operand[0].compareTo("*")!=0)) {
				for(int j=0; j<symctr; j++) {
					if(symboltable.symbolList.get(j).compareTo(tokentable.tokenList.get(i).operand[0])==0) {
						locator = symboltable.locationList.get(j);
					}else if (symboltable.symbolList.get(j).compareTo(tokentable.tokenList.get(i).operand[1])==0) {
						locator -= symboltable.locationList.get(j);
						break;
					}
				}
				symboltable.putSymbol(str[0], locator);
				tokentable.tokenList.get(locctr).location = locator;
				symctr++;
				continue;
			}
			if((str.length>2)&&(str[2].length()!=0)&&(str[2].charAt(0)=='=')) {
				String []equ = str[2].split("'");
				literaltable.putLiteral(equ[1], locator);
			}
			// ORG, LTORG�� ��� 
			if(str[1].compareTo("LTORG")==0) {
				literaltable.modifyLiteral(literaltable.literalList.get(literaltable.literalList.size()-1), locator);
				for(int j=0; j<tokentable.tokenList.size();j++) {
					if ((tokentable.tokenList.get(j).operand.length!=0)&&(tokentable.tokenList.get(j).operand[0]!=null)) {
						if(tokentable.tokenList.get(j).operand[0].contains(literaltable.literalList.get(literaltable.literalList.size()-1))) {
							String line = "\t"+tokentable.tokenList.get(j).operand[0];
							Token tk = new Token(line);
							tk.location = locator;
							tok.add(tk);
						}
					}
				}
			}
	
			if(str[1].compareTo("END")==0) {
				literaltable.modifyLiteral(literaltable.literalList.get(literaltable.literalList.size()-1), locator);
				for(int j=0; j<tokentable.tokenList.size();j++) {
					if ((tokentable.tokenList.get(j).operand.length!=0)&&(tokentable.tokenList.get(j).operand[0]!=null)) {
						if(tokentable.tokenList.get(j).operand[0].contains(literaltable.literalList.get(literaltable.literalList.size()-1))) {
							String line = "\t"+tokentable.tokenList.get(j).operand[0];
							Token tk = new Token(line);
							tk.location = locator;
							tok.add(tk);
						}
					}
				}
				symtabList.add(symboltable);
				TokenList.add(tokentable);
			}
			locctr++;
			// �� ��ɾ��� �ּҸ� ���Ѵ�.
			if((str[0].compareTo("")!=0)) {
				symboltable.putSymbol(str[0], locator);
				symctr++;
			}
			if (str[1].compareTo("RESW")==0) {
				locator += 3*Integer.parseInt(str[2]);
			}else if (str[1].compareTo("RESB")==0) {
				locator += Integer.parseInt(str[2]);
			}else if (str[1].compareTo("BYTE")==0) {
				if(str[2].charAt(0) == 'X') {
					locator += (str[2].length()-2)/2;
				}else if(str[2].charAt(0) == 'C') {
					locator += (str[2].length()-2);
				}					
			}else if (str[1].compareTo("LTORG")==0) {
				locator += 3;
			}else if(str[1].charAt(0) == '+') {
				locator += 4;
			}else {
				for(String key : instTable.instMap.keySet()) {
					Instruction value = instTable.instMap.get(key);
					if(str[1].compareTo(value.instruction)==0) {
						if(value.format == 2) locator += 2;
						else locator += 3;
						break;
					}
				}
			}
		}
		// ORG�� �ϼ��� ��ū���̺� �߰��Ѵ�.
		for(int i=0; i<TokenList.size(); i++) {
			for(int j=0; j<TokenList.get(i).tokenList.size(); j++) {
				if(TokenList.get(i).tokenList.get(j).operator.compareTo("LTORG")==0) {
					TokenList.get(i).tokenList.add(j+1, tok.get(0));
				}else if(TokenList.get(i).tokenList.get(j).operator.compareTo("END")==0) {
					TokenList.get(i).tokenList.add(j+1, tok.get(1));
				}
			}
		}
		literaltabList.add(literaltable);
	}
	
	/**
	 * �ۼ��� SymbolTable���� ������¿� �°� ����Ѵ�.
	 * @param fileName : ����Ǵ� ���� �̸�
	 */
	private void printSymbolTable(String fileName) {
		// TODO Auto-generated method stub
		try{
			//������
			String path = Assembler.class.getResource("").getPath();
			// ȿ�������� �Է��ϱ� ���� �Է� ���� ����
			PrintWriter pw = new PrintWriter(path+File.separator+fileName);
			for(int i=0; i<symtabList.size(); i++) {
				for (int j=0; j<symtabList.get(i).symbolList.size(); j++) {
					pw.println(symtabList.get(i).symbolList.get(j)+"\t"+Integer.toHexString(symtabList.get(i).locationList.get(j)).toUpperCase());
				}
				pw.println("");
			}
			pw.close();
		}catch (FileNotFoundException e) {
			e.printStackTrace();
		}
	}

	/**
	 * �ۼ��� LiteralTable���� ������¿� �°� ����Ѵ�.
	 * @param fileName : ����Ǵ� ���� �̸�
	 */
	private void printLiteralTable(String fileName) {
		// TODO Auto-generated method stub
		try{
			//������
			String path = Assembler.class.getResource("").getPath();
			PrintWriter pw = new PrintWriter(path+File.separator+fileName);
			for(int i=0; i<literaltabList.size(); i++) {
				for (int j=0; j<literaltabList.get(i).literalList.size(); j++) {
					pw.println(literaltabList.get(i).literalList.get(j)+"\t"+Integer.toHexString(literaltabList.get(i).locationList.get(j)).toUpperCase());
				}
				pw.println("");
			}
			pw.close();
		}catch (FileNotFoundException e) {
			e.printStackTrace();
		}
	}

	/**
	 * pass2 ������ �����Ѵ�.
	 *   1) �м��� ������ �������� object code�� �����Ͽ� codeList�� ����.
	 */
	private void pass2() {
		// TODO Auto-generated method stub
		for(int i=0; i<symtabList.size(); i++){
			for (int j=0; j<TokenList.get(i).tokenList.size(); j++) {
				TokenList.get(i).makeObjectCode(j);
				if (TokenList.get(i).getObjectCode(j)!=null) {
					codeList.add(TokenList.get(i).getObjectCode(j));
				}
			}
			codeList.add("");
		}
	}
	
	/**
	 * �ۼ��� codeList�� ������¿� �°� ����Ѵ�.
	 * @param fileName : ����Ǵ� ���� �̸�
	 */
	private void printObjectCode(String fileName) {
		// TODO Auto-generated method stub
		try{
			//������
			String path = Assembler.class.getResource("").getPath();
			PrintWriter pw = new PrintWriter(path+File.separator+fileName);
			for(int i=0; i<TokenList.size(); i++) {
				StringBuilder sb = new StringBuilder();
				int txtsize = 0;
				for(int j=0; j<TokenList.get(i).tokenList.size(); j++) {
					Token token = TokenList.get(i).tokenList.get(j);
					// Header �κ�
					if(token.operator.compareTo("START")==0) {
						pw.format("H%-6s%06X%06X\n", token.label, token.location, TokenList.get(i).tokenList.get(TokenList.get(i).tokenList.size()-2).location);			
					}else if(token.operator.compareTo("CSECT")==0) {
						pw.format("H%-6s%06X%06X\n", token.label, token.location, TokenList.get(i).tokenList.get(TokenList.get(i).tokenList.size()-1).location+TokenList.get(i).tokenList.get(TokenList.get(i).tokenList.size()-1).byteSize);			
					}
					// Def �κ�
					else if(token.operator.compareTo("EXTDEF")==0) {
						int oper0 = 0, oper1 = 0, oper2 = 0;
						if(token.operand.length==3) {
							for(int k=0; k<symtabList.get(i).symbolList.size(); k++) {
								if(token.operand[0].compareTo(symtabList.get(i).symbolList.get(k))==0) {
									oper0 = symtabList.get(i).locationList.get(k);
								}else if(token.operand[1].compareTo(symtabList.get(i).symbolList.get(k))==0) {
									oper1 = symtabList.get(i).locationList.get(k);
								}else if(token.operand[2].compareTo(symtabList.get(i).symbolList.get(k))==0) {
									oper2 = symtabList.get(i).locationList.get(k);
								}
							}
							pw.format("D%-6s%06X%-6s%06X%-6s%06X\n", token.operand[0], oper0, token.operand[1], oper1, token.operand[2], oper2);
						}else if(token.operand.length==2) {
							for(int k=0; k<symtabList.get(i).symbolList.size(); k++) {
								if(token.operand[0].compareTo(symtabList.get(i).symbolList.get(k))==0) {
									oper0 = symtabList.get(i).locationList.get(k);
								}else if(token.operand[1].compareTo(symtabList.get(i).symbolList.get(k))==0) {
									oper1 = symtabList.get(i).locationList.get(k);
								}
							}
							pw.format("D%-6s%06X%-6s%06X\n", token.operand[0], oper0, token.operand[1], oper1);
						}else if(token.operand.length==1) {
							for(int k=0; k<symtabList.get(i).symbolList.size(); k++) {
								if(token.operand[0].compareTo(symtabList.get(i).symbolList.get(k))==0) {
									oper0 = symtabList.get(i).locationList.get(k);
								}
							}
							pw.format("D%-6s%06X\n", token.operand[0], oper0);
						}
					}
					// Ref �κ�
					else if(token.operator.compareTo("EXTREF")==0) {
						if(token.operand.length==3) {
							pw.format("R%-6s%-6s%-6s\n", token.operand[0], token.operand[1],token.operand[2]);
						}else if(token.operand.length==2) {
							pw.format("R%-6s%-6s\n", token.operand[0], token.operand[1]);
						}else if(token.operand.length==1) {
							pw.format("R%-6s\n", token.operand[0]);
						}
					}
					// Text �κ�
					else {
						if(token.operator.compareTo("END")==0) {
							if(j!=TokenList.get(i).tokenList.size()-1) continue;
						}
						if(j==TokenList.get(i).tokenList.size()-1) {
							if(txtsize!=0) {
								sb.append(token.objectCode);
								txtsize += token.byteSize;
								pw.format("%02X%s\n", txtsize, sb);
								txtsize=0;
								sb = new StringBuilder();
							}else continue;
						}else if(token.objectCode==null) {
							if(txtsize!=0) {
								pw.format("%02X%s\n", txtsize, sb);
								txtsize=0;
								sb = new StringBuilder();
							}else continue;
						}else if(txtsize==0) {
							pw.format("T%06X", token.location);
							sb.append(token.objectCode);
							txtsize = token.byteSize;
						}else if(txtsize + token.byteSize<=30) {
							sb.append(token.objectCode);
							txtsize += token.byteSize;
						}else if(txtsize + token.byteSize>30){
							pw.format("%02X%s\nT%06X", txtsize, sb, token.location);
							txtsize=token.byteSize;
							sb = new StringBuilder();
						} 
					}
				}
				// Modification �κ�
				ArrayList<String> extref = new ArrayList<String>();
				for(int j=0; j<TokenList.get(i).tokenList.size(); j++) {
					Token token = TokenList.get(i).tokenList.get(j);
					if(token.operator.compareTo("EXTREF")==0) {
						extref.add(token.operand[0]);
						if(token.operand.length==2) {
							extref.add(token.operand[1]);
						}else if(token.operand.length==3) {
							extref.add(token.operand[1]);
							extref.add(token.operand[2]);
						}
					}else if(token.operand[0]!=null) {
						for (int k=0; k<extref.size(); k++) {
							if (token.operand[0].compareTo(extref.get(k))==0) {
								if(token.operator.charAt(0)=='+') {
									pw.format("M%06X05+%s\n", token.location+1, token.operand[0]);
								}else if(token.operator.compareTo("WORD")==0) {
									pw.format("M%06X06+%s\n", token.location, token.operand[0]);
									pw.format("M%06X06-%s\n", token.location, token.operand[1]);
								}
							}
						}
					}
					if(j == TokenList.get(i).tokenList.size()-1) {
						if(i==0) {
							pw.format("E000000\n");
						}else pw.format("E\n");
					}
				}
				pw.println("");
			}
			pw.close();
		}catch (FileNotFoundException e) {
			e.printStackTrace();
		}
	}
}
