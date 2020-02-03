package utils;

import org.apache.poi.hssf.usermodel.*;
import org.apache.poi.hssf.util.HSSFColor;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import play.Logger;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.*;

/**
 * Spreadsheet processing utility
 * 
 * @author joel
 *
 */
public class XLSUtil {

	private XLSUtil() {
	}
	
	private static HSSFCellStyle getHeaderStyle(HSSFWorkbook wb){
		HSSFCellStyle style = wb.createCellStyle();
        style.setBorderTop((short) 6); // double lines border
        style.setBorderBottom((short) 1); // single line border
        style.setFillBackgroundColor(HSSFColor.GREY_25_PERCENT.index);

        HSSFFont font = wb.createFont();
        font.setFontName(HSSFFont.FONT_ARIAL);
        font.setFontHeightInPoints((short) 20);
        font.setBoldweight(HSSFFont.BOLDWEIGHT_BOLD);
        font.setColor(HSSFColor.BLUE.index);
        style.setFont(font);
        return style;
	}
	
	/**
	 * Writes the given questions to the given output stream.
	 * 
	 * @param questions The list of XLSQuestion objects to export
	 * @param os The spreadsheet output stream.
	 */
	public static void exportQuestions( List<XLSQuestionOut> questions, OutputStream os) {
		if((os != null) && (questions != null)){
			HSSFWorkbook wb;
			try {
				wb = new HSSFWorkbook();
				HSSFSheet sheet = wb.createSheet("Questions");
				
				HSSFCellStyle headerStyle = getHeaderStyle(wb);
				
				List<String> columns = new ArrayList<String>();
				columns.add("ID");
				columns.add("Question");
				columns.add("Notes");
				columns.add("Tips");
				columns.add("Answers");
				columns.add("Category");
				columns.add("Difficulty");
				columns.add("Timing");
				columns.add("User");
				columns.add("Standard Score");
				columns.add("Creation Date");
				columns.add("Company");
				columns.add("Visibility");
				columns.add("Review Status");
				
				//Write column names in the first row
				HSSFRow headerRow = sheet.createRow(0);
				headerRow.setHeight((short)500);
				int column = 0;
				for (; column < columns.size(); ++column){
					HSSFCell cell = headerRow.createCell(column);
			        cell.setCellStyle(headerStyle);
					cell.setCellValue(columns.get(column));
				}
				
				//Write out the entire table
				int rowNum = 1;
				for(XLSQuestionOut question : questions){
					HSSFRow row = sheet.createRow(rowNum++);
					HSSFCell newCell;
					
					newCell = row.createCell(columns.indexOf("ID"));
					newCell.setCellValue(question.id);
					
					newCell = row.createCell(columns.indexOf("Question"));
					newCell.setCellValue(question.text);
					
					newCell = row.createCell(columns.indexOf("Tips"));
					newCell.setCellValue(question.tips);
					
					newCell = row.createCell(columns.indexOf("Answers"));
					newCell.setCellValue(question.answers);
					
					newCell = row.createCell(columns.indexOf("Category"));
					newCell.setCellValue(question.category);
					
					newCell = row.createCell(columns.indexOf("Difficulty"));
					newCell.setCellValue(question.difficulty);
					
					newCell = row.createCell(columns.indexOf("Timing"));
					newCell.setCellValue(question.time);
					
					newCell = row.createCell(columns.indexOf("User"));
					newCell.setCellValue(question.user);
					
					newCell = row.createCell(columns.indexOf("Standard Score"));
					newCell.setCellValue(question.standardScore);
					
					newCell = row.createCell(columns.indexOf("Creation Date"));
					newCell.setCellValue(question.creationDate);
					
					newCell = row.createCell(columns.indexOf("Company"));
					newCell.setCellValue(question.company);
					
					newCell = row.createCell(columns.indexOf("Visibility"));
					newCell.setCellValue(question.visibility);
					
					newCell = row.createCell(columns.indexOf("Review Status"));
					newCell.setCellValue(question.status);
				}
				wb.write(os);
				
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
		}
	}

	/**
	 * I Questions from the given spreadsheet input stream. Loops through all sheets.
	 * The first row in each sheet must contain valid column headers.
	 * 
	 * @param is Spreadsheet input stream.
	 * @return a list of XLSUtil.XLSQuestion objects
	 */
	public static List<XLSQuestionIn> importQuestions(InputStream is) {
		List<XLSQuestionIn> questions = new ArrayList<XLSQuestionIn>();
		if (is != null) {
			try {
				Workbook w = new HSSFWorkbook(is);
				int sheetNum = 0;
				Sheet sheet = w.getSheetAt(sheetNum);
				while (sheet != null) {
					Iterator<Row> rows = sheet.iterator();
					if (rows.hasNext()) {

						Map<QuestionColumn, Integer> columns = new HashMap<QuestionColumn, Integer>();
						// first row must be the column headers
						Row headers = rows.next();
						boolean textFound;
						int i = 0;
						do {// build the columns map first
							textFound = false;
							Cell cell = headers.getCell(i);
							if (cell != null) {
								String text = cell.getStringCellValue()
										.toLowerCase();
								if ((text != null) && (text.length() > 0)) {
									textFound = true;
                                    if (text.startsWith("question")) {
                                        columns.put(QuestionColumn.questions, i);
                                    } else if (text.contains("note")) {
                                        //The "notes" field on questions went away, but we still need
                                        //to be able to read old spreadsheets that contain the field
                                        //so that this information can be included elsewhere
                                        columns.put(QuestionColumn.notes, i);
                                    } else if (text.contains("tip")) {
                                        columns.put(QuestionColumn.tips, i);
                                    } else if (text.contains("answer")) {
                                        columns.put(QuestionColumn.answers, i);
                                    } else if (text.contains("difficult")) {
                                        columns.put(QuestionColumn.difficulty, i);
                                    } else if (text.contains("timing")) {
                                        columns.put(QuestionColumn.timing, i);
                                    } else if (text.contains("author")) {
                                        columns.put(QuestionColumn.user, i);
                                    } else if (text.contains("category")) {
                                        columns.put(QuestionColumn.category, i);
                                    }
								}
							}
							++i;
						} while (textFound);

						if (columns.get(QuestionColumn.questions) != null) {

							while (rows.hasNext()) {
								Row row = rows.next();
								Cell questionCell = row.getCell(columns
										.get(QuestionColumn.questions));
								if (questionCell != null) {
									XLSQuestionIn question = new XLSQuestionIn();
									question.text = questionCell.getStringCellValue();
                                                                        
                                                                        String possibleNotes = getCellAsString(row, columns.get(QuestionColumn.notes));
                                                                        String possibleTips = getCellAsString(row, columns.get(QuestionColumn.tips));

                                                                        String finalNotes;
                                                                        if (possibleNotes == null && possibleTips == null) {
                                                                            finalNotes = null;
                                                                        } else {
                                                                            finalNotes = (org.apache.commons.lang.StringUtils.defaultString(possibleTips) + "\n" + org.apache.commons.lang.StringUtils.defaultString(possibleNotes)).trim();
                                                                        }
                                                                        
									question.tips = finalNotes;
									
									question.answers = getCellAsString(row, columns.get(QuestionColumn.answers));
									
									question.category = getCellAsString(row, columns.get(QuestionColumn.category));

									question.difficulty = getCellAsString(row, columns.get(QuestionColumn.difficulty));
									
									question.time = getCellAsString(row, columns.get(QuestionColumn.timing));

                                    question.user = getCellAsString(row, columns.get(QuestionColumn.user));

									questions.add(question);
								} else {
									// As soon as we hit a row with no question
									// we are finished with the sheet
									break;
								}
							}
						}
					}
					try {
						sheet = w.getSheetAt(++sheetNum);
					} catch (Exception e) {
						sheet = null;
					}
				}

			} catch (Exception e) {
				Logger.error(
						"Unable to extract questions from spreadsheet. %s",
						e.getMessage());
			}
		}

		return questions;
	}
	
	private static String getCellAsString(Row row, Integer column){
		if (column != null) {
			Cell cell = row.getCell(column);
			if (cell != null) {
				return cell
						.getStringCellValue().trim();
			}
		}
		return null;
	}

	private enum QuestionColumn {
		questions, notes, tips, answers, category, difficulty, timing, user
	}
	
	public static class XLSQuestionIn {
		//For our tests to work these field names need to match the field names in the
		//Question entity
		
		public String text;
		public String tips;
		public String answers;
		public String category;
		public String difficulty;
		public String time;
        public String user;
	}
	
	public static class XLSQuestionOut {
		
		public String id;
		public String text;
		public String tips;
		public String answers;
		public String category;
		public String difficulty;
		public String time;
        public String user;
        public String standardScore;
        public String creationDate;
        public String company;
        public String visibility;
        public String status;
	}
}
