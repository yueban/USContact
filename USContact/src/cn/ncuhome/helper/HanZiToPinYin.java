package cn.ncuhome.helper;

import net.sourceforge.pinyin4j.PinyinHelper;
import net.sourceforge.pinyin4j.format.HanyuPinyinCaseType;
import net.sourceforge.pinyin4j.format.HanyuPinyinOutputFormat;
import net.sourceforge.pinyin4j.format.HanyuPinyinToneType;
import net.sourceforge.pinyin4j.format.HanyuPinyinVCharType;
import net.sourceforge.pinyin4j.format.exception.BadHanyuPinyinOutputFormatCombination;

public class HanZiToPinYin {
	// 将汉字转换成拼音
	public static String toPinYin(String hanzhis) {
		CharSequence s = hanzhis;

		char[] hanzhi = new char[s.length()];
		for (int i = 0; i < s.length(); i++) {
			hanzhi[i] = s.charAt(i);
		}

		char[] t1 = hanzhi;
		String[] t2 = new String[s.length()];
		/** */
		/**
		 * 设置输出格式
		 */
		HanyuPinyinOutputFormat format = new HanyuPinyinOutputFormat();
		format.setCaseType(HanyuPinyinCaseType.UPPERCASE);
		format.setToneType(HanyuPinyinToneType.WITHOUT_TONE);
		format.setVCharType(HanyuPinyinVCharType.WITH_V);

		int t0 = t1.length;
		String py = "";
		try {
			for (int i = 0; i < t0; i++) {
				t2 = PinyinHelper.toHanyuPinyinStringArray(t1[i], format);
				py = py + t2[0].toString();
			}
		} catch (BadHanyuPinyinOutputFormatCombination e1) {
			e1.printStackTrace();
		}
		return py;
	}
}