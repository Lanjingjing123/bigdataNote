package csii.work;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class File {

	@SuppressWarnings("resource")
	public static void main(String[] args) throws IOException {
		String filePath = "C:\\\\Users\\\\ljj\\\\Desktop\\dic-dataware.txt";
		// 获取dataware中的字段
		String[] validArrData = dataWareFile(filePath);

		// 获取xml中的字段
		filePath = "C:\\Users\\ljj\\Desktop\\sql配置\\pub.xml";
		String[] validArrMm = xmlFile(filePath);

		for (int i = 0; i < validArrMm.length; i++) {// 将mm.xml中的字段做外层循环，
			// 判断dataWare 里面是否有此字段的标志位
			int existflg = 0;
			for (int j = 0; j < validArrData.length; j++) {// dataware 做内层循环,讲mm.xml每一个字段与dataware的每一个字段做对比
				String mm = validArrMm[i];
				String data = validArrData[j];
				if (mm.equals(data)) {// 存在置existflg=1
					existflg = 1;
					break;
				}
			}
			if (existflg == 1) {// 存在，开始下一次循环
				continue;
			} else {// 不存在,打印，开始下一次循环
				System.out.println(validArrMm[i]);

			}
		}
	}

	/**
	 * 返回有效字段数组(Xml)
	 * 
	 * @param filePath
	 * @return validArr
	 * @throws IOException
	 */
	@SuppressWarnings("resource")
	private static String[] xmlFile(String filePath) throws IOException {

		FileReader fileReader = new FileReader(filePath);
		BufferedReader bufferedReader = new BufferedReader(fileReader);
		String str = "";
		int i = 0;
		String reg = "(name=\\\")([a-zA-Z0-9]+)..(type=\")[a-zA-Z]+\\\".(desc)";
		Pattern pattern = Pattern.compile(reg);

		// 存取有效数据 长度需要手动改动
		int length = xmlFileRows(filePath);
		String[] validArr = new String[length];
		while ((str = bufferedReader.readLine()) != null) {
			Matcher matcher = pattern.matcher(str);
			if (matcher.find()) {
				// 匹配出相应字段，并且变成大写
				String temp = matcher.group(2).toUpperCase();
				validArr[i++] = temp;
				// 测试读取的字段
//				System.out.println(i+":"+temp);
			}

		}
		return validArr;

	}

	/**
	 * 返回有效字段的数组形式(dataWare)
	 * 
	 * @param filePath
	 * @return validArr
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
	 * xml文件有效行数
	 * 
	 * @param filePath
	 * @return fileRows
	 * @throws IOException
	 */
	@SuppressWarnings("resource")
	private static int xmlFileRows(String filePath) throws IOException {
		FileReader fi = new FileReader(filePath);
		BufferedReader bf = new BufferedReader(fi);
		int fileRows = 0;
		String regex = "(name=\")([a-zA-Z0-9]+)..(type=\")[a-zA-Z]+\".(desc)";
		// 1.解析正则表达式
		Pattern pa = Pattern.compile(regex);

		String str = "";
		while ((str = bf.readLine()) != null) {// 读取一行不为空
			// 2.进行正则处理
			Matcher matcher = pa.matcher(str);
			if (matcher.find()) {// 3.找到匹配正则的字段
				fileRows++;
			}
		}
		System.out.println("xml有效行数：" + fileRows);
		return fileRows;
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
