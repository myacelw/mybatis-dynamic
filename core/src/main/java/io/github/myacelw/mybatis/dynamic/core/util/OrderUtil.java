package io.github.myacelw.mybatis.dynamic.core.util;

import io.github.myacelw.mybatis.dynamic.core.util.sequence.Sequence;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * 排序序号工具类。
 * 用于有序的数据库表行的序号生成，当新增或移动数据时，使用该工具生成所需新增或移动数据行的序号，而无需修改其他行数据的序号。
 * <b>
 * getFirstOrderString() 可以得到最前面的排序序号，用于插入最前面的数据。
 * getLastOrderString() 可以得到最后的排序序号，用于插入最后的数据。
 * getMiddleOrderString() 可以得到两个序号之间的中间序号，用于插入两个序号之间的数据。
 *
 * @author liuwei
 */
public class OrderUtil {
    /**
     * 排序序号的间隔，用于保证Last排序序号有足够的间隔插入数据。
     */
    private static final BigInteger INTERVAL = BigInteger.valueOf(2L << 60);

    /**
     * 序号长度，目前为25位
     */
    public static final int LENGTH = getLastOrderNumber().toString(Character.MAX_RADIX).length();

    /**
     * 获取最前面的排序序号
     */
    private static BigInteger getFirstOrderNumber() {
        return BigInteger.valueOf(Sequence.getInstance().nextId() * -1L).add(BigInteger.valueOf(Long.MAX_VALUE)).multiply(INTERVAL);
    }

    /**
     * 获取最后的排序序号
     */
    private static BigInteger getLastOrderNumber() {
        return BigInteger.valueOf(Sequence.getInstance().nextId()).add(BigInteger.valueOf(Long.MAX_VALUE)).multiply(INTERVAL);
    }

    /**
     * 获取最后的排序序号字符串
     */
    public static String getFirstOrderString() {
        return toString(getFirstOrderNumber());
    }

    /**
     * 批量获取最前面的排序序号字符串
     */
    public static List<String> getFirstOrderStrings(int n) {
        String[] result = new String[n];
        for (int i = n - 1; i >= 0; i--) {
            result[i] = getFirstOrderString();
        }
        return Arrays.asList(result);
    }


    /**
     * 获取最后的排序序号字符串
     */
    public static String getLastOrderString() {
        return toString(getLastOrderNumber());
    }

    /**
     * 批量获取最后的排序序号字符串
     */
    public static List<String> getLastOrderStrings(int n) {
        List<String> result = new ArrayList<>();
        for (int i = 0; i < n; i++) {
            result.add(getLastOrderString());
        }
        return result;
    }

    /**
     * 获取中间的排序序号字符串
     * next为空时移动到最后，previous为空时移动到最前，都为空时移动到最后面。
     *
     * @param previous 前一个序号
     * @param next     后一个序号
     */
    public static String getMiddleOrderString(String previous, String next) {
        BigInteger nextNum = toBigInteger(next);
        if (nextNum == null) {
            return getLastOrderString();
        }

        BigInteger previousNum = toBigInteger(previous);
        if (previousNum == null) {
            return getFirstOrderString();
        }

        BigInteger v = nextNum.subtract(previousNum).divide(BigInteger.valueOf(2)).add(previousNum);
        return toString(v);
    }

    /**
     * 批量获取中间的排序序号字符串
     *
     * @param previous 前一个序号
     * @param next     后一个序号
     * @param n        需要生成的中间序号的个数
     */
    public static List<String> getMiddleOrderStrings(String previous, String next, int n) {
        BigInteger nextNum = toBigInteger(next);
        if (nextNum == null) {
            return getLastOrderStrings(n);
        }

        BigInteger previousNum = toBigInteger(previous);
        if (previousNum == null) {
            return getFirstOrderStrings(n);
        }

        List<String> result = new ArrayList<>(n);
        BigInteger sep = nextNum.subtract(previousNum).divide(BigInteger.valueOf(n + 1));
        BigInteger v = previousNum;
        for (int i = 0; i < n; i++) {
            v = v.add(sep);
            result.add(toString(v));
        }
        return result;
    }

    /**
     * 排序序号大整数转换为字符串，25位，不足25位开头补0
     *
     * @param orderNumber 排序序号大整数
     * @return 排序序号字符串
     */
    private static String toString(BigInteger orderNumber) {
        String s = orderNumber.toString(Character.MAX_RADIX);
        //序号字符串长度小于指定长度时前补0
        if (s.length() < LENGTH) {
            return "000000000000000000000000000000".substring(0, LENGTH - s.length()) + s;
        }
        return s;
    }

    /**
     * 排序序号字符串转换为大整数
     *
     * @param orderNumber 排序序号字符串
     * @return 排序序号大整数
     */
    private static BigInteger toBigInteger(String orderNumber) {
        if (isEmpty(orderNumber)) {
            return null;
        }
        return new BigInteger(orderNumber, Character.MAX_RADIX);
    }

    private static boolean isEmpty(String next) {
        return next == null || next.isEmpty();
    }

}
