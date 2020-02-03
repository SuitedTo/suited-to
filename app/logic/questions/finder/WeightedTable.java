package logic.questions.finder;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

import logic.questions.finder.WeightedTable.Winner;

import org.apache.poi.hssf.usermodel.HSSFCell;
import org.apache.poi.hssf.usermodel.HSSFCellStyle;
import org.apache.poi.hssf.usermodel.HSSFFont;
import org.apache.poi.hssf.usermodel.HSSFRow;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.hssf.util.HSSFColor;
import org.apache.poi.ss.formula.functions.T;

import utils.LoggingUtil;




/**
 * 
 * This table provides a system of weighted cells.
 * 
 * Winners are chosen by moving across a row and picking
 * the question with the highest weight.
 * 
 * An idea of "promoting losers" is available to ensure that (other constraints permitting)
 * lower priority columns eventually get a win.
 * 
 * @author joel
 *
 */
public class WeightedTable <C,T> {
	private String dumpFile;
	private List<Winner<C,T>> initialWinners;
	private List<Column<C>> columns;
	private boolean fair;
	private List<Row> table;
	private ListStopper<C,T,List<Winner<C,T>>> stopper;
	
	
	/**
	 * @param stopper Tells the table when to stop building the
	 * list of winners
	 */
	public WeightedTable(final List<Winner<C,T>> initialWinners,
			final ListStopper<C,T,List<Winner<C,T>>> stopper,
			final String dumpFile){		
		table = new ArrayList<Row>();
		columns = new ArrayList<Column<C>>();
		
		if(initialWinners == null){
			this.initialWinners = new ArrayList<Winner<C,T>>();
		}else{
			this.initialWinners = new ArrayList<Winner<C,T>>(initialWinners);//shallow
			Collections.copy(this.initialWinners, initialWinners);//now deep
		}
		this.stopper = stopper;
		
		if (dumpFile != null) {
			this.dumpFile = dumpFile;
			deleteDumpFile();
		}
	}
	
	/**
	 * @param stopper Tells the table when to stop building the
	 * list of winners
	 * 
	 * @param fair A positive promotion weight applies a system
	 * of fairness to the process of collecting winners.
	 */
	public WeightedTable(final List<Winner<C,T>> initialWinners,
			final ListStopper<C,T,List<Winner<C,T>>> stopper,
			final boolean fair,
			final String dumpFile){
		
		this(initialWinners,stopper, dumpFile);
		this.fair = fair;
	}

	public final void addColumn(final C context, final List<Cell<T>> cells, final ListQualifier<C,T,List<Winner<C,T>>> qualifier){
		ListQualifier<C,T,List<Winner<C,T>>> stopper = (qualifier != null)?qualifier:
			new ListQualifier<C,T,List<Winner<C,T>>>(){

				@Override
				public boolean qualifies(final C context, final T applicant, final List<Winner<C,T>> list) {
					return true;
				}

		};
		
		if((context != null) && (cells != null) && (cells.size() > 0)){
			int column = lastColumn() + 1;
			int row = 0;
			for (int i = 0; i < cells.size(); ++i){
				setCell(row++, column, cells.get(i));
			}
			
			columns.add(new Column(context, stopper));
		}
	}
	
	public final List<T> getWinners(){

		List<Winner<C,T>> allWinners = initialWinners;
		
		List<Cell<T>> winnerCells = new ArrayList<Cell<T>>();//for debugging
		
		List<T> winners = new ArrayList<T>();
		
		long flTime = 0;
		long wpTime = 0;
		
		int i = 0;
		Row<T> row = getRow(i);
		int previousWinner = 0;
		while(row != null){			
			
			long startFL = System.currentTimeMillis();
			//Disqualify duplicates and any cells that the column qualifier doesn't like
			for(int column = firstColumn(); column <= lastColumn(); ++column){
				C context = columns.get(column).getContext();
				ListQualifier<C,T,List<Winner<C,T>>> qualifier = columns.get(column).getQualifier();
				Cell<T> cellObj = row.get(column);
				
				if (!(cellObj == null) &&
						!cellObj.isPlaceholder() &&
						(allWinners.contains(cellObj) ||
						!qualifier.qualifies(context,cellObj.value,allWinners))){
					row.get(column).disqualify();
				}
			}
			long endFL = System.currentTimeMillis();
			flTime += (endFL - startFL);
			
			long startWP = System.currentTimeMillis();
			List<Integer> winnerPositions = row.getWinnerPositions();
			
			long endWP = System.currentTimeMillis();
			wpTime += (endWP - startWP);
			
			//It's possible that all cells disqualified themselves from this pass and
			//we have no winners
			if (winnerPositions.size() > 0){
				int winnerIndex = selectNextWinnerIndex(previousWinner,winnerPositions);
				Cell<T> winner = row.get(winnerPositions.get(winnerIndex));
				C context = columns.get(winnerPositions.get(winnerIndex)).getContext();
				
				//As soon as the global stopper rejects a value
				//we stop building the list of winners
				if (!winner.isDisqualified() && !stopper.qualifies(null,winner.value, allWinners)){
					break;
				}

				winnerCells.add(winner);
			
				if(!winner.isDisqualified()){
					winners.add(winner.value);
					allWinners.add(new Winner(context,winner));
				}
				
				if(fair){
					promoteLosers(i, winnerPositions.get(winnerIndex));
				}
				previousWinner = winnerPositions.get(winnerIndex);
				
			}else{
				if (play.Play.mode == play.Play.Mode.DEV) {
					winnerCells.add(new Winner(null,null,null,0));
				}
			}
			
			if (dumpFile != null) {
				dumpSnapShot("Step " + (i+1), winnerCells);
			}
			
			
			row = getRow(++i);
		}
		
		System.out.println("wpTime: " + wpTime + "     flTime:" + flTime);
		
		return winners;
	}
	
	private static int selectNextWinnerIndex(final int previousWinner, final List<Integer> winnerPositions){
		if((winnerPositions == null) || (winnerPositions.size() <= 1) || (!winnerPositions.contains(previousWinner))) {
			return 0;
		}
			
		return (previousWinner + 1) % (winnerPositions.size() - 1);
	}
	
	/**
	 * 
	 * @param row
	 */
	private void promoteLosers(final int row, final int winnerColumn){
		Row rowObj = getRow(row);
		if (rowObj != null){
			Row promotedLosers = new Row<T>();
			for(int column = firstColumn(); column <= lastColumn(); ++ column){
				Cell<T> cell = rowObj.get(column);
				if ((winnerColumn != column) && (cell != null) && !cell.isDisqualified()){
					promotedLosers.add(cell.copy().addToWeight(1));
				}else{
					promotedLosers.add(new Cell<T>());
				}
			}
			if(row >= table.size()){
				table.add(promotedLosers);
			}else{
				table.add(row + 1, promotedLosers);
			}
			collapsePlaceHolderCells(row + 1);
		}
	}
	
	/**
	 * To collapse a cell is to delete it and move all of the cells
	 * beneath it up to fill the void. So we shrink the column to fill
	 * the void instead of shrinking the row.
	 * 
	 * @param row The row that contains the cells that are to be collapsed.
	 */
	public final void collapsePlaceHolderCells(final int row){
		int currentRow = row;
		int tableSize = table.size();
		while (currentRow < (tableSize - 1)){
			Row rowObj = table.get(currentRow);
			Row replacementRow = new Row<T>();
			int columns = rowObj.asList().size();
			for (int i = 0; i < columns; ++i){
				Cell cell = rowObj.get(i);
				if ((cell != null) && cell.isPlaceholder()){
					replacementRow.add(table.get(currentRow + 1).get(i));
					table.get(currentRow + 1).insert(i, new Cell<T>());
				}else{
					replacementRow.add(table.get(currentRow).get(i));
				}
			}
			table.set(currentRow, replacementRow);
			++currentRow;
		}

	}

	public final void setCell(final int row, final int column, final Cell<T> cell){
		Row<T> rowObj = getRow(row);
		if(rowObj == null){
			rowObj = add(new Row<T>());
		}
		rowObj.insert(column, cell);
	}

	public final Cell<T> getCell(final int row, final int column){
		if (column <= lastColumn()){
			try{
				Row<T> rowObj= table.get(row);
				return rowObj.get(column);
			}catch(IndexOutOfBoundsException e){
				
			}
		}
		return null;
	}
	
	public final Row<T> add(final Row<T> row){
		table.add(row);
		return row;
	}
	
	public final Row<T> getRow(final int row){
		if(row > (table.size() - 1)){
			return null;
		}
		return table.get(row);
	}
	
	private int firstColumn(){
		return 0;
	}
	
	private int lastColumn(){
		return columns.size() - 1;
	}
	
	
	/**
	 * Just holds column metadata right now
	 * @author joel
	 *
	 */
	private class Column<C>{
		private C context;
		private ListQualifier<C,T,List<Winner<C,T>>> qualifier;
		
		public Column(final C context, final ListQualifier<C,T,List<Winner<C,T>>> qualifier){
			this.context = context;
			this.qualifier = qualifier;
		}

		public C getContext() {
			return context;
		}

		public ListQualifier<C,T,List<Winner<C,T>>> getQualifier() {
			return qualifier;
		}
	}
	
	
	/**
	 * 
	 * A row in the table contains cells
	 * 
	 * @author joel
	 *
	 * @param <T>
	 */
	public static class Row <T>{
		private List<Cell<T>> row;
		
		public Row(){
			row = new ArrayList<Cell<T>>();
		}
		
		public Row (final List<Cell<T>> row){
			this.row = row;
		}
		
		public final List<Cell<T>> getIdenticalCells(final Cell<T> cell){
			List<Cell<T>> results = new ArrayList<Cell<T>>();
			Iterator<Cell<T>> it = row.iterator();
			while(it.hasNext()){
				Cell<T> next = it.next();
				if(!cell.isDisqualified() && cell.equals(next)){
					results.add(next);
				}
			}
			return results;
		}
		
		/**
		 * 
		 * @return A list of winner positions. Note that if two *different*
		 * Cells have the same weight then the first one encountered is the
		 * winner. This method returns a list because if two *identical* cells
		 * have the same weight then they are both winners.
		 */
		public final List<Integer> getWinnerPositions(){
			List<Integer> winners = new ArrayList<Integer>();
			
			
			//If multiple identical cells are found in this row then
			//each of them should be assigned a weight that is the sum of all
			//of their weights. Could be more efficient but doesn't matter too
			//much because we're going across a row
			List<Cell<T>> realized = new ArrayList<Cell<T>>();
			for (int cell = firstCell(); cell <= lastCell(); ++ cell){
				Cell cellObj = get(cell);
				if ((cellObj != null) && (!cellObj.isPlaceholder()) && !realized.contains(cellObj)){
					List<Cell<T>> duplicates = getIdenticalCells(cellObj);
					int totalWeight = 0;
					for(Cell<T> duplicate : duplicates){
						totalWeight += duplicate.getWeight();
					}
					for(Cell<T> duplicate : duplicates){
						
						duplicate.setWeight(totalWeight);
					}
					realized.add(cellObj);
				}
			}

			
			int maxWeight = Integer.MIN_VALUE;
			//first find the max weight value.			
			for (int cell = firstCell(); cell <= lastCell(); ++ cell){
				
				Cell cellObj = get(cell);
				if ((cellObj != null) && (!cellObj.isPlaceholder()) && !cellObj.isDisqualified()){
					int weight = cellObj.getWeight();
					maxWeight = (maxWeight < weight)?weight:maxWeight;
				}
			}
			
			
			if (maxWeight > Integer.MIN_VALUE){			
				String winnerId = null;
				for (int cell = firstCell(); cell <= lastCell(); ++ cell){
					Cell cellObj = get(cell);
					if ((cellObj != null) && !cellObj.isPlaceholder() && !cellObj.isDisqualified()){
						if (cellObj.getWeight() == maxWeight){
							if (winnerId == null){
								winnerId = cellObj.getIdentifier();
							}
							if(cellObj.getIdentifier().equals(winnerId)){
								winners.add(cell);
							}
						}
					}
				}
			}
			return winners;
		}
		
		public final Cell<T> get(final int column){
			if ((column >= firstCell()) && (column <= lastCell())){
				return row.get(column);
			}
			return null;
		}
		
		public final Cell<T> add(final Cell<T> cell){
			row.add(cell);
			return cell;
		}
		
		public final Cell<T> insert(final int column, final Cell<T> cell){
			try{
				row.set(column, cell);
			}catch(IndexOutOfBoundsException e){
				while(row.size() < column){
					row.add(new Cell<T>());
				}
				row.add(cell);
			}
			return cell;
		}
		
		public final List<Cell<T>> asList(){
			return row;
		}
		
		public final int size(){
			return row.size();
		}
		
		private int firstCell(){
			return 0;
		}
		
		private int lastCell(){
			return row.size() - 1;
		}
	}
	
	/**
	 * A cell that has made it into the winners list.
	 * 
	 * @author joel
	 *
	 * @param <C> context.
	 * @param <T> value
	 */
	public static class Winner<C,T> extends Cell<T>{
		private C context;
		
		public Winner(final C context, final Cell<T> cell){
			this(context, cell.identifier, cell.value, cell.weight);
		}
		
		public Winner(final C context, final String identifier, final T value, final int weight){
			super(identifier, value, weight);
			this.context = context;
		}

		public final C getContext() {
			return context;
		}

		public final void setContext(final C context) {
			this.context = context;
		}
		
		
	}
	
	
	/**
	 * 
	 * A cell is weighted. The identifier needs to identify the value uniquely.
	 * 
	 * @author joel
	 *
	 * @param <T>
	 */
	public static class Cell <T>{

		private String identifier;
		private T value;
		private int weight;
		private boolean disqualified;

		public Cell (final String identifier, final T value, final int weight){
			this.identifier = identifier;
			this.value = value;
			this.weight = weight;
		}
		
		public final Cell<T> copy(){
			return new Cell<T>(identifier, value, weight);
		}

		public Cell() {
		}
		
		public final boolean isPlaceholder(){
			return identifier == null;
		}

		public final String getIdentifier() {
			return identifier;
		}
		
		public final boolean isDisqualified(){
			return disqualified;
		}
		
		public final void disqualify(){
			disqualified = true;
		}

		public final void setIdentifier(final String identifier) {
			this.identifier = identifier;
		}

		public final T getValue() {
			return value;
		}

		public final Cell setValue(final T value) {
			this.value = value;
			return this;
		}

		public final int getWeight() {
			return weight;
		}
		
		public final Cell addToWeight(final int extraWeight){
			weight += extraWeight;
			return this;
		}

		public final void setWeight(final int weight) {
			this.weight = weight;
		}

		@Override
		public final int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result
					+ ((identifier == null) ? 0 : identifier.hashCode());
			return result;
		}

		@Override
		public final boolean equals(final Object obj) {
			if (this == obj) {
				return true;
			}
			if (obj == null) {
				return false;
			}
			if (getClass() != obj.getClass()) {
				return false;
			}
			Cell other = (Cell) obj;
			if (identifier == null) {
				if (other.identifier != null) {
					return false;
				}
			} else if (!identifier.equals(other.identifier)) {
				return false;
			}
			return true;
		}
	}
	
	private void deleteDumpFile(){
		new File(dumpFile).delete();
	}
	
	private static HSSFCellStyle getHeaderStyle(final HSSFWorkbook wb){
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
	
	private void dumpSnapShot(final String snapshotName, final List<Cell<T>> winners){
		final String fileName = dumpFile;
		InputStream is;
		
		try{
			is = new FileInputStream(fileName);
		}catch(Exception e){
			try {
				HSSFWorkbook wb = new HSSFWorkbook();
				FileOutputStream fileOut = new FileOutputStream(fileName);
				wb.write(fileOut);
				fileOut.close();
				
				is = new FileInputStream(fileName);
				
			} catch (IOException ioe) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				return;
			}
		}
		
		HSSFWorkbook wb;
		try {
			wb = new HSSFWorkbook(is);
			HSSFSheet sheet = wb.createSheet(snapshotName);
			
			HSSFCellStyle headerStyle = getHeaderStyle(wb);
			
			//Write column names in the first row
			HSSFRow headerRow = sheet.createRow(0);
			headerRow.setHeight((short)500);
			int column = 0;
			for (; column < columns.size(); ++column){
				HSSFCell cell = headerRow.createCell(column);
		        cell.setCellStyle(headerStyle);
				cell.setCellValue(columns.get(column).getContext().toString());
			}
			
			//Header for winner
			HSSFCell winnerHeaderCell = headerRow.createCell(column);
			winnerHeaderCell.setCellStyle(headerStyle);
			winnerHeaderCell.setCellValue("*Winner");
			
			//Write out the entire table
			int rowNum = 1;
			for(Row row : table){
				HSSFRow winnerRow = sheet.createRow(rowNum++);
				List<Cell<T>> cells = row.asList();
				int columnNum = 0;
				for(Cell<T> cell : cells){
					
					HSSFCell newCell = winnerRow.createCell(columnNum++);
					if((cell != null) && (!cell.isPlaceholder())){
						String txt = cell.isDisqualified()?"X":String.valueOf(cell.getWeight());
						newCell.setCellValue(cell.getIdentifier() + " (" + txt + ")");
					}
				}
			}
			
			
			//Write out the winners column
			for(int i = 1; i <= winners.size(); ++i){
				HSSFRow winnerRow = sheet.getRow(i);
				HSSFCell winnerCell = winnerRow.createCell(column);
				Cell<T> winner = winners.get(i-1);
				if(winner.isPlaceholder()){
					winnerCell.setCellValue("-");
				}else{
					String txt = winner.isDisqualified()?"X":String.valueOf(winner.getWeight());
					winnerCell.setCellValue(winner.getIdentifier() + " (" + txt + ")");
				}
			}
			
			
			FileOutputStream out = new FileOutputStream(fileName);
			wb.write(out);
			
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		
	}
}
