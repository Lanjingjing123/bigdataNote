package com.csii.excel;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.Iterator;

import org.apache.poi.hssf.usermodel.HSSFCell;
import org.apache.poi.hssf.usermodel.HSSFRow;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.poifs.filesystem.POIFSFileSystem;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFCell;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

public class ExcelRead {
	public String[] arrRow ;
	private static XSSFRow row;
	@SuppressWarnings("unused")
	public static void main(String[] args) throws IOException {
		String fileName = "C:\\Users\\ljj\\Desktop\\testExcel\\wc.xlsx";
		String[] arrExcel = readExcel(fileName);
		int i = 0;
		for (String string : arrExcel) {
			i++;
			System.out.println(i+":"+string);
		}
		
		
		/* 获取dataWare里面的字段*/
		
		fileName = "C:\\Users\\ljj\\Desktop\\dic-dataware.txt";
		String dataArr[] =  dataWareFile(fileName);
		i=0;
		for (String string : dataArr) {
			i++;
			System.out.println(i+":"+string);
		}
	}
	
	
	/**
	 * 返回一个字段数组
	 * @param fileName
	 * @return String[]
	 * @throws IOException
	 */
	@SuppressWarnings("unused")
	public static String[] readExcel(String fileName) throws IOException {

		File file = new File(fileName);
		if(file==null){
			System.out.println("读取文件失败！");
		}
		
		FileInputStream fileInputStream = new FileInputStream(file);
		// 创建excel对象
		XSSFWorkbook xssfWorkbook = new XSSFWorkbook(fileInputStream);
		if(file.exists()&&file.isFile()) {
			System.out.println("open file succeed!");
		}else {
			System.out.println("Error to open openworkbook.xlsx file!");
		}
		// 获取第一行
		XSSFSheet xssfSheet = xssfWorkbook.getSheetAt(0);
		Iterator <Row> rowIterator = xssfSheet.iterator();
		// 字段名
		String strName = "";
		// 级字段数
		int i = 0;
		// 存字段数组
		String[] arr = new String[24];
		while(rowIterator.hasNext()) {
			row = (XSSFRow) rowIterator.next();
			Iterator<Cell> cellIterator = row.cellIterator();
			while(cellIterator.hasNext()) {
				Cell cell = cellIterator.next();
				switch (cell.getCellType()) {
				// string 类型
				case STRING:
					
					strName = cell.getStringCellValue();
					strName = strName.trim().toUpperCase();
					arr[i++] = strName;
//					System.out.println(i+":"+strName);
					break;
				// number类型
				case NUMERIC:
					
					strName = String.valueOf(cell.getNumericCellValue());
					strName =  strName.trim().toUpperCase();
						arr[i++] = strName;
//					System.out.println(i+":"+strName);
					break;
				default:
					break;
				}
			}
		}
		return arr;
	}

	
	
	/**
	 * 返回有效字段的数组形式(dataWare)
	 * 
	 * @param filePath
	 * @return String[]
	 * @throws IOException
	 */
	@SuppressWarnings({ "unused", "resource" })
	private static String[] dataWareFile(String filePath) throws IOException {
		FileReader fileReader = new FileReader(filePath);
		BufferedReader bf = new BufferedReader(fileReader);
		// 获取dataWare 文件有效行数
		int length = dataWareFileRows(filePath);
		// 有效值数组
		String[] validArr = new String[length];

		int i = 0;
		String str = "";
		while ((str = bf.readLine()) != null) { // 按行读取
			// 按 逗号分离
			String[] arr = str.split(",");
			String validStr = arr[1];
			String type = arr[2];
			// 按 '_' 分割
			if (validStr.split("_").length > 1) {// 有含有"_"的字段，去掉"_",存入有效数组中
				validArr[i++] = validStr.replace("_", "");
			} else {
				validArr[i++] = validStr;
			}
		}
		return validArr;

	}	
	
	
	/**
	 * 返回dataWare 有效行数
	 * 
	 * @param filePath
	 * @return
	 * @throws IOException
	 */
	@SuppressWarnings("resource")
	private static int dataWareFileRows(String filePath) throws IOException {
		// 有效长度
		int fileRows = 0;
		FileReader fileReader = new FileReader(filePath);
		BufferedReader bf = new BufferedReader(fileReader);

		while (bf.readLine() != null) {
			fileRows++;
		}
		return fileRows;
	}
}
