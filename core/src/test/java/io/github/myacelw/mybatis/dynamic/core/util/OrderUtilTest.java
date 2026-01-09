package io.github.myacelw.mybatis.dynamic.core.util;

import org.junit.jupiter.api.Test;

import java.util.List;


class OrderUtilTest {

    @Test
    void test() {
        System.out.println(OrderUtil.getFirstOrderString());
        System.out.println(OrderUtil.getFirstOrderString());
        System.out.println("===");
        System.out.println(OrderUtil.getLastOrderString());
        System.out.println(OrderUtil.getLastOrderString());
    }


    @Test
    void test2() {
        System.out.println(OrderUtil.getMiddleOrderString("100", "200"));
        System.out.println(OrderUtil.getMiddleOrderString(null, "200"));
        System.out.println(OrderUtil.getMiddleOrderString("100", null));
        System.out.println(OrderUtil.getMiddleOrderString(null, null));

    }


    @Test
    void testToStrings() {
        List<String> firstList = OrderUtil.getFirstOrderStrings(3);
        for (String r : firstList){
            System.out.println(r);
        }

        System.out.println("===");

        List<String> lastList = OrderUtil.getLastOrderStrings(3);
        for (String r : lastList){
            System.out.println(r);
        }
    }

    @Test
    void getMiddleOrderNumbers() {
        String a = OrderUtil.getLastOrderString();
        String b = OrderUtil.getLastOrderString();

        System.out.println(a);
        System.out.println(b);
        System.out.println("===");

        List<String> result = OrderUtil.getMiddleOrderStrings(a, b, 4);

        for (String r : result){
            System.out.println(r);
        }
    }

    @Test
    void getMiddleOrderStrings() {
        String a = OrderUtil.getLastOrderString();
        String b = OrderUtil.getLastOrderString();

        System.out.println(a);
        System.out.println(b);
        System.out.println("===");

        List<String> result = OrderUtil.getMiddleOrderStrings(a, b, 4);

        for (String r : result){
            System.out.println(r);
        }
    }

}