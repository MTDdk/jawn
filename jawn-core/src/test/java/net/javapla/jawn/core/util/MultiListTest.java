package net.javapla.jawn.core.util;

import static com.google.common.truth.Truth.assertThat;

import java.util.Arrays;

import org.junit.Test;

public class MultiListTest {


    @Test
    public void mapToList() {
        
        MultiList<String> list = new MultiList<>();
        
        list.put("a", Arrays.asList("1","11","111"));
        list.put("b", Arrays.asList("2","22","222"));
        
        MultiList<Integer> list2 = list.map(Integer::valueOf);
        
        assertThat(list2.size()).isEqualTo(list.size());
        assertThat(list2.list("a")).containsExactly(1,11,111);
        assertThat(list2.list("b")).containsExactly(2,22,222);
    }

}
