import java.util.ArrayList;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;


/**
 * Assembler : 
 * 이 프로그램은 SIC/XE 머신을 위한 Assembler 프로그램의 메인 루틴이다.
 * 프로그램의 수행 작업은 다음과 같다. 
 * 1) 처음 시작하면 Instruction 명세를 읽어들여서 assembler를 세팅한다. 
 * 2) 사용자가 작성한 input 파일을 읽어들인 후 저장한다. 
 * 3) input 파일의 문장들을 단어별로 분할하고 의미를 파악해서 정리한다. (pass1) 
 * 4) 분석된 내용을 바탕으로 컴퓨터가 사용할 수 있는 object code를 생성한다. (pass2) 
 * 
 * 
 * 작성중의 유의사항 : 
 *  1) 새로운 클래스, 새로운 변수, 새로운 함수 선언은 얼마든지 허용됨. 단, 기존의 변수와 함수들을 삭제하거나 완전히 대체하는 것은 안된다.
 *  2) 마찬가지로 작성된 코드를 삭제하지 않으면 필요에 따라 예외처리, 인터페이스 또는 상속 사용 또한 허용됨.
 *  3) 모든 void 타입의 리턴값은 유저의 필요에 따라 다른 리턴 타입으로 변경 가능.
 *  4) 파일, 또는 콘솔창에 한글을 출력시키지 말 것. (채점상의 이유. 주석에 포함된 한글은 상관 없음)
 * 
 *     
 *  + 제공하는 프로그램 구조의 개선방법을 제안하고 싶은 분들은 보고서의 결론 뒷부분에 첨부 바랍니다. 내용에 따라 가산점이 있을 수 있습니다.
 */
public class Assembler {
	/** instruction 명세를 저장한 공간 */
	InstTable instTable;
	/** 읽어들인 input 파일의 내용을 한 줄 씩 저장하는 공간. */
	ArrayList<String> lineList;
	/** 프로그램의 section별로 symbol table을 저장하는 공간*/
	ArrayList<SymbolTable> symtabList;
	/** 프로그램의 section별로 literal table을 저장하는 공간*/
	ArrayList<LiteralTable> literaltabList;
	/** 프로그램의 section별로 프로그램을 저장하는 공간*/
	ArrayList<TokenTable> TokenList;
	/** 
	 * Token, 또는 지시어에 따라 만들어진 오브젝트 코드들을 출력 형태로 저장하는 공간.   
	 * 필요한 경우 String 대신 별도의 클래스를 선언하여 ArrayList를 교체해도 무방함.
	 */
	ArrayList<String> codeList;
	
	/**
	 * 클래스 초기화. instruction Table을 초기화와 동시에 세팅한다.
	 * 
	 * @param instFile : instruction 명세를 작성한 파일 이름. 
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
	 * 어셈블러의 메인 루틴
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
	 * inputFile을 읽어들여서 lineList에 저장한다.
	 * @param inputFile : input 파일 이름.
	 */
	private void loadInputFile(String inputFile) {
		// TODO Auto-generated method stub
		try {
			//절대경로
			String path = Assembler.class.getResource("").getPath();
			// 효율적으로 입력하기 위해 입력 버퍼 생성
			BufferedReader bufReader = new BufferedReader(new FileReader(path+File.separator+inputFile));
			String line = "";
			while((line = bufReader.readLine()) != null) {
				lineList.add(line);
			}
			// .readLine()은 끝에 개행문자를 읽지 않는다.
			bufReader.close();
		}catch (FileNotFoundException e) {
		}catch(IOException e) {
		}
	}

	/** 
	 * pass1 과정을 수행한다.
	 *   1) 프로그램 소스를 스캔하여 토큰단위로 분리한 뒤 토큰테이블 생성
	 *   2) label을 symbolTable에 정리
	 *   
	 *    주의사항 : SymbolTable과 TokenTable은 프로그램의 section별로 하나씩 선언되어야 한다.
	 */
	private void pass1() {
		// TODO Auto-generated method stub
		SymbolTable symboltable = new SymbolTable();
		LiteralTable literaltable = new LiteralTable();
		TokenTable tokentable = new TokenTable(symboltable, literaltable, instTable);
		ArrayList<Token> tok = new ArrayList<Token>();
		// sect별로 몇 번째 명령어인지 저장한다.
		int locctr = 0;
		// sect별로 몇 번째 심볼테이블인지 저장한다.
		int symctr = 0;
		// 현재 주소
		int locator = 0;
		for(int i=0; i<lineList.size();i++) {
			String[] str = lineList.get(i).split("\t");
			// '.'으로 시작하는 경우 무시한다.
			if(str[0].compareTo(".")==0) {
				tokentable.putToken(lineList.get(i));
				locctr++;
				continue;
			}
			// CSECT가 나올 경우 새로운 sect이기 때문에 초기화한다.
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
			// ORG, LTORG인 경우 
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
			// 각 명령어의 주소를 구한다.
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
		// ORG를 완성된 토큰테이블에 추가한다.
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
	 * 작성된 SymbolTable들을 출력형태에 맞게 출력한다.
	 * @param fileName : 저장되는 파일 이름
	 */
	private void printSymbolTable(String fileName) {
		// TODO Auto-generated method stub
		try{
			//절대경로
			String path = Assembler.class.getResource("").getPath();
			// 효율적으로 입력하기 위해 입력 버퍼 생성
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
	 * 작성된 LiteralTable들을 출력형태에 맞게 출력한다.
	 * @param fileName : 저장되는 파일 이름
	 */
	private void printLiteralTable(String fileName) {
		// TODO Auto-generated method stub
		try{
			//절대경로
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
	 * pass2 과정을 수행한다.
	 *   1) 분석된 내용을 바탕으로 object code를 생성하여 codeList에 저장.
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
	 * 작성된 codeList를 출력형태에 맞게 출력한다.
	 * @param fileName : 저장되는 파일 이름
	 */
	private void printObjectCode(String fileName) {
		// TODO Auto-generated method stub
		try{
			//절대경로
			String path = Assembler.class.getResource("").getPath();
			PrintWriter pw = new PrintWriter(path+File.separator+fileName);
			for(int i=0; i<TokenList.size(); i++) {
				StringBuilder sb = new StringBuilder();
				int txtsize = 0;
				for(int j=0; j<TokenList.get(i).tokenList.size(); j++) {
					Token token = TokenList.get(i).tokenList.get(j);
					// Header 부분
					if(token.operator.compareTo("START")==0) {
						pw.format("H%-6s%06X%06X\n", token.label, token.location, TokenList.get(i).tokenList.get(TokenList.get(i).tokenList.size()-2).location);			
					}else if(token.operator.compareTo("CSECT")==0) {
						pw.format("H%-6s%06X%06X\n", token.label, token.location, TokenList.get(i).tokenList.get(TokenList.get(i).tokenList.size()-1).location+TokenList.get(i).tokenList.get(TokenList.get(i).tokenList.size()-1).byteSize);			
					}
					// Def 부분
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
					// Ref 부분
					else if(token.operator.compareTo("EXTREF")==0) {
						if(token.operand.length==3) {
							pw.format("R%-6s%-6s%-6s\n", token.operand[0], token.operand[1],token.operand[2]);
						}else if(token.operand.length==2) {
							pw.format("R%-6s%-6s\n", token.operand[0], token.operand[1]);
						}else if(token.operand.length==1) {
							pw.format("R%-6s\n", token.operand[0]);
						}
					}
					// Text 부분
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
				// Modification 부분
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
