package framework;

import lombok.*;
import lombok.experimental.Accessors;

import java.util.ArrayList;

public class LombokTest {
    //@Accessors(fluent = true, chain = true)
    @Getter
    @Setter
    @AllArgsConstructor
    @NoArgsConstructor
    @ToString
    @Accessors(fluent = true, chain = true)
    public static class TestClass {
        private int value;
        private String name;

        @Singular
        private ArrayList<String> tags = new ArrayList<>();
    }

    public static void main(String[] args) {
        if (args.length == 0) {
            args = new String[]{"Hello", "World"};
        }
        TestClass testClass = new TestClass();
        for (String arg : args) {
            ArrayList<String> arrayList = new ArrayList<>();
            arrayList.add(arg);
            testClass.value(testClass.value + 1).name(testClass.name + "1").tags(arrayList);
        }
        //TODO: Did not work
        System.out.println(testClass);
    }
}
